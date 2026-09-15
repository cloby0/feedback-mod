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

import io.github.soundgoodizerfan.feedback.core.unit.Tu;
import io.github.soundgoodizerfan.feedback.core.unit.TuRate;
import io.github.soundgoodizerfan.feedback.core.unit.Units;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

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
 * <h2>Tempering: a hold that only counts on the way down</h2>
 * {@code requireCooling} is the one thing tempering needed that carburizing did not. Philosophy 13
 * says an actuator is a switch, never a dial, so there is no rate to store here -- the only lever
 * a Damper gives the player is on/off, timed by hand or by a controller, exactly like the Bellows/
 * Bimetallic-Strip thermostat. So the process asks for a direction instead of a rate: while this is
 * true, a tick where the body is flat or heating does not advance {@code holdTicks}, the same
 * "safe direction, nothing lost" treatment the out-of-band case above already gets -- it simply
 * does not count until the body is actually cooling in-band. The player still has to get it there
 * by reheating past the band first and then shutting the fire off or opening a Damper; nothing new
 * is needed for that half, it falls out of the existing fire/leak model.
 *
 * @param inputs              everything that must be present. Ingredients, so tags work and no
 *                            identity is ever named.
 * @param minTemperature      bottom of the band, in Tu. For a melt, this is the melt point and
 *                            the only number that matters -- see the class doc.
 * @param maxTemperature      top of the band, in Tu.
 * @param holdTicks           how long the contents must stay inside the band, cumulatively.
 * @param maxHeatingTuPerTick fastest the vessel may be climbing while the hold accumulates.
 * @param result              what the inputs become, if it is an item. Empty for a melt, where
 *                            {@link #resultFluid} is the real output instead.
 * @param spoilTemperature    above this the batch is ruined. Same as {@code maxTemperature} would
 *                            make the safe direction unsafe, so it always sits well above it.
 * @param spoiled             what is left when it is.
 * @param resultFluid         what the inputs become, if it is a fluid -- a melt. Empty for every
 *                            ordinary process. Never both this and {@link #result} at once.
 * @param requireCooling      tempering's flag -- see above. False for every ordinary process.
 */
public record ThermalProcess(List<Ingredient> inputs,
                             Tu minTemperature,
                             Tu maxTemperature,
                             int holdTicks,
                             TuRate maxHeatingTuPerTick,
                             ItemStack result,
                             Tu spoilTemperature,
                             ItemStack spoiled,
                             FluidStack resultFluid,
                             boolean requireCooling) {

    public static final Codec<ThermalProcess> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(ThermalProcess::inputs),
            Units.codec(Tu::new).fieldOf("min_temperature").forGetter(ThermalProcess::minTemperature),
            // Open by default -- a melt has a floor and nothing else, the same open-ended shape
            // the pooled vanilla-fallback cards already draw as "800+ Tu" (see
            // ThermalProcessCategory). An ordinary band-and-hold process still states both ends.
            Units.codec(Tu::new).optionalFieldOf("max_temperature", new Tu(Float.MAX_VALUE)).forGetter(ThermalProcess::maxTemperature),
            Codec.INT.fieldOf("hold_ticks").forGetter(ThermalProcess::holdTicks),
            Units.codec(TuRate::new).optionalFieldOf("max_heating", new TuRate(Float.MAX_VALUE)).forGetter(ThermalProcess::maxHeatingTuPerTick),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(ThermalProcess::result),
            Units.codec(Tu::new).optionalFieldOf("spoil_temperature", new Tu(Float.MAX_VALUE)).forGetter(ThermalProcess::spoilTemperature),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("spoiled", ItemStack.EMPTY).forGetter(ThermalProcess::spoiled),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("result_fluid", FluidStack.EMPTY).forGetter(ThermalProcess::resultFluid),
            Codec.BOOL.optionalFieldOf("require_cooling", false).forGetter(ThermalProcess::requireCooling)
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
                            new Tu(buffer.readFloat()),
                            new Tu(buffer.readFloat()),
                            buffer.readVarInt(),
                            new TuRate(buffer.readFloat()),
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                            new Tu(buffer.readFloat()),
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                            FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                            buffer.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ThermalProcess process) {
                    INPUTS.encode(buffer, process.inputs());
                    buffer.writeFloat(process.minTemperature().value());
                    buffer.writeFloat(process.maxTemperature().value());
                    buffer.writeVarInt(process.holdTicks());
                    buffer.writeFloat(process.maxHeatingTuPerTick().tuPerTick());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, process.result());
                    buffer.writeFloat(process.spoilTemperature().value());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, process.spoiled());
                    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, process.resultFluid());
                    buffer.writeBoolean(process.requireCooling());
                }
            };

    public boolean inBand(Tu tu) {
        return tu.value() >= minTemperature.value() && tu.value() <= maxTemperature.value();
    }

    public boolean spoilsAt(Tu tu) {
        return tu.value() > spoilTemperature.value();
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

    /** Whether this entry is a melt -- its output is a fluid rather than an item. */
    public boolean hasFluidResult() {
        return !resultFluid.isEmpty();
    }
}
