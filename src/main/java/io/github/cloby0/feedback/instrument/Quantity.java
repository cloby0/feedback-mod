package io.github.cloby0.feedback.instrument;

/**
 * Something a player might want a number for.
 *
 * <h2>Why measurement is never general</h2>
 * A thermometer says nothing about how flattened an ingot is, and calipers say nothing about how
 * fast a shaft turns. Precision is not a property a player accumulates — it is bought one
 * quantity at a time, and owning one instrument must never quietly sharpen every readout in the
 * game.
 */
public enum Quantity {

    /** Mechanical work beaten into a workpiece, in Fu. */
    WORK,
    /** How fast a run is turning, in RPM. */
    SPEED,
    /** What a network is carrying, in Su. */
    LOAD,
    /** How hot something is, in Tu. Nothing reads this yet. */
    TEMPERATURE
}
