package io.github.cloby0.feedback.machine.linkage;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
     * what it can deliver at each throw, the drive's gearing multiplies it, and the machine's own
     * construction caps the result.
     */
    public float getStrength() {
        Reciprocating driven = getDriven();
        if (!(driven instanceof StrengthPair pair))
            return 0;
        return Math.min(pair.getStrength(getThrow()) * getGearAdvantage(), pair.getMaxStrength());
    }

    /**
     * How much the gearing between the source and this linkage multiplies the force behind a blow.
     *
     * <h2>Why this is the reciprocal of the speed ratio</h2>
     * A gear train trades speed for force and keeps the product: turn at half the speed and you
     * turn with twice the torque. The linkage already runs at its own geared speed, so blows fall
     * half as often on their own -- work per second is unchanged, and what changes is whether any
     * one blow clears the material's hardness floor at all. That is philosophy §5's second answer
     * to the force problem: the short throw spends precision, a gear train spends capital and
     * space.
     *
     * <h2>The caveat worth knowing</h2>
     * Ratios are measured against whichever node the run is anchored to, which is the driving
     * source whenever there is one. A run with no source at all is coasting, and its anchor is
     * arbitrary -- so the force behind a blow struck purely on momentum is scaled against nothing
     * in particular. Left alone: a coasting run is stopping, and nothing is asking it for work.
     */
    private float getGearAdvantage() {
        float ratio = Math.abs(getRatio());
        return ratio == 0 ? 1 : 1 / ratio;
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
