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
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
	private final Component component;
	
	public InvasionType(final ResourceLocation idIn, final int rarityIn, final int tierIn, final InvasionTime invasionTimeIn, final InvasionPriority invasionPriorityIn, final SpawningSystem spawningSystemIn, final TimeModifier timeModifierIn, final TimeChangeability timeChangeabilityIn, final WeatherType weatherTypeIn, final List<SeverityInfo> severityInfoIn, final List<ResourceLocation> dimensionsIn) {
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
		final String text = "invasion." + idIn.getNamespace() + "." + idIn.getPath();
		final Component component = Component.translatable(text);
		this.component = component.getString().equals(text) ? Component.translatable(this.formatDefaultText(idIn)) : component;
	}
	
	private String formatDefaultText(final ResourceLocation idIn) {
		final String str = idIn.getPath().replace('_', ' ');
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
		final ArrayList<SeverityInfo.Builder> severityInfo = new ArrayList<>();
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

	public Component getComponent() {
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
		private final List<MobSpawnSettings.SpawnerData> mobSpawnList;
		private final float mobCapPercentage;
		private final boolean forceNoSleep;
		private final int lightLevel;
		private final int tickDelay;
		private final int clusterSize;

		private SeverityInfo(final InvasionSkyRenderer skyRendererIn, final List<MobSpawnSettings.SpawnerData> mobSpawnListIn, final float mobCapPercentageIn, final boolean forceNoSleepIn, final int lightLevelIn, final int tickDelayIn, final int clusterSizeIn) {
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

		public List<MobSpawnSettings.SpawnerData> getMobSpawnList() {
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
			private List<MobSpawnSettings.SpawnerData> mobSpawnList;
			private float mobCapPercentage = 1.0F;
			private boolean forceNoSleep = false;
			private int lightLevel = -1;
			private int tickDelay = 6;
			private int clusterSize = 1;

			private Builder(final InvasionSkyRenderer.Builder skyRendererIn, final List<MobSpawnSettings.SpawnerData> mobSpawnListIn, final float mobCapPercentageIn, final boolean forceNoSleepIn, final int lightLevelIn, final int tickDelayIn, final int clusterSizeIn) {
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

			public SeverityInfo.Builder skyRenderer(final InvasionSkyRenderer.Builder skyRendererIn) {
				this.skyRenderer = skyRendererIn;
				return this;
			}

			public SeverityInfo.Builder mobSpawnList(final List<MobSpawnSettings.SpawnerData> mobSpawnListIn) {
				this.mobSpawnList = mobSpawnListIn;
				return this;
			}
			
			public SeverityInfo.Builder setMobCapMultiplier(final float mobCapPercentageIn) {
				this.mobCapPercentage = mobCapPercentageIn;
				return this;
			}

			public SeverityInfo.Builder setForcesNoSleep() {
				this.forceNoSleep = true;
				return this;
			}

			public SeverityInfo.Builder withLightLevel(final int lightLevelIn) {
				this.lightLevel = lightLevelIn;
				return this;
			}

			public SeverityInfo.Builder withTickDelay(final int tickDelayIn) {
				this.tickDelay = tickDelayIn;
				return this;
			}
			
			public SeverityInfo.Builder withClusterSize(final int clusterSizeIn) {
				this.clusterSize = clusterSizeIn;
				return this;
			}

			public SeverityInfo build(final ResourceLocation idIn) {
				return new SeverityInfo(this.skyRenderer == null ? null : this.skyRenderer.build(idIn), this.mobSpawnList, this.mobCapPercentage, this.forceNoSleep, this.lightLevel, this.tickDelay, this.clusterSize);
			}

			public JsonObject serializeToJson() {
				final JsonObject jsonObject = new JsonObject();
				if (this.skyRenderer != null) {
					jsonObject.add("SkyRenderer", this.skyRenderer.serializeToJson());
				}
				if (this.mobSpawnList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final MobSpawnSettings.SpawnerData spawnInfo : this.mobSpawnList) {
						JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITY_TYPES.getKey(spawnInfo.type).toString());
						jsonObject1.addProperty("Weight", spawnInfo.getWeight().asInt());
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

			public static InvasionType.SeverityInfo.Builder fromJson(final JsonObject jsonObjectIn) {
				InvasionSkyRenderer.Builder skyRenderer = null;
				boolean errored = false;
				final List<MobSpawnSettings.SpawnerData> mobSpawnList = new ArrayList<>();
				final float mobCapPercentage = Mth.clamp(jsonObjectIn.get("MobCapPercentage").getAsFloat(), 0.0F, 1.0F);
				final boolean forceNoSleep = jsonObjectIn.get("ForceNoSleep").getAsBoolean();
				final int lightLevel = jsonObjectIn.has("LightLevel") ? Mth.clamp(jsonObjectIn.get("LightLevel").getAsInt(), 0, 15) : -1;
				final int tickDelay = jsonObjectIn.get("TickDelay").getAsInt();
				final int clusterSize = jsonObjectIn.get("ClusterSize").getAsInt();
				final JsonElement jsonElement = jsonObjectIn.getAsJsonObject("SkyRenderer");
				if (jsonElement != null) {
					if (jsonElement.isJsonObject()) {
						skyRenderer = InvasionSkyRenderer.Builder.fromJson(jsonElement.getAsJsonObject());
					} else {
						errored = true;
					}
				}
				final JsonElement jsonElement1 = jsonObjectIn.getAsJsonArray("MobSpawnList");
				if (jsonElement1 != null) {
					if (jsonElement1.isJsonArray()) {
						final JsonArray jsonArray = jsonElement1.getAsJsonArray();
						for (final JsonElement jsonElement2 : jsonArray) {
							if (jsonElement2.isJsonObject()) {
								final JsonObject jsonObject = jsonElement2.getAsJsonObject();
								final EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(jsonObject.get("EntityType").getAsString()));
								final int weight = jsonObject.get("Weight").getAsInt();
								final int minCount = jsonObject.get("MinCount").getAsInt();
								final int maxCount = jsonObject.get("MaxCount").getAsInt();
								mobSpawnList.add(new MobSpawnSettings.SpawnerData(type, weight, minCount, maxCount));
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

		private Builder(final int rarityIn, final int tierIn, final InvasionTime invasionTimeIn, final InvasionPriority invasionPriorityIn, final SpawningSystem spawningSystemIn, final TimeModifier timeModifierIn, final TimeChangeability timeChangeabilityIn, final WeatherType weatherTypeIn, final List<SeverityInfo.Builder> severityInfoIn, final List<ResourceLocation> dimensionsIn) {
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

		public InvasionType.Builder withRarity(final int rarityIn) {
			this.rarity = rarityIn;
			return this;
		}
		
		public InvasionType.Builder withTier(final int tierIn) {
			this.tier = tierIn;
			return this;
		}

		public InvasionType.Builder withInvasionTime(final InvasionTime invasionTimeIn) {
			this.invasionTime = invasionTimeIn;
			return this;
		}
		
		public InvasionType.Builder withInvasionPriority(final InvasionPriority invasionPriorityIn) {
			this.invasionPriority = invasionPriorityIn;
			return this;
		}
		
		public InvasionType.Builder withSpawningSystem(final SpawningSystem spawningSystemIn) {
			this.spawningSystem = spawningSystemIn;
			return this;
		}

		public InvasionType.Builder withTimeModifier(final TimeModifier timeModifierIn) {
			this.timeModifier = timeModifierIn;
			return this;
		}

		public InvasionType.Builder withTimeChangeability(final TimeChangeability timeChangeabilityIn) {
			this.timeChangeability = timeChangeabilityIn;
			return this;
		}
		
		public InvasionType.Builder withWeatherType(final WeatherType weatherTypeIn) {
			this.weatherType = weatherTypeIn;
			return this;
		}

		public InvasionType.Builder severityInfo(final List<SeverityInfo.Builder> severityInfoIn) {
			this.severityInfo = severityInfoIn;
			return this;
		}
		
		public InvasionType.Builder dimensions(final List<ResourceLocation> dimensionsIn) {
			this.dimensions = dimensionsIn;
			return this;
		}

		public InvasionType save(final Consumer<InvasionType> consumerIn, final String pathIn) {
			final InvasionType invasionType = this.build(new ResourceLocation(PureSufferingMod.MODID, pathIn));
			consumerIn.accept(invasionType);
			return invasionType;
		}

		public InvasionType build(final ResourceLocation idIn) {
			final ArrayList<SeverityInfo> severityInfo = new ArrayList<>();
			if (this.severityInfo != null)
				for (final SeverityInfo.Builder builder : this.severityInfo)
					severityInfo.add(builder.build(idIn));
			return new InvasionType(idIn, this.rarity, this.tier, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, severityInfo, this.dimensions);
		}

		public JsonObject serializeToJson() {
			final JsonObject jsonObject = new JsonObject();
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
				final JsonArray jsonArray = new JsonArray();
				for (final SeverityInfo.Builder builder : this.severityInfo) {
					jsonArray.add(builder.serializeToJson());
				}
				jsonObject.add("SeverityInfo", jsonArray);
			}
			if (this.dimensions != null) {
				final JsonArray jsonArray = new JsonArray();
				for (final ResourceLocation id : this.dimensions) {
					jsonArray.add(id.toString());
				}
				jsonObject.add("Dimensions", jsonArray);
			}
			return jsonObject;
		}

		public static InvasionType.Builder fromJson(final JsonObject jsonObjectIn) {
			final int rarity = jsonObjectIn.get("Rarity").getAsInt();
			final int tier = jsonObjectIn.get("Tier").getAsInt();
			InvasionTime invasionTime = null;
			for (final InvasionTime time : InvasionTime.values()) {
				if (time.toString().equals(jsonObjectIn.get("InvasionTime").getAsString())) {
					invasionTime = time;
					break;
				}
			}
			InvasionPriority invasionPriority = null;
			for (final InvasionPriority order : InvasionPriority.values()) {
				if (order.toString().equals(jsonObjectIn.get("InvasionPriority").getAsString())) {
					invasionPriority = order;
					break;
				}
			}
			SpawningSystem spawningSystem = null;
			for (final SpawningSystem system : SpawningSystem.values()) {
				if (system.toString().equals(jsonObjectIn.get("SpawningSystem").getAsString())) {
					spawningSystem = system;
					break;
				}
			}
			TimeModifier timeModifier = null;
			for (final TimeModifier modifier : TimeModifier.values()) {
				if (modifier.toString().equals(jsonObjectIn.get("TimeModifier").getAsString()) && ((invasionTime != InvasionTime.DAY && modifier != TimeModifier.DAY_TO_NIGHT) || (invasionTime != InvasionTime.NIGHT && modifier != TimeModifier.NIGHT_TO_DAY) || modifier == TimeModifier.NONE)) {
					timeModifier = modifier;
					break;
				}
			}
			TimeChangeability timeChangeability = null;
			for (final TimeChangeability changeability : TimeChangeability.values()) {
				if (changeability.toString().equals(jsonObjectIn.get("TimeChangeability").getAsString()) && ((invasionTime != InvasionTime.DAY && changeability != TimeChangeability.ONLY_DAY) || (invasionTime != InvasionTime.NIGHT && changeability != TimeChangeability.ONLY_NIGHT) || changeability == TimeChangeability.DEFAULT)) {
					timeChangeability = changeability;
					break;
				}
			}
			WeatherType weatherType = null;
			for (final WeatherType weather : WeatherType.values()) {
				if (weather.toString().equals(jsonObjectIn.get("WeatherType").getAsString())) {
					weatherType = weather;
					break;
				}
			}
			boolean errored = false;
			final List<SeverityInfo.Builder> severityInfo = new ArrayList<>();
			final JsonElement jsonElement = jsonObjectIn.getAsJsonArray("SeverityInfo");
			if (jsonElement != null) {
				if (jsonElement.isJsonArray()) {
					final JsonArray jsonArray = jsonElement.getAsJsonArray();
					for (int info = 0; info < jsonArray.size(); info++) {
						final JsonElement jsonElement1 = jsonArray.get(info);
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
			final List<ResourceLocation> dimensions = new ArrayList<>();
			final JsonElement jsonElement2 = jsonObjectIn.getAsJsonArray("Dimensions");
			if (jsonElement2 != null) {
				if (jsonElement2.isJsonArray()) {
					final JsonArray jsonArray = jsonElement2.getAsJsonArray();
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
