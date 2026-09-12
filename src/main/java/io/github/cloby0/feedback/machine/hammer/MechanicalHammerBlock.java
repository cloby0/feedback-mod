package io.github.cloby0.feedback.machine.hammer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The machine that does not know when to stop.
 * <p>
 * Right click with something to put it in, empty-handed to take it back out. Taking it out in
 * time is the entire game here.
 */
public class MechanicalHammerBlock extends Block implements EntityBlock {

    public MechanicalHammerBlock(Properties properties) {
        super(properties);
    }

    /**
     * Vanilla calls this even when the hand is empty, and only falls through to
     * {@link #useWithoutItem} when told to with PASS_TO_DEFAULT_BLOCK_INTERACTION. Returning
     * success for an empty hand therefore swallows the interaction -- which is exactly how
     * extraction came to be unreachable without breaking the block.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty() || player.isShiftKeyDown())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof MechanicalHammerBlockEntity hammer))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // An occupied hammer gives its workpiece back whatever you are holding. Requiring an
        // empty hand reads as "there is no way to get it out" the first time you try it with a
        // stack of ingots in hand, which is the only thing you would plausibly be holding.
        if (!level.isClientSide) {
            if (hammer.getWorkpiece().isEmpty())
                hammer.insert(stack);
            else
                give(player, hammer.removeWorkpiece());
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MechanicalHammerBlockEntity hammer))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (player.isShiftKeyDown())
                hammer.debugReport(player);
            else
                give(player, hammer.removeWorkpiece());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void give(Player player, ItemStack stack) {
        if (stack.isEmpty())
            return;
        if (!player.getInventory().add(stack))
            player.drop(stack, false);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MechanicalHammerBlockEntity hammer) {
            ItemStack held = hammer.getWorkpiece();
            if (!held.isEmpty())
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), held);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechanicalHammerBlockEntity(pos, state);
    }
}
