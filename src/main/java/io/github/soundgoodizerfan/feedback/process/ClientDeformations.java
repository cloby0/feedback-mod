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
 * The client's copy of the deformation table, kept only so a recipe browser can print it.
 *
 * <h2>Why a copy exists at all</h2>
 * {@link DeformationTable} is datapack data and therefore server-side; philosophy 8 nonetheless
 * says a <em>requirement</em> is free and exact, which means the figures have to reach the browser
 * somehow. So the server ships the table on datapack sync and this holds it.
 * <p>
 * Nothing here participates in the simulation. The hammer never reads this, and no outcome is ever
 * computed from it -- it is a printed handbook, not a second source of truth.
 *
 * <h2>The listener</h2>
 * JEI has to be told when the table arrives, but this class must not import JEI: it is loaded even
 * when JEI is absent. So the dependency runs the other way -- the JEI plugin leaves a callback
 * here, and an absent JEI simply never leaves one.
 */
public final class ClientDeformations {

    private static List<Deformation> entries = List.of();
    private static Runnable listener = () -> {
    };

    private ClientDeformations() {
    }

    public static List<Deformation> get() {
        return entries;
    }

    public static void set(List<Deformation> incoming) {
        entries = List.copyOf(incoming);
        listener.run();
    }

    /** Called whenever the table is replaced. One listener is enough; only JEI wants this. */
    public static void onChanged(Runnable onChanged) {
        listener = onChanged;
        onChanged.run();
    }
}
