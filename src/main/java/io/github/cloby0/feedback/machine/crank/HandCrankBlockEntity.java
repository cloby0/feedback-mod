package io.github.cloby0.feedback.machine.crank;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.core.rotation.RotationPropagator;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rotation for as long as somebody keeps clicking.
 * <p>
 * Slice 1: 12 Su. Its speed, and how long one click keeps it going, are invented -- see
 * {@link FTuning}.
 * <p>
 * A right click is a single event, but turning a crank is continuous, so each click buys a short
 * run of rotation and holding the button refreshes it before it lapses. The crank turns for
 * exactly as long as somebody is working it.
 */
public class HandCrankBlockEntity extends RotationNode {

    private int turningTicks;

    public HandCrankBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.HAND_CRANK.get(), pos, state);
    }

    public void turn() {
        if (level == null || level.isClientSide)
            return;
        boolean wasStopped = turningTicks <= 0;
        turningTicks = FTuning.HAND_CRANK_TICKS_PER_TURN;
        if (wasStopped)
            RotationPropagator.rebuildFrom(level, worldPosition);
    }

    public void tickServer() {
        if (turningTicks <= 0)
            return;
        turningTicks--;
        if (turningTicks == 0 && level != null)
            RotationPropagator.rebuildFrom(level, worldPosition);
    }

    @Override
    public float getGeneratedRpm() {
        return turningTicks > 0 ? FTuning.HAND_CRANK_RPM : 0;
    }

    @Override
    public float getCapacitySu() {
        return FTuning.HAND_CRANK_CAPACITY_SU;
    }

    @Override
    public float getInertia() {
        return FTuning.HAND_CRANK_INERTIA;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TurningTicks", turningTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        turningTicks = tag.getInt("TurningTicks");
    }
}
