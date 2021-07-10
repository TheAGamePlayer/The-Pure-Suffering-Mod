package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraftforge.registries.ForgeRegistries;

public final class InvasionType {
	private final ResourceLocation id;
	private final Map<Integer, InvasionSkyRenderer> skyRenderer;
	private final Map<Integer, List<Spawners>> mobSpawnList;
	private final boolean isDayInvasion;
	private final boolean forceNoSleep;
	private final boolean setsToNight;
	private final boolean isRepeating;
	private final boolean onlyDuringNight;
	private final int lightLevel;
	private final int tickDelay;
	private final int rarity;
	private final TranslationTextComponent component;

	public InvasionType(ResourceLocation idIn, Map<Integer, InvasionSkyRenderer> skyRendererIn, Map<Integer, List<Spawners>> mobSpawnListIn, boolean isDayInvasionIn, boolean forceNoSleepIn, boolean setsToNightIn, boolean isRepeatingIn, boolean onlyDuringNightIn, int lightLevelIn, int tickDelayIn, int rarityIn) {
		this.id = idIn;
		this.skyRenderer = skyRendererIn;
		this.mobSpawnList = mobSpawnListIn;
		this.isDayInvasion = isDayInvasionIn;
		this.forceNoSleep = forceNoSleepIn;
		this.setsToNight = setsToNightIn;
		this.isRepeating = isRepeatingIn;
		this.onlyDuringNight = onlyDuringNightIn;
		this.lightLevel = lightLevelIn;
		this.tickDelay = tickDelayIn;
		this.rarity = rarityIn;
		this.component = new TranslationTextComponent("invasion." + idIn.getNamespace() + "." + idIn.getPath());
	}
	
	public InvasionType.Builder deconstruct() {
		Map<Integer, InvasionSkyRenderer.Builder> skyRenderer = new HashMap<>();
		for (Entry<Integer, InvasionSkyRenderer> entry : this.skyRenderer.entrySet())
			skyRenderer.put(entry.getKey(), entry.getValue().deconstruct());
		return new InvasionType.Builder(skyRenderer, this.mobSpawnList, this.isDayInvasion, this.forceNoSleep, this.setsToNight, this.isRepeating, this.onlyDuringNight, this.lightLevel, this.tickDelay, this.rarity);
	}
	
	public ResourceLocation getId() {
		return this.id;
	}
	
	public Map<Integer, InvasionSkyRenderer> getSkyRenderer() {
		return this.skyRenderer;
	}
	
	public Map<Integer, List<Spawners>> getMobSpawnList() {
		return this.mobSpawnList;
	}
	
	public boolean isDayInvasion() {
		return this.isDayInvasion;
	}
	
	public boolean forcesNoSleep() {
		return this.forceNoSleep;
	}
	
	public boolean setsEventsToNight() {
		return this.setsToNight;
	}
	
	public boolean isRepeatable() {
		return this.isRepeating;
	}
	
	public boolean isOnlyDuringNight() {
		return this.onlyDuringNight;
	}
	
	public int getLightLevel() {
		return this.lightLevel;
	}
	
	public int getTickDelay() {
		return this.tickDelay;
	}
	
	public int getRarity() {
		return this.rarity;
	}
	
	public TranslationTextComponent getComponent() {
		return this.component;
	}
	
	public int getMaxSeverity() {
		return this.mobSpawnList.isEmpty() ? 1 : this.mobSpawnList.size();
	}
	
	@Override
	public String toString() {
		return this.getId().toString();
	}
	
	public static class Builder {
		private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
		private Map<Integer, InvasionSkyRenderer.Builder> skyRenderer;
		private Map<Integer, List<Spawners>> mobSpawnList;
		private boolean isDayInvasion = false;
		private boolean forceNoSleep = false;
		private boolean setsToNight = false;
		private boolean isRepeating = true;
		private boolean onlyDuringNight = false;
		private int lightLevel; 
		private int tickDelay = 6;
		private int rarity = 0;
		
