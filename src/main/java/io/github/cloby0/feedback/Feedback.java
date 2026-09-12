package io.github.cloby0.feedback;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;

@Mod(Feedback.MOD_ID)
public class Feedback {
    public static final String MOD_ID = "feedback";

    public Feedback(IEventBus modBus, ModContainer container) {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
