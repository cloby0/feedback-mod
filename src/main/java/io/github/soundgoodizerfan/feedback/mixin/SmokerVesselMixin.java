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

import net.minecraft.world.level.block.entity.SmokerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;

/**
 * The one explicit band wall in §15 -- see {@code FTuning.SMOKER_CEILING_TU} for why the Smoker
 * needs a real clamp and the Furnace and Crude Blast Furnace do not.
 * <p>
 * No {@code implements} clause here: {@link AbstractFurnaceVesselMixin} already puts
 * {@code ThermalBody}/{@code HeatSource}/{@code VanillaVessel} on the shared superclass, so this
 * is a plain override of three of its methods, the same way any subclass overrides an inherited
 * one.
 */
@Mixin(SmokerBlockEntity.class)
public class SmokerVesselMixin {

    public ThermalMass getThermalMass() {
        return FTuning.SMOKER_MASS;
    }

    public Conductance getLeak() {
        return FTuning.SMOKER_LEAK;
    }

    public float getBandCeilingTu() {
        return FTuning.SMOKER_CEILING_TU.value();
    }
}
