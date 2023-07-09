package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.LevelStem;
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
	
	private final String formatDefaultText(final ResourceLocation idIn) {
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

	public final InvasionType.Builder deconstruct() {
		final ArrayList<SeverityInfo.Builder> severityInfo = new ArrayList<>();
		for (SeverityInfo info : this.severityInfo)
			severityInfo.add(info.deconstruct());
		return new InvasionType.Builder(this.rarity, this.tier, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, severityInfo, this.dimensions);
	}

	public final ResourceLocation getId() {
		return this.id;
	}

	public final int getRarity() {
		return this.rarity;
	}
	
	public final int getTier() {
		return this.tier;
	}

	public final InvasionTime getInvasionTime() {
		return this.invasionTime;
	}
	
	public final InvasionPriority getInvasionPriority() {
		return this.invasionPriority;
	}
	
	public final SpawningSystem getSpawningSystem() {
		return this.spawningSystem;
	}

	public final TimeModifier getTimeModifier() {
		return this.timeModifier;
	}

	public final TimeChangeability getTimeChangeability() {
		return this.timeChangeability;
	}
	
	public final WeatherType getWeatherType() {
		return this.weatherType;
	}

	public final List<SeverityInfo> getSeverityInfo() {
		return this.severityInfo;
	}
	
	public final List<ResourceLocation> getDimensions() {
		return this.dimensions;
	}

	public final Component getComponent() {
		return this.component;
	}

	public final int getMaxSeverity() {
		return this.severityInfo.isEmpty() ? 1 : this.severityInfo.size();
	}

	@Override
	public final String toString() {
		return this.getId().toString();
	}
	
	public static enum InvasionTime {
		BOTH,
		NIGHT,
		DAY;
	}
	
	public static enum InvasionPriority {
		BOTH,
		PRIMARY_ONLY,
		SECONDARY_ONLY;
	}
	
	public static enum SpawningSystem {
		DEFAULT,
		BIOME_BOOSTED,
		BIOME_MIXED;
	}
	
	public static enum TimeModifier {
		NONE,
		DAY_TO_NIGHT,
		NIGHT_TO_DAY;
	}
	
	public static enum TimeChangeability {
		DEFAULT,
		ONLY_NIGHT,
		ONLY_DAY;
	}
	
	public static enum WeatherType {
		DEFAULT,
		CLEAR,
		RAIN,
		THUNDER;
	}

	public static final class SeverityInfo {
		private final InvasionSkyRenderer skyRenderer;
		private final List<MobSpawnSettings.SpawnerData> mobSpawnList;
		private final List<ClusterEntitySpawnData> clusterEntitiesList;
		private final float mobCapPercentage;
		private final boolean forceNoSleep;
		private final int lightLevel;
		private final int tickDelay;
		private final int clusterSize;

		private SeverityInfo(final InvasionSkyRenderer skyRendererIn, final List<MobSpawnSettings.SpawnerData> mobSpawnListIn, final List<ClusterEntitySpawnData> clusterEntitiesListIn, final float mobCapPercentageIn, final boolean forceNoSleepIn, final int lightLevelIn, final int tickDelayIn, final int clusterSizeIn) {
			this.skyRenderer = skyRendererIn;
			this.mobSpawnList = mobSpawnListIn;
			this.clusterEntitiesList = clusterEntitiesListIn;
			this.mobCapPercentage = mobCapPercentageIn;
			this.forceNoSleep = forceNoSleepIn;
			this.lightLevel = lightLevelIn;
			this.tickDelay = tickDelayIn;
			this.clusterSize = clusterSizeIn;
		}
		
		public final SeverityInfo.Builder deconstruct() {
			return new SeverityInfo.Builder(this.skyRenderer == null ? null : this.skyRenderer.deconstruct(), this.mobSpawnList, this.clusterEntitiesList, this.mobCapPercentage, this.forceNoSleep, this.lightLevel, this.tickDelay, this.clusterSize);
		}

		public final InvasionSkyRenderer getSkyRenderer() {
			return this.skyRenderer;
		}

		public final List<MobSpawnSettings.SpawnerData> getMobSpawnList() {
			return this.mobSpawnList;
		}
		
		public final List<ClusterEntitySpawnData> getClusterEntities() {
			return this.clusterEntitiesList;
		}
		
		public final float getMobCapPercentage() {
			return this.mobCapPercentage;
		}

		public final boolean forcesNoSleep() {
			return this.forceNoSleep;
		}

		public final int getLightLevel() {
			return this.lightLevel;
		}

		public final int getTickDelay() {
			return this.tickDelay;
		}
		
		public final int getClusterSize() {
			return this.clusterSize;
		}

		public static final class Builder {
			private static final Logger LOGGER = PureSufferingMod.LOGGER;
			private InvasionSkyRenderer.Builder skyRenderer = null;
			private List<MobSpawnSettings.SpawnerData> mobSpawnList;
			private List<ClusterEntitySpawnData> clusterEntitiesList;
			private float mobCapPercentage = 1.0F;
			private boolean forceNoSleep = false;
			private int lightLevel = -1;
			private int tickDelay = 6;
			private int clusterSize = 1;

			private Builder(final InvasionSkyRenderer.Builder skyRendererIn, final List<MobSpawnSettings.SpawnerData> mobSpawnListIn, final List<ClusterEntitySpawnData> clusterEntitiesListIn, final float mobCapPercentageIn, final boolean forceNoSleepIn, final int lightLevelIn, final int tickDelayIn, final int clusterSizeIn) {
				this.skyRenderer = skyRendererIn;
				this.mobSpawnList = mobSpawnListIn;
				this.clusterEntitiesList = clusterEntitiesListIn;
				this.mobCapPercentage = mobCapPercentageIn;
				this.forceNoSleep = forceNoSleepIn;
				this.lightLevel = lightLevelIn;
				this.tickDelay = tickDelayIn;
				this.clusterSize = clusterSizeIn;
			}

			private Builder() {};
			
			public static final SeverityInfo.Builder severityInfo() {
				return new SeverityInfo.Builder();
			}

			public final SeverityInfo.Builder skyRenderer(final InvasionSkyRenderer.Builder skyRendererIn) {
				this.skyRenderer = skyRendererIn;
				return this;
			}

			public final SeverityInfo.Builder mobSpawnList(final List<MobSpawnSettings.SpawnerData> mobSpawnListIn) {
				this.mobSpawnList = mobSpawnListIn;
				return this;
			}
			
			public final SeverityInfo.Builder clusterEntitiesList(final List<ClusterEntitySpawnData> clusterEntitiesListIn) {
				this.clusterEntitiesList = clusterEntitiesListIn;
				return this;
			}
			
			public final SeverityInfo.Builder setMobCapMultiplier(final float mobCapPercentageIn) {
				this.mobCapPercentage = mobCapPercentageIn;
				return this;
			}

			public final SeverityInfo.Builder setForcesNoSleep() {
				this.forceNoSleep = true;
				return this;
			}

			public final SeverityInfo.Builder withLightLevel(final int lightLevelIn) {
				this.lightLevel = lightLevelIn;
				return this;
			}

			public final SeverityInfo.Builder withTickDelay(final int tickDelayIn) {
				this.tickDelay = tickDelayIn;
				return this;
			}
			
			public final SeverityInfo.Builder withClusterSize(final int clusterSizeIn) {
				this.clusterSize = clusterSizeIn;
				return this;
			}

			public final SeverityInfo build(final ResourceLocation idIn) {
				return new SeverityInfo(this.skyRenderer == null ? null : this.skyRenderer.build(idIn), this.mobSpawnList, this.clusterEntitiesList, this.mobCapPercentage, this.forceNoSleep, this.lightLevel, this.tickDelay, this.clusterSize);
			}

			public final JsonObject serializeToJson() {
				final JsonObject jsonObject = new JsonObject();
				if (this.skyRenderer != null) {
					jsonObject.add("SkyRenderer", this.skyRenderer.serializeToJson());
				}
				if (this.mobSpawnList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final MobSpawnSettings.SpawnerData spawnInfo : this.mobSpawnList) {
						final JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITY_TYPES.getKey(spawnInfo.type).toString());
						jsonObject1.addProperty("Weight", spawnInfo.getWeight().asInt());
						jsonObject1.addProperty("MinCount", spawnInfo.minCount);
						jsonObject1.addProperty("MaxCount", spawnInfo.maxCount);
						jsonArray.add(jsonObject1);
					}
					jsonObject.add("MobSpawnList", jsonArray);
				}
				if (this.clusterEntitiesList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final ClusterEntitySpawnData spawnInfo : this.clusterEntitiesList) {
						final JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITY_TYPES.getKey(spawnInfo.getEntityType()).toString());
						jsonObject1.addProperty("MinCount", spawnInfo.getMinCount());
						jsonObject1.addProperty("MaxCount", spawnInfo.getMaxCount());
						jsonObject1.addProperty("Chance", spawnInfo.getChance());
						jsonArray.add(jsonObject1);
					}
					jsonObject.add("ClusterEntitiesList", jsonArray);
				}
				jsonObject.addProperty("MobCapPercentage", this.mobCapPercentage);
				jsonObject.addProperty("ForceNoSleep", this.forceNoSleep);
				if (this.lightLevel > -1)
					jsonObject.addProperty("LightLevel", this.lightLevel);
				jsonObject.addProperty("TickDelay", this.tickDelay);
				jsonObject.addProperty("ClusterSize", this.clusterSize);
				return jsonObject;
			}

			public static final InvasionType.SeverityInfo.Builder fromJson(final JsonObject jsonObjectIn) {
				InvasionSkyRenderer.Builder skyRenderer = null;
				boolean errored = false;
				final ArrayList<MobSpawnSettings.SpawnerData> mobSpawnList = new ArrayList<>();
				final ArrayList<ClusterEntitySpawnData> clusterEntitiesList = new ArrayList<>();
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
				final JsonElement jsonElement2 = jsonObjectIn.getAsJsonArray("ClusterEntitiesList");
				if (jsonElement2 != null) {
					if (jsonElement2.isJsonArray()) {
						final JsonArray jsonArray = jsonElement2.getAsJsonArray();
						for (final JsonElement jsonElement3 : jsonArray) {
							if (jsonElement3.isJsonObject()) {
								final JsonObject jsonObject = jsonElement3.getAsJsonObject();
								final EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(jsonObject.get("EntityType").getAsString()));
								final int minCount = jsonObject.get("MinCount").getAsInt();
								final int maxCount = jsonObject.get("MaxCount").getAsInt();
								final int chance = jsonObject.get("Chance").getAsInt();
								clusterEntitiesList.add(new ClusterEntitySpawnData(type, minCount, maxCount, chance));
							} else {
								errored = true;
								break;
							}
						}
					} else {
						errored = true;
					}
				}
				if (errored)
					LOGGER.error("JsonElement is incorrectly setup: " + jsonObjectIn.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
				return new SeverityInfo.Builder(skyRenderer, mobSpawnList, clusterEntitiesList, mobCapPercentage, forceNoSleep, lightLevel, tickDelay, clusterSize);
			}
		}
	}

	public static final class Builder {
		private static final Logger LOGGER = PureSufferingMod.LOGGER;
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

		public static final InvasionType.Builder invasionType() {
			return new InvasionType.Builder();
		}

		public final InvasionType.Builder withRarity(final int rarityIn) {
			this.rarity = rarityIn;
			return this;
		}
		
		public final InvasionType.Builder withTier(final int tierIn) {
			this.tier = tierIn;
			return this;
		}

		public final InvasionType.Builder withInvasionTime(final InvasionTime invasionTimeIn) {
			this.invasionTime = invasionTimeIn;
			return this;
		}
		
		public final InvasionType.Builder withInvasionPriority(final InvasionPriority invasionPriorityIn) {
			this.invasionPriority = invasionPriorityIn;
			return this;
		}
		
		public final InvasionType.Builder withSpawningSystem(final SpawningSystem spawningSystemIn) {
			this.spawningSystem = spawningSystemIn;
			return this;
		}

		public final InvasionType.Builder withTimeModifier(final TimeModifier timeModifierIn) {
			this.timeModifier = timeModifierIn;
			return this;
		}

		public final InvasionType.Builder withTimeChangeability(final TimeChangeability timeChangeabilityIn) {
			this.timeChangeability = timeChangeabilityIn;
			return this;
		}
		
		public final InvasionType.Builder withWeatherType(final WeatherType weatherTypeIn) {
			this.weatherType = weatherTypeIn;
			return this;
		}

		public final InvasionType.Builder severityInfo(final List<SeverityInfo.Builder> severityInfoIn) {
			this.severityInfo = severityInfoIn;
			return this;
		}
		
		public final InvasionType.Builder dimensions(final List<ResourceLocation> dimensionsIn) {
			this.dimensions = dimensionsIn;
			return this;
		}

		public final InvasionType save(final Consumer<InvasionType> consumerIn, final String pathIn) {
			final InvasionType invasionType = this.build(PureSufferingMod.namespace(pathIn));
			consumerIn.accept(invasionType);
			return invasionType;
		}

		public final InvasionType build(final ResourceLocation idIn) {
			final ArrayList<SeverityInfo> severityInfo = new ArrayList<>();
			if (this.severityInfo != null)
				for (final SeverityInfo.Builder builder : this.severityInfo)
					severityInfo.add(builder.build(idIn));
			return new InvasionType(idIn, this.rarity, this.tier, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, severityInfo, this.dimensions);
		}

		public final JsonObject serializeToJson() {
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

		public static final InvasionType.Builder fromJson(final JsonObject jsonObjectIn) {
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
						final ResourceLocation dimId = ResourceLocation.tryParse(jsonArray.get(dim).getAsString());
						dimensions.add(dimId);
						if (dimId == LevelStem.OVERWORLD.location()) {
							for (final String modDim : PSConfigValues.common.overworldLikeDimensions)
								dimensions.add(ResourceLocation.tryParse(modDim));
						} else if (dimId == LevelStem.NETHER.location()) {
							for (final String modDim : PSConfigValues.common.netherLikeDimensions)
								dimensions.add(ResourceLocation.tryParse(modDim));
						} else if (dimId == LevelStem.END.location()) {
							for (final String modDim : PSConfigValues.common.endLikeDimensions)
								dimensions.add(ResourceLocation.tryParse(modDim));
						}
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
