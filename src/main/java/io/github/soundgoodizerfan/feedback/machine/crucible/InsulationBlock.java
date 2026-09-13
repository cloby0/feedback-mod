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
package io.github.soundgoodizerfan.feedback.machine.crucible;

import net.minecraft.world.level.block.Block;

/**
 * A block of packed insulation. Build it against a vessel and the vessel leaks less.
 *
 * <h2>The most literal "upgrades are physical components" in the mod</h2>
 * Philosophy 4 says an upgrade is something you could point at, and this is a block you stack
 * around a thing. How well it works depends on how much of the thing you covered, which is not a
 * rule anybody wrote -- the crucible counts its neighbours. There is no upgrade slot, no tier and
 * no percentage.
 * <p>
 * It has no block entity and no behaviour. All of the logic is on the vessel, which is correct:
 * insulation does not <em>do</em> anything, it is simply in the way.
 *
 * <h2>And it is not always right</h2>
 * It slows heating as well as cooling. Steel wants thirty seconds inside a 60 Tu band, so a large
 * insulated crucible wins outright -- its sluggishness is the feature and it will hold the band
 * almost by itself. A process that needs to <em>move</em> would rather have neither. Neither
 * option is better, which is the test §5 sets for any upgrade worth having.
 */
public class InsulationBlock extends Block {

    public InsulationBlock(Properties properties) {
        super(properties);
    }
}
