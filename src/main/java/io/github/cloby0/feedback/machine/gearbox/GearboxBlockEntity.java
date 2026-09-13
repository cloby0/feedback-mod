package io.github.cloby0.feedback.machine.gearbox;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A gearbox on the network: a relay that drags more than a shaft, because it is four meshes
 * rather than a pair of bearings. Nothing else -- the direction change is entirely a property of
 * the block, and the propagator works it out from the face.
 */
public class GearboxBlockEntity extends RotationNode {

    public GearboxBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.GEARBOX.get(), pos, state);
    }

    @Override
    public float getDragSuPerRpm() {
        return FTuning.GEARBOX_DRAG_SU_PER_RPM;
    }

    @Override
    public float getInertia() {
        return FTuning.GEARBOX_INERTIA;
    }
}
