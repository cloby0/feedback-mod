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
package io.github.soundgoodizerfan.feedback.registry;

import io.github.soundgoodizerfan.feedback.Feedback;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Feedback.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.feedback"))
                    .icon(() -> new ItemStack(FItems.SHAFT.get()))
                    .displayItems((params, output) -> {
                        output.accept(FItems.HAND_HAMMER.get());
                        output.accept(FItems.SHAFT.get());
                        output.accept(FItems.SMALL_COG.get());
                        output.accept(FItems.LARGE_COG.get());
                        output.accept(FItems.GEARBOX.get());
                        output.accept(FItems.HAND_CRANK.get());
                        output.accept(FItems.WATER_WHEEL.get());
                        output.accept(FItems.CRANK_LINKAGE.get());
                        output.accept(FItems.MECHANICAL_HAMMER.get());
                        output.accept(FItems.CLUTCH.get());
                        output.accept(FItems.TIMER.get());
                        output.accept(FItems.CALIPERS.get());
                        output.accept(FItems.COPPER_PLATE.get());
                        output.accept(FItems.COPPER_FOIL.get());
                        output.accept(FItems.COPPER_SCRAP.get());
                        output.accept(FItems.FIREBOX.get());
                        output.accept(FItems.SMALL_CRUCIBLE.get());
                        output.accept(FItems.LARGE_CRUCIBLE.get());
                        output.accept(FItems.INSULATION.get());
                        output.accept(FItems.BELLOWS.get());
                        output.accept(FItems.BIMETALLIC_STRIP.get());
                        output.accept(FItems.THERMOMETER.get());
                        output.accept(FItems.STEEL_INGOT.get());
                        output.accept(FItems.BURNT_IRON.get());
                        output.accept(FItems.HARDENED_STEEL.get());
                        output.accept(FItems.STEEL_PLATE.get());
                    })
                    .build());

    private FCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
