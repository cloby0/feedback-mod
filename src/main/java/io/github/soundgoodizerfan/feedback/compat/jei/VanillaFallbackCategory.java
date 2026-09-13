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

import java.util.Locale;

import io.github.soundgoodizerfan.feedback.client.Readout;
import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.process.VanillaFallback;
import io.github.soundgoodizerfan.feedback.registry.FItems;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * A card for a pooled vanilla/modded cooking recipe that no hand-authored {@code ThermalProcess}
 * already claims -- see {@link FeedbackJeiPlugin} for where these are sourced and filtered.
 *
 * <h2>Not {@link ThermalProcessCategory}, on purpose</h2>
 * That card publishes exact figures because a hand-authored process is real published data (§8).
 * A pooled recipe has no such figures behind it -- only the same two floors every vessel already
 * enforces, {@link FTuning#FOOD_MAX_TU} and {@link FTuning#METAL_MIN_TU} -- so this card states
 * the floor as a free adjective, never as a number nobody actually wrote down.
 *
 * <h2>Which vessel can reach the floor is the card's business, not the catalyst list's</h2>
 * All three vessels are registered as this category's catalyst (see {@link FeedbackJeiPlugin}).
 * Whether the Smoker can actually get hot enough for a given card is a fact the printed floor
 * already states in words; deciding it by catalyst instead would be a whitelist by another name
 * (§15).
 */
public class VanillaFallbackCategory implements IRecipeCategory<RecipeHolder<AbstractCookingRecipe>> {

    @SuppressWarnings("unchecked")
    static final RecipeType<RecipeHolder<AbstractCookingRecipe>> TYPE = RecipeType.create(
            "feedback", "vanilla_fallback", (Class<RecipeHolder<AbstractCookingRecipe>>) (Class<?>) RecipeHolder.class);

    private static final int WIDTH = 168;
    private static final int HEIGHT = 66;

    private static final int LABEL_X = 24;
    private static final int VALUE_X = 76;

    private static final int TITLE_COLOUR = 0xFF3F3F3F;
    private static final int LABEL_COLOUR = 0xFF7A7A7A;
    private static final int VALUE_COLOUR = 0xFF262626;
    private static final int RULE_COLOUR = 0x40000000;

    private final IDrawable icon;

    public VanillaFallbackCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(FItems.FURNACE.get()));
    }

    @Override
    public RecipeType<RecipeHolder<AbstractCookingRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("feedback.jei.vanilla_fallback");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AbstractCookingRecipe> recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 13).setStandardSlotBackground().addIngredients(recipe.value().getIngredients().get(0));
        HolderLookup.Provider access = registryAccess();
        if (access != null)
            builder.addOutputSlot(1, 45).setOutputSlotBackground().addItemStack(recipe.value().getResultItem(access));
    }

    @Override
    public void draw(RecipeHolder<AbstractCookingRecipe> recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                      double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        HolderLookup.Provider access = registryAccess();
        String title = access == null ? "" : recipe.value().getResultItem(access).getHoverName().getString();
        graphics.drawString(font, title.toUpperCase(Locale.ROOT), 1, 1, TITLE_COLOUR, false);
        graphics.fill(1, 11, WIDTH - 1, 12, RULE_COLOUR);

        row(graphics, font, 30, "feedback.jei.requires", floor(recipe).getString());
    }

    /** The free adjective for whichever floor this recipe is gated by -- never a figure. */
    private static Component floor(RecipeHolder<AbstractCookingRecipe> recipe) {
        return VanillaFallback.isFood(recipe)
                ? Component.translatable("feedback.jei.floor.food")
                : Component.translatable("feedback.jei.floor.metal", Readout.temperature(FTuning.METAL_MIN_TU));
    }

    @org.jetbrains.annotations.Nullable
    private static HolderLookup.Provider registryAccess() {
        return Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.registryAccess();
    }

    private static void row(GuiGraphics graphics, Font font, int y, String labelKey, String value) {
        graphics.drawString(font, Component.translatable(labelKey), LABEL_X, y, LABEL_COLOUR, false);
        graphics.drawString(font, value, VALUE_X, y, VALUE_COLOUR, false);
    }
}
