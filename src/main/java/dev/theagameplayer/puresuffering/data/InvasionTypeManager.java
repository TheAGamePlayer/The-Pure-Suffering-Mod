package dev.theagameplayer.puresuffering.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public final class InvasionTypeManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private static final Gson GSON = (new GsonBuilder()).create();
	private final HashMap<ResourceLocation, InvasionType> invasionTypeMap = new HashMap<>();
	private final Registry<LevelStem> dimensions;

	public InvasionTypeManager(final Registry<LevelStem> pDimension) {
		super(GSON, "invasion_types");
		this.dimensions = pDimension;
	}

	@Override
	protected final void apply(final Map<ResourceLocation, JsonElement> pObjects, final ResourceManager pResourceManager, final ProfilerFiller pProfiler) {
		this.invasionTypeMap.clear();
		for (final Map.Entry<ResourceLocation, JsonElement> entry : pObjects.entrySet()) {
			try {
				final JsonObject jsonObject = GsonHelper.convertToJsonObject(entry.getValue(), "invasion_type");
				final InvasionType invasionType = InvasionType.Builder.fromJson(this.dimensions, jsonObject).build(entry.getKey());
				if (invasionType == null) {
					LOGGER.debug("Skipping loading invasion type {} as it's conditions were not met.", entry.getKey());
					return;
				} else if (PSConfigValues.common.invasionBlacklist.contains(entry.getKey().toString())) {
					LOGGER.debug("Skipping loading invasion type {} as it is blacklisted.", entry.getKey());
					return;
				}
				if (this.invasionTypeMap.containsKey(entry.getKey())) {
					if (!invasionType.overridesExisting()) continue;
					if (this.invasionTypeMap.get(entry.getKey()) != null && this.invasionTypeMap.get(entry.getKey()).overridesExisting())
						throw new JsonParseException("Cannot have 2 invasion types of the same id override each other: " + entry.getKey());
				}
				this.invasionTypeMap.put(entry.getKey(), invasionType);
			} catch (final IllegalArgumentException | JsonParseException exceptionIn) {
				LOGGER.error("Parsing error loading custom invasion types {}: {}", entry.getKey(), exceptionIn.getMessage());	
			}
		}
		LOGGER.info("Loaded {} invasion types", (int)this.invasionTypeMap.size());
	}

	@Nullable
	public final InvasionType getInvasionType(final ResourceLocation pId) {
		return this.invasionTypeMap.get(pId);
	}

	public final Stream<InvasionType> getAllInvasionTypes() {
		return this.invasionTypeMap.values().stream();
	}

	public final ArrayList<InvasionType> getInvasionTypesOf(final Predicate<InvasionType> pOf) {
		final ArrayList<InvasionType> invasionList = new ArrayList<>();
		for (final InvasionType invasionType : this.invasionTypeMap.values()) {
			if (pOf.test(invasionType))
				invasionList.add(invasionType);
		}
		return invasionList;
	}

	public final boolean verifyInvasion(final String pId) {
		for (final ResourceLocation id : this.invasionTypeMap.keySet()) {
			if (id.toString().equals(pId))
				return true;
		}
		return false;
	}
}
