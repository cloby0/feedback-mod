package io.github.cloby0.feedback.machine.hammer;

import io.github.cloby0.feedback.core.FTuning;
import io.github.cloby0.feedback.machine.linkage.Reciprocating;
import io.github.cloby0.feedback.machine.linkage.StrengthPair;
import io.github.cloby0.feedback.machine.linkage.Throw;
import io.github.cloby0.feedback.process.Deformation;
import io.github.cloby0.feedback.process.DeformationTable;
import io.github.cloby0.feedback.registry.FBlockEntities;
import io.github.cloby0.feedback.registry.FDataComponents;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hits whatever is in front of it, forever.
 *
 * <h2>The whole mod, in one block</h2>
 * The hammer has no recipe list and no notion of completion. It delivers {@code Fu} at whatever
 * {@code St} its linkage provides, and it keeps doing that for as long as it is driven. When the
 * workpiece has taken enough work it becomes the next thing along the same physical axis -- and
 * then the hammer carries straight on working <em>that</em>.
 * <p>
 * Copper ingot to plate is the process. Plate to foil is the same process, not a malfunction:
 * foil is a real material the thermometer needs, so the first overrun a player meets is a
 * sidegrade rather than a punishment. Only past foil is there scrap.
 * <p>
 * Nothing here decides any of that. The chain lives in {@link DeformationTable} as a property of
 * the material, and the hammer never consults it for anything except "how much work does this
 * need and how hard must I hit it".
 */
public class MechanicalHammerBlockEntity extends BlockEntity implements Reciprocating, StrengthPair {

    /** Stages one blow may cascade through. A guard against a table that loops back on itself. */
    private static final int MAX_CASCADE = 8;

    private ItemStack workpiece = ItemStack.EMPTY;

    public MechanicalHammerBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.MECHANICAL_HAMMER.get(), pos, state);
    }

    public ItemStack getWorkpiece() {
        return workpiece;
    }

    public ItemStack removeWorkpiece() {
        ItemStack taken = workpiece;
        workpiece = ItemStack.EMPTY;
        sync();
        return taken;
    }

    public boolean insert(ItemStack stack) {
        if (!workpiece.isEmpty() || stack.isEmpty())
            return false;
        workpiece = stack.split(1);
        sync();
        return true;
    }

    @Override
    public float getStrength(Throw installed) {
        return installed == Throw.SHORT ? FTuning.HAMMER_ST_SHORT : FTuning.HAMMER_ST_LONG;
    }

    @Override
    public float getMaxStrength() {
        return FTuning.HAMMER_MAX_ST;
    }

    @Override
    public float getLoadSu() {
        return FTuning.HAMMER_LOAD_SU;
    }

    @Override
    public void onStroke(float strength) {
        if (level == null || level.isClientSide)
            return;

        playBlow();

        if (workpiece.isEmpty())
            return;

        Optional<Deformation> maybe = DeformationTable.get().find(workpiece);
        if (maybe.isEmpty())
            return; // Nothing this material does under a hammer. Scrap is already scrap.

        Deformation deformation = maybe.get();

        // How much a blow accomplishes is the material's business, not the machine's. Below the
        // hardness threshold nothing lands at all -- philosophy 7's hard gate, a genuine
        // impossibility rather than a slower version of the process.
        int delivered = deformation.workFrom(strength);
        if (delivered <= 0)
            return;

        int worked = workpiece.getOrDefault(FDataComponents.WORK.get(), 0) + delivered;

        // Surplus carries, and carries through a finished stage into the next one. A blow does
        // not politely stop at the finish line, which is the whole point: a hard enough blow on
        // a soft enough material runs straight past what you wanted. Overshoot is the mechanic,
        // not an edge case.
        Deformation stage = deformation;
        for (int guard = 0; guard < MAX_CASCADE && worked >= stage.work(); guard++) {
            worked -= stage.work();
            workpiece = stage.result().copy();

            Optional<Deformation> next = DeformationTable.get().find(workpiece);
            if (next.isEmpty()) {
                worked = 0;   // nothing further to become; the work has nowhere to go
                break;
            }
            stage = next.get();
        }

        if (worked > 0) {
            workpiece.set(FDataComponents.WORK.get(), worked);
            // Required work rides along so the workpiece can describe its own progress wherever
            // it goes, without anything having to look the material up.
            workpiece.set(FDataComponents.WORK_REQUIRED.get(), stage.work());
        } else {
            workpiece.remove(FDataComponents.WORK.get());
            workpiece.remove(FDataComponents.WORK_REQUIRED.get());
        }
        sync();
    }

    private void playBlow() {
        if (level == null)
            return;
        level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.15f, 1.6f);
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * The key is written even when the hammer is empty, and that is load-bearing.
     * <p>
     * NeoForge's {@code onDataPacket} discards an update tag that is completely empty:
     * <pre>if (!compoundtag.isEmpty()) self().loadWithComponents(...)</pre>
     * Omitting the key when there is nothing to save produced exactly that -- an empty tag -- so
     * the packet announcing "the workpiece is gone" was thrown away and the client went on
     * rendering an item that no longer existed.
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Workpiece", workpiece.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        workpiece = ItemStack.parseOptional(registries, tag.getCompound("Workpiece"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
