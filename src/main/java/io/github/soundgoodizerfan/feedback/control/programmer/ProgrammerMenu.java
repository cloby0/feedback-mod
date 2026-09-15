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
package io.github.soundgoodizerfan.feedback.control.programmer;

import io.github.soundgoodizerfan.feedback.registry.FItems;
import io.github.soundgoodizerfan.feedback.registry.FMenus;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * One Punch Card slot plus the player's own inventory. The node canvas itself is not slot-based
 * -- it lives entirely in {@link ProgrammerScreen}, reading {@link ProgrammerBlockEntity#getDisplayedGraph()}
 * straight off the block entity the way Jade already reads a debug controller's links, and
 * sending edits back over {@link ProgrammerActionPayload} rather than through a slot.
 */
public class ProgrammerMenu extends AbstractContainerMenu {

    private static final int CARD_SLOT = 0;
    /** Must track {@code ProgrammerScreen}'s CARD_SLOT_Y/palette/print layout -- kept clear of the left-rail widgets there. */
    private static final int CARD_SLOT_Y = 20;
    private static final int PLAYER_INV_Y = 192;
    private static final int HOTBAR_Y = 250;

    private final Container container;
    private final BlockPos pos;
    @Nullable
    private final ProgrammerBlockEntity programmer;

    /** Client-side open. */
    public ProgrammerMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, pos, resolve(playerInventory, pos));
    }

    /** Server-side open. */
    public ProgrammerMenu(int id, Inventory playerInventory, ProgrammerBlockEntity programmer) {
        this(id, playerInventory, programmer.getBlockPos(), programmer);
    }

    private ProgrammerMenu(int id, Inventory playerInventory, BlockPos pos, @Nullable ProgrammerBlockEntity programmer) {
        super(FMenus.PROGRAMMER.get(), id);
        this.pos = pos;
        this.programmer = programmer;
        this.container = programmer != null ? programmer : new SimpleContainer(1);

        addSlot(new Slot(container, CARD_SLOT, 8, CARD_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(FItems.PUNCH_CARD.get());
            }
        });

        for (int row = 0; row < 3; row++)
            for (int column = 0; column < 9; column++)
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, PLAYER_INV_Y + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column, 8 + column * 18, HOTBAR_Y));
    }

    @Nullable
    private static ProgrammerBlockEntity resolve(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof ProgrammerBlockEntity programmer
                ? programmer : null;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public ProgrammerBlockEntity getProgrammer() {
        return programmer;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack held = slot.getItem();
        ItemStack moved = held.copy();

        if (index == CARD_SLOT) {
            if (!moveItemStackTo(held, 1, slots.size(), true))
                return ItemStack.EMPTY;
        } else if (held.is(FItems.PUNCH_CARD.get())) {
            if (!moveItemStackTo(held, CARD_SLOT, CARD_SLOT + 1, false))
                return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (held.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();
        if (held.getCount() == moved.getCount())
            return ItemStack.EMPTY;

        slot.onTake(player, held);
        return moved;
    }
}
