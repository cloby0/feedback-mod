package io.github.cloby0.feedback.compat.jei;

import java.util.List;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.process.ClientDeformations;
import io.github.cloby0.feedback.process.Deformation;
import io.github.cloby0.feedback.registry.FItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

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
 */
@JeiPlugin
public class FeedbackJeiPlugin implements IModPlugin {

    @Nullable
    private static IJeiRuntime runtime;
    @Nullable
    private static List<Deformation> published;

    @Override
    public ResourceLocation getPluginUid() {
        return Feedback.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DeformationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * The hammer is listed as the thing that performs the operation, which is true and is not an
     * identity check: it says "this machine deforms", not "this machine makes copper plate".
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(DeformationCategory.TYPE, FItems.MECHANICAL_HAMMER.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        published = null;
        ClientDeformations.onChanged(FeedbackJeiPlugin::publish);
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        published = null;
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
}
