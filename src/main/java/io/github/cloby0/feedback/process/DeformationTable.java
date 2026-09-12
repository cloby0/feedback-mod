package io.github.cloby0.feedback.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import io.github.cloby0.feedback.Feedback;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

/**
 * Loads every {@link Deformation} from {@code data/<namespace>/deformation/*.json}.
 *
 * <h2>Why this is not a vanilla recipe type</h2>
 * Philosophy 15 wants cross-mod support authored as a table rather than as code, which a datapack
 * gives us. But vanilla's recipe machinery brings a recipe book, a crafting-grid shaped matching
 * API and automatic client sync, none of which apply: there is no grid, nothing is ever "crafted",
 * and only the server ever needs to know.
 * <p>
 * There is a pleasing consequence of staying out of the recipe system. A recipe browser genuinely
 * has nothing to show for the Mechanical Hammer -- which is correct, because the hammer does not
 * know how to make anything. It hits what is in front of it.
 */
public class DeformationTable extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "deformation";

    private static final DeformationTable INSTANCE = new DeformationTable();

    private List<Deformation> entries = List.of();

    private DeformationTable() {
        super(new com.google.gson.GsonBuilder().create(), DIRECTORY);
    }

    public static DeformationTable get() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> json, ResourceManager resources, ProfilerFiller profiler) {
        List<Deformation> loaded = new ArrayList<>();
        json.forEach((id, element) -> Deformation.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> Feedback.LOGGER.error("Bad deformation {}: {}", id, error))
                .ifPresent(loaded::add));
        entries = List.copyOf(loaded);
        Feedback.LOGGER.info("Loaded {} deformation entries", entries.size());
    }

    /** Every entry, in load order. Used to ship the table to the client for display. */
    public List<Deformation> entries() {
        return entries;
    }

    /** What this stack turns into when worked, if anything does. */
    public Optional<Deformation> find(ItemStack stack) {
        for (Deformation entry : entries)
            if (entry.input().test(stack))
                return Optional.of(entry);
        return Optional.empty();
    }
}
