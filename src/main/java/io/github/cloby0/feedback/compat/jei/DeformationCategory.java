package io.github.cloby0.feedback.compat.jei;

import io.github.cloby0.feedback.client.Readout;
import io.github.cloby0.feedback.process.Deformation;
import io.github.cloby0.feedback.registry.FItems;

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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A process card: what mechanical deformation does to one material, stated in exact units.
 *
 * <h2>A spec sheet, not a recipe</h2>
 * Every figure here is a <em>requirement</em>, and philosophy 8 says requirements are free and
 * exact. A player may read that copper wants 14 Fu on their first day, own nothing, and still fail
 * repeatedly -- knowing the target and knowing where you are are different problems, and only the
 * second is for sale. So the card prints figures without apology.
 *
 * <h2>Why the card looks like this</h2>
 * It deliberately does not look like a crafting recipe. There is no arrow implying a machine that
 * performs a transformation on request, because no such machine exists: the hammer applies force to
 * whatever is in front of it and this table describes how the <em>material</em> answers. Labelled
 * rows read as a handbook entry for a material, which is what it is.
 * <p>
 * The operation name lives on the category tab rather than on the card for the same reason. The
 * card is about copper; "mechanical deformation" is the shelf it sits on.
 */
public class DeformationCategory implements IRecipeCategory<Deformation> {

    static final RecipeType<Deformation> TYPE =
            RecipeType.create("feedback", "mechanical_deformation", Deformation.class);

    private static final int WIDTH = 168;
    private static final int HEIGHT = 66;

    /** Where the labels start, clear of the slot column, and where their values line up. */
    private static final int LABEL_X = 24;
    private static final int VALUE_X = 76;

    private final IDrawable icon;

    public DeformationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(FItems.MECHANICAL_HAMMER.get()));
    }

    @Override
    public RecipeType<Deformation> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("feedback.jei.mechanical_deformation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, Deformation recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 13).setStandardSlotBackground().addIngredients(recipe.input());
        builder.addOutputSlot(1, 45).setOutputSlotBackground().addItemStack(recipe.result());
    }

    @Override
    public void draw(Deformation recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, recipe.result().getHoverName().getString().toUpperCase(java.util.Locale.ROOT),
                1, 1, 0xFFFFFFFF, false);

        ItemStack[] accepted = recipe.input().getItems();
        row(graphics, font, 17, "feedback.jei.input", quantity(accepted.length == 0 ? ItemStack.EMPTY : accepted[0]));
        row(graphics, font, 29, "feedback.jei.work", recipe.work() + " Fu");
        row(graphics, font, 41, "feedback.jei.hardness", Readout.number(recipe.hardness()));
        row(graphics, font, 53, "feedback.jei.output", quantity(recipe.result()));
    }

    private static void row(GuiGraphics graphics, Font font, int y, String labelKey, String value) {
        graphics.drawString(font, Component.translatable(labelKey), LABEL_X, y, 0xFF808080, false);
        graphics.drawString(font, value, VALUE_X, y, 0xFFE0E0E0, false);
    }

    private static String quantity(ItemStack stack) {
        return stack.isEmpty() ? "-" : stack.getCount() + " × " + stack.getHoverName().getString();
    }
}
