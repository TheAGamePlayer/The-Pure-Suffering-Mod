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
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public final class InvasionTypeManager extends JsonReloadListener {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final Gson GSON = (new GsonBuilder()).create();
	private HashMap<ResourceLocation, InvasionType> invasionTypeMap = new HashMap<>();

	public InvasionTypeManager() {
		super(GSON, "invasion_types");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		this.invasionTypeMap.clear();
		objectIn.forEach((conditions, invasionType) -> {
			try {
				JsonObject jsonObject = JSONUtils.convertToJsonObject(invasionType, "invasion_type");
				InvasionType invasionType1 = InvasionType.Builder.fromJson(jsonObject).build(conditions);
				if (invasionType1 == null) {
					LOGGER.debug("Skipping loading invasion type {} as it's conditions were not met.", conditions);
					return;
				}
				if (PSConfigValues.common.invasionBlacklist.contains(conditions.toString())) {
					LOGGER.debug("Skipping loading invasion type {} as it is blacklisted.", conditions);
					return;
				}
				this.invasionTypeMap.put(conditions, invasionType1);
			} catch (IllegalArgumentException | JsonParseException jsonParseExceptionIn) {
				LOGGER.error("Parsing error loading custom invasion types {}: {}", conditions, jsonParseExceptionIn.getMessage());	
			}
		});
		LOGGER.info("Loaded {} invasion types", (int)this.invasionTypeMap.size());
	}
	
	@Nullable
	public InvasionType getInvasionType(ResourceLocation idIn) {
		return this.invasionTypeMap.get(idIn);
	}

	public Collection<InvasionType> getAllInvasionTypes() {
		return this.invasionTypeMap.values();
	}
	
	public ArrayList<InvasionType> getInvasionTypesOf(Predicate<InvasionType> predIn) {
		ArrayList<InvasionType> invasionList = new ArrayList<>();
		for (InvasionType invasionType : this.invasionTypeMap.values()) {
			if (predIn.test(invasionType))
				invasionList.add(invasionType);
		}
		return invasionList;
	}
	
	public boolean verifyInvasion(String idIn) {
		for (ResourceLocation id : this.invasionTypeMap.keySet()) {
			if (id.toString().matches(idIn))
				return true;
		}
		return false;
	}
}
