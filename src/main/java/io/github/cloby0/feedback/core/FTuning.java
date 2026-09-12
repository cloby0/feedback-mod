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
     * Invented. Bearing friction per shaft, in Su per RPM.
     *
     * <h3>Why loss is Su, and why it scales with speed</h3>
     * Loss is Su rather than RPM because a rigid shaft turns at one speed along its whole length
     * -- a difference between its ends is torsion, not loss -- and what friction consumes is
     * torque. Because Su sums network-wide, loss is automatically distance-independent: moving a
     * machine nearer the generator saves nothing, since every bearing in the run turns either
     * way, including those on a branch nobody uses. Sprawl costs; distance does not.
     * <p>
     * Scaling with speed is what makes it interesting rather than a tax. Three things follow,
     * none of which had to be designed:
     * <ul>
     *   <li><b>A stopped shaft costs nothing.</b> An idle network is free, and an unpowered one
     *       is not "overstressed" -- it simply has no load.</li>
     *   <li><b>The network finds its own top speed.</b> It accelerates until surplus torque runs
     *       out, at {@code capacity = drag x rpm}. A long run does not hit a wall and refuse; it
     *       just turns more slowly, because friction ate the torque. This is deliberately unlike
     *       a hard per-shaft Su cap, which produces the absurdity of a generator being <em>too
     *       good</em> for its own shafting.</li>
     *   <li><b>Slow and wide becomes a real alternative to fast and narrow</b>, because speed is
     *       now something you pay for by the block.</li>
     * </ul>
     */
    public static final float SHAFT_DRAG_SU_PER_RPM = 0.1f;

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
    /** Invented, and deliberately large -- this is why a water wheel coasts so visibly. */
    public static final float WATER_WHEEL_INERTIA = 400f;

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
     * Floor on braking force, so an utterly frictionless network still eventually stops rather
     * than coasting for the rest of the save.
     */
    public static final float MINIMUM_BRAKING_SU = 0.5f;

    /**
     * Below this, a coasting network is called stopped. Without a floor, speed approaches zero
     * asymptotically and machines tick forever at 0.0001 RPM.
     */
    public static final float STOPPED_RPM_THRESHOLD = 0.05f;
}
