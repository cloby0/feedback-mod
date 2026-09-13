/*
 * Feedback -- a Minecraft technology mod.
 * Copyright (C) 2026 soundgoodizerfan
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Assets under src/main/resources/assets are NOT covered by this licence.
 * See LICENSE-ASSETS.
 */
package io.github.cloby0.feedback.machine.clutch;

import io.github.cloby0.feedback.control.Switchable;
import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.core.rotation.RotationPropagator;
import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.core.unit.Drag;
import io.github.cloby0.feedback.core.unit.Inertia;

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
    public Drag getDragSuPerRpm() {
        return FTuning.SHAFT_DRAG_SU_PER_RPM;
    }

    @Override
    public Inertia getInertia() {
        return FTuning.SHAFT_INERTIA;
    }
}
