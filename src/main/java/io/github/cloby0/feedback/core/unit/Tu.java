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
 * A temperature, in Tu.
 *
 * <h2>Tu is a state, not an amount</h2>
 * The units table is strict about this and it is worth restating on the type itself: a body is
 * <em>at</em> a temperature. It does not contain some Tu that could be poured into another body.
 * What moves between bodies is Work, and how much Work it takes to move one Tu is
 * {@link ThermalMass}. That distinction is the reason there is no {@code getHeat()} anywhere in
 * the mod and never will be.
 *
 * <p>Also used for a <em>difference</em> of two temperatures -- an instrument's resolution, a
 * band's width. See the package javadoc for why that looseness was accepted rather than given a
 * fifth type.
 */
public record Tu(float value) {
}
