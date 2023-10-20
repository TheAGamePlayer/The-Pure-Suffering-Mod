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
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraftforge.registries.ForgeRegistries;

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

	public InvasionType(final ResourceLocation idIn, final boolean overridesExistingIn, final String defaultNameIn, final int rarityIn, final int tierIn, final boolean stopConversionsIn, final InvasionTime invasionTimeIn, final InvasionPriority invasionPriorityIn, final SpawningSystem spawningSystemIn, final TimeModifier timeModifierIn, final TimeChangeability timeChangeabilityIn, final WeatherType weatherTypeIn, final DayNightCycleRequirement dayNightCycleRequirementIn, final List<SeverityInfo> severityInfoIn, final List<ResourceLocation> dimensionsIn) {
		this.id = idIn;
		this.overridesExisting = overridesExistingIn;
		this.defaultName = defaultNameIn;
		this.rarity = rarityIn;
		this.tier = tierIn;
		this.stopConversions = stopConversionsIn;
		this.invasionTime = invasionTimeIn;
		this.invasionPriority = invasionPriorityIn;
		this.spawningSystem = spawningSystemIn;
		this.timeModifier = timeModifierIn;
		this.timeChangeability = timeChangeabilityIn;
		this.weatherType = weatherTypeIn;
		this.dayNightCycleRequirement = dayNightCycleRequirementIn;
		this.severityInfo = severityInfoIn;
		this.dimensions = dimensionsIn;
		final String text = "invasion." + idIn.getNamespace() + "." + idIn.getPath();
		final Component component = Component.translatable(text);
		this.component = component.getString().equals(text) && defaultNameIn != null ? Component.literal(defaultNameIn) : component;
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

		private SeverityInfo(final InvasionSkyRenderInfo skyRenderInfoIn, final List<InvasionSpawnerData> mobSpawnListIn, final List<AdditionalEntitySpawnData> additionalEntitiesListIn, final float mobCapPercentageIn, final int fixedMobCapIn, final boolean forceNoSleepIn, final int tickDelayIn, final int clusterSizeIn) {
			this.skyRenderInfo = skyRenderInfoIn;
			this.mobSpawnList = mobSpawnListIn;
			this.additionalEntitiesList = additionalEntitiesListIn;
			this.mobCapPercentage = mobCapPercentageIn;
			this.fixedMobCap = fixedMobCapIn;
			this.forceNoSleep = forceNoSleepIn;
			this.tickDelay = tickDelayIn;
			this.clusterSize = clusterSizeIn;
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

		public final List<AdditionalEntitySpawnData> getClusterEntities() {
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

			private Builder(final InvasionSkyRenderInfo.Builder skyRenderInfoIn, final List<InvasionSpawnerData> mobSpawnListIn, final List<AdditionalEntitySpawnData> additionalEntitiesListIn, final float mobCapPercentageIn, final int fixedMobCapIn, final boolean forceNoSleepIn, final int tickDelayIn, final int clusterSizeIn) {
				this.skyRenderInfo = skyRenderInfoIn;
				this.mobSpawnList = mobSpawnListIn;
				this.additionalEntitiesList = additionalEntitiesListIn;
				this.mobCapPercentage = mobCapPercentageIn;
				this.fixedMobCap = fixedMobCapIn;
				this.forceNoSleep = forceNoSleepIn;
				this.tickDelay = tickDelayIn;
				this.clusterSize = clusterSizeIn;
			}

			private Builder() {};

			public static final SeverityInfo.Builder severityInfo() {
				return new SeverityInfo.Builder();
			}

			public final SeverityInfo.Builder skyRenderInfo(final InvasionSkyRenderInfo.Builder skyRenderInfoIn) {
				this.skyRenderInfo = skyRenderInfoIn;
				return this;
			}

			public final SeverityInfo.Builder mobSpawnList(final InvasionSpawnerData... mobSpawnListIn) {
				this.mobSpawnList = List.of(mobSpawnListIn);
				return this;
			}

			public final SeverityInfo.Builder additionalEntitiesList(final AdditionalEntitySpawnData... additionalEntitiesListIn) {
				this.additionalEntitiesList = List.of(additionalEntitiesListIn);
				return this;
			}

			public final SeverityInfo.Builder setMobCapMultiplier(final float mobCapPercentageIn) {
				this.mobCapPercentage = mobCapPercentageIn;
				return this;
			}
			
			public final SeverityInfo.Builder setFixedMobCap(final int fixedMobCapIn) {
				this.fixedMobCap = fixedMobCapIn;
				return this;
			}

			public final SeverityInfo.Builder setForcesNoSleep() {
				this.forceNoSleep = true;
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
				return new SeverityInfo(this.skyRenderInfo == null ? InvasionSkyRenderInfo.Builder.skyRenderInfo().build(idIn) : this.skyRenderInfo.build(idIn), this.mobSpawnList, this.additionalEntitiesList, this.mobCapPercentage, this.fixedMobCap, this.forceNoSleep, this.tickDelay, this.clusterSize);
			}

			public final JsonObject serializeToJson() {
				final JsonObject jsonObject = new JsonObject();
				if (this.skyRenderInfo != null)
					jsonObject.add("SkyRenderInfo", this.skyRenderInfo.serializeToJson());
				if (this.mobSpawnList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final InvasionSpawnerData spawnInfo : this.mobSpawnList) {
						final JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITY_TYPES.getKey(spawnInfo.type).toString());
						jsonObject1.addProperty("Weight", spawnInfo.getWeight().asInt());
						jsonObject1.addProperty("MinCount", spawnInfo.minCount);
						jsonObject1.addProperty("MaxCount", spawnInfo.maxCount);
						if (spawnInfo.ignoreSpawnRules)
							jsonObject1.addProperty("IgnoreSpawnRules", spawnInfo.ignoreSpawnRules);
						if (spawnInfo.forceDespawn)
							jsonObject1.addProperty("ForceDespawn", spawnInfo.forceDespawn);
						jsonArray.add(jsonObject1);
					}
					jsonObject.add("MobSpawnList", jsonArray);
				}
				if (this.additionalEntitiesList != null) {
					final JsonArray jsonArray = new JsonArray();
					for (final AdditionalEntitySpawnData spawnInfo : this.additionalEntitiesList) {
						final JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITY_TYPES.getKey(spawnInfo.getEntityType()).toString());
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

			public static final InvasionType.SeverityInfo.Builder fromJson(final JsonObject jsonObjectIn) {
				boolean errored = false;
				InvasionSkyRenderInfo.Builder skyRenderInfo = null;
				final JsonElement jsonElement = jsonObjectIn.getAsJsonObject("SkyRenderInfo");
				if (jsonElement != null) {
					if (jsonElement.isJsonObject()) {
						skyRenderInfo = InvasionSkyRenderInfo.Builder.fromJson(jsonElement.getAsJsonObject());
					} else {
						errored = true;
					}
				}
				final ArrayList<InvasionSpawnerData> mobSpawnList = new ArrayList<>();
				final JsonElement mobSpawnListElement = jsonObjectIn.getAsJsonArray("MobSpawnList");
				if (mobSpawnListElement != null) {
					if (mobSpawnListElement.isJsonArray()) {
						final JsonArray a = mobSpawnListElement.getAsJsonArray();
						for (final JsonElement e : a) {
							if (e.isJsonObject()) {
								final JsonObject o = e.getAsJsonObject();
								final EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(o.get("EntityType").getAsString()));
								final int weight = o.get("Weight").getAsInt();
								final int minCount = o.get("MinCount").getAsInt();
								final int maxCount = o.get("MaxCount").getAsInt();
								final boolean ignoreSpawnRules = o.has("IgnoreSpawnRules") && o.get("IgnoreSpawnRules").getAsBoolean();
								final boolean forceDespawn = o.has("ForceDespawn") && o.get("ForceDespawn").getAsBoolean();
								mobSpawnList.add(new InvasionSpawnerData(type, weight, minCount, maxCount, ignoreSpawnRules, forceDespawn));
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
				final JsonElement additionalEntitiesListElement = jsonObjectIn.getAsJsonArray("AdditionalEntitiesList");
				if (additionalEntitiesListElement != null) {
					if (additionalEntitiesListElement.isJsonArray()) {
						final JsonArray a = additionalEntitiesListElement.getAsJsonArray();
						for (final JsonElement e : a) {
							if (e.isJsonObject()) {
								final JsonObject o = e.getAsJsonObject();
								final EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(o.get("EntityType").getAsString()));
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
				final float mobCapPercentage = jsonObjectIn.has("MobCapPercentage") ? Math.max(jsonObjectIn.get("MobCapPercentage").getAsFloat(), 0.0F) : 0.0F;
				final int fixedMobCap = jsonObjectIn.has("FixedMobCap") ? Math.max(jsonObjectIn.get("FixedMobCap").getAsInt(), 0) : 0;
				final boolean forceNoSleep = jsonObjectIn.has("ForceNoSleep") && jsonObjectIn.get("ForceNoSleep").getAsBoolean();
				final int tickDelay = jsonObjectIn.get("TickDelay").getAsInt();
				final int clusterSize = jsonObjectIn.has("MobClusterSize") ? jsonObjectIn.get("MobClusterSize").getAsInt() : 1;
				if (errored || tickDelay < 0 || clusterSize < 1)
					LOGGER.error("JsonElement is incorrectly setup: " + jsonObjectIn.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
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

		private Builder(final boolean overridesExistingIn, final String defaultNameIn, final int rarityIn, final int tierIn, final boolean stopConversionsIn, final InvasionTime invasionTimeIn, final InvasionPriority invasionPriorityIn, final SpawningSystem spawningSystemIn, final TimeModifier timeModifierIn, final TimeChangeability timeChangeabilityIn, final WeatherType weatherTypeIn, final DayNightCycleRequirement dayNightCycleRequirementIn, final List<SeverityInfo.Builder> severityInfoIn, final List<ResourceLocation> dimensionsIn) {
			this.overridesExisting = overridesExistingIn;
			this.defaultName = defaultNameIn;
			this.rarity = rarityIn;
			this.tier = tierIn;
			this.stopConversions = stopConversionsIn;
			this.invasionTime = invasionTimeIn;
			this.invasionPriority = invasionPriorityIn;
			this.spawningSystem = spawningSystemIn;
			this.timeModifier = timeModifierIn;
			this.timeChangeability = timeChangeabilityIn;
			this.weatherType = weatherTypeIn;
			this.dayNightCycleRequirement = dayNightCycleRequirementIn;
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

		public final InvasionType.Builder withConversionsStopped() {
			this.stopConversions = true;
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

		public final InvasionType.Builder withDayNightCycleRequirement(final DayNightCycleRequirement dayNightCycleRequirementIn) {
			this.dayNightCycleRequirement = dayNightCycleRequirementIn;
			return this;
		}

		public final InvasionType.Builder severityInfo(final SeverityInfo.Builder... severityInfoIn) {
			this.severityInfo = List.of(severityInfoIn);
			return this;
		}

		public final InvasionType.Builder dimensions(final ResourceLocation... dimensionsIn) {
			this.dimensions = List.of(dimensionsIn);
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
			return new InvasionType(idIn, this.overridesExisting, this.defaultName, this.rarity, this.tier, this.stopConversions, this.invasionTime, this.invasionPriority, this.spawningSystem, this.timeModifier, this.timeChangeability, this.weatherType, this.dayNightCycleRequirement, severityInfo, this.dimensions);
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

		public static final InvasionType.Builder fromJson(final Registry<LevelStem> dimensionsIn, final JsonObject jsonObjectIn) {
			final boolean overridesExisting = jsonObjectIn.has("OverridesExisting") && jsonObjectIn.get("OverridesExisting").getAsBoolean();
			final String defaultName = jsonObjectIn.has("DefaultName") ? jsonObjectIn.get("DefaultName").getAsString() : null;
			final int rarity = Math.max(jsonObjectIn.get("Rarity").getAsInt(), 1) - 1;
			final int tier = Math.max(jsonObjectIn.get("Tier").getAsInt(), 1) - 1;
			final boolean stopConversions = jsonObjectIn.has("StopConversions") && jsonObjectIn.get("StopConversions").getAsBoolean();
			InvasionTime invasionTime = null;
			if (jsonObjectIn.has("InvasionTime")) {
				for (final InvasionTime time : InvasionTime.values()) {
					if (time.toString().equals(jsonObjectIn.get("InvasionTime").getAsString())) {
						invasionTime = time;
						break;
					}
				}
			}
			InvasionPriority invasionPriority = InvasionPriority.BOTH;
			if (jsonObjectIn.has("InvasionPriority")) {
				for (final InvasionPriority order : InvasionPriority.values()) {
					if (order.toString().equals(jsonObjectIn.get("InvasionPriority").getAsString())) {
						invasionPriority = order;
						break;
					}
				}
			}
			SpawningSystem spawningSystem = SpawningSystem.DEFAULT;
			if (jsonObjectIn.has("SpawningSystem")) {
				for (final SpawningSystem system : SpawningSystem.values()) {
					if (system.toString().equals(jsonObjectIn.get("SpawningSystem").getAsString())) {
						spawningSystem = system;
						break;
					}
				}
			}
			TimeModifier timeModifier = TimeModifier.NONE;
			if (jsonObjectIn.has("TimeModifier")) {
				for (final TimeModifier modifier : TimeModifier.values()) {
					if (modifier.toString().equals(jsonObjectIn.get("TimeModifier").getAsString()) && ((invasionTime != InvasionTime.DAY && modifier != TimeModifier.DAY_TO_NIGHT) || (invasionTime != InvasionTime.NIGHT && modifier != TimeModifier.NIGHT_TO_DAY) || modifier == TimeModifier.NONE)) {
						timeModifier = modifier;
						break;
					}
				}
			}
			TimeChangeability timeChangeability = TimeChangeability.DEFAULT;
			if (jsonObjectIn.has("TimeChangeability")) {
				for (final TimeChangeability changeability : TimeChangeability.values()) {
					if (changeability.toString().equals(jsonObjectIn.get("TimeChangeability").getAsString()) && ((invasionTime != InvasionTime.DAY && changeability != TimeChangeability.ONLY_DAY) || (invasionTime != InvasionTime.NIGHT && changeability != TimeChangeability.ONLY_NIGHT) || changeability == TimeChangeability.DEFAULT)) {
						timeChangeability = changeability;
						break;
					}
				}
			}
			WeatherType weatherType = WeatherType.DEFAULT;
			if (jsonObjectIn.has("WeatherType")) {
				for (final WeatherType weather : WeatherType.values()) {
					if (weather.toString().equals(jsonObjectIn.get("WeatherType").getAsString())) {
						weatherType = weather;
						break;
					}
				}
			}
			DayNightCycleRequirement dayNightCycleRequirement = DayNightCycleRequirement.NONE;
			if (jsonObjectIn.has("DayNightCycleRequirement")) {
				for (final DayNightCycleRequirement requirement : DayNightCycleRequirement.values()) {
					if (requirement.toString().equals(jsonObjectIn.get("DayNightCycleRequirement").getAsString())) {
						dayNightCycleRequirement = requirement;
						break;
					}
				}
			}
			boolean errored = false;
			final ArrayList<SeverityInfo.Builder> severityInfo = new ArrayList<>();
			final JsonElement severityInfoElement = jsonObjectIn.getAsJsonArray("SeverityInfo");
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
			final JsonElement dimensionsElement = jsonObjectIn.getAsJsonArray("Dimensions");
			if (dimensionsElement != null) {
				if (dimensionsElement.isJsonArray()) {
					final JsonArray a = dimensionsElement.getAsJsonArray();
					for (int dim = 0; dim < a.size(); dim++) {
						final ResourceLocation dimId = ResourceLocation.tryParse(a.get(dim).getAsString());
						dimensions.add(dimId);
						if (dimId.equals(Level.OVERWORLD.location()) && dayNightCycleRequirement != null) {
							for (final String modDim : PSConfigValues.common.overworldLikeDimensions) {
								final ResourceLocation modDimId = ResourceLocation.tryParse(modDim);
								if (dayNightCycleRequirement.meetsRequirement(dimensionsIn.get(modDimId), modDim))
									dimensions.add(modDimId);
							}
						} else if (dimId.equals(Level.NETHER.location())) {
							for (final String modDim : PSConfigValues.common.netherLikeDimensions) {
								final ResourceLocation modDimId = ResourceLocation.tryParse(modDim);
								if (dayNightCycleRequirement.meetsRequirement(dimensionsIn.get(modDimId), modDim))
									dimensions.add(modDimId);
							}
						} else if (dimId.equals(Level.END.location())) {
							for (final String modDim : PSConfigValues.common.endLikeDimensions) {
								final ResourceLocation modDimId = ResourceLocation.tryParse(modDim);
								if (dayNightCycleRequirement.meetsRequirement(dimensionsIn.get(modDimId), modDim))
									dimensions.add(modDimId);
							}
						}
					}
				} else {
					errored = true;
				}
			}
			if (invasionTime == null || errored)
				LOGGER.error("JsonElement is incorrectly setup: " + jsonObjectIn.toString() + ". Therefore InvasionType wasn't registered! Most likely a datapack error?");
			return new InvasionType.Builder(overridesExisting, defaultName, rarity, tier, stopConversions, invasionTime, invasionPriority, spawningSystem, timeModifier, timeChangeability, weatherType, dayNightCycleRequirement, severityInfo, dimensions);
		}
	}
}
