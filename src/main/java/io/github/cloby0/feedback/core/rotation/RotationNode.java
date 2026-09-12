package io.github.cloby0.feedback.core.rotation;

import io.github.cloby0.feedback.core.FTuning;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base block entity for anything on a rotation network: shafts, cranks, wheels, machines.
 *
 * <h2>What a node knows</h2>
 * Very little, deliberately. A node knows its <em>ratio</em> -- how its own speed relates to the
 * network's -- and the Su it supplies or demands. It does not store a speed: the whole run turns
 * as one object, so speed belongs to {@link RotationNetwork} and a node just scales it.
 * <p>
 * That split is what makes momentum possible. Inertia is a property of the entire spinning mass
 * and cannot live in any single block.
 * <p>
 * Derived in design from Create's {@code KineticBlockEntity} (MIT), but much smaller: Create's
 * also carries a behaviour framework, goggle tooltips, sound scapes and a rendering bridge.
 */
public abstract class RotationNode extends BlockEntity {

    /**
     * This node's speed as a multiple of the network's. 1 for a plain shaft; gearing makes it
     * something else, and a negative ratio means it turns the other way.
     */
    protected float ratio = 1f;

    @Nullable
    protected RotationNetwork network;

    protected float networkCapacitySu;
    protected float networkLoadSu;

    /**
     * Speed as the client last heard it. The server recomputes speed every tick while a network
     * is spinning up, and sending that to clients every tick for every block would be absurd, so
     * a node only syncs once its speed has moved a noticeable amount.
     */
    protected float syncedRpm;

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

    /**
     * Su this node demands regardless of speed -- a machine's working draw.
     * <p>
     * Split from {@link #getDragSuPerRpm()} so the network can hold one figure for each and
     * recompute live load from speed without walking every member each tick.
     */
    public float getLoadSu() {
        return 0;
    }

    /** Su this node demands per RPM: friction, which costs nothing while stopped. */
    public float getDragSuPerRpm() {
        return 0;
    }

    /** How much this node resists a change in the network's speed. */
    public float getInertia() {
        return FTuning.SHAFT_INERTIA;
    }

    public boolean isSource() {
        return getGeneratedRpm() != 0;
    }

    // --- speed ------------------------------------------------------------------------------

    /**
     * This node's actual speed.
     * <p>
     * On the server this is the network's live speed scaled by this node's ratio. On the client
     * there is no network, so it is whatever the server last told us.
     */
    public float getRpm() {
        if (level != null && level.isClientSide)
            return syncedRpm;
        return network == null ? 0 : network.getCurrentRpm() * ratio;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
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

    // --- callbacks from the network -----------------------------------------------------------

    /** The Su ledger moved. */
    public void onNetworkChanged(float capacitySu, float loadSu) {
        this.networkCapacitySu = capacitySu;
        this.networkLoadSu = loadSu;
        setChanged();
    }

    /**
     * The network's speed moved. Called every tick while spinning up or down, so this must stay
     * cheap and must not sync unconditionally.
     */
    public void onNetworkSpeedChanged() {
        float rpm = getRpm();
        if (Math.abs(rpm - syncedRpm) >= 1f || (rpm == 0 && syncedRpm != 0)) {
            syncedRpm = rpm;
            sync();
        }
    }

    /**
     * Degrees of rotation per tick, per RPM. One revolution is 360 degrees and RPM counts
     * revolutions per 60 seconds of 20 ticks, so one RPM is 360/1200 = 0.3 degrees per tick.
     * <p>
     * Named because the renderer has to extrapolate with exactly this figure to place the shaft
     * between ticks. If the two ever disagreed the shaft would lurch back and forth around the
     * angle the simulation actually holds.
     */
    public static final float DEGREES_PER_TICK_PER_RPM = 0.3f;

    /**
     * Push this node's speed to clients whether or not it changed.
     *
     * <h3>Why this is not the same as {@link #onNetworkSpeedChanged()}</h3>
     * That one only syncs once the speed has moved a noticeable amount, which is right while a
     * network is spinning up and wrong immediately after a rebuild. A rebuild can hand a node a
     * speed identical to the one its old network had -- adding a shaft to a line that is already
     * turning, say -- and then nothing ever changes, nothing ever syncs, and the client goes on
     * believing the run is stopped while the server turns it. It reads as a shaft that reports
     * 12 RPM and refuses to animate.
     */
    public void syncSpeedNow() {
        syncedRpm = getRpm();
        // Adopt the run's shared angle at the same moment as its speed. After this every member
        // advances by the same amount each tick, so agreeing once is agreeing forever -- which is
        // what stops a reconnected shaft from turning at the right speed in the wrong phase.
        if (network != null)
            visualAngle = network.getPhase() * ratio;
        sync();
    }

    public void tickClient() {
        visualAngle = (visualAngle + getRpm() * DEGREES_PER_TICK_PER_RPM) % 360f;
    }

    /** Accumulated rotation in degrees. Client-side; the server never renders anything. */
    public float getVisualAngle() {
        return visualAngle;
    }

    /**
     * Rotation in degrees at a point partway through the current tick.
     * <p>
     * Frames are far shorter than ticks, so drawing {@link #getVisualAngle()} directly makes a
     * slow shaft step twenty times a second -- which reads as a machine stuttering rather than a
     * machine turning slowly. Extrapolating forward from the last tick at the current speed is
     * exact here, because speed is constant within a tick by construction.
     */
    public float getVisualAngle(float partialTick) {
        return visualAngle + getRpm() * DEGREES_PER_TICK_PER_RPM * partialTick;
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

    // --- persistence ------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Rpm", syncedRpm);
        tag.putFloat("Phase", visualAngle);
        tag.putFloat("Ratio", ratio);
        tag.putFloat("CapacitySu", networkCapacitySu);
        tag.putFloat("LoadSu", networkLoadSu);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        syncedRpm = tag.getFloat("Rpm");
        visualAngle = tag.getFloat("Phase");
        ratio = tag.contains("Ratio") ? tag.getFloat("Ratio") : 1f;
        networkCapacitySu = tag.getFloat("CapacitySu");
        networkLoadSu = tag.getFloat("LoadSu");
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
