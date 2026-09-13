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
package io.github.cloby0.feedback.core.unit;

/**
 * Rotational speed, in revolutions per minute.
 *
 * <h2>Independent of {@link Su}, and that independence is load-bearing</h2>
 * A shaft has a speed and a network has a load, and neither implies the other. Keeping them
 * apart is what lets gearing be an honest trade: a geared-up branch turns faster and therefore
 * charges the ledger more, because friction and referred mass both scale, but nothing about
 * being fast makes it strong.
 *
 * <h2>Speed is a real number and a coarse readout</h2>
 * The simulation holds an exact RPM; a player standing next to a shaft gets four words for it
 * (§8, and {@code FTuning}'s speed bands). Both are true at once, which is the ordinary state of
 * affairs in this mod -- the world resolves on the figure and the player resolves on the
 * adjective until they buy something better.
 *
 * <h2>Signed, deliberately</h2>
 * The sign is the direction of rotation, so a reversed shaft is a negative RPM rather than a
 * separate flag. Anything comparing magnitudes has to say {@code Math.abs} and mean it.
 */
public record Rpm(float value) implements Unit {

    @Override
    public float raw() {
        return value;
    }

    @Override
    public String unitKey() {
        return "feedback.unit.rpm";
    }
}
