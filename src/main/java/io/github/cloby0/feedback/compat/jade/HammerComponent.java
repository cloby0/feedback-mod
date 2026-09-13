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
package io.github.cloby0.feedback.compat.jade;

import io.github.cloby0.feedback.instrument.Quantity;
import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.client.Readout;
import io.github.cloby0.feedback.machine.hammer.MechanicalHammerBlockEntity;
import io.github.cloby0.feedback.machine.linkage.Throw;
import io.github.cloby0.feedback.registry.FDataComponents;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * What is on the anvil, and roughly how far along it is.
 *
 * <h2>Why the specs are exact and the progress is not</h2>
 * {@code 12 / 3 St} and {@code 80 Su} are stamped on the machine (§17): a spec sheet is a thing you
 * read off the casting, not a measurement, and nothing is discovered by hiding it. What the
 * workpiece has absorbed is the other half of philosophy 8's sentence -- it is the state of an
 * object, it changes while you watch, and reading it is what calipers will be sold for.
 * <p>
 * The figures are asked of the block entity rather than read from {@code FTuning}, because a
 * machine stating its own capability is the design (§17) and a HUD that quoted the tuning table
 * would go on being right after somebody built a second, stronger hammer.
 *
 * <h2>No server data needed</h2>
 * Unlike rotation, everything here is already on the client: the workpiece is synced for rendering
 * and carries its own {@code Fu} as data components.
 */
public class HammerComponent implements IBlockComponentProvider {

    public static final HammerComponent INSTANCE = new HammerComponent();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof MechanicalHammerBlockEntity hammer))
            return;

        tooltip.add(Component.translatable("feedback.spec.strength",
                Readout.number(hammer.getStrength(Throw.SHORT)),
                Readout.number(hammer.getStrength(Throw.LONG))));
        tooltip.add(Component.translatable("feedback.spec.ceiling",
                Readout.number(hammer.getMaxStrength())));
        tooltip.add(Component.translatable("feedback.spec.load", Readout.number(hammer.getLoadSu())));

        // The spec figures above are already scaled by condition, so a worn hammer quotes what it
        // can actually do rather than what it left the workshop able to do. This line says why
        // they moved -- without it the numbers on the casting would appear to change on their own.
        Component condition = Readout.condition(hammer.getCondition());
        if (condition != null)
            tooltip.add(condition);

        ItemStack workpiece = hammer.getWorkpiece();
        if (workpiece.isEmpty())
            return;

        tooltip.add(workpiece.getHoverName());

        int worked = workpiece.getOrDefault(FDataComponents.WORK.get(), 0);
        int required = workpiece.getOrDefault(FDataComponents.WORK_REQUIRED.get(), 0);
        if (worked <= 0 || required <= 0)
            return;

        if (Readout.instrumented(Quantity.WORK))
            tooltip.add(Readout.reading(Quantity.WORK, "feedback.readout.work", worked, required));
        else
            tooltip.add(Readout.progress(worked, required));
    }

    @Override
    public ResourceLocation getUid() {
        return Feedback.id("mechanical_hammer");
    }
}
