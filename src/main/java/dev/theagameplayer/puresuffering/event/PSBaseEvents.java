package dev.theagameplayer.puresuffering.event;

import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.data.InvasionTypeManager;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.InvasionSoundPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRulesRegistry;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.util.InvasionMessageTimer;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;

public final class PSBaseEvents {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private static InvasionTypeManager invasionTypeManager = new InvasionTypeManager();

	public static final void addReloadListeners(final AddReloadListenerEvent eventIn) {
		eventIn.addListener(invasionTypeManager);
	}

	public static final InvasionTypeManager getInvasionTypeManager() {
		return invasionTypeManager;
	}

	public static final void registerCommands(final RegisterCommandsEvent eventIn) {
		PSCommands.build(eventIn.getDispatcher());
	}

	public static final void levelTick(final TickEvent.LevelTickEvent eventIn) {
		if (eventIn.side.isServer() && eventIn.phase == TickEvent.Phase.END) {
			final ServerLevel level = (ServerLevel)eventIn.level;
			final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(level);
			if (iwData != null && PSGameRulesRegistry.getEnableInvasions(level)) {
				if (!iwData.hasFixedTime()) {
					final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
					final boolean isDay = ServerTimeUtil.isServerDay(level, tiwData);
					final boolean isNight = ServerTimeUtil.isServerNight(level, tiwData);
					if (isDay && !tiwData.hasCheckedNight()) { //Sets events for night time
						tiwData.setDays(level.getDayTime() / 24000L);
						final int amount = Mth.clamp((int)(level.getDayTime() / (24000L * PSConfigValues.common.nightDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxNightInvasions);
						final int chance = level.random.nextInt((int)(PSConfigValues.common.nightDifficultyIncreaseDelay * PSConfigValues.common.nightCancelChanceMultiplier) + 1);
						final boolean cancelFlag = chance == 0 && amount > 1 && tiwData.getInvasionSpawner().getQueuedNightInvasions().isEmpty() && PSConfigValues.common.canNightInvasionsBeCanceled;
						LOGGER.info("Day: " + iwData.getDays() + ", Possible Invasions: " + amount);
						tiwData.getInvasionSpawner().setNightInvasions(level, cancelFlag, amount, tiwData.getDays());
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.DAY));
						tiwData.setDayXPMultiplier(0.0D);
						tiwData.setCheckedDay(false);
						tiwData.setCheckedNight(true);
						if (!tiwData.getInvasionSpawner().getDayInvasions().isEmpty() || tiwData.getInvasionSpawner().getDayInvasions().isCanceled()) {
							HyperType hyperType = HyperType.DEFAULT;
							for (final Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
								if (invasion.getHyperType().ordinal() > hyperType.ordinal())
									hyperType = invasion.getHyperType();
							}
							for (final ServerPlayer player : level.players()) {
								if (tiwData.getInvasionSpawner().getDayInvasions().isCanceled()) {
									player.sendSystemMessage(Component.translatable("invasion.puresuffering.day.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
									continue;
								}
								PSPacketHandler.sendToClient(new InvasionSoundPacket(hyperType), player);
							}
							if (!tiwData.getInvasionSpawner().getDayInvasions().isCanceled())
								InvasionMessageTimer.createTimer(level, hyperType);
						}
					} else if (isNight && !tiwData.hasCheckedDay()) { //Sets events for day time
						tiwData.setDays(level.getDayTime() / 24000L);
						final int amount = Mth.clamp((int)(level.getDayTime() / (24000L * PSConfigValues.common.dayDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxDayInvasions);
						final int chance = level.random.nextInt((int)(PSConfigValues.common.dayDifficultyIncreaseDelay * PSConfigValues.common.dayCancelChanceMultiplier) + 1);
						final boolean cancelFlag = chance == 0 && amount > 1 && tiwData.getInvasionSpawner().getQueuedDayInvasions().isEmpty() && PSConfigValues.common.canDayInvasionsBeCanceled;
						LOGGER.info("Night: " + iwData.getDays() + ", Possible Invasions: " + amount);
						tiwData.getInvasionSpawner().setDayInvasions(level, cancelFlag, amount, tiwData.getDays());
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.NIGHT));
						tiwData.setNightXPMultiplier(0.0D);
						tiwData.setCheckedDay(true);
						tiwData.setCheckedNight(false);
						if (!tiwData.getInvasionSpawner().getNightInvasions().isEmpty() || tiwData.getInvasionSpawner().getNightInvasions().isCanceled()) {
							HyperType hyperType = HyperType.DEFAULT;
							for (final Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
								if (invasion.getHyperType().ordinal() > hyperType.ordinal())
									hyperType = invasion.getHyperType();
							}
							for (final ServerPlayer player : level.players()) {
								if (tiwData.getInvasionSpawner().getNightInvasions().isCanceled()) {
									player.sendSystemMessage(Component.translatable("invasion.puresuffering.night.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
									continue;
								}
								PSPacketHandler.sendToClient(new InvasionSoundPacket(hyperType), player);
							}
							if (!tiwData.getInvasionSpawner().getNightInvasions().isCanceled())
								InvasionMessageTimer.createTimer(level, hyperType);
						}
					} else {
						tiwData.setCheckedDay(ServerTimeUtil.isServerNight(level, tiwData));
						tiwData.setCheckedNight(ServerTimeUtil.isServerDay(level, tiwData));
					}
					if (isDay) {
						InvasionMessageTimer.tick(level, tiwData.getInvasionSpawner().getDayInvasions());
					} else if (isNight) {
						InvasionMessageTimer.tick(level, tiwData.getInvasionSpawner().getNightInvasions());
					}
				} else {
					final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
					final boolean flag = level.getDayTime() % 24000L < 12000L;
					if (fiwData.isFirstCycle() ? flag : !flag) {
						fiwData.setDays(level.getDayTime() / 24000L);
						final int amount = Mth.clamp((int)(level.getDayTime() / (24000L * PSConfigValues.common.fixedDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxFixedInvasions);
						final int chance = level.random.nextInt((int)(PSConfigValues.common.fixedDifficultyIncreaseDelay * PSConfigValues.common.fixedCancelChanceMultiplier) + 1);
						final boolean cancelFlag = chance == 0 && amount > 1 && fiwData.getInvasionSpawner().getQueuedInvasions().isEmpty() && PSConfigValues.common.canFixedInvasionsBeCanceled;
						LOGGER.info("Cycle: " + iwData.getDays() + ", Possible Invasions: " + amount);
						fiwData.getInvasionSpawner().setInvasions(level, cancelFlag, amount, fiwData.getDays());
						fiwData.setFirstCycle(!fiwData.isFirstCycle());
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.FIXED));
						fiwData.setXPMultiplier(0.0D);
						if (!fiwData.getInvasionSpawner().getInvasions().isEmpty() || fiwData.getInvasionSpawner().getInvasions().isCanceled()) {
							HyperType hyperType = HyperType.DEFAULT;
							for (final Invasion invasion : fiwData.getInvasionSpawner().getInvasions()) {
								if (invasion.getHyperType().ordinal() > hyperType.ordinal())
									hyperType = invasion.getHyperType();
							}
							for (final ServerPlayer player : level.players()) {
								if (fiwData.getInvasionSpawner().getInvasions().isCanceled()) {
									player.sendSystemMessage(Component.translatable("invasion.puresuffering.fixed.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
									continue;
								}
								PSPacketHandler.sendToClient(new InvasionSoundPacket(hyperType), player);
							}
							if (!fiwData.getInvasionSpawner().getInvasions().isCanceled())
								InvasionMessageTimer.createTimer(level, hyperType);
						}
						fiwData.setUpdateRequired(true);
						return;
					}
					if (fiwData.requiresUpdate()) {
						for (final ServerPlayer player : level.players())
							fiwData.getInvasionSpawner().getInvasions().update(player);
						fiwData.setUpdateRequired(false);
					}
					InvasionMessageTimer.tick(level, fiwData.getInvasionSpawner().getInvasions());
				}
			}
		}
	}
}
