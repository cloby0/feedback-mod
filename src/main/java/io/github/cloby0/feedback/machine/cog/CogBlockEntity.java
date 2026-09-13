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
