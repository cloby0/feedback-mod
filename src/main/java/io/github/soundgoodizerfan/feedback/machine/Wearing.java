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
package io.github.soundgoodizerfan.feedback.machine;

/**
 * A machine that absorbs the force it fails to put into the work, and is worse for it.
 *
 * <h2>Two methods, and the second is the surprising one</h2>
 * Reading condition needs the figure, obviously. It also needs to know whether the machine is
 * <em>running</em>, because philosophy 8's rule is that a reading is passive when taking it is
 * free and an action when it costs something -- and what this reading costs is a stopped line.
 * Without the second method the price is unenforceable and the instrument becomes a keystroke
 * with no decision in it, which is the thing 3 cuts.
 *
 * <h2>What is deliberately not here</h2>
 * There is no {@code wear(float)}. Absorbing force is still private to the one machine that does
 * it, because the <em>consequence</em> of a wasted blow is the machine's own business -- a bellows
 * takes the same surplus as nothing at all. This interface is the read side only, which is why an
 * instrument can depend on it without depending on any machine.
 */
public interface Wearing {

    /** How sound this machine is, 1 as built, falling to its own floor. */
    float getCondition();

    /**
     * Whether it is working right now, and so cannot be measured.
     * <p>
     * "Running" is drawing Su, which is the intuitive answer and the one 4b settled on. What
     * stands in for it today is the drive: a machine with a linkage turning in front of it is
     * working, and one with a stopped linkage is not. Equivalent in practice, and it works without
     * first making machine load fall to zero at rest -- which is a rotation-network change with
     * consequences for the Su ledger and wants deciding on its own merits.
     */
    boolean isRunning();
}
