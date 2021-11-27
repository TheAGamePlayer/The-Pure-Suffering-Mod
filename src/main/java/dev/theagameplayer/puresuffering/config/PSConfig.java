package dev.theagameplayer.puresuffering.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.ImmutableList;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public final class PSConfig {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final String CONFIG = PureSufferingMod.MODID + ".config.";
	
	public static final class CommonConfig {
		public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		public static final CommonConfig COMMON = new CommonConfig();
		//Invasion
		public final ForgeConfigSpec.IntValue primaryInvasionMobCap;
		public final ForgeConfigSpec.IntValue secondaryInvasionMobCap;
		public final ForgeConfigSpec.IntValue dayDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue nightDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue maxDayInvasions;
		public final ForgeConfigSpec.IntValue maxNightInvasions;
		public final ForgeConfigSpec.BooleanValue consistentInvasions;
		public final ForgeConfigSpec.BooleanValue tieredInvasions;
		public final ConfigValue<List<? extends String>> invasionBlacklist;
		public final ConfigValue<List<? extends String>> primaryWhitelist;
		//Balancing
		public final ForgeConfigSpec.IntValue dayInvasionRarity;
		public final ForgeConfigSpec.IntValue nightInvasionRarity;
		public final ForgeConfigSpec.BooleanValue canDayInvasionsBeCanceled;
		public final ForgeConfigSpec.BooleanValue canNightInvasionsBeCanceled;
		public final ForgeConfigSpec.DoubleValue dayCancelChanceMultiplier;
		public final ForgeConfigSpec.DoubleValue nightCancelChanceMultiplier;
		//Modifications
		public final ForgeConfigSpec.BooleanValue hyperAggression;
		public final ConfigValue<List<? extends String>> hyperAggressionBlacklist;
		public final ForgeConfigSpec.BooleanValue weakenedVexes;
		public final ForgeConfigSpec.BooleanValue useXPMultiplier;
		public final ForgeConfigSpec.BooleanValue explosionsDestroyBlocks;
		public final ForgeConfigSpec.BooleanValue shouldMobsDieAtEndOfInvasions;
		public final ForgeConfigSpec.BooleanValue shouldMobsSpawnWithMaxRange;
		public final ForgeConfigSpec.IntValue naturalSpawnChance;
		
		
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
					.comment("List of Invasions that can't occur.", "Ex: 'puresuffering:solar_eclipse', 'puresuffering:phantom_zone' (must be surrounded by quotation marks)")
					.defineList("invasionBlacklist", ImmutableList.of(), string -> {
						return string != "";
					});
			primaryWhitelist = COMMON_BUILDER
					.translation(CONFIG + "primary_whitelist")
					.worldRestart()
					.comment("List of Invasions that can be primary invasions.", "NOTE: The Invasion's Priority cannot be labeled as Secondary Only!", "Ex: 'puresuffering:solar_eclipse', 'puresuffering:phantom_zone' (must be surrounded by quotation marks)")
					.defineList("primaryWhitelist", ImmutableList.of(), string -> {
						return string != "";
					});
			COMMON_BUILDER.pop();
			
			COMMON_BUILDER.push("Balancing");
			dayInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "day_invasion_rarity")
					.worldRestart()
					.comment("How often should Day Invasions occur.")
					.defineInRange("dayInvasionRarity", 21, 1, 100);
			nightInvasionRarity = COMMON_BUILDER
					.translation(CONFIG + "night_invasion_rarity")
					.worldRestart()
					.comment("How often should Night Invasions occur.")
					.defineInRange("nightInvasionRarity", 3, 1, 100);
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
			dayCancelChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "day_cancel_chance_multiplier")
					.worldRestart()
					.comment("Chance for Day Invasions to be canceled.", "NOTE: Multiplied by Day Difficulty Increase Delay.")
					.defineInRange("dayCancelChanceMultiplier", 1.0D, 0.0D, 10.0D);
			nightCancelChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "night_cancel_chance_multiplier")
					.worldRestart()
					.comment("Chance for Night Invasions to be canceled.", "NOTE: Multiplied by Night Difficulty Increase Delay.")
					.defineInRange("nightCancelChanceMultiplier", 1.0D, 0.0D, 10.0D);
			COMMON_BUILDER.pop();
			
			COMMON_BUILDER.push("Modifications");
			hyperAggression = COMMON_BUILDER
					.translation(CONFIG + "hyper_aggression")
					.worldRestart()
					.comment("Should neutral invasion mobs agro the player when spawned?")
					.define("hyperAggression", true);
			hyperAggressionBlacklist = COMMON_BUILDER
					.translation(CONFIG + "hyper_aggression_blacklist")
					.worldRestart()
					.comment("List of Mobs that won't be hyper aggressive towards the player. (If setting is turned on)")
					.defineList("hyperAggressionBlacklist", ImmutableList.of("minecraft:vex"), string -> {
						return string != "";
					});
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
			explosionsDestroyBlocks = COMMON_BUILDER
					.translation(CONFIG + "explosions_destroy_blocks")
					.worldRestart()
					.comment("Should explosions caused by invasion mobs break blocks?")
					.define("explosionsDestroy", false);
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
			COMMON_BUILDER.pop();
			COMMON_BUILDER.pop();
		}
		
		private ForgeConfigSpec build() {
			return COMMON_BUILDER.build();
		}
	}
	
	public static final class ClientConfig {
		public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		public static final ClientConfig CLIENT = new ClientConfig();
		
		public final ForgeConfigSpec.BooleanValue useSkyBoxRenderer;
		public final ForgeConfigSpec.BooleanValue canInvasionsChangeBrightness;
		
		private ClientConfig() {
			CLIENT_BUILDER.push("Rendering");
			useSkyBoxRenderer = CLIENT_BUILDER
					.translation(CONFIG + "use_sky_box_renderer")
					.comment("Can render Invasions with a custom sky box renderer?", "NOTE: Set false with incompatible shaders!")
					.define("useSkyBoxRenderer", true);
			canInvasionsChangeBrightness = CLIENT_BUILDER
					.translation(CONFIG + "can_invasions_change_brightness")
					.comment("Can Invasions change the brightness Values?", "NOTE: Set false with incompatible shaders!")
					.define("canInvasionsChangeBrightness", true);
			CLIENT_BUILDER.pop();
		}
		
		private ForgeConfigSpec build() {
			return CLIENT_BUILDER.build();
		}
	}

	public static void initConfig() {
		Path configPath = FMLPaths.CONFIGDIR.get();
		Path psConfigPath = Paths.get(configPath.toAbsolutePath().toString(), PureSufferingMod.MODID);
		try {
			Files.createDirectory(psConfigPath);
		} catch (FileAlreadyExistsException exceptionIn) {
			LOGGER.info("Config directory for " + PureSufferingMod.MODID + " already exists!");
		} catch (IOException exceptionIn) {
			LOGGER.error("Failed to create " + PureSufferingMod.MODID + " config directory!", exceptionIn);
		}
		loadConfig(CommonConfig.COMMON.build(), configPath.resolve(PureSufferingMod.MODID + "/" + PureSufferingMod.MODID + "-common.toml"));
		loadConfig(ClientConfig.CLIENT.build(), configPath.resolve(PureSufferingMod.MODID + "/" + PureSufferingMod.MODID + "-client.toml"));
	}
	
    private static void loadConfig(ForgeConfigSpec specIn, Path pathIn) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(pathIn)
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        configData.load();
        specIn.setConfig(configData);
    }
	
	public static void configReloading(ModConfig.Reloading eventIn) {
		/*ModConfig config = eventIn.getConfig();
		if (config.getModId().equals(PureSufferingMod.MODID)) {
			//ForgeConfigSpec spec = config.getSpec();
			//TODO: Server Config Sync goes here.
		}*/
	}
}
