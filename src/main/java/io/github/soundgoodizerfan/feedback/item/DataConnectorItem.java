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

import io.github.soundgoodizerfan.feedback.control.data.DataLinkManager;
import io.github.soundgoodizerfan.feedback.control.data.DataNode;
import io.github.soundgoodizerfan.feedback.fitting.Fittable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Links two {@link DataNode}s -- select one, then the other, no cable in the world at any point.
 *
 * <h2>No node-precise raycasting needed, and that is an actual simplification</h2>
 * MrCrayfish's Furniture Mod: Refurbished (MIT) needs custom sub-block raytracing because several
 * of its electricity nodes can live inside one block. Feedback's sided fittings map exactly onto
 * vanilla's own block face hit result -- one fitting per side, and vanilla already tells any
 * {@code useOn} which face was clicked -- so this needs no raycast of its own at all. Adapting
 * the read changed the shape rather than just the names.
 */
public class DataConnectorItem extends Item {

    public DataConnectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        DataNode target = resolve(level, context);
        if (target == null)
            return InteractionResult.PASS;

        if (!level.isClientSide)
            DataLinkManager.get().onNodeInteract(player, target);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static DataNode resolve(Level level, UseOnContext context) {
        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        if (be instanceof DataNode node)
            return node;

        Direction side = context.getClickedFace();
        if (be instanceof Fittable fittable && fittable.getSidedFitting(side) instanceof DataNode node)
            return node;

        return null;
    }
}
