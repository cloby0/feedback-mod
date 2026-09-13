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
package io.github.cloby0.feedback.core.rotation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Works out how fast everything on a rotation network turns.
 *
 * <h2>Why this is a rebuild and not an incremental update</h2>
 * Create propagates incrementally: each node remembers which neighbour drives it, a faster
 * source "overpowers" a slower one, and changes ripple outward from wherever they happened.
 * That is the right call for Create, whose networks are enormous and whose propagation cost
 * would otherwise show up in the frame time.
 * <p>
 * We rebuild the whole connected run instead. Flood fill it, find the strongest source, walk
 * outward from that source assigning speeds. It is O(n) in the size of one shaft run rather
 * than O(size of the change), which is worse on paper and irrelevant in practice at the scale
 * a Feedback factory actually reaches -- and it is enormously easier to be sure it is correct,
 * because there is no accumulated ownership state to get out of step with the world.
 * <p>
 * It also deletes a whole bug class. Create needs a "flicker score" that breaks blocks whose
 * speed changes too often, because a network wired into a loop can propagate forever. A rebuild
 * visits each node once and cannot loop at all.
 * <p>
 * Design derived from Create's {@code RotationPropagator} (MIT) -- see THIRD-PARTY-LICENSES.md.
 */
public class RotationPropagator {

    public static void onAdded(Level level, BlockPos pos, RotationNode node) {
        if (level.isClientSide)
            return;
        rebuildFrom(level, pos);
    }

    public static void onRemoved(Level level, BlockPos pos, RotationNode removed) {
        if (level.isClientSide)
            return;
        if (removed.getNetwork() != null)
            removed.getNetwork().remove(removed);
        // The run may have been cut in two. Rebuild from each side; a neighbour now in a
        // separate component simply flood fills a smaller set. Neighbours still joined to each
        // other share a component, so skip any that the previous rebuild already covered.
        Set<BlockPos> covered = new HashSet<>();
        for (Direction face : Direction.values()) {
            BlockPos neighbourPos = pos.relative(face);
            if (covered.contains(neighbourPos) || nodeAt(level, neighbourPos) == null)
                continue;
            rebuildFrom(level, neighbourPos);
            RotationNode neighbour = nodeAt(level, neighbourPos);
            if (neighbour != null)
                for (RotationNode member : floodFill(level, neighbour))
                    covered.add(member.getBlockPos());
        }
    }

    /** Recompute ratios, target speed and network membership for the whole run at {@code pos}. */
    public static void rebuildFrom(Level level, BlockPos pos) {
        RotationNode start = nodeAt(level, pos);
        if (start == null)
            return;

        List<RotationNode> component = floodFill(level, start);

        RotationNode strongest = null;
        for (RotationNode node : component)
            if (node.isSource() && (strongest == null
                    || Math.abs(node.getGeneratedRpm()) > Math.abs(strongest.getGeneratedRpm())))
                strongest = node;

        // Ratios are measured against a reference node. The driving source is the natural
        // choice; with nothing driving, any node will do, since the run still has to keep its
        // relative speeds straight while it coasts.
        RotationNode reference = strongest != null ? strongest : start;

        Map<RotationNode, Float> ratios = new HashMap<>();
        if (!assignRatios(level, reference, ratios)) {
            // Two sources are fighting over the same run. Something has to give, and it is the
            // block that was just placed to complete the conflict.
            level.destroyBlock(pos, true);
            return;
        }

        // Carry the reference node's actual speed across the rebuild, so adding a shaft to a
        // turning line does not stop it dead. Its ratio is 1 by construction, so its speed is
        // the network's speed.
        float inheritedRpm = reference.getRpm();
        // Read the phase off the old NETWORK, not off the node. A node's own visualAngle is only
        // written when it syncs, so between syncs it is stale, while the network's is live.
        float inheritedPhase = reference.getNetwork() == null ? 0f : reference.getNetwork().getPhase();

        // Retire whatever networks these nodes used to belong to. Without this the old network
        // object stays in the registry with its members still listed, and goes on ticking
        // forever alongside the new one.
        RotationNetworks registry = RotationNetworks.of(level);
        Set<Long> retired = new HashSet<>();
        for (RotationNode node : component) {
            RotationNetwork previous = node.getNetwork();
            if (previous != null && retired.add(previous.id))
                registry.discard(previous.id);
        }

        RotationNetwork network = registry.create();
        for (RotationNode node : component) {
            node.setRatio(ratios.getOrDefault(node, 0f));
            node.setNetwork(network);
            network.add(node);
        }
        network.inheritSpeed(inheritedRpm);
        network.inheritPhase(inheritedPhase);
        network.setTargetRpm(strongest != null ? strongest.getGeneratedRpm() : 0);
        network.recalculate();

        // Tell every client unconditionally. A rebuild often settles on exactly the speed the
        // run was already at, and the tick loop only syncs on a CHANGE -- so without this the
        // client never hears the speed at all, and a perfectly good shaft sits there reporting
        // its RPM while refusing to turn.
        for (RotationNode node : component)
            node.syncSpeedNow();
    }

