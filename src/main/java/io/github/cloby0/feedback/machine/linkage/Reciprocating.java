package io.github.cloby0.feedback.machine.linkage;

/**
 * Implemented by a machine that is driven by strokes rather than by rotation.
 * <p>
 * The split is the point. A hammer goes up and down and a shaft goes round and round, so
 * something has to convert one into the other, and that something is a component the player
 * places rather than a detail hidden inside the machine.
 */
public interface Reciprocating {

    /**
     * One stroke has been delivered.
     *
     * @param strength the force behind it, in St, chosen by which crank is installed.
     */
    void onStroke(float strength);

    /** Su this machine draws while being driven. */
    float getLoadSu();
}
