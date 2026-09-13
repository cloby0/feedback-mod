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
 * How readily Work crosses a boundary, in {@code Work/t} per Tu of difference across it:
 * {@code Work/t/Tu}.
 *
 * <h2>One type, two roles, and noticing that was the point of the exercise</h2>
 * A fire's grip on the vessel above it and a vessel's leak to the room are the same kind of
 * quantity, and the code did not say so. {@code Heat.equilibrium} adds them --
 * {@code (conductance x fire + leak x ambient) / (conductance + leak)} -- which is only a legal
 * thing to write because they share a dimension, and until this type existed nothing recorded
 * that. A leak <em>is</em> a conductance; the boundary it crosses is the one between the vessel
 * and the room, and calling it a leak is a statement about whether you wanted it, not about what
 * it is.
 *
 * <h2>Why heat must flow on a difference</h2>
 * Restated here because this is the type that carries it. The first version had a fire deliver a
 * flat {@code Work/t} with no conductance in it, and a vessel then settles where supply equals
 * leak -- so halving the leak doubles the final temperature and insulation runs away without
 * limit. Insulation became a trap instead of an upgrade.
 * <p>
 * Multiplying by {@code (fire - vessel)} fixes it by construction: a vessel approaches its fire's
 * temperature and can never pass it, insulation means getting closer to the flame and holding
 * there, and the only way past a flame is a hotter flame. See
 * {@link io.github.cloby0.feedback.core.thermal.Heat} for the full argument.
 */
public record Conductance(float workPerTickPerTu) {
}
