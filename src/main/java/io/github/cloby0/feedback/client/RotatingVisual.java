package io.github.cloby0.feedback.client;

import io.github.cloby0.feedback.core.rotation.Rotatable;
import io.github.cloby0.feedback.core.rotation.RotationNode;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Draws a rotation node as its own block model, turning.
 *
 * <h2>Why this is not decoration</h2>
 * Philosophy 8 says the player's own senses are free and sufficient — that you should be able to
 * walk into a factory and read its state without buying an instrument. Until a shaft visibly
 * turns, that is a claim the mod cannot back: a running network and a dead one look identical,
 * and every question about speed has to be answered by a HUD. Motion is the first readout, and
 * the only one that costs the player nothing.
 *
 * <h2>Why instanced</h2>
 * A shaft run is many copies of one model differing only by position and angle, which is exactly
 * what instancing is for: one draw call for the whole run instead of one per block. The transform
 * is recomputed on the CPU each frame rather than derived from a clock in a vertex shader —
 * slower in principle, and irrelevant at our block counts. Create does the GPU version with its
 * own shaders, which are not ours to take.
 *
 * <p>One class covers every spinning block because {@link Rotatable#getRotationAxis} and
 * {@link Models#block} between them answer everything a rotating visual needs to know: which way
 * it turns, and what it looks like. Nothing here is per-block, so nothing here should be.
 */
public class RotatingVisual extends AbstractBlockEntityVisual<RotationNode> implements SimpleDynamicVisual {

    private final TransformedInstance instance;
    private final Direction.Axis axis;

    public RotatingVisual(VisualizationContext context, RotationNode node, float partialTick) {
        super(context, node, partialTick);

        this.axis = ((Rotatable) blockState.getBlock()).getRotationAxis(blockState);
        // Models.block bakes the state's own model, blockstate rotation included, so the model
        // already points along its axis and we only have to spin it about that axis.
        this.instance = instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, Models.block(blockState))
                .createInstance();

        instance.light(computePackedLight());
        applyTransform(partialTick);
    }

    /** Once per frame rather than once per tick, so a slow shaft glides instead of stepping. */
    @Override
    public void beginFrame(DynamicVisual.Context context) {
        applyTransform(context.partialTick());
    }

    private void applyTransform(float partialTick) {
        BlockPos origin = getVisualPosition();
        instance.setIdentityTransform()
                .translate(origin.getX(), origin.getY(), origin.getZ())
                .rotateCenteredDegrees(blockEntity.getVisualAngle(partialTick), axis)
                .setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(instance);
    }

    @Override
    protected void _delete() {
        instance.delete();
    }

    /**
     * The visualizer to hand Flywheel for a block entity type.
     * <p>
     * {@code skipVanillaRender} is left at its default of always skipping, which suppresses the
     * fallback {@link RotatingRenderer} while the backend is on. It does <em>not</em> suppress the
     * static chunk model — that is what {@code RenderShape.INVISIBLE} on the block is for, and
     * without both the block is drawn twice, once still and once spinning.
     */
    public static <T extends RotationNode> SimpleBlockEntityVisualizer.Factory<T> factory() {
        return RotatingVisual::new;
    }
}
