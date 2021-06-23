package dev.theagameplayer.puresuffering;

import java.util.ArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderHandler;
import dev.theagameplayer.puresuffering.command.PSCommands;
import dev.theagameplayer.puresuffering.coremod.PSCoreModHandler;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionTypeManager;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo.FogType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
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
		public static void fogDensity(FogDensity eventIn) {
			Minecraft mc = Minecraft.getInstance();
			float density = eventIn.getDensity();
			if (mc.level.dimension() == World.OVERWORLD) {
				if (TimeUtil.isDay(mc.level)) {
					ImmutableList<Pair<InvasionType, Integer>> invasionList = ImmutableList.copyOf(InvasionSpawner.getDayInvasions().stream().filter(pair -> {
						return !pair.getLeft().getSkyRenderer().isEmpty() && pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer().isFogDensityChanged();
					}).iterator());
					if (!invasionList.isEmpty())
						eventIn.setCanceled(true);
					for (Pair<InvasionType, Integer> pair : invasionList) {
						float densityOffset = pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer().getDensityOffset();
						density -= MathHelper.clamp(densityOffset, 0.0F, 0.1F) / invasionList.size();
					}
					density = (float)Math.round(density * 1000.0F) / 1000.0F;
				} else if (TimeUtil.isNight(mc.level)) {
					ImmutableList<Pair<InvasionType, Integer>> invasionList = ImmutableList.copyOf(InvasionSpawner.getNightInvasions().stream().filter(pair -> {
						return !pair.getLeft().getSkyRenderer().isEmpty() && pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer().isFogDensityChanged();
					}).iterator());
					if (!invasionList.isEmpty())
						eventIn.setCanceled(true);
					for (Pair<InvasionType, Integer> pair : invasionList) {
						float densityOffset = pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer().getDensityOffset();
						density -= MathHelper.clamp(densityOffset, 0.0F, 0.1F) / invasionList.size();
					}
					density = (float)Math.round(density * 1000.0F) / 1000.0F;
				}
			}
			eventIn.setDensity(density);
		}
		
		public static void fogColors(FogColors eventIn) {
			Minecraft mc = Minecraft.getInstance();
			float red = eventIn.getRed();
			float green = eventIn.getGreen();
			float blue = eventIn.getBlue();
			if (mc.level.dimension() == World.OVERWORLD) {
				if (TimeUtil.isDay(mc.level)) {
					ImmutableList<Pair<InvasionType, Integer>> invasionList = ImmutableList.copyOf(InvasionSpawner.getDayInvasions().stream().filter(pair -> {
						return !pair.getLeft().getSkyRenderer().isEmpty() && pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer().isFogColorChanged();
					}).iterator());
					for (Pair<InvasionType, Integer> pair : invasionList) {
						InvasionFogRenderer fogRenderer = pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer();
						red += fogRenderer.getRedOffset() / invasionList.size();
						green += fogRenderer.getGreenOffset() / invasionList.size();
						blue += fogRenderer.getBlueOffset() / invasionList.size();
					}
				} else if (TimeUtil.isNight(mc.level)) {
					ImmutableList<Pair<InvasionType, Integer>> invasionList = ImmutableList.copyOf(InvasionSpawner.getNightInvasions().stream().filter(pair -> {
						return !pair.getLeft().getSkyRenderer().isEmpty() && pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer().isFogColorChanged();
					}).iterator());
					for (Pair<InvasionType, Integer> pair : invasionList) {
						InvasionFogRenderer fogRenderer = pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getFogRenderer();
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
					if (TimeUtil.isDay(mc.level)) {
						eventIn.getLeft().add(TextFormatting.RED + "[PureSuffering]" + TextFormatting.RESET + " Current Day Invasions: " + InvasionSpawner.getDayInvasions().size());
						return;
					} else if (TimeUtil.isNight(mc.level)) {
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
					if (TimeUtil.isDay(world) && !checkedNight) { //Sets events for night time
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
								for (Pair<InvasionType, Integer> pair : InvasionSpawner.getDayInvasions()) {
									if (!invasionList.contains(pair.getLeft())) {
										if (!invasionList.isEmpty())
											component.append(", ");
										invasionList.add(pair.getLeft());
										component.append(pair.getLeft().getComponent());
									}
								};
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(component.withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)), player.getUUID());
								invasionList.clear();
							}
							for (Pair<InvasionType, Integer> pair : InvasionSpawner.getDayInvasions()) {
								if (pair.getLeft().changesDarkness())
									PSCoreModHandler.LIGHT_INVASIONS.add(pair);
							}
						}
					} else if (TimeUtil.isNight(world) && !checkedDay) { //Sets events for day time
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
								for (Pair<InvasionType, Integer> pair : InvasionSpawner.getNightInvasions()) {
									if (!invasionList.contains(pair.getLeft())) {
										if (!invasionList.isEmpty())
											component.append(", ");
										invasionList.add(pair.getLeft());
										component.append(pair.getLeft().getComponent());
									}
								};
								player.sendMessage(new TranslationTextComponent("invasion.puresuffering.message1").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), player.getUUID());
								player.sendMessage(component.withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)), player.getUUID());
								invasionList.clear();
							}
							for (Pair<InvasionType, Integer> pair : InvasionSpawner.getNightInvasions()) {
								if (pair.getLeft().changesDarkness())
									PSCoreModHandler.LIGHT_INVASIONS.add(pair);
							}
						}
					} else {
						checkedDay = TimeUtil.isNight(world);
						checkedNight = TimeUtil.isDay(world);
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
					if (TimeUtil.isDay(serverWorld) && !InvasionSpawner.getDayInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					} else if (TimeUtil.isNight(serverWorld) && !InvasionSpawner.getNightInvasions().isEmpty()) {
						eventIn.setResult(Result.DENY);
					}
				}
			}
		}
		
		public static void allowDespawn(LivingSpawnEvent.AllowDespawn eventIn) {
			if (eventIn.getWorld() instanceof ServerWorld) {
				ServerWorld serverWorld = (ServerWorld)eventIn.getWorld();
				CompoundNBT persistentData = eventIn.getEntity().getPersistentData();
				if (TimeUtil.isDay(serverWorld) && persistentData.contains("InvasionMob") && pairContainsInvasionType(InvasionSpawner.getDayInvasions(), persistentData)) {
					eventIn.setResult(Result.DENY);
				} else if (TimeUtil.isNight(serverWorld) && persistentData.contains("InvasionMob") && pairContainsInvasionType(InvasionSpawner.getNightInvasions(), persistentData)) {
					eventIn.setResult(Result.DENY);
				} else if (PSConfig.COMMON.shouldMobsDieAtEndOfInvasions.get() && persistentData.contains("InvasionMob")) {
					eventIn.setResult(Result.ALLOW);
				}
			}
		}
		
		private static boolean pairContainsInvasionType(ArrayList<Pair<InvasionType, Integer>> pairListIn, CompoundNBT nbtIn) {
	        for (Pair<InvasionType, Integer> pair : pairListIn) {
	        	if (pair.getLeft() == BaseEvents.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(nbtIn.getString("InvasionMob"))))
	        		return true;
	        }
	        return false;
		}
	}
	
	public static class PlayerEvents {
		public static void playerSleepInBed(PlayerSleepInBedEvent eventIn) {
			ServerWorld world = (ServerWorld)eventIn.getPlayer().level;
			if (TimeUtil.isDay(world) && !InvasionSpawner.getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
				for (Pair<InvasionType, Integer> pair : InvasionSpawner.getDayInvasions()) {
					if (pair.getLeft().forcesNoSleep()) {
						eventIn.setResult(SleepResult.NOT_POSSIBLE_NOW);
						return;
					}
				}
			} else if (TimeUtil.isNight(world) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				for (Pair<InvasionType, Integer> pair : InvasionSpawner.getNightInvasions()) {
					if (pair.getLeft().forcesNoSleep()) {
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
			checkedDay = TimeUtil.isNight(eventIn.getServer().overworld());
			checkedNight = TimeUtil.isDay(eventIn.getServer().overworld());
		}
		
		public static void serverStarted(FMLServerStartedEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedInvasions().clear();
			PSCoreModHandler.LIGHT_INVASIONS.clear();
			eventIn.getServer().addTickable(new Thread(() -> {
				InvasionSpawner.invasionTick(eventIn.getServer());
			}, "Invasion Ticker"));
		}
		
		public static void serverStopping(FMLServerStoppingEvent eventIn) {
			InvasionSpawner.getDayInvasions().clear();
			InvasionSpawner.getNightInvasions().clear();
			InvasionSpawner.getQueuedInvasions().clear();
			PSCoreModHandler.LIGHT_INVASIONS.clear();
			Minecraft mc = Minecraft.getInstance();
			mc.level.effects().setSkyRenderHandler(null);
		}
	}
}
