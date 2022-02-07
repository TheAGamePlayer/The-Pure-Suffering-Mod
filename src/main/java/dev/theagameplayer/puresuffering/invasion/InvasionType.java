package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.registries.ForgeRegistries;

public final class InvasionType {
	private final ResourceLocation id;
	private final int rarity;
	private final int tier;
	private final InvasionTime invasionTime;
	private final InvasionPriority invasionPriority;
	private final SpawningSystem spawningSystem;
	private final TimeModifier timeModifier;
	private final TimeChangeability timeChangeability;
	private final WeatherType weatherType;
	private final List<SeverityInfo> severityInfo;
	private final List<ResourceLocation> dimensions;
	private final ITextComponent component;
	
	public InvasionType(ResourceLocation idIn, int rarityIn, int tierIn, InvasionTime invasionTimeIn, InvasionPriority invasionPriorityIn, SpawningSystem spawningSystemIn, TimeModifier timeModifierIn, TimeChangeability timeChangeabilityIn, WeatherType weatherTypeIn, List<SeverityInfo> severityInfoIn, List<ResourceLocation> dimensionsIn) {
		this.id = idIn;
		this.rarity = rarityIn;
		this.tier = tierIn;
		this.invasionTime = invasionTimeIn;
		this.invasionPriority = invasionPriorityIn;
		this.spawningSystem = spawningSystemIn;
		this.timeModifier = timeModifierIn;
		this.timeChangeability = timeChangeabilityIn;
		this.weatherType = weatherTypeIn;
		this.severityInfo = severityInfoIn;
		this.dimensions = dimensionsIn;
		String text = "invasion." + idIn.getNamespace() + "." + idIn.getPath();
		TranslationTextComponent component = new TranslationTextComponent(text);
		this.component = component.getString().equals(text) ? new StringTextComponent(this.formatDefaultText(idIn)) : component;
	}
	
