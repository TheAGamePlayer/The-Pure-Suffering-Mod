package dev.theagameplayer.puresuffering.config;

import java.util.HashMap;
import java.util.List;

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.loading.FMLEnvironment;

public abstract class PSConfigValues { //Exists so that the config file doesn't have to be read every time a value is needed.
	public static CommonValues common;
	public static ClientValues client;
	public static final HashMap<ServerLevel, LevelValues> LEVELS = new HashMap<>();

	static {
		common = new CommonValues();
		if (FMLEnvironment.dist.isClient())  client = new ClientValues();
	}

	public static final void resyncCommon() {
		common = new CommonValues();
	}

	public static final void resyncClient() {
		if (FMLEnvironment.dist.isClient()) client = new ClientValues();
	}

	public static final void addLevelValues(final ServerLevel pLevel) {
		LEVELS.put(pLevel, new LevelValues(pLevel));
	}

	public static final class CommonValues {
		//GameRules - Boolean
		public final boolean overrideGameRules;
		public final boolean enableHyperInvasions;
		public final boolean enableNightmareInvasions;
		public final boolean invasionAntiGrief;
		public final boolean consistentInvasions;
		public final boolean tieredInvasions;
		public final boolean hyperAggression;
		public final boolean hyperCharge;
		public final boolean forceInvasionSleeplessness;
		public final boolean useXPMultiplier;
		public final boolean mobsDieAtEndOfInvasions;
		public final boolean weakenedInvasionVexes;
		public final boolean enableInvasionAmbience;
		public final boolean notifyPlayersAboutInvasions;
		//GameRules - Integer
		public final int primaryInvasionMobCap;
		public final int secondaryInvasionMobCap;
		//Invasions
		public final List<? extends String> invasionBlacklist;
		public final List<? extends String> primaryWhitelist;
		public final List<? extends String> overworldLikeDimensions;
		public final List<? extends String> netherLikeDimensions;
		public final List<? extends String> endLikeDimensions;
		public final List<? extends String> hyperAggressionBlacklist;
		public final List<? extends String> hyperChargeBlacklist;
		public final List<? extends String> modBiomeBoostedBlacklist;
		public final List<? extends String> mobBiomeBoostedBlacklist;
		public final double naturalSpawnChance;
		public final double hyperChargeChance;
		public final int blessingEffectRespawnDuration;
		public final int blessingEffectDimensionChangeDuration;

		private CommonValues() {
			//GameRules - Boolean
			this.overrideGameRules = PSConfig.COMMON.overrideGameRules.get();
			this.enableHyperInvasions = PSConfig.COMMON.enableHyperInvasions.get();
			this.enableNightmareInvasions = PSConfig.COMMON.enableNightmareInvasions.get();
			this.invasionAntiGrief = PSConfig.COMMON.invasionAntiGrief.get();
			this.consistentInvasions = PSConfig.COMMON.consistentInvasions.get();
			this.tieredInvasions = PSConfig.COMMON.tieredInvasions.get();
			this.hyperAggression = PSConfig.COMMON.hyperAggression.get();
			this.hyperCharge = PSConfig.COMMON.hyperCharge.get();
			this.forceInvasionSleeplessness = PSConfig.COMMON.forceInvasionSleeplessness.get();
			this.useXPMultiplier = PSConfig.COMMON.useXPMultiplier.get();
			this.mobsDieAtEndOfInvasions = PSConfig.COMMON.mobsDieAtEndOfInvasions.get();
			this.weakenedInvasionVexes = PSConfig.COMMON.weakenedInvasionVexes.get();
			this.enableInvasionAmbience = PSConfig.COMMON.enableInvasionAmbience.get();
			this.notifyPlayersAboutInvasions = PSConfig.COMMON.notifyPlayersAboutInvasions.get();
			//GameRules - Integer
			this.primaryInvasionMobCap = PSConfig.COMMON.primaryInvasionMobCap.get();
			this.secondaryInvasionMobCap = PSConfig.COMMON.secondaryInvasionMobCap.get();
			//Invasions
			this.invasionBlacklist = PSConfig.COMMON.invasionBlacklist.get();
			this.primaryWhitelist = PSConfig.COMMON.primaryWhitelist.get();
			this.overworldLikeDimensions = PSConfig.COMMON.overworldLikeDimensions.get();
			this.netherLikeDimensions = PSConfig.COMMON.netherLikeDimensions.get();
			this.endLikeDimensions = PSConfig.COMMON.endLikeDimensions.get();
			this.hyperAggressionBlacklist = PSConfig.COMMON.hyperAggressionBlacklist.get();
			this.hyperChargeBlacklist = PSConfig.COMMON.hyperChargeBlacklist.get();
			this.modBiomeBoostedBlacklist = PSConfig.COMMON.modBiomeBoostedBlacklist.get();
			this.mobBiomeBoostedBlacklist = PSConfig.COMMON.mobBiomeBoostedBlacklist.get();
			this.naturalSpawnChance = PSConfig.COMMON.naturalSpawnChance.get();
			this.hyperChargeChance = PSConfig.COMMON.hyperChargeChance.get();
			this.blessingEffectRespawnDuration = PSConfig.COMMON.blessingEffectRespawnDuration.get();
			this.blessingEffectDimensionChangeDuration = PSConfig.COMMON.blessingEffectDimensionChangeDuration.get();
		}
	}

