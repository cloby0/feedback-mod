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
package io.github.soundgoodizerfan.feedback.mixin;

import io.github.soundgoodizerfan.feedback.core.FTuning;
import io.github.soundgoodizerfan.feedback.core.thermal.HeatSource;
import io.github.soundgoodizerfan.feedback.core.thermal.ThermalBody;
import io.github.soundgoodizerfan.feedback.core.thermal.VanillaVessel;
import io.github.soundgoodizerfan.feedback.core.thermal.VanillaVessels;
import io.github.soundgoodizerfan.feedback.core.unit.Conductance;
import io.github.soundgoodizerfan.feedback.core.unit.ThermalMass;
import io.github.soundgoodizerfan.feedback.core.unit.Tu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Philosophy 15: the Furnace, Smoker and Crude Blast Furnace stop being an instant black box and
 * become the crudest real thermal vessels in the game -- each one is its own fire and its own
 * body, with the same {@code (fire - vessel)} physics the crucible runs on.
 *
 * <h2>Why here, and why a mixin</h2>
 * The three block entities are vanilla classes; this is the one place in the mod something is
 * genuinely mixed in rather than added, because there is no other seam. This mixin targets the
 * shared abstract base and supplies the Furnace's own numbers as the default -- {@code Smoker} and
 * {@code BlastFurnace} each carry one small sibling mixin that overrides only what makes them a
 * different device (see {@code SmokerVesselMixin}, {@code BlastFurnaceVesselMixin}).
 *
 * <h2>What this does not touch</h2>
 * Vanilla's own {@code litTime}/{@code litDuration}/{@code cookingProgress}/
 * {@code cookingTotalTime} fields are left alone -- reading and writing them from a mixin on a
 * <em>static</em> method needs a cast through the target's own woven type, which is one risk this
 * pass does not need to take. The block's LIT state (fire, light, particles) is kept honest
 * because it costs nothing extra to set; the container screen's flame icon and progress arrow are
 * not, and stay cosmetic placeholders until that is worth doing (see TODO.md).
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceVesselMixin implements ThermalBody, HeatSource, VanillaVessel {

    private float feedback$temperature = FTuning.AMBIENT_TU.value();
    private int feedback$fuelTicksLeft;
    private int feedback$fuelDuration;
    private float feedback$flameRollTu = FTuning.AMBIENT_TU.value();
    private float feedback$accumulatedWork;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void feedback$tick(Level level, BlockPos pos, BlockState state,
                                       AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        ci.cancel();

        // AbstractFurnaceBlockEntity does not implement these interfaces in source -- only after
        // this very mixin is woven into it, which is exactly what is happening here.
        var vessel = (AbstractFurnaceBlockEntity & ThermalBody & HeatSource & VanillaVessel) blockEntity;

        VanillaVessels.TickResult result = VanillaVessels.tick(level, vessel);

        boolean wasLit = state.getValue(AbstractFurnaceBlock.LIT);
        if (wasLit != result.lit())
            level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, result.lit()), 3);

        blockEntity.setChanged();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void feedback$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putFloat("FeedbackTemperature", feedback$temperature);
        tag.putInt("FeedbackFuelTicksLeft", feedback$fuelTicksLeft);
        tag.putInt("FeedbackFuelDuration", feedback$fuelDuration);
        tag.putFloat("FeedbackFlameRoll", feedback$flameRollTu);
        tag.putFloat("FeedbackAccumulatedWork", feedback$accumulatedWork);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void feedback$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        feedback$temperature = tag.contains("FeedbackTemperature")
                ? tag.getFloat("FeedbackTemperature") : FTuning.AMBIENT_TU.value();
        feedback$fuelTicksLeft = tag.getInt("FeedbackFuelTicksLeft");
        feedback$fuelDuration = tag.getInt("FeedbackFuelDuration");
        feedback$flameRollTu = tag.getFloat("FeedbackFlameRoll");
        feedback$accumulatedWork = tag.getFloat("FeedbackAccumulatedWork");
    }

    // --- ThermalBody --------------------------------------------------------------------------

    @Override
    public Tu getTemperature() {
        return new Tu(feedback$temperature);
    }

    @Override
    public void setTemperature(Tu tu) {
        feedback$temperature = tu.value();
    }

    @Override
    public ThermalMass getThermalMass() {
        return FTuning.FURNACE_MASS;
    }

    @Override
    public Conductance getLeak() {
        return FTuning.FURNACE_LEAK;
    }

    /**
     * Sealed, same as every vanilla vessel (§15) -- fuel goes in because it is sealed, nothing
     * attaches because it is sealed, and it is the wall the Crucible exists to get around.
     */
    @Override
    public boolean hasThermowell() {
        return false;
    }

    // --- HeatSource -----------------------------------------------------------------------------

    @Override
    public Tu getFireTu() {
        return feedback$fuelTicksLeft > 0 ? new Tu(feedback$flameRollTu) : FTuning.AMBIENT_TU;
    }

    // --- VanillaVessel --------------------------------------------------------------------------

    @Override
    public int getFuelTicksLeft() {
        return feedback$fuelTicksLeft;
    }

    @Override
    public void setFuelTicksLeft(int ticks) {
        feedback$fuelTicksLeft = ticks;
    }

    @Override
    public int getFuelDuration() {
        return feedback$fuelDuration;
    }

    @Override
    public void setFuelDuration(int ticks) {
        feedback$fuelDuration = ticks;
    }

    @Override
    public float getFlameRollTu() {
        return feedback$flameRollTu;
    }

    @Override
    public void setFlameRollTu(float tu) {
        feedback$flameRollTu = tu;
    }

    @Override
    public float getAccumulatedWork() {
        return feedback$accumulatedWork;
    }

    @Override
    public void setAccumulatedWork(float work) {
        feedback$accumulatedWork = work;
    }
}
