package dev.theagameplayer.puresuffering;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderHandler;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.coremod.PSCoreModHandler;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionTypeManager;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.TimeUtil;
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
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public final class PSEventManager {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static InvasionTypeManager invasionTypeManager = new InvasionTypeManager();;
	private static int difficultyIncreaseDelay;
	private static boolean checkedDay;
	private static boolean checkedNight;

	public static void attachClientEventListeners(IEventBus modBusIn, IEventBus forgeBusIn) {
		//Client
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
		forgeBusIn.addListener(PlayerEvents::playerSleepInBed);
		//Server
		forgeBusIn.addListener(ServerEvents::serverStarting);
		forgeBusIn.addListener(ServerEvents::serverStarted);
		forgeBusIn.addListener(ServerEvents::serverStopping);
	} 
	
	public static class ClientEvents {
		public static void fogDensity(EntityViewRenderEvent.FogDensity eventIn) {
			Minecraft mc = Minecraft.getInstance();
			float density = eventIn.getDensity();
			if (mc.level.dimension() == World.OVERWORLD) {
				if (TimeUtil.isClientDay()) {
					ImmutableList<Invasion> invasionList = ImmutableList.copyOf(InvasionSpawner.getDayInvasions().stream().filter(invasion -> {
						return !invasion.getType().getSkyRenderer().isEmpty() && invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer().isFogDensityChanged();
					}).iterator());
					if (!invasionList.isEmpty())
						eventIn.setCanceled(true);
					for (Invasion invasion : invasionList) {
						float densityOffset = invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer().getDensityOffset();
						density -= MathHelper.clamp(densityOffset, 0.0F, 0.1F) / invasionList.size();
					}
					density = (float)Math.round(density * 1000.0F) / 1000.0F;
				} else if (TimeUtil.isClientNight()) {
					ImmutableList<Invasion> invasionList = ImmutableList.copyOf(InvasionSpawner.getNightInvasions().stream().filter(invasion -> {
						return !invasion.getType().getSkyRenderer().isEmpty() && invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer().isFogDensityChanged();
					}).iterator());
					if (!invasionList.isEmpty())
						eventIn.setCanceled(true);
					for (Invasion invasion : invasionList) {
						float densityOffset = invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer().getDensityOffset();
						density -= MathHelper.clamp(densityOffset, 0.0F, 0.1F) / invasionList.size();
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
				if (TimeUtil.isClientDay()) {
					ImmutableList<Invasion> invasionList = ImmutableList.copyOf(InvasionSpawner.getDayInvasions().stream().filter(invasion -> {
						return !invasion.getType().getSkyRenderer().isEmpty() && invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer().isFogColorChanged();
					}).iterator());
					for (Invasion invasion : invasionList) {
						InvasionFogRenderer fogRenderer = invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer();
						red += fogRenderer.getRedOffset() / invasionList.size();
						green += fogRenderer.getGreenOffset() / invasionList.size();
						blue += fogRenderer.getBlueOffset() / invasionList.size();
					}
				} else if (TimeUtil.isClientNight()) {
					ImmutableList<Invasion> invasionList = ImmutableList.copyOf(InvasionSpawner.getNightInvasions().stream().filter(invasion -> {
						return !invasion.getType().getSkyRenderer().isEmpty() && invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer().isFogColorChanged();
					}).iterator());
					for (Invasion invasion : invasionList) {
						InvasionFogRenderer fogRenderer = invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getFogRenderer();
						red += fogRenderer.getRedOffset() / invasionList.size();
						green += fogRenderer.getGreenOffset() / invasionList.size();
						blue += fogRenderer.getBlueOffset() / invasionList.size();
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
					if (TimeUtil.isClientDay()) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Day Invasions: " + InvasionSpawner.getDayInvasions().size());
						return;
					} else if (TimeUtil.isClientNight()) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Night Invasions: " + InvasionSpawner.getNightInvasions().size());
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
	
	public static class BaseEvents {
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
					if (TimeUtil.isServerDay(world) && !checkedNight) { //Sets events for night time
						days = world.getDayTime() / 24000L;
						int interval = MathHelper.clamp((int)(world.getDayTime() / (12000L * difficultyIncreaseDelay)) + 1, 0, PSConfig.COMMON.maxDayInvasions.get());
						LOGGER.info("Day: " + days + ", Possible Invasions: " + interval);
						InvasionSpawner.setNightTimeEvents(world, interval);
						PSCoreModHandler.LIGHT_INVASIONS.clear();
						checkedDay = false;
						checkedNight = true;
						if (!InvasionSpawner.getDayInvasions().isEmpty()) {
							int chance = world.random.nextInt(world.random.nextInt((int)(difficultyIncreaseDelay * PSConfig.COMMON.dayChanceMultiplier.get()) + 1) + 1);
							boolean chanceFlag = chance == 0 && InvasionSpawner.getDayInvasions().size() > 1 && PSConfig.COMMON.canDayInvasionsBeCanceled.get();
							if (chanceFlag) {
								InvasionSpawner.getDayInvasions().clear();
							}
							for (ServerPlayerEntity player : world.players()) {
								if (chanceFlag) {
									player.sendMessage(new TranslationTextComponent("invasion.puresuffering.day.cancel").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), player.getUUID());
									continue;
								}
								TranslationTextComponent component = new TranslationTextComponent("invasion.puresuffering.message2");
								ArrayList<InvasionType> invasionList = new ArrayList<>();
								for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
									if (!invasionList.contains(invasion.getType())) {
										if (!invasionList.isEmpty())
											component.append(", ");
										invasionList.add(invasion.getType());
										component.append(invasion.getType().getComponent());
									}
								};
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(component.withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)), player.getUUID());
								invasionList.clear();
							}
							for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
								if (invasion.getType().changesDarkness())
									PSCoreModHandler.LIGHT_INVASIONS.add(invasion);
							}
						}
					} else if (TimeUtil.isServerNight(world) && !checkedDay) { //Sets events for day time
						int interval = MathHelper.clamp((int)(world.getDayTime() / (12000L * difficultyIncreaseDelay)) + 1, 0, PSConfig.COMMON.maxNightInvasions.get());
						InvasionSpawner.setDayTimeEvents(world, interval);
						PSCoreModHandler.LIGHT_INVASIONS.clear();
						checkedDay = true;
						checkedNight = false;
						if (!InvasionSpawner.getNightInvasions().isEmpty()) {
							int chance = world.random.nextInt(world.random.nextInt((int)(difficultyIncreaseDelay * PSConfig.COMMON.nightChanceMultiplier.get()) + 1) + 1);
							boolean chanceFlag = chance == 0 && InvasionSpawner.getNightInvasions().size() > 1 && PSConfig.COMMON.canNightInvasionsBeCanceled.get();
							if (chanceFlag) {
								InvasionSpawner.getNightInvasions().clear();
							}
							for (ServerPlayerEntity player : world.players()) {
								if (chanceFlag) {
									player.sendMessage(new TranslationTextComponent("invasion.puresuffering.night.cancel").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), player.getUUID());
									continue;
								}
								TranslationTextComponent component = new TranslationTextComponent("invasion.puresuffering.message2");
								ArrayList<InvasionType> invasionList = new ArrayList<>();
								for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
									if (!invasionList.contains(invasion.getType())) {
										if (!invasionList.isEmpty())
											component.append(", ");
										invasionList.add(invasion.getType());
										component.append(invasion.getType().getComponent());
									}
								};
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(component.withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)), player.getUUID());
								invasionList.clear();
							}
							for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
								if (invasion.getType().changesDarkness())
									PSCoreModHandler.LIGHT_INVASIONS.add(invasion);
							}
						}
					} else {
						checkedDay = TimeUtil.isServerNight(world);
						checkedNight = TimeUtil.isServerDay(world);
					}
				}
			}
		}
	}
	
	public static class LivingEvents {
		public static void checkSpawn(LivingSpawnEvent.CheckSpawn eventIn) {
			if (eventIn.getWorld() instanceof ServerWorld) {
				if (eventIn.getSpawnReason().equals(SpawnReason.NATURAL)) {
					ServerWorld serverWorld = (ServerWorld)eventIn.getWorld();
					if (TimeUtil.isServerDay(serverWorld) && !InvasionSpawner.getDayInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					} else if (TimeUtil.isServerNight(serverWorld) && !InvasionSpawner.getNightInvasions().isEmpty()) {
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
						eventIn.setResult(Result.DENY);
					} else if (PSConfig.COMMON.shouldMobsDieAtEndOfInvasions.get()) {
						eventIn.setResult(Result.ALLOW);
					}
				}
			}
		}
	}
	
	public static class PlayerEvents {
		public static void playerSleepInBed(PlayerSleepInBedEvent eventIn) {
			ServerWorld world = (ServerWorld)eventIn.getPlayer().level;
			if (TimeUtil.isServerDay(world) && !InvasionSpawner.getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
				for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
					if (invasion.getType().forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			} else if (TimeUtil.isServerNight(world) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
					if (invasion.getType().forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			}
		}
	}
	
	public static class ServerEvents {
		public static void serverStarting(FMLServerStartingEvent eventIn) {
			difficultyIncreaseDelay = PSConfig.COMMON.difficultyIncreaseDelay.get();
			checkedDay = TimeUtil.isServerNight(eventIn.getServer().overworld());
			checkedNight = TimeUtil.isServerDay(eventIn.getServer().overworld());
		}
		
		public static void serverStarted(FMLServerStartedEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedDayInvasions().clear();
			InvasionSpawner.getQueuedNightInvasions().clear();
			PSCoreModHandler.LIGHT_INVASIONS.clear();
			eventIn.getServer().addTickable(new Thread(() -> {
				InvasionSpawner.invasionTick(eventIn.getServer());
			}, "Invasion Ticker"));
		}
		
		public static void serverStopping(FMLServerStoppingEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedDayInvasions().clear();
			InvasionSpawner.getQueuedNightInvasions().clear();
			PSCoreModHandler.LIGHT_INVASIONS.clear();
			Minecraft mc = Minecraft.getInstance();
			mc.level.effects().setSkyRenderHandler(null);
		}
	}
}
