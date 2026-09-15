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
package io.github.soundgoodizerfan.feedback.control.program;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 2 boolean in, 1 boolean out. Spec §2.3: a boolean input has no sensible default, so unlike
 * {@link ComparatorNode} both inputs here are mandatory wires -- {@link ProgramNode#UNWIRED} on
 * a freshly placed card, and refused at print time until both are wired.
 */
public record AndNode(int id, int leftInput, int rightInput) implements ProgramNode {

    public static final String TYPE = "and";

    public static final MapCodec<AndNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(AndNode::id),
            Codec.INT.fieldOf("left_input").forGetter(AndNode::leftInput),
            Codec.INT.fieldOf("right_input").forGetter(AndNode::rightInput)
    ).apply(instance, AndNode::new));

    public static final StreamCodec<ByteBuf, AndNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AndNode::id,
            ByteBufCodecs.VAR_INT, AndNode::leftInput,
            ByteBufCodecs.VAR_INT, AndNode::rightInput,
            AndNode::new);

    @Override
    public String typeKey() {
        return TYPE;
    }
}
