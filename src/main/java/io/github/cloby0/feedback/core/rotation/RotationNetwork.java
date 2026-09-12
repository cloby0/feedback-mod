package io.github.cloby0.feedback.core.rotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * One connected run of rotating blocks, and the ledger of what it can carry against what
 * is hanging off it.
 * <p>
 * Derived from Create's {@code KineticNetwork} (MIT) -- see THIRD-PARTY-LICENSES.md.
 * <p>
 * The network holds no speed of its own. Speed belongs to individual nodes and is settled by
 * {@link RotationPropagator}; the network only answers "is this run overloaded", which is a
 * whole-network property and cannot be decided locally.
 */
public class RotationNetwork {

    public final long id;

    /** Nodes that drive the network, mapped to the Su each can supply. */
    private final Map<RotationNode, Float> sources = new HashMap<>();
    /** Every node on the network, mapped to the Su each demands. */
    private final Map<RotationNode, Float> members = new HashMap<>();

    private float capacitySu;
    private float loadSu;

    public RotationNetwork(long id) {
        this.id = id;
    }

    public void add(RotationNode node) {
        if (members.containsKey(node))
            return;
        if (node.isSource())
            sources.put(node, node.getCapacitySu());
        members.put(node, node.getLoadSu());
        recalculate();
    }

    public void remove(RotationNode node) {
        if (!members.containsKey(node))
            return;
        sources.remove(node);
        members.remove(node);
        node.onNetworkChanged(0, 0);
        if (members.isEmpty()) {
            RotationNetworks.of(node.getLevel()).discard(id);
            return;
        }
        recalculate();
    }

    public void updateCapacityOf(RotationNode node, float capacitySu) {
        if (!sources.containsKey(node))
            return;
        sources.put(node, capacitySu);
        recalculate();
    }

    public void updateLoadOf(RotationNode node, float loadSu) {
        if (!members.containsKey(node))
            return;
        members.put(node, loadSu);
        recalculate();
    }

    /**
     * Recompute the ledger and tell every member, but only when something actually moved.
     * Members are told unconditionally once a figure changes, because "am I overstressed" is
     * a network answer and a node has no way to work it out for itself.
     */
    public void recalculate() {
        float newCapacity = sum(sources);
        float newLoad = sum(members);
        if (newCapacity == capacitySu && newLoad == loadSu)
            return;
        capacitySu = newCapacity;
        loadSu = newLoad;
        for (RotationNode member : members.keySet())
            member.onNetworkChanged(capacitySu, loadSu);
    }

    /**
     * Sum a ledger, dropping entries whose block entity is no longer the one at its position.
     * Block entities are replaced on chunk reload, and a stale key keeps the old object alive
     * and its Su in the total forever.
     */
    private float sum(Map<RotationNode, Float> ledger) {
        float total = 0;
        for (Iterator<Map.Entry<RotationNode, Float>> it = ledger.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<RotationNode, Float> entry = it.next();
            RotationNode node = entry.getKey();
            if (node.getLevel() == null || node.getLevel().getBlockEntity(node.getBlockPos()) != node) {
                it.remove();
                continue;
            }
            total += entry.getValue();
        }
        return total;
    }

    public boolean isOverstressed() {
        return loadSu > capacitySu;
    }

    public float getCapacitySu() {
        return capacitySu;
    }

    public float getLoadSu() {
        return loadSu;
    }

    public int size() {
        return members.size();
    }
}
