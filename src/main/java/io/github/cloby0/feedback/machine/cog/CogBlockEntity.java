package io.github.cloby0.feedback.machine.cog;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A cog on the network. Carries no gearing logic at all -- the ratio is a property of how two
 * cogs are placed, which is {@link CogBlock}'s business and the propagator's.
 * <p>
 * One block entity type serves both sizes, and the figures are read off the block rather than
 * held here. Anything stored on the block entity would have to be written, read back and kept in
 * step with the block it belongs to, to describe something that cannot change without the block
 * being replaced.
 */
public class CogBlockEntity extends RotationNode {

    public CogBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.COG.get(), pos, state);
    }

    @Override
    public float getDragSuPerRpm() {
        return getBlockState().getBlock() instanceof CogBlock cog
                ? cog.getDragSuPerRpm()
                : FTuning.SMALL_COG_DRAG_SU_PER_RPM;
    }

    @Override
    public float getInertia() {
        return getBlockState().getBlock() instanceof CogBlock cog
                ? cog.getInertia()
                : FTuning.SMALL_COG_INERTIA;
    }
}
