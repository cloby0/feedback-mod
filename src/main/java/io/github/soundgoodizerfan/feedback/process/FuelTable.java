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
package io.github.soundgoodizerfan.feedback.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import io.github.soundgoodizerfan.feedback.Feedback;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

/**
 * Loads every {@link Fuel} from {@code data/<namespace>/fuel/*.json}.
 *
 * <h2>Nothing burns unless a pack says it does</h2>
 * There is deliberately no fallback to vanilla's furnace burn times. A vanilla burn time says how
 * many items something smelts, which is a statement about the furnace and carries no temperature
 * at all -- and a fire whose temperature was guessed would quietly make every hard gate in beat 2
 * negotiable. An unlisted item simply will not light, which is legible and is a one-line fix for
 * anyone who wants it to.
 */
public class FuelTable extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "fuel";

    private static final FuelTable INSTANCE = new FuelTable();

    private List<Fuel> entries = List.of();

    private FuelTable() {
        super(new GsonBuilder().create(), DIRECTORY);
    }

    public static FuelTable get() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> json, ResourceManager resources, ProfilerFiller profiler) {
        List<Fuel> loaded = new ArrayList<>();
        json.forEach((id, element) -> Fuel.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> Feedback.LOGGER.error("Bad fuel {}: {}", id, error))
                .ifPresent(loaded::add));
        entries = List.copyOf(loaded);
        Feedback.LOGGER.info("Loaded {} fuel entries", entries.size());
    }

    public List<Fuel> entries() {
        return entries;
    }

    public Optional<Fuel> find(ItemStack stack) {
        for (Fuel entry : entries)
            if (entry.ingredient().test(stack))
                return Optional.of(entry);
        return Optional.empty();
    }
}
