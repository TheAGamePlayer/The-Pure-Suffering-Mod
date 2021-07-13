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
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public final class PSConfig {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final String CONFIG = PureSufferingMod.MODID + ".config.";
	
	public static final class CommonConfig {
		public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		public static final CommonConfig COMMON = new CommonConfig();
		
		public final ForgeConfigSpec.IntValue invasionMobCap;
		public final ForgeConfigSpec.IntValue dayDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue nightDifficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue maxDayInvasions;
		public final ForgeConfigSpec.IntValue maxNightInvasions;
		
		public final ForgeConfigSpec.IntValue dayInvasionRarity;
		public final ForgeConfigSpec.IntValue nightInvasionRarity;
		public final ForgeConfigSpec.BooleanValue canDayInvasionsBeCanceled;
		public final ForgeConfigSpec.BooleanValue canNightInvasionsBeCanceled;
		public final ForgeConfigSpec.DoubleValue dayChanceMultiplier;
		public final ForgeConfigSpec.DoubleValue nightChanceMultiplier;
		
		public final ForgeConfigSpec.BooleanValue autoAgro;
		public final ForgeConfigSpec.ConfigValue<List<String>> autoAgroBlacklist;
		public final ForgeConfigSpec.BooleanValue explosionsDestroyBlocks;
		public final ForgeConfigSpec.BooleanValue shouldMobsDieAtEndOfInvasions;
		public final ForgeConfigSpec.BooleanValue shouldMobsSpawnWithMaxRange;
		public final ForgeConfigSpec.IntValue naturalSpawnChance;
		
		
		private CommonConfig() {
			COMMON_BUILDER.push("Gameplay");
			COMMON_BUILDER.push("InvasionDifficulty");
			invasionMobCap = COMMON_BUILDER
					.translation(CONFIG + "invasion_mob_cap")
					.worldRestart()
					.comment("The Max amount of mobs that can spawn from Invasions at once.", "NOTE: Reduce for increased performance!")
					.defineInRange("invasionMobCap", 150, 0, Integer.MAX_VALUE);
			dayDifficultyIncreaseDelay = COMMON_BUILDER
					.translation(CONFIG + "day_difficulty_increase_delay")
					.worldRestart()
					.comment("How many days should pass when the Day Invasion Difficulty increases.")
					.defineInRange("dayDifficultyIncreaseDelay", 125, 0, Integer.MAX_VALUE);
			nightDifficultyIncreaseDelay = COMMON_BUILDER
					.translation(CONFIG + "night_difficulty_increase_delay")
					.worldRestart()
					.comment("How many days should pass when the Night Invasion Difficulty increases.")
					.defineInRange("nightDifficultyIncreaseDelay", 100, 0, Integer.MAX_VALUE);
			maxDayInvasions = COMMON_BUILDER
					.translation(CONFIG + "max_day_invasions")
					.worldRestart()
					.comment("Max Day Invasions that can occur.")
					.defineInRange("maxDayInvasions", 75, 0, Integer.MAX_VALUE);
			maxNightInvasions = COMMON_BUILDER
					.translation(CONFIG + "max_night_invasions")
					.worldRestart()
					.comment("Max Night Invasions that can occur.")
					.defineInRange("maxNightInvasions", 100, 0, Integer.MAX_VALUE);
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
					.comment("Can Day Invasions have a random chance to be canceled.")
					.define("canDayInvasionsBeCanceled", true);
			canNightInvasionsBeCanceled = COMMON_BUILDER
					.translation(CONFIG + "can_night_invasions_be_canceled")
					.worldRestart()
					.comment("Can Night Invasions have a random chance to be canceled.")
					.define("canNightInvasionsBeCanceled", true);
			dayChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "day_chance_multiplier")
					.worldRestart()
					.comment("Chance for Day Invasions to be canceled.", "NOTE: Multiplied by Day Difficulty Increase Delay.")
					.defineInRange("dayChanceMultiplier", 1.0D, 0.0D, 10.0D);
			nightChanceMultiplier = COMMON_BUILDER
					.translation(CONFIG + "night_chance_multiplier")
					.worldRestart()
					.comment("Chance for Night Invasions to be canceled.", "NOTE: Multiplied by Night Difficulty Increase Delay.")
					.defineInRange("nightChanceMultiplier", 1.0D, 0.0D, 10.0D);
			COMMON_BUILDER.pop();
			
			COMMON_BUILDER.push("Modifications");
			autoAgro = COMMON_BUILDER
					.translation(CONFIG + "auto_agro")
					.worldRestart()
					.comment("Should neutral invasion mobs agro the player when spawned.")
					.define("autoAgro", true);
			autoAgroBlacklist = COMMON_BUILDER
					.translation(CONFIG + "auto_agro_blacklist")
					.worldRestart()
					.comment("List of Mobs that won't auto agro the player. (If setting is turned on)")
					.define("autoAgroBlacklist", ImmutableList.of("minecraft:vex"));
			
			explosionsDestroyBlocks = COMMON_BUILDER
					.translation(CONFIG + "explosions_destroy_blocks")
					.worldRestart()
					.comment("Should explosions caused by invasion mobs break blocks.")
					.define("explosionsDestroy", false);
			shouldMobsDieAtEndOfInvasions = COMMON_BUILDER
					.translation(CONFIG + "should_mobs_die_at_end_of_invasions")
					.worldRestart()
					.comment("Should Invasion Mobs die when the Invasions are over.", "NOTE: Can be used to reduce server lag.")
					.define("shouldMobsDieAtEndOfInvasions", false);
			shouldMobsSpawnWithMaxRange = COMMON_BUILDER
					.translation(CONFIG + "should_mobs_spawn_with_max_range")
					.worldRestart()
					.comment("Should Invasion Mobs spawn with max follow range.", "NOTE: Very Taxing on server performance!")
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
		
		private ClientConfig() {
			CLIENT_BUILDER.push("Rendering");
			useSkyBoxRenderer = CLIENT_BUILDER
					.translation(CONFIG + "use_sky_box_renderer")
					.comment("Can render Invasions with a custom sky box renderer.", "NOTE: Set false with incompatible shaders!")
					.define("useSkyBoxRenderer", true);
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
