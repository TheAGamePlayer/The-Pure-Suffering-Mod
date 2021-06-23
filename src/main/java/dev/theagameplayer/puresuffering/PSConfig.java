package dev.theagameplayer.puresuffering;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public class PSConfig {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	
	public static class CommonConfig {
		public final ForgeConfigSpec.IntValue difficultyIncreaseDelay;
		public final ForgeConfigSpec.IntValue maxDayInvasions;
		public final ForgeConfigSpec.IntValue maxNightInvasions;
		public final ForgeConfigSpec.BooleanValue canDayInvasionsBeCanceled;
		public final ForgeConfigSpec.BooleanValue canNightInvasionsBeCanceled;
		public final ForgeConfigSpec.DoubleValue dayChanceMultiplier;
		public final ForgeConfigSpec.DoubleValue nightChanceMultiplier;
		public final ForgeConfigSpec.BooleanValue shouldMobsDieAtEndOfInvasions;
		public final ForgeConfigSpec.BooleanValue shouldMobsSpawnWithInfiniteRange;
		
		
		CommonConfig(ForgeConfigSpec.Builder builderIn) {
			builderIn.comment("Gameplay modifications").push("common");
			difficultyIncreaseDelay = builderIn
					.comment("How many days should pass when the Invasion Difficulty increases.")
					.worldRestart()
					.defineInRange("difficultyIncreaseDelay", 100, 0, Integer.MAX_VALUE);
			maxDayInvasions = builderIn
					.comment("Max Day Invasions that can occur.")
					.worldRestart()
					.defineInRange("maxDayInvasions", 100, 0, Integer.MAX_VALUE);
			maxNightInvasions = builderIn
					.comment("Max Night Invasions that can occur.")
					.worldRestart()
					.defineInRange("maxDayInvasions", 100, 0, Integer.MAX_VALUE);
			canDayInvasionsBeCanceled = builderIn
					.comment("Can Day Invasions have a random chance to be canceled.")
					.worldRestart()
					.define("canDayInvasionsBeCanceled", true);
			canNightInvasionsBeCanceled = builderIn
					.comment("Can Night Invasions have a random chance to be canceled.")
					.worldRestart()
					.define("canNightInvasionsBeCanceled", true);
			dayChanceMultiplier = builderIn
					.comment("Chance for Day Invasions to be canceled.", "NOTE: Difficulty Increase Delay is base value!")
					.worldRestart()
					.defineInRange("dayChanceMultiplier", 1.0D, 0.0D, 10.0D);
			nightChanceMultiplier = builderIn
					.comment("Chance for Night Invasions to be canceled.", "NOTE: Difficulty Increase Delay is base value!")
					.worldRestart()
					.defineInRange("nightChanceMultiplier", 1.0D, 0.0D, 10.0D);
			shouldMobsDieAtEndOfInvasions = builderIn
					.comment("Should Invasion Mobs die when the Invasions are over.", "NOTE: Can be used to reduce server lag.")
					.worldRestart()
					.define("shouldMobsDieAtEndOfInvasions", false);
			shouldMobsSpawnWithInfiniteRange = builderIn
					.comment("Should Invasion Mobs spawn with infinite follow range.", "NOTE: Very Taxing on server performance!")
					.worldRestart()
					.define("shouldMobsSpawnWithInfiniteRange", false);
			builderIn.pop();
		}
	}
	
	public static class ClientConfig {
		public final ForgeConfigSpec.BooleanValue useSkyBoxRenderer;
		
		ClientConfig(ForgeConfigSpec.Builder builderIn) {
			builderIn.comment("Rendering modifications").push("client");
			useSkyBoxRenderer = builderIn
					.comment("Can render Invasions with a custom sky box renderer.", "NOTE: Set false with incompatible shaders!")
					.define("UseSkyBoxRenderer", true);
			builderIn.pop();
		}
	}
	
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final CommonConfig COMMON;
	
	static {
		final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
	
	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final ClientConfig CLIENT;
	
	static {
		final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
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
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, PureSufferingMod.MODID + "/" + PureSufferingMod.MODID + "-common.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, PureSufferingMod.MODID + "/" + PureSufferingMod.MODID + "-client.toml");
	}
	
	public static void configReloading(ModConfig.Reloading eventIn) {
		ModConfig config = eventIn.getConfig();
		if (config.getModId().equals(PureSufferingMod.MODID)) {
			//ForgeConfigSpec spec = config.getSpec();
			//TODO: Server Config Sync goes here.
		}
	}
}
