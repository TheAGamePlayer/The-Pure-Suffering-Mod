package dev.theagameplayer.puresuffering.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.ImmutableList;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.loading.FMLPaths;

public final class PSConfig {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private static final String CONFIG = PureSufferingMod.MODID + ".config.";
	
	public static final class CommonConfig {
		public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		public static final CommonConfig COMMON = new CommonConfig();
		//Invasion
		public final ForgeConfigSpec.IntValue primaryInvasionMobCap;
		public final ForgeConfigSpec.IntValue secondaryInvasionMobCap;
		public final ForgeConfigSpec.IntValue dayDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue nightDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue fixedDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue maxDayInvasions;
		public final ForgeConfigSpec.IntValue maxNightInvasions;
		public final ForgeConfigSpec.IntValue maxFixedInvasions;
		public final ForgeConfigSpec.BooleanValue multiThreadedInvasions;
		public final ForgeConfigSpec.BooleanValue consistentInvasions;
		public final ForgeConfigSpec.BooleanValue tieredInvasions;
		public final ConfigValue<List<? extends String>> invasionBlacklist;
		public final ConfigValue<List<? extends String>> primaryWhitelist;
		public final ConfigValue<List<? extends String>> overworldLikeDimensions;
		public final ConfigValue<List<? extends String>> netherLikeDimensions;
		public final ConfigValue<List<? extends String>> endLikeDimensions;
		//Balancing
		public final ForgeConfigSpec.IntValue dayInvasionRarity;
		public final ForgeConfigSpec.IntValue nightInvasionRarity;
		public final ForgeConfigSpec.IntValue fixedInvasionRarity;
		public final ForgeConfigSpec.IntValue hyperInvasionRarity;
		public final ForgeConfigSpec.IntValue nightmareInvasionRarity;
		public final ForgeConfigSpec.BooleanValue canDayInvasionsBeCanceled;
		public final ForgeConfigSpec.BooleanValue canNightInvasionsBeCanceled;
		public final ForgeConfigSpec.BooleanValue canFixedInvasionsBeCanceled;
		public final ForgeConfigSpec.DoubleValue dayCancelChanceMultiplier;
		public final ForgeConfigSpec.DoubleValue nightCancelChanceMultiplier;
		public final ForgeConfigSpec.DoubleValue fixedCancelChanceMultiplier;
		public final ForgeConfigSpec.IntValue maxHyperCharge;
		//Modifications
		public final ForgeConfigSpec.BooleanValue hyperAggression;
		public final ForgeConfigSpec.BooleanValue hyperCharge;
		public final ForgeConfigSpec.BooleanValue hyperInvasions;
		public final ForgeConfigSpec.BooleanValue nightmareInvasions;
		public final ConfigValue<List<? extends String>> hyperAggressionBlacklist;
		public final ConfigValue<List<? extends String>> hyperChargeBlacklist;
		public final ConfigValue<List<? extends String>> modBiomeBoostedBlacklist;
		public final ConfigValue<List<? extends String>> mobBiomeBoostedBlacklist;
		public final ForgeConfigSpec.BooleanValue forceInvasionSleeplessness;
		public final ForgeConfigSpec.BooleanValue weakenedVexes;
		public final ForgeConfigSpec.BooleanValue useXPMultiplier;
		public final ForgeConfigSpec.BooleanValue invasionAntiGrief;
		public final ForgeConfigSpec.BooleanValue shouldMobsDieAtEndOfInvasions;
		public final ForgeConfigSpec.BooleanValue shouldMobsSpawnWithMaxRange;
		public final ForgeConfigSpec.IntValue naturalSpawnChance;
		public final ForgeConfigSpec.IntValue hyperChargeChance;
		public final ForgeConfigSpec.IntValue blessingEffectRespawnDuration;
		public final ForgeConfigSpec.IntValue blessingEffectDimensionChangeDuration;
		
