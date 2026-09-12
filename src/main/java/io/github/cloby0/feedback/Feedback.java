package io.github.cloby0.feedback;

import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.registry.FBlocks;
import io.github.cloby0.feedback.registry.FCreativeTabs;
import io.github.cloby0.feedback.registry.FItems;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Feedback.MOD_ID)
public class Feedback {

    public static final String MOD_ID = "feedback";

    public Feedback(IEventBus modBus, ModContainer container) {
        FBlocks.register(modBus);
        FItems.register(modBus);
        FBlockEntities.register(modBus);
        FCreativeTabs.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
