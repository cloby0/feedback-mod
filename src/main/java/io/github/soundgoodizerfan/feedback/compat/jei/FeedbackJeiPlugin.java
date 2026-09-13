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
package io.github.soundgoodizerfan.feedback.compat.jei;

import java.util.List;
import java.util.stream.Stream;

import io.github.soundgoodizerfan.feedback.Feedback;
import io.github.soundgoodizerfan.feedback.process.ClientDeformations;
import io.github.soundgoodizerfan.feedback.process.ClientThermalProcesses;
import io.github.soundgoodizerfan.feedback.process.Deformation;
import io.github.soundgoodizerfan.feedback.process.ThermalProcess;
import io.github.soundgoodizerfan.feedback.registry.FItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Puts the deformation table in the recipe browser without pretending it is a recipe book.
 *
 * <h2>No vanilla recipe type, deliberately</h2>
 * JEI's {@code IRecipeCategory<T>} is generic over any type at all, so a category can be built
 * straight from {@link Deformation} -- which means the mod never has to register a vanilla
 * {@code RecipeType}, never appears in the recipe book, and never acquires an API that would let
 * something ask the hammer what it can make. The hammer genuinely does not know; a compatibility
 * layer that implied otherwise would be the mod lying about its own central claim.
 *
 * <h2>Why the recipes arrive late</h2>
 * JEI starts its runtime from the vanilla recipe sync, which lands during the configuration phase,
 * while our table arrives on datapack sync in the play phase. So {@code registerRecipes} would
 * almost always see an empty table on a first join, and adding there as well as here would produce
 * every card twice. Pushing once, from the runtime, is the version with one code path.
 * <p>
 * {@code hideRecipes} before each push is what makes a second push idempotent: a {@code /reload}
 * can deliver a fresh table without JEI restarting, and without this the old cards would linger
 * alongside the new ones.
 *
 * <h2>Vanilla's own categories are hidden here too</h2>
 * The Furnace, Smoker and Blast Furnace never touch a vanilla block class any more (§4f), so
 * JEI's built-in smelting/blasting/smoking categories describe blocks that no longer exist in a
 * normal playthrough. {@link VanillaFallbackCategory} replaces them with cards for whatever
 * {@link io.github.soundgoodizerfan.feedback.process.VanillaFallback} would actually run, pooled
 * straight from JEI's own recipe manager rather than a second search.
 */
@JeiPlugin
public class FeedbackJeiPlugin implements IModPlugin {

    @Nullable
    private static IJeiRuntime runtime;
    @Nullable
    private static List<Deformation> published;
    @Nullable
    private static List<ThermalProcess> publishedThermal;
    @Nullable
    private static List<RecipeHolder<AbstractCookingRecipe>> publishedFallback;

    @Override
    public ResourceLocation getPluginUid() {
        return Feedback.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new DeformationCategory(registration.getJeiHelpers().getGuiHelper()),
                new ThermalProcessCategory(registration.getJeiHelpers().getGuiHelper()),
                new VanillaFallbackCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * The hammer is listed as the thing that performs the operation, which is true and is not an
     * identity check: it says "this machine deforms", not "this machine makes copper plate".
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(DeformationCategory.TYPE, FItems.MECHANICAL_HAMMER.get());
        // Both crucibles, and not the firebox: the firebox is a fire and has no opinion about
        // materials at all. A vessel is where a thermal process happens.
        registration.addRecipeCatalysts(ThermalProcessCategory.TYPE,
                FItems.SMALL_CRUCIBLE.get(), FItems.LARGE_CRUCIBLE.get());
        // All three, deliberately, even though the Smoker will never clear METAL_MIN_TU: which
        // one can actually reach a card's printed floor is a fact the card states in words, not a
        // fact this list gets to decide (§15, no whitelists).
        registration.addRecipeCatalysts(VanillaFallbackCategory.TYPE,
                FItems.FURNACE.get(), FItems.SMOKER.get(), FItems.BLAST_FURNACE.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        published = null;
        publishedThermal = null;
        publishedFallback = null;
        // Our vessels are the only things left that can run these; vanilla's own built-in
        // categories now describe blocks that are permanently inert (§4f).
        IRecipeManager recipes = jeiRuntime.getRecipeManager();
        recipes.hideRecipeCategory(RecipeTypes.SMELTING);
        recipes.hideRecipeCategory(RecipeTypes.BLASTING);
        recipes.hideRecipeCategory(RecipeTypes.SMOKING);
        ClientDeformations.onChanged(FeedbackJeiPlugin::publish);
        ClientThermalProcesses.onChanged(() -> {
            publishThermal();
            publishFallback();
        });
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        published = null;
        publishedThermal = null;
        publishedFallback = null;
    }

    private static void publish() {
        if (runtime == null)
            return;
        IRecipeManager recipes = runtime.getRecipeManager();
        if (published != null)
            recipes.hideRecipes(DeformationCategory.TYPE, published);
        published = ClientDeformations.get();
        recipes.addRecipes(DeformationCategory.TYPE, published);
    }

    private static void publishThermal() {
        if (runtime == null)
            return;
        IRecipeManager recipes = runtime.getRecipeManager();
        if (publishedThermal != null)
            recipes.hideRecipes(ThermalProcessCategory.TYPE, publishedThermal);
        publishedThermal = ClientThermalProcesses.get();
        recipes.addRecipes(ThermalProcessCategory.TYPE, publishedThermal);
    }

    /**
     * Sourced from JEI's own recipe manager, not a second pooling pass -- {@code createRecipeLookup}
     * already returns the same deduplicated, cross-mod recipes {@link
     * io.github.soundgoodizerfan.feedback.process.VanillaFallback#find} searches at runtime.
     * Filtered against the client's copy of {@link io.github.soundgoodizerfan.feedback.process.ThermalProcessTable}
     * so steel does not also show up as a generic smelting card once something hand-authored
     * already claims its input.
     */
    private static void publishFallback() {
        if (runtime == null)
            return;
        IRecipeManager recipes = runtime.getRecipeManager();
        List<ThermalProcess> handAuthored = ClientThermalProcesses.get();
        List<RecipeHolder<AbstractCookingRecipe>> pooled = Stream.of(
                        lookup(recipes, RecipeTypes.SMELTING),
                        lookup(recipes, RecipeTypes.BLASTING),
                        lookup(recipes, RecipeTypes.SMOKING))
                .flatMap(stream -> stream)
                .filter(recipe -> !claimed(recipe, handAuthored))
                .toList();
        if (publishedFallback != null)
            recipes.hideRecipes(VanillaFallbackCategory.TYPE, publishedFallback);
        publishedFallback = pooled;
        recipes.addRecipes(VanillaFallbackCategory.TYPE, publishedFallback);
    }

    @SuppressWarnings("unchecked")
    private static <R extends AbstractCookingRecipe> Stream<RecipeHolder<AbstractCookingRecipe>> lookup(
            IRecipeManager recipes, RecipeType<RecipeHolder<R>> type) {
        return recipes.createRecipeLookup(type).get()
                .map(holder -> (RecipeHolder<AbstractCookingRecipe>) (RecipeHolder<?>) holder);
    }

    /** Whether some hand-authored {@link ThermalProcess} already answers for this input. */
    private static boolean claimed(RecipeHolder<AbstractCookingRecipe> recipe, List<ThermalProcess> handAuthored) {
        Ingredient recipeInput = recipe.value().getIngredients().get(0);
        for (ThermalProcess process : handAuthored)
            for (Ingredient input : process.inputs())
                for (ItemStack stack : recipeInput.getItems())
                    if (input.test(stack))
                        return true;
        return false;
    }
}
