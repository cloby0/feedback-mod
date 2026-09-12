package io.github.cloby0.feedback.client;

import java.util.Locale;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.item.CalipersItem;
import io.github.cloby0.feedback.item.DebugHelmetItem;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * The only place the mod turns a simulated figure into something a player is allowed to read.
 *
 * <h2>Why this is one class</h2>
 * Philosophy 8's rule -- senses give adjectives, numbers cost an instrument -- is a rule about
 * <em>wording</em>, and wording duplicated across a tooltip, a HUD and whatever comes next drifts
 * apart until one of them quietly starts printing a figure. Keeping the vocabulary in one place
 * makes the rule enforceable by reading a single file.
 *
 * <h2>What belongs here and what does not</h2>
 * Only <em>state</em>: what a machine is doing right now, which is the half of the information
 * layer that has to be bought. Requirements and equipment specs are free and exact (§8, <em>what
 * you need is free</em>), so they are formatted at their call sites with no ceremony -- there is no
 * rule to enforce about them.
 *
 * <h2>The instrument seam</h2>
 * {@link #instrumented()} is currently "is the player wearing the debug helmet", which is a cheat
 * standing in for a technology that does not exist yet. When calipers and thermometers arrive they
 * do not replace this -- they widen it, because the real question is per-quantity and per-accuracy:
 * <em>what resolution can this player resolve this variable to, here?</em> A boolean is the
 * degenerate case of that question and the call sites are already asking it.
 */
public final class Readout {

    private Readout() {
    }

    /**
     * A kind of measurement, because instruments are specific.
     *
     * <h3>Why this is not one boolean</h3>
     * A thermometer tells you nothing about how flattened an ingot is, and calipers tell you
     * nothing about how fast a shaft is turning. Precision is never a general property a player
     * accumulates -- it is bought one quantity at a time, and owning one instrument must not
     * quietly sharpen every readout in the game.
     */
    public enum Quantity {
        /** Mechanical work beaten into a workpiece. Calipers. */
        WORK,
        /** How fast a run is turning. No instrument for this exists yet. */
        SPEED,
        /** What a network is carrying. No instrument for this exists yet. */
        LOAD
    }

    /**
     * Whether the player may read this quantity as a figure rather than an adjective.
     * <p>
     * Reads the local player rather than taking one, because every caller is a client display and
     * there is only ever one player looking.
     * <p>
     * The debug helmet answers yes to everything — it is perfect instrumentation, which §8 says a
     * player may never actually buy, and it is a creative-only cheat precisely so that stays true.
     */
    public static boolean instrumented(Quantity quantity) {
        return source(quantity) != null;
    }

    /**
     * Which instrument, if any, is answering for this quantity.
     *
     * <h3>Why every figure is signed</h3>
     * A bare number is unfalsifiable. §8 promises that instruments never lie but says nothing
     * about them being accurate -- they drift, they have limited resolution, and eventually they
     * need recalibrating. The moment any of that is real, a player looking at a bad batch needs to
     * know <em>which instrument</em> to distrust, and two instruments of different tiers
     * disagreeing about the same quantity has to read as informative rather than broken.
     * <p>
     * So a reading is attributed to the thing that produced it, always. It also quietly teaches
     * where the number came from: not from the world, but from a device the player chose to own.
     */
    @Nullable
    public static Component source(Quantity quantity) {
        Player player = Minecraft.getInstance().player;
        if (DebugHelmetItem.wornBy(player))
            return Component.translatable("feedback.instrument.debug");
        return switch (quantity) {
            case WORK -> CalipersItem.carriedBy(player)
                    ? Component.translatable("feedback.instrument.calipers")
                    : null;
            case SPEED, LOAD -> null;
        };
    }

    /**
     * A reading, signed by the instrument that took it: {@code Calipers: 9 / 14 Fu}.
     */
    public static Component reading(Quantity quantity, String key, Object... args) {
        Component instrument = source(quantity);
        Component figure = Component.translatable(key, args);
        if (instrument == null)
            return figure;
        return Component.translatable("feedback.readout.attributed", instrument, figure)
                .withStyle(ChatFormatting.AQUA);
    }

    /**
     * How fast a run is turning, in words. The bands are in {@link FTuning} because they are a
     * balance decision about what is distinguishable, not a display detail.
     */
    public static Component speed(float rpm) {
        float magnitude = Math.abs(rpm);
        if (magnitude < FTuning.STOPPED_RPM_THRESHOLD)
            return adjective("feedback.readout.stopped");
        if (magnitude < FTuning.TURNING_RPM)
            return adjective("feedback.readout.turning_slowly");
        if (magnitude < FTuning.SPINNING_FAST_RPM)
            return adjective("feedback.readout.turning");
        return adjective("feedback.readout.spinning_fast");
    }

    /**
     * Whether a network is in trouble, in words, or null when it is not.
     *
     * <h3>Why this is free</h3>
     * It hands over no figures -- it does not say how far over, or by how much, or what the ledger
     * is. It says only <em>this one, and it is struggling</em>, which is exactly what the smoke and
     * the creak already say at the sources. A player who cannot diagnose a stalled factory at all
     * has no problem to solve, only a mod that appears broken.
     */
    @Nullable
    public static Component strain(float loadSu, float capacitySu, boolean overstressed) {
        if (overstressed)
            return Component.translatable("feedback.readout.overloaded").withStyle(ChatFormatting.RED);
        if (capacitySu > 0 && loadSu > capacitySu * FTuning.STRAINING_LOAD_FRACTION)
            return Component.translatable("feedback.readout.straining").withStyle(ChatFormatting.GOLD);
        return null;
    }

    /**
     * How far along a workpiece is, in words.
     * <p>
     * Tempting and wrong: a progress bar. That is a number wearing a picture.
     */
    public static Component progress(int worked, int required) {
        float fraction = worked / (float) required;
        if (fraction < 0.25f)
            return adjective("feedback.workpiece.barely_marked");
        if (fraction < 0.5f)
            return adjective("feedback.workpiece.taking_shape");
        if (fraction < 0.75f)
            return adjective("feedback.workpiece.visibly_worked");
        return adjective("feedback.workpiece.nearly_there");
    }

    /** A figure with one decimal place, and without one when it would read ".0". */
    public static String number(float value) {
        return value == Math.rint(value)
                ? String.format(Locale.ROOT, "%.0f", value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

    private static Component adjective(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}