	public static final class ClientValues {
		//Rendering
		public final boolean useSkyBoxRenderer;
		public final boolean canInvasionsChangeBrightness;
		public final boolean enableInvasionStartEffects;
		public final boolean enableSkyFlickering;
		public final boolean enableSkyEffects;
		public final boolean useFastEffects;
		public final int minVortexParticleLifespan;
		public final int maxVortexParticleLifespan;
		public final int vortexParticleDelay;
		//Sound
		public final boolean useInvasionSoundEffects;

		private ClientValues() {
			//Rendering
			this.useSkyBoxRenderer = PSConfig.CLIENT.useSkyBoxRenderer.get();
			this.canInvasionsChangeBrightness = PSConfig.CLIENT.canInvasionsChangeBrightness.get();
			this.enableInvasionStartEffects = PSConfig.CLIENT.enableInvasionStartEffects.get();
			this.enableSkyFlickering = PSConfig.CLIENT.enableSkyFlickering.get();
			this.enableSkyEffects = PSConfig.CLIENT.enableSkyEffects.get();
			this.useFastEffects = PSConfig.CLIENT.useFastEffects.get();
			this.minVortexParticleLifespan = PSConfig.CLIENT.minVortexParticleLifespan.get();
			this.maxVortexParticleLifespan = PSConfig.CLIENT.maxVortexParticleLifespan.get();
			this.vortexParticleDelay = PSConfig.CLIENT.vortexParticleDelay.get();
			//Sound
			this.useInvasionSoundEffects = PSConfig.CLIENT.useInvasionSoundEffects.get();
		}
	}

	public static final class LevelValues {
		public final int[] invasionSessionTypeRarity;
		public final int[] invasionDifficultyRarity;
		public final boolean[] cancelableInvasions;
		public final int[] cancelInvasionRarity;
		public final int[] maxInvasions;
		public final int[] tierIncreaseDelay;

		private LevelValues(final ServerLevel pLevel) {
			final PSConfig.LevelConfig config = PSConfig.LEVELS.get(pLevel.dimension());
			final int sessionTypeLength = pLevel.dimensionType().hasFixedTime() ? 1 : 2;
			final int difficultyLength = InvasionDifficulty.values().length - 1;
			this.invasionSessionTypeRarity = new int[sessionTypeLength];
			this.invasionDifficultyRarity = new int[difficultyLength];
			this.cancelableInvasions = new boolean[sessionTypeLength];
			this.cancelInvasionRarity = new int[sessionTypeLength];
			this.maxInvasions = new int[sessionTypeLength];
			this.tierIncreaseDelay = new int[sessionTypeLength];
			for (int st = 0; st < sessionTypeLength; st++) {
				this.invasionSessionTypeRarity[st] = config.invasionSessionTypeRarity[st].get();
				this.cancelableInvasions[st] = config.cancelableInvasions[st].get();
				this.cancelInvasionRarity[st] = config.cancelInvasionRarity[st].get();
				this.maxInvasions[st] = config.maxInvasions[st].get();
				this.tierIncreaseDelay[st] = config.tierIncreaseDelay[st].get();
			}
			for (int d = 0; d < difficultyLength; d++)
				this.invasionDifficultyRarity[d] = config.invasionDifficultyRarity[d].get();
		}
	}
}
