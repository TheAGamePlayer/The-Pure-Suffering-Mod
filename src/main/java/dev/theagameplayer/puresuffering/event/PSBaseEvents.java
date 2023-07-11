package dev.theagameplayer.puresuffering.event;

import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.SkyParticle;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.data.InvasionTypeManager;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.InvasionSoundPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRulesRegistry;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.util.InvasionRendererMap;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionMessageTimer;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.client.multiplayer.ClientLevel;
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
		if (eventIn.phase == TickEvent.Phase.END) {
			if (eventIn.side.isClient()) { //Ticking Invasion Sky Particles
				if (PSConfigValues.client.enableVortexParticles) {
					final ClientLevel level = (ClientLevel)eventIn.level;
					if (!level.dimensionType().hasFixedTime()) {
						final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(level);
						final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(level);
						if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty() && dayInfo.getRendererMap().getHyperType() != HyperType.DEFAULT) {
							SkyParticle.tickParticles(level, level.dayTime() % 12000L, dayInfo.getRendererMap().getHyperType());
						} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty() && nightInfo.getRendererMap().getHyperType() != HyperType.DEFAULT) {
							SkyParticle.tickParticles(level, level.dayTime() % 12000L, nightInfo.getRendererMap().getHyperType());
						}
					} else {
						final InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(level).getRendererMap();
						if (!fixedRenderers.isEmpty() && fixedRenderers.getHyperType() != HyperType.DEFAULT)
							SkyParticle.tickParticles(level, level.dayTime() % 12000L, fixedRenderers.getHyperType());
					}
				}
			} else { //Assigning & Syncing Invasions
				final ServerLevel level = (ServerLevel)eventIn.level;
				final InvasionWorldData<?> iwData = InvasionWorldData.getInvasionData().get(level);
				if (iwData != null && PSGameRulesRegistry.getEnableInvasions(level)) {
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final boolean isDay = ServerTimeUtil.isServerDay(level, tiwData);
						final boolean isNight = ServerTimeUtil.isServerNight(level, tiwData);
						if (isDay && !tiwData.hasCheckedNight()) { //Sets events for night time
							tiwData.setDays(level.getDayTime() / 24000L);
							final int maxInvasions = Mth.clamp((int)(level.getDayTime() / (24000L * PSConfigValues.common.nightDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxNightInvasions);
							LOGGER.info("Day: " + iwData.getDays() + ", Possible Invasions: " + maxInvasions);
							tiwData.getInvasionSpawner().setNightInvasions(level, tiwData.getDays(), maxInvasions);
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.DAY));
							tiwData.setDayXPMultiplier(0.0D);
							tiwData.setCheckedDay(false);
							tiwData.setCheckedNight(true);
							final InvasionList invasions = tiwData.getInvasionSpawner().getDayInvasions();
							if (!invasions.isEmpty() || invasions.isCanceled()) {
								HyperType hyperType = HyperType.DEFAULT;
								for (final Invasion invasion : invasions) {
									if (invasion.getHyperType().ordinal() > hyperType.ordinal())
										hyperType = invasion.getHyperType();
								}
								for (final ServerPlayer player : level.players())
									PSPacketHandler.sendToClient(new InvasionSoundPacket(hyperType, invasions.isCanceled()), player);
								InvasionMessageTimer.createTimer(level, InvasionListType.DAY, hyperType, invasions.isCanceled());
							}
						} else if (isNight && !tiwData.hasCheckedDay()) { //Sets events for day time
							tiwData.setDays(level.getDayTime() / 24000L);
							final int maxInvasions = Mth.clamp((int)(level.getDayTime() / (24000L * PSConfigValues.common.dayDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxDayInvasions);
							LOGGER.info("Night: " + iwData.getDays() + ", Possible Invasions: " + maxInvasions);
							tiwData.getInvasionSpawner().setDayInvasions(level, tiwData.getDays(), maxInvasions);
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.NIGHT));
							tiwData.setNightXPMultiplier(0.0D);
							tiwData.setCheckedDay(true);
							tiwData.setCheckedNight(false);
							final InvasionList invasions = tiwData.getInvasionSpawner().getNightInvasions();
							if (!invasions.isEmpty() || invasions.isCanceled()) {
								HyperType hyperType = HyperType.DEFAULT;
								for (final Invasion invasion : invasions) {
									if (invasion.getHyperType().ordinal() > hyperType.ordinal())
										hyperType = invasion.getHyperType();
								}
								for (final ServerPlayer player : level.players())
									PSPacketHandler.sendToClient(new InvasionSoundPacket(hyperType, invasions.isCanceled()), player);
								InvasionMessageTimer.createTimer(level, InvasionListType.NIGHT, hyperType, invasions.isCanceled());
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
							final int maxInvasions = Mth.clamp((int)(level.getDayTime() / (24000L * PSConfigValues.common.fixedDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxFixedInvasions);
							LOGGER.info("Cycle: " + iwData.getDays() + ", Possible Invasions: " + maxInvasions);
							fiwData.getInvasionSpawner().setInvasions(level, fiwData.getDays(), maxInvasions);
							fiwData.setFirstCycle(!fiwData.isFirstCycle());
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.FIXED));
							fiwData.setXPMultiplier(0.0D);
							final InvasionList invasions = fiwData.getInvasionSpawner().getInvasions();
							if (!invasions.isEmpty() || invasions.isCanceled()) {
								HyperType hyperType = HyperType.DEFAULT;
								for (final Invasion invasion : invasions) {
									if (invasion.getHyperType().ordinal() > hyperType.ordinal())
										hyperType = invasion.getHyperType();
								}
								for (final ServerPlayer player : level.players())
									PSPacketHandler.sendToClient(new InvasionSoundPacket(hyperType, invasions.isCanceled()), player);
								InvasionMessageTimer.createTimer(level, InvasionListType.FIXED, hyperType, invasions.isCanceled());
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
}
