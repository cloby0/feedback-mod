package io.github.cloby0.feedback.machine.linkage;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Counts out strokes from rotation and hands them to whatever it is pointed at.
 *
 * <h2>Why the load is borrowed rather than owned</h2>
 * The linkage reports the driven machine's Su as its own, so a hammer's 80 Su appears on the
 * rotation network even though the hammer is not on it. Otherwise a machine could be driven for
 * free simply because a converter sat between it and the shaft.
 */
public class CrankLinkageBlockEntity extends RotationNode {

    /** Fractional strokes accumulated since the last one landed. */
    private float strokeProgress;

    public CrankLinkageBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.CRANK_LINKAGE.get(), pos, state);
    }

    public Throw getThrow() {
        return getBlockState().getValue(CrankLinkageBlock.THROW);
    }

    private Direction getFacing() {
        return getBlockState().getValue(CrankLinkageBlock.FACING);
    }

    /** The reciprocating machine in front, if there is one. */
    @Nullable
    public Reciprocating getDriven() {
        if (level == null)
            return null;
        BlockEntity be = level.getBlockEntity(worldPosition.relative(getFacing()));
        return be instanceof Reciprocating reciprocating ? reciprocating : null;
    }

    public void tickServer() {
        float rpm = Math.abs(getRpm());
        if (rpm <= 0) {
            strokeProgress = 0;
            return;
        }

        strokeProgress += rpm * FTuning.STROKES_PER_RPM_PER_TICK;
        if (strokeProgress < 1)
            return;

        Reciprocating driven = getDriven();
        // Count down rather than zeroing, so a fast linkage can land more than one stroke per
        // tick instead of silently throwing the extras away.
        while (strokeProgress >= 1) {
            strokeProgress -= 1;
            if (driven != null)
                driven.onStroke(getStrength());
        }
    }

    /**
     * Force behind each stroke. The linkage does not decide this on its own -- the machine states
     * what it can deliver at each throw, and the linkage picks which of the two applies.
     */
    public float getStrength() {
        Reciprocating driven = getDriven();
        return driven instanceof StrengthPair pair ? pair.getStrength(getThrow()) : 0;
    }

    @Override
    public float getLoadSu() {
        Reciprocating driven = getDriven();
        return driven == null ? 0 : driven.getLoadSu();
    }

    @Override
    public float getDragSuPerRpm() {
        return FTuning.SHAFT_DRAG_SU_PER_RPM;
    }

    @Override
    public float getInertia() {
        return FTuning.SHAFT_INERTIA;
    }

    @Override
    public void debugReport(Player player) {
        super.debugReport(player);
        player.displayClientMessage(Component.literal(
                String.format("[debug] throw %s  |  %.0f St  |  driving %s",
                        getThrow().getSerializedName(), getStrength(),
                        getDriven() == null ? "nothing" : "a machine")), false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("StrokeProgress", strokeProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        strokeProgress = tag.getFloat("StrokeProgress");
    }
}