	private String formatDefaultText(ResourceLocation idIn) {
		String str = idIn.getPath().replace('_', ' ');
        if (str == null || str.length() == 0)
            return str;
        final char[] buffer = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (Character.isWhitespace(ch)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer[i] = Character.toTitleCase(ch);
                capitalizeNext = false;
            }
        }
		return new String(buffer);
	}

	public InvasionType.Builder deconstruct() {
		ArrayList<SeverityInfo.Builder> severityInfo = new ArrayList<>();
		for (SeverityInfo info : this.severityInfo)
			severityInfo.add(info.deconstruct());
		return new InvasionType.Builder(this.rarity, this.tier, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, severityInfo, this.dimensions);
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public int getRarity() {
		return this.rarity;
	}
	
	public int getTier() {
		return this.tier;
	}

	public InvasionTime getInvasionTime() {
		return this.invasionTime;
	}
	
	public InvasionPriority getInvasionPriority() {
		return this.invasionPriority;
	}
	
	public SpawningSystem getSpawningSystem() {
		return this.spawningSystem;
	}

	public TimeModifier getTimeModifier() {
		return this.timeModifier;
	}

	public TimeChangeability getTimeChangeability() {
		return this.timeChangeability;
	}
	
	public WeatherType getWeatherType() {
		return this.weatherType;
	}

	public List<SeverityInfo> getSeverityInfo() {
		return this.severityInfo;
	}
	
	public List<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

	public ITextComponent getComponent() {
		return this.component;
	}

	public int getMaxSeverity() {
		return this.severityInfo.isEmpty() ? 1 : this.severityInfo.size();
	}

	@Override
	public String toString() {
		return this.getId().toString();
	}
	
	public static enum InvasionTime {
		BOTH,
		NIGHT,
		DAY
	}
	
	public static enum InvasionPriority {
		BOTH,
		PRIMARY_ONLY,
		SECONDARY_ONLY
	}
	
	public static enum SpawningSystem {
		DEFAULT,
		BIOME_BOOSTED,
		BIOME_MIXED
	}
	
	public static enum TimeModifier {
		NONE,
		DAY_TO_NIGHT,
		NIGHT_TO_DAY
	}
	
	public static enum TimeChangeability {
		DEFAULT,
		ONLY_NIGHT,
		ONLY_DAY
	}
	
	public static enum WeatherType {
		DEFAULT,
		CLEAR,
		RAIN,
		THUNDER
	}

	public static class SeverityInfo {
		private final InvasionSkyRenderer skyRenderer;
		private final List<MobSpawnInfo.Spawners> mobSpawnList;
		private final float mobCapPercentage;
		private final boolean forceNoSleep;
		private final int lightLevel;
		private final int tickDelay;
		private final int clusterSize;

		private SeverityInfo(InvasionSkyRenderer skyRendererIn, List<MobSpawnInfo.Spawners> mobSpawnListIn, float mobCapPercentageIn, boolean forceNoSleepIn, int lightLevelIn, int tickDelayIn, int clusterSizeIn) {
			this.skyRenderer = skyRendererIn;
			this.mobSpawnList = mobSpawnListIn;
			this.mobCapPercentage = mobCapPercentageIn;
			this.forceNoSleep = forceNoSleepIn;
			this.lightLevel = lightLevelIn;
			this.tickDelay = tickDelayIn;
			this.clusterSize = clusterSizeIn;
		}
		
		public SeverityInfo.Builder deconstruct() {
			return new SeverityInfo.Builder(this.skyRenderer == null ? null : this.skyRenderer.deconstruct(), this.mobSpawnList, this.mobCapPercentage, this.forceNoSleep, this.lightLevel, this.tickDelay, this.clusterSize);
		}

		public InvasionSkyRenderer getSkyRenderer() {
			return this.skyRenderer;
		}

		public List<MobSpawnInfo.Spawners> getMobSpawnList() {
			return this.mobSpawnList;
		}
		
		public float getMobCapPercentage() {
			return this.mobCapPercentage;
		}

		public boolean forcesNoSleep() {
			return this.forceNoSleep;
		}

		public int getLightLevel() {
			return this.lightLevel;
		}

		public int getTickDelay() {
			return this.tickDelay;
		}
		
		public int getClusterSize() {
			return this.clusterSize;
		}

		public static class Builder {
			private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
			private InvasionSkyRenderer.Builder skyRenderer = null;
			private List<MobSpawnInfo.Spawners> mobSpawnList;
			private float mobCapPercentage = 1.0F;
			private boolean forceNoSleep = false;
			private int lightLevel = -1;
			private int tickDelay = 6;
			private int clusterSize = 1;

			private Builder(InvasionSkyRenderer.Builder skyRendererIn, List<MobSpawnInfo.Spawners> mobSpawnListIn, float mobCapPercentageIn, boolean forceNoSleepIn, int lightLevelIn, int tickDelayIn, int clusterSizeIn) {
				this.skyRenderer = skyRendererIn;
				this.mobSpawnList = mobSpawnListIn;
				this.mobCapPercentage = mobCapPercentageIn;
				this.forceNoSleep = forceNoSleepIn;
				this.lightLevel = lightLevelIn;
				this.tickDelay = tickDelayIn;
				this.clusterSize = clusterSizeIn;
			}

			private Builder() {};
			
			public static SeverityInfo.Builder severityInfo() {
				return new SeverityInfo.Builder();
			}

			public SeverityInfo.Builder skyRenderer(InvasionSkyRenderer.Builder skyRendererIn) {
				this.skyRenderer = skyRendererIn;
				return this;
			}

			public SeverityInfo.Builder mobSpawnList(List<MobSpawnInfo.Spawners> mobSpawnListIn) {
				this.mobSpawnList = mobSpawnListIn;
				return this;
			}
			
			public SeverityInfo.Builder setMobCapMultiplier(float mobCapPercentageIn) {
				this.mobCapPercentage = mobCapPercentageIn;
				return this;
			}

			public SeverityInfo.Builder setForcesNoSleep() {
				this.forceNoSleep = true;
				return this;
			}

			public SeverityInfo.Builder withLightLevel(int lightLevelIn) {
				this.lightLevel = lightLevelIn;
				return this;
			}

			public SeverityInfo.Builder withTickDelay(int tickDelayIn) {
				this.tickDelay = tickDelayIn;
				return this;
			}
			
			public SeverityInfo.Builder withClusterSize(int clusterSizeIn) {
				this.clusterSize = clusterSizeIn;
				return this;
			}

			public SeverityInfo build(ResourceLocation idIn) {
				return new SeverityInfo(this.skyRenderer == null ? null : this.skyRenderer.build(idIn), this.mobSpawnList, this.mobCapPercentage, this.forceNoSleep, this.lightLevel, this.tickDelay, this.clusterSize);
			}

			public JsonObject serializeToJson() {
				JsonObject jsonObject = new JsonObject();
				if (this.skyRenderer != null) {
					jsonObject.add("SkyRenderer", this.skyRenderer.serializeToJson());
				}
				if (this.mobSpawnList != null) {
					JsonArray jsonArray = new JsonArray();
					for (MobSpawnInfo.Spawners spawnInfo : this.mobSpawnList) {
						JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITIES.getKey(spawnInfo.type).toString());
						jsonObject1.addProperty("Weight", spawnInfo.weight);
						jsonObject1.addProperty("MinCount", spawnInfo.minCount);
						jsonObject1.addProperty("MaxCount", spawnInfo.maxCount);
						jsonArray.add(jsonObject1);
					}
					jsonObject.add("MobSpawnList", jsonArray);
				}
				jsonObject.addProperty("MobCapPercentage", this.mobCapPercentage);
				jsonObject.addProperty("ForceNoSleep", this.forceNoSleep);
				if (this.lightLevel > -1)
					jsonObject.addProperty("LightLevel", this.lightLevel);
				jsonObject.addProperty("TickDelay", this.tickDelay);
				jsonObject.addProperty("ClusterSize", this.clusterSize);
				return jsonObject;
			}

			public static InvasionType.SeverityInfo.Builder fromJson(JsonObject jsonObjectIn) {
				InvasionSkyRenderer.Builder skyRenderer = null;
				List<MobSpawnInfo.Spawners> mobSpawnList = new ArrayList<>();
				float mobCapPercentage = MathHelper.clamp(jsonObjectIn.get("MobCapPercentage").getAsFloat(), 0.0F, 1.0F);
				boolean forceNoSleep = jsonObjectIn.get("ForceNoSleep").getAsBoolean();
				int lightLevel = jsonObjectIn.has("LightLevel") ? MathHelper.clamp(jsonObjectIn.get("LightLevel").getAsInt(), 0, 15) : -1;
				int tickDelay = jsonObjectIn.get("TickDelay").getAsInt();
				int clusterSize = jsonObjectIn.get("ClusterSize").getAsInt();
				boolean errored = false;
				JsonElement jsonElement = jsonObjectIn.getAsJsonObject("SkyRenderer");
				if (jsonElement != null) {
					if (jsonElement.isJsonObject()) {
						skyRenderer = InvasionSkyRenderer.Builder.fromJson(jsonElement.getAsJsonObject());
					} else {
						errored = true;
					}
				}
				JsonElement jsonElement1 = jsonObjectIn.getAsJsonArray("MobSpawnList");
				if (jsonElement1 != null) {
					if (jsonElement1.isJsonArray()) {
						JsonArray jsonArray = jsonElement1.getAsJsonArray();
						for (JsonElement jsonElement2 : jsonArray) {
							if (jsonElement2.isJsonObject()) {
								JsonObject jsonObject = jsonElement2.getAsJsonObject();
								EntityType<?> type = ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryParse(jsonObject.get("EntityType").getAsString()));
								int weight = jsonObject.get("Weight").getAsInt();
								int minCount = jsonObject.get("MinCount").getAsInt();
								int maxCount = jsonObject.get("MaxCount").getAsInt();
								mobSpawnList.add(new MobSpawnInfo.Spawners(type, weight, minCount, maxCount));
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
					LOGGER.error("JsonElement is incorrectly setup: " + jsonObjectIn.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
				}
				return new SeverityInfo.Builder(skyRenderer, mobSpawnList, mobCapPercentage, forceNoSleep, lightLevel, tickDelay, clusterSize);
			}
		}
	}

	public static class Builder {
		private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
		private int rarity = 0;
		private int tier = 0;
		private InvasionTime invasionTime;
		private InvasionPriority invasionPriority = InvasionPriority.BOTH;
		private SpawningSystem spawningSystem = SpawningSystem.DEFAULT;
		private TimeModifier timeModifier = TimeModifier.NONE;
		private TimeChangeability timeChangeability = TimeChangeability.DEFAULT;
		private WeatherType weatherType = WeatherType.DEFAULT;
		private List<SeverityInfo.Builder> severityInfo;
		private List<ResourceLocation> dimensions;

		private Builder(int rarityIn, int tierIn, InvasionTime invasionTimeIn, InvasionPriority invasionPriorityIn, SpawningSystem spawningSystemIn, TimeModifier timeModifierIn, TimeChangeability timeChangeabilityIn, WeatherType weatherTypeIn, List<SeverityInfo.Builder> severityInfoIn, List<ResourceLocation> dimensionsIn) {
			this.rarity = rarityIn;
			this.tier = tierIn;
			this.invasionTime = invasionTimeIn;
			this.invasionPriority = invasionPriorityIn;
			this.spawningSystem = spawningSystemIn;
			this.timeModifier = timeModifierIn;
			this.timeChangeability = timeChangeabilityIn;
			this.weatherType = weatherTypeIn;
			this.severityInfo = severityInfoIn;
			this.dimensions = dimensionsIn;
		}

		private Builder() {};

		public static InvasionType.Builder invasionType() {
			return new InvasionType.Builder();
		}

		public InvasionType.Builder withRarity(int rarityIn) {
			this.rarity = rarityIn;
			return this;
		}
		
		public InvasionType.Builder withTier(int tierIn) {
			this.tier = tierIn;
			return this;
		}

		public InvasionType.Builder withInvasionTime(InvasionTime invasionTimeIn) {
			this.invasionTime = invasionTimeIn;
			return this;
		}
		
		public InvasionType.Builder withInvasionPriority(InvasionPriority invasionPriorityIn) {
			this.invasionPriority = invasionPriorityIn;
			return this;
		}
		
		public InvasionType.Builder withSpawningSystem(SpawningSystem spawningSystemIn) {
			this.spawningSystem = spawningSystemIn;
			return this;
		}

		public InvasionType.Builder withTimeModifier(TimeModifier timeModifierIn) {
			this.timeModifier = timeModifierIn;
			return this;
		}

		public InvasionType.Builder withTimeChangeability(TimeChangeability timeChangeabilityIn) {
			this.timeChangeability = timeChangeabilityIn;
			return this;
		}
		
		public InvasionType.Builder withWeatherType(WeatherType weatherTypeIn) {
			this.weatherType = weatherTypeIn;
			return this;
		}

		public InvasionType.Builder severityInfo(List<SeverityInfo.Builder> severityInfoIn) {
			this.severityInfo = severityInfoIn;
			return this;
		}
		
		public InvasionType.Builder dimensions(List<ResourceLocation> dimensionsIn) {
			this.dimensions = dimensionsIn;
			return this;
		}

		public InvasionType save(Consumer<InvasionType> consumerIn, String pathIn) {
			InvasionType invasionType = this.build(new ResourceLocation(PureSufferingMod.MODID, pathIn));
			consumerIn.accept(invasionType);
			return invasionType;
		}

		public InvasionType build(ResourceLocation idIn) {
			ArrayList<SeverityInfo> severityInfo = new ArrayList<>();
			if (this.severityInfo != null)
				for (SeverityInfo.Builder builder : this.severityInfo)
					severityInfo.add(builder.build(idIn));
			return new InvasionType(idIn, this.rarity, this.tier, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, severityInfo, this.dimensions);
		}

		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("Rarity", this.rarity);
			jsonObject.addProperty("Tier", this.tier);
			jsonObject.addProperty("InvasionTime", this.invasionTime.toString());
			jsonObject.addProperty("InvasionPriority", this.invasionPriority.toString());
			jsonObject.addProperty("SpawningSystem", this.spawningSystem.toString());
			if ((this.invasionTime != InvasionTime.DAY && this.timeModifier != TimeModifier.DAY_TO_NIGHT) || (this.invasionTime != InvasionTime.NIGHT && this.timeModifier != TimeModifier.NIGHT_TO_DAY) || this.timeModifier == TimeModifier.NONE)
				jsonObject.addProperty("TimeModifier", this.timeModifier.toString());
			if ((this.invasionTime != InvasionTime.DAY && this.timeChangeability != TimeChangeability.ONLY_DAY) || (this.invasionTime != InvasionTime.NIGHT && this.timeChangeability != TimeChangeability.ONLY_NIGHT) || this.timeChangeability == TimeChangeability.DEFAULT)
				jsonObject.addProperty("TimeChangeability", this.timeChangeability.toString());
			jsonObject.addProperty("WeatherType", this.weatherType.toString());
			if (this.severityInfo != null) {
				JsonArray jsonArray = new JsonArray();
				for (SeverityInfo.Builder builder : this.severityInfo) {
					jsonArray.add(builder.serializeToJson());
				}
				jsonObject.add("SeverityInfo", jsonArray);
			}
			if (this.dimensions != null) {
				JsonArray jsonArray = new JsonArray();
				for (ResourceLocation id : this.dimensions) {
					jsonArray.add(id.toString());
				}
				jsonObject.add("Dimensions", jsonArray);
			}
			return jsonObject;
		}

		public static InvasionType.Builder fromJson(JsonObject jsonObjectIn) {
			int rarity = jsonObjectIn.get("Rarity").getAsInt();
			int tier = jsonObjectIn.get("Tier").getAsInt();
			InvasionTime invasionTime = null;
			for (InvasionTime time : InvasionTime.values()) {
				if (time.toString().equals(jsonObjectIn.get("InvasionTime").getAsString())) {
					invasionTime = time;
					break;
				}
			}
			InvasionPriority invasionPriority = null;
			for (InvasionPriority order : InvasionPriority.values()) {
				if (order.toString().equals(jsonObjectIn.get("InvasionPriority").getAsString())) {
					invasionPriority = order;
					break;
				}
			}
			SpawningSystem spawningSystem = null;
			for (SpawningSystem system : SpawningSystem.values()) {
				if (system.toString().equals(jsonObjectIn.get("SpawningSystem").getAsString())) {
					spawningSystem = system;
					break;
				}
			}
			TimeModifier timeModifier = null;
			for (TimeModifier modifier : TimeModifier.values()) {
				if (modifier.toString().equals(jsonObjectIn.get("TimeModifier").getAsString()) && ((invasionTime != InvasionTime.DAY && modifier != TimeModifier.DAY_TO_NIGHT) || (invasionTime != InvasionTime.NIGHT && modifier != TimeModifier.NIGHT_TO_DAY) || modifier == TimeModifier.NONE)) {
					timeModifier = modifier;
					break;
				}
			}
			TimeChangeability timeChangeability = null;
			for (TimeChangeability changeability : TimeChangeability.values()) {
				if (changeability.toString().equals(jsonObjectIn.get("TimeChangeability").getAsString()) && ((invasionTime != InvasionTime.DAY && changeability != TimeChangeability.ONLY_DAY) || (invasionTime != InvasionTime.NIGHT && changeability != TimeChangeability.ONLY_NIGHT) || changeability == TimeChangeability.DEFAULT)) {
					timeChangeability = changeability;
					break;
				}
			}
			WeatherType weatherType = null;
			for (WeatherType weather : WeatherType.values()) {
				if (weather.toString().equals(jsonObjectIn.get("WeatherType").getAsString())) {
					weatherType = weather;
					break;
				}
			}
			List<SeverityInfo.Builder> severityInfo = new ArrayList<>();
			boolean errored = false;
			JsonElement jsonElement = jsonObjectIn.getAsJsonArray("SeverityInfo");
			if (jsonElement != null) {
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					for (int info = 0; info < jsonArray.size(); info++) {
						JsonElement jsonElement1 = jsonArray.get(info);
						if (jsonElement1.isJsonObject()) {
							severityInfo.add(SeverityInfo.Builder.fromJson(jsonElement1.getAsJsonObject()));
						} else {
							errored = true;
							break;
						}
					}
				} else {
					errored = true;
				}
			}
			List<ResourceLocation> dimensions = new ArrayList<>();
			JsonElement jsonElement2 = jsonObjectIn.getAsJsonArray("Dimensions");
			if (jsonElement2 != null) {
				if (jsonElement2.isJsonArray()) {
					JsonArray jsonArray = jsonElement2.getAsJsonArray();
					for (int dim = 0; dim < jsonArray.size(); dim++) {
						dimensions.add(ResourceLocation.tryParse(jsonArray.get(dim).getAsString()));
					}
				} else {
					errored = true;
				}
			}
			if (invasionTime == null || invasionPriority == null || spawningSystem == null || timeModifier == null || timeChangeability == null || weatherType == null || errored)
				LOGGER.error("JsonElement is incorrectly setup: " + jsonObjectIn.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
			return new InvasionType.Builder(rarity, tier, invasionTime, invasionPriority, spawningSystem, timeModifier, timeChangeability, weatherType, severityInfo, dimensions);
		}
	}
}
