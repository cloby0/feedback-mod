package io.github.cloby0.feedback.machine.crank;

import io.github.cloby0.feedback.core.rotation.Rotatable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The first source of rotation in the game, and the cheapest possible statement of the mod's
 * bargain: it produces power only while a person is standing there turning it.
 */
public class HandCrankBlock extends Block implements EntityBlock, Rotatable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public HandCrankBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof HandCrankBlockEntity crank) {
            // TEMPORARY: sneak reports instead of cranking. See RotationNode#debugReport.
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide)
                    crank.debugReport(player);
            } else {
                crank.turn();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelAccessor level, BlockPos pos, BlockState state, Direction face) {
        // Drives along its whole axis rather than out of one face. An axle passes through, and
        // being fussy about which end only produces cranks that silently do nothing.
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HandCrankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? (l, p, s, be) -> ((HandCrankBlockEntity) be).tickClient()
                : (l, p, s, be) -> ((HandCrankBlockEntity) be).tickServer();
    }
}
