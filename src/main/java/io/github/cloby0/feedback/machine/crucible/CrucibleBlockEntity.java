/*
 * Feedback -- a Minecraft technology mod.
 * Copyright (C) 2026 soundgoodizerfan
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Assets under src/main/resources/assets are NOT covered by this licence.
 * See LICENSE-ASSETS.
 */
package io.github.cloby0.feedback.machine.crucible;

import java.util.List;
import java.util.Optional;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.thermal.Heat;
import io.github.cloby0.feedback.core.thermal.HeatSource;
import io.github.cloby0.feedback.core.thermal.ItemHeat;
import io.github.cloby0.feedback.core.thermal.ThermalBody;
import io.github.cloby0.feedback.core.unit.Conductance;
import io.github.cloby0.feedback.core.unit.ThermalMass;
import io.github.cloby0.feedback.core.unit.Tu;
import io.github.cloby0.feedback.core.unit.TuRate;
import io.github.cloby0.feedback.process.ThermalProcess;
import io.github.cloby0.feedback.process.ThermalProcessTable;
import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.registry.FBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A vessel that was designed to be measured.
 *
 * <h2>Why it exists, and why it is worse</h2>
 * The slice's best thermal vessel is the sealed blast furnace, and everything good about it
 * follows from being closed -- which is also why an instrument cannot be got at the inside of it.
 * The player's best vessel and their first instrument are mutually exclusive and no amount of
 * iron fixes it. The way out is not a better furnace; it is a vessel with a hole in it.
 * <p>
 * So the crucible is <b>not an upgrade</b>. It is slower and clumsier than the thing it does not
 * replace, and it is the only one of the two that can ever be part of a loop. What the player
 * buys with it is the option to walk away, which is philosophy 7's possible/reliable/economical
 * distinction arriving as something they feel rather than read.
 *
 * <h2>It knows nothing about steel</h2>
 * The crucible holds items at whatever temperature its fire and its own mass produce. What
 * happens to them is a property of the materials, looked up in {@link ThermalProcessTable}, and
 * the crucible never asks what it can make -- it has no answer. Leave iron in past its window and
 * the vessel does not stop, so the iron burns: the same machine-does-not-know-when-to-stop as the
 * hammer, in the second energy type.
 *
 * <h2>Mass is the whole of the upgrade</h2>
 * The two sizes differ by one number, {@link #getThermalMass()}, and everything the player
 * notices follows from it -- how fast it climbs, how far a twenty-tick-old reading has drifted,
 * whether it can satisfy a process's heating-rate limit at all. Insulation is a second number
 * arrived at by stacking blocks against it. Neither is "+50% anything"; both are things you could
 * point at.
 */
public class CrucibleBlockEntity extends BlockEntity implements ThermalBody {

    /** How often the vessel looks around to see what has been built against it. */
    private static final int INSULATION_RECHECK_INTERVAL = 20;

    private final NonNullList<ItemStack> contents =
            NonNullList.withSize(FTuning.CRUCIBLE_SLOTS, ItemStack.EMPTY);

    /**
     * Stored as a primitive, and wrapped only at the accessor. A {@link Tu} in a field would
     * outlive the method that made it, which is exactly the case the JIT's escape analysis cannot
     * eliminate -- so it would be a real allocation, held for the lifetime of the block entity,
     * buying nothing the accessor's return type does not already buy.
     */
    private float temperature = FTuning.AMBIENT_TU.value();
    /** How fast it moved last tick. A process may care about that as well as how hot it got. */
    private float lastDelta;
    private int holdTicks;

    private int insulation;
    private int sinceInsulationCheck;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.CRUCIBLE.get(), pos, state);
    }

    // --- thermal ----------------------------------------------------------------------------

    @Override
    public Tu getTemperature() {
        return new Tu(temperature);
    }

    @Override
    public void setTemperature(Tu tu) {
        temperature = tu.value();
    }

    @Override
    public ThermalMass getThermalMass() {
        return getBlockState().getBlock() == FBlocks.LARGE_CRUCIBLE.get()
                ? FTuning.CRUCIBLE_LARGE_MASS
                : FTuning.CRUCIBLE_SMALL_MASS;
    }

    @Override
    public Conductance getLeak() {
        return new Conductance(FTuning.VESSEL_LEAK.workPerTickPerTu()
                * (float) Math.pow(FTuning.INSULATION_LEAK_FACTOR, insulation));
    }

    /** How many insulating blocks are packed against it. Shown as a spec, not as a reading. */
    public int getInsulation() {
        return insulation;
    }

    public TuRate getHeatingRate() {
        return new TuRate(lastDelta);
    }

    public int getHoldTicks() {
        return holdTicks;
    }

    // --- contents ---------------------------------------------------------------------------

    public List<ItemStack> getContents() {
        return contents;
    }

    public ItemStack getItem(int slot) {
        return contents.get(slot);
    }

    public boolean insert(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        for (int slot = 0; slot < contents.size(); slot++) {
            if (!contents.get(slot).isEmpty())
                continue;
            ItemStack taken = stack.split(1);
            // A cold item put into a hot vessel is still cold; it is the vessel that will heat it.
            // Clearing any stale stamp here is what stops an ingot that was hot an hour ago
            // arriving with a temperature it no longer has.
            if (level != null)
                ItemHeat.settle(taken, level);
            contents.set(slot, taken);
            sync();
            return true;
        }
        return false;
    }

    /**
     * Take something out, at the temperature the vessel is at.
     *
     * <h3>Where a workpiece picks up its heat</h3>
     * Contents are not stamped while they sit here -- they are at the vessel's temperature by
     * definition, and re-stamping every tick would rewrite a data component sixty times a second
     * to say something already known. The stamp is applied at the moment the item leaves, which
     * is the moment it starts cooling on its own and the moment anybody could ask.
     */
    public ItemStack removeItem(int slot) {
        ItemStack taken = contents.get(slot);
        if (taken.isEmpty())
            return ItemStack.EMPTY;
        contents.set(slot, ItemStack.EMPTY);
        if (level != null)
            ItemHeat.set(taken, new Tu(temperature), level);
        holdTicks = 0;
        sync();
        return taken;
    }

    public ItemStack removeFirst() {
        for (int slot = contents.size() - 1; slot >= 0; slot--)
            if (!contents.get(slot).isEmpty())
                return removeItem(slot);
        return ItemStack.EMPTY;
    }

    // --- the tick ---------------------------------------------------------------------------

    public void tickServer() {
        if (level == null)
            return;

        if (++sinceInsulationCheck >= INSULATION_RECHECK_INTERVAL) {
            sinceInsulationCheck = 0;
            insulation = countInsulation();
        }

        Tu fireTu = HeatSource.below(level, worldPosition);
        lastDelta = Heat.tick(this, fireTu, fireTu.value() > FTuning.AMBIENT_TU.value()).tuPerTick();

        advanceProcess();
    }

    private void advanceProcess() {
        Optional<ThermalProcess> maybe = ThermalProcessTable.get().find(contents);
        if (maybe.isEmpty()) {
            holdTicks = 0;
            return;
        }
        ThermalProcess process = maybe.get();

        // Overrun. The vessel did not stop, so the batch kept getting hotter, so it is ruined --
        // and it is ruined the instant it passes the line rather than after a grace period,
        // because a grace period would be the machine noticing.
        if (process.spoilsAt(temperature)) {
            spoil(process);
            return;
        }

        // Outside the band nothing happens and nothing is lost. Progress stalls rather than
        // resetting: the safe direction is meant to be genuinely safe (§6), and a player who
        // drops a little under the window should not be punished for the same mistake twice.
        if (!process.inBand(temperature))
            return;

        // Climbing too fast is the one failure with no visible cause, and therefore the one an
        // instrument genuinely fixes. It is also what makes a bigger vessel necessary rather than
        // merely nicer -- a small crucible on a full fire cannot satisfy a 25 Tu/t limit at all.
        if (lastDelta > process.maxHeatingTuPerTick()) {
            holdTicks = 0;
            return;
        }

        if (++holdTicks < process.holdTicks())
            return;

        complete(process);
    }

    private void complete(ThermalProcess process) {
        int[] assignment = ThermalProcessTable.match(process.inputs(), contents);
        if (assignment == null)
            return;
        for (int slot : assignment)
            contents.get(slot).shrink(1);
        cleanEmpties();

        ItemStack result = process.result().copy();
        if (level != null)
            ItemHeat.set(result, new Tu(temperature), level);
        place(result);

        holdTicks = 0;
        sync();
    }

    private void spoil(ThermalProcess process) {
        int[] assignment = ThermalProcessTable.match(process.inputs(), contents);
        if (assignment == null)
            return;
        for (int slot : assignment)
            contents.get(slot).shrink(1);
        cleanEmpties();

        ItemStack ruined = process.spoiled().copy();
        if (!ruined.isEmpty()) {
            if (level != null)
                ItemHeat.set(ruined, new Tu(temperature), level);
            place(ruined);
        }

        holdTicks = 0;
        sync();
    }

    /** Put a stack into the first free slot, or drop it if there is somehow none. */
    private void place(ItemStack stack) {
        for (int slot = 0; slot < contents.size(); slot++) {
            if (contents.get(slot).isEmpty()) {
                contents.set(slot, stack);
                return;
            }
        }
        if (level != null)
            net.minecraft.world.Containers.dropItemStack(level,
                    worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), stack);
    }

    private void cleanEmpties() {
        for (int slot = 0; slot < contents.size(); slot++)
            if (contents.get(slot).isEmpty())
                contents.set(slot, ItemStack.EMPTY);
    }

    private int countInsulation() {
        if (level == null)
            return 0;
        int found = 0;
        for (Direction face : Direction.values())
            if (level.getBlockState(worldPosition.relative(face)).is(FBlocks.INSULATION.get()))
                found++;
        return Math.min(FTuning.INSULATION_MAX_BLOCKS, found);
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, true, registries);
        tag.putFloat("Temperature", temperature);
        tag.putInt("HoldTicks", holdTicks);
        tag.putInt("Insulation", insulation);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
        temperature = tag.contains("Temperature")
                ? tag.getFloat("Temperature")
                : FTuning.AMBIENT_TU.value();
        holdTicks = tag.getInt("HoldTicks");
        insulation = tag.getInt("Insulation");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
