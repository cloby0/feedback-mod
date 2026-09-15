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
package io.github.soundgoodizerfan.feedback.control.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Select-then-connect, server-side -- the same shape as MrCrayfish's Furniture Mod: Refurbished's
 * {@code LinkManager} (MIT): the first node the player interacts with is remembered per player,
 * the second one completes the link. No cable item, no in-world entity; the pending selection is
 * pure server memory and never persisted, which is correct -- it isn't world state, it's "what
 * was I about to do."
 * <p>
 * Left behind from the read: link-length limits and a "powerable zone" -- both are about power
 * delivery, which a data link doesn't do. {@link DataNode#getMaxNodeLinks()} is this mod's only
 * cap, and it is per-node fan-out, not link distance.
 */
public final class DataLinkManager {

    private static final DataLinkManager INSTANCE = new DataLinkManager();

    public static DataLinkManager get() {
        return INSTANCE;
    }

    private final Map<UUID, DataNodeRef> pending = new HashMap<>();

    private DataLinkManager() {
    }

    /** Called server-side when a player right-clicks a data node with the connector. */
    public void onNodeInteract(Player player, DataNode node) {
        UUID id = player.getUUID();
        DataNodeRef previous = pending.remove(id);

        if (previous == null || previous.equals(node.getNodeRef())) {
            pending.put(id, node.getNodeRef());
            player.displayClientMessage(Component.translatable("feedback.data_connector.selected"), true);
            return;
        }

        Level level = node.getNodeLevel();
        Optional<DataNode> from = DataNode.resolve(level, previous);
        boolean linked = from.isPresent() && from.get().linkTo(node);
        player.displayClientMessage(Component.translatable(linked
                ? "feedback.data_connector.linked" : "feedback.data_connector.failed"), true);
    }

    /** Drops a player's half-made link -- called on logout so it can't outlive the session. */
    public void clear(Player player) {
        pending.remove(player.getUUID());
    }

    /**
     * Pops whatever this player last selected with the connector, for a Programmer card's "Link"
     * button -- reusing the same one-click selection {@link #onNodeInteract} already records,
     * without ever completing a real {@code DataNode#linkTo} between two world blocks. A card's
     * wiring lives on the printed program, not as a persistent link on either endpoint.
     */
    public Optional<DataNodeRef> takePending(Player player) {
        return Optional.ofNullable(pending.remove(player.getUUID()));
    }

    /**
     * Reads a player's pending selection without consuming it -- lets a caller check whether it
     * fits (e.g. a Sensor card checking for a {@code SensorFitting}) before deciding whether to
     * {@link #takePending} it at all.
     */
    public Optional<DataNodeRef> peekPending(Player player) {
        return Optional.ofNullable(pending.get(player.getUUID()));
    }
}
