package io.github.cloby0.feedback.core.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implemented by any block that participates in a rotation network.
 * <p>
 * A rotatable block answers two questions and nothing else: which way does it spin, and
 * does it present a shaft to a given face. Everything about speed, load and who is driving
 * whom lives in {@link RotationPropagator} and {@link RotationNetwork}, never in the block.
 */
public interface Rotatable {

    /** The axis this block turns about, in its current state. */
    Direction.Axis getRotationAxis(BlockState state);

    /** Whether a shaft sticks out of this block toward {@code face}, so a neighbour can couple to it. */
    boolean hasShaftTowards(LevelAccessor level, BlockPos pos, BlockState state, Direction face);
}
