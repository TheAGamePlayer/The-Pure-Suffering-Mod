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
		public final int primaryInvasionMobCap;
		public final int secondaryInvasionMobCap;
		public final int dayDifficultyIncreaseDelay;
		public final int nightDifficultyIncreaseDelay;
		public final int fixedDifficultyIncreaseDelay;
		public final int maxDayInvasions;
		public final int maxNightInvasions;
		public final int maxFixedInvasions;
		public final boolean multiThreadedInvasions;
		public final boolean consistentInvasions;
		public final boolean tieredInvasions;
		public final List<? extends String> invasionBlacklist;
		public final List<? extends String> primaryWhitelist;
		
		public final int dayInvasionRarity;
		public final int nightInvasionRarity;
		public final int fixedInvasionRarity;
		public final int hyperInvasionRarity;
		public final int mysteryInvasionRarity;
		public final boolean canDayInvasionsBeCanceled;
		public final boolean canNightInvasionsBeCanceled;
		public final boolean canFixedInvasionsBeCanceled;
		public final double dayCancelChanceMultiplier;
		public final double nightCancelChanceMultiplier;
		public final double fixedCancelChanceMultiplier;
		public final int maxHyperCharge;
		
		public final boolean hyperAggression;
		public final boolean hyperCharge;
		public final boolean hyperInvasions;
		public final boolean mysteryInvasions;
		public final List<? extends String> hyperAggressionBlacklist;
		public final List<? extends String> hyperChargeBlacklist;
		public final List<? extends String> modBiomeBoostedBlacklist;
		public final List<? extends String> mobBiomeBoostedBlacklist;
		public final boolean forceInvasionSleeplessness;
		public final boolean weakenedVexes;
		public final boolean useXPMultiplier;
		public final boolean explosionsDestroyBlocks;
		public final boolean shouldMobsDieAtEndOfInvasions;
		public final boolean shouldMobsSpawnWithMaxRange;
		public final int naturalSpawnChance;
		public final int hyperChargeChance;
		public final int blessingEffectRespawnDuration;
		public final int blessingEffectDimensionChangeDuration;
		
		private CommonValues() {
			this.primaryInvasionMobCap = PSConfig.CommonConfig.COMMON.primaryInvasionMobCap.get();
			this.secondaryInvasionMobCap = PSConfig.CommonConfig.COMMON.secondaryInvasionMobCap.get();
			this.dayDifficultyIncreaseDelay = PSConfig.CommonConfig.COMMON.dayDifficultyIncreaseDelay.get();
			this.nightDifficultyIncreaseDelay = PSConfig.CommonConfig.COMMON.nightDifficultyIncreaseDelay.get();
			this.fixedDifficultyIncreaseDelay = PSConfig.CommonConfig.COMMON.fixedDifficultyIncreaseDelay.get();
			this.maxDayInvasions = PSConfig.CommonConfig.COMMON.maxDayInvasions.get();
			this.maxNightInvasions = PSConfig.CommonConfig.COMMON.maxNightInvasions.get();
			this.maxFixedInvasions = PSConfig.CommonConfig.COMMON.maxFixedInvasions.get();
			this.multiThreadedInvasions = PSConfig.CommonConfig.COMMON.multiThreadedInvasions.get();
			this.consistentInvasions = PSConfig.CommonConfig.COMMON.consistentInvasions.get();
			this.tieredInvasions = PSConfig.CommonConfig.COMMON.tieredInvasions.get();
			this.invasionBlacklist = PSConfig.CommonConfig.COMMON.invasionBlacklist.get();
			this.primaryWhitelist = PSConfig.CommonConfig.COMMON.primaryWhitelist.get();
			
			this.dayInvasionRarity = PSConfig.CommonConfig.COMMON.dayInvasionRarity.get();
			this.nightInvasionRarity = PSConfig.CommonConfig.COMMON.nightInvasionRarity.get();
			this.fixedInvasionRarity = PSConfig.CommonConfig.COMMON.fixedInvasionRarity.get();
			this.hyperInvasionRarity = PSConfig.CommonConfig.COMMON.hyperInvasionRarity.get();
			this.mysteryInvasionRarity = PSConfig.CommonConfig.COMMON.mysteryInvasionRarity.get();
			this.canDayInvasionsBeCanceled = PSConfig.CommonConfig.COMMON.canDayInvasionsBeCanceled.get();
			this.canNightInvasionsBeCanceled = PSConfig.CommonConfig.COMMON.canNightInvasionsBeCanceled.get();
			this.canFixedInvasionsBeCanceled = PSConfig.CommonConfig.COMMON.canFixedInvasionsBeCanceled.get();
			this.dayCancelChanceMultiplier = PSConfig.CommonConfig.COMMON.dayCancelChanceMultiplier.get();
			this.nightCancelChanceMultiplier = PSConfig.CommonConfig.COMMON.nightCancelChanceMultiplier.get();
			this.fixedCancelChanceMultiplier = PSConfig.CommonConfig.COMMON.fixedCancelChanceMultiplier.get();
			this.maxHyperCharge = PSConfig.CommonConfig.COMMON.maxHyperCharge.get();
			
			this.hyperAggression = PSConfig.CommonConfig.COMMON.hyperAggression.get();
			this.hyperCharge = PSConfig.CommonConfig.COMMON.hyperCharge.get();
			this.hyperInvasions = PSConfig.CommonConfig.COMMON.hyperInvasions.get();
			this.mysteryInvasions = PSConfig.CommonConfig.COMMON.mysteryInvasions.get();
			this.hyperAggressionBlacklist = PSConfig.CommonConfig.COMMON.hyperAggressionBlacklist.get();
			this.hyperChargeBlacklist = PSConfig.CommonConfig.COMMON.hyperChargeBlacklist.get();
			this.modBiomeBoostedBlacklist = PSConfig.CommonConfig.COMMON.modBiomeBoostedBlacklist.get();
			this.mobBiomeBoostedBlacklist = PSConfig.CommonConfig.COMMON.mobBiomeBoostedBlacklist.get();
			this.forceInvasionSleeplessness = PSConfig.CommonConfig.COMMON.forceInvasionSleeplessness.get();
			this.weakenedVexes = PSConfig.CommonConfig.COMMON.weakenedVexes.get();
			this.useXPMultiplier = PSConfig.CommonConfig.COMMON.useXPMultiplier.get();
			this.explosionsDestroyBlocks = PSConfig.CommonConfig.COMMON.explosionsDestroyBlocks.get();
			this.shouldMobsDieAtEndOfInvasions = PSConfig.CommonConfig.COMMON.shouldMobsDieAtEndOfInvasions.get();
			this.shouldMobsSpawnWithMaxRange = PSConfig.CommonConfig.COMMON.shouldMobsSpawnWithMaxRange.get();
			this.naturalSpawnChance = PSConfig.CommonConfig.COMMON.naturalSpawnChance.get();
			this.hyperChargeChance = PSConfig.CommonConfig.COMMON.hyperChargeChance.get();
			this.blessingEffectRespawnDuration = PSConfig.CommonConfig.COMMON.blessingEffectRespawnDuration.get();
			this.blessingEffectDimensionChangeDuration = PSConfig.CommonConfig.COMMON.blessingEffectDimensionChangeDuration.get();
		}
	}
	
	public static final class ClientValues extends PSConfigValues {
		public final boolean useSkyBoxRenderer;
		public final boolean canInvasionsChangeBrightness;
		
		private ClientValues() {
			this.useSkyBoxRenderer = PSConfig.ClientConfig.CLIENT.useSkyBoxRenderer.get();
			this.canInvasionsChangeBrightness = PSConfig.ClientConfig.CLIENT.canInvasionsChangeBrightness.get();
		}
	}
}
