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
package io.github.soundgoodizerfan.feedback.process;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * What a set of materials becomes when held in a temperature band for long enough.
 *
 * <h2>The same claim {@link Deformation} makes</h2>
 * This is a property of the materials, not a recipe belonging to a machine. The crucible never
 * consults a list of things it can make; it holds items at whatever temperature its fire and its
 * mass produce, and what happens to them is their own business. A crucible asked what it makes
 * has no answer, which is correct.
 *
 * <h2>Four numbers, four different kinds of failure</h2>
 * Philosophy 6 wants overrun to have a character rather than to be a timer running out, and the
 * shape of that is in these fields:
 * <ul>
 *   <li><b>Below {@code minTemperature}</b> -- nothing happens, and nothing is lost. The player
 *       may fail safely in this direction all day.</li>
 *   <li><b>Above {@code maxTemperature}</b> -- also nothing, but now they are climbing towards
 *       the expensive direction and have no way of knowing how close they are.</li>
 *   <li><b>Above {@code spoilTemperature}</b> -- the inputs are destroyed, immediately, and turn
 *       into {@code spoiled}. This is the overrun, and it is the same idea as a plate being
 *       hammered into foil: the machine did not stop, so it kept doing what it does.</li>
 *   <li><b>Rising faster than {@code maxHeatingTuPerTick}</b> -- the process will not take, and
 *       whatever hold has accumulated is lost. Nothing is destroyed; the player simply cannot
 *       work out why it is not working, which is the one failure that instruments genuinely fix.
 *       It is also what makes a large vessel <em>necessary</em> rather than merely nicer: a small
 *       crucible on a full fire climbs at 29 Tu/t and can never satisfy a 25 Tu/t limit.</li>
 * </ul>
 *
 * @param inputs              everything that must be present. Ingredients, so tags work and no
 *                            identity is ever named.
 * @param minTemperature      bottom of the band, in Tu.
 * @param maxTemperature      top of the band, in Tu.
 * @param holdTicks           how long the contents must stay inside the band, cumulatively.
 * @param maxHeatingTuPerTick fastest the vessel may be climbing while the hold accumulates.
 * @param result              what the inputs become.
 * @param spoilTemperature    above this the batch is ruined. Same as {@code maxTemperature} would
 *                            make the safe direction unsafe, so it always sits well above it.
 * @param spoiled             what is left when it is.
 */
public record ThermalProcess(List<Ingredient> inputs,
                             float minTemperature,
                             float maxTemperature,
                             int holdTicks,
                             float maxHeatingTuPerTick,
                             ItemStack result,
                             float spoilTemperature,
                             ItemStack spoiled) {

    public static final Codec<ThermalProcess> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(ThermalProcess::inputs),
            Codec.FLOAT.fieldOf("min_temperature").forGetter(ThermalProcess::minTemperature),
            Codec.FLOAT.fieldOf("max_temperature").forGetter(ThermalProcess::maxTemperature),
            Codec.INT.fieldOf("hold_ticks").forGetter(ThermalProcess::holdTicks),
            Codec.FLOAT.optionalFieldOf("max_heating", Float.MAX_VALUE).forGetter(ThermalProcess::maxHeatingTuPerTick),
            ItemStack.CODEC.fieldOf("result").forGetter(ThermalProcess::result),
            Codec.FLOAT.optionalFieldOf("spoil_temperature", Float.MAX_VALUE).forGetter(ThermalProcess::spoilTemperature),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("spoiled", ItemStack.EMPTY).forGetter(ThermalProcess::spoiled)
    ).apply(instance, ThermalProcess::new));

    /**
     * For printing on the client. Philosophy 8: a requirement is published data and costs nothing.
     * <p>
     * Written out rather than composed, because {@code StreamCodec.composite} stops at six fields
     * and this record has eight. Splitting the record to fit a helper would be letting the wire
     * format dictate the design.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ThermalProcess> STREAM_CODEC =
            new StreamCodec<>() {

                private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> INPUTS =
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());

                @Override
                public ThermalProcess decode(RegistryFriendlyByteBuf buffer) {
                    return new ThermalProcess(
                            INPUTS.decode(buffer),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readVarInt(),
                            buffer.readFloat(),
                            ItemStack.STREAM_CODEC.decode(buffer),
                            buffer.readFloat(),
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ThermalProcess process) {
                    INPUTS.encode(buffer, process.inputs());
                    buffer.writeFloat(process.minTemperature());
                    buffer.writeFloat(process.maxTemperature());
                    buffer.writeVarInt(process.holdTicks());
                    buffer.writeFloat(process.maxHeatingTuPerTick());
                    ItemStack.STREAM_CODEC.encode(buffer, process.result());
                    buffer.writeFloat(process.spoilTemperature());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, process.spoiled());
                }
            };

    public boolean inBand(float tu) {
        return tu >= minTemperature && tu <= maxTemperature;
    }

    public boolean spoilsAt(float tu) {
        return tu > spoilTemperature;
    }

    /**
     * {@code holdTicks} for a process the JEI plugin synthesises from a pooled vanilla/modded
     * cooking recipe rather than loading from a file -- see {@code FeedbackJeiPlugin.pooled}.
     * Metal counts an integrated Work total there and food counts plain ticks, neither of which
     * is "stay in this band for N ticks", so there is no honest number to put here and the card
     * knows to leave the row off instead of printing one that was never true.
     */
    public static final int NO_HOLD = -1;

    public boolean hasHold() {
        return holdTicks >= 0;
    }
}
