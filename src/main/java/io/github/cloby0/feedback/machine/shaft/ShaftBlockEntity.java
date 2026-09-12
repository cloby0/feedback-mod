package io.github.cloby0.feedback.machine.shaft;

import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A pure relay: generates nothing, demands nothing. */
public class ShaftBlockEntity extends RotationNode {

    public ShaftBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.SHAFT.get(), pos, state);
    }
}
