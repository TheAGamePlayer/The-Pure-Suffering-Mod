package dev.theagameplayer.puresuffering.registries.other;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.network.UpdateGameRulePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public final class PSGameRules {
	public static final ArrayList<PSGameRule<?>> GAME_RULES = new ArrayList<>();

	public static final BooleanRule ENABLE_INVASIONS = new BooleanRule("enableInvasions", true, true);
	public static final BooleanRule ENABLE_HYPER_INVASIONS = new BooleanRule("enableHyperInvasions", true, PSConfigValues.common.enableHyperInvasions);
	public static final BooleanRule ENABLE_NIGHTMARE_INVASIONS = new BooleanRule("enableNightmareInvasions", true, PSConfigValues.common.enableNightmareInvasions);
	public static final BooleanRule INVASION_ANTI_GRIEF = new BooleanRule("invasionAntiGrief", false, PSConfigValues.common.invasionAntiGrief);
	public static final BooleanRule CONSISTENT_INVASIONS = new BooleanRule("consistentInvasions", false, PSConfigValues.common.consistentInvasions);
	public static final BooleanRule TIERED_INVASIONS = new BooleanRule("tieredInvasions", false, PSConfigValues.common.tieredInvasions);
	public static final BooleanRule CANCELABLE_INVASIONS = new BooleanRule("cancelableInvasions", false, true);
	public static final BooleanRule HYPER_AGGRESSION = new BooleanRule("hyperAggression", false, PSConfigValues.common.hyperAggression);
	public static final BooleanRule HYPER_CHARGE = new BooleanRule("hyperCharge", true, PSConfigValues.common.hyperCharge);
	public static final BooleanRule FORCE_INVASION_SLEEPLESSNESS = new BooleanRule("forceInvasionSleeplessness", false, PSConfigValues.common.forceInvasionSleeplessness);
	public static final BooleanRule USE_XP_MULTIPLIER = new BooleanRule("useXPMultiplier", false, PSConfigValues.common.useXPMultiplier);
	public static final BooleanRule MOBS_DIE_AT_END_OF_INVASIONS = new BooleanRule("mobsDieAtEndOfInvasions", false, PSConfigValues.common.mobsDieAtEndOfInvasions);
	public static final BooleanRule WEAKENED_INVASION_VEXES = new BooleanRule("weakenedInvasionVexes", false, PSConfigValues.common.weakenedInvasionVexes);
	public static final BooleanRule ENABLE_INVASION_AMBIENCE = new BooleanRule("enableInvasionAmbience", false, PSConfigValues.common.enableInvasionAmbience);
	public static final BooleanRule NOTIFY_PLAYERS_ABOUT_INVASIONS = new BooleanRule("notifyPlayersAboutInvasions", false, PSConfigValues.common.notifyPlayersAboutInvasions);
	public static final BooleanRule ZERO_TICK_DELAY = new BooleanRule("zeroTickDelay", false, PSConfigValues.common.zeroTickDelay);

	public static final IntegerRule INVASION_START_DELAY = new IntegerRule("invasionStartDelay", false, PSConfigValues.common.invasionStartDelay);
	public static final IntegerRule PRIMARY_INVASION_MOB_CAP = new IntegerRule("primaryInvasionMobCap", false, PSConfigValues.common.primaryInvasionMobCap);
	public static final IntegerRule SECONDARY_INVASION_MOB_CAP = new IntegerRule("secondaryInvasionMobCap", false, PSConfigValues.common.secondaryInvasionMobCap);
	public static final IntegerRule MOB_KILL_LIMIT = new IntegerRule("mobKillLimit", false, PSConfigValues.common.mobKillLimit);
	public static final IntegerRule MOB_SPAWN_CHUNK_RADIUS = new IntegerRule("mobSpawnChunkRadius", false, PSConfigValues.common.mobSpawnChunkRadius);

	public static final void registerGameRules() {
		final GameRules.Category category = GameRules.Category.valueOf("PURE_SUFFERING");
		for (final PSGameRule<?> gameRule : GAME_RULES) gameRule.register(category);
	}

	public static final PSGameRule<?> fromString(final String pName) {
		for (final PSGameRule<?> gameRule : GAME_RULES)
			if (gameRule.name.equals(pName)) return gameRule;
		return null;
	}

	public static final void syncToConfig(final MinecraftServer pServer) {
		for (final PSGameRule<?> gameRule : GAME_RULES) gameRule.syncToConfig(pServer);
	}

	public static final void syncToServer(final ServerPlayer pPlayer) {
		for (final PSGameRule<?> gameRule : GAME_RULES) gameRule.syncToServer(pPlayer);
	}

	public static abstract class PSGameRule<T extends GameRules.Value<T>> {
		private final String name;
		private final GameRules.Type<T> type;
		private final BiConsumer<MinecraftServer, PSGameRule<T>> resyncToConfig;
		private final BiConsumer<ServerPlayer, PSGameRule<T>> resyncToServer;
		private final boolean updatesClient;
		protected GameRules.Key<T> key;

		private PSGameRule(final String pName, final GameRules.Type<T> pType, final BiConsumer<MinecraftServer, PSGameRule<T>> pResyncToConfig, final BiConsumer<ServerPlayer, PSGameRule<T>> pResyncToServer, final boolean pUpdatesClient) {
			this.name = pName;
			this.type = pType;
			this.resyncToConfig = pResyncToConfig;
			this.resyncToServer = pResyncToServer;
			this.updatesClient = pUpdatesClient;
			GAME_RULES.add(this);
		}

		public final T getRule(final GameRules pGameRules) {
			return pGameRules.getRule(this.key);
		}

		public final void register(final GameRules.Category pCategory) {
			this.key = GameRules.register(PureSufferingMod.MODID + ":" + this.name, pCategory, this.type);
		}

		public final void syncToConfig(final MinecraftServer pServer) {
			this.resyncToConfig.accept(pServer, this);
		}

		public final void syncToServer(final ServerPlayer pPlayer) {
			if (this.updatesClient)
				this.resyncToServer.accept(pPlayer, this);
		}

		@Override
		public final boolean equals(final Object pObj) {
			return this.name.equals(pObj);
		}

		@Override
		public final String toString() {
			return this.name;
		}
	}

	public static final class BooleanRule extends PSGameRule<GameRules.BooleanValue> {
		private final boolean defaultValue;

		private BooleanRule(final String pName, final boolean pUpdatesClient, final boolean pValue) {
			super(pName, pUpdatesClient ? GameRules.BooleanValue.create(pValue, (server, value) -> {
				PSPackets.sendToAllClients(new UpdateGameRulePacket(fromString(pName), value.get()));
			}) : GameRules.BooleanValue.create(pValue), (server, gameRule) -> gameRule.getRule(server.getGameRules()).set(pValue, server), (player, gameRule) -> {
				PSPackets.sendToClient(new UpdateGameRulePacket(gameRule, gameRule.getRule(player.server.getGameRules()).get()), player);
			}, pUpdatesClient);
			this.defaultValue = pValue;
		}

		public final boolean get(final Level pLevel) {
			return PSConfigValues.common.overrideGameRules ? this.defaultValue : pLevel.getGameRules().getBoolean(this.key);
		}
	}

	public static final class IntegerRule extends PSGameRule<GameRules.IntegerValue> {
		private final int defaultValue;

		private IntegerRule(final String pName, final boolean pUpdatesClient, final int pValue) {
			super(pName, pUpdatesClient ? GameRules.IntegerValue.create(pValue, (server, value) -> {
				PSPackets.sendToAllClients(new UpdateGameRulePacket(fromString(pName), value.get()));
			}) : GameRules.IntegerValue.create(pValue), (server, gameRule) -> gameRule.getRule(server.getGameRules()).set(pValue, server), (player, gameRule) -> {
				PSPackets.sendToClient(new UpdateGameRulePacket(gameRule, gameRule.getRule(player.server.getGameRules()).get()), player);
			}, pUpdatesClient);
			this.defaultValue = pValue;
		}

		public final int get(final Level pLevel) {
			return PSConfigValues.common.overrideGameRules ? this.defaultValue : pLevel.getGameRules().getInt(this.key);
		}
	}
}
