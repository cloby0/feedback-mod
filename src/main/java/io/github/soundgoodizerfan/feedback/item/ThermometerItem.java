/*
 * Feedback -- a Minecraft technology mod.
 * Copyright (C) 2026 soundgoodizerfan
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Assets under src/main/resources/assets are NOT covered by this licence.
 * See LICENSE-ASSETS.
 */
package io.github.soundgoodizerfan.feedback.item;

import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.instrument.Instrument;
import io.github.soundgoodizerfan.feedback.instrument.Quantity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

/**
 * Copper foil, a sealed tube, and something that expands.
 *
 * <h2>An instrument that reveals rather than refines</h2>
 * This is the larger of the slice's two instruments and it arrives second, which is the right way
 * round. Calipers refine a thing the player could already see -- a workpiece was visibly
 * flattening before they owned any -- and they exist to let the player <em>aim</em>. The
 * thermometer reveals a quantity the player cannot perceive at all. The word "hot" is the entire
 * vocabulary they had, and steel's window is 60 Tu wide.
 * <p>
 * The order matters because calipers already taught what an instrument is for. Arriving first,
 * this would have looked like a key.
 *
 * <h2>It is immediately, obviously insufficient</h2>
 * 25 Tu of resolution against a 60 Tu window means the player can be at 1425 or 1450 or 1475 and
 * cannot tell which. And it is still enough to work by hand -- heat, look, wait, look, adjust --
 * miserable, slow, and it produces steel reliably enough to be worth doing.
 * <p>
 * That is the progression moment, and nothing was unlocked. The recipe was never locked; what the
 * player lacked was never heat (§7). They gained <em>information</em>, and information became
 * capability. A better thermometer later buys significant figures, not access.
 *
 * <h2>Why it is carried rather than attached</h2>
 * Same argument as the calipers: reading is passive and costs no hands. The slice's wall -- that
 * the player's best vessel cannot be measured -- is not enforced by making this a cover, but by
 * the vessels themselves, through {@code ThermalBody.hasThermowell}. A sealed furnace refuses to
 * be read by <em>any</em> instrument, which is a fact about the furnace rather than a rule about
 * this item, and it stays true for every instrument we ever add.
 */
public class ThermometerItem extends Item implements Instrument {

    public ThermometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canRead(Quantity quantity) {
        return quantity == Quantity.TEMPERATURE;
    }

    @Override
    public float resolution(Quantity quantity) {
        return FTuning.THERMOMETER_RESOLUTION_TU;
    }

    @Override
    public Component label() {
        return Component.translatable("feedback.instrument.thermometer");
    }
}
