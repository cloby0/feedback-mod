package io.github.cloby0.feedback.client;

import io.github.cloby0.feedback.Feedback;
import io.github.cloby0.feedback.registry.FDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Says what a workpiece needs, and describes how far along it is.
 *
 * <h2>The two halves are not the same kind of fact</h2>
 * Philosophy 8, <em>what you need is free, what you have is a cost</em>, cuts straight through the
 * middle of this tooltip. {@code Needs 14 Fu} is published data -- a handbook figure, true before
 * the player owns anything, and stating it gates nothing. {@code Has 9 Fu} is the state of the
 * object in your hand, and that is what calipers are for.
 * <p>
 * So the requirement prints as a figure and the progress prints as a word, on adjacent lines. That
 * juxtaposition is the lesson: the player can see precisely what they are aiming at and only
 * roughly where they are, which is the entire shape of the mod in two lines of tooltip.
 */
@EventBusSubscriber(modid = Feedback.MOD_ID, value = Dist.CLIENT)
public class WorkpieceTooltip {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        int worked = stack.getOrDefault(FDataComponents.WORK.get(), 0);
        int required = stack.getOrDefault(FDataComponents.WORK_REQUIRED.get(), 0);
        if (worked <= 0 || required <= 0)
            return;

        if (Readout.instrumented(Readout.Quantity.WORK)) {
            event.getToolTip().add(Readout.reading(Readout.Quantity.WORK,
                    "feedback.readout.work", worked, required));
            return;
        }

        event.getToolTip().add(Component.translatable("feedback.workpiece.needs", required)
                .withStyle(ChatFormatting.DARK_GRAY));
        event.getToolTip().add(Readout.progress(worked, required));
    }
}
