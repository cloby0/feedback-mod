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
package io.github.cloby0.feedback.net;

import java.util.List;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.process.ClientDeformations;
import io.github.cloby0.feedback.process.Deformation;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Ships the deformation table to the client so a recipe browser can print it.
 *
 * <h2>Why a packet rather than JEI's own hooks</h2>
 * Both were available and the packet wins on honesty. The table is datapack data, so the client
 * genuinely does not have it -- that is a missing-data problem, not a JEI problem, and solving it
 * in JEI would mean any other display we ever write has to solve it again. Sending it once on
 * datapack sync leaves a plain list any consumer can read, and JEI becomes an optional reader of
 * it rather than the owner of it.
 * <p>
 * It also keeps the deletion story simple: if JEI is not installed, this packet still arrives and
 * costs three entries of memory.
 */
public record DeformationSyncPayload(List<Deformation> entries) implements CustomPacketPayload {

    public static final Type<DeformationSyncPayload> TYPE = new Type<>(Feedback.id("deformation_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeformationSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Deformation.STREAM_CODEC.apply(ByteBufCodecs.list()), DeformationSyncPayload::entries,
                    DeformationSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeformationSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientDeformations.set(payload.entries()));
    }
}
