package dev.theagameplayer.puresuffering;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.data.InvasionTypeManager;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRulesRegistry;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import dev.theagameplayer.puresuffering.util.ClientTimeUtil;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo.FogType;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public final class PSEventManager {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static InvasionTypeManager invasionTypeManager = new InvasionTypeManager();
	private static boolean checkedDay, checkedNight;

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
		forgeBusIn.addListener(LivingEvents::experienceDrop);
		forgeBusIn.addListener(LivingEvents::checkSpawn);
		forgeBusIn.addListener(LivingEvents::specialSpawn);
		forgeBusIn.addListener(LivingEvents::allowDespawn);
		//Player
		forgeBusIn.addListener(PlayerEvents::playerLoggedIn);
		forgeBusIn.addListener(PlayerEvents::playerSleepInBed);
		//Server
		forgeBusIn.addListener(ServerEvents::serverStarted);
		forgeBusIn.addListener(ServerEvents::serverStarting);
		forgeBusIn.addListener(ServerEvents::serverStopping);
	} 
	
	public static final class ClientEvents {
		public static int dayInvasionsCount, nightInvasionsCount;
		public static double dayXPMult, nightXPMult;
		
		public static void loggedIn(ClientPlayerNetworkEvent.LoggedInEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.client);
		}
		
		public static void loggedOut(ClientPlayerNetworkEvent.LoggedOutEvent eventIn) {
			ClientInvasionUtil.getDayRenderers().clear();
			ClientInvasionUtil.getNightRenderers().clear();
			PSConfigValues.resync(PSConfigValues.client);
		}
		
		public static void fogColors(EntityViewRenderEvent.FogColors eventIn) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level.dimension() == World.OVERWORLD) {
				float red = 0.0F, green = 0.0F, blue = 0.0F;
				if (ClientTimeUtil.isClientDay() && !ClientInvasionUtil.getDayRenderers().isEmpty()) {
					ArrayList<InvasionSkyRenderer> rendererList = ClientInvasionUtil.getDayRenderers().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					} 
				} else if (ClientTimeUtil.isClientNight() && !ClientInvasionUtil.getNightRenderers().isEmpty()) {
					ArrayList<InvasionSkyRenderer> rendererList = ClientInvasionUtil.getNightRenderers().getRenderersOf(renderer -> {
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
				if (mc.level.dimension() == World.OVERWORLD) {
					if (ClientTimeUtil.isClientDay()) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Day Invasions: " + dayInvasionsCount);
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Day Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? dayXPMult + "x" : "Disabled"));
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Night Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? nightXPMult + "x" : "Disabled"));
						return;
					} else if (ClientTimeUtil.isClientNight()) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Night Invasions: " + nightInvasionsCount);
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Night Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? nightXPMult + "x" : "Disabled"));
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Day Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? dayXPMult + "x" : "Disabled"));
						return;
					}
				}
				eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " ?");
				eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " ?");
				eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " ?");
			}
		}
		
		public static void renderWorldLast(RenderWorldLastEvent eventIn) {
			Minecraft mc = Minecraft.getInstance();
			ClientWorld clientWorld = mc.level;
			if (clientWorld != null && clientWorld.effects().skyType() == FogType.NORMAL && clientWorld.dimension() == World.OVERWORLD && PSConfigValues.client.useSkyBoxRenderer) {
				ISkyRenderHandler skyRenderHandler = clientWorld.effects().getSkyRenderHandler();
				if (!(skyRenderHandler instanceof InvasionSkyRenderHandler)) {
					clientWorld.effects().setSkyRenderHandler(new InvasionSkyRenderHandler(skyRenderHandler));
				}
			}
		}
	}
	
	public static final class BaseEvents {
		private static long days;
		
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
				ServerWorld world = (ServerWorld)eventIn.world;
				if (world == world.getServer().overworld() && PSGameRulesRegistry.getEnableInvasions(world)) {
					if (ServerTimeUtil.isServerDay(world) && !checkedNight) { //Sets events for night time
						days = world.getDayTime() / 24000L;
						int possibleAmount = MathHelper.clamp((int)(world.getDayTime() / (24000L * PSConfigValues.common.nightDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxNightInvasions);
						int chance = world.random.nextInt(world.random.nextInt((int)(PSConfigValues.common.nightDifficultyIncreaseDelay * PSConfigValues.common.nightCancelChanceMultiplier) + 1) + 1);
						boolean cancelFlag = chance == 0 && possibleAmount > 1 && InvasionSpawner.getQueuedNightInvasions().isEmpty() && PSConfigValues.common.canNightInvasionsBeCanceled;
						int amount = cancelFlag ? 0 : possibleAmount;
						LOGGER.info("Day: " + days + ", Possible Invasions: " + amount);
						InvasionSpawner.setNightTimeEvents(world, amount, days);
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, true));
						LivingEvents.dayXPMultiplier = 0.0D;
						checkedDay = false;
						checkedNight = true;
						if (!InvasionSpawner.getDayInvasions().isEmpty()) {
							for (ServerPlayerEntity player : world.players()) {
								if (cancelFlag) {
									player.sendMessage(new TranslationTextComponent("invasion.puresuffering.day.cancel").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), player.getUUID());
									continue;
								}
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", InvasionSpawner.getDayInvasions()).withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)), player.getUUID());
							}
						}
					} else if (ServerTimeUtil.isServerNight(world) && !checkedDay) { //Sets events for day time
						days = world.getDayTime() / 24000L;
						int possibleAmount = MathHelper.clamp((int)(world.getDayTime() / (24000L * PSConfigValues.common.dayDifficultyIncreaseDelay)) + 1, 0, PSConfigValues.common.maxDayInvasions);
						int chance = world.random.nextInt(world.random.nextInt((int)(PSConfigValues.common.dayDifficultyIncreaseDelay * PSConfigValues.common.dayCancelChanceMultiplier) + 1) + 1);
						boolean cancelFlag = chance == 0 && possibleAmount > 1 && InvasionSpawner.getQueuedDayInvasions().isEmpty() && PSConfigValues.common.canDayInvasionsBeCanceled;
						int amount = cancelFlag ? 0 : possibleAmount;
						LOGGER.info("Night: " + days + ", Possible Invasions: " + amount);
						InvasionSpawner.setDayTimeEvents(world, amount, days);
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(0.0D, false));
						LivingEvents.nightXPMultiplier = 0.0D;
						checkedDay = true;
						checkedNight = false;
						if (!InvasionSpawner.getNightInvasions().isEmpty()) {
							for (ServerPlayerEntity player : world.players()) {
								if (cancelFlag) {
									player.sendMessage(new TranslationTextComponent("invasion.puresuffering.night.cancel").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), player.getUUID());
									continue;
								}
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", InvasionSpawner.getNightInvasions()).withStyle(TextFormatting.DARK_RED), player.getUUID());
							}
						}
					} else {
						checkedDay = ServerTimeUtil.isServerNight(world);
						checkedNight = ServerTimeUtil.isServerDay(world);
					}
				}
			}
		}
	}
	
	public static final class EntityEvents {
		public static void joinWorld(EntityJoinWorldEvent eventIn) {
			if (eventIn.getEntity() instanceof TameableEntity) {
				TameableEntity tameableEntity = (TameableEntity)eventIn.getEntity();
				if (tameableEntity.getOwner() != null && tameableEntity.getOwner().getPersistentData().contains("AntiGrief")) {
					tameableEntity.getPersistentData().putBoolean("AntiGrief", tameableEntity.getOwner().getPersistentData().getBoolean("AntiGrief"));
				}
			} else if (PSConfigValues.common.weakenedVexes && eventIn.getEntity() instanceof VexEntity) {
				VexEntity vexEntity = (VexEntity)eventIn.getEntity();
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
		public static double dayXPMultiplier = 0.0D;
		public static double nightXPMultiplier = 0.0D;
		
		public static void experienceDrop(LivingExperienceDropEvent eventIn) {
			CompoundNBT persistentData = eventIn.getEntityLiving().getPersistentData();
			if (PSConfigValues.common.useXPMultiplier && persistentData.contains("InvasionMob") && persistentData.contains("AntiGrief")) {
				if (persistentData.getBoolean("AntiGrief")) {
					dayXPMultiplier++;
					double log = Math.log1p(dayXPMultiplier) / Math.E;
					eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
					PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, true));
				} else {
					nightXPMultiplier++;
					double log = Math.log1p(nightXPMultiplier) / Math.E;
					eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
					PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, false));
				}
			}
		}
		
		public static void checkSpawn(LivingSpawnEvent.CheckSpawn eventIn) {
			if (eventIn.getWorld() instanceof ServerWorld) {
				if (eventIn.getSpawnReason().equals(SpawnReason.NATURAL)) {
					ServerWorld serverWorld = (ServerWorld)eventIn.getWorld();
					if (serverWorld.random.nextInt(10000) < PSConfigValues.common.naturalSpawnChance) {
						eventIn.setResult(Result.DEFAULT);
					} else if (ServerTimeUtil.isServerDay(serverWorld) && !InvasionSpawner.getDayInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					} else if (ServerTimeUtil.isServerNight(serverWorld) && !InvasionSpawner.getNightInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					}
				}
			}
		}
		
		public static void specialSpawn(LivingSpawnEvent.SpecialSpawn eventIn) {
			if (eventIn.getWorld() instanceof ServerWorld && eventIn.getEntityLiving().getClassification(false) == EntityClassification.MONSTER) {
				if (eventIn.getSpawnReason() == SpawnReason.NATURAL) {
					ServerWorld serverWorld = (ServerWorld)eventIn.getWorld();
					CompoundNBT persistentData = eventIn.getEntityLiving().getPersistentData();
					if (!InvasionSpawner.getDayInvasions().isEmpty() && ServerTimeUtil.isServerDay(serverWorld)) {
						persistentData.putBoolean("AntiGrief", false);
					} else if (!InvasionSpawner.getNightInvasions().isEmpty() && ServerTimeUtil.isServerNight(serverWorld)) {
						persistentData.putBoolean("AntiGrief", false);
					}
				}
			}
		}
		
		public static void allowDespawn(LivingSpawnEvent.AllowDespawn eventIn) {
			if (PSConfigValues.common.shouldMobsDieAtEndOfInvasions && eventIn.getWorld() instanceof ServerWorld && eventIn.getEntityLiving() instanceof MobEntity) {
				ServerWorld serverWorld = (ServerWorld)eventIn.getWorld();
				MobEntity mobEntity = (MobEntity)eventIn.getEntityLiving();
				CompoundNBT persistentData = mobEntity.getPersistentData();
				if (persistentData.contains("InvasionMob")) {
					if (ServerTimeUtil.isServerDay(serverWorld)) {
						for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
							if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
								return;
							}
						}
						eventIn.setResult(Result.ALLOW);
					} else if (ServerTimeUtil.isServerNight(serverWorld)) {
						for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
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
			if (eventIn.getPlayer() instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity)eventIn.getPlayer();
				InvasionSpawner.getDayInvasions().update(player);
				InvasionSpawner.getNightInvasions().update(player);
				ServerTimeUtil.updateTime(player);
			}
		}
		
		public static void playerSleepInBed(PlayerSleepInBedEvent eventIn) {
			ServerWorld world = (ServerWorld)eventIn.getPlayer().level;
			if (ServerTimeUtil.isServerDay(world) && !InvasionSpawner.getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
				for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
					if (invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			} else if (ServerTimeUtil.isServerNight(world) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
					if (invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			}
		}
	}
	
	public static final class ServerEvents {
		public static void serverStarted(FMLServerStartedEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedDayInvasions().clear();
			InvasionSpawner.getQueuedNightInvasions().clear();
			eventIn.getServer().addTickable(new Thread(() -> {
				InvasionSpawner.invasionTick(eventIn.getServer());
			}, "Invasion Ticker"));
		}
		
		public static void serverStarting(FMLServerStartingEvent eventIn) {
			PSConfigValues.resync(PSConfigValues.common);
			checkedDay = ServerTimeUtil.isServerNight(eventIn.getServer().overworld());
			checkedNight = ServerTimeUtil.isServerDay(eventIn.getServer().overworld());
		}
		
		
		public static void serverStopping(FMLServerStoppingEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedDayInvasions().clear();
			InvasionSpawner.getQueuedNightInvasions().clear();
			PSConfigValues.resync(PSConfigValues.common);
		}
	}
}
