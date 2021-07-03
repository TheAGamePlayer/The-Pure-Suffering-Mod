package dev.theagameplayer.puresuffering.invasion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public final class InvasionTypeManager extends JsonReloadListener {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final Gson GSON = (new GsonBuilder()).create();
	private HashMap<ResourceLocation, InvasionType> allInvasionTypeMap = new HashMap<>();
	private HashMap<ResourceLocation, InvasionType> dayInvasionTypeMap = new HashMap<>();
	private HashMap<ResourceLocation, InvasionType> nightInvasionTypeMap = new HashMap<>();

	public InvasionTypeManager() {
		super(GSON, "invasion_types");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		this.allInvasionTypeMap.clear();
		this.dayInvasionTypeMap.clear();
		this.nightInvasionTypeMap.clear();
		objectIn.forEach((conditions, invasionType) -> {
			try {
				JsonObject jsonObject = JSONUtils.convertToJsonObject(invasionType, "invasion_type");
				InvasionType invasionType1 = InvasionType.Builder.fromJson(jsonObject).build(conditions);
				if (invasionType1 == null) {
					LOGGER.debug("Skipping loading invasion type {} as it's conditions were not met", conditions);
					return;
				}
				this.allInvasionTypeMap.put(conditions, invasionType1);
				if (invasionType1.isDayInvasion()) {
					this.dayInvasionTypeMap.put(conditions, invasionType1);
				} else {
					this.nightInvasionTypeMap.put(conditions, invasionType1);
				}
			} catch (IllegalArgumentException | JsonParseException jsonParseExceptionIn) {
				LOGGER.error("Parsing error loading custom invasion types {}: {}", conditions, jsonParseExceptionIn.getMessage());	
			}
		});
		LOGGER.info("Loaded {} invasion types", (int)this.allInvasionTypeMap.size());
	}
	
	@Nullable
	public InvasionType getInvasionType(ResourceLocation idIn) {
		return this.allInvasionTypeMap.get(idIn);
	}

	public Collection<InvasionType> getAllInvasionTypes() {
		return this.allInvasionTypeMap.values();
	}
	
	public Collection<InvasionType> getDayInvasionTypes() {
		return this.dayInvasionTypeMap.values();
	}
	
	public Collection<InvasionType> getNightInvasionTypes() {
		return this.nightInvasionTypeMap.values();
	}
}
