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

    /**
     * The most St this machine can deliver, however hard it is driven.
     * <p>
     * A ceiling set by construction, not by the drive. Gearing down multiplies force without any
     * natural limit, so without this one long gear train would make every machine of a kind
     * equivalent and the force axis would collapse into how many cogs somebody was willing to
     * place. A paper blade at a million RPM still will not cut steel.
     * <p>
     * It is also what keeps a better machine worth buying once gearing exists: a gear train
     * reaches the ceiling, and only a better machine raises it.
     */
    float getMaxStrength();
}
