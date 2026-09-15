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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A whole printed program -- what a Punch Card actually carries. Persisted and network-synced as
 * one {@code FDataComponents.PROGRAM_GRAPH} component; the working copy edited in a Programmer is
 * the same type, just not yet written back to an item.
 */
public record ProgramGraph(List<ProgramNode> nodes) {

    public static final ProgramGraph EMPTY = new ProgramGraph(List.of());

    public static final Codec<ProgramGraph> CODEC =
            ProgramNode.CODEC.listOf().xmap(ProgramGraph::new, ProgramGraph::nodes);

    public static final StreamCodec<ByteBuf, ProgramGraph> STREAM_CODEC =
            ProgramNode.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ProgramGraph::new, ProgramGraph::nodes);

    public Optional<ProgramNode> find(int id) {
        return nodes.stream().filter(n -> n.id() == id).findFirst();
    }

    /**
     * Why a card would be rejected at print time (spec §2.4's "rejected at print time, not
     * runtime"), or empty if the graph is fine to burn onto a card. The translated message lives
     * under {@code feedback.programmer.error.<key>}.
     */
    public Optional<String> findError(int maxNodes) {
        if (nodes.size() > maxNodes)
            return Optional.of("too_many_nodes");

        Set<Integer> ids = new HashSet<>();
        for (ProgramNode node : nodes)
            if (!ids.add(node.id()))
                return Optional.of("duplicate_id");

        for (ProgramNode node : nodes)
            if (node instanceof SensorNode n && n.target() == null
                    || node instanceof ActuatorNode a && a.target() == null)
                return Optional.of("unlinked_card");

        for (ProgramNode node : nodes)
            for (int input : node.inputs())
                if (!ids.contains(input))
                    return Optional.of("dangling_wire");

        if (ProgramEvaluator.topologicalOrder(this).isEmpty())
            return Optional.of("cycle");

        return Optional.empty();
    }
}
