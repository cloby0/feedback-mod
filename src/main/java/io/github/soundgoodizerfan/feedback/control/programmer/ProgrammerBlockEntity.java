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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import io.github.soundgoodizerfan.feedback.control.Switchable;
import io.github.soundgoodizerfan.feedback.control.data.DataLinkManager;
import io.github.soundgoodizerfan.feedback.control.data.DataNode;
import io.github.soundgoodizerfan.feedback.control.data.DataNodeRef;
import io.github.soundgoodizerfan.feedback.control.program.ActuatorNode;
import io.github.soundgoodizerfan.feedback.control.program.AndNode;
import io.github.soundgoodizerfan.feedback.control.program.ComparatorNode;
import io.github.soundgoodizerfan.feedback.control.program.ProgramGraph;
import io.github.soundgoodizerfan.feedback.control.program.ProgramNode;
import io.github.soundgoodizerfan.feedback.control.program.SensorNode;
import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.fitting.SensorFitting;
import io.github.soundgoodizerfan.feedback.instrument.Quantity;
import io.github.soundgoodizerfan.feedback.registry.FBlockEntities;
import io.github.soundgoodizerfan.feedback.registry.FDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Where a Punch Card's {@link ProgramGraph} is authored. Holds the card being worked on plus a
 * separate {@link #working} graph -- printing is the one moment they become the same thing.
 * <p>
 * A printed card (its own {@code PROGRAM_GRAPH} component non-empty) refuses every further
 * {@link ProgrammerAction}: spec §4's hard-rewrite tier is enforced here, not by the item.
 */
public class ProgrammerBlockEntity extends BlockEntity implements Container, MenuProvider {

    private static final String CARD = "Card";
    private static final String WORKING = "Working";
    private static final String NEXT_ID = "NextId";
    private static final String POSITIONS = "Positions";

    private ItemStack card = ItemStack.EMPTY;
    private ProgramGraph working = ProgramGraph.EMPTY;
    private int nextNodeId = 1;

    /**
     * Where each card sits on the {@link ProgrammerScreen} canvas -- cosmetic only, never part
     * of {@link ProgramGraph} (a printed card carries no layout, only wiring). Keyed by node id,
     * same lifetime as {@link #working}.
     */
    private final Map<Integer, NodePos> positions = new HashMap<>();

    public ProgrammerBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.PROGRAMMER.get(), pos, state);
    }

    public boolean isPrinted() {
        return !card.getOrDefault(FDataComponents.PROGRAM_GRAPH, ProgramGraph.EMPTY).nodes().isEmpty();
    }

    public Map<Integer, NodePos> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    /** What a screen should draw: the printed graph if there is one, else the working copy. */
    public ProgramGraph getDisplayedGraph() {
        ProgramGraph printed = card.getOrDefault(FDataComponents.PROGRAM_GRAPH, ProgramGraph.EMPTY);
        return printed.nodes().isEmpty() ? working : printed;
    }

    public void apply(ProgrammerAction action, Player player) {
        if (level == null || level.isClientSide)
            return;
        if (isPrinted() && !(action instanceof ProgrammerAction.Print)) {
            player.displayClientMessage(Component.translatable("feedback.programmer.already_printed"), true);
            return;
        }

        switch (action) {
            case ProgrammerAction.AddNode a -> addNode(a.typeKey(), player);
            case ProgrammerAction.RemoveNode a -> removeNode(a.nodeId());
            case ProgrammerAction.SetInput a -> setInput(a.nodeId(), a.slot(), a.fromId(), player);
            case ProgrammerAction.SetLiteral a -> setLiteral(a.nodeId(), a.value());
            case ProgrammerAction.ToggleOp a -> toggleOp(a.nodeId());
            case ProgrammerAction.Link a -> link(a.nodeId(), player);
            case ProgrammerAction.MoveNode a -> moveNode(a.nodeId(), a.x(), a.y());
            case ProgrammerAction.Print ignored -> print(player);
        }
        sync();
    }

    /**
     * A Sensor/Actuator card placed while a connector selection is already pending links itself
     * immediately -- the ordinary case (select in the world, then place the card) shouldn't need
     * a second click on top of that. {@link #link} stays as the explicit fallback: placing
     * without a pending selection, or relinking a card afterward.
     */
    private void addNode(String typeKey, Player player) {
        if (working.nodes().size() >= FTuning.CONTROLLER_MAX_NODES)
            return;
        int id = nextNodeId++;
        ProgramNode node = switch (typeKey) {
            case SensorNode.TYPE -> new SensorNode(id, null, null);
            case "comparator_greater" -> new ComparatorNode(id, ComparatorNode.Compare.GREATER,
                    ProgramNode.UNWIRED, ProgramNode.UNWIRED, 0f);
            case "comparator_less" -> new ComparatorNode(id, ComparatorNode.Compare.LESS,
                    ProgramNode.UNWIRED, ProgramNode.UNWIRED, 0f);
            case AndNode.TYPE -> new AndNode(id, ProgramNode.UNWIRED, ProgramNode.UNWIRED);
            case ActuatorNode.TYPE -> new ActuatorNode(id, ProgramNode.UNWIRED, null);
            default -> null;
        };
        if (node == null)
            return;

        if (node instanceof SensorNode || node instanceof ActuatorNode) {
            Optional<DataNodeRef> pending = DataLinkManager.get().peekPending(player);
            if (pending.isPresent()) {
                ProgramNode linked = resolveLink(node, pending.get());
                if (linked != null) {
                    DataLinkManager.get().takePending(player);
                    node = linked;
                }
            }
        }

        List<ProgramNode> nodes = new ArrayList<>(working.nodes());
        nodes.add(node);
        working = new ProgramGraph(nodes);
        positions.put(id, defaultPosition(nodes.size() - 1));
    }

    private void removeNode(int nodeId) {
        List<ProgramNode> nodes = new ArrayList<>(working.nodes());
        nodes.removeIf(n -> n.id() == nodeId);
        working = new ProgramGraph(nodes);
        positions.remove(nodeId);
    }

    /**
     * A Number port only ever comes from a {@link SensorNode}, and a Boolean port only from a
     * {@link ComparatorNode}/{@link AndNode} -- spec §2.3's port types, checked here rather than
     * left to fail at evaluation. Two {@link SensorNode}s feeding the same comparator must also
     * share a {@link Quantity} once both are linked (unlinked sides are simply not checked yet).
     */
    private void setInput(int nodeId, int slot, int fromId, Player player) {
        ProgramNode target = working.find(nodeId).orElse(null);
        ProgramNode from = working.find(fromId).orElse(null);
        if (target == null || from == null)
            return;

        if (target instanceof ComparatorNode comparator) {
            if (!(from instanceof SensorNode fromSensor)) {
                player.displayClientMessage(Component.translatable("feedback.programmer.error.type_mismatch"), true);
                return;
            }
            int otherInputId = slot == 0 ? comparator.rightInput() : comparator.leftInput();
            if (otherInputId != ProgramNode.UNWIRED
                    && working.find(otherInputId).orElse(null) instanceof SensorNode otherSensor
                    && otherSensor.quantity() != null && fromSensor.quantity() != null
                    && otherSensor.quantity() != fromSensor.quantity()) {
                player.displayClientMessage(Component.translatable("feedback.programmer.error.quantity_mismatch"), true);
                return;
            }
        } else if ((target instanceof AndNode || target instanceof ActuatorNode)
                && !(from instanceof ComparatorNode || from instanceof AndNode)) {
            player.displayClientMessage(Component.translatable("feedback.programmer.error.type_mismatch"), true);
            return;
        }

        replaceNode(nodeId, node -> switch (node) {
            case ComparatorNode n -> slot == 0
                    ? new ComparatorNode(n.id(), n.op(), fromId, n.rightInput(), n.literalDefault())
                    : new ComparatorNode(n.id(), n.op(), n.leftInput(), fromId, n.literalDefault());
            case AndNode n -> slot == 0
                    ? new AndNode(n.id(), fromId, n.rightInput())
                    : new AndNode(n.id(), n.leftInput(), fromId);
            case ActuatorNode n -> new ActuatorNode(n.id(), fromId, n.target());
            case SensorNode n -> n;
        });
    }

    private void moveNode(int nodeId, int x, int y) {
        if (working.find(nodeId).isPresent())
            positions.put(nodeId, new NodePos(x, y));
    }

    /** Cascaded so a fresh card never lands exactly on top of the last one; the player can drag it anywhere after. */
    private static NodePos defaultPosition(int index) {
        return new NodePos(20 + (index % 2) * 80, 20 + (index / 2) * 40);
    }

    private void setLiteral(int nodeId, float value) {
        replaceNode(nodeId, node -> node instanceof ComparatorNode n
                ? new ComparatorNode(n.id(), n.op(), n.leftInput(), n.rightInput(), value)
                : node);
    }

    private void toggleOp(int nodeId) {
        replaceNode(nodeId, node -> node instanceof ComparatorNode n
                ? new ComparatorNode(n.id(), n.op() == ComparatorNode.Compare.GREATER
                        ? ComparatorNode.Compare.LESS : ComparatorNode.Compare.GREATER,
                        n.leftInput(), n.rightInput(), n.literalDefault())
                : node);
    }

    private void link(int nodeId, Player player) {
        if (level == null)
            return;
        ProgramNode current = working.find(nodeId).orElse(null);
        if (!(current instanceof SensorNode) && !(current instanceof ActuatorNode))
            return;

        Optional<DataNodeRef> pending = DataLinkManager.get().peekPending(player);
        if (pending.isEmpty()) {
            player.displayClientMessage(Component.translatable("feedback.programmer.nothing_selected"), true);
            return;
        }
        ProgramNode linked = resolveLink(current, pending.get());
        if (linked == null) {
            player.displayClientMessage(Component.translatable(current instanceof SensorNode
                    ? "feedback.programmer.not_a_sensor" : "feedback.programmer.not_switchable"), true);
            return;
        }
        DataLinkManager.get().takePending(player);
        replaceNode(nodeId, ignored -> linked);
    }

    /**
     * Whether {@code ref} fits {@code node}'s target type, and what to set on the node if so --
     * shared by {@link #addNode} (auto-link on placement) and {@link #link} (explicit/relink).
     * Returns {@code null} without side effects if it doesn't fit; the caller decides whether to
     * consume the pending selection.
     */
    @Nullable
    private ProgramNode resolveLink(ProgramNode node, DataNodeRef ref) {
        if (level == null)
            return null;
        Optional<DataNode> resolved = DataNode.resolve(level, ref);
        if (node instanceof SensorNode sensor)
            return resolved.filter(SensorFitting.class::isInstance).map(SensorFitting.class::cast)
                    .<ProgramNode>map(fitting -> new SensorNode(sensor.id(), ref, resolveQuantity(fitting)))
                    .orElse(null);
        if (node instanceof ActuatorNode actuator)
            return resolved.filter(Switchable.class::isInstance).isPresent()
                    ? new ActuatorNode(actuator.id(), actuator.input(), ref) : null;
        return null;
    }

    /** The one {@link Quantity} a linked fitting reads -- every fitting so far reads exactly one. */
    @Nullable
    private static Quantity resolveQuantity(SensorFitting fitting) {
        for (Quantity quantity : Quantity.values())
            if (fitting.canRead(quantity))
                return quantity;
        return null;
    }

    private void print(Player player) {
        if (isPrinted())
            return;
        if (card.isEmpty()) {
            // card is ItemStack.EMPTY here -- the shared vanilla singleton. card.set(...) below
            // mutates whatever stack `card` refers to, so skipping this check would corrupt that
            // singleton's component map for every empty stack in the game, not just this one.
            player.displayClientMessage(Component.translatable("feedback.programmer.no_card"), true);
            return;
        }
        Optional<String> error = working.findError(FTuning.CONTROLLER_MAX_NODES);
        if (error.isPresent()) {
            player.displayClientMessage(Component.translatable("feedback.programmer.error." + error.get()), true);
            return;
        }
        card.set(FDataComponents.PROGRAM_GRAPH, working);
        player.displayClientMessage(Component.translatable("feedback.programmer.printed"), true);
    }

    private void replaceNode(int nodeId, UnaryOperator<ProgramNode> mutator) {
        List<ProgramNode> nodes = new ArrayList<>(working.nodes());
        for (int i = 0; i < nodes.size(); i++)
            if (nodes.get(i).id() == nodeId) {
                nodes.set(i, mutator.apply(nodes.get(i)));
                break;
            }
        working = new ProgramGraph(nodes);
    }

    // --- Container (one Punch Card slot) ---------------------------------------------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return card.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return card;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack result = card.split(count);
        if (card.isEmpty())
            resetWorking();
        if (!result.isEmpty())
            setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = card;
        card = ItemStack.EMPTY;
        resetWorking();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        card = stack;
        card.limitSize(getMaxStackSize(card));
        resetWorking();
        setChanged();
    }

    private void resetWorking() {
        working = ProgramGraph.EMPTY;
        nextNodeId = 1;
        positions.clear();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        card = ItemStack.EMPTY;
        resetWorking();
    }

    // --- MenuProvider ---------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("feedback.container.programmer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ProgrammerMenu(id, playerInventory, this);
    }

    // --- client sync ------------------------------------------------------------------------

    /** Also called from {@link ProgrammerBlock} right before opening the menu -- the screen reads {@link #working}/{@link #positions} straight off this block entity, not through a synced menu slot, so the opening player needs a fresh push rather than relying on whatever sync happened to fire during the last edit. */
    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // --- persistence --------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!card.isEmpty())
            tag.put(CARD, card.save(registries, new CompoundTag()));
        tag.put(WORKING, ProgramGraph.CODEC.encodeStart(NbtOps.INSTANCE, working).getOrThrow());
        tag.putInt(NEXT_ID, nextNodeId);

        ListTag positionList = new ListTag();
        positions.forEach((id, pos) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Id", id);
            entry.putInt("X", pos.x());
            entry.putInt("Y", pos.y());
            positionList.add(entry);
        });
        tag.put(POSITIONS, positionList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        card = tag.contains(CARD)
                ? ItemStack.parse(registries, tag.getCompound(CARD)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        working = tag.contains(WORKING)
                ? ProgramGraph.CODEC.parse(NbtOps.INSTANCE, tag.get(WORKING)).result().orElse(ProgramGraph.EMPTY)
                : ProgramGraph.EMPTY;
        nextNodeId = Math.max(1, tag.getInt(NEXT_ID));

        positions.clear();
        for (Tag entry : tag.getList(POSITIONS, Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) entry;
            positions.put(c.getInt("Id"), new NodePos(c.getInt("X"), c.getInt("Y")));
        }
    }
}
