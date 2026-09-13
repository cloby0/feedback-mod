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
import io.github.soundgoodizerfan.feedback.control.bimetallic.BimetallicStripBlockEntity;
import io.github.soundgoodizerfan.feedback.control.timer.TimerBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.bellows.BellowsBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.clutch.ClutchBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.crucible.CrucibleBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.firebox.FireboxBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.cog.CogBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.crank.HandCrankBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.gearbox.GearboxBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.hammer.MechanicalHammerBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.linkage.CrankLinkageBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.shaft.ShaftBlockEntity;
import io.github.soundgoodizerfan.feedback.machine.waterwheel.WaterWheelBlockEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Feedback.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaftBlockEntity>> SHAFT =
            BLOCK_ENTITIES.register("shaft", () -> BlockEntityType.Builder
                    .of(ShaftBlockEntity::new, FBlocks.SHAFT.get())
                    .build(null));

    /** One type for both cog sizes: the difference is entirely in the block, not in the state. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CogBlockEntity>> COG =
            BLOCK_ENTITIES.register("cog", () -> BlockEntityType.Builder
                    .of(CogBlockEntity::new, FBlocks.SMALL_COG.get(), FBlocks.LARGE_COG.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GearboxBlockEntity>> GEARBOX =
            BLOCK_ENTITIES.register("gearbox", () -> BlockEntityType.Builder
                    .of(GearboxBlockEntity::new, FBlocks.GEARBOX.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HandCrankBlockEntity>> HAND_CRANK =
            BLOCK_ENTITIES.register("hand_crank", () -> BlockEntityType.Builder
                    .of(HandCrankBlockEntity::new, FBlocks.HAND_CRANK.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>> WATER_WHEEL =
            BLOCK_ENTITIES.register("water_wheel", () -> BlockEntityType.Builder
                    .of(WaterWheelBlockEntity::new, FBlocks.WATER_WHEEL.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrankLinkageBlockEntity>> CRANK_LINKAGE =
            BLOCK_ENTITIES.register("crank_linkage", () -> BlockEntityType.Builder
                    .of(CrankLinkageBlockEntity::new, FBlocks.CRANK_LINKAGE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalHammerBlockEntity>> MECHANICAL_HAMMER =
            BLOCK_ENTITIES.register("mechanical_hammer", () -> BlockEntityType.Builder
                    .of(MechanicalHammerBlockEntity::new, FBlocks.MECHANICAL_HAMMER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClutchBlockEntity>> CLUTCH =
            BLOCK_ENTITIES.register("clutch", () -> BlockEntityType.Builder
                    .of(ClutchBlockEntity::new, FBlocks.CLUTCH.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimerBlockEntity>> TIMER =
            BLOCK_ENTITIES.register("timer", () -> BlockEntityType.Builder
                    .of(TimerBlockEntity::new, FBlocks.TIMER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireboxBlockEntity>> FIREBOX =
            BLOCK_ENTITIES.register("firebox", () -> BlockEntityType.Builder
                    .of(FireboxBlockEntity::new, FBlocks.FIREBOX.get())
                    .build(null));

    /** One type for both crucible sizes, for the same reason the cogs share one: size is the block. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder
                    .of(CrucibleBlockEntity::new, FBlocks.SMALL_CRUCIBLE.get(), FBlocks.LARGE_CRUCIBLE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BellowsBlockEntity>> BELLOWS =
            BLOCK_ENTITIES.register("bellows", () -> BlockEntityType.Builder
                    .of(BellowsBlockEntity::new, FBlocks.BELLOWS.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BimetallicStripBlockEntity>> BIMETALLIC_STRIP =
            BLOCK_ENTITIES.register("bimetallic_strip", () -> BlockEntityType.Builder
                    .of(BimetallicStripBlockEntity::new, FBlocks.BIMETALLIC_STRIP.get())
                    .build(null));

    private FBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
