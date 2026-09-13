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
package io.github.soundgoodizerfan.feedback.mixin;

import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.core.unit.Conductance;
import io.github.soundgoodizerfan.feedback.core.unit.ThermalMass;

import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;

/**
 * "Genuinely stable for a primitive device" (§15), and cheaply enough said: higher mass and a
 * tighter leak than either of our own crucibles. No explicit floor -- see the note on
 * {@code FTuning.BLAST_FURNACE_MASS} for why that stays a claim about how long the climb takes
 * rather than a second clamp.
 * <p>
 * No {@code implements} clause: see {@code SmokerVesselMixin} for why this is a plain override.
 */
@Mixin(BlastFurnaceBlockEntity.class)
public class BlastFurnaceVesselMixin {

    public ThermalMass getThermalMass() {
        return FTuning.BLAST_FURNACE_MASS;
    }

    public Conductance getLeak() {
        return FTuning.BLAST_FURNACE_LEAK;
    }
}
