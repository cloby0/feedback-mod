package io.github.cloby0.feedback.registry;

import io.github.cloby0.feedback.Feedback;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Feedback.MOD_ID);

    public static final DeferredItem<BlockItem> SHAFT = ITEMS.registerSimpleBlockItem(FBlocks.SHAFT);
    public static final DeferredItem<BlockItem> HAND_CRANK = ITEMS.registerSimpleBlockItem(FBlocks.HAND_CRANK);
    public static final DeferredItem<BlockItem> WATER_WHEEL = ITEMS.registerSimpleBlockItem(FBlocks.WATER_WHEEL);

    public static final DeferredItem<BlockItem> CRANK_LINKAGE = ITEMS.registerSimpleBlockItem(FBlocks.CRANK_LINKAGE);
    public static final DeferredItem<BlockItem> MECHANICAL_HAMMER = ITEMS.registerSimpleBlockItem(FBlocks.MECHANICAL_HAMMER);

    public static final DeferredItem<BlockItem> CLUTCH = ITEMS.registerSimpleBlockItem(FBlocks.CLUTCH);
    public static final DeferredItem<BlockItem> TIMER = ITEMS.registerSimpleBlockItem(FBlocks.TIMER);

    // Beat 1's overrun chain. Every step is a real item, and only the last one is a mistake:
    // foil is a genuine sidegrade that beat 2's thermometer needs. See slice 1, "Overrun".
    public static final DeferredItem<Item> COPPER_PLATE = ITEMS.registerSimpleItem("copper_plate");
    public static final DeferredItem<Item> COPPER_FOIL = ITEMS.registerSimpleItem("copper_foil");
    public static final DeferredItem<Item> COPPER_SCRAP = ITEMS.registerSimpleItem("copper_scrap");

    private FItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
