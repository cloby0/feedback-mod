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
package io.github.soundgoodizerfan.feedback.process;

import java.util.List;

/**
 * The client's copy of the thermal process table, kept only so a recipe browser can print it.
 *
 * <h2>Why the figures are given away</h2>
 * Philosophy 8: what a process <em>requires</em> is published data and costs nothing. Steel's
 * window is exactly 1420 to 1480 Tu and stating so on day one, with nothing owned, gates nothing
 * -- the player still cannot tell what temperature they are at. That is the gap instruments are
 * sold into, and it is a much better gap than one made by hiding a number.
 * <p>
 * Nothing here participates in the simulation. The crucible never reads this and no outcome is
 * ever computed from it. It is a printed handbook.
 */
public final class ClientThermalProcesses {

    private static List<ThermalProcess> entries = List.of();
    private static Runnable listener = () -> {
    };

    private ClientThermalProcesses() {
    }

    public static List<ThermalProcess> get() {
        return entries;
    }

    public static void set(List<ThermalProcess> incoming) {
        entries = List.copyOf(incoming);
        listener.run();
    }

    /** Called whenever the table is replaced. One listener is enough; only JEI wants this. */
    public static void onChanged(Runnable onChanged) {
        listener = onChanged;
        onChanged.run();
    }
}
