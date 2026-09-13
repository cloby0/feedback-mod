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
package io.github.cloby0.feedback.instrument;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Finds what the player can measure with. */
public final class Instruments {

    private Instruments() {
    }

    /**
     * The finest instrument the player is carrying that can read this quantity, or null.
     *
     * <h3>Carried, not held</h3>
     * Reading is passive and costs no hands: you look at something and either own the means to put
     * a number on it or you do not. Requiring an instrument in hand would mean swapping tools to
     * look at things, which is friction with no decision inside it (§3). Holding is reserved for
     * <em>acting</em> on the world. What you can perceive is a property of your kit; what you can
     * do is a property of your hands.
     */
    @Nullable
    public static Instrument best(@Nullable Player player, Quantity quantity) {
        if (player == null)
            return null;

        Inventory inventory = player.getInventory();
        Instrument best = null;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof Instrument candidate) || !candidate.canRead(quantity))
                continue;
            if (best == null || candidate.resolution(quantity) < best.resolution(quantity))
                best = candidate;
        }
        return best;
    }

    /**
     * Round a true value to what an instrument can actually distinguish.
     * <p>
     * This is the whole of what a tier buys. The world is never consulted differently and the
     * reading is never wrong — it is simply coarser, which is why a poor instrument costs
     * reproducibility rather than success (§8).
     */
    public static float quantise(float value, float resolution) {
        if (resolution <= 0)
            return value;
        return Math.round(value / resolution) * resolution;
    }
}
