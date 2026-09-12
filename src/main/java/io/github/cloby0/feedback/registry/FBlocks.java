package io.github.cloby0.feedback.registry;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.machine.crank.HandCrankBlock;
import io.github.cloby0.feedback.machine.hammer.MechanicalHammerBlock;
import io.github.cloby0.feedback.machine.linkage.CrankLinkageBlock;
import io.github.cloby0.feedback.machine.shaft.ShaftBlock;
import io.github.cloby0.feedback.machine.waterwheel.WaterWheelBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Feedback.MOD_ID);

    public static final DeferredBlock<ShaftBlock> SHAFT = BLOCKS.register("shaft",
            () -> new ShaftBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<HandCrankBlock> HAND_CRANK = BLOCKS.register("hand_crank",
            () -> new HandCrankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<WaterWheelBlock> WATER_WHEEL = BLOCKS.register("water_wheel",
            () -> new WaterWheelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<CrankLinkageBlock> CRANK_LINKAGE = BLOCKS.register("crank_linkage",
            () -> new CrankLinkageBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    public static final DeferredBlock<MechanicalHammerBlock> MECHANICAL_HAMMER = BLOCKS.register("mechanical_hammer",
            () -> new MechanicalHammerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    private FBlocks() {
    }

    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
