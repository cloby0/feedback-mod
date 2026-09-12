package io.github.cloby0.feedback.process;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * What a material becomes when enough mechanical work is beaten into it.
 *
 * <h2>This is a material property, not a recipe</h2>
 * The distinction matters and the philosophy is strict about it. A machine never consults this.
 * The Mechanical Hammer knows only how to deliver {@code Fu} at a given {@code St} to whatever is
 * in front of it; it has no list of things it can make and cannot be asked what it produces.
 * <p>
 * What this table describes is how a <em>material</em> responds to being hit -- the same way a
 * melting point belongs to the metal rather than to the furnace. Entries chain: the output of one
 * is the input of the next, which is the whole of the overrun mechanic. A plate that keeps being
 * struck becomes foil because foil is what a struck plate <em>is</em>, not because the machine
 * decided to make foil.
 *
 * @param input      what is being worked. An Ingredient, so tags work and identity need not be
 *                   named -- "any copper ingot" is a property, "this exact item" is not.
 * @param work       cumulative Fu required to complete the change.
 * @param minimumSt  force each blow must reach to do anything at all. Below it the work does not
 *                   land; this is philosophy 7's hard gate, a genuine physical impossibility.
 * @param result     what it becomes.
 */
public record Deformation(Ingredient input, int work, float minimumSt, ItemStack result) {

    public static final Codec<Deformation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(Deformation::input),
            Codec.INT.fieldOf("work").forGetter(Deformation::work),
            Codec.FLOAT.optionalFieldOf("minimum_strength", 0f).forGetter(Deformation::minimumSt),
            ItemStack.CODEC.fieldOf("result").forGetter(Deformation::result)
    ).apply(instance, Deformation::new));
}
