/*
 * Feedback -- a Minecraft technology mod.
 * Copyright (C) 2026 soundgoodizerfan
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Assets under src/main/resources/assets are NOT covered by this licence.
 * See LICENSE-ASSETS.
 */
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
        syncedRpm = liveRpm();
        visualAngle = livePhase();
        sync();
    }

    /**
     * This node's speed as the server currently knows it, falling back to the cached value on the
     * client, where there is no network to ask.
     */
    private float liveRpm() {
        if (level == null || level.isClientSide || network == null)
            return syncedRpm;
        return network.getCurrentRpm() * ratio;
    }

    /**
     * The run's shared angle, scaled by this node's gearing.
     * <p>
     * Taken from the network rather than accumulated per block, because a shaft run is bolted
     * together and its parts cannot be at different angles. Every member reads the same number,
     * so they agree by construction instead of by luck.
     */
    private float livePhase() {
        if (level == null || level.isClientSide || network == null)
            return visualAngle;
        return network.getPhase() * ratio;
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
        // Write what is true NOW, not what was last cached in these fields.
        //
        // This is worth being careful about because getting it wrong is so hard to read from the
        // symptom. syncedRpm and visualAngle are client-facing caches; on the server the live
        // values live on the network and change every tick. Shipping the cached ones meant every
        // sync handed the client a stale angle, which it snapped back to and then advanced from
        // -- so a machine changing speed appeared to rubber-band, jumping to straight and turning
        // a little, over and over. The same staleness made a coasting run look like it had
        // stopped dead the instant it was cut free.
        tag.putFloat("Rpm", liveRpm());
        tag.putFloat("Phase", livePhase());
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
