package io.github.cloby0.feedback.machine.crank;

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
 * Slice 1: 12 Su. Placeholder RPM -- the slice fixes Su for every machine but never fixes a
 * speed, so this number is invented and marked as such.
 */
public class HandCrankBlockEntity extends RotationNode {

    public static final float RPM = 32f;
    public static final float CAPACITY_SU = 12f;

    /**
     * A right click is a single event, but turning a crank is continuous. Each click buys a
     * short run of rotation; holding the button down refreshes it before it lapses, so the crank
     * keeps going exactly as long as somebody is working it.
     */
    private static final int TICKS_PER_TURN = 12;

    private int turningTicks;

    public HandCrankBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.HAND_CRANK.get(), pos, state);
    }

    public void turn() {
        if (level == null || level.isClientSide)
            return;
        boolean wasStopped = turningTicks <= 0;
        turningTicks = TICKS_PER_TURN;
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
        return turningTicks > 0 ? RPM : 0;
    }

    @Override
    public float getCapacitySu() {
        return CAPACITY_SU;
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
