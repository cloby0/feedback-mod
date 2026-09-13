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
package io.github.cloby0.feedback.core.unit;

/**
 * Cumulative mechanical work beaten into a workpiece.
 *
 * <h2>An integer, and that is a model decision rather than an optimisation</h2>
 * Work arrives in blows. A workpiece is at 9 Fu of the 14 it needs because it has been hit twice,
 * not because a rate integrated to 9.2, and the whole overrun mechanic depends on the player
 * being able to count: a plate at five blows on the gentle setting and two on the strong one is a
 * fact you can hold in your head. Fractional Fu would make "one more blow" a question rather than
 * an arithmetic.
 * <p>
 * So this is the one unit that does not wrap a float, which is why {@link Units} carries integer
 * flavours of its codec and wire helpers, and why {@link Unit#raw()} is documented as good enough
 * to draw with rather than as the value.
 *
 * <h2>Not independent of {@link St}</h2>
 * §17 is explicit and it is the easiest thing here to get wrong: work delivered per blow is
 * {@code St / hardness}. A machine has a blow strength; what that blow <em>accomplishes</em> is a
 * property of the material being hit, never a machine stat. The two types are separate because
 * they are different quantities, not because they are unrelated -- and no method here converts
 * one to the other, because the conversion needs a material and neither type has one.
 */
public record Fu(int value) implements Unit {

    @Override
    public float raw() {
        return value;
    }

    @Override
    public String unitKey() {
        return "feedback.unit.fu";
    }
}
