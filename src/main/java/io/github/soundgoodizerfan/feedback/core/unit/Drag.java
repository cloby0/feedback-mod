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
 * Bearing friction: the load a component adds per RPM it turns at, in {@code Su/RPM}.
 *
 * <h2>Why friction is charged in Su and scales with speed</h2>
 * Loss is {@link Su} rather than {@link Rpm} because a rigid shaft turns at one speed along its
 * whole length -- a difference between its ends would be torsion, not loss -- and what friction
 * actually consumes is torque. Scaling with speed is what turns a tax into a decision, and three
 * things follow that nobody had to write as rules:
 * <ul>
 *   <li>A stopped shaft costs nothing, so an idle network is free rather than "overstressed".</li>
 *   <li>A network finds its own top speed, where {@code capacity = drag x rpm}. A long run does
 *       not hit a wall and refuse to turn; it just turns more slowly, because friction ate the
 *       torque. Unlike a hard per-shaft cap, which produces the absurdity of a generator being
 *       too good for its own shafting.</li>
 *   <li>Slow and wide becomes a real alternative to fast and narrow, because speed is now
 *       something bought by the block.</li>
 * </ul>
 *
 * <h2>Kept apart from {@link Inertia} on purpose</h2>
 * Both are per-component floats that live two lines apart in {@code FTuning}, both describe "how
 * much this part resists turning", and they are completely different quantities: drag is a
 * steady-state cost and inertia is a transient one. A network with high drag and no inertia is
 * slow and twitchy; one with the reverse is fast and ponderous. Swapping them would produce a
 * network that still ran, still looked plausible, and felt wrong -- the exact failure mode this
 * package exists to make impossible.
 */
public record Drag(float suPerRpm) implements Unit {

    @Override
    public float raw() {
        return suPerRpm;
    }

    @Override
    public String unitKey() {
        return "feedback.unit.drag";
    }
}
