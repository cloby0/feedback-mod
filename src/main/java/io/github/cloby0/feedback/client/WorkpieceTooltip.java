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
 * Describes how far along a workpiece is, in words.
 *
 * <h2>Adjectives, not figures</h2>
 * Philosophy 8: the player's own senses report qualitatively and for free, and numbers must be
 * bought. Beat 1 ships no measuring tool at all, deliberately -- a part-worked ingot is visibly
 * flattening and eyes are sufficient for the problem beat 1 poses.
 * <p>
 * So this reports "visibly worked" and never {@code 22 / 30 Fu}, even though the exact figure is
 * sitting right there on the stack. The number is what calipers are <em>for</em>: they turn
 * "kinda flattened" into a reading, which is the whole reason a player buys them.
 * <p>
 * Tempting and wrong: a progress bar. That is a number wearing a picture.
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

        event.getToolTip().add(Component.translatable(describe(worked / (float) required))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    private static String describe(float progress) {
        if (progress < 0.25f)
            return "feedback.workpiece.barely_marked";
        if (progress < 0.5f)
            return "feedback.workpiece.taking_shape";
        if (progress < 0.75f)
            return "feedback.workpiece.visibly_worked";
        return "feedback.workpiece.nearly_there";
    }
}
