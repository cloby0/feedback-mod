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
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.github.soundgoodizerfan.feedback.control.program.ActuatorNode;
import io.github.soundgoodizerfan.feedback.control.program.AndNode;
import io.github.soundgoodizerfan.feedback.control.program.ComparatorNode;
import io.github.soundgoodizerfan.feedback.control.program.ProgramGraph;
import io.github.soundgoodizerfan.feedback.control.program.ProgramNode;
import io.github.soundgoodizerfan.feedback.control.program.SensorNode;
import io.github.soundgoodizerfan.feedback.instrument.Quantity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The node canvas from the design mockup: a palette of card kinds on the left, freely placed and
 * wired cards on the right. Cards drag by their title bar; {@link ProgrammerBlockEntity#getPositions()}
 * is the server-held, persisted layout -- {@code dragPreviewPos} below is only a same-frame
 * client preview while the mouse button is still down, never authoritative.
 * <p>
 * Every edit is a {@link ProgrammerAction} sent to the block entity at {@link ProgrammerMenu#getPos()}
 * -- nothing here mutates server state directly.
 */
public class ProgrammerScreen extends AbstractContainerScreen<ProgrammerMenu> {

    private static final int CANVAS_X = 104;
    private static final int CANVAS_Y = 18;
    private static final int CANVAS_INNER_W = 188;
    private static final int CANVAS_INNER_H = 156;
    /** Canvas backdrop's bottom edge -- above this, in the left rail, sits the palette and Print; below it, the player inventory. Keeps the two from ever sharing a row. */
    private static final int CANVAS_BOTTOM = 178;
    /** Left rail (x 8..98): the Punch Card slot up top, palette buttons below it, Print below that -- see {@link #init}. */
    private static final int CARD_SLOT_Y = 20;
    private static final int PALETTE_Y = 44;
    private static final int PALETTE_SPACING = 22;
    private static final int PRINT_Y = 160;
    private static final int NODE_W = 88;
    private static final int NODE_H = 32;
    private static final int TITLE_H = 10;
    private static final int PORT_RADIUS = 3;
    /** Screen pixels of movement before a press-in-a-node's-body turns into a drag instead of its click action. */
    private static final int DRAG_THRESHOLD = 4;

    private static final int COLOR_BOOLEAN = 0xFFCC5555;
    private static final int COLOR_NUMBER_UNKNOWN = 0xFF55CC55;
    private static final int COLOR_BOX = 0xFF3A3A3A;
    private static final int COLOR_BOX_BORDER = 0xFFAAAAAA;
    private static final int COLOR_TITLE_BAR = 0xFF57657A;

    @Nullable
    private Integer pendingFromNodeId;
    @Nullable
    private EditBox literalBox;
    private int literalBoxNodeId = -1;

    @Nullable
    private Integer draggingNodeId;
    private int dragOffsetX, dragOffsetY;
    @Nullable
    private int[] dragPreviewPos;

    /** A body click that hasn't yet resolved into either a drag or its click action -- see {@link #mouseDragged}. */
    @Nullable
    private Integer pressNodeId;
    private double pressStartX, pressStartY;
    private int pressOriginX, pressOriginY;
    @Nullable
    private Runnable pendingClickAction;

    public ProgrammerScreen(ProgrammerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 300;
        // Player inventory (ProgrammerMenu.PLAYER_INV_Y) starts at 192; keep these two in step.
        imageHeight = 276;
        inventoryLabelY = 182;
    }

    @Override
    protected void init() {
        super.init();

        addPaletteButton(0, "feedback.programmer.card.sensor", SensorNode.TYPE);
        addPaletteButton(1, "feedback.programmer.card.greater", "comparator_greater");
        addPaletteButton(2, "feedback.programmer.card.less", "comparator_less");
        addPaletteButton(3, "feedback.programmer.card.and", AndNode.TYPE);
        addPaletteButton(4, "feedback.programmer.card.actuator", ActuatorNode.TYPE);

        addRenderableWidget(Button.builder(Component.translatable("feedback.programmer.print"),
                        b -> send(new ProgrammerAction.Print()))
                .bounds(leftPos + 8, topPos + PRINT_Y, 90, 20)
                .build());
    }

    private void addPaletteButton(int index, String labelKey, String typeKey) {
        addRenderableWidget(Button.builder(Component.translatable(labelKey),
                        b -> send(new ProgrammerAction.AddNode(typeKey)))
                .bounds(leftPos + 8, topPos + PALETTE_Y + index * PALETTE_SPACING, 90, 20)
                .build());
    }

    private void send(ProgrammerAction action) {
        PacketDistributor.sendToServer(new ProgrammerActionPayload(menu.getPos(), action));
    }

    /** {@link NodePos}/{@code NodeLayout} coordinates are canvas-relative -- the canvas itself starts at {@link #CANVAS_X}/{@link #CANVAS_Y} inside the panel, past the palette. */
    private int screenX(int canvasX) {
        return leftPos + CANVAS_X + canvasX;
    }

    private int screenY(int canvasY) {
        return topPos + CANVAS_Y + canvasY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderNodeHint(graphics, mouseX, mouseY);
    }

    /**
     * The whole body is a drag origin (press and move past {@link #DRAG_THRESHOLD} -- see
     * {@link #mouseDragged}), which isn't otherwise obvious, so hovering hints at it; an
     * unlinked Sensor/Actuator card hints at the Data Connector step instead, since that's what
     * a plain click there does. Skipped near the edge ports (wiring, not dragging), while a card
     * is being dragged, and on a printed (read-only) card.
     */
    private void renderNodeHint(GuiGraphics graphics, int mouseX, int mouseY) {
        if (draggingNodeId != null || isPrinted())
            return;
        ProgramGraph graph = getDisplayedGraph();
        for (NodeLayout layout : layoutNodes(graph)) {
            int x1 = screenX(layout.x);
            int y1 = screenY(layout.y);
            int x2 = x1 + layout.w;
            if (mouseX < x1 || mouseX > x2 || mouseY < y1 || mouseY > y1 + layout.h)
                continue;
            if (mouseX < x1 + 6 || mouseX > x2 - 6)
                return; // over a port -- no hint, that's a wire click
            ProgramNode node = graph.find(layout.id()).orElse(null);
            boolean unlinked = node instanceof SensorNode s && s.target() == null
                    || node instanceof ActuatorNode a && a.target() == null;
            graphics.renderTooltip(font, Component.translatable(unlinked
                    ? "feedback.programmer.link_hint" : "feedback.programmer.drag_hint"), mouseX, mouseY);
            return;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF1E1E1E);
        graphics.fill(leftPos + CANVAS_X - 4, topPos + CANVAS_Y - 4,
                leftPos + imageWidth - 8, topPos + CANVAS_BOTTOM, 0xFF141414);

        graphics.drawString(font, Component.translatable("feedback.programmer.card_slot"),
                leftPos + 8, topPos + CARD_SLOT_Y - 10, 0xFFAAAAAA, false);

        ProgramGraph graph = getDisplayedGraph();
        List<NodeLayout> layout = layoutNodes(graph);

        for (NodeLayout node : layout)
            for (ProgramNode raw : graph.nodes())
                if (raw.id() == node.id())
                    drawWiresInto(graphics, graph, layout, raw);

        for (NodeLayout node : layout)
            drawNode(graphics, graph, node, mouseX, mouseY);

        if (pendingFromNodeId != null) {
            NodeLayout from = findLayout(layout, pendingFromNodeId);
            if (from != null) {
                int fromX = screenX(from.x) + from.w;
                int fromY = screenY(from.y) + from.h / 2;
                drawWire(graphics, fromX, fromY, mouseX, mouseY, COLOR_NUMBER_UNKNOWN);
            }
        }

        if (isPrinted())
            graphics.drawCenteredString(font, Component.translatable("feedback.programmer.printed_label"),
                    leftPos + imageWidth / 2, topPos + 4, 0xFFAAAAAA);
    }

    private ProgramGraph getDisplayedGraph() {
        return menu.getProgrammer() != null ? menu.getProgrammer().getDisplayedGraph() : ProgramGraph.EMPTY;
    }

    private boolean isPrinted() {
        return menu.getProgrammer() != null && menu.getProgrammer().isPrinted();
    }

    private List<NodeLayout> layoutNodes(ProgramGraph graph) {
        Map<Integer, NodePos> positions = menu.getProgrammer() != null
                ? menu.getProgrammer().getPositions() : Map.of();
        List<NodeLayout> layout = new ArrayList<>();
        int index = 0;
        for (ProgramNode node : graph.nodes()) {
            int x;
            int y;
            if (draggingNodeId != null && draggingNodeId == node.id() && dragPreviewPos != null) {
                x = dragPreviewPos[0];
                y = dragPreviewPos[1];
            } else {
                NodePos pos = positions.get(node.id());
                x = pos != null ? pos.x() : 20 + (index % 2) * 80;
                y = pos != null ? pos.y() : 20 + (index / 2) * 40;
            }
            layout.add(new NodeLayout(node.id(), x, y, NODE_W, NODE_H));
            index++;
        }
        return layout;
    }

    @Nullable
    private NodeLayout findLayout(List<NodeLayout> layout, int id) {
        for (NodeLayout node : layout)
            if (node.id() == id)
                return node;
        return null;
    }

    /** Distinct per {@link Quantity} so a wire's colour names what it carries, not just Number-vs-Boolean. */
    private static int quantityColor(@Nullable Quantity quantity) {
        if (quantity == null)
            return COLOR_NUMBER_UNKNOWN;
        return switch (quantity) {
            case TEMPERATURE -> 0xFFFF8833;
            case LOAD -> 0xFFAA55FF;
            case SPEED -> 0xFF55CCFF;
            case WORK -> 0xFFFFDD55;
            case CONDITION -> 0xFFAAAAAA;
        };
    }

    private static String unitSuffix(@Nullable Quantity quantity) {
        return quantity == null ? "" : Component.translatable("feedback.programmer.unit." + quantity.getSerializedName()).getString();
    }

    /** The {@link Quantity} feeding a Comparator's given input, or null if that input isn't a linked Sensor. */
    @Nullable
    private static Quantity inputQuantity(ProgramGraph graph, int fromId) {
        return graph.find(fromId).orElse(null) instanceof SensorNode sensor ? sensor.quantity() : null;
    }

    private void drawNode(GuiGraphics graphics, ProgramGraph graph, NodeLayout layout, int mouseX, int mouseY) {
        int x1 = screenX(layout.x);
        int y1 = screenY(layout.y);
        int x2 = x1 + layout.w;
        int y2 = y1 + layout.h;

        graphics.fill(x1, y1, x2, y2, COLOR_BOX);
        graphics.fill(x1, y1, x2, y1 + TITLE_H, COLOR_TITLE_BAR);
        graphics.fill(x1, y1, x2, y1 + 1, COLOR_BOX_BORDER);
        graphics.fill(x1, y2 - 1, x2, y2, COLOR_BOX_BORDER);
        graphics.fill(x1, y1, x1 + 1, y2, COLOR_BOX_BORDER);
        graphics.fill(x2 - 1, y1, x2, y2, COLOR_BOX_BORDER);

        ProgramNode node = graph.find(layout.id()).orElse(null);
        if (node == null)
            return;

        graphics.drawString(font, kindLabel(node), x1 + 4, y1 + 3, 0xFFFFFFFF, false);

        switch (node) {
            case SensorNode n -> {
                drawPort(graphics, x2, y1 + layout.h / 2, quantityColor(n.quantity()));
                graphics.drawString(font, n.target() == null
                        ? Component.translatable("feedback.programmer.unlinked")
                        : Component.translatable("feedback.programmer.linked"), x1 + 4, y1 + 18, 0xFFAAAAAA, false);
            }
            case ComparatorNode n -> {
                Quantity leftQuantity = inputQuantity(graph, n.leftInput());
                Quantity rightQuantity = n.rightInput() == ProgramNode.UNWIRED
                        ? leftQuantity : inputQuantity(graph, n.rightInput());
                drawPort(graphics, x1, y1 + layout.h / 3, quantityColor(leftQuantity));
                drawPort(graphics, x1, y1 + layout.h * 2 / 3, quantityColor(rightQuantity));
                drawPort(graphics, x2, y1 + layout.h / 2, COLOR_BOOLEAN);
                String op = n.op() == ComparatorNode.Compare.GREATER ? ">" : "<";
                String right = n.rightInput() == ProgramNode.UNWIRED
                        ? (n.literalDefault() + " " + unitSuffix(leftQuantity)).trim() : "wire";
                graphics.drawString(font, op + " " + right, x1 + 4, y1 + 18, 0xFFAAAAAA, false);
            }
            case AndNode ignored -> {
                drawPort(graphics, x1, y1 + layout.h / 3, COLOR_BOOLEAN);
                drawPort(graphics, x1, y1 + layout.h * 2 / 3, COLOR_BOOLEAN);
                drawPort(graphics, x2, y1 + layout.h / 2, COLOR_BOOLEAN);
            }
            case ActuatorNode n -> {
                drawPort(graphics, x1, y1 + layout.h / 2, COLOR_BOOLEAN);
                graphics.drawString(font, n.target() == null
                        ? Component.translatable("feedback.programmer.unlinked")
                        : Component.translatable("feedback.programmer.linked"), x1 + 4, y1 + 18, 0xFFAAAAAA, false);
            }
        }
    }

    private void drawPort(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x - PORT_RADIUS, y - PORT_RADIUS, x + PORT_RADIUS, y + PORT_RADIUS, color);
    }

    private void drawWiresInto(GuiGraphics graphics, ProgramGraph graph, List<NodeLayout> layout, ProgramNode node) {
        NodeLayout to = findLayout(layout, node.id());
        if (to == null)
            return;
        List<Integer> inputs = node.inputs();
        for (int slot = 0; slot < inputs.size(); slot++) {
            int fromId = inputs.get(slot);
            if (fromId == ProgramNode.UNWIRED)
                continue;
            NodeLayout from = findLayout(layout, fromId);
            if (from == null)
                continue;
            int fromX = screenX(from.x) + from.w;
            int fromY = screenY(from.y) + from.h / 2;
            int toY = screenY(to.y) + (inputs.size() == 1 ? to.h / 2
                    : slot == 0 ? to.h / 3 : to.h * 2 / 3);
            int toX = screenX(to.x);
            int color = node instanceof ComparatorNode ? quantityColor(inputQuantity(graph, fromId)) : COLOR_BOOLEAN;
            drawWire(graphics, fromX, fromY, toX, toY, color);
        }
    }

    /** Bresenham, one pixel per step -- {@code graphics.fill} only draws axis-aligned rects, so this is the only way to connect two ports that aren't level with each other. */
    private void drawWire(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = -Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        int x = x1, y = y1;
        while (true) {
            graphics.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2)
                break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private String kindLabel(ProgramNode node) {
        return switch (node) {
            case SensorNode ignored -> "Sensor";
            case ComparatorNode ignored -> "Compare";
            case AndNode ignored -> "AND";
            case ActuatorNode ignored -> "Clutch";
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (literalBox != null && !literalBox.isMouseOver(mouseX, mouseY))
            commitLiteral();

        // Real widgets (palette buttons, the literal EditBox) first -- an EditBox sits inside a
        // node's body, so the node hit-test below must not get first refusal on its clicks.
        // AbstractContainerScreen#mouseClicked always returns true for a click anywhere inside
        // leftPos/topPos..+imageWidth/imageHeight even when no widget/slot is hit, so it can't be
        // tried first either -- that swallowed the canvas whole. Its slot logic (Punch Card slot,
        // player inventory) still needs a turn, so it goes last as the fallback.
        for (var child : children())
            if (child.isMouseOver(mouseX, mouseY) && child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                if (button == 0)
                    setDragging(true);
                return true;
            }

        ProgramGraph graph = getDisplayedGraph();
        List<NodeLayout> layout = layoutNodes(graph);
        for (NodeLayout node : layout) {
            ProgramNode raw = graph.find(node.id()).orElse(null);
            if (raw == null)
                continue;
            if (handleNodeClick(graph, node, raw, mouseX, mouseY, button))
                return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleNodeClick(ProgramGraph graph, NodeLayout layout, ProgramNode node,
                                     double mouseX, double mouseY, int button) {
        int x1 = screenX(layout.x);
        int y1 = screenY(layout.y);
        int x2 = x1 + layout.w;
        int y2 = y1 + layout.h;
        if (mouseX < x1 || mouseX > x2 || mouseY < y1 || mouseY > y2)
            return false;

        if (isPrinted())
            return true; // hard-rewrite: read only

        if (button == 1) {
            if (node.id() == literalBoxNodeId)
                closeLiteralBox(); // node's going away -- don't leave its EditBox behind, don't commit into it either
            send(new ProgrammerAction.RemoveNode(node.id()));
            return true;
        }

        boolean nearRightEdge = mouseX > x2 - 6;
        boolean nearLeftEdge = mouseX < x1 + 6;

        if (nearRightEdge && !(node instanceof ActuatorNode)) {
            pendingFromNodeId = node.id();
            return true;
        }
        if (nearLeftEdge && pendingFromNodeId != null && pendingFromNodeId != node.id()) {
            int slot = (mouseY - y1) < layout.h / 2.0 ? 0 : 1;
            send(new ProgrammerAction.SetInput(node.id(), slot, pendingFromNodeId));
            pendingFromNodeId = null;
            return true;
        }

        // Anywhere else in the body: don't act yet -- a drag past DRAG_THRESHOLD (mouseDragged)
        // moves the card instead. Only a release without crossing that threshold runs the click
        // action below, so the whole body (not just a thin handle) works as a drag origin.
        pressNodeId = node.id();
        pressStartX = mouseX;
        pressStartY = mouseY;
        dragOffsetX = (int) mouseX - x1;
        dragOffsetY = (int) mouseY - y1;
        pressOriginX = layout.x;
        pressOriginY = layout.y;

        if (node instanceof SensorNode || node instanceof ActuatorNode) {
            pendingClickAction = () -> send(new ProgrammerAction.Link(node.id()));
        } else if (node instanceof ComparatorNode comparator) {
            pendingClickAction = mouseY - y1 < layout.h / 2.0
                    ? () -> send(new ProgrammerAction.ToggleOp(node.id()))
                    : () -> openLiteralBox(comparator, x1 + 4, y1 + 18);
        } else {
            pendingClickAction = null;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingNodeId == null && pressNodeId != null
                && (Math.abs(mouseX - pressStartX) > DRAG_THRESHOLD || Math.abs(mouseY - pressStartY) > DRAG_THRESHOLD)) {
            draggingNodeId = pressNodeId;
            dragPreviewPos = new int[]{pressOriginX, pressOriginY};
            pendingClickAction = null;
        }
        if (draggingNodeId != null) {
            int x = (int) mouseX - leftPos - CANVAS_X - dragOffsetX;
            int y = (int) mouseY - topPos - CANVAS_Y - dragOffsetY;
            dragPreviewPos = new int[]{
                    Math.max(0, Math.min(x, CANVAS_INNER_W - NODE_W)),
                    Math.max(0, Math.min(y, CANVAS_INNER_H - NODE_H))};
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingNodeId != null) {
            int id = draggingNodeId;
            draggingNodeId = null;
            pressNodeId = null;
            if (dragPreviewPos != null)
                send(new ProgrammerAction.MoveNode(id, dragPreviewPos[0], dragPreviewPos[1]));
            dragPreviewPos = null;
            return true;
        }
        if (pressNodeId != null) {
            pressNodeId = null;
            if (pendingClickAction != null) {
                pendingClickAction.run();
                pendingClickAction = null;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void openLiteralBox(ComparatorNode node, int x, int y) {
        commitLiteral();
        literalBox = new EditBox(font, x, y, 40, 12, Component.empty());
        literalBox.setValue(String.valueOf(node.literalDefault()));
        literalBoxNodeId = node.id();
        addRenderableWidget(literalBox);
    }

    private void commitLiteral() {
        if (literalBox == null)
            return;
        try {
            float value = Float.parseFloat(literalBox.getValue());
            send(new ProgrammerAction.SetLiteral(literalBoxNodeId, value));
        } catch (NumberFormatException ignored) {
            // leave the literal as it was server-side
        }
        closeLiteralBox();
    }

    private void closeLiteralBox() {
        if (literalBox == null)
            return;
        removeWidget(literalBox);
        literalBox = null;
        literalBoxNodeId = -1;
    }

    @Override
    public void onClose() {
        commitLiteral();
        super.onClose();
    }

    private record NodeLayout(int id, int x, int y, int w, int h) {
    }
}
