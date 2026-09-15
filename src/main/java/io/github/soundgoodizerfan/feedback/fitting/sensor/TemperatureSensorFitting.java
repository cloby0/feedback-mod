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
package io.github.soundgoodizerfan.feedback.fitting.sensor;

import java.util.HashSet;
import java.util.Set;

import io.github.soundgoodizerfan.feedback.control.data.DataNodeRef;
import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.fitting.SensorFitting;
import io.github.soundgoodizerfan.feedback.instrument.Instruments;
import io.github.soundgoodizerfan.feedback.instrument.Quantity;
import io.github.soundgoodizerfan.feedback.machine.crucible.CrucibleBlockEntity;
import io.github.soundgoodizerfan.feedback.registry.FItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The mod's first fitting -- proof of {@code fitting/}'s shape the way the Thermometer proved
 * {@link io.github.soundgoodizerfan.feedback.instrument.Instrument}. Reads a
 * {@link CrucibleBlockEntity}'s own temperature, because the crucible is the one vessel
 * philosophy already calls "designed to be measured" (see its class doc) -- the obvious first
 * holder to wire {@code Fittable} into.
 */
public class TemperatureSensorFitting implements SensorFitting {

    private static final String LINKS = "Links";

    private final CrucibleBlockEntity holder;
    private final Direction side;
    private final Set<DataNodeRef> links = new HashSet<>();

    public TemperatureSensorFitting(CrucibleBlockEntity holder, Direction side) {
        this.holder = holder;
        this.side = side;
    }

    // --- Instrument -----------------------------------------------------------------------

    @Override
    public boolean canRead(Quantity quantity) {
        return quantity == Quantity.TEMPERATURE;
    }

    @Override
    public float resolution(Quantity quantity) {
        return FTuning.TEMPERATURE_SENSOR_RESOLUTION_TU;
    }

    @Override
    public Component label() {
        return Component.translatable("feedback.instrument.temperature_sensor");
    }

    @Override
    public Component readValue() {
        float shown = readRaw();
        return Instruments.signed(this, Component.translatable("feedback.readout.tu", Math.round(shown)));
    }

    @Override
    public float readRaw() {
        return Instruments.quantise(holder.getTemperature().value(), resolution(Quantity.TEMPERATURE));
    }

    // --- Fitting ----------------------------------------------------------------------------

    @Override
    public ItemStack getPickItem() {
        return new ItemStack(FItems.TEMPERATURE_SENSOR.get());
    }

    // --- DataNode ---------------------------------------------------------------------------

    @Override
    public BlockPos getNodePos() {
        return holder.getBlockPos();
    }

    @Override
    public Direction getNodeSide() {
        return side;
    }

    @Override
    public Level getNodeLevel() {
        return holder.getLevel();
    }

    @Override
    public BlockEntity getNodeOwner() {
        return holder;
    }

    @Override
    public Set<DataNodeRef> getNodeLinks() {
        return links;
    }

    // --- persistence --------------------------------------------------------------------------

    public void writeNbt(CompoundTag tag) {
        ListTag list = new ListTag();
        for (DataNodeRef ref : links)
            list.add(ref.toTag());
        tag.put(LINKS, list);
    }

    public void readNbt(CompoundTag tag) {
        links.clear();
        for (Tag entry : tag.getList(LINKS, Tag.TAG_COMPOUND))
            links.add(DataNodeRef.fromTag((CompoundTag) entry));
    }
}
