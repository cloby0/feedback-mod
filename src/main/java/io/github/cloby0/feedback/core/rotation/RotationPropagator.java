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
        // The run may have been cut in two. Rebuild from each side independently; a neighbour
        // that is now in a separate component will simply flood fill a smaller set.
        for (Direction face : Direction.values()) {
            BlockPos neighbour = pos.relative(face);
            if (nodeAt(level, neighbour) != null)
                rebuildFrom(level, neighbour);
        }
    }

    /** Recompute speeds and network membership for the entire run containing {@code pos}. */
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

        Map<RotationNode, Float> speeds = new HashMap<>();
        if (strongest != null && !assignSpeeds(level, strongest, speeds)) {
            // Two sources are fighting over the same run. Something has to give, and it is the
            // block that was just placed to complete the conflict.
            level.destroyBlock(pos, true);
            return;
        }

        RotationNetwork network = RotationNetworks.of(level).create();
        for (RotationNode node : component) {
            float previous = node.getTheoreticalRpm();
            float assigned = speeds.getOrDefault(node, 0f);

            node.setNetwork(network);
            network.add(node);

            if (previous != assigned) {
                node.setRpm(assigned);
                node.onSpeedChanged(previous);
                node.sync();
            }
        }
        network.recalculate();
    }

    /**
     * Walk outward from a source, giving every reachable node the speed that source implies.
     *
     * @return false if two sources demand incompatible speeds of the same node.
     */
    private static boolean assignSpeeds(Level level, RotationNode source, Map<RotationNode, Float> speeds) {
        speeds.put(source, source.getGeneratedRpm());

        Deque<RotationNode> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            RotationNode current = queue.poll();
            float currentRpm = speeds.get(current);

            for (RotationNode neighbour : connectedNeighbours(level, current)) {
                float ratio = ratioBetween(level, current, neighbour);
                if (ratio == 0)
                    continue;

                float conveyed = currentRpm * ratio;

                Float existing = speeds.get(neighbour);
                if (existing != null) {
                    // Rejoining the walk at a node we have already set is fine, as long as both
                    // routes agree. Disagreement means the run is geared against itself.
                    if (Math.abs(existing - conveyed) > 1e-4f)
                        return false;
                    continue;
                }

                // A second, independently driven source in the same run only survives if it
                // happens to want exactly what it is being given.
                if (neighbour.isSource() && Math.abs(neighbour.getGeneratedRpm() - conveyed) > 1e-4f)
                    return false;

                speeds.put(neighbour, conveyed);
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
     * Only shaft-to-shaft coupling exists today, which is always 1:1. Gearing returns a real
     * ratio here -- negative to reverse direction -- and nothing else needs to change.
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
        boolean coupled = rotatableFrom.hasShaftTowards(level, from.getBlockPos(), stateFrom, face)
                && rotatableTo.hasShaftTowards(level, to.getBlockPos(), stateTo, face.getOpposite());

        return coupled ? 1 : 0;
    }

    private static RotationNode nodeAt(Level level, BlockPos pos) {
        if (!level.isLoaded(pos))
            return null;
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RotationNode node ? node : null;
    }
}
