package io.github.cloby0.feedback.process;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
 * <h2>Hardness does both jobs</h2>
 * There is deliberately no "force required" separate from "work required". Force and work were
 * two numbers describing the same event, and nothing is strong enough to dent steel yet fails to
 * slam copper -- so what a blow accomplishes is not a property of the machine at all. It is what
 * the <em>material</em> does with the force it is handed:
 * <pre>
 *     Fu per blow  =  blow force (St)  /  hardness
 * </pre>
 * One number, doing both jobs. It is the floor below which nothing happens, and the divisor for
 * how much lands above it. Copper's hardness of 1 means a 12 St blow dumps 12 Fu into it; steel's
 * 15 means the same blow does nothing whatsoever.
 * <p>
 * The consequence is the interesting part. A hard blow on a soft material <em>overshoots</em> --
 * three blows take copper to a plate and two more ruin it into foil, while a gentle blow gives
 * ten and seven. So a short crank throw is the power option and a long one is the precision
 * option, and neither is strictly better. A forging press really does wreck copper.
 *
 * @param input     what is being worked. An Ingredient, so tags work and identity need not be
 *                  named -- "any copper ingot" is a property, "this exact item" is not.
 * @param work      cumulative Fu required to complete the change.
 * @param hardness  how stubborn the material is. Both the minimum force that does anything --
 *                  philosophy 7's hard gate, a real impossibility rather than a slow version --
 *                  and how little of a bigger blow actually lands.
 * @param result    what it becomes.
 */
public record Deformation(Ingredient input, int work, float hardness, ItemStack result) {

    public static final Codec<Deformation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(Deformation::input),
            Codec.INT.fieldOf("work").forGetter(Deformation::work),
            Codec.FLOAT.optionalFieldOf("hardness", 1f).forGetter(Deformation::hardness),
            ItemStack.CODEC.fieldOf("result").forGetter(Deformation::result)
    ).apply(instance, Deformation::new));

    /**
     * For sending the table to the client, which needs it only to <em>print</em> it.
     *
     * <h3>Why the client is told at all</h3>
     * Philosophy 8: what a process requires is published data and costs nothing. The recipe
     * browser is allowed to state {@code 14 Fu} exactly, on day one, with no instrument owned --
     * so the figures have to get across. Nothing on the client ever computes with them.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Deformation> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, Deformation::input,
            ByteBufCodecs.VAR_INT, Deformation::work,
            ByteBufCodecs.FLOAT, Deformation::hardness,
            ItemStack.STREAM_CODEC, Deformation::result,
            Deformation::new);

    /**
     * Fu this material absorbs from one blow of the given force, or 0 if the blow is too weak.
     * Always at least 1 once the threshold is met, so a barely-sufficient blow still progresses
     * rather than hammering forever at zero.
     */
    public int workFrom(float blowForce) {
        if (blowForce < hardness)
            return 0;
        return Math.max(1, Math.round(blowForce / hardness));
    }
}
