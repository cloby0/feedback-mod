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
package io.github.soundgoodizerfan.feedback.machine.vessel;

import java.util.List;
import java.util.Optional;

import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.core.thermal.Heat;
import io.github.soundgoodizerfan.feedback.core.thermal.HeatSource;
import io.github.soundgoodizerfan.feedback.core.thermal.ThermalBody;
import io.github.soundgoodizerfan.feedback.core.unit.Conductance;
import io.github.soundgoodizerfan.feedback.core.unit.ThermalMass;
import io.github.soundgoodizerfan.feedback.core.unit.Tu;
import io.github.soundgoodizerfan.feedback.core.unit.TuRate;
import io.github.soundgoodizerfan.feedback.machine.bellows.Blown;
import io.github.soundgoodizerfan.feedback.process.Fuel;
import io.github.soundgoodizerfan.feedback.process.FuelTable;
import io.github.soundgoodizerfan.feedback.process.ThermalProcess;
import io.github.soundgoodizerfan.feedback.process.ThermalProcessTable;
import io.github.soundgoodizerfan.feedback.process.VanillaFallback;
import io.github.soundgoodizerfan.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A self-fired thermal vessel with nine generic workpiece slots and one fuel slot -- the
 * Furnace, Smoker and Crude Blast Furnace, all one class, differing only by {@link VesselKind}
 * (§15).
 *
 * <h2>Why this replaced mixing into the vanilla furnace family</h2>
 * The first pass at §15 mixed into {@code AbstractFurnaceBlockEntity} and its menu/screen, and it
 * worked right up until it had to disagree with vanilla about what a slot was for. Every vanilla
 * assumption this mod needed to change -- what slot 2 means, whether a click there is even
 * accepted, whether the container will let an item sit there, what the GUI draws -- turned out to
 * be hardcoded somewhere else in the vanilla class hierarchy, and each one only surfaced by
 * actually crashing. An independent block, block entity, menu and screen has none of that: every
 * assumption here is one this class actually makes.
 *
 * <h2>Nine slots, and why they are interchangeable</h2>
 * Unlike the constrained three-slot vanilla shape, there is always a free slot to place a result
 * in, which is what lets this go back to the crucible's own model rather than the whole-stack
 * workaround the mixin pass needed: {@link ThermalProcessTable} is checked against the whole
 * inventory first (a multi-ingredient process, like steel, can be satisfied by any two slots),
 * and whatever it doesn't touch falls through to {@link VanillaFallback} independently, per slot.
 * No slot is the input, the reagent or the output; a chest of nine identical stacks is what the
 * vessel actually is, physically.
 */
