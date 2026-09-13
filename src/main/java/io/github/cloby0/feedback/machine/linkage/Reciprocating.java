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
package io.github.cloby0.feedback.machine.linkage;

/**
 * Implemented by a machine that is driven by strokes rather than by rotation.
 * <p>
 * The split is the point. A hammer goes up and down and a shaft goes round and round, so
 * something has to convert one into the other, and that something is a component the player
 * places rather than a detail hidden inside the machine.
 */
public interface Reciprocating {

    /**
     * One stroke has been delivered.
     *
     * <h3>The figure is raw, and the machine clamps it</h3>
     * What arrives is what the drive actually put behind the stroke -- the installed throw times
     * the gear advantage -- and it may be more than the machine's own {@code getMaxStrength()}.
     * The linkage deliberately does not clamp it, because clamping in the linkage threw the
     * surplus away with nothing able to notice: over-gearing became a silent no-op and the ceiling
     * was undiscoverable.
     * <p>
     * So the ceiling stays a declaration and enforcing it is the machine's own business, which is
     * also the only place that knows what the excess <em>does</em>. A hammer takes it as damage; a
     * bellows takes it as nothing at all, because there is no more air inside it to find. Same
     * stroke, two honest answers.
     *
     * @param strength the force behind it, in St, before this machine's ceiling is applied.
     */
    void onStroke(float strength);

    /** Su this machine draws while being driven. */
    float getLoadSu();
}
