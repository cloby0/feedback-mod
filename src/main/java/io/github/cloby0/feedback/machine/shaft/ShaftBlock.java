package io.github.cloby0.feedback.machine.shaft;

import io.github.cloby0.feedback.core.rotation.Rotatable;
import io.github.cloby0.feedback.registry.FBlockEntities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Transmits rotation along its axis, and does nothing else.
 * <p>
 * The shaft is not a machine and is deliberately dumb: laying one out is a spatial and budget
 * puzzle (philosophy 3, "keep the shaft, skip the fluid dynamics"), so the block exists and the
 * simulation inside it does not.
 */
public class ShaftBlock extends RotatedPillarBlock implements EntityBlock, Rotatable {

    private static final VoxelShape X = box(0, 6, 6, 16, 10, 10);
    private static final VoxelShape Y = box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape Z = box(6, 6, 0, 10, 10, 16);

    public ShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> X;
            case Y -> Y;
            case Z -> Z;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Place along the face the player clicked, so a shaft run extends the way it is pointed.
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelAccessor level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    // TEMPORARY: sneak-right-click prints the network state. Development scaffolding, not a
    // feature -- see RotationNode#debugReport.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown() && !level.isClientSide
                && level.getBlockEntity(pos) instanceof ShaftBlockEntity node)
            node.debugReport(player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShaftBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Only the client needs a tick, and only to advance the rendered angle.
        return level.isClientSide ? (l, p, s, be) -> ((ShaftBlockEntity) be).tickClient() : null;
    }
}
