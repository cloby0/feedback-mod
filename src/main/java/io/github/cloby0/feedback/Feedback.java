package io.github.cloby0.feedback;

import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.registry.FBlocks;
import io.github.cloby0.feedback.registry.FCreativeTabs;
import io.github.cloby0.feedback.registry.FDataComponents;
import io.github.cloby0.feedback.registry.FItems;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import io.github.cloby0.feedback.process.DeformationTable;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Feedback.MOD_ID)
public class Feedback {

    public static final String MOD_ID = "feedback";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Feedback(IEventBus modBus, ModContainer container) {
        FBlocks.register(modBus);
        FItems.register(modBus);
        FBlockEntities.register(modBus);
        FCreativeTabs.register(modBus);
        FDataComponents.register(modBus);

        NeoForge.EVENT_BUS.addListener(Feedback::onAddReloadListeners);
    }

    /**
     * The deformation table is datapack data, so it reloads with the server rather than with
     * resources. Only the server needs it: the hammer runs server-side and nothing on the client
     * asks what a material becomes.
     */
    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DeformationTable.get());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
