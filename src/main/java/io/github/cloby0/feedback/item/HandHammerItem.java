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
package io.github.cloby0.feedback.item;

import io.github.cloby0.feedback.core.FTuning;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The slowest way to make anything, and it never stops working.
 *
 * <h2>Why a hand hammer exists at all</h2>
 * Philosophy 7: <em>manual production stays theoretically possible for a surprising share of the
 * game, at tiny rates.</em> Automation makes a process practical; it does not unlock it. Without
 * this item that promise had nothing behind it -- a player's first copper plate required a
 * Mechanical Hammer, and a Mechanical Hammer required copper plates, which is a hard gate standing
 * exactly where the philosophy says there is no physical impossibility.
 *
 * <h2>It swings in a crafting grid, not at a block</h2>
 * Hammer and workpiece in any crafting table; one craft is one blow. The alternative -- right-click
 * a block in the world -- was considered and dropped for needing a surface to hit, which is the
 * bootstrap problem all over again, and for being a fifth sneak-click handler immediately after
 * four were deleted.
 *
 * <p>The grid route earns something the block route could not. A craft is atomic, so five crafts
 * make a plate and a sixth makes foil -- the {@code WORK} component accumulates on the ingot
 * exactly as it does on the anvil (see {@code HandDeformationRecipe}). Shift-clicking runs the
 * whole stack straight past plate into foil into scrap, which is beat 1's entire lesson delivered
 * by the player's own hand, at their own pace, on the material chosen for being forgiving.
 *
 * <h2>Where the reference came from</h2>
 * The durability mechanism is GregTech CEu Modern's shape (LGPL-3.0, conveyable under GPL-3.0):
 * a tool is an ordinary crafting ingredient whose <em>remainder</em> is itself, one point more
 * damaged. See {@code IGTTool#definition$getCraftingRemainingItem}. Read, not copied.
 *
 * <p>Deliberately <em>not</em> taken from GregTech: its tool ingredients are matched by tag and
 * indexed by a symbol in a recipe pattern, which is a system for the nine crafting tools it has.
 * One tool needs no registry of tools.
 *
 * <p>Deliberately not taken from TerraFirmaCraft either, which solves the same problem with an
 * anvil block and a forging minigame. That is a far larger and far better mechanic than this slice
 * has any use for; hand work here is meant to be tedious, not skilful.
 *
 * <h2>It does not wear on a wasted swing</h2>
 * The Mechanical Hammer absorbs force that cannot go into the work, because a machine geared past
 * what its head can take is a mistake the machine records. A hand hammer has no equivalent: a swing
 * that cannot land is a craft the grid never offers, so nothing is spent and nothing is damaged.
 * You cannot mis-swing at something you were never able to lift.
 */
public class HandHammerItem extends Item {

    public HandHammerItem(Properties properties) {
        super(properties);
    }

    /**
     * How hard this lands, in St. A property of the tool, divided by the material's hardness to
     * find what a blow actually accomplishes (§17) -- which is why 3 St is a plate in five swings
     * and is simply nothing at all against steel's hardness of 15.
     */
    public float getStrength() {
        return FTuning.HAND_HAMMER_ST;
    }

    /**
     * The hammer survives the craft, one point worse.
     *
     * <p>Vanilla's {@code Recipe#getRemainingItems} calls this for every slot, so the recipe itself
     * needs no durability code. A hammer damaged past its limit returns empty and is destroyed,
     * which is vanilla's own behaviour for a broken tool rather than a rule invented here.
     */
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.setDamageValue(remainder.getDamageValue() + 1);
        return remainder.getDamageValue() >= remainder.getMaxDamage() ? ItemStack.EMPTY : remainder;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
