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
package io.github.soundgoodizerfan.feedback.core.unit;

/**
 * Resistance to a change in speed: {@code Su x t / RPM}.
 *
 * <h2>Where the dimension comes from</h2>
 * Acceleration is net torque over moment of inertia -- {@code (capacity - load) / inertia} RPM per
 * tick -- which is the real relationship rather than a fudge. Rearranged, inertia is Su divided by
 * RPM-per-tick, so its unit carries a tick in it. Writing that down is worth doing because the
 * figures have to be calibrated against the {@link Su} figures or momentum is simply invisible: at
 * the first values tried, a water wheel reached full speed in one tick.
 *
 * <h2>What it buys, and why Create does not have it</h2>
 * Create's networks change speed instantly. Ours do not, and the difference is not decoration --
 * it is what makes a flywheel a component you can point at, a clutch something that coasts rather
 * than stops, and a heavy network visibly reluctant. "Clutches coast" needed no code; it fell out
 * of this number existing.
 *
 * <h2>See {@link Drag}</h2>
 * for why these two are separate types despite being neighbours in {@code FTuning} and describing
 * superficially similar things.
 */
public record Inertia(float suTicksPerRpm) implements Unit {

    @Override
    public float raw() {
        return suTicksPerRpm;
    }

    @Override
    public String unitKey() {
        return "feedback.unit.inertia";
    }
}
