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
package io.github.cloby0.feedback.machine.hammer;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.thermal.ItemHeat;
import io.github.cloby0.feedback.machine.linkage.Reciprocating;
import io.github.cloby0.feedback.machine.linkage.StrengthPair;
import io.github.cloby0.feedback.machine.linkage.Throw;
import io.github.cloby0.feedback.process.Deformation;
import io.github.cloby0.feedback.process.DeformationTable;
import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.registry.FDataComponents;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hits whatever is in front of it, forever.
 *
 * <h2>The whole mod, in one block</h2>
 * The hammer has no recipe list and no notion of completion. It delivers {@code Fu} at whatever
 * {@code St} its linkage provides, and it keeps doing that for as long as it is driven. When the
 * workpiece has taken enough work it becomes the next thing along the same physical axis -- and
 * then the hammer carries straight on working <em>that</em>.
 * <p>
 * Copper ingot to plate is the process. Plate to foil is the same process, not a malfunction:
 * foil is a real material the thermometer needs, so the first overrun a player meets is a
 * sidegrade rather than a punishment. Only past foil is there scrap.
 * <p>
 * Nothing here decides any of that. The chain lives in {@link DeformationTable} as a property of
 * the material, and the hammer never consults it for anything except "how much work does this
 * need and how hard must I hit it".
 */
public class MechanicalHammerBlockEntity extends BlockEntity implements Reciprocating, StrengthPair {

    /** Stages one blow may cascade through. A guard against a table that loops back on itself. */
    private static final int MAX_CASCADE = 8;

    private ItemStack workpiece = ItemStack.EMPTY;

    /**
     * How sound the head is, 1 down to {@link FTuning#HAMMER_CONDITION_FLOOR}.
     *
     * <h3>Why it scales both figures and not just the ceiling</h3>
     * Scaling only the ceiling was the first version and it punished exactly one mistake:
     * over-gearing. A hammer battered by beating cold steel went on hitting at its full 12 St,
     * which made two thirds of the wear invisible. Scaling {@link #getStrength} as well means a
     * worn head delivers less of every blow, which is what a mushroomed face actually does.
     * <p>
     * Gearing can then partly compensate for a worn hammer -- up to the ceiling, which has fallen
     * too -- and that is a genuine trade rather than a loophole. Recovering force by gearing costs
     * cogs and speed, and drives the machine nearer a ceiling it can no longer take.
     */
    private float condition = 1f;

