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
package io.github.cloby0.feedback.control;

/**
 * Something a control device can start and stop.
 *
 * <h2>Why this interface exists at all</h2>
 * Philosophy 13: the controller is a switch, never a dial. Its only output is starting or
 * stopping a supply, so everything that can be controlled presents exactly this — one boolean,
 * no target, no setpoint, no proportional anything.
 * <p>
 * Keeping it this narrow is what lets slice 2's controller <em>replace</em> the Timer instead of
 * being a new concept bolted alongside it. Anything that can switch a clutch can switch every
 * future actuator, and no actuator ever needs to know what is switching it.
 */
public interface Switchable {

    void setEngaged(boolean engaged);

    boolean isEngaged();
}
