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
 * Stress -- how much load a rotational network carries, and how much its sources can supply.
 *
 * <h2>Create's meaning, and never speed</h2>
 * Taken deliberately from Create, because a large number of players already know what a stress
 * unit is and re-teaching a familiar word with a new meaning would be a cost paid for nothing.
 * The units table is emphatic on the one thing that gets confused: {@code Su} is load, never
 * rotational speed. Speed is {@link Rpm}, they are independent, and a network can be fast and
 * unloaded or slow and overstressed.
 *
 * <h2>It sums network-wide, which is why distance is free and sprawl is not</h2>
 * Every machine's demand and every bearing's friction land in one ledger for the whole connected
 * run, so moving a machine nearer its generator saves nothing -- the shafts in between were
 * already turning. What costs is having more of them, including on a branch nobody is using.
 * That falls out of the ledger rather than being a rule, and it is the reason a factory's layout
 * is a real decision instead of a pipe-length optimisation.
 */
public record Su(float value) implements Unit {

    @Override
    public float raw() {
        return value;
    }

    @Override
    public String unitKey() {
        return "feedback.unit.su";
    }
}
