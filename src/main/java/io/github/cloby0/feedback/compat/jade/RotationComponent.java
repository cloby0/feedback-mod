package io.github.cloby0.feedback.compat.jade;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.client.Readout;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.machine.linkage.CrankLinkageBlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Describes a rotating block on the HUD, in words.
 *
 * <h2>Two kinds of line, and the difference is the whole rule</h2>
 * Philosophy 8 splits this card down the middle. An <em>equipment spec</em> -- the throw a linkage
 * has installed -- is printed on the block (§17), so it is free and exact. <em>State</em> -- how
 * fast this thing is actually going, how loaded the network actually is -- is what instruments are
 * for, and gets an adjective.
 * <p>
 * Strain is the interesting case, and it is deliberately on the free side. It names no figure: it
 * says only that this network is near its limit or past it, which is the same thing the smoke and
 * the creak at the sources already say. Without it a stalled factory is silent and indistinguishable
 * from a broken mod, and there is no puzzle in a problem you cannot locate.
 */
public class RotationComponent implements IBlockComponentProvider {

    public static final RotationComponent INSTANCE = new RotationComponent();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof RotationNode node))
            return;

        if (node instanceof CrankLinkageBlockEntity linkage) {
            tooltip.add(Component.translatable("feedback.spec.throw",
                    Component.translatable("feedback.throw." + linkage.getThrow().getSerializedName())));
        }

        CompoundTag data = accessor.getServerData();
        if (!data.contains(RotationServerData.NETWORKED))
            return;   // no Jade on the server: say nothing rather than guess

        float rpm = data.getFloat(RotationServerData.RPM);
        boolean networked = data.getBoolean(RotationServerData.NETWORKED);
        float capacitySu = data.getFloat(RotationServerData.CAPACITY_SU);
        float loadSu = data.getFloat(RotationServerData.LOAD_SU);

        if (Readout.instrumented(Readout.Quantity.SPEED)) {
            exact(tooltip, "feedback.readout.rpm", Readout.number(rpm));
            if (networked) {
                exact(tooltip, "feedback.readout.su", Readout.number(loadSu), Readout.number(capacitySu));
                exact(tooltip, "feedback.readout.drive",
                        Readout.number(data.getFloat(RotationServerData.TARGET_RPM)),
                        Readout.number(data.getFloat(RotationServerData.INERTIA)));
            }
            return;
        }

        tooltip.add(Readout.speed(rpm));
        Component strain = Readout.strain(loadSu, capacitySu,
                data.getBoolean(RotationServerData.OVERSTRESSED));
        if (strain != null)
            tooltip.add(strain);
    }

    private static void exact(ITooltip tooltip, String key, Object... args) {
        tooltip.add(Readout.reading(Readout.Quantity.SPEED, key, args));
    }

    @Override
    public ResourceLocation getUid() {
        return Feedback.id("rotation");
    }
}