		private CommonConfig() {
			COMMON_BUILDER.push("Gameplay");
			COMMON_BUILDER.push("InvasionDifficulty");
			primaryInvasionMobCap = COMMON_BUILDER
					.translation(CONFIG + "primary_invasion_mob_cap")
					.worldRestart()
					.comment("The Max amount of mobs that can spawn from Primary Invasions at once.", "NOTE: Reduce for increased performance!")
					.defineInRange("primaryInvasionMobCap", 100, 0, Integer.MAX_VALUE);
			secondaryInvasionMobCap = COMMON_BUILDER
					.translation(CONFIG + "secondary_invasion_mob_cap")
					.worldRestart()
					.comment("The Max amount of mobs that can spawn from Primary Invasions at once.", "NOTE: Reduce for increased performance!")
					.defineInRange("secondaryInvasionMobCap", 25, 0, Integer.MAX_VALUE);
			dayDifficultyIncreaseDelay = COMMON_BUILDER
					.translation(CONFIG + "day_difficulty_increase_delay")
					.worldRestart()
					.comment("How many days should pass when the Day Invasion Difficulty increases?")
					.defineInRange("dayDifficultyIncreaseDelay", 60, 0, Integer.MAX_VALUE);
			nightDifficultyIncreaseDelay = COMMON_BUILDER
					.translation(CONFIG + "night_difficulty_increase_delay")
					.worldRestart()
					.comment("How many days should pass when the Night Invasion Difficulty increases?")
					.defineInRange("nightDifficultyIncreaseDelay", 40, 0, Integer.MAX_VALUE);
			fixedDifficultyIncreaseDelay = COMMON_BUILDER
					.translation(CONFIG + "fixed_difficulty_increase_delay")
					.worldRestart()
					.comment("How many days should pass when the Fixed Invasion Difficulty increases?")
					.defineInRange("fixedDifficultyIncreaseDelay", 50, 0, Integer.MAX_VALUE);
			maxDayInvasions = COMMON_BUILDER
					.translation(CONFIG + "max_day_invasions")
					.worldRestart()
					.comment("Max Day Invasions that can occur.")
					.defineInRange("maxDayInvasions", 3, 0, Integer.MAX_VALUE);
			maxNightInvasions = COMMON_BUILDER
					.translation(CONFIG + "max_night_invasions")
					.worldRestart()
					.comment("Max Night Invasions that can occur.")
					.defineInRange("maxNightInvasions", 3, 0, Integer.MAX_VALUE);
			maxFixedInvasions = COMMON_BUILDER
					.translation(CONFIG + "max_fixed_invasions")
					.worldRestart()
					.comment("Max Fixed Invasions that can occur.")
					.defineInRange("maxFixedInvasions", 3, 0, Integer.MAX_VALUE);
			multiThreadedInvasions = COMMON_BUILDER
					.translation(CONFIG + "multi_threaded_invasions")
					.worldRestart()
					.comment("Should a thread be added for every dimension's Invasion Spawner instead of just 1 for all?", "NOTE: This can boost performance on multi-threaded CPUs!")
					.define("multiThreadedInvasions", false);
			consistentInvasions = COMMON_BUILDER
					.translation(CONFIG + "consistent_invasions")
					.worldRestart()
					.comment("Should the rarity of Invasions act as a set delay between Invasions instead?")
					.define("consistentInvasions", false);
			tieredInvasions = COMMON_BUILDER
					.translation(CONFIG + "tiered_invasions")
					.worldRestart()
					.comment("Should invasions follow the tier system?")
					.define("tieredInvasions", true);
			invasionBlacklist = COMMON_BUILDER
					.translation(CONFIG + "invasion_blacklist")
					.worldRestart()
					.comment("List of Invasions that can't occur.", "Ex: 'puresuffering:solar_eclipse', 'puresuffering:phantom_zone' (swap '' with quotation marks)")
					.defineList("invasionBlacklist", ImmutableList.of(), string -> {
						return string != "";
					});
			primaryWhitelist = COMMON_BUILDER
					.translation(CONFIG + "primary_whitelist")
					.worldRestart()
					.comment("List of Invasions that can be primary invasions.", "NOTE: The Invasion's Priority cannot be labeled as Secondary Only!", "Ex: 'puresuffering:solar_eclipse', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("primaryWhitelist", ImmutableList.of(), string -> {
						return string != "";
					});
			overworldLikeDimensions = COMMON_BUILDER
					.translation(CONFIG + "overworld_like_dimensions")
					.worldRestart()
					.comment("List of Dimensions that should use Overworld Invasions.", "NOTE: May not work with randomly generated dimensions! (RFTools/Mystcraft)", "Ex: 'twilightforest:twilight_forest', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("overworldLikeDimensions", ImmutableList.of(), string -> {
						return string != "";
					});
			netherLikeDimensions = COMMON_BUILDER
					.translation(CONFIG + "nether_like_dimensions")
					.worldRestart()
					.comment("List of Dimensions that should use Nether Invasions.", "NOTE: May not work with randomly generated dimensions! (RFTools/Mystcraft)", "Ex: 'twilightforest:twilight_forest', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("netherLikeDimensions", ImmutableList.of(), string -> {
						return string != "";
					});
			endLikeDimensions = COMMON_BUILDER
					.translation(CONFIG + "end_like_dimensions")
					.worldRestart()
					.comment("List of Dimensions that should use End Invasions.", "NOTE: May not work with randomly generated dimensions! (RFTools/Mystcraft)", "Ex: 'twilightforest:twilight_forest', 'lostcities:lostcity' (swap '' with quotation marks)")
					.defineList("endLikeDimensions", ImmutableList.of(), string -> {
						return string != "";
					});
			COMMON_BUILDER.pop();
			
			COMMON_BUILDER.push("Balancing");
			dayInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "day_invasion_rarity")
					.worldRestart()
					.comment("How often should Day Invasions occur.")
					.defineInRange("dayInvasionRarity", 21, 1, 100); //Once every 7 hours
			nightInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "night_invasion_rarity")
					.worldRestart()
					.comment("How often should Night Invasions occur.")
					.defineInRange("nightInvasionRarity", 3, 1, 100); //Once every hour
			fixedInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "fixed_invasion_rarity")
					.worldRestart()
					.comment("How often should Fixed Invasions occur.")
					.defineInRange("fixedInvasionRarity", 12, 1, 100); //Once every 2 hours
			hyperInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "hyper_invasion_rarity")
					.worldRestart()
					.comment("How often should Hyper Invasions occur.")
					.defineInRange("hyperInvasionRarity", 12, 1, 100); //Once every half a day (Night Invasions)
			nightmareInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "nightmare_invasion_rarity")
					.worldRestart()
					.comment("How often should Nightmare Invasions occur.")
					.defineInRange("nightmareInvasionRarity", 6, 1, 100); //Once every few actual days (Night Invasions)
			canDayInvasionsBeCanceled = COMMON_BUILDER
					.translation(CONFIG + "can_day_invasions_be_canceled")
					.worldRestart()
					.comment("Can Day Invasions have a random chance to be canceled?")
					.define("canDayInvasionsBeCanceled", true);
			canNightInvasionsBeCanceled = COMMON_BUILDER
					.translation(CONFIG + "can_night_invasions_be_canceled")
					.worldRestart()
					.comment("Can Night Invasions have a random chance to be canceled?")
					.define("canNightInvasionsBeCanceled", true);
			canFixedInvasionsBeCanceled = COMMON_BUILDER
					.translation(CONFIG + "can_fixed_invasions_be_canceled")
					.worldRestart()
					.comment("Can Fixed Invasions have a random chance to be canceled?")
					.define("canFixedInvasionsBeCanceled", true);
			dayCancelChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "day_cancel_chance_multiplier")
					.worldRestart()
					.comment("Chance for Day Invasions to be canceled.", "NOTE: Multiplied by Day Difficulty Increase Delay.")
					.defineInRange("dayCancelChanceMultiplier", 2.5D, 0.0D, 10.0D);
			nightCancelChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "night_cancel_chance_multiplier")
					.worldRestart()
					.comment("Chance for Night Invasions to be canceled.", "NOTE: Multiplied by Night Difficulty Increase Delay.")
					.defineInRange("nightCancelChanceMultiplier", 2.5D, 0.0D, 10.0D);
			fixedCancelChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "fixed_cancel_chance_multiplier")
					.worldRestart()
					.comment("Chance for Fixed Invasions to be canceled.", "NOTE: Multiplied by Fixed Difficulty Increase Delay.")
					.defineInRange("fixedCancelChanceMultiplier", 2.5D, 0.0D, 10.0D);
			maxHyperCharge = COMMON_BUILDER
					.translation(CONFIG + "max_hyper_charge")
					.worldRestart()
					.comment("Maximum value in which a mob can be hypercharged", "NOTE: Max value can only occur in Extreme Invasions", "NOTE: May affect performance at higher numbers!")
					.defineInRange("maxHyperCharge", 4, 1, 100);
			COMMON_BUILDER.pop();
			
			COMMON_BUILDER.push("Modifications");
			hyperAggression = COMMON_BUILDER
					.translation(CONFIG + "hyper_aggression")
					.worldRestart()
					.comment("Should neutral invasion mobs agro the player when spawned?")
					.define("hyperAggression", true);
			hyperCharge = COMMON_BUILDER
					.translation(CONFIG + "hyper_charge")
					.worldRestart()
					.comment("Should mobs be able to be hyper charged?")
					.define("hyperCharge", true);
			hyperInvasions = COMMON_BUILDER
					.translation(CONFIG + "hyper_invasions")
					.worldRestart()
					.comment("Should hyper invasions be able to occur?")
					.define("hyperInvasions", true);
			nightmareInvasions = COMMON_BUILDER
					.translation(CONFIG + "nightmare_invasions")
					.worldRestart()
					.comment("Should nightmare invasions be able to occur?", "NOTE: hyper invasions must be enabled.")
					.define("nightmareInvasions", true);
			hyperAggressionBlacklist = COMMON_BUILDER
					.translation(CONFIG + "hyper_aggression_blacklist")
					.worldRestart()
					.comment("List of Mobs that won't be hyper aggressive towards the player. (If setting is turned on)")
					.defineList("hyperAggressionBlacklist", ImmutableList.of("minecraft:vex"), string -> {
						return string != "";
					});
			hyperChargeBlacklist = COMMON_BUILDER
					.translation(CONFIG + "hyper_charge_blacklist")
					.worldRestart()
					.comment("List of Mobs that can't be hyper charged. (If setting is turned on)")
					.defineList("hyperChargeBlacklist", ImmutableList.of("minecraft:vex"), string -> {
						return string != "";
					});
			modBiomeBoostedBlacklist = COMMON_BUILDER
					.translation(CONFIG + "mod_biome_boosted_blacklist")
					.worldRestart()
					.comment("List of Mods that won't be allowed to have their mobs spawn in Biome Boosted Invasions.", "Ex: 'johncraft', 'mutantbeasts'")
					.defineList("modBiomeBoostedBlacklist", ImmutableList.of(), string -> {
						return string != "";
					});
			mobBiomeBoostedBlacklist = COMMON_BUILDER
					.translation(CONFIG + "mob_biome_boosted_blacklist")
					.worldRestart()
					.comment("List of Mobs that won't be allowed to spawn in Biome Boosted Invasions.", "Ex: 'minecraft:enderman', 'mutantbeasts:mutant_creeper'")
					.defineList("mobBiomeBoostedBlacklist", ImmutableList.of(), string -> {
						return string != "";
					});
			forceInvasionSleeplessness = COMMON_BUILDER
					.translation(CONFIG + "force_invasion_sleeplessness")
					.worldRestart()
					.comment("Should players be unable to sleep during all invasions?")
					.define("forceInvasionSleeplessness", false);
			weakenedVexes = COMMON_BUILDER
					.translation(CONFIG + "weakened_vexes")
					.worldRestart()
					.comment("Should vexes in Invasions be weakened?")
					.define("weakenedVexes", true);
			useXPMultiplier = COMMON_BUILDER
					.translation(CONFIG + "use_xp_multiplier")
					.worldRestart()
					.comment("This determines whether invasion mobs should have an xp boost per kill.")
					.define("useXPMultiplier", true);
			invasionAntiGrief = COMMON_BUILDER
					.translation(CONFIG + "invasion_anti_grief")
					.worldRestart()
					.comment("Should Invasion Mobs cause fire and explosions?")
					.define("invasionAntiGrief", false);
			shouldMobsDieAtEndOfInvasions = COMMON_BUILDER
					.translation(CONFIG + "should_mobs_die_at_end_of_invasions")
					.worldRestart()
					.comment("Should Invasion Mobs die when the Invasions are over?", "NOTE: Can be used to reduce server lag.")
					.define("shouldMobsDieAtEndOfInvasions", false);
			shouldMobsSpawnWithMaxRange = COMMON_BUILDER
					.translation(CONFIG + "should_mobs_spawn_with_max_range")
					.worldRestart()
					.comment("Should Invasion Mobs spawn with max follow range?", "NOTE: Very Taxing on server performance!")
					.define("shouldMobsSpawnWithMaxRange", false);
			naturalSpawnChance = COMMON_BUILDER
					.translation(CONFIG + "natural_spawn_chance")
					.worldRestart()
					.comment("The Chance of a naturally spawning mob has of spawning during an Invasion.", "NOTE: May affect performance at higher numbers!")
					.defineInRange("naturalSpawnChance", 3, 0, 10000);
			hyperChargeChance = COMMON_BUILDER
					.translation(CONFIG + "hyper_charge_chance")
					.worldRestart()
					.comment("The Chance of an invasion mob being hyper charged")
					.defineInRange("hyperChargeChance", 20, 0, 10000);
			blessingEffectRespawnDuration = COMMON_BUILDER
					.translation(CONFIG + "blessing_effect_respawn_duration")
					.worldRestart()
					.comment("How many ticks the Blessing Effect lasts when respawning.")
					.defineInRange("blessingEffectRespawnDuration", 600, 0, Integer.MAX_VALUE);
			blessingEffectDimensionChangeDuration = COMMON_BUILDER
					.translation(CONFIG + "blessing_effect_dimension_change_duration")
					.worldRestart()
					.comment("How many ticks the Blessing Effect lasts when changing dimensions.")
					.defineInRange("blessingEffectDimensionChangeDuration", 300, 0, Integer.MAX_VALUE);
			COMMON_BUILDER.pop();
			COMMON_BUILDER.pop();
		}
	}
	
	public static final class ClientConfig {
		public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		public static final ClientConfig CLIENT = new ClientConfig();
		
		public final ForgeConfigSpec.BooleanValue useSkyBoxRenderer;
		public final ForgeConfigSpec.BooleanValue useInvasionSoundEffects;
		public final ForgeConfigSpec.BooleanValue canInvasionsChangeBrightness;
		public final ForgeConfigSpec.BooleanValue enableVortexParticles;
		public final ForgeConfigSpec.IntValue minVortexParticleLifespan;
		public final ForgeConfigSpec.IntValue maxVortexParticleLifespan;
		public final ForgeConfigSpec.IntValue vortexParticleSpread;
		
		private ClientConfig() {
			CLIENT_BUILDER.push("Rendering");
			useSkyBoxRenderer = CLIENT_BUILDER
					.translation(CONFIG + "use_sky_box_renderer")
					.comment("Can render Invasions with a custom sky box renderer?", "NOTE: Set false with incompatible shaders!")
					.define("useSkyBoxRenderer", true);
			useInvasionSoundEffects = CLIENT_BUILDER
					.translation(CONFIG + "use_invasion_sound_effects")
					.comment("Should the sound effects signaling invasion be used?")
					.define("useInvasionSoundEffects", true);
			canInvasionsChangeBrightness = CLIENT_BUILDER
					.translation(CONFIG + "can_invasions_change_brightness")
					.comment("Can Invasions change the brightness Values?", "NOTE: Set false with incompatible shaders!")
					.define("canInvasionsChangeBrightness", true);
			enableVortexParticles = CLIENT_BUILDER
					.translation(CONFIG + "enable_vortex_particles")
					.comment("Should Hyper Invasion vortex particles be enabled?", "NOTE: Set false with incompatible shaders!", "NOTE: May effect performance in hyper & nightmare invasions!")
					.define("enableVortexParticles", true);
			minVortexParticleLifespan = CLIENT_BUILDER
					.translation(CONFIG + "min_vortex_particle_lifespan")
					.comment("Minimum lifespan for vortex particles in Hyper Invasions.", "NOTE: Set to 'enableVortexParticles' to false to disable.")
					.defineInRange("minVortexParticleLifespan", 300, 1, Integer.MAX_VALUE);
			maxVortexParticleLifespan = CLIENT_BUILDER
					.translation(CONFIG + "max_vortex_particles_lifespan")
					.comment("Maximum lifespan for vortex particles in Hyper Invasions.", "NOTE: Set to 'enableVortexParticles' to false to disable.")
					.defineInRange("maxVortexParticleLifespan", 6300, 1, Integer.MAX_VALUE);
			vortexParticleSpread = CLIENT_BUILDER
					.translation(CONFIG + "vortex_particle_spread")
					.comment("Spread value for vortex particles (Delay between particles).")
					.defineInRange("vortexParticleSpread", 6, 1, Integer.MAX_VALUE);
			CLIENT_BUILDER.pop();
		}
	}

	public static final void initConfig() {
		final Path configPath = FMLPaths.CONFIGDIR.get();
		final Path psConfigPath = Paths.get(configPath.toAbsolutePath().toString(), PureSufferingMod.MODID);
		try {
			Files.createDirectory(psConfigPath);
		} catch (final FileAlreadyExistsException exceptionIn) {
			LOGGER.info("Config directory for " + PureSufferingMod.MODID + " already exists!");
		} catch (final IOException exceptionIn) {
			LOGGER.error("Failed to create " + PureSufferingMod.MODID + " config directory!", exceptionIn);
		}
		loadConfig(CommonConfig.COMMON_BUILDER.build(), configPath.resolve(PureSufferingMod.MODID + "/" + PureSufferingMod.MODID + "-common.toml"));
		loadConfig(ClientConfig.CLIENT_BUILDER.build(), configPath.resolve(PureSufferingMod.MODID + "/" + PureSufferingMod.MODID + "-client.toml"));
	}
	
    private static final void loadConfig(final ForgeConfigSpec specIn, final Path pathIn) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(pathIn)
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        configData.load();
        specIn.setConfig(configData);
    }
}
