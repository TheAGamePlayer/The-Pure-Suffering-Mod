package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.data.AdditionalEntitySpawnData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData.MobTagData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class InvasionType {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private final ResourceLocation id;
	private final boolean overridesExisting;
	private final String defaultName;
	private final int rarity;
	private final int tier;
	private final boolean stopConversions;
	private final InvasionTime invasionTime;
	private final InvasionPriority invasionPriority;
	private final SpawningSystem spawningSystem;
	private final TimeModifier timeModifier;
	private final TimeChangeability timeChangeability;
	private final WeatherType weatherType;
	private final DayNightCycleRequirement dayNightCycleRequirement;
	private final List<SeverityInfo> severityInfo;
	private final List<ResourceLocation> dimensions;
	private final Component component;

	public InvasionType(final ResourceLocation pId, final boolean pOverridesExisting, final String pDefaultName, final int pRarity, final int pTier, final boolean pStopConversions, final InvasionTime pInvasionTime, final InvasionPriority pInvasionPriority, final SpawningSystem pSpawningSystem, final TimeModifier pTimeModifier, final TimeChangeability pTimeChangeability, final WeatherType pWeatherType, final DayNightCycleRequirement pDayNightCycleRequirement, final List<SeverityInfo> pSeverityInfo, final List<ResourceLocation> pDimensions) {
		this.id = pId;
		this.overridesExisting = pOverridesExisting;
		this.defaultName = pDefaultName;
		this.rarity = pRarity;
		this.tier = pTier;
		this.stopConversions = pStopConversions;
		this.invasionTime = pInvasionTime;
		this.invasionPriority = pInvasionPriority;
		this.spawningSystem = pSpawningSystem;
		this.timeModifier = pTimeModifier;
		this.timeChangeability = pTimeChangeability;
		this.weatherType = pWeatherType;
		this.dayNightCycleRequirement = pDayNightCycleRequirement;
		this.severityInfo = pSeverityInfo;
		this.dimensions = pDimensions;
		final String text = "invasion." + pId.getNamespace() + "." + pId.getPath();
		final Component component = Component.translatable(text);
		this.component = component.getString().equals(text) && pDefaultName != null ? Component.literal(pDefaultName) : component;
	}

	public final InvasionType.Builder deconstruct() {
		final ArrayList<SeverityInfo.Builder> severityInfo = new ArrayList<>();
		for (SeverityInfo info : this.severityInfo)
			severityInfo.add(info.deconstruct());
		return new InvasionType.Builder(this.overridesExisting, this.defaultName, this.rarity, this.tier, this.stopConversions, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, this.dayNightCycleRequirement, severityInfo, this.dimensions);
	}

	public final ResourceLocation getId() {
		return this.id;
	}
	
	public final boolean overridesExisting() {
		return this.overridesExisting;
	}

	public final int getRarity() {
		return this.rarity;
	}

	public final int getTier() {
		return this.tier;
	}

	public final boolean stopsConversions() {
		return this.stopConversions;
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

	public final DayNightCycleRequirement getDayNightCycleRequirement() {
		return this.dayNightCycleRequirement;
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
		return this.severityInfo.size();
	}

	@Override
	public final String toString() {
		return this.id.toString();
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
		THUNDER,
		UNSTABLE;
	}

	public static enum DayNightCycleRequirement {
		NONE(dimType -> true),
		NEEDS_CYCLE(dimType -> !dimType.hasFixedTime()),
		NO_CYCLE(dimType -> dimType.hasFixedTime());
		
		private final Predicate<DimensionType> requirement;

		private DayNightCycleRequirement(final Predicate<DimensionType> requirementIn) {
			this.requirement = requirementIn;
		}
		
		public final boolean meetsRequirement(final LevelStem levelStemIn, final String modDimIn) {
			if (levelStemIn == null || !levelStemIn.type().isBound()) LOGGER.warn("Could not find dimension with id, likely typed it wrong in Config: " + modDimIn);
			return this.requirement.test(levelStemIn.type().value());
		}
	}

	public static final class SeverityInfo {
		private final InvasionSkyRenderInfo skyRenderInfo;
		private final List<InvasionSpawnerData> mobSpawnList;
		private final List<AdditionalEntitySpawnData> additionalEntitiesList;
		private final float mobCapPercentage;
		private final int fixedMobCap;
		private final boolean forceNoSleep;
		private final int tickDelay;
		private final int clusterSize;

		private SeverityInfo(final InvasionSkyRenderInfo pSkyRenderInfo, final List<InvasionSpawnerData> pMobSpawnList, final List<AdditionalEntitySpawnData> pAdditionalEntitiesList, final float pMobCapPercentage, final int pFixedMobCap, final boolean pForceNoSleep, final int pTickDelay, final int pClusterSize) {
			this.skyRenderInfo = pSkyRenderInfo;
			this.mobSpawnList = pMobSpawnList;
			this.additionalEntitiesList = pAdditionalEntitiesList;
			this.mobCapPercentage = pMobCapPercentage;
			this.fixedMobCap = pFixedMobCap;
			this.forceNoSleep = pForceNoSleep;
			this.tickDelay = pTickDelay;
			this.clusterSize = pClusterSize;
		}

		public final SeverityInfo.Builder deconstruct() {
			return new SeverityInfo.Builder(this.skyRenderInfo == null ? null : this.skyRenderInfo.deconstruct(), this.mobSpawnList, this.additionalEntitiesList, this.mobCapPercentage, this.fixedMobCap, this.forceNoSleep, this.tickDelay, this.clusterSize);
		}

		public final InvasionSkyRenderInfo getSkyRenderInfo() {
			return this.skyRenderInfo;
		}

		public final List<InvasionSpawnerData> getMobSpawnList() {
			return this.mobSpawnList;
		}

		public final List<AdditionalEntitySpawnData> getAdditionalEntities() {
			return this.additionalEntitiesList;
		}

		public final float getMobCapPercentage() {
			return this.mobCapPercentage;
		}
		
		public final int getFixedMobCap() {
			return this.fixedMobCap;
		}

		public final boolean forcesNoSleep() {
			return this.forceNoSleep;
		}

		public final int getTickDelay() {
			return this.tickDelay;
		}

		public final int getClusterSize() {
			return this.clusterSize;
		}

		public static final class Builder {
			private InvasionSkyRenderInfo.Builder skyRenderInfo = null;
			private List<InvasionSpawnerData> mobSpawnList;
			private List<AdditionalEntitySpawnData> additionalEntitiesList;
			private float mobCapPercentage = 1.0F;
			private int fixedMobCap = 0;
			private boolean forceNoSleep = false;
			private int tickDelay = -1;
			private int clusterSize = 1;

			private Builder(final InvasionSkyRenderInfo.Builder pSkyRenderInfo, final List<InvasionSpawnerData> pMobSpawnList, final List<AdditionalEntitySpawnData> pAdditionalEntitiesList, final float pMobCapPercentage, final int pFixedMobCap, final boolean pForceNoSleep, final int pTickDelay, final int pClusterSize) {
				this.skyRenderInfo = pSkyRenderInfo;
				this.mobSpawnList = pMobSpawnList;
				this.additionalEntitiesList = pAdditionalEntitiesList;
				this.mobCapPercentage = pMobCapPercentage;
				this.fixedMobCap = pFixedMobCap;
				this.forceNoSleep = pForceNoSleep;
				this.tickDelay = pTickDelay;
				this.clusterSize = pClusterSize;
			}

			private Builder() {};

			public static final SeverityInfo.Builder severityInfo() {
				return new SeverityInfo.Builder();
			}

			public final SeverityInfo.Builder skyRenderInfo(final InvasionSkyRenderInfo.Builder pSkyRenderInfo) {
				this.skyRenderInfo = pSkyRenderInfo;
				return this;
			}

			public final SeverityInfo.Builder mobSpawnList(final InvasionSpawnerData... pMobSpawnList) {
				this.mobSpawnList = List.of(pMobSpawnList);
				return this;
			}

			public final SeverityInfo.Builder additionalEntitiesList(final AdditionalEntitySpawnData... pAdditionalEntitiesList) {
				this.additionalEntitiesList = List.of(pAdditionalEntitiesList);
				return this;
			}

			public final SeverityInfo.Builder setMobCapMultiplier(final float pMobCapPercentage) {
				this.mobCapPercentage = pMobCapPercentage;
				return this;
			}
			
			public final SeverityInfo.Builder setFixedMobCap(final int pFixedMobCap) {
				this.fixedMobCap = pFixedMobCap;
				return this;
			}

			public final SeverityInfo.Builder setForcesNoSleep() {
				this.forceNoSleep = true;
				return this;
			}

			public final SeverityInfo.Builder withTickDelay(final int pTickDelay) {
				this.tickDelay = pTickDelay;
				return this;
			}

			public final SeverityInfo.Builder withClusterSize(final int pClusterSize) {
				this.clusterSize = pClusterSize;
				return this;
			}

			public final SeverityInfo build(final ResourceLocation pId) {
				return new SeverityInfo(this.skyRenderInfo == null ? InvasionSkyRenderInfo.Builder.skyRenderInfo().build(pId) : this.skyRenderInfo.build(pId), this.mobSpawnList, this.additionalEntitiesList, this.mobCapPercentage, this.fixedMobCap, this.forceNoSleep, this.tickDelay, this.clusterSize);
			}

			public final JsonObject serializeToJson() {
				final JsonObject jsonObject = new JsonObject();
				if (this.skyRenderInfo != null)
					jsonObject.add("SkyRenderInfo", this.skyRenderInfo.serializeToJson());
				if (this.mobSpawnList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final InvasionSpawnerData spawnInfo : this.mobSpawnList) {
						final JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", BuiltInRegistries.ENTITY_TYPE.getKey(spawnInfo.type).toString());
						jsonObject1.addProperty("Weight", spawnInfo.getWeight().asInt());
						jsonObject1.addProperty("MinCount", spawnInfo.minCount);
						jsonObject1.addProperty("MaxCount", spawnInfo.maxCount);
						if (spawnInfo.ignoreSpawnRules)
							jsonObject1.addProperty("IgnoreSpawnRules", spawnInfo.ignoreSpawnRules);
						if (spawnInfo.forceDespawn)
							jsonObject1.addProperty("ForceDespawn", spawnInfo.forceDespawn);
						if (spawnInfo.tags.length > 0) {
							final JsonArray jsonArray1 = new JsonArray();
							for (final MobTagData tag : spawnInfo.tags)
								jsonArray1.add(tag.addTagToJson());
							jsonObject1.add("Tags", jsonArray1);
						}
						jsonArray.add(jsonObject1);
					}
					jsonObject.add("MobSpawnList", jsonArray);
				}
				if (this.additionalEntitiesList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final AdditionalEntitySpawnData spawnInfo : this.additionalEntitiesList) {
						final JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", BuiltInRegistries.ENTITY_TYPE.getKey(spawnInfo.getEntityType()).toString());
						jsonObject1.addProperty("MinCount", spawnInfo.getMinCount());
						jsonObject1.addProperty("MaxCount", spawnInfo.getMaxCount());
						jsonObject1.addProperty("Chance", spawnInfo.getChance());
						if (spawnInfo.isSurfaceSpawn())
							jsonObject1.addProperty("IsSurfaceSpawn", true);
						jsonArray.add(jsonObject1);
					}
					jsonObject.add("AdditionalEntitiesList", jsonArray);
				}
				if (this.mobCapPercentage > 0)
					jsonObject.addProperty("MobCapPercentage", this.mobCapPercentage);
				if (this.fixedMobCap > 0)
					jsonObject.addProperty("FixedMobCap", this.fixedMobCap);
				if (this.forceNoSleep)
					jsonObject.addProperty("ForceNoSleep", this.forceNoSleep);
				jsonObject.addProperty("TickDelay", this.tickDelay);
				if (this.clusterSize > 1)
					jsonObject.addProperty("MobClusterSize", this.clusterSize);
				return jsonObject;
			}

			public static final InvasionType.SeverityInfo.Builder fromJson(final JsonObject pJsonObject) {
				boolean errored = false;
				InvasionSkyRenderInfo.Builder skyRenderInfo = null;
				final JsonElement jsonElement = pJsonObject.getAsJsonObject("SkyRenderInfo");
				if (jsonElement != null) {
					if (jsonElement.isJsonObject()) {
						skyRenderInfo = InvasionSkyRenderInfo.Builder.fromJson(jsonElement.getAsJsonObject());
					} else {
						errored = true;
					}
				}
				final ArrayList<InvasionSpawnerData> mobSpawnList = new ArrayList<>();
				final JsonElement mobSpawnListElement = pJsonObject.getAsJsonArray("MobSpawnList");
				if (mobSpawnListElement != null) {
					if (mobSpawnListElement.isJsonArray()) {
						final JsonArray a = mobSpawnListElement.getAsJsonArray();
						for (final JsonElement e : a) {
							if (e.isJsonObject()) {
								final JsonObject o = e.getAsJsonObject();
								final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(o.get("EntityType").getAsString()));
								final int weight = o.get("Weight").getAsInt();
								final int minCount = o.get("MinCount").getAsInt();
								final int maxCount = o.get("MaxCount").getAsInt();
								final boolean ignoreSpawnRules = o.has("IgnoreSpawnRules") && o.get("IgnoreSpawnRules").getAsBoolean();
								final boolean forceDespawn = o.has("ForceDespawn") && o.get("ForceDespawn").getAsBoolean();
								final MobTagData[] tags = new MobTagData[o.has("Tags") ? o.get("Tags").getAsJsonArray().size() : 0];
								if (o.has("Tags")) {
									for (int t = 0; t < tags.length; t++) {
										final JsonElement e1 = o.get("Tags").getAsJsonArray().get(t);
										if (!e1.isJsonObject()) continue;
										tags[t] = MobTagData.addTagData(e1.getAsJsonObject());
									}
								}
								mobSpawnList.add(new InvasionSpawnerData(type, weight, minCount, maxCount, ignoreSpawnRules, forceDespawn, tags));
							} else {
								errored = true;
								break;
							}
						}
					} else {
						errored = true;
					}
				}
				final ArrayList<AdditionalEntitySpawnData> additionalEntitiesList = new ArrayList<>();
				final JsonElement additionalEntitiesListElement = pJsonObject.getAsJsonArray("AdditionalEntitiesList");
				if (additionalEntitiesListElement != null) {
					if (additionalEntitiesListElement.isJsonArray()) {
						final JsonArray a = additionalEntitiesListElement.getAsJsonArray();
						for (final JsonElement e : a) {
							if (e.isJsonObject()) {
								final JsonObject o = e.getAsJsonObject();
								final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(o.get("EntityType").getAsString()));
								final int minCount = o.get("MinCount").getAsInt();
								final int maxCount = o.get("MaxCount").getAsInt();
								final int chance = o.get("Chance").getAsInt();
								final boolean isSurfaceSpawn = o.has("IsSurfaceSpawn") && o.get("IsSurfaceSpawn").getAsBoolean();
								additionalEntitiesList.add(new AdditionalEntitySpawnData(type, minCount, maxCount, chance, isSurfaceSpawn));
							} else {
								errored = true;
								break;
							}
						}
					} else {
						errored = true;
					}
				}
				final float mobCapPercentage = pJsonObject.has("MobCapPercentage") ? Math.max(pJsonObject.get("MobCapPercentage").getAsFloat(), 0.0F) : 0.0F;
				final int fixedMobCap = pJsonObject.has("FixedMobCap") ? Math.max(pJsonObject.get("FixedMobCap").getAsInt(), 0) : 0;
				final boolean forceNoSleep = pJsonObject.has("ForceNoSleep") && pJsonObject.get("ForceNoSleep").getAsBoolean();
				final int tickDelay = pJsonObject.get("TickDelay").getAsInt();
				final int clusterSize = pJsonObject.has("MobClusterSize") ? pJsonObject.get("MobClusterSize").getAsInt() : 1;
				if (errored || tickDelay < 0 || clusterSize < 1)
					LOGGER.error("JsonElement is incorrectly setup: " + pJsonObject.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
				return new SeverityInfo.Builder(skyRenderInfo, mobSpawnList, additionalEntitiesList, mobCapPercentage, fixedMobCap, forceNoSleep, tickDelay, clusterSize);
			}
		}
	}

	public static final class Builder {
		private static final Logger LOGGER = PureSufferingMod.LOGGER;
		private boolean overridesExisting;
		private String defaultName;
		private int rarity;
		private int tier;
		private boolean stopConversions;
		private InvasionTime invasionTime;
		private InvasionPriority invasionPriority;
		private SpawningSystem spawningSystem;
		private TimeModifier timeModifier;
		private TimeChangeability timeChangeability;
		private WeatherType weatherType;
		private DayNightCycleRequirement dayNightCycleRequirement;
		private List<SeverityInfo.Builder> severityInfo;
		private List<ResourceLocation> dimensions;

		private Builder(final boolean pOverridesExisting, final String pDefaultName, final int pRarity, final int pTier, final boolean pStopConversions, final InvasionTime pInvasionTime, final InvasionPriority pInvasionPriority, final SpawningSystem pSpawningSystem, final TimeModifier pTimeModifier, final TimeChangeability pTimeChangeability, final WeatherType pWeatherType, final DayNightCycleRequirement pDayNightCycleRequirement, final List<SeverityInfo.Builder> pSeverityInfo, final List<ResourceLocation> pDimensions) {
			this.overridesExisting = pOverridesExisting;
			this.defaultName = pDefaultName;
			this.rarity = pRarity;
			this.tier = pTier;
			this.stopConversions = pStopConversions;
			this.invasionTime = pInvasionTime;
			this.invasionPriority = pInvasionPriority;
			this.spawningSystem = pSpawningSystem;
			this.timeModifier = pTimeModifier;
			this.timeChangeability = pTimeChangeability;
			this.weatherType = pWeatherType;
			this.dayNightCycleRequirement = pDayNightCycleRequirement;
			this.severityInfo = pSeverityInfo;
			this.dimensions = pDimensions;
		}

		private Builder() {};

		public static final InvasionType.Builder invasionType() {
			return new InvasionType.Builder();
		}

		public final InvasionType.Builder withRarity(final int pRarity) {
			this.rarity = pRarity;
			return this;
		}

		public final InvasionType.Builder withTier(final int pTier) {
			this.tier = pTier;
			return this;
		}

		public final InvasionType.Builder withConversionsStopped() {
			this.stopConversions = true;
			return this;
		}

		public final InvasionType.Builder withInvasionTime(final InvasionTime pInvasionTime) {
			this.invasionTime = pInvasionTime;
			return this;
		}

		public final InvasionType.Builder withInvasionPriority(final InvasionPriority pInvasionPriority) {
			this.invasionPriority = pInvasionPriority;
			return this;
		}

		public final InvasionType.Builder withSpawningSystem(final SpawningSystem pSpawningSystem) {
			this.spawningSystem = pSpawningSystem;
			return this;
		}

		public final InvasionType.Builder withTimeModifier(final TimeModifier pTimeModifier) {
			this.timeModifier = pTimeModifier;
			return this;
		}

		public final InvasionType.Builder withTimeChangeability(final TimeChangeability pTimeChangeability) {
			this.timeChangeability = pTimeChangeability;
			return this;
		}

		public final InvasionType.Builder withWeatherType(final WeatherType pWeatherType) {
			this.weatherType = pWeatherType;
			return this;
		}

		public final InvasionType.Builder withDayNightCycleRequirement(final DayNightCycleRequirement pDayNightCycleRequirement) {
			this.dayNightCycleRequirement = pDayNightCycleRequirement;
			return this;
		}

		public final InvasionType.Builder severityInfo(final SeverityInfo.Builder... pSeverityInfo) {
			this.severityInfo = List.of(pSeverityInfo);
			return this;
		}

		public final InvasionType.Builder dimensions(final ResourceLocation... pDimensions) {
			this.dimensions = List.of(pDimensions);
			return this;
		}

		public final InvasionType save(final Consumer<InvasionType> pConsumer, final String pPath) {
			final InvasionType invasionType = this.build(PureSufferingMod.namespace(pPath));
			pConsumer.accept(invasionType);
			return invasionType;
		}

		public final InvasionType build(final ResourceLocation pId) {
			final ArrayList<SeverityInfo> severityInfo = new ArrayList<>();
			if (this.severityInfo != null)
				for (final SeverityInfo.Builder builder : this.severityInfo)
					severityInfo.add(builder.build(pId));
			return new InvasionType(pId, this.overridesExisting, this.defaultName, this.rarity, this.tier, this.stopConversions, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, this.dayNightCycleRequirement, severityInfo, this.dimensions);
		}

		public final JsonObject serializeToJson() {
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("Rarity", this.rarity + 1);
			jsonObject.addProperty("Tier", this.tier + 1);
			if (this.stopConversions)
				jsonObject.addProperty("StopConversions", this.stopConversions);
			if (this.invasionTime != null)
				jsonObject.addProperty("InvasionTime", this.invasionTime.toString());
			if (this.invasionPriority != null)
				jsonObject.addProperty("InvasionPriority", this.invasionPriority.toString());
			if (this.spawningSystem != null)
				jsonObject.addProperty("SpawningSystem", this.spawningSystem.toString());
			if (this.timeModifier != null && ((this.invasionTime != InvasionTime.NIGHT && this.timeModifier != TimeModifier.NIGHT_TO_DAY) || (this.invasionTime != InvasionTime.DAY && this.timeModifier != TimeModifier.DAY_TO_NIGHT)))
				jsonObject.addProperty("TimeModifier", this.timeModifier.toString());
			if (this.timeChangeability != null && ((this.invasionTime != InvasionTime.NIGHT && this.timeChangeability != TimeChangeability.ONLY_NIGHT) || (this.invasionTime != InvasionTime.DAY && this.timeChangeability != TimeChangeability.ONLY_DAY)))
				jsonObject.addProperty("TimeChangeability", this.timeChangeability.toString());
			if (this.weatherType != null)
				jsonObject.addProperty("WeatherType", this.weatherType.toString());
			if (this.dayNightCycleRequirement != null)
				jsonObject.addProperty("DayNightCycleRequirement", this.dayNightCycleRequirement.toString());
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

		public static final InvasionType.Builder fromJson(final Registry<LevelStem> pDimensions, final JsonObject pJsonObject) {
			final boolean overridesExisting = pJsonObject.has("OverridesExisting") && pJsonObject.get("OverridesExisting").getAsBoolean();
			final String defaultName = pJsonObject.has("DefaultName") ? pJsonObject.get("DefaultName").getAsString() : null;
			final int rarity = Math.max(pJsonObject.get("Rarity").getAsInt(), 1) - 1;
			final int tier = Math.max(pJsonObject.get("Tier").getAsInt(), 1) - 1;
			final boolean stopConversions = pJsonObject.has("StopConversions") && pJsonObject.get("StopConversions").getAsBoolean();
			InvasionTime invasionTime = null;
			if (pJsonObject.has("InvasionTime")) {
				for (final InvasionTime time : InvasionTime.values()) {
					if (time.toString().equals(pJsonObject.get("InvasionTime").getAsString())) {
						invasionTime = time;
						break;
					}
				}
			}
			InvasionPriority invasionPriority = InvasionPriority.BOTH;
			if (pJsonObject.has("InvasionPriority")) {
				for (final InvasionPriority order : InvasionPriority.values()) {
					if (order.toString().equals(pJsonObject.get("InvasionPriority").getAsString())) {
						invasionPriority = order;
						break;
					}
				}
			}
			SpawningSystem spawningSystem = SpawningSystem.DEFAULT;
			if (pJsonObject.has("SpawningSystem")) {
				for (final SpawningSystem system : SpawningSystem.values()) {
					if (system.toString().equals(pJsonObject.get("SpawningSystem").getAsString())) {
						spawningSystem = system;
						break;
					}
				}
			}
			TimeModifier timeModifier = TimeModifier.NONE;
			if (pJsonObject.has("TimeModifier")) {
				for (final TimeModifier modifier : TimeModifier.values()) {
					if (modifier.toString().equals(pJsonObject.get("TimeModifier").getAsString()) && ((invasionTime != InvasionTime.DAY && modifier != TimeModifier.DAY_TO_NIGHT) || (invasionTime != InvasionTime.NIGHT && modifier != TimeModifier.NIGHT_TO_DAY) || modifier == TimeModifier.NONE)) {
						timeModifier = modifier;
						break;
					}
				}
			}
			TimeChangeability timeChangeability = TimeChangeability.DEFAULT;
			if (pJsonObject.has("TimeChangeability")) {
				for (final TimeChangeability changeability : TimeChangeability.values()) {
					if (changeability.toString().equals(pJsonObject.get("TimeChangeability").getAsString()) && ((invasionTime != InvasionTime.DAY && changeability != TimeChangeability.ONLY_DAY) || (invasionTime != InvasionTime.NIGHT && changeability != TimeChangeability.ONLY_NIGHT) || changeability == TimeChangeability.DEFAULT)) {
						timeChangeability = changeability;
						break;
					}
				}
			}
			WeatherType weatherType = WeatherType.DEFAULT;
			if (pJsonObject.has("WeatherType")) {
				for (final WeatherType weather : WeatherType.values()) {
					if (weather.toString().equals(pJsonObject.get("WeatherType").getAsString())) {
						weatherType = weather;
						break;
					}
				}
			}
			DayNightCycleRequirement dayNightCycleRequirement = DayNightCycleRequirement.NONE;
			if (pJsonObject.has("DayNightCycleRequirement")) {
				for (final DayNightCycleRequirement requirement : DayNightCycleRequirement.values()) {
					if (requirement.toString().equals(pJsonObject.get("DayNightCycleRequirement").getAsString())) {
						dayNightCycleRequirement = requirement;
						break;
					}
				}
			}
			boolean errored = false;
			final ArrayList<SeverityInfo.Builder> severityInfo = new ArrayList<>();
			final JsonElement severityInfoElement = pJsonObject.getAsJsonArray("SeverityInfo");
			if (severityInfoElement != null) {
				if (severityInfoElement.isJsonArray()) {
					final JsonArray a = severityInfoElement.getAsJsonArray();
					for (int info = 0; info < a.size(); info++) {
						final JsonElement e = a.get(info);
						if (e.isJsonObject()) {
							severityInfo.add(SeverityInfo.Builder.fromJson(e.getAsJsonObject()));
						} else {
							errored = true;
							break;
						}
					}
				} else {
					errored = true;
				}
			}
			final ArrayList<ResourceLocation> dimensions = new ArrayList<>();
			final JsonElement dimensionsElement = pJsonObject.getAsJsonArray("Dimensions");
			if (dimensionsElement != null) {
				if (dimensionsElement.isJsonArray()) {
					final JsonArray a = dimensionsElement.getAsJsonArray();
					for (int dim = 0; dim < a.size(); dim++) {
						final ResourceLocation dimId = ResourceLocation.tryParse(a.get(dim).getAsString());
						dimensions.add(dimId);
						if (dimId.equals(Level.OVERWORLD.location()) && dayNightCycleRequirement != null) {
							for (final String modDim : PSConfigValues.common.overworldLikeDimensions) {
								final ResourceLocation modDimId = ResourceLocation.tryParse(modDim);
								if (dayNightCycleRequirement.meetsRequirement(pDimensions.get(modDimId), modDim))
									dimensions.add(modDimId);
							}
						} else if (dimId.equals(Level.NETHER.location())) {
							for (final String modDim : PSConfigValues.common.netherLikeDimensions) {
								final ResourceLocation modDimId = ResourceLocation.tryParse(modDim);
								if (dayNightCycleRequirement.meetsRequirement(pDimensions.get(modDimId), modDim))
									dimensions.add(modDimId);
							}
						} else if (dimId.equals(Level.END.location())) {
							for (final String modDim : PSConfigValues.common.endLikeDimensions) {
								final ResourceLocation modDimId = ResourceLocation.tryParse(modDim);
								if (dayNightCycleRequirement.meetsRequirement(pDimensions.get(modDimId), modDim))
									dimensions.add(modDimId);
							}
						}
					}
				} else {
					errored = true;
				}
			}
			if (invasionTime == null || errored)
				LOGGER.error("JsonElement is incorrectly setup: " + pJsonObject.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
			return new InvasionType.Builder(overridesExisting, defaultName, rarity, tier, stopConversions, invasionTime, invasionPriority, spawningSystem, timeModifier, timeChangeability, weatherType, dayNightCycleRequirement, severityInfo, dimensions);
		}
	}
}
