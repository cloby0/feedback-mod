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

import net.minecraft.world.item.ItemStack;

/**
 * A physical addon installed on a machine -- philosophy §4's "upgrades are physical components
 * you could point at," widened to cover the other two shapes that rule turned out to imply.
 *
 * <h2>Not GregTech's cover, on purpose</h2>
 * GTCEu's cover system (read for this, see {@code TODO.md} §4d) is one concept: a sided
 * attachment that filters a face's I/O. Feedback needs three, and they don't share a shape:
 * a {@link io.github.soundgoodizerfan.feedback.instrument.Instrument Sensor} is sided and
 * read-only, an {@code Upgrade} is unsided and changes the holder's own numbers, and an
 * {@code Adapter} is sided like a sensor but grants the holder a second energy type to run on
 * (a motor bolted to a mechanical machine, a coil dropped into a crucible). Calling all three
 * "cover" would have been the GTCEu name wearing a shape it was never built for, so this mod
 * uses its own -- see {@code THIRD-PARTY-LICENSES.md} for what was and wasn't taken.
 * <p>
 * What the three do share is this interface: something a player attached, that can be picked
 * back up, and that can react to being attached or removed.
 */
public interface Fitting {

    /** The stack this fitting drops as, if removed intact. */
    ItemStack getPickItem();

    /** Called once, after the holder has recorded this fitting as attached. */
    default void onAttached() {
    }

    /** Called once, before the holder forgets this fitting. */
    default void onRemoved() {
    }
}
