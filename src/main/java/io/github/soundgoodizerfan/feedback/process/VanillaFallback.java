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
import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;

/**
 * Philosophy 15's fallback: the compatibility shim that lets the Furnace, Smoker and Crude Blast
 * Furnace attempt any vanilla or modded cooking recipe, so that reworking the three of them
 * cannot make a single existing recipe uncraftable.
 *
 * <h2>Whitelists come off here, on purpose</h2>
 * Vanilla keeps three separate recipe types -- {@code smelting}, {@code blasting},
 * {@code smoking} -- as a stand-in for "which appliance this belongs in". §15 says that stand-in
 * has to go: nothing may declare the Smoker a food machine or the Blast Furnace an ore machine,
 * three numbers in {@code FTuning} do that instead. So every vessel searches all three recipe
 * types for a match on its own input, and whichever one exists is a fact about the <em>item</em>,
 * never about which block it happens to be sitting in.
 * <p>
 * A recipe's type is still read once it is found, but only as a heuristic for {@code isFood} --
 * vanilla's own {@code smoking} entries are already a statement "this is food", and reusing that
 * is cheaper and no less honest than hand-classifying every item in every installed mod. It is
 * not an identity check on the item; it is reading a fact vanilla already published about the
 * recipe.
 */
public final class VanillaFallback {

    private static final List<RecipeType<? extends AbstractCookingRecipe>> TYPES =
            List.of(RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.SMOKING);

    private VanillaFallback() {
    }

    /**
     * The best (shortest cooking time) cooking recipe across all three vanilla types for this
     * input, or empty if nothing anywhere claims it.
     */
    @SuppressWarnings("unchecked")
    public static Optional<RecipeHolder<AbstractCookingRecipe>> find(Level level, ItemStack input) {
        if (input.isEmpty())
            return Optional.empty();

        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        RecipeHolder<AbstractCookingRecipe> best = null;
        for (RecipeType<? extends AbstractCookingRecipe> type : TYPES) {
            RecipeHolder<AbstractCookingRecipe> match = (RecipeHolder<AbstractCookingRecipe>) (RecipeHolder<?>)
                    level.getRecipeManager()
                            .getRecipeFor((RecipeType<AbstractCookingRecipe>) type, recipeInput, level)
                            .orElse(null);
            if (match != null && (best == null || match.value().getCookingTime() < best.value().getCookingTime()))
                best = match;
        }
        return Optional.ofNullable(best);
    }

    /** Whether vanilla itself already calls this recipe food, per its {@code smoking} type. */
    public static boolean isFood(RecipeHolder<AbstractCookingRecipe> recipe) {
        return recipe.value() instanceof SmokingRecipe;
    }

    /** Work needed for this recipe to finish, scaled off its own declared cooking time. */
    public static float requiredWork(RecipeHolder<AbstractCookingRecipe> recipe,
                                      float workPer200Ticks) {
        return workPer200Ticks * (recipe.value().getCookingTime() / 200f);
    }
}
