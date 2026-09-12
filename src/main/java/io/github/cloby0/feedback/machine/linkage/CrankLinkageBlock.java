package io.github.cloby0.feedback.machine.linkage;

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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Turns rotation into strokes. Shaft goes in the back, machine sits in front.
 * <p>
 * This is a block the player places rather than a detail inside the hammer, because choosing the
 * throw is the slice's first real gearing decision and it should be a thing you can point at.
 */
public class CrankLinkageBlock extends Block implements EntityBlock, Rotatable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Throw> THROW = EnumProperty.create("throw", Throw.class);

    public CrankLinkageBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(THROW, Throw.SHORT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, THROW);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Point the output at the block clicked, so it drives what you placed it against.
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    /**
     * Right click swaps the throw. Re-gearing a machine you already own is the point of the
     * component, so it should not require breaking and replacing it.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CrankLinkageBlockEntity linkage)
                linkage.debugReport(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            Throw next = state.getValue(THROW) == Throw.SHORT ? Throw.LONG : Throw.SHORT;
            level.setBlockAndUpdate(pos, state.setValue(THROW, next));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelAccessor level, BlockPos pos, BlockState state, Direction face) {
        // Driven from behind only. The front is where the reciprocating machine goes.
        return face == state.getValue(FACING).getOpposite();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrankLinkageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? (l, p, s, be) -> ((CrankLinkageBlockEntity) be).tickClient()
                : (l, p, s, be) -> ((CrankLinkageBlockEntity) be).tickServer();
    }
}
