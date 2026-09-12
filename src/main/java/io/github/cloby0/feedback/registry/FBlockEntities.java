package io.github.cloby0.feedback.registry;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.control.timer.TimerBlockEntity;
import io.github.cloby0.feedback.machine.clutch.ClutchBlockEntity;
import io.github.cloby0.feedback.machine.crank.HandCrankBlockEntity;
import io.github.cloby0.feedback.machine.hammer.MechanicalHammerBlockEntity;
import io.github.cloby0.feedback.machine.linkage.CrankLinkageBlockEntity;
import io.github.cloby0.feedback.machine.shaft.ShaftBlockEntity;
import io.github.cloby0.feedback.machine.waterwheel.WaterWheelBlockEntity;

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

    private FBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
