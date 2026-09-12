package io.github.cloby0.feedback.core.rotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.github.cloby0.feedback.core.FTuning;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

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

    /** Ticks between complaints. An overloaded network is more insistent than a strained one. */
    private static final int OVERLOADED_INTERVAL = 40;
    private static final int STRAINING_INTERVAL = 80;

    public final long id;

    /** Nodes that drive the network, mapped to the Su each can supply. */
    private final Map<RotationNode, Float> sources = new HashMap<>();
    /** Every node on the network, mapped to the Su each demands. */
    private final Map<RotationNode, Float> members = new HashMap<>();

    private float capacitySu;
    /** Demand that does not depend on speed: machines doing work. */
    private float staticLoadSu;
    /** Demand per RPM: friction. Costs nothing while stopped. */
    private float dragSuPerRpm;
    private float inertia = FTuning.MINIMUM_INERTIA;

    /** Speed the sources are asking for. Set by {@link RotationPropagator}. */
    private float targetRpm;
    /** Speed the network is actually turning at. */
    private float currentRpm;

    /** Counts down to the next complaint, so a labouring network does not smoke every tick. */
    private int ticksUntilComplaint;

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

    /**
     * Su actually being demanded right now: working draw plus friction at the current speed.
     * <p>
     * Held as two figures rather than recomputed from the members each tick, so a long run costs
     * no more to simulate than a short one.
     */
    public float getLoadSu() {
        return staticLoadSu + dragSuPerRpm * Math.abs(currentRpm);
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
        // Before the early-out below: a network that is stuck at zero because it is overloaded is
        // exactly the case worth complaining about, and it never changes speed.
        if (!members.isEmpty())
            complainIfLabouring(members.keySet().iterator().next().getLevel());

        float effectiveTarget = isOverstressed() ? 0 : targetRpm;
        float liveLoad = getLoadSu();

        // Friction rises with speed, so a run can be unable to reach the speed its source is
        // asking for. It is not blocked from trying -- it simply runs out of surplus torque
        // first and settles there. That settling point is the whole of "shaft loss".
        if (effectiveTarget != 0 && dragSuPerRpm > 0) {
            float terminal = Math.max(0, capacitySu - staticLoadSu) / dragSuPerRpm;
            if (Math.abs(effectiveTarget) > terminal)
                effectiveTarget = Math.signum(effectiveTarget) * terminal;
        }

        if (currentRpm == effectiveTarget)
            return;

        boolean speedingUp = Math.abs(effectiveTarget) > Math.abs(currentRpm)
                && Math.signum(effectiveTarget) == Math.signum(currentRpm);
        boolean reversing = effectiveTarget != 0 && currentRpm != 0
                && Math.signum(effectiveTarget) != Math.signum(currentRpm);

        // Accelerating is paid for out of spare power; slowing is driven by whatever is dragging.
        // A reversal has to brake to a stop first, so it is charged like slowing down.
        // Accelerating force ignores friction and the terminal clamp above handles it instead.
        // Using live surplus here is more literal but behaves badly: surplus goes to zero exactly
        // at terminal speed, so a run approaches its own top speed asymptotically and spends
        // fifteen seconds creeping the last revolution. Constant drive up to a hard ceiling is
        // both more readable and far easier to reason about.
        float force = (speedingUp && !reversing)
                ? Math.max(0, capacitySu - staticLoadSu)
                : Math.max(liveLoad, FTuning.MINIMUM_BRAKING_SU);

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
     * Let a labouring network be seen and heard.
     *
     * <h2>Why this is not optional polish</h2>
     * An overloaded network simply fails to turn, and a network near its limit takes a minute and
     * a half to reach speed instead of two seconds. Both were completely silent -- the factory
     * just did not work, with nothing to look at and nowhere to start. That is the same failure
     * as a linkage facing the wrong way: no error, it just quietly does nothing.
     * <p>
     * Philosophy 8 permits this for free because it is an <em>adjective</em>. Smoke and a creak
     * say "this one, and it is struggling" without handing over the Su figures an instrument is
     * supposed to sell. A player learns where to look; they still cannot read the ledger.
     */
    private void complainIfLabouring(Level level) {
        if (!(level instanceof ServerLevel server) || members.isEmpty())
            return;

        boolean overloaded = isOverstressed();
        boolean straining = !overloaded && capacitySu > 0
                && getLoadSu() > capacitySu * FTuning.STRAINING_LOAD_FRACTION;
        if (!overloaded && !straining) {
            ticksUntilComplaint = 0;
            return;
        }

        if (--ticksUntilComplaint > 0)
            return;
        ticksUntilComplaint = overloaded ? OVERLOADED_INTERVAL : STRAINING_INTERVAL;

        // Complain at the sources. They are where a person would put their hand to feel a machine
        // labouring, there are far fewer of them than members, and it points at the half of the
        // problem the player can actually do something about.
        for (RotationNode source : sources.keySet()) {
            BlockPos pos = source.getBlockPos();
            server.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                    overloaded ? 4 : 1, 0.2, 0.1, 0.2, 0.01);
            server.playSound(null, pos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS,
                    overloaded ? 0.35f : 0.2f, overloaded ? 0.45f : 0.6f);
        }
    }

    /**
     * Recompute the ledger and tell every member, but only when something actually moved.
     * Members are told whenever a figure changes, because "am I overstressed" is a network
     * answer and a node has no way to work it out for itself.
     */
    public void recalculate() {
        float newCapacity = 0;
        float newStaticLoad = 0;
        float newDrag = 0;
        float newInertia = 0;

        dropStale(sources);
        for (Map.Entry<RotationNode, Float> entry : sources.entrySet())
            newCapacity += entry.getValue();

        dropStale(members);
        for (RotationNode member : members.keySet()) {
            newStaticLoad += member.getLoadSu();
            newDrag += member.getDragSuPerRpm();
            newInertia += member.getInertia();
        }

        inertia = newInertia;

        if (newCapacity == capacitySu && newStaticLoad == staticLoadSu && newDrag == dragSuPerRpm)
            return;
        capacitySu = newCapacity;
        staticLoadSu = newStaticLoad;
        dragSuPerRpm = newDrag;
        for (RotationNode member : members.keySet())
            member.onNetworkChanged(capacitySu, getLoadSu());
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

    /**
     * True only when something is driving the network and it still cannot turn.
     * <p>
     * Two things are deliberately <em>not</em> overstress. A network with no source is not
     * overloaded, it is unpowered -- an idle shaft reporting OVERSTRESSED because its own
     * friction exceeds a capacity of zero is nonsense. And a network that simply cannot reach
     * the speed asked of it is not faulty either; it settles at a lower speed, which is what
     * friction is supposed to feel like.
     * <p>
     * What remains is the real fault: machines demanding more than the supply can give even
     * before anything starts turning.
     */
    public boolean isOverstressed() {
        return capacitySu > 0 && staticLoadSu > capacitySu;
    }

    public float getCapacitySu() {
        return capacitySu;
    }

    public float getStaticLoadSu() {
        return staticLoadSu;
    }

    public float getDragSuPerRpm() {
        return dragSuPerRpm;
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
