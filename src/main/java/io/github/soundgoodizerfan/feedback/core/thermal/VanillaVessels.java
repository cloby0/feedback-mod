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
package io.github.soundgoodizerfan.feedback.core.thermal;

import java.util.Optional;

import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.core.unit.Tu;
import io.github.soundgoodizerfan.feedback.process.Fuel;
import io.github.soundgoodizerfan.feedback.process.FuelTable;
import io.github.soundgoodizerfan.feedback.process.VanillaFallback;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * §15's rework, run on whichever vanilla furnace-family block entity a mixin hands it. Every
 * mixin does is: shadow four fields, cast {@code this} through the interfaces below, forward the
 * whole tick here, and translate the result back into the vanilla fields the block and its GUI
 * still read. This class never touches a shadowed field itself, so it can be read and changed
 * without knowing a single thing about Mixin.
 *
 * <h2>The vessel is its own fire</h2>
 * Unlike the crucible, there is no separate firebox below it -- {@link #tick} lights the fuel
 * slot directly off {@link FuelTable}, exactly the table the firebox already reads, so a pack that
 * teaches its own coke a hotter flame teaches it to the Blast Furnace for free.
 *
 * <h2>Recipes are pooled, not routed</h2>
 * See {@link VanillaFallback}. Every vessel searches all three vanilla cooking types for the
 * input's own recipe; only the vessel's own temperature decides whether it can finish, never
 * which block it is sitting in.
 */
public final class VanillaVessels {

    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    private static final int SLOT_RESULT = 2;

    /** What the mixin needs back to keep the flame icon, the recipe arrow and the LIT state honest. */
    public record TickResult(boolean lit, int fuelTicksLeft, int fuelDuration, int progress, int progressTotal) {
    }

    private VanillaVessels() {
    }

    public static <T extends BlockEntity & Container & RecipeCraftingHolder & ThermalBody
            & HeatSource & VanillaVessel> TickResult tick(Level level, T vessel) {
        if (vessel.getFuelTicksLeft() <= 0)
            light(level, vessel);

        boolean lit = vessel.getFuelTicksLeft() > 0;
        if (lit)
            vessel.setFuelTicksLeft(vessel.getFuelTicksLeft() - 1);

        Tu fireTu = lit ? new Tu(vessel.getFlameRollTu()) : FTuning.AMBIENT_TU;
        Heat.tick(vessel, fireTu, lit);
        if (vessel.getTemperature().value() > vessel.getBandCeilingTu())
            vessel.setTemperature(new Tu(vessel.getBandCeilingTu()));

        int progress = 0;
        int progressTotal = 100;
        Optional<RecipeHolder<AbstractCookingRecipe>> maybe =
                VanillaFallback.find(level, vessel.getItem(SLOT_INPUT));
        if (maybe.isPresent())
            progress = advance(level, vessel, maybe.get());

        return new TickResult(lit, vessel.getFuelTicksLeft(), vessel.getFuelDuration(), progress, progressTotal);
    }

    private static <T extends VanillaVessel & Container> void light(Level level, T vessel) {
        ItemStack fuelStack = vessel.getItem(SLOT_FUEL);
        Optional<Fuel> found = FuelTable.get().find(fuelStack);
        if (found.isEmpty())
            return;

        Fuel burning = found.get();
        vessel.setFuelTicksLeft(burning.duration());
        vessel.setFuelDuration(burning.duration());
        // Rolled once and held for the whole burn -- see FireboxBlockEntity for why per-tick
        // noise would be the same variance with nothing in it to learn.
        float roll = 1f + (level.random.nextFloat() * 2f - 1f) * burning.spread();
        vessel.setFlameRollTu(burning.temperature() * roll);

        fuelStack.shrink(1);
        if (fuelStack.isEmpty() && fuelStack.hasCraftingRemainingItem())
            vessel.setItem(SLOT_FUEL, fuelStack.getCraftingRemainingItem());
    }

    /** @return the display progress, 0-100, for whichever recipe is in the input slot right now. */
    private static <T extends Container & RecipeCraftingHolder & ThermalBody & VanillaVessel>
    int advance(Level level, T vessel, RecipeHolder<AbstractCookingRecipe> recipe) {
        ItemStack input = vessel.getItem(SLOT_INPUT);
        float temperature = vessel.getTemperature().value();
        boolean food = VanillaFallback.isFood(recipe);

        // Overshoot destroys outright, the instant it crosses the line -- no grace period, the
        // same rule the crucible's own carburizing hold uses for iron above 1540 Tu.
        if (food && temperature > FTuning.FOOD_MAX_TU.value()) {
            input.shrink(1);
            vessel.setAccumulatedWork(0);
            return 0;
        }

        boolean inBand = food || temperature >= FTuning.METAL_MIN_TU.value();
        if (!inBand)
            return progressOf(vessel, recipe);

        vessel.setAccumulatedWork(vessel.getAccumulatedWork()
                + Math.max(0, temperature - FTuning.AMBIENT_TU.value()));

        float required = VanillaFallback.requiredWork(recipe, FTuning.FALLBACK_WORK_PER_200_TICKS);
        if (vessel.getAccumulatedWork() >= required) {
            complete(level, vessel, recipe, input);
            vessel.setAccumulatedWork(0);
            return 0;
        }
        return progressOf(vessel, recipe);
    }

    private static int progressOf(VanillaVessel vessel, RecipeHolder<AbstractCookingRecipe> recipe) {
        float required = VanillaFallback.requiredWork(recipe, FTuning.FALLBACK_WORK_PER_200_TICKS);
        if (required <= 0)
            return 0;
        return (int) Math.min(100, 100 * vessel.getAccumulatedWork() / required);
    }

    /** Mirrors vanilla's own canBurn/burn stack-size rules, just off our own accrual instead of ticks. */
    private static <T extends Container & RecipeCraftingHolder>
    void complete(Level level, T vessel, RecipeHolder<AbstractCookingRecipe> recipe, ItemStack input) {
        ItemStack result = recipe.value().assemble(new SingleRecipeInput(input), level.registryAccess());
        if (result.isEmpty())
            return;

        ItemStack existing = vessel.getItem(SLOT_RESULT);
        if (existing.isEmpty()) {
            vessel.setItem(SLOT_RESULT, result.copy());
        } else {
            if (!ItemStack.isSameItemSameComponents(existing, result))
                return;
            if (existing.getCount() + result.getCount() > existing.getMaxStackSize())
                return;
            existing.grow(result.getCount());
        }

        input.shrink(1);
        vessel.setRecipeUsed(recipe);
    }
}
