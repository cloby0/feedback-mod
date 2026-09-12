package io.github.cloby0.feedback.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Turns "kinda flattened" into a figure.
 *
 * <h2>An instrument that refines rather than reveals</h2>
 * The player could always see a workpiece flattening — beat 1 deliberately ships no measuring
 * tool, because eyes are sufficient for *avoiding* scrap. What eyes cannot do is <em>aim</em>.
 * <p>
 * Calipers exist because of the overrun band: foil is a genuinely useful material sitting in a
 * narrow window past plate, and hitting that window on purpose every time is the first thing in
 * the game that needs a number rather than a glance. So this is a yield technology and not a key
 * (§7). It unlocks nothing. It improves a conversion ratio, which is why anyone buys it.
 * <p>
 * It matters that this is the weaker of the slice's two instruments and arrives first. When the
 * thermometer turns up it will reveal a quantity the player cannot perceive at all, which is a
 * much larger event — and it lands harder because calipers already taught what an instrument is
 * for.
 *
 * <h2>Why holding it is enough</h2>
 * No right-click to take a reading. Applying calipers is a real action, but as a mechanic it is
 * a keystroke with no decision inside it, and §3 cuts mechanics that are real, well-precedented
 * and simply not fun. Holding them is the same information at the same price.
 */
public class CalipersItem extends Item {

    public CalipersItem(Properties properties) {
        super(properties);
    }

    /**
     * Whether the player has calipers on them at all.
     *
     * <h3>Carried, not held</h3>
     * Reading is passive and costs no hands: you glance at a workpiece and you either own the
     * means to put a number on it or you do not. Requiring them in hand would mean swapping tools
     * to look at something, which is friction with no decision inside it (§3).
     * <p>
     * Holding is for <em>acting</em> on the world — measuring a machine in place, when something
     * eventually needs that. The distinction is worth keeping: what you can perceive is a
     * property of your kit, what you can do is a property of your hands.
     */
    public static boolean carriedBy(Player player) {
        return player != null
                && player.getInventory().contains(stack -> stack.getItem() instanceof CalipersItem);
    }
}
