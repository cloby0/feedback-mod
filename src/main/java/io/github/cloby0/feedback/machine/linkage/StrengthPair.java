package io.github.cloby0.feedback.machine.linkage;

/**
 * A machine that delivers a different force depending on how it is geared.
 * <p>
 * Philosophy 17: a machine has no single strength. It publishes both figures -- printed on the
 * block, as {@code 12 / 3 St} -- and which one the player gets depends on the crank they bolted
 * to it. Satisfying a minimum-force requirement means re-gearing the drive, not buying a
 * stronger machine.
 */
public interface StrengthPair {

    float getStrength(Throw installed);
}
