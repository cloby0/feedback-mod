package io.github.cloby0.feedback.core.rotation;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base block entity for anything on a rotation network: shafts, cranks, wheels, machines.
 * <p>
 * Derived in design from Create's {@code KineticBlockEntity} (MIT), but deliberately much
 * smaller. Create's carries goggle tooltips, sound scapes, a behaviour framework and a
 * rendering bridge; this carries speed, ownership and Su, and nothing else.
 *
 * <h2>What a node knows</h2>
 * A node knows its own speed and which neighbour drives it. It does not know the shape of the
 * network, and it cannot decide on its own whether it is overstressed -- that is a whole-network
 * question, answered by {@link RotationNetwork} and pushed down via
 * {@link #onNetworkChanged(float, float)}.
 */
public abstract class RotationNode extends BlockEntity {

    /** Signed: magnitude is speed, sign is direction of rotation about the axis. */
    protected float rpm;

    /** Position of the neighbour driving this node, or null if nothing is. */
    @Nullable
    protected BlockPos source;

    @Nullable
    protected RotationNetwork network;

    protected float networkCapacitySu;
    protected float networkLoadSu;

    /** Client-side only: accumulated rotation in degrees, for rendering. */
    protected float visualAngle;

    public RotationNode(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --- what subclasses define -------------------------------------------------------------

    /** RPM this node produces on its own. Zero for anything that merely passes rotation along. */
    public float getGeneratedRpm() {
        return 0;
    }

    /** Su this node can supply to its network. Meaningless unless {@link #isSource()}. */
    public float getCapacitySu() {
        return 0;
    }

    /** Su this node demands from its network. */
    public float getLoadSu() {
        return 0;
    }

    public boolean isSource() {
        return getGeneratedRpm() != 0;
    }

    // --- speed and ownership ----------------------------------------------------------------

    public float getRpm() {
        return isOverstressed() ? 0 : rpm;
    }

    /**
     * The speed this node would run at if the network were not overloaded.
     * <p>
     * Propagation must use this rather than {@link #getRpm()}: an overstressed network reads
     * zero everywhere, and a propagation that believed that would tear itself down and rebuild
     * the moment the load came off.
     */
    public float getTheoreticalRpm() {
        return rpm;
    }

    public void setRpm(float rpm) {
        this.rpm = rpm;
    }

    public boolean hasSource() {
        return source != null;
    }

    @Nullable
    public BlockPos getSource() {
        return source;
    }

    public void setSource(@Nullable BlockPos source) {
        this.source = source;
    }

    @Nullable
    public RotationNetwork getNetwork() {
        return network;
    }

    public void setNetwork(@Nullable RotationNetwork network) {
        this.network = network;
    }

    public boolean isOverstressed() {
        return network != null && network.isOverstressed();
    }

    public float getNetworkCapacitySu() {
        return networkCapacitySu;
    }

    public float getNetworkLoadSu() {
        return networkLoadSu;
    }

    /** Called by the network whenever the Su ledger moves. */
    public void onNetworkChanged(float capacitySu, float loadSu) {
        boolean wasOverstressed = this.networkLoadSu > this.networkCapacitySu;
        this.networkCapacitySu = capacitySu;
        this.networkLoadSu = loadSu;
        if (wasOverstressed != (loadSu > capacitySu))
            sync();
    }

    /** Called after this node's speed changes, for subclasses that care. */
    public void onSpeedChanged(float previousRpm) {
        setChanged();
    }

    // --- lifecycle --------------------------------------------------------------------------

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide)
            RotationPropagator.onAdded(level, worldPosition, this);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide)
            RotationPropagator.onRemoved(level, worldPosition, this);
        super.setRemoved();
    }

    public void tickClient() {
        // Degrees per tick: one revolution is 360 degrees, and RPM is revolutions per 60 seconds
        // of 20 ticks each, so 1 RPM is 360/1200 degrees per tick.
        visualAngle = (visualAngle + getRpm() * 0.3f) % 360f;
    }

    /** Accumulated rotation in degrees. Client-side; the server never renders anything. */
    public float getVisualAngle() {
        return visualAngle;
    }

    // --- persistence ------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Rpm", rpm);
        tag.putFloat("CapacitySu", networkCapacitySu);
        tag.putFloat("LoadSu", networkLoadSu);
        if (source != null)
            tag.put("Source", NbtUtils.writeBlockPos(source));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rpm = tag.getFloat("Rpm");
        networkCapacitySu = tag.getFloat("CapacitySu");
        networkLoadSu = tag.getFloat("LoadSu");
        source = tag.contains("Source") ? NbtUtils.readBlockPos(tag, "Source").orElse(null) : null;
    }

    // --- client sync ------------------------------------------------------------------------

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