public class ThermalVesselBlockEntity extends BlockEntity
        implements Container, MenuProvider, ThermalBody, HeatSource, Blown {

    public static final int SLOTS = 9;
    public static final int SLOT_FUEL = SLOTS;
    private static final int TOTAL_SLOTS = SLOTS + 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private final float[] work = new float[SLOTS];
    private int holdTicks;
    private float lastDelta;

    private float temperature = FTuning.AMBIENT_TU.value();
    private int fuelTicksLeft;
    private int fuelDuration;
    private float flameRollTu = FTuning.AMBIENT_TU.value();

    /** Banked by a bellows; see {@code FireboxBlockEntity} for why draught, not the bank itself,
     * sets the flame temperature. This vessel is its own fire, so it takes air directly rather
     * than through a separate firebox. */
    private float air;
    private float draught;

    private final VesselKind kind;
    /** Fuel-left, fuel-duration, temperature (whole Tu) -- what the screen actually needs. */
    private final ContainerData data = new SimpleContainerData(3);

    public ThermalVesselBlockEntity(BlockPos pos, BlockState state, VesselKind kind) {
        super(FBlockEntities.THERMAL_VESSEL.get(), pos, state);
        this.kind = kind;
    }

    public VesselKind getKind() {
        return kind;
    }

    public ContainerData getContainerData() {
        return data;
    }

    // --- thermal --------------------------------------------------------------------------

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
        return kind.mass();
    }

    @Override
    public Conductance getLeak() {
        return kind.leak();
    }

    @Override
    public boolean hasThermowell() {
        return kind.hasThermowell();
    }

    @Override
    public Tu getFireTu() {
        if (fuelTicksLeft <= 0)
            return FTuning.AMBIENT_TU;
        // Air multiplies the fuel's own flame temperature rather than replacing it -- see
        // FireboxBlockEntity for why a cool fuel blown hard is still a cool fire.
        float blown = Mth.clamp(draught / FTuning.FIREBOX_AIR_PER_TICK, 0f, 1f);
        return new Tu(flameRollTu * Mth.lerp(blown, 1f, FTuning.FULL_AIR_TEMPERATURE_FACTOR));
    }

    /**
     * A vessel takes air directly rather than through a separate firebox -- it is already its
     * own fire (§15), so a bellows aimed at it plays exactly the role it plays for a crucible's
     * firebox, just without the middleman.
     */
    @Override
    public void addAir(float amount) {
        air = Math.min(FTuning.FIREBOX_MAX_AIR, air + amount);
    }

    // --- the tick ---------------------------------------------------------------------------

    public void tickServer() {
        if (level == null)
            return;

        draught = Math.min(air, FTuning.FIREBOX_AIR_PER_TICK);
        air -= draught;

        if (fuelTicksLeft <= 0)
            light();
        boolean lit = fuelTicksLeft > 0;
        Tu fireTu = getFireTu();
        if (lit)
            fuelTicksLeft--;

        lastDelta = Heat.tick(this, fireTu, lit).tuPerTick();
        if (temperature > kind.ceilingTu())
            temperature = kind.ceilingTu();

        List<ItemStack> contents = items.subList(0, SLOTS);
        Optional<ThermalProcess> process = ThermalProcessTable.get().find(contents);
        if (process.isPresent()) {
            advanceProcess(process.get(), contents);
        } else {
            holdTicks = 0;
            for (int slot = 0; slot < SLOTS; slot++)
                advanceSlot(slot);
        }

        data.set(0, fuelTicksLeft);
        data.set(1, fuelDuration);
        data.set(2, Math.round(temperature));

        BlockState state = getBlockState();
        if (state.hasProperty(ThermalVesselBlock.LIT) && state.getValue(ThermalVesselBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(ThermalVesselBlock.LIT, lit), 3);

        setChanged();
    }

    private void light() {
        ItemStack fuelStack = items.get(SLOT_FUEL);
        Optional<Fuel> found = FuelTable.get().find(fuelStack);
        if (found.isEmpty())
            return;

        Fuel burning = found.get();
        fuelTicksLeft = burning.duration();
        fuelDuration = burning.duration();
        // Rolled once and held for the whole burn -- see FireboxBlockEntity for why per-tick
        // noise would be the same variance with nothing in it to learn.
        float roll = 1f + (level.random.nextFloat() * 2f - 1f) * burning.spread();
        flameRollTu = burning.temperature() * roll;

        fuelStack.shrink(1);
        if (fuelStack.isEmpty() && fuelStack.hasCraftingRemainingItem())
            items.set(SLOT_FUEL, fuelStack.getCraftingRemainingItem());
    }

    /**
     * One slot's own single-item vanilla recipe, independent of every other slot -- see the
     * class doc on why food counts ticks and metal counts Work.
     */
    private void advanceSlot(int slot) {
        ItemStack input = items.get(slot);
        if (input.isEmpty()) {
            work[slot] = 0;
            return;
        }

        Optional<RecipeHolder<AbstractCookingRecipe>> maybe = VanillaFallback.find(level, input);
        if (maybe.isEmpty()) {
            work[slot] = 0;
            return;
        }
        RecipeHolder<AbstractCookingRecipe> recipe = maybe.get();
        boolean food = VanillaFallback.isFood(recipe);

        if (food && temperature > FTuning.FOOD_MAX_TU.value()) {
            items.set(slot, ItemStack.EMPTY);
            work[slot] = 0;
            return;
        }
        boolean inBand = food || temperature >= FTuning.METAL_MIN_TU.value();
        if (!inBand)
            return;

        work[slot] += food ? 1f : Math.max(0, temperature - FTuning.AMBIENT_TU.value());
        float required = food
                ? recipe.value().getCookingTime() * input.getCount()
                : VanillaFallback.requiredWork(recipe, FTuning.FALLBACK_WORK_PER_200_TICKS) * input.getCount();
        if (work[slot] >= required) {
            ItemStack result = recipe.value().assemble(new SingleRecipeInput(input), level.registryAccess());
            if (!result.isEmpty()) {
                result.setCount(result.getCount() * input.getCount());
                items.set(slot, result);
            }
            work[slot] = 0;
        }
    }

    /** The {@link ThermalProcessTable} path, run exactly the way the crucible runs one. */
    private void advanceProcess(ThermalProcess process, List<ItemStack> contents) {
        if (process.spoilsAt(temperature)) {
            spoilProcess(process, contents);
            return;
        }
        if (!process.inBand(temperature)) {
            holdTicks = 0;
            return;
        }
        if (Math.abs(lastDelta) > process.maxHeatingTuPerTick()) {
            holdTicks = 0;
            return;
        }
        if (++holdTicks < process.holdTicks())
            return;

        completeProcess(process, contents);
        holdTicks = 0;
    }

    private void completeProcess(ThermalProcess process, List<ItemStack> contents) {
        int[] assignment = ThermalProcessTable.match(process.inputs(), contents);
        if (assignment == null)
            return;
        for (int slot : assignment)
            contents.get(slot).shrink(1);

        place(process.result().copy());
        setChanged();
    }

    private void spoilProcess(ThermalProcess process, List<ItemStack> contents) {
        int[] assignment = ThermalProcessTable.match(process.inputs(), contents);
        if (assignment == null)
            return;
        for (int slot : assignment)
            contents.get(slot).shrink(1);

        ItemStack ruined = process.spoiled().copy();
        if (!ruined.isEmpty())
            place(ruined);
        holdTicks = 0;
        setChanged();
    }

    /** Into the first free or stackable slot among the nine, or dropped if somehow none. */
    private void place(ItemStack stack) {
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty()) {
                items.set(slot, stack);
                return;
            }
            if (ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
                existing.grow(stack.getCount());
                return;
            }
        }
        if (level != null)
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), stack);
    }

    // --- Container ----------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return ContainerHelper.removeItem(items, slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // --- MenuProvider ---------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ThermalVesselMenu(id, playerInventory, this);
    }

    // --- persistence ----------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putFloat("Temperature", temperature);
        tag.putInt("FuelTicksLeft", fuelTicksLeft);
        tag.putInt("FuelDuration", fuelDuration);
        tag.putFloat("FlameRoll", flameRollTu);
        tag.putFloat("Air", air);
        tag.putInt("HoldTicks", holdTicks);
        for (int slot = 0; slot < SLOTS; slot++)
            tag.putFloat("Work" + slot, work[slot]);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        temperature = tag.contains("Temperature") ? tag.getFloat("Temperature") : FTuning.AMBIENT_TU.value();
        fuelTicksLeft = tag.getInt("FuelTicksLeft");
        fuelDuration = tag.getInt("FuelDuration");
        flameRollTu = tag.getFloat("FlameRoll");
        air = tag.getFloat("Air");
        holdTicks = tag.getInt("HoldTicks");
        for (int slot = 0; slot < SLOTS; slot++)
            work[slot] = tag.getFloat("Work" + slot);
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
