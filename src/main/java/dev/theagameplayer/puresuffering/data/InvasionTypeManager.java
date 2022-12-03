package dev.theagameplayer.puresuffering.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public final class InvasionTypeManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final Gson GSON = (new GsonBuilder()).create();
	private final HashMap<ResourceLocation, InvasionType> invasionTypeMap = new HashMap<>();

	public InvasionTypeManager() {
		super(GSON, "invasion_types");
	}

	@Override
	protected final void apply(final Map<ResourceLocation, JsonElement> objectIn, final ResourceManager resourceManagerIn, final ProfilerFiller profilerIn) {
		this.invasionTypeMap.clear();
		objectIn.forEach((conditions, invasionType) -> {
			try {
				final JsonObject jsonObject = GsonHelper.convertToJsonObject(invasionType, "invasion_type");
				final InvasionType invasionType1 = InvasionType.Builder.fromJson(jsonObject).build(conditions);
				if (invasionType1 == null) {
					LOGGER.debug("Skipping loading invasion type {} as it's conditions were not met.", conditions);
					return;
				}
				if (PSConfigValues.common.invasionBlacklist.contains(conditions.toString())) {
					LOGGER.debug("Skipping loading invasion type {} as it is blacklisted.", conditions);
					return;
				}
				this.invasionTypeMap.put(conditions, invasionType1);
			} catch (final IllegalArgumentException | JsonParseException jsonParseExceptionIn) {
				LOGGER.error("Parsing error loading custom invasion types {}: {}", conditions, jsonParseExceptionIn.getMessage());	
			}
		});
		LOGGER.info("Loaded {} invasion types", (int)this.invasionTypeMap.size());
	}
	
	@Nullable
	public final InvasionType getInvasionType(final ResourceLocation idIn) {
		return this.invasionTypeMap.get(idIn);
	}

	public final Collection<InvasionType> getAllInvasionTypes() {
		return this.invasionTypeMap.values();
	}
	
	public final ArrayList<InvasionType> getInvasionTypesOf(final Predicate<InvasionType> predIn) {
		final ArrayList<InvasionType> invasionList = new ArrayList<>();
		for (InvasionType invasionType : this.invasionTypeMap.values()) {
			if (predIn.test(invasionType))
				invasionList.add(invasionType);
		}
		return invasionList;
	}
	
	public final boolean verifyInvasion(final String idIn) {
		for (final ResourceLocation id : this.invasionTypeMap.keySet()) {
			if (id.toString().matches(idIn))
				return true;
		}
		return false;
	}
}
