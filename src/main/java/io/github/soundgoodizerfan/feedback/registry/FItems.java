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
import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.item.CalipersItem;
import io.github.soundgoodizerfan.feedback.item.DebugHelmetItem;
import io.github.soundgoodizerfan.feedback.item.HandHammerItem;
import io.github.soundgoodizerfan.feedback.item.ThermometerItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Feedback.MOD_ID);

    public static final DeferredItem<BlockItem> SHAFT = ITEMS.registerSimpleBlockItem(FBlocks.SHAFT);
    public static final DeferredItem<BlockItem> SMALL_COG = ITEMS.registerSimpleBlockItem(FBlocks.SMALL_COG);
    public static final DeferredItem<BlockItem> LARGE_COG = ITEMS.registerSimpleBlockItem(FBlocks.LARGE_COG);
    public static final DeferredItem<BlockItem> GEARBOX = ITEMS.registerSimpleBlockItem(FBlocks.GEARBOX);
    public static final DeferredItem<BlockItem> HAND_CRANK = ITEMS.registerSimpleBlockItem(FBlocks.HAND_CRANK);
    public static final DeferredItem<BlockItem> WATER_WHEEL = ITEMS.registerSimpleBlockItem(FBlocks.WATER_WHEEL);

    public static final DeferredItem<BlockItem> CRANK_LINKAGE = ITEMS.registerSimpleBlockItem(FBlocks.CRANK_LINKAGE);
    public static final DeferredItem<BlockItem> MECHANICAL_HAMMER = ITEMS.registerSimpleBlockItem(FBlocks.MECHANICAL_HAMMER);

    public static final DeferredItem<BlockItem> CLUTCH = ITEMS.registerSimpleBlockItem(FBlocks.CLUTCH);
    public static final DeferredItem<BlockItem> TIMER = ITEMS.registerSimpleBlockItem(FBlocks.TIMER);

    /**
     * The one route to a plate that needs no machine, and the reason there is one at all.
     *
     * <p>Philosophy 7 promises manual production stays theoretically possible for a surprising
     * share of the game. Until this existed the promise was empty in the first ten minutes: a plate
     * needed a Mechanical Hammer, and a Mechanical Hammer needs plates.
     */
    public static final DeferredItem<HandHammerItem> HAND_HAMMER =
            ITEMS.registerItem("hand_hammer", HandHammerItem::new,
                    new Item.Properties().stacksTo(1).durability(FTuning.HAND_HAMMER_DURABILITY));

    /**
     * The slice's first instrument. Registered here with the blocks rather than off in a tools
     * section, because §7 insists an instrument is a yield technology and not a tier -- it sits
     * alongside the machinery it improves, not above it.
     */
    public static final DeferredItem<Item> CALIPERS =
            ITEMS.registerItem("calipers", CalipersItem::new, new Item.Properties().stacksTo(1));

    // Beat 1's overrun chain. Every step is a real item, and only the last one is a mistake:
    // foil is a genuine sidegrade that beat 2's thermometer needs. See slice 1, "Overrun".
    public static final DeferredItem<Item> COPPER_PLATE = ITEMS.registerSimpleItem("copper_plate");
    public static final DeferredItem<Item> COPPER_FOIL = ITEMS.registerSimpleItem("copper_foil");
    public static final DeferredItem<Item> COPPER_SCRAP = ITEMS.registerSimpleItem("copper_scrap");

    // --- beat 2: heat -----------------------------------------------------------------------

    public static final DeferredItem<BlockItem> FIREBOX = ITEMS.registerSimpleBlockItem(FBlocks.FIREBOX);
    public static final DeferredItem<BlockItem> SMALL_CRUCIBLE = ITEMS.registerSimpleBlockItem(FBlocks.SMALL_CRUCIBLE);
    public static final DeferredItem<BlockItem> LARGE_CRUCIBLE = ITEMS.registerSimpleBlockItem(FBlocks.LARGE_CRUCIBLE);
    public static final DeferredItem<BlockItem> INSULATION = ITEMS.registerSimpleBlockItem(FBlocks.INSULATION);
    public static final DeferredItem<BlockItem> BELLOWS = ITEMS.registerSimpleBlockItem(FBlocks.BELLOWS);
    public static final DeferredItem<BlockItem> BIMETALLIC_STRIP = ITEMS.registerSimpleBlockItem(FBlocks.BIMETALLIC_STRIP);

    /**
     * The slice's second instrument, and the one that reveals rather than refines.
     * <p>
     * Copper foil is in it, which is why beat 1's first overrun had to be a sidegrade: the mistake
     * the hammer makes on the way past a plate is the material this is built out of.
     */
    public static final DeferredItem<Item> THERMOMETER =
            ITEMS.registerItem("thermometer", ThermometerItem::new, new Item.Properties().stacksTo(1));

    // Beat 2's materials. Burnt Iron is the overrun and it is a dead end on purpose -- the
    // thermal chain's first mistake is a loss, where the mechanical chain's first mistake was
    // foil. One of the two beats has to teach that overrun is sometimes simply bad.
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");
    public static final DeferredItem<Item> BURNT_IRON = ITEMS.registerSimpleItem("burnt_iron");
    public static final DeferredItem<Item> HARDENED_STEEL = ITEMS.registerSimpleItem("hardened_steel");
    public static final DeferredItem<Item> STEEL_PLATE = ITEMS.registerSimpleItem("steel_plate");

    /**
     * Perfect instrumentation, and deliberately absent from {@link FCreativeTabs#MAIN}.
     * <p>
     * It is a development cheat, not the top of the instrument ladder -- see
     * {@link DebugHelmetItem}. Anything reachable from the mod's own tab reads as content.
     */
    public static final DeferredItem<DebugHelmetItem> DEBUG_HELMET = ITEMS.register("debug_helmet",
            () -> new DebugHelmetItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> FURNACE = ITEMS.registerSimpleBlockItem(FBlocks.FURNACE);
    public static final DeferredItem<BlockItem> SMOKER = ITEMS.registerSimpleBlockItem(FBlocks.SMOKER);
    public static final DeferredItem<BlockItem> BLAST_FURNACE = ITEMS.registerSimpleBlockItem(FBlocks.BLAST_FURNACE);

    private FItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
