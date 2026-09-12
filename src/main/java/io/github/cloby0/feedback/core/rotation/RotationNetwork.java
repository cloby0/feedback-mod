package io.github.cloby0.feedback.core.rotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.github.cloby0.feedback.core.FTuning;

/**
 * One connected run of rotating blocks: its Su ledger, its momentum, and its speed.
 *
 * <h2>Speed lives here, not on the blocks</h2>
 * A rigid shaft run turns as one object, so there is exactly one speed for the whole network and
 * each node simply scales it by its own gear ratio. That is not only simpler than a speed per
 * block -- it is what makes inertia expressible at all, because momentum is a property of the
 * whole spinning mass and cannot be stored in any one block.
 *
 * <h2>Inertia</h2>
 * The network has a <em>target</em> speed, which is whatever the sources are asking for, and a
 * <em>current</em> speed, which chases it. How fast it chases depends on total inertia -- the
 * mass bolted to the shaft.
 * <ul>
 *   <li><b>Spinning up</b> is paid for out of surplus Su. Plenty of spare power, or very little
 *       mass, and the network comes up to speed quickly.</li>
 *   <li><b>Slowing down</b> is driven by load. A network with machines hanging off it stops
 *       promptly; an unloaded flywheel coasts for a long time.</li>
 * </ul>
 * Two things fall out of that without being designed. Cutting power makes a network wind down
 * rather than stop, which is the coasting behaviour philosophy 13 asks of a mechanical cutoff.
 * And a heavy network is slow to change but hard to disturb, while a light one is responsive and
 * twitchy -- the same trade the philosophy already describes for thermal mass, arriving in a
 * different energy type without being installed there.
 *
 * <h2>Overstress</h2>
 * An overloaded network does not snap to zero. It simply stops being driven and winds down under
 * its own load, which is both more honest and more readable than a hard stop.
 * <p>
 * Derived in design from Create's {@code KineticNetwork} (MIT) -- see THIRD-PARTY-LICENSES.md.
 * Create has no inertia; its networks change speed instantly.
 */
public class RotationNetwork {

    public final long id;

    /** Nodes that drive the network, mapped to the Su each can supply. */
    private final Map<RotationNode, Float> sources = new HashMap<>();
    /** Every node on the network, mapped to the Su each demands. */
    private final Map<RotationNode, Float> members = new HashMap<>();

    private float capacitySu;
    private float loadSu;
    private float inertia = FTuning.MINIMUM_INERTIA;

    /** Speed the sources are asking for. Set by {@link RotationPropagator}. */
    private float targetRpm;
    /** Speed the network is actually turning at. */
    private float currentRpm;

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

    public void updateLoadOf(RotationNode node, float loadSu) {
        if (!members.containsKey(node))
            return;
        members.put(node, loadSu);
        recalculate();
    }

    public void setTargetRpm(float targetRpm) {
        this.targetRpm = targetRpm;
    }

    public float getTargetRpm() {
        return targetRpm;
    }

    public float getCurrentRpm() {
        return currentRpm;
    }

    /** Used when rebuilding a network, so a spinning run does not lose its momentum. */
    public void inheritSpeed(float rpm) {
        this.currentRpm = rpm;
    }

    /**
     * Advance the network one tick: move current speed toward target.
     * <p>
     * Called once per level tick by {@link RotationTicker}, not by the blocks -- momentum belongs
     * to the network, and letting each block nudge it would advance it once per member.
     */
    public void tick() {
        float effectiveTarget = isOverstressed() ? 0 : targetRpm;
        if (currentRpm == effectiveTarget)
            return;

        boolean speedingUp = Math.abs(effectiveTarget) > Math.abs(currentRpm)
                && Math.signum(effectiveTarget) == Math.signum(currentRpm);
        boolean reversing = effectiveTarget != 0 && currentRpm != 0
                && Math.signum(effectiveTarget) != Math.signum(currentRpm);

        // Accelerating is paid for out of spare power; slowing is driven by whatever is dragging.
        // A reversal has to brake to a stop first, so it is charged like slowing down.
        float force = (speedingUp && !reversing)
                ? Math.max(0, capacitySu - loadSu)
                : Math.max(loadSu, FTuning.SHAFT_LOSS_SU);

        float step = force / Math.max(inertia, FTuning.MINIMUM_INERTIA) * FTuning.INERTIA_RESPONSE;

        if (Math.abs(effectiveTarget - currentRpm) <= step)
            currentRpm = effectiveTarget;
        else
            currentRpm += Math.signum(effectiveTarget - currentRpm) * step;

        if (effectiveTarget == 0 && Math.abs(currentRpm) < FTuning.STOPPED_RPM_THRESHOLD)
            currentRpm = 0;

        for (RotationNode member : members.keySet())
            member.onNetworkSpeedChanged();
    }

    /**
     * Recompute the ledger and tell every member, but only when something actually moved.
     * Members are told whenever a figure changes, because "am I overstressed" is a network
     * answer and a node has no way to work it out for itself.
     */
    public void recalculate() {
        float newCapacity = 0;
        float newLoad = 0;
        float newInertia = 0;

        dropStale(sources);
        for (Map.Entry<RotationNode, Float> entry : sources.entrySet())
            newCapacity += entry.getValue();

        dropStale(members);
        for (Map.Entry<RotationNode, Float> entry : members.entrySet()) {
            newLoad += entry.getValue();
            newInertia += entry.getKey().getInertia();
        }

        inertia = newInertia;

        if (newCapacity == capacitySu && newLoad == loadSu)
            return;
        capacitySu = newCapacity;
        loadSu = newLoad;
        for (RotationNode member : members.keySet())
            member.onNetworkChanged(capacitySu, loadSu);
    }

    /**
     * Drop entries whose block entity is no longer the one at its position. A chunk reload
     * replaces block entities, and a stale key would keep the old object alive with its Su
     * counted forever.
     */
    private void dropStale(Map<RotationNode, Float> ledger) {
        for (Iterator<RotationNode> it = ledger.keySet().iterator(); it.hasNext(); ) {
            RotationNode node = it.next();
            if (node.getLevel() == null || node.getLevel().getBlockEntity(node.getBlockPos()) != node)
                it.remove();
        }
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

    public float getInertia() {
        return inertia;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public int size() {
        return members.size();
    }
}
