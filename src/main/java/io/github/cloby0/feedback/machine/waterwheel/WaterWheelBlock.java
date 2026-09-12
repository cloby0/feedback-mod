package io.github.cloby0.feedback.machine.waterwheel;

import io.github.cloby0.feedback.core.rotation.Rotatable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
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

/**
 * Continuous rotation, paid for with real estate and a river rather than with fuel.
 * <p>
 * Slice 1 gives it 256 Su against the hammer's 80, which is the arithmetic the player is meant
 * to run: three hammers fit, four do not, and nothing anywhere says "requires tier 2".
 */
public class WaterWheelBlock extends RotatedPillarBlock implements EntityBlock, Rotatable {

    public WaterWheelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
                && level.getBlockEntity(pos) instanceof WaterWheelBlockEntity node)
            node.debugReport(player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterWheelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? (l, p, s, be) -> ((WaterWheelBlockEntity) be).tickClient()
                : (l, p, s, be) -> ((WaterWheelBlockEntity) be).tickServer();
    }
}
