package dev.theagameplayer.puresuffering.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FastMap;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PSConfig {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private static final String CONFIG = PureSufferingMod.MODID + ".config.";
	protected static final CommonConfig COMMON = new CommonConfig();
	protected static final ClientConfig CLIENT = new ClientConfig();
	protected static final FastMap<ResourceLocation, LevelConfig> LEVELS = new FastMap<>();

	public static final class CommonConfig {
		private static final String NOTE_HN_PERFORMANCE = "NOTE: May affect performance at higher numbers!";
		private static final String NOTE_REDUCE_FOR_PERFORMANCE = "NOTE: Reduce for increased performance!";
		private final ModConfigSpec spec;
		//GameRules - Boolean
		public final ModConfigSpec.BooleanValue overrideGameRules;
		public final ModConfigSpec.BooleanValue enableHyperInvasions;
		public final ModConfigSpec.BooleanValue enableNightmareInvasions;
		public final ModConfigSpec.BooleanValue invasionAntiGrief;
		public final ModConfigSpec.BooleanValue consistentInvasions;
		public final ModConfigSpec.BooleanValue tieredInvasions;
		public final ModConfigSpec.BooleanValue hyperAggression;
		public final ModConfigSpec.BooleanValue hyperCharge;
		public final ModConfigSpec.BooleanValue forceInvasionSleeplessness;
		public final ModConfigSpec.BooleanValue useXPMultiplier;
		public final ModConfigSpec.BooleanValue mobsDieAtEndOfInvasions;
		public final ModConfigSpec.BooleanValue weakenedInvasionVexes;
		public final ModConfigSpec.BooleanValue enableInvasionAmbience;
		public final ModConfigSpec.BooleanValue notifyPlayersAboutInvasions;
		//GameRules - Integer
		public final ModConfigSpec.IntValue invasionStartDelay;
		public final ModConfigSpec.IntValue primaryInvasionMobCap;
		public final ModConfigSpec.IntValue secondaryInvasionMobCap;
		public final ModConfigSpec.IntValue mobKillLimit;
		//Invasions
		public final ModConfigSpec.ConfigValue<List<? extends String>> invasionBlacklist;
		public final ModConfigSpec.ConfigValue<List<? extends String>> primaryWhitelist;
		public final ModConfigSpec.ConfigValue<List<? extends String>> overworldLikeDimensions;
		public final ModConfigSpec.ConfigValue<List<? extends String>> netherLikeDimensions;
		public final ModConfigSpec.ConfigValue<List<? extends String>> endLikeDimensions;
		public final ModConfigSpec.ConfigValue<List<? extends String>> hyperAggressionBlacklist;
		public final ModConfigSpec.ConfigValue<List<? extends String>> hyperChargeBlacklist;
		public final ModConfigSpec.ConfigValue<List<? extends String>> modBiomeBoostedBlacklist;
		public final ModConfigSpec.ConfigValue<List<? extends String>> mobBiomeBoostedBlacklist;
		public final ModConfigSpec.DoubleValue naturalSpawnChance;
		public final ModConfigSpec.DoubleValue hyperChargeChance;
		public final ModConfigSpec.IntValue blessingEffectRespawnDuration;
		public final ModConfigSpec.IntValue blessingEffectDimensionChangeDuration;
		//Text
		public final ModConfigSpec.ConfigValue<String> defaultInvasionStartMessage;
		public final ModConfigSpec.ConfigValue<String> hyperInvasionStartMessage;
		public final ModConfigSpec.ConfigValue<String> nightmareInvasionStartMessage;
		public final ModConfigSpec.ConfigValue<String> cancelInvasionStartMessage;

		private CommonConfig() {
			final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
			builder.push("game_rules");
			this.overrideGameRules = builder
					.translation(CONFIG + "override_game_rules")
					.comment("This will make these config options override their game rule values.", "NOTE: Can be used to help modpack creators enforce certain settings.")
					.define("overrideGameRules", false);
			builder.push("game_rules_boolean");
			this.enableHyperInvasions = builder
					.translation(CONFIG + "enable_hyper_invasions")
					.comment("Should hyper invasions be able to occur?")
					.define("enableHyperInvasions", true);
			this.enableNightmareInvasions = builder
					.translation(CONFIG + "enable_nightmare_invasions")
					.comment("Should nightmare invasions be able to occur?", "NOTE: Hyper invasions must be enabled.")
					.define("enableNightmareInvasions", true);
			this.invasionAntiGrief = builder
					.translation(CONFIG + "invasion_anti_grief")
					.comment("Will disable explosions, fire, etc... from entities spawned by invasions.")
					.define("invasionAntiGrief", false);
			this.consistentInvasions = builder
					.translation(CONFIG + "consistent_invasions")
					.comment("Rather than the invasion occuring once in every 'rarity' days, it will instead be set to occur every 'rarity' days.")
					.define("consistentInvasions", false);
			this.tieredInvasions = builder
					.translation(CONFIG + "tiered_invasions")
					.worldRestart()
					.comment("Tiers make certain invasions only able to occur after so many days, turning this off will make the world ignore the day count when selecting invasions.")
					.define("tieredInvasions", true);
			this.hyperAggression = builder
					.translation(CONFIG + "hyper_aggression")
					.comment("Hyper Aggression is what invasion mobs have to target the player from across the world, turning this off will make them use default targeting.")
					.define("hyperAggression", true);
			this.hyperCharge = builder
					.translation(CONFIG + "hyper_charge")
					.comment("Hyper Charge is what the buffed mobs spawned by invasions have, turning this off will disabled these buffed mobs from spawning (This also disables Hyper invasions).")
					.define("hyperCharge", true);
			this.forceInvasionSleeplessness = builder
					.translation(CONFIG + "force_invasion_sleeplessness")
					.comment("This determines whether players will be unable to sleep during all invasions.")
					.define("forceInvasionSleeplessness", false);
			this.useXPMultiplier = builder
					.translation(CONFIG + "use_xp_multiplier")
					.comment("This determines whether invasion mobs should have an xp boost per kill.")
					.define("useXPMultiplier", true);
			this.mobsDieAtEndOfInvasions = builder
					.translation(CONFIG + "mobs_die_at_end_of_invasions")
					.comment("Determines if invasion mobs should die when the invasions are over.", "NOTE: Can be used to reduce server lag.")
					.define("mobsDieAtEndOfInvasions", false);
			this.weakenedInvasionVexes = builder
					.translation(CONFIG + "weakened_invasion_vexes")
					.comment("Determines vexes in invasions have a limited lifespan.")
					.define("weakenedInvasionVexes", true);
			this.enableInvasionAmbience = builder
					.translation(CONFIG + "enable_invasion_ambience")
					.comment("Determines if invasion ambience sounds should occur.")
					.define("enableInvasionAmbience", true);
			this.notifyPlayersAboutInvasions = builder
					.translation(CONFIG + "notify_players_about_invasions")
					.comment("Determines if players be notified when invasions start.")
					.define("notifyPlayersAboutInvasions", true);
			builder.pop();
			builder.push("game_rules_integer");
			this.invasionStartDelay = builder
					.translation(CONFIG + "invasion_start_delay")
					.comment("The amount of days until an invasion can start.")
					.defineInRange("invasionStartDelay", 1, 0, Integer.MAX_VALUE);
			this.primaryInvasionMobCap = builder
					.translation(CONFIG + "primary_invasion_mob_cap")
					.comment("The max amount of mobs that can spawn from Primary Invasions at once.", NOTE_REDUCE_FOR_PERFORMANCE)
					.defineInRange("primaryInvasionMobCap", 100, 0, Integer.MAX_VALUE);
			this.secondaryInvasionMobCap = builder
					.translation(CONFIG + "secondary_invasion_mob_cap")
					.comment("The max amount of mobs that can spawn from Secondary Invasions at once.", NOTE_REDUCE_FOR_PERFORMANCE)
					.defineInRange("secondaryInvasionMobCap", 25, 0, Integer.MAX_VALUE);
			this.mobKillLimit = builder
					.translation(CONFIG + "mob_kill_limit")
					.comment("The mob kill limit for the invasion that would cause it to end after the player kills so many mobs from that invasion.", "NOTE: Setting to 0 will disable the limit, limits specified by a datapack will still apply.")
					.defineInRange("mobKillLimit", 0, 0, Integer.MAX_VALUE);
			builder.pop();
			builder.pop();
			builder.push("invasions");
			this.invasionBlacklist = builder
					.translation(CONFIG + "invasion_blacklist")
					.worldRestart()
					.comment("List of Invasions that can't occur.", "Ex: 'puresuffering:solar_eclipse', 'puresuffering:phantom_zone' (swap '' with quotation marks)")
					.defineList("invasionBlacklist", List.of(), null, string -> {
						return string != "";
					});
			this.primaryWhitelist = builder
					.translation(CONFIG + "primary_whitelist")
					.worldRestart()
					.comment("List of Invasions that can be primary invasions.", "NOTE: The Invasion's Priority cannot be labeled as Secondary Only!", "Ex: 'puresuffering:solar_eclipse', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("primaryWhitelist", List.of(), null, string -> {
						return string != "";
					});
			this.overworldLikeDimensions = builder
					.translation(CONFIG + "overworld_like_dimensions")
					.worldRestart()
					.comment("List of Dimensions that should use Overworld Invasions.", "NOTE: May not work with randomly generated dimensions! (RFTools/Mystcraft)", "Ex: 'twilightforest:twilight_forest', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("overworldLikeDimensions", List.of(), null, string -> {
						return string != "";
					});
			this.netherLikeDimensions = builder
					.translation(CONFIG + "nether_like_dimensions")
					.worldRestart()
					.comment("List of Dimensions that should use Nether Invasions.", "NOTE: May not work with randomly generated dimensions! (RFTools/Mystcraft)", "Ex: 'twilightforest:twilight_forest', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("netherLikeDimensions", List.of(), null, string -> {
						return string != "";
					});
			this.endLikeDimensions = builder
					.translation(CONFIG + "end_like_dimensions")
					.worldRestart()
					.comment("List of Dimensions that should use End Invasions.", "NOTE: May not work with randomly generated dimensions! (RFTools/Mystcraft)", "Ex: 'twilightforest:twilight_forest', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("endLikeDimensions", List.of(), null, string -> {
						return string != "";
					});
			this.hyperAggressionBlacklist = builder
					.translation(CONFIG + "hyper_aggression_blacklist")
					.worldRestart()
					.comment("List of mobs that won't be hyper aggressive towards the player. (If setting is turned on)")
					.defineList("hyperAggressionBlacklist", List.of("minecraft:vex"), null, string -> {
						return string != "";
					});
			this.hyperChargeBlacklist = builder
					.translation(CONFIG + "hyper_charge_blacklist")
					.worldRestart()
					.comment("List of mobs that can't be hyper charged. (If setting is turned on)")
					.defineList("hyperChargeBlacklist", List.of("minecraft:vex"), null, string -> {
						return string != "";
					});
			this.modBiomeBoostedBlacklist = builder
					.translation(CONFIG + "mod_biome_boosted_blacklist")
					.worldRestart()
					.comment("List of mods that won't be allowed to have their mobs spawn in Biome Boosted Invasions.", "Ex: 'twilightforest', 'mutantbeasts'")
					.defineList("modBiomeBoostedBlacklist", List.of(), null, string -> {
						return string != "";
					});
			this.mobBiomeBoostedBlacklist = builder
					.translation(CONFIG + "mob_biome_boosted_blacklist")
					.worldRestart()
					.comment("List of mobs that won't be allowed to spawn in Biome Boosted Invasions.", "Ex: 'minecraft:enderman', 'mutantbeasts:mutant_creeper'")
					.defineList("mobBiomeBoostedBlacklist", List.of(), null, string -> {
						return string != "";
					});
			this.naturalSpawnChance = builder
					.translation(CONFIG + "natural_spawn_chance")
					.worldRestart()
					.comment("The chance of a naturally spawning mob has of spawning during an invasion.", NOTE_HN_PERFORMANCE)
					.defineInRange("naturalSpawnChance", 0.0025D, 0.0D, 1.0D);
			this.hyperChargeChance = builder
					.translation(CONFIG + "hyper_charge_chance")
					.worldRestart()
					.comment("The chance of an invasion mob being hyper charged.")
					.defineInRange("hyperChargeChance", 0.2D, 0.0D, 1.0D);
			this.blessingEffectRespawnDuration = builder
					.translation(CONFIG + "blessing_effect_respawn_duration")
					.worldRestart()
					.comment("How many ticks the Blessing Effect lasts when respawning.")
					.defineInRange("blessingEffectRespawnDuration", 400, 0, Integer.MAX_VALUE);
			this.blessingEffectDimensionChangeDuration = builder
					.translation(CONFIG + "blessing_effect_dimension_change_duration")
					.worldRestart()
					.comment("How many ticks the Blessing Effect lasts when changing dimensions.")
					.defineInRange("blessingEffectDimensionChangeDuration", 200, 0, Integer.MAX_VALUE);
			builder.pop();
			builder.push("text");
			this.defaultInvasionStartMessage = builder
					.translation(CONFIG + "default_invasion_start_message")
					.comment("Start message for default invasions, leave blank to for default message.")
					.define("defaultInvasionStartMessage", "");
			this.hyperInvasionStartMessage = builder
					.translation(CONFIG + "hyper_invasion_start_message")
					.comment("Start message for hyper invasions, leave blank to for default message.")
					.define("hyperInvasionStartMessage", "");
			this.nightmareInvasionStartMessage = builder
					.translation(CONFIG + "nightmare_invasion_start_message")
					.comment("Start message for nightmare invasions, leave blank to for default message.")
					.define("nightmareInvasionStartMessage", "");
			this.cancelInvasionStartMessage = builder
					.translation(CONFIG + "cancel_invasion_start_message")
					.comment("Cancel message for invasions, leave blank to for default message.")
					.define("cancelInvasionStartMessage", "");
			builder.pop();
			this.spec = builder.build();
		}
	}

	public static final class ClientConfig {
		private static final String NOTE_INCOMPATIBLE_SHADERS = "NOTE: Set false with incompatible shaders!";
		private static final String NOTE_DISABLE_SKY_EFFECTS = "NOTE: Set to 'enableSkyEffects' to false to disable.";
		private final ModConfigSpec spec;

		public final ModConfigSpec.BooleanValue useSkyBoxRenderer;
		public final ModConfigSpec.BooleanValue canInvasionsChangeBrightness;
		public final ModConfigSpec.BooleanValue enableInvasionStartEffects;
		public final ModConfigSpec.BooleanValue enableSkyFlickering;
		public final ModConfigSpec.BooleanValue enableSkyEffects;
		public final ModConfigSpec.BooleanValue useFastEffects;
		public final ModConfigSpec.IntValue minVortexParticleLifespan;
		public final ModConfigSpec.IntValue maxVortexParticleLifespan;
		public final ModConfigSpec.IntValue vortexParticleDelay;

		public final ModConfigSpec.BooleanValue useInvasionSoundEffects;

		private ClientConfig() {
			final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
			builder.push("rendering");
			this.useSkyBoxRenderer = builder
					.translation(CONFIG + "use_sky_box_renderer")
					.comment("Can render Invasions with a custom sky box renderer?", NOTE_INCOMPATIBLE_SHADERS)
					.define("useSkyBoxRenderer", true);
			this.canInvasionsChangeBrightness = builder
					.translation(CONFIG + "can_invasions_change_brightness")
					.comment("Can Invasions change the brightness Values?", NOTE_INCOMPATIBLE_SHADERS)
					.define("canInvasionsChangeBrightness", true);
			this.enableInvasionStartEffects = builder
					.translation(CONFIG + "enable_invasion_start_effects")
					.comment("Should Invasion start effects be enabled?", NOTE_INCOMPATIBLE_SHADERS)
					.define("enableInvasionStartEffects", true);
			this.enableSkyFlickering = builder
					.translation(CONFIG + "enable_sky_flickering")
					.comment("Should sky flickering effects be enabled?", NOTE_INCOMPATIBLE_SHADERS)
					.define("enableSkyFlickering", true);
			this.enableSkyEffects = builder
					.translation(CONFIG + "enable_sky_effects")
					.comment("Should Hyper Invasion sky effects be enabled?", NOTE_INCOMPATIBLE_SHADERS, "NOTE: May effect performance in hyper invasions!")
					.define("enableSkyEffects", true);
			this.useFastEffects = builder
					.translation(CONFIG + "use_fast_effects")
					.comment("Should sky effects be rendered in their fast graphics mode?", NOTE_INCOMPATIBLE_SHADERS, "NOTE: This can improve performance, even when fast graphics are on.")
					.define("useFastEffects", false);
			this.minVortexParticleLifespan = builder
					.translation(CONFIG + "min_vortex_particle_lifespan")
					.comment("Minimum lifespan for vortex particles in Hyper Invasions.", NOTE_DISABLE_SKY_EFFECTS)
					.defineInRange("minVortexParticleLifespan", 30, 1, Integer.MAX_VALUE);
			this.maxVortexParticleLifespan = builder
					.translation(CONFIG + "max_vortex_particles_lifespan")
					.comment("Maximum lifespan for vortex particles in Hyper Invasions.", NOTE_DISABLE_SKY_EFFECTS)
					.defineInRange("maxVortexParticleLifespan", 630, 1, Integer.MAX_VALUE);
			this.vortexParticleDelay = builder
					.translation(CONFIG + "vortex_particle_delay")
					.comment("Delay value for spawning vortex particles.", "NOTE: Increasing the delay will result in less particles, increasing performance.", "NOTE: total particle = particles * 1/(value + 1)")
					.defineInRange("vortexParticleDelay", 1, 0, Integer.MAX_VALUE);
			builder.pop();
			builder.push("sound");
			this.useInvasionSoundEffects = builder
					.translation(CONFIG + "use_invasion_sound_effects")
					.comment("Should the sound effects signaling invasion be used?")
					.define("useInvasionSoundEffects", true);
			builder.pop();
			this.spec = builder.build();
		}
	}

	public static final class LevelConfig {
		private static final int[][] DEFAULT_FIXED_RARITY = new int[][] {{12}, {12, 5}, {40}};
		private static final int[][] OVERWORLD_RARITY = new int[][] {{21, 3}, {12, 5}, {50, 30}};
		private static final int[][] NETHER_RARITY = new int[][] {{8}, {12, 5}, {40}};
		private static final int[][] END_RARITY = new int[][] {{8}, {10, 4}, {40}};
		private final ModConfigSpec spec;

		public final ModConfigSpec.IntValue[] invasionSessionTypeRarity;
		public final ModConfigSpec.IntValue[] invasionDifficultyRarity;
		public final ModConfigSpec.BooleanValue[] cancelableInvasions;
		public final ModConfigSpec.IntValue[] cancelInvasionRarity;
		public final ModConfigSpec.IntValue[] maxInvasions;
		public final ModConfigSpec.IntValue[] tierIncreaseDelay;
		
		private LevelConfig(final ResourceLocation pDimension, final LevelStem pLevelStem) {
			final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
			final boolean hasFixedTime = pLevelStem.type().value().hasFixedTime();
			int[][] values = null;
			if (pDimension.equals(Level.NETHER.location())) {
				values = NETHER_RARITY;
			} else if (pDimension.equals(Level.END.location())) {
				values = END_RARITY;
			} else {
				values = hasFixedTime ? DEFAULT_FIXED_RARITY : OVERWORLD_RARITY;
			}
			final int sessionTypeLength = hasFixedTime ? 1 : 2;
			final int difficultyLength = InvasionDifficulty.values().length - 1;
			this.invasionSessionTypeRarity = new ModConfigSpec.IntValue[sessionTypeLength];
			this.invasionDifficultyRarity = new ModConfigSpec.IntValue[difficultyLength];
			this.cancelableInvasions = new ModConfigSpec.BooleanValue[sessionTypeLength];
			this.cancelInvasionRarity = new ModConfigSpec.IntValue[sessionTypeLength];
			this.maxInvasions = new ModConfigSpec.IntValue[sessionTypeLength];
			this.tierIncreaseDelay = new ModConfigSpec.IntValue[sessionTypeLength];
			for (int st = 0; st < sessionTypeLength; ++st) {
				final InvasionSessionType sessionType = hasFixedTime ? InvasionSessionType.FIXED : InvasionSessionType.values()[st];
				this.invasionSessionTypeRarity[st] = builder
						.translation(CONFIG + sessionType + "_invasion_rarity")
						.worldRestart()
						.comment("How often should " + sessionType.getDefaultName() + " Invasions occur.")
						.defineInRange(sessionType + "InvasionRarity", values[0][st], 1, Integer.MAX_VALUE);
				this.cancelableInvasions[st] = builder
						.translation(CONFIG + "cancelable_" + sessionType + "_invasions")
						.worldRestart()
						.comment("Determines if " + sessionType + " invasions can be canceled.")
						.define("cancelable" + sessionType.getDefaultName() + "Invasions", true);
				this.cancelInvasionRarity[st] = builder
						.translation(CONFIG + sessionType + "_cancel_invasion_rarity")
						.worldRestart()
						.comment("How often should " + sessionType + " invasions be canceled.", "NOTE: An invasion is canceled once in every 'value' fixed invasions.", "NOTE: If an invasion is set to be cancel on the same cycle as a hyper/nightmare invasion, it will not be cancled.")
						.defineInRange(sessionType + "CancelInvasionRarity", values[0][st] * 4, 1, Integer.MAX_VALUE);
				this.maxInvasions[st] = builder
						.translation(CONFIG + "max_" + sessionType + "_invasions")
						.worldRestart()
						.comment("Max " + sessionType.getDefaultName() + " Invasions that can occur at once.")
						.defineInRange("max" + sessionType.getDefaultName() + "Invasions", 3, 1, Integer.MAX_VALUE);
				this.tierIncreaseDelay[st] = builder
						.translation(CONFIG + sessionType + "_tier_increase_delay")
						.worldRestart()
						.comment("How many days should pass when the " + sessionType.getDefaultName() + " Invasion Tier increases.")
						.defineInRange(sessionType + "TierIncreaseDelay", values[2][st], 1, Integer.MAX_VALUE);
			}
			for (int d = 0; d < difficultyLength; ++d) {
				final InvasionDifficulty difficulty = InvasionDifficulty.values()[d + 1];
				this.invasionDifficultyRarity[d] = builder
						.translation(CONFIG + difficulty + "_invasion_rarity")
						.worldRestart()
						.comment("How often should " + difficulty.getDefaultName() + " Invasions occur.")
						.defineInRange(difficulty + "InvasionRarity", values[1][d], 1, Integer.MAX_VALUE);
			}
			this.spec = builder.build();
		}
	}

	public static final void initConfig(final ModContainer pModContainer, final boolean pIsClient) {
		final Path configPath = FMLPaths.CONFIGDIR.get();
		final Path psConfigPath = Paths.get(configPath.toAbsolutePath().toString(), PureSufferingMod.MODID);
		try {
			Files.createDirectory(psConfigPath);
		} catch (final FileAlreadyExistsException exception) {
			LOGGER.info("Config directory for " + PureSufferingMod.MODID + " already exists!");
		} catch (final IOException exception) {
			LOGGER.error("Failed to create " + PureSufferingMod.MODID + " config directory!", exception);
		}
		pModContainer.registerConfig(ModConfig.Type.COMMON, COMMON.spec, psConfigPath.resolve(PureSufferingMod.MODID + "-common.toml").toString());
		if (pIsClient) pModContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT.spec, psConfigPath.resolve(PureSufferingMod.MODID + "-client.toml").toString());
	}

	public static final void initLevelConfig(final Entry<ResourceKey<LevelStem>, LevelStem> pLevelStem) {
		if (PureSufferingMod.MC == null) return;
		final ResourceLocation dimLoc = pLevelStem.getKey().location();
		if (LEVELS.containsKey(dimLoc)) return;
		final String levelFileName = dimLoc.toDebugFileName();
		final Path configPath = FMLPaths.CONFIGDIR.get();
		final Path psConfigPath = Paths.get(configPath.toAbsolutePath().toString(), PureSufferingMod.MODID);
		final Path psLevelConfigPath = Paths.get(psConfigPath.toAbsolutePath().toString(), "dimensions");
		try {
			Files.createDirectory(psLevelConfigPath);
		} catch (final FileAlreadyExistsException exception) {
			if (LEVELS.isEmpty()) LOGGER.info("Config directory for puresuffering dimensions already exists!");
		} catch (final IOException exception) {
			LOGGER.error("Failed to create puresuffering dimensions config directory!", exception);
		}
		final LevelConfig config = new LevelConfig(dimLoc, pLevelStem.getValue());
		LEVELS.put(dimLoc, config);
		PureSufferingMod.MC.registerConfig(ModConfig.Type.SERVER, config.spec, psLevelConfigPath.resolve(levelFileName + "-level.toml").toString());
	}
	
	public static final void loading(final ModConfigEvent.Loading pEvent) {
		sync(pEvent.getConfig());
	}
	
	public static final void reloading(final ModConfigEvent.Reloading pEvent) {
		sync(pEvent.getConfig());
	}
	
	private static final void sync(final ModConfig pConfig) {
		switch (pConfig.getType()) {
		case COMMON:
			if (!pConfig.getSpec().equals(PSConfig.COMMON.spec)) return;
			PSConfigValues.syncCommon();
			return;
		case CLIENT:
			if (!pConfig.getSpec().equals(PSConfig.CLIENT.spec)) return;
			PSConfigValues.syncClient();
			return;
		default: return;
		}
	}
}
