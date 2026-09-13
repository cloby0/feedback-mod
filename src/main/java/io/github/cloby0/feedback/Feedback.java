package io.github.cloby0.feedback;

import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.registry.FCapabilities;
import io.github.cloby0.feedback.registry.FBlocks;
import io.github.cloby0.feedback.registry.FCreativeTabs;
import io.github.cloby0.feedback.registry.FDataComponents;
import io.github.cloby0.feedback.registry.FItems;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import io.github.cloby0.feedback.net.DeformationSyncPayload;
import io.github.cloby0.feedback.process.DeformationTable;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
        FCapabilities.register(modBus);

        NeoForge.EVENT_BUS.addListener(Feedback::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(Feedback::onDatapackSync);
        modBus.addListener(Feedback::registerPayloads);
    }

    /**
     * The deformation table is datapack data, so it reloads with the server rather than with
     * resources. Only the server needs it: the hammer runs server-side and nothing on the client
     * asks what a material becomes.
     */
    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DeformationTable.get());
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                DeformationSyncPayload.TYPE, DeformationSyncPayload.STREAM_CODEC, DeformationSyncPayload::handle);
    }

    /**
     * Hand the client the deformation table whenever the server's copy could have changed.
     * <p>
     * The client needs it only to display requirements, which philosophy 8 gives away for free.
     * A null player means this was a {@code /reload} rather than a login, so everybody gets it.
     */
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        DeformationSyncPayload payload = new DeformationSyncPayload(DeformationTable.get().entries());
        if (event.getPlayer() != null)
            PacketDistributor.sendToPlayer(event.getPlayer(), payload);
        else
            PacketDistributor.sendToAllPlayers(payload);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
