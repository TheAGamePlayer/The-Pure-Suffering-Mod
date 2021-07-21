package dev.theagameplayer.puresuffering.config;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;

public abstract class PSConfigValues {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	public static CommonValues common = new CommonValues();
	public static ClientValues client = new ClientValues();
	
	public static final <C extends PSConfigValues> void resync(C configIn) {
		if (configIn == common) {
			common = new CommonValues();
		} else if (configIn == client) {
			client = new ClientValues();
		} else {
			LOGGER.error("Unknown Config Values: ", configIn);
		}
	}
	
	public static final class CommonValues extends PSConfigValues {
		public final int invasionMobCap;
		public final int dayDifficultyIncreaseDelay;
		public final int nightDifficultyIncreaseDelay;
		public final int maxDayInvasions;
		public final int maxNightInvasions;
		
		public final int dayInvasionRarity;
		public final int nightInvasionRarity;
		public final boolean canDayInvasionsBeCanceled;
		public final boolean canNightInvasionsBeCanceled;
		public final double dayChanceMultiplier;
		public final double nightChanceMultiplier;
		
		public final boolean autoAgro;
		public final List<? extends String> autoAgroBlacklist;
		public final boolean useXPMultiplier;
		public final boolean explosionsDestroyBlocks;
		public final boolean shouldMobsDieAtEndOfInvasions;
		public final boolean shouldMobsSpawnWithMaxRange;
		public final int naturalSpawnChance;
		
		private CommonValues() {
			this.invasionMobCap = PSConfig.CommonConfig.COMMON.invasionMobCap.get() + 1;
			this.dayDifficultyIncreaseDelay = PSConfig.CommonConfig.COMMON.dayDifficultyIncreaseDelay.get();
			this.nightDifficultyIncreaseDelay = PSConfig.CommonConfig.COMMON.nightDifficultyIncreaseDelay.get();
			this.maxDayInvasions = PSConfig.CommonConfig.COMMON.maxDayInvasions.get();
			this.maxNightInvasions = PSConfig.CommonConfig.COMMON.maxNightInvasions.get();
			
			this.dayInvasionRarity = PSConfig.CommonConfig.COMMON.dayInvasionRarity.get();
			this.nightInvasionRarity = PSConfig.CommonConfig.COMMON.nightInvasionRarity.get();
			this.canDayInvasionsBeCanceled = PSConfig.CommonConfig.COMMON.canDayInvasionsBeCanceled.get();
			this.canNightInvasionsBeCanceled = PSConfig.CommonConfig.COMMON.canNightInvasionsBeCanceled.get();
			this.dayChanceMultiplier = PSConfig.CommonConfig.COMMON.dayChanceMultiplier.get();
			this.nightChanceMultiplier = PSConfig.CommonConfig.COMMON.nightChanceMultiplier.get();
			
			this.autoAgro = PSConfig.CommonConfig.COMMON.autoAgro.get();
			this.autoAgroBlacklist = PSConfig.CommonConfig.COMMON.autoAgroBlacklist.get();
			this.useXPMultiplier = PSConfig.CommonConfig.COMMON.useXPMultiplier.get();
			this.explosionsDestroyBlocks = PSConfig.CommonConfig.COMMON.explosionsDestroyBlocks.get();
			this.shouldMobsDieAtEndOfInvasions = PSConfig.CommonConfig.COMMON.shouldMobsDieAtEndOfInvasions.get();
			this.shouldMobsSpawnWithMaxRange = PSConfig.CommonConfig.COMMON.shouldMobsSpawnWithMaxRange.get();
			this.naturalSpawnChance = PSConfig.CommonConfig.COMMON.naturalSpawnChance.get();
		}
	}
	
	public static final class ClientValues extends PSConfigValues {
		public final boolean useSkyBoxRenderer;
		
		private ClientValues() {
			this.useSkyBoxRenderer = PSConfig.ClientConfig.CLIENT.useSkyBoxRenderer.get();
		}
	}
}
