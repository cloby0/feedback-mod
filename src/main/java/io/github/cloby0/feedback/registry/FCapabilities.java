package io.github.cloby0.feedback.registry;

import io.github.cloby0.feedback.machine.hammer.HammerItemHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * What the mod's blocks expose to other blocks. One place to look, like the registries beside it.
 * <p>
 * Not a {@code DeferredRegister} -- capabilities are attached on an event rather than registered
 * -- but the reason for gathering them here is the same. A machine that can be piped into is a
 * fact about the mod, not a detail of one block entity.
 */
public class FCapabilities {

    private FCapabilities() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(FCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Every side, no filtering. A hammer has one anvil and it faces the room; which side a
        // hopper approaches from is not a fact the machine has any way to care about.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FBlockEntities.MECHANICAL_HAMMER.get(),
                (hammer, side) -> new HammerItemHandler(hammer));
    }
}
