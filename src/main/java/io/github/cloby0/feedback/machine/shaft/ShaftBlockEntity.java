package io.github.cloby0.feedback.machine.shaft;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A relay. Generates nothing, but is not free: every shaft charges its bearing friction to the
 * network, so a sprawling layout costs real Su that a compact one does not.
 */
public class ShaftBlockEntity extends RotationNode {

    public ShaftBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.SHAFT.get(), pos, state);
    }

    @Override
    public float getLoadSu() {
        return FTuning.SHAFT_LOSS_SU;
    }

    @Override
    public float getInertia() {
        return FTuning.SHAFT_INERTIA;
    }
}
