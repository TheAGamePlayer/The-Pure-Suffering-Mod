package dev.theagameplayer.puresuffering.registries.other;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateGameRulePacket;
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

	public static final IntegerRule PRIMARY_INVASION_MOB_CAP = new IntegerRule("primaryInvasionMobCap", false, PSConfigValues.common.primaryInvasionMobCap);
	public static final IntegerRule SECONDARY_INVASION_MOB_CAP = new IntegerRule("secondaryInvasionMobCap", false, PSConfigValues.common.secondaryInvasionMobCap);

	public static final void registerGameRules() {
		final GameRules.Category category = GameRules.Category.valueOf("PURE_SUFFERING");
		for (final PSGameRule<?> gameRule : GAME_RULES) gameRule.register(category);
	}

	public static final PSGameRule<?> fromString(final String nameIn) {
		for (final PSGameRule<?> gameRule : GAME_RULES)
			if (gameRule.name.equals(nameIn)) return gameRule;
		return null;
	}

	public static final void syncToConfig(final MinecraftServer serverIn) {
		for (final PSGameRule<?> gameRule : GAME_RULES) gameRule.syncToConfig(serverIn);
	}

	public static final void syncToServer(final ServerPlayer playerIn) {
		for (final PSGameRule<?> gameRule : GAME_RULES) gameRule.syncToServer(playerIn);
	}

	public static abstract class PSGameRule<T extends GameRules.Value<T>> {
		private final String name;
		private final GameRules.Type<T> type;
		private final BiConsumer<MinecraftServer, PSGameRule<T>> resyncToConfig;
		private final BiConsumer<ServerPlayer, PSGameRule<T>> resyncToServer;
		private final boolean updatesClient;
		protected GameRules.Key<T> key;

		private PSGameRule(final String nameIn, final GameRules.Type<T> typeIn, final BiConsumer<MinecraftServer, PSGameRule<T>> resyncToConfigIn, final BiConsumer<ServerPlayer, PSGameRule<T>> resyncToServerIn, final boolean updatesClientIn) {
			this.name = nameIn;
			this.type = typeIn;
			this.resyncToConfig = resyncToConfigIn;
			this.resyncToServer = resyncToServerIn;
			this.updatesClient = updatesClientIn;
			GAME_RULES.add(this);
		}

		public final T getRule(final GameRules gameRulesIn) {
			return gameRulesIn.getRule(this.key);
		}

		public final void register(final GameRules.Category categoryIn) {
			this.key = GameRules.register(PureSufferingMod.MODID + ":" + this.name, categoryIn, this.type);
		}

		public final void syncToConfig(final MinecraftServer serverIn) {
			this.resyncToConfig.accept(serverIn, this);
		}

		public final void syncToServer(final ServerPlayer playerIn) {
			if (this.updatesClient)
				this.resyncToServer.accept(playerIn, this);
		}

		@Override
		public final boolean equals(final Object objIn) {
			return this.name.equals(objIn);
		}

		@Override
		public final String toString() {
			return this.name;
		}
	}

	public static final class BooleanRule extends PSGameRule<GameRules.BooleanValue> {
		private final boolean defaultValue;

		private BooleanRule(final String nameIn, final boolean updatesClientIn, final boolean valueIn) {
			super(nameIn, updatesClientIn ? GameRules.BooleanValue.create(valueIn, (server, value) -> {
				PSPacketHandler.sendToAllClients(new UpdateGameRulePacket(fromString(nameIn), value.get()));
			}) : GameRules.BooleanValue.create(valueIn), (server, gameRule) -> gameRule.getRule(server.getGameRules()).set(valueIn, server), (player, gameRule) -> {
				PSPacketHandler.sendToClient(new UpdateGameRulePacket(gameRule, gameRule.getRule(player.server.getGameRules()).get()), player);
			}, updatesClientIn);
			this.defaultValue = valueIn;
		}

		public final boolean get(final Level levelIn) {
			return PSConfigValues.common.overrideGameRules ? this.defaultValue : levelIn.getGameRules().getBoolean(this.key);
		}
	}

	public static final class IntegerRule extends PSGameRule<GameRules.IntegerValue> {
		private final int defaultValue;

		private IntegerRule(final String nameIn, final boolean updatesClientIn, final int valueIn) {
			super(nameIn, updatesClientIn ? GameRules.IntegerValue.create(valueIn, (server, value) -> {
				PSPacketHandler.sendToAllClients(new UpdateGameRulePacket(fromString(nameIn), value.get()));
			}) : GameRules.IntegerValue.create(valueIn), (server, gameRule) -> gameRule.getRule(server.getGameRules()).set(valueIn, server), (player, gameRule) -> {
				PSPacketHandler.sendToClient(new UpdateGameRulePacket(gameRule, gameRule.getRule(player.server.getGameRules()).get()), player);
			}, updatesClientIn);
			this.defaultValue = valueIn;
		}

		public final int get(final Level levelIn) {
			return PSConfigValues.common.overrideGameRules ? this.defaultValue : levelIn.getGameRules().getInt(this.key);
		}
	}
}
