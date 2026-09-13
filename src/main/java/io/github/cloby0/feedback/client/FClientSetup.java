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
package io.github.cloby0.feedback.client;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.registry.FBlockEntities;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client registration. The whole class is {@link Dist#CLIENT}, and has to be: Flywheel is a
 * client-only mod, so a dedicated server that so much as resolved these class references would
 * fail to start.
 *
 * <p>Every spinning block is named twice here — once for a visualizer, once for the fallback
 * renderer — and a third time on the block itself, as a {@code RenderShape} that keeps it out of
 * the chunk mesh. All three are required together. Two of them and the block is drawn twice, or
 * not at all.
 */
@EventBusSubscriber(modid = Feedback.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FBlockEntities.MECHANICAL_HAMMER.get(), MechanicalHammerRenderer::new);

        event.registerBlockEntityRenderer(FBlockEntities.SHAFT.get(), RotatingRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.COG.get(), RotatingRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.HAND_CRANK.get(), RotatingRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.WATER_WHEEL.get(), RotatingRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.CLUTCH.get(), RotatingRenderer::new);
    }

    /**
     * Visualizers go in during client setup rather than alongside the renderers, because Flywheel's
     * registry is a plain map keyed by block entity type and has no registration event of its own.
     */
    @SubscribeEvent
    public static void registerVisualizers(FMLClientSetupEvent event) {
        rotatingVisual(FBlockEntities.SHAFT.get());
        rotatingVisual(FBlockEntities.COG.get());
        rotatingVisual(FBlockEntities.HAND_CRANK.get());
        rotatingVisual(FBlockEntities.WATER_WHEEL.get());
        rotatingVisual(FBlockEntities.CLUTCH.get());
    }

    private static <T extends RotationNode> void rotatingVisual(BlockEntityType<T> type) {
        SimpleBlockEntityVisualizer.builder(type)
                .factory(RotatingVisual.factory())
                .apply();
    }
}
