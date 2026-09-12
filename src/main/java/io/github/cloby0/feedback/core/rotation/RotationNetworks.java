package io.github.cloby0.feedback.core.rotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Per-level registry of live rotation networks.
 * <p>
 * Networks are runtime-only and nothing here is saved. On load every node re-announces itself
 * and the networks rebuild from the blocks, which are the real source of truth. Momentum is the
 * one thing lost across a reload, which is a reasonable thing to lose when the chunk was not
 * ticking anyway.
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

    public void discard(long id) {
        networks.remove(id);
    }

    /** Advance every network one tick. Copied first, because a network can discard itself. */
    public void tickAll() {
        if (networks.isEmpty())
            return;
        List<RotationNetwork> snapshot = new ArrayList<>(networks.values());
        for (RotationNetwork network : snapshot)
            network.tick();
    }
}
