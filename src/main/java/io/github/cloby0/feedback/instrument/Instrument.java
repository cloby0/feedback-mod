package io.github.cloby0.feedback.instrument;

import net.minecraft.network.chat.Component;

/**
 * An item that converts one of the player's adjectives into a figure.
 *
 * <h2>The point of this interface</h2>
 * Before it, every instrument meant its own display code: which tooltip it changed, how the line
 * was worded, where the reading came from. That is the shape where a thermometer costs a day and
 * a fourth instrument costs a week.
 * <p>
 * An instrument now declares three things and nothing else — what it can read, how finely, and
 * what to call it — and every display in the mod already knows what to do with that. A new
 * instrument touches no tooltip, no HUD and no compat code.
 *
 * <h2>Resolution is here from the start on purpose</h2>
 * Philosophy 8 says each tier of instrument buys <em>significant figures</em>, not access: a
 * crude thermometer reads to 25 Tu and a good one to 0.1, and both are reading the same world.
 * Putting resolution in the interface before any instrument needs it means a tier is one number,
 * rather than a reason to reopen every display.
 */
public interface Instrument {

    /** Whether this device measures that quantity at all. */
    boolean canRead(Quantity quantity);

    /**
     * The smallest step this device can distinguish, in the quantity's own units. Zero means it
     * reports the world exactly, which no purchasable instrument should ever do (§8).
     */
    float resolution(Quantity quantity);

    /** What to sign a reading with. Every figure the mod shows names the device that took it. */
    Component label();
}
