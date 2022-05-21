package dev.theagameplayer.puresuffering;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.InvasionSkyRenderHandler;
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
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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

	public static void attachClientEventListeners(IEventBus modBusIn, IEventBus forgeBusIn) {
		//Client
		forgeBusIn.addListener(ClientEvents::loggedIn);
		forgeBusIn.addListener(ClientEvents::loggedOut);
		forgeBusIn.addListener(ClientEvents::fogColors);
		forgeBusIn.addListener(ClientEvents::renderGameOverlayText);
		forgeBusIn.addListener(ClientEvents::renderWorldLast);
	}

	public static void attachCommonEventListeners(IEventBus modBusIn, IEventBus forgeBusIn) {
		//Base
		forgeBusIn.addListener(BaseEvents::addReloadListeners);
		forgeBusIn.addListener(BaseEvents::registerCommands);
		forgeBusIn.addListener(BaseEvents::worldTick);
		//Entity
		forgeBusIn.addListener(EntityEvents::joinWorld);
		forgeBusIn.addListener(EntityEvents::mobGriefing);
		//Living
		forgeBusIn.addListener(LivingEvents::livingConversion);
		forgeBusIn.addListener(LivingEvents::livingUpdate);
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
		public static void loggedIn(ClientPlayerNetworkEvent.LoggedInEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.client);
		}

		public static void loggedOut(ClientPlayerNetworkEvent.LoggedOutEvent eventIn) {
			Minecraft mc = Minecraft.getInstance();
			ClientInvasionWorldInfo.getDayClientInfo(mc.level).getRendererMap().clear();
			ClientInvasionWorldInfo.getNightClientInfo(mc.level).getRendererMap().clear();
			ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap().clear();
			PSConfigValues.resync(PSConfigValues.client);
		}

		public static void fogColors(EntityViewRenderEvent.FogColors eventIn) {
			Minecraft mc = Minecraft.getInstance();
			if (!mc.level.dimensionType().hasFixedTime()) {
				float red = 0.0F, green = 0.0F, blue = 0.0F;
				ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
				ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
				if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
					ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					} 
				} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
					ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				}
				ClientTransitionHandler.tickFogColor(eventIn, red, green, blue, mc.level.getDayTime() % 12000L);
			} else {
				float red = 0.0F, green = 0.0F, blue = 0.0F;
				InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap();
				if (!fixedRenderers.isEmpty()) {
					ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				}
				ClientTransitionHandler.tickFogColor(eventIn, red, green, blue, mc.level.getDayTime() % 12000L);
			}
		}

		public static void renderGameOverlayText(RenderGameOverlayEvent.Text eventIn) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.renderDebug) {
				eventIn.getLeft().add("");
				if (!mc.level.dimensionType().hasFixedTime()) {
					ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
					ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
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
					ClientInvasionWorldInfo fixedInfo = ClientInvasionWorldInfo.getFixedClientInfo(mc.level);
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Invasions: " + fixedInfo.getInvasionsCount());
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? fixedInfo.getXPMultiplier() + "x" : "Disabled"));
				}
			}
		}

		public static void renderWorldLast(RenderLevelLastEvent eventIn) {
			Minecraft mc = Minecraft.getInstance();
			ClientLevel clientWorld = mc.level;
			if (clientWorld != null && PSConfigValues.client.useSkyBoxRenderer) {
				ISkyRenderHandler skyRenderHandler = clientWorld.effects().getSkyRenderHandler();
				if (!(skyRenderHandler instanceof InvasionSkyRenderHandler)) {
					clientWorld.effects().setSkyRenderHandler(new InvasionSkyRenderHandler(skyRenderHandler));
				}
			}
		}
	}

	public static final class BaseEvents {
		private static InvasionTypeManager invasionTypeManager = new InvasionTypeManager();

		public static void addReloadListeners(AddReloadListenerEvent eventIn) {
			eventIn.addListener(invasionTypeManager);
		}

		public static InvasionTypeManager getInvasionTypeManager() {
			return invasionTypeManager;
		}

		public static void registerCommands(RegisterCommandsEvent eventIn) {
			PSCommands.build(eventIn.getDispatcher());
		}

		public static void worldTick(TickEvent.WorldTickEvent eventIn) {
			if (eventIn.side.isServer() && eventIn.phase == TickEvent.Phase.END) {
				ServerLevel world = (ServerLevel)eventIn.world;
				InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(world);
				if (iwData != null && PSGameRulesRegistry.getEnableInvasions(world)) {
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(world, tiwData) && !tiwData.hasCheckedNight()) { //Sets events for night time
							tiwData.setDays(world.getDayTime() / 24000L);
							int amount = Mth.clamp((int)(world.getDayTime() / (24000L * PSConfigValues.common.nightDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxNightInvasions);
							int chance = world.random.nextInt((int)(PSConfigValues.common.nightDifficultyIncreaseDelay * PSConfigValues.common.nightCancelChanceMultiplier) + 1);
							boolean cancelFlag = chance == 0 && amount > 1 && tiwData.getInvasionSpawner().getQueuedNightInvasions().isEmpty() && PSConfigValues.common.canNightInvasionsBeCanceled;
							LOGGER.info("Day: " + iwData.getDays() + ", Possible Invasions: " + amount);
							tiwData.getInvasionSpawner().setNightInvasions(world, cancelFlag, amount, tiwData.getDays());
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.DAY));
							tiwData.setDayXPMultiplier(0.0D);
							tiwData.setCheckedDay(false);
							tiwData.setCheckedNight(true);
							if (!tiwData.getInvasionSpawner().getDayInvasions().isEmpty() || tiwData.getInvasionSpawner().getDayInvasions().isCanceled()) {
								for (ServerPlayer player : world.players()) {
									if (tiwData.getInvasionSpawner().getDayInvasions().isCanceled()) {
										player.sendMessage(new TranslatableComponent("invasion.puresuffering.day.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), player.getUUID());
										continue;
									}
									player.sendMessage(new TranslatableComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), player.getUUID());
									player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", tiwData.getInvasionSpawner().getDayInvasions()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)), player.getUUID());
								}
							}
						} else if (ServerTimeUtil.isServerNight(world, tiwData) && !tiwData.hasCheckedDay()) { //Sets events for day time
							tiwData.setDays(world.getDayTime() / 24000L);
							int amount = Mth.clamp((int)(world.getDayTime() / (24000L * PSConfigValues.common.dayDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxDayInvasions);
							int chance = world.random.nextInt((int)(PSConfigValues.common.dayDifficultyIncreaseDelay * PSConfigValues.common.dayCancelChanceMultiplier) + 1);
							boolean cancelFlag = chance == 0 && amount > 1 && tiwData.getInvasionSpawner().getQueuedDayInvasions().isEmpty() && PSConfigValues.common.canDayInvasionsBeCanceled;
							LOGGER.info("Night: " + iwData.getDays() + ", Possible Invasions: " + amount);
							tiwData.getInvasionSpawner().setDayInvasions(world, cancelFlag, amount, tiwData.getDays());
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.NIGHT));
							tiwData.setNightXPMultiplier(0.0D);
							tiwData.setCheckedDay(true);
							tiwData.setCheckedNight(false);
							if (!tiwData.getInvasionSpawner().getNightInvasions().isEmpty() || tiwData.getInvasionSpawner().getNightInvasions().isCanceled()) {
								for (ServerPlayer player : world.players()) {
									if (tiwData.getInvasionSpawner().getNightInvasions().isCanceled()) {
										player.sendMessage(new TranslatableComponent("invasion.puresuffering.night.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), player.getUUID());
										continue;
									}
									player.sendMessage(new TranslatableComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), player.getUUID());
									player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", tiwData.getInvasionSpawner().getNightInvasions()).withStyle(ChatFormatting.DARK_RED), player.getUUID());
								}
							}
						} else {
							tiwData.setCheckedDay(ServerTimeUtil.isServerNight(world, tiwData));
							tiwData.setCheckedNight(ServerTimeUtil.isServerDay(world, tiwData));
						}
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						boolean flag = world.getDayTime() % 24000L < 12000L;
						if (fiwData.isFirstCycle() ? flag : !flag) {
							fiwData.setDays(world.getDayTime() / 24000L);
							int amount = Mth.clamp((int)(world.getDayTime() / (24000L * PSConfigValues.common.fixedDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxFixedInvasions);
							int chance = world.random.nextInt((int)(PSConfigValues.common.fixedDifficultyIncreaseDelay * PSConfigValues.common.fixedCancelChanceMultiplier) + 1);
							boolean cancelFlag = chance == 0 && amount > 1 && fiwData.getInvasionSpawner().getQueuedInvasions().isEmpty() && PSConfigValues.common.canFixedInvasionsBeCanceled;
							LOGGER.info("Cycle: " + iwData.getDays() + ", Possible Invasions: " + amount);
							fiwData.getInvasionSpawner().setInvasions(world, cancelFlag, amount, fiwData.getDays());
							fiwData.setFirstCycle(!fiwData.isFirstCycle());
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, InvasionListType.FIXED));
							fiwData.setXPMultiplier(0.0D);
							if (!fiwData.getInvasionSpawner().getInvasions().isEmpty() || fiwData.getInvasionSpawner().getInvasions().isCanceled()) {
								for (ServerPlayer player : world.players()) {
									if (fiwData.getInvasionSpawner().getInvasions().isCanceled()) {
										player.sendMessage(new TranslatableComponent("invasion.puresuffering.fixed.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), player.getUUID());
										continue;
									}
									player.sendMessage(new TranslatableComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), player.getUUID());
									player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", fiwData.getInvasionSpawner().getInvasions()).withStyle(ChatFormatting.DARK_RED), player.getUUID());
								}
							}
						}
					}
				}
			}
		}
	}

	public static final class EntityEvents {
		public static void joinWorld(EntityJoinWorldEvent eventIn) {
			if (eventIn.getEntity() instanceof TamableAnimal) {
				TamableAnimal tameableEntity = (TamableAnimal)eventIn.getEntity();
				if (tameableEntity.getOwner() != null && tameableEntity.getOwner().getPersistentData().contains("AntiGrief")) {
					tameableEntity.getPersistentData().putBoolean("AntiGrief", tameableEntity.getOwner().getPersistentData().getBoolean("AntiGrief"));
				}
			} else if (PSConfigValues.common.weakenedVexes && eventIn.getEntity() instanceof Vex) {
				Vex vexEntity = (Vex)eventIn.getEntity();
				if (vexEntity.getOwner() != null && vexEntity.getOwner().getPersistentData().contains("InvasionMob")) {
					vexEntity.setLimitedLife(25 + eventIn.getWorld().getRandom().nextInt(65)); //Attempt to fix lag & spawn camping with vexes
				}
			}
		}

		public static void mobGriefing(EntityMobGriefingEvent eventIn) {
			if (!PSConfigValues.common.explosionsDestroyBlocks && eventIn.getEntity() != null && eventIn.getEntity().getPersistentData().contains("AntiGrief")) {
				eventIn.setResult(Result.DENY);
			}
		}
	}

	public static final class LivingEvents {
		public static void livingConversion(LivingConversionEvent.Post eventIn) { //Needed for occasional bugginess
			if (eventIn.getOutcome().getClassification(false) == MobCategory.MONSTER) {
				CompoundTag persistentData = eventIn.getEntityLiving().getPersistentData();
				CompoundTag outcomeData = eventIn.getOutcome().getPersistentData();
				if (persistentData.contains("InvasionMob"))
					outcomeData.putString("InvasionMob", persistentData.getString("InvasionMob"));
				if (persistentData.contains("AntiGrief"))
					outcomeData.putString("AntiGrief", persistentData.getString("AntiGrief"));
			}
		}

		public static void livingUpdate(LivingEvent.LivingUpdateEvent eventIn) {
			if (eventIn.getEntityLiving() instanceof Mob && eventIn.getEntityLiving().getPersistentData().contains("InvasionMob") && (eventIn.getEntityLiving().getLastHurtByMob() == null || !eventIn.getEntityLiving().getLastHurtByMob().isAlive()) && PSConfigValues.common.hyperAggression && !PSConfigValues.common.hyperAggressionBlacklist.contains(eventIn.getEntityLiving().getType().getRegistryName().toString())) {
				Mob mob = (Mob)eventIn.getEntityLiving();
				if (mob.getTarget() instanceof Player) return;
				Player player = mob.level.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), 144.0D, PSEntityPredicates.HYPER_AGGRESSION);
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

		public static void experienceDrop(LivingExperienceDropEvent eventIn) {
			CompoundTag persistentData = eventIn.getEntityLiving().getPersistentData();
			if (PSConfigValues.common.useXPMultiplier && persistentData.contains("InvasionMob")) {
				ServerLevel serverWorld = (ServerLevel)eventIn.getEntityLiving().level;
				InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverWorld);
				if (iwData != null) {
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(serverWorld, tiwData)) {
							tiwData.setDayXPMultiplier(tiwData.getDayXPMultiplier() + 1);
							double log = Math.log1p(tiwData.getDayXPMultiplier()) / Math.E;
							eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.DAY));
						} else if (ServerTimeUtil.isServerNight(serverWorld, tiwData)) {
							tiwData.setNightXPMultiplier(tiwData.getNightXPMultiplier() + 1);
							double log = Math.log1p(tiwData.getNightXPMultiplier()) / Math.E;
							eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
							PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.NIGHT));
						}
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.setXPMultiplier(fiwData.getXPMultiplier() + 1);
						double log = Math.log1p(fiwData.getXPMultiplier()) / Math.E;
						eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.FIXED));
					}
				}
			}
		}

		public static void checkSpawn(LivingSpawnEvent.CheckSpawn eventIn) {
			if (!eventIn.getWorld().isClientSide()) {
				if (eventIn.getSpawnReason().equals(MobSpawnType.NATURAL)) {
					ServerLevel serverWorld = (ServerLevel)eventIn.getWorld();
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverWorld);
					if (iwData != null) {
						if (serverWorld.random.nextInt(10000) < PSConfigValues.common.naturalSpawnChance) {
							eventIn.setResult(Result.DEFAULT);
						} else {
							if (!iwData.hasFixedTime()) {
								TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
								if ((ServerTimeUtil.isServerDay(serverWorld, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) || (ServerTimeUtil.isServerNight(serverWorld, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()))
									eventIn.setResult(Result.DENY);
							} else {
								FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
								if (!fiwData.getInvasionSpawner().getInvasions().isEmpty())
									eventIn.setResult(Result.DENY);
							}
						}
					}
				}
			}
		}

		public static void specialSpawn(LivingSpawnEvent.SpecialSpawn eventIn) {
			if (!eventIn.getWorld().isClientSide() && eventIn.getEntityLiving().getClassification(false) == MobCategory.MONSTER) {
				if (eventIn.getSpawnReason() == MobSpawnType.NATURAL) {
					ServerLevel serverWorld = (ServerLevel)eventIn.getWorld();
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverWorld);
					if (iwData != null) {
						if (!iwData.hasFixedTime()) {
							TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
							CompoundTag persistentData = eventIn.getEntityLiving().getPersistentData();
							if (!tiwData.getInvasionSpawner().getDayInvasions().isEmpty() || !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
								persistentData.putBoolean("AntiGrief", false);
							}
						} else {
							FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
							if (!fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
								eventIn.getEntityLiving().getPersistentData().putBoolean("AntiGrief", true);
							}
						}
					}
				}
			}
		}

		public static void allowDespawn(LivingSpawnEvent.AllowDespawn eventIn) {
			if (!eventIn.getWorld().isClientSide() && PSConfigValues.common.shouldMobsDieAtEndOfInvasions && eventIn.getEntityLiving() instanceof Mob) {
				ServerLevel serverWorld = (ServerLevel)eventIn.getWorld();
				InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverWorld);
				Mob mobEntity = (Mob)eventIn.getEntityLiving();
				CompoundTag persistentData = mobEntity.getPersistentData();
				if (iwData != null && persistentData.contains("InvasionMob")) {
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(serverWorld, tiwData)) {
							for (Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
								if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
									return;
								}
							}
							eventIn.setResult(Result.ALLOW);
						} else if (ServerTimeUtil.isServerNight(serverWorld, tiwData)) {
							for (Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
								if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
									return;
								}
							}
							eventIn.setResult(Result.ALLOW);
						}
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						for (Invasion invasion : fiwData.getInvasionSpawner().getInvasions()) {
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
		public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent eventIn) {
			updatePlayer(eventIn);
		}

		public static void playerRespawn(PlayerEvent.PlayerRespawnEvent eventIn) {
			if (PSConfigValues.common.hyperAggression)
				eventIn.getEntityLiving().addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), PSConfigValues.common.blessingEffectRespawnDuration, 0));
			updatePlayer(eventIn);
		}

		public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent eventIn) {
			if (PSConfigValues.common.hyperAggression)
				eventIn.getEntityLiving().addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), PSConfigValues.common.blessingEffectDimensionChangeDuration, 0));
			updatePlayer(eventIn);
		}

		private static void updatePlayer(PlayerEvent eventIn) {
			if (eventIn.getPlayer() instanceof ServerPlayer) {
				ServerPlayer player = (ServerPlayer)eventIn.getPlayer();
				InvasionWorldData iwData = InvasionWorldData.getInvasionData().get((ServerLevel)player.level);
				if (iwData != null) {
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getDayInvasions().update(player);
						tiwData.getInvasionSpawner().getNightInvasions().update(player);
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getInvasions().update(player);
					}
				}
				ServerTimeUtil.updateTime(player);
			}
		}

		public static void playerSleepInBed(PlayerSleepInBedEvent eventIn) {
			ServerLevel world = (ServerLevel)eventIn.getPlayer().level;
			InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(world);
			if (iwData != null && !iwData.hasFixedTime()) {
				TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (ServerTimeUtil.isServerDay(world, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
					for (Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
						if (PSConfigValues.common.forceInvasionSleeplessness || invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
							eventIn.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
							return;
						}
					}
				} else if (ServerTimeUtil.isServerNight(world, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
					for (Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
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
		public static void serverStarted(ServerStartedEvent eventIn) {
			if (PSConfigValues.common.multiThreadedInvasions) {
				for (InvasionWorldData iwData : InvasionWorldData.getInvasionData().values()) {
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
					for (InvasionWorldData iwData : InvasionWorldData.getInvasionData().values()) {
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

		public static void serverStarting(ServerStartingEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.common);
			eventIn.getServer().getAllLevels().forEach(level -> {
				boolean hasFixedTime = level.dimensionType().hasFixedTime();
				InvasionWorldData.getInvasionData().put(level, level.getDataStorage().computeIfAbsent(data -> { 
					return hasFixedTime ? FixedInvasionWorldData.load(level, data) : TimedInvasionWorldData.load(level, data);
				}, () -> {
					return hasFixedTime ? new FixedInvasionWorldData(level) : new TimedInvasionWorldData(level);
				}, InvasionWorldData.getFileId(level.dimensionTypeRegistration())));
			});
		}


		public static void serverStopping(ServerStoppingEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.common);
		}
	}
}
