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
package io.github.soundgoodizerfan.feedback.compat.jade;

import io.github.soundgoodizerfan.feedback.Feedback;
import io.github.soundgoodizerfan.feedback.control.debug.DebugControllerBlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Every connected reading, sent as plain strings rather than a serialized {@link Component}.
 * <p>
 * Every other {@code ServerData} provider in this package sends primitives and lets the client
 * component rebuild the wording (see {@link ThermalServerData}); this one sends the already-built
 * text instead. That's a deliberate exception, not an inconsistency: {@code SensorFitting.readValue()}
 * formats itself, per fitting, and this block has no business knowing the shape of every sensor
 * that might ever be wired to it. It is a debug tool -- losing rich-text structure over the wire
 * costs it nothing a real controller would ever need to care about.
 */
public class DebugControllerServerData implements IServerDataProvider<BlockAccessor> {

    public static final DebugControllerServerData INSTANCE = new DebugControllerServerData();

    static final String READINGS = "Readings";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof DebugControllerBlockEntity controller))
            return;

        ListTag readings = new ListTag();
        for (Component value : controller.readConnectedValues())
            readings.add(net.minecraft.nbt.StringTag.valueOf(value.getString()));
        data.put(READINGS, readings);
    }

    @Override
    public ResourceLocation getUid() {
        return Feedback.id("debug_controller");
    }
}
