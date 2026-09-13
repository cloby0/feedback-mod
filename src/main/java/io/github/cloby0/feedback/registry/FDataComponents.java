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

    /**
     * The temperature this stack was last set to, in Tu -- not its temperature now.
     *
     * <h3>Why a stamp and not a reading</h3>
     * Paired with {@link #HEATED_AT}, and the pair is the whole of how a workpiece cools in a
     * chest nobody is ticking. See {@link io.github.cloby0.feedback.core.thermal.ItemHeat}: the
     * current figure is computed from these two whenever it is asked for, so no code anywhere has
     * to remember to cool anything. Read this field directly and you will get a number that was
     * true some minutes ago.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> TEMPERATURE =
            COMPONENTS.register("temperature", () -> DataComponentType.<Float>builder()
                    .persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT)
                    .build());

    /** The game tick {@link #TEMPERATURE} was stamped on. Meaningless without it. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> HEATED_AT =
            COMPONENTS.register("heated_at", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

    private FDataComponents() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
