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
 * How fast a temperature is changing, in {@code Tu/t}.
 *
 * <h2>Why this is separate from {@link Tu}</h2>
 * It is the same unit over time, and it is the one place in the thermal model where confusing the
 * two would be easy and silent: a crucible's heating rate and a crucible's temperature are both
 * "a number of Tu", and the process check compares one of them against a limit. A rate of
 * 1450 Tu/t and a temperature of 1450 Tu would both look entirely reasonable in a debug readout.
 *
 * <h2>What it is used for</h2>
 * Three things, and they are more different than they look:
 * <ul>
 *   <li>How fast a loose workpiece sheds heat, which sets the size of the player's workshop --
 *       the walk from fire to anvil is a temperature drop divided by this.</li>
 *   <li>How fast a vessel is climbing, which a thermal process can refuse to run above. That is
 *       what makes a large crucible necessary rather than merely nicer.</li>
 *   <li>What one tick of {@link io.github.cloby0.feedback.core.thermal.Heat#tick} moved, which is
 *       the previous item measured.</li>
 * </ul>
 */
public record TuRate(float tuPerTick) {
}
