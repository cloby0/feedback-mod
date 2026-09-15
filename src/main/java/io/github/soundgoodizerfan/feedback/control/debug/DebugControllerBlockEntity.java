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
package io.github.soundgoodizerfan.feedback.control.debug;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.soundgoodizerfan.feedback.control.data.DataNode;
import io.github.soundgoodizerfan.feedback.control.data.DataNodeRef;
import io.github.soundgoodizerfan.feedback.fitting.SensorFitting;
import io.github.soundgoodizerfan.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shows every value connected to it. The tech-demo endpoint for the data-link system, and
 * everything {@link io.github.soundgoodizerfan.feedback.item.DebugHelmetItem} already is for
 * carried instruments: a development cheat, not the top of a ladder, deliberately absent from
 * the mod's own creative tab (see {@code FItems.DEBUG_CONTROLLER}).
 * <p>
 * An unsided {@link DataNode} -- the whole block is the one endpoint, unlike a sensor fitting
 * which is one of six on its holder.
 */
public class DebugControllerBlockEntity extends BlockEntity implements DataNode {

    private static final String LINKS = "Links";

    private final Set<DataNodeRef> links = new HashSet<>();

    public DebugControllerBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.DEBUG_CONTROLLER.get(), pos, state);
    }

    /** Every readable value on the other end of a link, resolved fresh -- nothing here is cached. */
    public List<Component> readConnectedValues() {
        List<Component> values = new ArrayList<>();
        if (level == null)
            return values;
        for (DataNodeRef ref : links)
            DataNode.resolve(level, ref)
                    .filter(SensorFitting.class::isInstance)
                    .map(SensorFitting.class::cast)
                    .map(SensorFitting::readValue)
                    .ifPresent(values::add);
        return values;
    }

    // --- DataNode -----------------------------------------------------------------------------

    @Override
    public BlockPos getNodePos() {
        return getBlockPos();
    }

    @Override
    public Direction getNodeSide() {
        return null;
    }

    @Override
    public Level getNodeLevel() {
        return level;
    }

    @Override
    public BlockEntity getNodeOwner() {
        return this;
    }

    @Override
    public Set<DataNodeRef> getNodeLinks() {
        return links;
    }

    // --- persistence --------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (DataNodeRef ref : links)
            list.add(ref.toTag());
        tag.put(LINKS, list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        for (Tag entry : tag.getList(LINKS, Tag.TAG_COMPOUND))
            links.add(DataNodeRef.fromTag((CompoundTag) entry));
    }
}
