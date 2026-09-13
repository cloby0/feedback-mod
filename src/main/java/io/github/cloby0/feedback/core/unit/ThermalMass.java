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
 * How much Work it takes to raise a body by one Tu: {@code Work/Tu}.
 *
 * <h2>This is the number an upgrade changes</h2>
 * A larger vessel is not faster and it is not more efficient. It is <em>heavier</em>, and
 * everything the player notices about it follows from that one figure: mass over conductance is
 * the time constant, so a heavy vessel is slow to heat, slow to cool, slow to overshoot and slow
 * to respond. Neither end of that is better. A process that needs to hold a narrow window wants
 * the large crucible; a process that needs to move wants the small one.
 *
 * <p>Not to be confused with {@link Conductance}, which is {@code Work/t/Tu} -- a rate of flow
 * rather than a capacity, and the reason the two accessors here are named after their dimensions
 * rather than both being {@code value()}.
 */
public record ThermalMass(float workPerTu) implements Unit {

    @Override
    public float raw() {
        return workPerTu;
    }

    @Override
    public String unitKey() {
        return "feedback.unit.thermal_mass";
    }
}
