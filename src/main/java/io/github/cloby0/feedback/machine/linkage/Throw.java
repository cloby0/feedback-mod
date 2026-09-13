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

import net.minecraft.util.StringRepresentable;

/**
 * How far the crank throws, which is the only setting it has.
 *
 * <h2>Two options, deliberately</h2>
 * Philosophy 3: a crank throw that is short or long is a decision, while one that is any number
 * from 1 to 64 is a spreadsheet with a correct answer hidden in it.
 *
 * <h2>What a throw actually trades</h2>
 * A lever exchanges force for distance and the work per stroke is the same either way. So throw
 * does <em>not</em> change how fast a hammer flattens anything -- it changes only how hard each
 * blow lands, which matters when a material has a minimum force below which nothing happens.
 * <p>
 * <h2>Why this is the slice's sharpest lesson</h2>
 * Copper is soft enough that a short throw <em>overshoots a whole stage</em>: one blow leaves an
 * ingot, the second carries it past plate and straight into foil. Plate is not merely quick to
 * pass, it is unreachable at that setting. The long throw reaches plate in five and gives three
 * blows to grab it.
 * <p>
 * So the two settings do not make the same product faster or slower -- they make
 * <em>different products</em>, chosen by how the machine is geared rather than by any recipe
 * selector. A short-throw hammer is a foil machine.
 * <p>
 * It is also why the same part reads backwards on a bellows, where the useful output is
 * displaced air rather than work, and a longer stroke simply moves more of it.
 */
public enum Throw implements StringRepresentable {

    SHORT("short"),
    LONG("long");

    private final String name;

    Throw(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
