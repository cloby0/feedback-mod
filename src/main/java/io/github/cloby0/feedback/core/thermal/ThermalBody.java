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
package io.github.cloby0.feedback.core.thermal;

import io.github.cloby0.feedback.core.FTuning;

/**
 * Something that holds a temperature.
 *
 * <h2>Tu is a state, not an amount</h2>
 * The units table is strict about this and it shapes the whole interface. A body does not have
 * "some heat in it" that could be added to a bucket and carried elsewhere; it is <em>at</em> a
 * temperature, and what moves between bodies is {@code Work}. So there is no {@code getHeat()}
 * here and never will be. There is a temperature, a mass that says what a unit of Work does to
 * it, and a leak that says how fast the room takes it back.
 *
 * <h2>Why this is one interface across vessels and workpieces</h2>
 * Philosophy 9: a hot item is hot wherever it is, which means the thing that knows how to cool
 * is not the crucible. It is the general relationship, and the crucible, the firebox and the
 * ingot in your pocket are all cases of it. Writing it once is also the only way the numbers
 * stay consistent -- two implementations of Newton's law drift apart the first time one of them
 * is tuned.
 */
public interface ThermalBody {

    /** Current temperature, in Tu. */
    float getTemperature();

    void setTemperature(float tu);

    /**
     * Work required to raise this body by one Tu.
     * <p>
     * This is the number an upgrade changes. A larger vessel is not faster or more efficient --
     * it is heavier, and everything the player notices about it follows from that.
     */
    float getThermalMass();

    /** Work per tick this body loses per Tu it sits above ambient. */
    default float getLeak() {
        return FTuning.VESSEL_LEAK;
    }

    /**
     * Whether an instrument can be got at the inside of this body.
     *
     * <h3>This is the wall beat 2 is built around</h3>
     * The slice's best thermal vessel is the sealed blast furnace, and everything good about it
     * follows from being closed. A vessel that can be measured has to be <em>opened</em>, which
     * costs it exactly the stability that made the sealed one good. So the player's best vessel
     * and their first instrument are mutually exclusive, and no amount of iron fixes it -- the
     * way out is a vessel designed to be measured, which is what the crucible is for.
     * <p>
     * Modelling it here rather than as a rule about which instruments attach to what keeps a
     * thermometer a plain carried instrument: it reads whatever will let itself be read.
     */
    default boolean hasThermowell() {
        return true;
    }
}
