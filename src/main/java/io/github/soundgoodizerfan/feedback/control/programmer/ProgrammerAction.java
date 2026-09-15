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
package io.github.soundgoodizerfan.feedback.control.programmer;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One edit a player makes in a {@link ProgrammerScreen}, carried to the server over
 * {@link ProgrammerActionPayload}. Never persisted -- this is a command, not state, the same way
 * {@code DataLinkManager}'s pending selection is a command's worth of memory and not world data.
 */
public sealed interface ProgrammerAction {

    record AddNode(String typeKey) implements ProgrammerAction {
    }

    record RemoveNode(int nodeId) implements ProgrammerAction {
    }

    /** {@code slot} 0 is a node's only/left input, 1 its right input (comparator/AND). */
    record SetInput(int nodeId, int slot, int fromId) implements ProgrammerAction {
    }

    record SetLiteral(int nodeId, float value) implements ProgrammerAction {
    }

    /** Toggles a {@code ComparatorNode} between Greater Than and Less Than. */
    record ToggleOp(int nodeId) implements ProgrammerAction {
    }

    /** Consumes this player's pending connector selection for the named card. */
    record Link(int nodeId) implements ProgrammerAction {
    }

    /** Drags a card to a new canvas position. Cosmetic only -- never part of {@code ProgramGraph}. */
    record MoveNode(int nodeId, int x, int y) implements ProgrammerAction {
    }

    record Print() implements ProgrammerAction {
    }

    StreamCodec<ByteBuf, ProgrammerAction> STREAM_CODEC = StreamCodec.of(
            (buf, action) -> {
                switch (action) {
                    case AddNode a -> {
                        buf.writeByte(0);
                        ByteBufCodecs.STRING_UTF8.encode(buf, a.typeKey());
                    }
                    case RemoveNode a -> {
                        buf.writeByte(1);
                        ByteBufCodecs.VAR_INT.encode(buf, a.nodeId());
                    }
                    case SetInput a -> {
                        buf.writeByte(2);
                        ByteBufCodecs.VAR_INT.encode(buf, a.nodeId());
                        ByteBufCodecs.VAR_INT.encode(buf, a.slot());
                        ByteBufCodecs.VAR_INT.encode(buf, a.fromId());
                    }
                    case SetLiteral a -> {
                        buf.writeByte(3);
                        ByteBufCodecs.VAR_INT.encode(buf, a.nodeId());
                        ByteBufCodecs.FLOAT.encode(buf, a.value());
                    }
                    case ToggleOp a -> {
                        buf.writeByte(4);
                        ByteBufCodecs.VAR_INT.encode(buf, a.nodeId());
                    }
                    case Link a -> {
                        buf.writeByte(5);
                        ByteBufCodecs.VAR_INT.encode(buf, a.nodeId());
                    }
                    case MoveNode a -> {
                        buf.writeByte(6);
                        ByteBufCodecs.VAR_INT.encode(buf, a.nodeId());
                        ByteBufCodecs.VAR_INT.encode(buf, a.x());
                        ByteBufCodecs.VAR_INT.encode(buf, a.y());
                    }
                    case Print ignored -> buf.writeByte(7);
                }
            },
            buf -> {
                int kind = buf.readByte();
                return switch (kind) {
                    case 0 -> new AddNode(ByteBufCodecs.STRING_UTF8.decode(buf));
                    case 1 -> new RemoveNode(ByteBufCodecs.VAR_INT.decode(buf));
                    case 2 -> new SetInput(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf));
                    case 3 -> new SetLiteral(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.FLOAT.decode(buf));
                    case 4 -> new ToggleOp(ByteBufCodecs.VAR_INT.decode(buf));
                    case 5 -> new Link(ByteBufCodecs.VAR_INT.decode(buf));
                    case 6 -> new MoveNode(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf));
                    case 7 -> new Print();
                    default -> throw new IllegalArgumentException("Unknown programmer action kind: " + kind);
                };
            });
}
