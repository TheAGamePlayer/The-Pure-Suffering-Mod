package dev.theagameplayer.puresuffering;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionTypeManager;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import dev.theagameplayer.puresuffering.util.ClientTimeUtil;
import dev.theagameplayer.puresuffering.util.ServerInvasionUtil;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import dev.theagameplayer.puresuffering.util.text.InvasionText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo.FogType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
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
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public final class PSEventManager {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static InvasionTypeManager invasionTypeManager = new InvasionTypeManager();;
	private static boolean checkedDay;
	private static boolean checkedNight;

	public static void attachClientEventListeners(IEventBus modBusIn, IEventBus forgeBusIn) {
		//Client
		forgeBusIn.addListener(ClientEvents::loggedOut);
		forgeBusIn.addListener(ClientEvents::fogDensity);
		forgeBusIn.addListener(ClientEvents::fogColors);
		forgeBusIn.addListener(ClientEvents::renderGameOverlayText);
		forgeBusIn.addListener(ClientEvents::renderWorldLast);
	}
	
	public static void attachCommonEventListeners(IEventBus modBusIn, IEventBus forgeBusIn) {
		//Base
		forgeBusIn.addListener(BaseEvents::addReloadListeners);
		forgeBusIn.addListener(BaseEvents::registerCommands);
		forgeBusIn.addListener(BaseEvents::worldTick);
		//Living
		forgeBusIn.addListener(LivingEvents::checkSpawn);
		forgeBusIn.addListener(LivingEvents::allowDespawn);
		//Player
		forgeBusIn.addListener(PlayerEvents::playerLoggedIn);
		forgeBusIn.addListener(PlayerEvents::playerSleepInBed);
		//World
		forgeBusIn.addListener(WorldEvents::explosionDetonation);
		//Server
		forgeBusIn.addListener(ServerEvents::serverStarting);
		forgeBusIn.addListener(ServerEvents::serverStarted);
		forgeBusIn.addListener(ServerEvents::serverStopping);
	} 
	
	public static final class ClientEvents {
		public static int dayInvasionsCount;
		public static int nightInvasionsCount;
		
		public static void loggedOut(ClientPlayerNetworkEvent.LoggedOutEvent eventIn) {
			ClientInvasionUtil.getDayRenderers().clear();
			ClientInvasionUtil.getNightRenderers().clear();
			ClientInvasionUtil.getLightRenderers().clear();
		}
		
		public static void fogDensity(EntityViewRenderEvent.FogDensity eventIn) {
			Minecraft mc = Minecraft.getInstance();
			float density = eventIn.getDensity();
			if (mc.level.dimension() == World.OVERWORLD) {
				if (ClientTimeUtil.isClientDay()) {
					ImmutableList<InvasionSkyRenderer> rendererList = ImmutableList.copyOf(ClientInvasionUtil.getDayRenderers().stream().filter(renderer -> {
						return renderer.getFogRenderer().isFogDensityChanged();
					}).iterator());
					if (!rendererList.isEmpty())
						eventIn.setCanceled(true);
					for (InvasionSkyRenderer renderer : rendererList) {
						float densityOffset = renderer.getFogRenderer().getDensityOffset();
						density -= MathHelper.clamp(densityOffset, 0.0F, 0.1F) / rendererList.size();
					}
					density = (float)Math.round(density * 1000.0F) / 1000.0F;
				} else if (ClientTimeUtil.isClientNight()) {
					ImmutableList<InvasionSkyRenderer> rendererList = ImmutableList.copyOf(ClientInvasionUtil.getNightRenderers().stream().filter(renderer -> {
						return renderer.getFogRenderer().isFogDensityChanged();
					}).iterator());
					if (!rendererList.isEmpty())
						eventIn.setCanceled(true);
					for (InvasionSkyRenderer renderer : rendererList) {
						float densityOffset = renderer.getFogRenderer().getDensityOffset();
						density -= MathHelper.clamp(densityOffset, 0.0F, 0.1F) / rendererList.size();
					}
					density = (float)Math.round(density * 1000.0F) / 1000.0F;
				}
			}
			eventIn.setDensity(density);
		}
		
		public static void fogColors(EntityViewRenderEvent.FogColors eventIn) {
			Minecraft mc = Minecraft.getInstance();
			float red = eventIn.getRed();
			float green = eventIn.getGreen();
			float blue = eventIn.getBlue();
			if (mc.level.dimension() == World.OVERWORLD) {
				if (ClientTimeUtil.isClientDay()) {
					ImmutableList<InvasionSkyRenderer> rendererList = ImmutableList.copyOf(ClientInvasionUtil.getDayRenderers().stream().filter(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					}).iterator());
					for (InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				} else if (ClientTimeUtil.isClientNight()) {
					ImmutableList<InvasionSkyRenderer> rendererList = ImmutableList.copyOf(ClientInvasionUtil.getNightRenderers().stream().filter(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					}).iterator());
					for (InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				}
			}
			eventIn.setRed(red);
			eventIn.setGreen(green);
			eventIn.setBlue(blue);
		}
		
		public static void renderGameOverlayText(RenderGameOverlayEvent.Text eventIn) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.renderDebug) {
				eventIn.getLeft().add("");
				if (mc.level.dimension() == World.OVERWORLD) {
					if (ClientTimeUtil.isClientDay()) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Day Invasions: " + dayInvasionsCount);
						return;
					} else if (ClientTimeUtil.isClientNight()) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Night Invasions: " + nightInvasionsCount);
						return;
					}
				}
				eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " ?");
			}
		}
		
		public static void renderWorldLast(RenderWorldLastEvent eventIn) {
			Minecraft mc = Minecraft.getInstance();
			ClientWorld clientWorld = mc.level;
			if (clientWorld != null && clientWorld.effects().skyType() == FogType.NORMAL && clientWorld.dimension() == World.OVERWORLD && PSConfig.CLIENT.useSkyBoxRenderer.get()) {
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
				if (world == world.getServer().overworld()) {
					if (ServerTimeUtil.isServerDay(world) && !checkedNight) { //Sets events for night time
						days = world.getDayTime() / 24000L;
						int interval = MathHelper.clamp((int)(world.getDayTime() / (24000L * PSConfig.COMMON.nightDifficultyIncreaseDelay.get())) + 1, 0, PSConfig.COMMON.maxNightInvasions.get());
						LOGGER.info("Day: " + days + ", Possible Invasions: " + interval);
						ServerInvasionUtil.getLightInvasions().clear();
						InvasionSpawner.setNightTimeEvents(world, interval);
						checkedDay = false;
						checkedNight = true;
						if (!InvasionSpawner.getDayInvasions().isEmpty()) {
							int chance = world.random.nextInt(world.random.nextInt((int)(PSConfig.COMMON.dayDifficultyIncreaseDelay.get() * PSConfig.COMMON.dayChanceMultiplier.get()) + 1) + 1);
							boolean cancelFlag = chance == 0 && InvasionSpawner.getDayInvasions().size() > 1 && PSConfig.COMMON.canDayInvasionsBeCanceled.get();
							if (cancelFlag) {
								InvasionSpawner.getDayInvasions().clear();
							}
							for (ServerPlayerEntity player : world.players()) {
								if (cancelFlag) {
									player.sendMessage(new TranslationTextComponent("invasion.puresuffering.day.cancel").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), player.getUUID());
									continue;
								}
								HashMap<InvasionType, InvasionText> invasionMap = new HashMap<>();
								for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
									if (!invasionMap.containsKey(invasion.getType())) {
										invasionMap.put(invasion.getType(), new InvasionText(invasion.getSeverity()));
									} else if (invasionMap.get(invasion.getType()).getSeverity() < invasion.getSeverity()) {
										invasionMap.get(invasion.getType()).setSeverity(invasion.getSeverity());
									}
									invasionMap.get(invasion.getType()).incrementAmount();
								};
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", invasionMap).withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)), player.getUUID());
								invasionMap.clear();
							}
							for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
								if (invasion.getType().getLightLevel() != 0) {
									ServerInvasionUtil.getLightInvasions().add(invasion);
								}
							}
						}
					} else if (ServerTimeUtil.isServerNight(world) && !checkedDay) { //Sets events for day time
						int interval = MathHelper.clamp((int)(world.getDayTime() / (24000L * PSConfig.COMMON.dayDifficultyIncreaseDelay.get())) + 1, 0, PSConfig.COMMON.maxDayInvasions.get());
						ServerInvasionUtil.getLightInvasions().clear();
						InvasionSpawner.setDayTimeEvents(world, interval);
						checkedDay = true;
						checkedNight = false;
						if (!InvasionSpawner.getNightInvasions().isEmpty()) {
							int chance = world.random.nextInt(world.random.nextInt((int)(PSConfig.COMMON.nightDifficultyIncreaseDelay.get() * PSConfig.COMMON.nightChanceMultiplier.get()) + 1) + 1);
							boolean cancelFlag = chance == 0 && InvasionSpawner.getNightInvasions().size() > 1 && PSConfig.COMMON.canNightInvasionsBeCanceled.get();
							if (cancelFlag) {
								InvasionSpawner.getNightInvasions().clear();
							}
							for (ServerPlayerEntity player : world.players()) {
								if (cancelFlag) {
									player.sendMessage(new TranslationTextComponent("invasion.puresuffering.night.cancel").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), player.getUUID());
									continue;
								}
								HashMap<InvasionType, InvasionText> invasionMap = new HashMap<>();
								for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
									if (!invasionMap.containsKey(invasion.getType())) {
										invasionMap.put(invasion.getType(), new InvasionText(invasion.getSeverity()));
									} else if (invasionMap.get(invasion.getType()).getSeverity() < invasion.getSeverity()) {
										invasionMap.get(invasion.getType()).setSeverity(invasion.getSeverity());
									}
									invasionMap.get(invasion.getType()).incrementAmount();
								};
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(new InvasionListTextComponent("invasion.puresuffering.message2", invasionMap).withStyle(TextFormatting.DARK_RED), player.getUUID());
								invasionMap.clear();
							}
							for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
								if (invasion.getType().getLightLevel() != 0) {
									ServerInvasionUtil.getLightInvasions().add(invasion);
								}
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
	
	public static final class LivingEvents {
		public static void checkSpawn(LivingSpawnEvent.CheckSpawn eventIn) {
			if (eventIn.getWorld() instanceof ServerWorld) {
				if (eventIn.getSpawnReason().equals(SpawnReason.NATURAL)) {
					ServerWorld serverWorld = (ServerWorld)eventIn.getWorld();
					if (serverWorld.random.nextInt(100) < PSConfig.COMMON.naturalSpawnChance.get()) {
						eventIn.setResult(Result.DEFAULT);
					} else if (ServerTimeUtil.isServerDay(serverWorld) && !InvasionSpawner.getDayInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					} else if (ServerTimeUtil.isServerNight(serverWorld) && !InvasionSpawner.getNightInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					}
				}
			}
		}
		
		public static void allowDespawn(LivingSpawnEvent.AllowDespawn eventIn) {
			if (eventIn.getWorld() instanceof ServerWorld && eventIn.getEntityLiving() instanceof MobEntity) {
				MobEntity mobEntity = (MobEntity)eventIn.getEntityLiving();
				CompoundNBT persistentData = mobEntity.getPersistentData();
				if (persistentData.contains("InvasionMob")) {
					if (Invasion.INVASION_MOBS.contains(mobEntity)) {
						eventIn.setResult(Result.DEFAULT);
					} else if (PSConfig.COMMON.shouldMobsDieAtEndOfInvasions.get()) {
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
				ServerInvasionUtil.getLightInvasions().update(player);
				ServerTimeUtil.updateTime(player);
			}
		}
		
		public static void playerSleepInBed(PlayerSleepInBedEvent eventIn) {
			ServerWorld world = (ServerWorld)eventIn.getPlayer().level;
			if (ServerTimeUtil.isServerDay(world) && !InvasionSpawner.getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
				for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
					if (invasion.getType().forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			} else if (ServerTimeUtil.isServerNight(world) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
					if (invasion.getType().forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			}
		}
	}
	
	public static final class WorldEvents {
		public static void explosionDetonation(ExplosionEvent.Detonate eventIn) {
			if (!PSConfig.COMMON.explosionsDestroyBlocks.get()) {
				if (eventIn.getExplosion().getSourceMob().getPersistentData().contains("InvasionMob")) {
					eventIn.getAffectedBlocks().clear();
				}
			}
		}
	}
	
	public static final class ServerEvents {
		public static void serverStarting(FMLServerStartingEvent eventIn) {
			checkedDay = ServerTimeUtil.isServerNight(eventIn.getServer().overworld());
			checkedNight = ServerTimeUtil.isServerDay(eventIn.getServer().overworld());
		}
		
		public static void serverStarted(FMLServerStartedEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedDayInvasions().clear();
			InvasionSpawner.getQueuedNightInvasions().clear();
			ServerInvasionUtil.getLightInvasions().clear();
			eventIn.getServer().addTickable(new Thread(() -> {
				InvasionSpawner.invasionTick(eventIn.getServer());
			}, "Invasion Ticker"));
		}
		
		public static void serverStopping(FMLServerStoppingEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedDayInvasions().clear();
			InvasionSpawner.getQueuedNightInvasions().clear();
			ServerInvasionUtil.getLightInvasions().clear();
		}
	}
}
