package io.github.cloby0.feedback.registry;

import com.mojang.serialization.Codec;

import io.github.cloby0.feedback.Feedback;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Feedback.MOD_ID);

    /**
     * Cumulative mechanical work beaten into this stack, in Fu.
     * <p>
     * Lives on the item rather than in the machine, so a half-worked ingot stays half-worked when
     * it is carried out, dropped, or put in a chest. Philosophy 9: material carries its own
     * history between machines, and the gap between two machines is part of the process.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WORK =
            COMPONENTS.register("work", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    /**
     * Fu this workpiece needs before it becomes the next thing.
     * <p>
     * Stored on the stack next to {@link #WORK} so the two travel together. That is what lets a
     * tooltip say "nearly there" without the client knowing the deformation table, and it is the
     * same pair the calipers will eventually report as {@code 16 / 20 Fu}.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WORK_REQUIRED =
            COMPONENTS.register("work_required", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private FDataComponents() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
