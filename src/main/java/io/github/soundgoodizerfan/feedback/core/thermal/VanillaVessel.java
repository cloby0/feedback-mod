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
package io.github.soundgoodizerfan.feedback.core.thermal;

/**
 * The extra state a vanilla furnace/smoker/blast furnace needs on top of {@link ThermalBody} and
 * {@link HeatSource}, because unlike a firebox-and-crucible pair, one vanilla block is both.
 *
 * <h2>Why this is not folded into {@link ThermalBody} or {@link HeatSource}</h2>
 * Those two interfaces are general -- the crucible and firebox use them and know nothing about a
 * fuel slot burning down or a recipe's accumulated Work. A vanilla vessel's fuel state and its
 * recipe progress are specific to it being self-fired, so they get their own small seam rather
 * than widening two interfaces that other machines already implement cleanly.
 */
public interface VanillaVessel {

    int getFuelTicksLeft();

    void setFuelTicksLeft(int ticks);

    /** How long the piece currently burning lasts in total, for the flame icon's fraction. */
    int getFuelDuration();

    void setFuelDuration(int ticks);

    /** This burn's rolled flame temperature, held for its whole duration. */
    float getFlameRollTu();

    void setFlameRollTu(float tu);

    /** Work accrued toward whatever recipe is currently sitting in the input slot. */
    float getAccumulatedWork();

    void setAccumulatedWork(float work);

    /**
     * A hard cap this vessel's own construction imposes on its temperature, independent of its
     * fire. Only the Smoker overrides this -- see {@code FTuning.SMOKER_CEILING_TU} for why the
     * Furnace and the Crude Blast Furnace do not need one.
     */
    default float getBandCeilingTu() {
        return Float.MAX_VALUE;
    }
}
