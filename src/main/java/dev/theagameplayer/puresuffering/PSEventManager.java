package dev.theagameplayer.puresuffering;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.data.InvasionTypeManager;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPredicates;
import dev.theagameplayer.puresuffering.registries.other.PSGameRulesRegistry;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.util.InvasionRendererMap;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionText;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;

public final class PSEventManager {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);

	public static void attachClientEventListeners(final IEventBus modBusIn, final IEventBus forgeBusIn) {
		//Client
		forgeBusIn.addListener(ClientEvents::loggedIn);
		forgeBusIn.addListener(ClientEvents::loggedOut);
		forgeBusIn.addListener(ClientEvents::fogColors);
		forgeBusIn.addListener(ClientEvents::customizeGuiOverlayDebugText);
	}

	public static void attachCommonEventListeners(final IEventBus modBusIn, final IEventBus forgeBusIn) {
		//Base
		forgeBusIn.addListener(BaseEvents::addReloadListeners);
		forgeBusIn.addListener(BaseEvents::registerCommands);
		forgeBusIn.addListener(BaseEvents::levelTick);
		//Entity
		forgeBusIn.addListener(EntityEvents::joinLevel);
		forgeBusIn.addListener(EntityEvents::mobGriefing);
		//Living
		forgeBusIn.addListener(LivingEvents::livingConversion);
		forgeBusIn.addListener(LivingEvents::livingTick);
		forgeBusIn.addListener(LivingEvents::experienceDrop);
		forgeBusIn.addListener(LivingEvents::checkSpawn);
		forgeBusIn.addListener(LivingEvents::specialSpawn);
		forgeBusIn.addListener(LivingEvents::allowDespawn);
		//Player
		forgeBusIn.addListener(PlayerEvents::playerLoggedIn);
		forgeBusIn.addListener(PlayerEvents::playerRespawn);
		forgeBusIn.addListener(PlayerEvents::playerChangeDimension);
		forgeBusIn.addListener(PlayerEvents::playerSleepInBed);
		//Server
		forgeBusIn.addListener(ServerEvents::serverStarted);
		forgeBusIn.addListener(ServerEvents::serverStarting);
		forgeBusIn.addListener(ServerEvents::serverStopping);
	} 

	public static final class ClientEvents {
		public static void loggedIn(final ClientPlayerNetworkEvent.LoggingIn eventIn) {
			PSConfigValues.resync(PSConfigValues.client);
		}

		public static void loggedOut(final ClientPlayerNetworkEvent.LoggingOut eventIn) {
			final Minecraft mc = Minecraft.getInstance();
			ClientInvasionWorldInfo.getDayClientInfo(mc.level).getRendererMap().clear();
			ClientInvasionWorldInfo.getNightClientInfo(mc.level).getRendererMap().clear();
			ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap().clear();
			PSConfigValues.resync(PSConfigValues.client);
		}

		public static void fogColors(final ViewportEvent.ComputeFogColor eventIn) {
			final Minecraft mc = Minecraft.getInstance();
			if (!mc.level.dimensionType().hasFixedTime()) {
				float red = 0.0F, green = 0.0F, blue = 0.0F;
				final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
				final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
				if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
					final ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (final InvasionSkyRenderer renderer : rendererList) {
						final InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					} 
				} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
					final ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (final InvasionSkyRenderer renderer : rendererList) {
						final InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				}
				ClientTransitionHandler.tickFogColor(eventIn, red, green, blue, mc.level.getDayTime() % 12000L);
			} else {
				float red = 0.0F, green = 0.0F, blue = 0.0F;
				final InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap();
				if (!fixedRenderers.isEmpty()) {
					final ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (final InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				}
				ClientTransitionHandler.tickFogColor(eventIn, red, green, blue, mc.level.getDayTime() % 12000L);
			}
		}

		public static void customizeGuiOverlayDebugText(final CustomizeGuiOverlayEvent.DebugText eventIn) {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.options.renderDebug) {
				eventIn.getLeft().add("");
				if (!mc.level.dimensionType().hasFixedTime()) {
					final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
					final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
					if (dayInfo.isClientTime()) {
						eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Day Invasions: " + dayInfo.getInvasionsCount());
						eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Day Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? dayInfo.getXPMultiplier() + "x" : "Disabled"));
						eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Night Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? nightInfo.getXPMultiplier() + "x" : "Disabled"));
						return;
					} else if (nightInfo.isClientTime()) {
						eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Night Invasions: " + nightInfo.getInvasionsCount());
						eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Night Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? nightInfo.getXPMultiplier() + "x" : "Disabled"));
						eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Day Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? dayInfo.getXPMultiplier() + "x" : "Disabled"));
						return;
					}
				} else {
					final ClientInvasionWorldInfo fixedInfo = ClientInvasionWorldInfo.getFixedClientInfo(mc.level);
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Invasions: " + fixedInfo.getInvasionsCount());
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? fixedInfo.getXPMultiplier() + "x" : "Disabled"));
				}
			}
		}
	}

	public static final class BaseEvents {
		private static InvasionTypeManager invasionTypeManager = new InvasionTypeManager();

		public static void addReloadListeners(final AddReloadListenerEvent eventIn) {
			eventIn.addListener(invasionTypeManager);
		}

		public static InvasionTypeManager getInvasionTypeManager() {
			return invasionTypeManager;
		}

		public static void registerCommands(final RegisterCommandsEvent eventIn) {
			PSCommands.build(eventIn.getDispatcher());
		}

		public static void levelTick(final TickEvent.LevelTickEvent eventIn) {
			if (eventIn.side.isServer() && eventIn.phase == TickEvent.Phase.END) {
				final ServerLevel level = (ServerLevel)eventIn.level;
				final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(level);
				if (iwData != null && PSGameRulesRegistry.getEnableInvasions(level)) {
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(level, tiwData) && !tiwData.hasCheckedNight()) { //Sets events for night time
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
								for (final ServerPlayer player : level.players()) {
									if (tiwData.getInvasionSpawner().getDayInvasions().isCanceled()) {
										player.sendSystemMessage(Component.translatable("invasion.puresuffering.day.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
										continue;
									}
									player.sendSystemMessage(Component.translatable("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
									player.sendSystemMessage(InvasionText.create("invasion.puresuffering.message2", tiwData.getInvasionSpawner().getDayInvasions()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
								}
							}
						} else if (ServerTimeUtil.isServerNight(level, tiwData) && !tiwData.hasCheckedDay()) { //Sets events for day time
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
								for (final ServerPlayer player : level.players()) {
									if (tiwData.getInvasionSpawner().getNightInvasions().isCanceled()) {
										player.sendSystemMessage(Component.translatable("invasion.puresuffering.night.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
										continue;
									}
									player.sendSystemMessage(Component.translatable("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
									player.sendSystemMessage(InvasionText.create("invasion.puresuffering.message2", tiwData.getInvasionSpawner().getNightInvasions()).withStyle(ChatFormatting.DARK_RED));
								}
							}
						} else {
							tiwData.setCheckedDay(ServerTimeUtil.isServerNight(level, tiwData));
							tiwData.setCheckedNight(ServerTimeUtil.isServerDay(level, tiwData));
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
								for (final ServerPlayer player : level.players()) {
									if (fiwData.getInvasionSpawner().getInvasions().isCanceled()) {
										player.sendSystemMessage(Component.translatable("invasion.puresuffering.fixed.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
										continue;
									}
									player.sendSystemMessage(Component.translatable("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
									player.sendSystemMessage(InvasionText.create("invasion.puresuffering.message2", fiwData.getInvasionSpawner().getInvasions()).withStyle(ChatFormatting.DARK_RED));
								}
							}
						}
					}
				}
			}
		}
	}

	public static final class EntityEvents {
		public static void joinLevel(final EntityJoinLevelEvent eventIn) {
			if (eventIn.getEntity() instanceof TamableAnimal) {
				final TamableAnimal tameableEntity = (TamableAnimal)eventIn.getEntity();
				if (tameableEntity.getOwner() != null && tameableEntity.getOwner().getPersistentData().contains("AntiGrief")) {
					tameableEntity.getPersistentData().putBoolean("AntiGrief", tameableEntity.getOwner().getPersistentData().getBoolean("AntiGrief"));
				}
			} else if (PSConfigValues.common.weakenedVexes && eventIn.getEntity() instanceof Vex) {
				final Vex vexEntity = (Vex)eventIn.getEntity();
				if (vexEntity.getOwner() != null && vexEntity.getOwner().getPersistentData().contains("InvasionMob")) {
					vexEntity.setLimitedLife(25 + eventIn.getLevel().getRandom().nextInt(65)); //Attempt to fix lag & spawn camping with vexes
				}
			}
		}

		public static void mobGriefing(final EntityMobGriefingEvent eventIn) {
			if (!PSConfigValues.common.explosionsDestroyBlocks && eventIn.getEntity() != null && eventIn.getEntity().getPersistentData().contains("AntiGrief")) {
				eventIn.setResult(Result.DENY);
			}
		}
	}

	public static final class LivingEvents {
		public static void livingConversion(final LivingConversionEvent.Post eventIn) { //Needed for occasional bugginess
			if (eventIn.getOutcome().getClassification(false) == MobCategory.MONSTER) {
				final CompoundTag persistentData = eventIn.getEntity().getPersistentData();
				final CompoundTag outcomeData = eventIn.getOutcome().getPersistentData();
				if (persistentData.contains("InvasionMob"))
					outcomeData.putString("InvasionMob", persistentData.getString("InvasionMob"));
				if (persistentData.contains("AntiGrief"))
					outcomeData.putString("AntiGrief", persistentData.getString("AntiGrief"));
			}
		}

		public static void livingTick(final LivingEvent.LivingTickEvent eventIn) {
			if (eventIn.getEntity() instanceof Mob && eventIn.getEntity().getPersistentData().contains("InvasionMob") && (eventIn.getEntity().getLastHurtByMob() == null || !eventIn.getEntity().getLastHurtByMob().isAlive()) && PSConfigValues.common.hyperAggression && !PSConfigValues.common.hyperAggressionBlacklist.contains(eventIn.getEntity().getType().getDescriptionId())) {
				final Mob mob = (Mob)eventIn.getEntity();
				if (mob.getTarget() instanceof Player) return;
				final Player player = mob.level.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), 144.0D, PSEntityPredicates.HYPER_AGGRESSION);
				if (player != null && player.isAlive()) {
					if (mob instanceof AbstractPiglin) { //If your wondering why a mob doesn't get aggressive, its because it didn't use the default targeting...
						mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, player.getUUID(), 12000L);
					} else if (mob instanceof Hoglin) {
						mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
						mob.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
						mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, 12000L);
					} else if (mob instanceof Zoglin) {
						mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
						mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, 12000L);
					} else {
						mob.setTarget(player);
					}
				}
			}
		}

		public static void experienceDrop(final LivingExperienceDropEvent eventIn) {
			final CompoundTag persistentData = eventIn.getEntity().getPersistentData();
			if (PSConfigValues.common.useXPMultiplier && persistentData.contains("InvasionMob")) {
				final ServerLevel serverLevel = (ServerLevel)eventIn.getEntity().level;
				final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
				if (iwData != null) {
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(serverLevel, tiwData)) {
							tiwData.setDayXPMultiplier(tiwData.getDayXPMultiplier() + 1);
							final double log = Math.log1p(tiwData.getDayXPMultiplier()) / Math.E;
							eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.DAY));
						} else if (ServerTimeUtil.isServerNight(serverLevel, tiwData)) {
							tiwData.setNightXPMultiplier(tiwData.getNightXPMultiplier() + 1);
							final double log = Math.log1p(tiwData.getNightXPMultiplier()) / Math.E;
							eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.NIGHT));
						}
					} else {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.setXPMultiplier(fiwData.getXPMultiplier() + 1);
						final double log = Math.log1p(fiwData.getXPMultiplier()) / Math.E;
						eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.FIXED));
					}
				}
			}
		}

		public static void checkSpawn(final LivingSpawnEvent.CheckSpawn eventIn) {
			if (!eventIn.getLevel().isClientSide()) {
				if (eventIn.getSpawnReason().equals(MobSpawnType.NATURAL)) {
					final ServerLevel serverLevel = (ServerLevel)eventIn.getLevel();
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
					if (iwData != null) {
						if (serverLevel.random.nextInt(10000) < PSConfigValues.common.naturalSpawnChance) {
							eventIn.setResult(Result.DEFAULT);
						} else {
							if (!iwData.hasFixedTime()) {
								final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
								if ((ServerTimeUtil.isServerDay(serverLevel, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) || (ServerTimeUtil.isServerNight(serverLevel, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()))
									eventIn.setResult(Result.DENY);
							} else {
								final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
								if (!fiwData.getInvasionSpawner().getInvasions().isEmpty())
									eventIn.setResult(Result.DENY);
							}
						}
					}
				}
			}
		}

		public static void specialSpawn(final LivingSpawnEvent.SpecialSpawn eventIn) {
			if (!eventIn.getLevel().isClientSide() && eventIn.getEntity().getClassification(false) == MobCategory.MONSTER) {
				if (eventIn.getSpawnReason() == MobSpawnType.NATURAL) {
					final ServerLevel serverLevel = (ServerLevel)eventIn.getLevel();
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
					if (iwData != null) {
						if (!iwData.hasFixedTime()) {
							final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
							if (!tiwData.getInvasionSpawner().getDayInvasions().isEmpty() || !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
								eventIn.getEntity().getPersistentData().putBoolean("AntiGrief", false);
							}
						} else {
							final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
							if (!fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
								eventIn.getEntity().getPersistentData().putBoolean("AntiGrief", true);
							}
						}
					}
				}
			}
		}

		public static void allowDespawn(final LivingSpawnEvent.AllowDespawn eventIn) {
			if (!eventIn.getLevel().isClientSide() && PSConfigValues.common.shouldMobsDieAtEndOfInvasions && eventIn.getEntity() instanceof Mob) {
				final ServerLevel serverLevel = (ServerLevel)eventIn.getLevel();
				final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
				final Mob mobEntity = (Mob)eventIn.getEntity();
				final CompoundTag persistentData = mobEntity.getPersistentData();
				if (iwData != null && persistentData.contains("InvasionMob")) {
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(serverLevel, tiwData)) {
							for (final Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
								if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
									return;
								}
							}
							eventIn.setResult(Result.ALLOW);
						} else if (ServerTimeUtil.isServerNight(serverLevel, tiwData)) {
							for (final Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
								if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
									return;
								}
							}
							eventIn.setResult(Result.ALLOW);
						}
					} else {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						for (final Invasion invasion : fiwData.getInvasionSpawner().getInvasions()) {
							if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
								return;
							}
						}
						eventIn.setResult(Result.ALLOW);
					}
				}
			}
		}
	}

	public static final class PlayerEvents {
		public static void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent eventIn) {
			updatePlayer(eventIn);
		}

		public static void playerRespawn(final PlayerEvent.PlayerRespawnEvent eventIn) {
			if (PSConfigValues.common.hyperAggression)
				eventIn.getEntity().addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), PSConfigValues.common.blessingEffectRespawnDuration, 0));
			updatePlayer(eventIn);
		}

		public static void playerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent eventIn) {
			if (PSConfigValues.common.hyperAggression)
				eventIn.getEntity().addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), PSConfigValues.common.blessingEffectDimensionChangeDuration, 0));
			updatePlayer(eventIn);
		}

		private static void updatePlayer(final PlayerEvent eventIn) {
			if (eventIn.getEntity() instanceof ServerPlayer) {
				final ServerPlayer player = (ServerPlayer)eventIn.getEntity();
				final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get((ServerLevel)player.level);
				if (iwData != null) {
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getDayInvasions().update(player);
						tiwData.getInvasionSpawner().getNightInvasions().update(player);
					} else {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getInvasions().update(player);
					}
				}
				ServerTimeUtil.updateTime(player);
			}
		}

		public static void playerSleepInBed(final PlayerSleepInBedEvent eventIn) {
			final ServerLevel level = (ServerLevel)eventIn.getEntity().level;
			final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(level);
			if (iwData != null && !iwData.hasFixedTime()) {
				final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (ServerTimeUtil.isServerDay(level, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
					for (final Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
						if (PSConfigValues.common.forceInvasionSleeplessness || invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
							eventIn.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
							return;
						}
					}
				} else if (ServerTimeUtil.isServerNight(level, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
					for (final Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
						if (PSConfigValues.common.forceInvasionSleeplessness || invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
							eventIn.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
							return;
						}
					}
				}
			}
		}
	}

	public static final class ServerEvents {
		public static void serverStarted(final ServerStartedEvent eventIn) {
			if (PSConfigValues.common.multiThreadedInvasions) {
				for (final InvasionWorldData iwData : InvasionWorldData.getInvasionData().values()) {
					eventIn.getServer().addTickable(new Thread(() -> {
						if (!iwData.hasFixedTime()) {
							((TimedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
						} else {
							((FixedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
						}
					}, "Invasion Ticker: " + iwData.getWorld().dimension().location()));
				}
			} else {
				eventIn.getServer().addTickable(new Thread(() -> {
					for (final InvasionWorldData iwData : InvasionWorldData.getInvasionData().values()) {
						if (!iwData.getWorld().players().isEmpty()) {
							if (!iwData.hasFixedTime()) {
								((TimedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
							} else {
								((FixedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
							}
						}
					}
				}, "Invasion Ticker"));
			}
		}

		public static void serverStarting(final ServerStartingEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.common);
			eventIn.getServer().getAllLevels().forEach(level -> {
				final boolean hasFixedTime = level.dimensionType().hasFixedTime();
				InvasionWorldData.getInvasionData().put(level, level.getDataStorage().computeIfAbsent(data -> { 
					return hasFixedTime ? FixedInvasionWorldData.load(level, data) : TimedInvasionWorldData.load(level, data);
				}, () -> {
					return hasFixedTime ? new FixedInvasionWorldData(level) : new TimedInvasionWorldData(level);
				}, InvasionWorldData.getFileId(level.dimensionTypeRegistration())));
			});
		}


		public static void serverStopping(final ServerStoppingEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.common);
		}
	}
}
