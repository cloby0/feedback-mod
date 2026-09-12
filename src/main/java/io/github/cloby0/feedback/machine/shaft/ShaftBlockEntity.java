package io.github.cloby0.feedback.machine.shaft;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A relay. Generates nothing and demands nothing while stopped, but drags once it turns: every
 * shaft charges bearing friction to the network in proportion to speed.
 * <p>
 * So a sprawling layout costs Su that a compact one does not, and a long run cannot be driven as
 * fast as a short one -- not because anything forbids it, but because friction runs out of
 * torque first.
 */
public class ShaftBlockEntity extends RotationNode {

    public ShaftBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.SHAFT.get(), pos, state);
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