    public MechanicalHammerBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.MECHANICAL_HAMMER.get(), pos, state);
    }

    public ItemStack getWorkpiece() {
        return workpiece;
    }

    public ItemStack removeWorkpiece() {
        ItemStack taken = workpiece;
        workpiece = ItemStack.EMPTY;
        if (level != null)
            ItemHeat.settle(taken, level);
        sync();
        return taken;
    }

    public boolean insert(ItemStack stack) {
        if (!workpiece.isEmpty() || stack.isEmpty())
            return false;
        workpiece = stack.split(1);
        sync();
        return true;
    }

    /** How sound the head is, 1 down to {@link FTuning#HAMMER_CONDITION_FLOOR}. */
    public float getCondition() {
        return condition;
    }

    @Override
    public float getStrength(Throw installed) {
        float rated = installed == Throw.SHORT ? FTuning.HAMMER_ST_SHORT : FTuning.HAMMER_ST_LONG;
        return rated * condition;
    }

    @Override
    public float getMaxStrength() {
        return FTuning.HAMMER_MAX_ST * condition;
    }

    @Override
    public float getLoadSu() {
        return FTuning.HAMMER_LOAD_SU;
    }

    /**
     * @param raw force the drive put behind the stroke, before this hammer's ceiling.
     */
    @Override
    public void onStroke(float raw) {
        if (level == null || level.isClientSide)
            return;

        playBlow();

        // The ceiling is enforced here rather than in the linkage, and the surplus is not
        // discarded -- it is absorbed. Gearing past what the head can take now makes the head
        // worse at taking it, so the limit is discoverable by watching the machine instead of by
        // reading the source.
        float ceiling = getMaxStrength();
        float strength = Math.min(raw, ceiling);
        if (raw > ceiling)
            wear(raw - ceiling);

        // A hammer beating air takes nothing, deliberately. Nothing resists it, so there is no
        // shock to do the damage -- and making an idle hammer wear would turn wear into an uptime
        // tax, which is the GregTech maintenance model this deliberately is not. Running empty is
        // already answered by overrun: leave the plate there and it becomes foil, then scrap. The
        // punishment for walking away belongs to the workpiece, not the machine.
        if (workpiece.isEmpty())
            return;

        Optional<Deformation> maybe = DeformationTable.get().find(workpiece);
        if (maybe.isEmpty()) {
            // Nothing this material does under a hammer. Scrap is already scrap -- but the blow
            // still lands on something solid, so the force has nowhere to go but into the head.
            wear(strength);
            return;
        }

        Deformation deformation = maybe.get();

        // Nothing heats the anvil. The workpiece carries its own heat (§9), and if it has fallen
        // out of its working range the hammer goes on hitting it to no effect whatsoever -- which
        // is the honest answer, because the hammer has no way to tell. The heat is wasted and the
        // material is intact, matching beat 2's asymmetry: the player loses a trip to the fire,
        // not the steel.
        //
        // This is where the two beats meet. A hot ingot is on a clock from the moment it leaves
        // the crucible, so the distance between fire and anvil became a decision nobody installed,
        // and the dead time between blows became the most expensive thing in the build. The
        // brute-force answer is a faster hammer and the Su to sustain it; the other is two hammers
        // and a player willing to shuffle hot metal between them. Capital against attention, in a
        // third place nobody put it (§5).
        //
        // The slice always said the hammer wears here and it never did. It does now: the blow is
        // landing on metal too stiff to move, so all of it comes back up the handle.
        if (deformation.isHotWorking() && !deformation.worksAt(ItemHeat.get(workpiece, level))) {
            wear(strength);
            return;
        }

        // How much a blow accomplishes is the material's business, not the machine's. Below the
        // hardness threshold nothing lands at all -- philosophy 7's hard gate, a genuine
        // impossibility rather than a slower version of the process.
        int delivered = deformation.workFrom(strength);
        if (delivered <= 0) {
            wear(strength);
            return;
        }

        int worked = workpiece.getOrDefault(FDataComponents.WORK.get(), 0) + delivered;

        // Surplus carries, and carries through a finished stage into the next one. A blow does
        // not politely stop at the finish line, which is the whole point: a hard enough blow on
        // a soft enough material runs straight past what you wanted. Overshoot is the mechanic,
        // not an edge case.
        Deformation stage = deformation;
        for (int guard = 0; guard < MAX_CASCADE && worked >= stage.work(); guard++) {
            worked -= stage.work();

            // The new stage inherits the old one's heat. Beating a hot ingot into a plate does not
            // cool it, and losing the stamp here would have made every hot-working chain a single
            // step by accident.
            float carried = ItemHeat.get(workpiece, level);
            workpiece = stage.result().copy();
            ItemHeat.set(workpiece, carried, level);

            Optional<Deformation> next = DeformationTable.get().find(workpiece);
            if (next.isEmpty()) {
                worked = 0;   // nothing further to become; the work has nowhere to go
                break;
            }
            stage = next.get();
        }

        if (worked > 0) {
            workpiece.set(FDataComponents.WORK.get(), worked);
            // Required work rides along so the workpiece can describe its own progress wherever
            // it goes, without anything having to look the material up.
            workpiece.set(FDataComponents.WORK_REQUIRED.get(), stage.work());
        } else {
            workpiece.remove(FDataComponents.WORK.get());
            workpiece.remove(FDataComponents.WORK_REQUIRED.get());
        }
        sync();
    }

    /**
     * Force that went into the machine instead of the work.
     *
     * <h3>One method, because it is one rule</h3>
     * Every way a blow can accomplish nothing ends up here -- too cold, too soft a blow, too hard
     * a blow, or a workpiece with nothing further to become. Writing a separate consequence for
     * each was the obvious shape and it would have been four balance decisions pretending to be
     * physics. There is one physical fact instead: the energy is conserved and the head is what
     * absorbs it, so a mistake that wastes more force wears the machine faster, with nothing
     * needing to know which mistake it was.
     */
    private void wear(float stAbsorbed) {
        if (stAbsorbed <= 0)
            return;

        int before = FTuning.conditionBand(condition);
        condition = Math.max(FTuning.HAMMER_CONDITION_FLOOR,
                condition - stAbsorbed * FTuning.HAMMER_WEAR_PER_ST);

        setChanged();
        // Sync only when the adjective would change. Wear happens on every wasted blow and the
        // readout has four states in the life of the machine; syncing per blow would be a packet
        // several times a second to say nothing new.
        if (FTuning.conditionBand(condition) != before)
            sync();
    }

    private void playBlow() {
        if (level == null)
            return;
        level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.15f, 1.6f);
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * The key is written even when the hammer is empty, and that is load-bearing.
     * <p>
     * NeoForge's {@code onDataPacket} discards an update tag that is completely empty:
     * <pre>if (!compoundtag.isEmpty()) self().loadWithComponents(...)</pre>
     * Omitting the key when there is nothing to save produced exactly that -- an empty tag -- so
     * the packet announcing "the workpiece is gone" was thrown away and the client went on
     * rendering an item that no longer existed.
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Workpiece", workpiece.saveOptional(registries));
        tag.putFloat("Condition", condition);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        workpiece = ItemStack.parseOptional(registries, tag.getCompound("Workpiece"));
        // A hammer saved before wear existed has no key, and getFloat gives 0 -- which would read
        // as a machine worn past its own floor. Absent means new.
        condition = tag.contains("Condition") ? tag.getFloat("Condition") : 1f;
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
