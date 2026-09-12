package io.github.cloby0.feedback.core.rotation;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Per-level registry of live rotation networks.
 * <p>
 * Networks are runtime-only. Nothing here is saved: on load every node re-announces itself
 * and the networks rebuild from the blocks, which are the actual source of truth.
 */
public class RotationNetworks {

    private static final Map<LevelAccessor, RotationNetworks> PER_LEVEL = new WeakHashMap<>();

    private final Map<Long, RotationNetwork> networks = new HashMap<>();
    private long nextId;

    public static RotationNetworks of(Level level) {
        return PER_LEVEL.computeIfAbsent(level, l -> new RotationNetworks());
    }

    public RotationNetwork create() {
        RotationNetwork network = new RotationNetwork(nextId++);
        networks.put(network.id, network);
        return network;
    }

    public RotationNetwork get(long id) {
        return networks.get(id);
    }

    public void discard(long id) {
        networks.remove(id);
    }
}
