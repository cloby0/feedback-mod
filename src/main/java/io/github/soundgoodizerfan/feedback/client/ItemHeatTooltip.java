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
package io.github.soundgoodizerfan.feedback.client;

import io.github.soundgoodizerfan.feedback.Feedback;
import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.core.thermal.ItemHeat;
import io.github.soundgoodizerfan.feedback.core.unit.Tu;
import io.github.soundgoodizerfan.feedback.registry.FDataComponents;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * A hot item says so wherever it is looked at -- a hotbar, a chest, another mod's inventory --
 * because philosophy 9 means a workpiece carries its temperature with it, and a fact that costs
 * nothing to hold should not need a specific screen to show it (§8: senses give adjectives for
 * free; only a figure costs an instrument, and {@link Readout#temperatureReading} already draws
 * that line for whoever is carrying a thermometer).
 * <p>
 * Nothing here is specific to the thermal vessel's own GUI -- see {@code ThermalVesselScreen} for
 * the one thing this cannot cover: a workpiece still mid-cook has no stamped temperature at all
 * ({@link ItemHeat} is only set when an item leaves a slot), so a vessel's own screen adds the
 * vessel's live reading for exactly those slots.
 */
@EventBusSubscriber(modid = Feedback.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ItemHeatTooltip {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.has(FDataComponents.TEMPERATURE.get()))
            return;

        Tu tu = ItemHeat.get(stack, event.getEntity().level());
        if (tu.value() <= FTuning.WARM_TU.value())
            return;

        event.getToolTip().add(Readout.temperatureReading(tu));
    }
}
