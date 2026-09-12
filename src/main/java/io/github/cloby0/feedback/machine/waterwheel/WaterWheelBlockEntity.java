package io.github.cloby0.feedback.machine.waterwheel;

import io.github.cloby0.feedback.core.rotation.RotationNode;
import io.github.cloby0.feedback.core.rotation.RotationPropagator;
import io.github.cloby0.feedback.registry.FBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Slice 1: 256 Su, continuous, needs flowing water.
 * <p>
 * Speed depends on how many sides are being driven, which makes siting the wheel a real
 * decision rather than a placement. Placeholder RPM -- the slice fixes Su, never speed.
 */
public class WaterWheelBlockEntity extends RotationNode {

    public static final float RPM_PER_FLOW = 4f;
    public static final float CAPACITY_SU = 256f;

    /** Rechecking flow every tick is wasted work; water does not change that often. */
    private static final int FLOW_CHECK_INTERVAL = 20;

    private int flowingSides;
    private int ticksUntilFlowCheck;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.WATER_WHEEL.get(), pos, state);
    }

    public void tickServer() {
        if (level == null)
            return;
        if (--ticksUntilFlowCheck > 0)
            return;
        ticksUntilFlowCheck = FLOW_CHECK_INTERVAL;

        int previous = flowingSides;
        flowingSides = countFlowingSides();
        if (previous != flowingSides)
            RotationPropagator.rebuildFrom(level, worldPosition);
    }

    /**
     * Count faces with moving water against them, ignoring the two the axle passes through --
     * water there would be hitting the hub rather than a blade.
     */
    private int countFlowingSides() {
        Direction.Axis axis = getBlockState().getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS);
        int count = 0;
        for (Direction face : Direction.values()) {
            if (face.getAxis() == axis)
                continue;
            FluidState fluid = level.getFluidState(worldPosition.relative(face));
            if (!fluid.isEmpty() && !fluid.isSource())
                count++;
        }
        return count;
    }

    @Override
    public float getGeneratedRpm() {
        return flowingSides * RPM_PER_FLOW;
    }

    @Override
    public float getCapacitySu() {
        return CAPACITY_SU;
    }
}
