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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MechanicalHammerBlockEntity hammer))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (player.isShiftKeyDown())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide && hammer.insert(stack))
            return ItemInteractionResult.SUCCESS;
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
            else {
                ItemStack taken = hammer.removeWorkpiece();
                if (!taken.isEmpty() && !player.getInventory().add(taken))
                    player.drop(taken, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
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
