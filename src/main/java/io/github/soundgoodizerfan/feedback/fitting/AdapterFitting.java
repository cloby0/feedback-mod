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
package io.github.soundgoodizerfan.feedback.fitting;

/**
 * A fitting that grants its holder a second energy type to run on -- a motor bolted to a
 * mechanical machine so it also takes electricity, a coil dropped into a crucible so it also
 * heats from electricity rather than only a flame.
 * <p>
 * Sided like a {@link SensorFitting} (it occupies a face, physically), addon-shaped like an
 * {@code Upgrade} (it doesn't read anything, it changes what the holder can do). Empty for now
 * on purpose: what an adapter actually converts to what is a different pairing for every energy
 * combination (electrical→mechanical, electrical→thermal, and philosophy §10's "local
 * conversion covers" lists more), and generalizing that before a second concrete adapter exists
 * to compare against would be guessing at a shape instead of finding one.
 */
public non-sealed interface AdapterFitting extends SidedFitting {
}
