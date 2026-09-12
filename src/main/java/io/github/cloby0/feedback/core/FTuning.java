package io.github.cloby0.feedback.core;

/**
 * Every tunable number in the mod, in one place.
 *
 * <h2>Why this file exists</h2>
 * Almost none of these figures are defensible yet. Slice 1 fixes {@code Su} for a few machines
 * and never fixes a speed at all, and the philosophy is explicit that exact values are not
 * decided. They will stay fiction until the slice has been played.
 * <p>
 * So the point of centralising them is not tidiness, it is <em>turnaround</em>. Tuning should be
 * editing one list, not hunting through a dozen block entities. When the numbers start to mean
 * something, this becomes a config file; until then it is the single place a playtest note can
 * be applied without reading any code.
 * <p>
 * Anything here marked SLICE comes from feedback_slice_01.md. Everything else is invented.
 */
public final class FTuning {

    private FTuning() {
    }

    // --- sources --------------------------------------------------------------------------

    /** SLICE: the Hand Crank supplies 12 Su while somebody is turning it. */
    public static final float HAND_CRANK_CAPACITY_SU = 12f;
    /** Invented. */
    public static final float HAND_CRANK_RPM = 32f;
    /** Invented. How long one click keeps the crank turning; holding the button refreshes it. */
    public static final int HAND_CRANK_TICKS_PER_TURN = 12;

    /** SLICE: the Water Wheel supplies 256 Su, continuously, given flowing water. */
    public static final float WATER_WHEEL_CAPACITY_SU = 256f;
    /** Invented. Speed scales with how many faces have moving water against them. */
    public static final float WATER_WHEEL_RPM_PER_FLOW = 4f;
    /** Invented. Water does not change often enough to justify checking every tick. */
    public static final int WATER_WHEEL_FLOW_CHECK_INTERVAL = 20;

    // --- transmission ---------------------------------------------------------------------

    /**
     * Invented. Bearing friction, charged as Su against the network.
     * <p>
     * Loss is Su rather than RPM on purpose. A rigid shaft turns at one speed along its whole
     * length -- a speed difference between its ends is torsion, not loss. What friction actually
     * consumes is torque, and torque is Su.
     * <p>
     * Because Su sums network-wide, this is automatically distance-independent: moving a machine
     * closer to the generator saves nothing, because every bearing in the run is turning either
     * way, including the ones on a branch nobody is using. Sprawl is what costs, not distance.
     */
    public static final float SHAFT_LOSS_SU = 1f;

    /**
     * Invented. How much a shaft resists a change in speed.
     *
     * <h3>How the inertia numbers were picked</h3>
     * Acceleration is net torque over moment of inertia -- {@code (capacity - load) / inertia}
     * RPM per tick -- which is the real relationship and not a fudge. That means the inertia
     * figures have to be calibrated against the Su figures, or momentum is invisible: at the
     * first values tried, a water wheel reached full speed in a single tick.
     * <p>
     * They are set so that a water wheel takes roughly two seconds to come up to speed and a
     * hand crank a little under one. The spread between them is large, and correctly so: a water
     * wheel is an enormous slab of timber and a crank handle is not.
     */
    public static final float SHAFT_INERTIA = 2f;

    /** Invented. A generator is a lump of mass too; without this a bare source has no momentum. */
    public static final float HAND_CRANK_INERTIA = 4f;
    /** Invented, and deliberately huge -- this is why a water wheel coasts for so long. */
    public static final float WATER_WHEEL_INERTIA = 1200f;

    /**
     * Floor on total network inertia, so a network of one weightless block still takes a moment
     * to change speed instead of snapping. Prevents a divide-by-zero as well.
     */
    public static final float MINIMUM_INERTIA = 1f;

    /**
     * Global multiplier on how quickly anything changes speed. 1 means the physics above is used
     * as written.
     * <p>
     * Raise it and machines feel light and twitchy; lower it and everything runs as though in
     * oil. This is the first number to reach for when the mod feels wrong but nothing specific
     * is wrong with it.
     */
    public static final float INERTIA_RESPONSE = 1f;

    /**
     * Below this, a coasting network is called stopped. Without a floor, speed approaches zero
     * asymptotically and machines tick forever at 0.0001 RPM.
     */
    public static final float STOPPED_RPM_THRESHOLD = 0.05f;
}
