package io.github.cloby0.feedback.machine.clutch;

import io.github.cloby0.feedback.control.Switchable;
import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.core.rotation.RotationPropagator;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ClutchBlockEntity extends RotationNode implements Switchable {

    public ClutchBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.CLUTCH.get(), pos, state);
    }

    @Override
    public boolean isEngaged() {
        return getBlockState().getValue(ClutchBlock.ENGAGED);
    }

    @Override
    public void setEngaged(boolean engaged) {
        if (level == null || level.isClientSide || isEngaged() == engaged)
            return;

        Direction output = getBlockState().getValue(ClutchBlock.FACING);
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ClutchBlock.ENGAGED, engaged));

        // Both sides have to be rebuilt, and from each side separately: engaging must merge two
        // networks into one, disengaging must split one into two, and a rebuild only ever sees
        // the run it can reach from where it started.
        RotationPropagator.rebuildFrom(level, worldPosition);
        RotationPropagator.rebuildFrom(level, worldPosition.relative(output));

        level.playSound(null, worldPosition,
                net.minecraft.sounds.SoundEvents.WOODEN_BUTTON_CLICK_ON,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.4f, engaged ? 0.9f : 0.6f);
    }

    @Override
    public float getDragSuPerRpm() {
        return FTuning.SHAFT_DRAG_SU_PER_RPM;
    }

    @Override
    public float getInertia() {
        return FTuning.SHAFT_INERTIA;
    }
}