		private Builder(Map<Integer, InvasionSkyRenderer.Builder> skyRendererIn, Map<Integer, List<Spawners>> mobSpawnListIn, boolean isDayInvasionIn, boolean forceNoSleepIn, boolean setsToNightIn, boolean isRepeatingIn, boolean onlyDuringNightIn, int lightLevelIn, int tickDelayIn, int rarityIn) {
			this.skyRenderer = skyRendererIn;
			this.mobSpawnList = mobSpawnListIn;
			this.isDayInvasion = isDayInvasionIn;
			this.forceNoSleep = forceNoSleepIn;
			this.setsToNight = setsToNightIn;
			this.isRepeating = isRepeatingIn;
			this.onlyDuringNight = onlyDuringNightIn;
			this.lightLevel = lightLevelIn;
			this.tickDelay = tickDelayIn;
			this.rarity = rarityIn;
		}
		
		private Builder() {};
		
		public static InvasionType.Builder invasionType() {
			return new InvasionType.Builder();
		}
		
		public InvasionType.Builder skyRenderer(Map<Integer, InvasionSkyRenderer.Builder> skyRendererIn) {
			this.skyRenderer = skyRendererIn;
			return this;
		}
		
		public InvasionType.Builder mobSpawnList(Map<Integer, List<Spawners>> mobSpawnListIn) {
			this.mobSpawnList = mobSpawnListIn;
			return this;
		}
		
		public InvasionType.Builder setDayTimeEvent() {
			this.isDayInvasion = true;
			return this;
		}
		
		public InvasionType.Builder setForcesNoSleep() {
			this.forceNoSleep = true;
			return this;
		}
		
		public InvasionType.Builder setToNightEvents() {
			this.setsToNight = true;
			return this;
		}
		
		public InvasionType.Builder withLight(int lightLevelIn) {
			this.lightLevel = lightLevelIn;
			return this;
		}
		
		public InvasionType.Builder setNonRepeatable() {
			this.isRepeating = false;
			return this;
		}
		
		public InvasionType.Builder setOnlyDuringNight() {
			this.onlyDuringNight = true;
			return this;
		}
		
		public InvasionType.Builder tickDelay(int tickDelayIn) {
			this.tickDelay = tickDelayIn;
			return this;
		}
		
		public InvasionType.Builder withRarity(int rarityIn) {
			this.rarity = rarityIn;
			return this;
		}
		
		public InvasionType save(Consumer<InvasionType> consumerIn, String pathIn) {
			InvasionType invasionType = this.build(new ResourceLocation(PureSufferingMod.MODID, pathIn));
			consumerIn.accept(invasionType);
			return invasionType;
		}
		
