package io.github.cloby0.feedback.item;

import io.github.cloby0.feedback.instrument.Instrument;
import io.github.cloby0.feedback.instrument.Quantity;

import net.minecraft.network.chat.Component;
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
public class CalipersItem extends Item implements Instrument {

    public CalipersItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canRead(Quantity quantity) {
        return quantity == Quantity.WORK;
    }

    @Override
    public float resolution(Quantity quantity) {
        return 1f;   // Fu is whole numbers; there is nothing finer to resolve.
    }

    @Override
    public Component label() {
        return Component.translatable("feedback.instrument.calipers");
    }
}
