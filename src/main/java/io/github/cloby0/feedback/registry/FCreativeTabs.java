package io.github.cloby0.feedback.registry;

import io.github.cloby0.feedback.Feedback;

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
                        output.accept(FItems.SHAFT.get());
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
                    })
                    .build());

    private FCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