		public InvasionType build(ResourceLocation idIn) {
			Map<Integer, InvasionSkyRenderer> skyRenderer = new HashMap<>();
			if (this.skyRenderer != null)
				for (Entry<Integer, InvasionSkyRenderer.Builder> entry : this.skyRenderer.entrySet())
					skyRenderer.put(entry.getKey(), entry.getValue().build(idIn));
			return new InvasionType(idIn, skyRenderer, this.mobSpawnList, this.isDayInvasion, this.forceNoSleep, this.setsToNight, this.isRepeating, this.onlyDuringNight, this.lightLevel, this.tickDelay, this.rarity);
		}
		
		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.mobSpawnList != null) {
				JsonArray jsonArray = new JsonArray();
				for (List<Spawners> spawnerList : this.mobSpawnList.values()) {
					JsonArray jsonArray1 = new JsonArray();
					for (Spawners spawners : spawnerList) {
						JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
						jsonObject1.addProperty("Weight", spawners.weight);
						jsonObject1.addProperty("MinCount", spawners.minCount);
						jsonObject1.addProperty("MaxCount", spawners.maxCount);
						jsonArray1.add(jsonObject1);
					}
					jsonArray.add(jsonArray1);
				}
				jsonObject.add("MobSpawnList", jsonArray);
			}
			if (this.skyRenderer != null) {
				JsonArray jsonArray = new JsonArray();
				for (InvasionSkyRenderer.Builder builder : this.skyRenderer.values()) {
					jsonArray.add(builder.serializeToJson());
				}
				jsonObject.add("SkyRenderer", jsonArray);
			}
			jsonObject.addProperty("IsDayInvasion", this.isDayInvasion);
			jsonObject.addProperty("ForceNoSleep", this.forceNoSleep);
			if (this.isDayInvasion)
				jsonObject.addProperty("SetsEventsToNight", this.setsToNight);
			if (this.lightLevel != 0.0F) {
				jsonObject.addProperty("LightLevel", this.lightLevel);
			}
			jsonObject.addProperty("IsRepeatable", this.isRepeating);
			if (!this.isDayInvasion)
				jsonObject.addProperty("OnlyDuringNight", this.onlyDuringNight);
			jsonObject.addProperty("TickDelay", this.tickDelay);
			jsonObject.addProperty("Rarity", this.rarity);
			return jsonObject;
		}
		
		public static InvasionType.Builder fromJson(JsonObject jsonObjectIn) {
			boolean isDayInvasion = jsonObjectIn.get("IsDayInvasion").getAsBoolean();
			boolean forceNoSleep = jsonObjectIn.has("ForceNoSleep") ? jsonObjectIn.get("ForceNoSleep").getAsBoolean() : false;
			boolean setsToNight = jsonObjectIn.has("SetsEventsToNight") ? jsonObjectIn.get("SetsEventsToNight").getAsBoolean() : false;
			boolean isRepeating = jsonObjectIn.get("IsRepeatable").getAsBoolean();
			boolean onlyDuringNight = jsonObjectIn.has("OnlyDuringNight") ? jsonObjectIn.get("OnlyDuringNight").getAsBoolean() : false;
			int lightLevel = jsonObjectIn.has("LightLevel") ? jsonObjectIn.get("LightLevel").getAsInt() : 0;
			int tickDelay = jsonObjectIn.get("TickDelay").getAsInt();
			int rarity = jsonObjectIn.get("Rarity").getAsInt();
			boolean errored = false;
			Map<Integer, List<Spawners>> mobSpawnList = new HashMap<>();
			JsonElement jsonElement = jsonObjectIn.getAsJsonArray("MobSpawnList");
			if (jsonElement != null) {
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					for (int list = 0; list < jsonArray.size(); list++) {
						ArrayList<Spawners> spawnList = new ArrayList<>();
						JsonElement jsonElement1 = jsonArray.get(list);
						if (jsonElement1.isJsonArray()) {
							JsonArray jsonArray1 = jsonElement1.getAsJsonArray();
							for (JsonElement jsonElement2 : jsonArray1) {
								if (jsonElement2.isJsonObject()) {
									JsonObject jsonObject = jsonElement2.getAsJsonObject();
									spawnList.add(new Spawners(ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryParse(jsonObject.get("EntityType").getAsString())), jsonObject.get("Weight").getAsInt(), jsonObject.get("MinCount").getAsInt(), jsonObject.get("MaxCount").getAsInt()));
								} else {
									errored = true;
									break;
								}
							}
						} else {
							errored = true;
							break;
						}
						mobSpawnList.put(list, spawnList);
					}
				} else {
					errored = true;
				}
			}
			Map<Integer, InvasionSkyRenderer.Builder> skyRenderer = new HashMap<>();
			JsonElement jsonElement1 = jsonObjectIn.getAsJsonArray("SkyRenderer");
			if (jsonElement1 != null) {
				if (jsonElement1.isJsonArray()) {
					JsonArray jsonArray = jsonElement1.getAsJsonArray();
					for (int sky = 0; sky < jsonArray.size(); sky++) {
						JsonElement jsonElement2 = jsonArray.get(sky);
						if (jsonElement2.isJsonObject()) {
							skyRenderer.put(sky, InvasionSkyRenderer.Builder.fromJson(jsonElement2.getAsJsonObject()));
						} else {
							errored = true;
							break;
						}
					}
				} else {
					errored = true;
				}
			}
			if (errored) {
				LOGGER.error("JsonElement is incorrectly setup: " + jsonElement.toString() + ". Therefore InvasionType wasn't registered!");
			}
			return new InvasionType.Builder(skyRenderer, mobSpawnList, isDayInvasion, forceNoSleep, setsToNight, isRepeating, onlyDuringNight, lightLevel, tickDelay, rarity);
		}
	}
}
