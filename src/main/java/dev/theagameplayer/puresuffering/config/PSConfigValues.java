package dev.theagameplayer.puresuffering.config;

import java.util.List;

import org.codehaus.plexus.util.FastMap;

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.loading.FMLEnvironment;

public abstract class PSConfigValues { //Exists so that the config file doesn't have to be read every time a value is needed.
	public static CommonValues common;
	public static ClientValues client;
	public static final FastMap<ServerLevel, LevelValues> LEVELS = new FastMap<>();
	
	public static final void syncCommon() {
		common = new CommonValues();
	}

	public static final void syncClient() {
		if (FMLEnvironment.dist.isClient()) client = new ClientValues();
	}

	public static final void addLevelValues(final ServerLevel pLevel) {
		LEVELS.put(pLevel, new LevelValues(pLevel));
	}

	public static final class CommonValues {
		//GameRules - Boolean
		public final boolean overrideGameRules = PSConfig.COMMON.overrideGameRules.get();
		public final boolean enableHyperInvasions = PSConfig.COMMON.enableHyperInvasions.get();
		public final boolean enableNightmareInvasions = PSConfig.COMMON.enableNightmareInvasions.get();
		public final boolean invasionAntiGrief = PSConfig.COMMON.invasionAntiGrief.get();
		public final boolean consistentInvasions = PSConfig.COMMON.consistentInvasions.get();
		public final boolean tieredInvasions = PSConfig.COMMON.tieredInvasions.get();
		public final boolean hyperAggression = PSConfig.COMMON.hyperAggression.get();
		public final boolean hyperCharge = PSConfig.COMMON.hyperCharge.get();
		public final boolean forceInvasionSleeplessness = PSConfig.COMMON.forceInvasionSleeplessness.get();
		public final boolean useXPMultiplier = PSConfig.COMMON.useXPMultiplier.get();
		public final boolean mobsDieAtEndOfInvasions = PSConfig.COMMON.mobsDieAtEndOfInvasions.get();
		public final boolean weakenedInvasionVexes = PSConfig.COMMON.weakenedInvasionVexes.get();
		public final boolean enableInvasionAmbience = PSConfig.COMMON.enableInvasionAmbience.get();
		public final boolean notifyPlayersAboutInvasions = PSConfig.COMMON.notifyPlayersAboutInvasions.get();
		//GameRules - Integer
		public final int primaryInvasionMobCap = PSConfig.COMMON.primaryInvasionMobCap.get();
		public final int secondaryInvasionMobCap = PSConfig.COMMON.secondaryInvasionMobCap.get();
		//Invasions
		public final List<? extends String> invasionBlacklist = PSConfig.COMMON.invasionBlacklist.get();
		public final List<? extends String> primaryWhitelist = PSConfig.COMMON.primaryWhitelist.get();
		public final List<? extends String> overworldLikeDimensions = PSConfig.COMMON.overworldLikeDimensions.get();
		public final List<? extends String> netherLikeDimensions = PSConfig.COMMON.netherLikeDimensions.get();
		public final List<? extends String> endLikeDimensions = PSConfig.COMMON.endLikeDimensions.get();
		public final List<? extends String> hyperAggressionBlacklist = PSConfig.COMMON.hyperAggressionBlacklist.get();
		public final List<? extends String> hyperChargeBlacklist = PSConfig.COMMON.hyperChargeBlacklist.get();
		public final List<? extends String> modBiomeBoostedBlacklist = PSConfig.COMMON.modBiomeBoostedBlacklist.get();
		public final List<? extends String> mobBiomeBoostedBlacklist = PSConfig.COMMON.mobBiomeBoostedBlacklist.get();
		public final double naturalSpawnChance = PSConfig.COMMON.naturalSpawnChance.get();
		public final double hyperChargeChance = PSConfig.COMMON.hyperChargeChance.get();
		public final int blessingEffectRespawnDuration = PSConfig.COMMON.blessingEffectRespawnDuration.get();
		public final int blessingEffectDimensionChangeDuration = PSConfig.COMMON.blessingEffectDimensionChangeDuration.get();
	}

	public static final class ClientValues {
		//Rendering
		public final boolean useSkyBoxRenderer = PSConfig.CLIENT.useSkyBoxRenderer.get();
		public final boolean canInvasionsChangeBrightness = PSConfig.CLIENT.canInvasionsChangeBrightness.get();
		public final boolean enableInvasionStartEffects = PSConfig.CLIENT.enableInvasionStartEffects.get();
		public final boolean enableSkyFlickering = PSConfig.CLIENT.enableSkyFlickering.get();
		public final boolean enableSkyEffects = PSConfig.CLIENT.enableSkyEffects.get();
		public final boolean useFastEffects = PSConfig.CLIENT.useFastEffects.get();
		public final int minVortexParticleLifespan = PSConfig.CLIENT.minVortexParticleLifespan.get();
		public final int maxVortexParticleLifespan = PSConfig.CLIENT.maxVortexParticleLifespan.get();
		public final int vortexParticleDelay = PSConfig.CLIENT.vortexParticleDelay.get();
		//Sound
		public final boolean useInvasionSoundEffects = PSConfig.CLIENT.useInvasionSoundEffects.get();
	}

	public static final class LevelValues {
		public final int[] invasionSessionTypeRarity;
		public final int[] invasionDifficultyRarity;
		public final boolean[] cancelableInvasions;
		public final int[] cancelInvasionRarity;
		public final int[] maxInvasions;
		public final int[] tierIncreaseDelay;

		private LevelValues(final ServerLevel pLevel) {
			final PSConfig.LevelConfig config = PSConfig.LEVELS.get(pLevel.dimension().location());
			if (config == null) throw new IllegalStateException("No pure suffering config found for dimension " + pLevel.dimension().location());
			final int sessionTypeLength = pLevel.dimensionType().hasFixedTime() ? 1 : 2;
			final int difficultyLength = InvasionDifficulty.values().length - 1;
			this.invasionSessionTypeRarity = new int[sessionTypeLength];
			this.invasionDifficultyRarity = new int[difficultyLength];
			this.cancelableInvasions = new boolean[sessionTypeLength];
			this.cancelInvasionRarity = new int[sessionTypeLength];
			this.maxInvasions = new int[sessionTypeLength];
			this.tierIncreaseDelay = new int[sessionTypeLength];
			for (int st = 0; st < sessionTypeLength; ++st) {
				this.invasionSessionTypeRarity[st] = config.invasionSessionTypeRarity[st].get();
				this.cancelableInvasions[st] = config.cancelableInvasions[st].get();
				this.cancelInvasionRarity[st] = config.cancelInvasionRarity[st].get();
				this.maxInvasions[st] = config.maxInvasions[st].get();
				this.tierIncreaseDelay[st] = config.tierIncreaseDelay[st].get();
			}
			for (int d = 0; d < difficultyLength; ++d)
				this.invasionDifficultyRarity[d] = config.invasionDifficultyRarity[d].get();
		}
	}
}