    /**
     * Walk outward from the reference node, giving every reachable node its speed ratio.
     *
     * @return false if two sources demand incompatible speeds of the same node.
     */
    private static boolean assignRatios(Level level, RotationNode reference, Map<RotationNode, Float> ratios) {
        ratios.put(reference, 1f);
        float referenceRpm = reference.getGeneratedRpm();

        Deque<RotationNode> queue = new ArrayDeque<>();
        queue.add(reference);

        while (!queue.isEmpty()) {
            RotationNode current = queue.poll();
            float currentRatio = ratios.get(current);

            for (RotationNode neighbour : connectedNeighbours(level, current)) {
                float gearing = ratioBetween(level, current, neighbour);
                if (gearing == 0)
                    continue;

                float conveyed = currentRatio * gearing;

                Float existing = ratios.get(neighbour);
                if (existing != null) {
                    // Rejoining the walk at a node we have already set is fine, as long as both
                    // routes agree. Disagreement means the run is geared against itself.
                    if (Math.abs(existing - conveyed) > 1e-4f)
                        return false;
                    continue;
                }

                // A second source in the same run only survives if it happens to want exactly
                // the speed this one is already giving it. Checked in RPM rather than ratio,
                // because a ratio means nothing without a reference speed to apply it to.
                if (neighbour.isSource() && referenceRpm != 0
                        && Math.abs(neighbour.getGeneratedRpm() - conveyed * referenceRpm) > 1e-4f)
                    return false;

                ratios.put(neighbour, conveyed);
                queue.add(neighbour);
            }
        }
        return true;
    }

    /** Every rotation node reachable from {@code start} through shaft connections. */
    private static List<RotationNode> floodFill(Level level, RotationNode start) {
        List<RotationNode> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<RotationNode> queue = new ArrayDeque<>();

        queue.add(start);
        seen.add(start.getBlockPos());

        while (!queue.isEmpty()) {
            RotationNode current = queue.poll();
            found.add(current);
            for (RotationNode neighbour : connectedNeighbours(level, current))
                if (seen.add(neighbour.getBlockPos()))
                    queue.add(neighbour);
        }
        return found;
    }

    private static List<RotationNode> connectedNeighbours(Level level, RotationNode node) {
        List<RotationNode> neighbours = new ArrayList<>(6);
        for (Direction face : Direction.values()) {
            RotationNode other = nodeAt(level, node.getBlockPos().relative(face));
            if (other != null && ratioBetween(level, node, other) != 0)
                neighbours.add(other);
        }
        return neighbours;
    }

    /**
     * Speed multiplier from {@code from} to {@code to}, or 0 when they are not coupled.
     * <p>
     * Two ways to be coupled, and they are different physical things. Teeth <em>mesh</em>, which
     * reverses direction and changes speed by the ratio of the two gears. A shaft <em>runs
     * through</em>, which is always the same speed -- though not necessarily the same direction,
     * since a gearbox drives its faces off separate bevels.
     */
    private static float ratioBetween(Level level, RotationNode from, RotationNode to) {
        BlockState stateFrom = from.getBlockState();
        BlockState stateTo = to.getBlockState();
        if (!(stateFrom.getBlock() instanceof Rotatable rotatableFrom)
                || !(stateTo.getBlock() instanceof Rotatable rotatableTo))
            return 0;

        BlockPos diff = to.getBlockPos().subtract(from.getBlockPos());
        if (diff.distManhattan(BlockPos.ZERO) != 1)
            return 0;

        Direction face = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());

        float mesh = rotatableFrom.meshRatioTowards(stateFrom, face, stateTo);
        if (mesh != 0)
            return mesh;

        boolean coupled = rotatableFrom.hasShaftTowards(level, from.getBlockPos(), stateFrom, face)
                && rotatableTo.hasShaftTowards(level, to.getBlockPos(), stateTo, face.getOpposite());
        if (!coupled)
            return 0;

        return rotatableFrom.shaftSignTowards(stateFrom, face)
                * rotatableTo.shaftSignTowards(stateTo, face.getOpposite());
    }

    private static RotationNode nodeAt(Level level, BlockPos pos) {
        if (!level.isLoaded(pos))
            return null;
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RotationNode node ? node : null;
    }
}
