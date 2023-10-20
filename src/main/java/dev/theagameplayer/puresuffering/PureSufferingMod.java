package dev.theagameplayer.puresuffering;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.config.PSConfig;
import dev.theagameplayer.puresuffering.data.InvasionTypesProvider;
import dev.theagameplayer.puresuffering.event.PSBaseEvents;
import dev.theagameplayer.puresuffering.event.PSClientEvents;
import dev.theagameplayer.puresuffering.event.PSClientSoundEvents;
import dev.theagameplayer.puresuffering.event.PSEntityEvents;
import dev.theagameplayer.puresuffering.event.PSLevelEvents;
import dev.theagameplayer.puresuffering.event.PSLivingEvents;
import dev.theagameplayer.puresuffering.event.PSPlayerEvents;
import dev.theagameplayer.puresuffering.event.PSServerEvents;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.registries.PSActivities;
import dev.theagameplayer.puresuffering.registries.PSCommandArgumentTypes;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.registries.other.PSCommands;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPlacements;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSReloadListeners;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

//TheAGamePlayer was here :>
@Mod(value = PureSufferingMod.MODID)
public final class PureSufferingMod {
	public static final String MODID = "puresuffering";
	public static final String MUSICID = MODID + "music";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public PureSufferingMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		this.registerAll(modEventBus);
		this.createRegistries(modEventBus);
		this.createConfig();
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::gatherData);
		if (FMLEnvironment.dist.isClient())
			attachClientEventListeners(modEventBus, MinecraftForge.EVENT_BUS);
		attachCommonEventListeners(modEventBus, MinecraftForge.EVENT_BUS);
	}
	
	public static final ResourceLocation namespace(final String nameIn) {
		return new ResourceLocation(MODID, nameIn);
	}
	
	private final void createConfig() {
		PSConfig.initConfig(FMLEnvironment.dist.isClient());
		LOGGER.info("Created mod config.");
	}
	
	private final void createRegistries(final IEventBus busIn) {
		LOGGER.info("Created custom registries.");
	}
	
	private final void registerAll(final IEventBus busIn) {
		PSActivities.ACTIVITIES.register(busIn);
		PSCommandArgumentTypes.COMMAND_ARGUMENT_TYPES.register(busIn);
		PSMobEffects.MOB_EFFECTS.register(busIn);
		PSSoundEvents.SOUND_EVENTS.register(busIn);
		LOGGER.info("Registered all event buses.");
	}
	
	public static final void attachClientEventListeners(final IEventBus modBusIn, final IEventBus forgeBusIn) {
		//Client
		modBusIn.addListener(PSClientEvents::addLayers);
		forgeBusIn.addListener(PSClientEvents::loggedIn);
		forgeBusIn.addListener(PSClientEvents::loggedOut);
		forgeBusIn.addListener(PSClientEvents::debugText);
		forgeBusIn.addListener(PSClientEvents::renderLevelStage);
		forgeBusIn.addListener(PSClientEvents::screenInitPre);
		forgeBusIn.addListener(PSClientEvents::fogColors);
		//Sounds
		forgeBusIn.addListener(PSClientSoundEvents::playSound);
		forgeBusIn.addListener(PSClientSoundEvents::playSoundSource);
		forgeBusIn.addListener(PSClientSoundEvents::playStreamingSource);
	}

	public static final void attachCommonEventListeners(final IEventBus modBusIn, final IEventBus forgeBusIn) {
		//Registries
		modBusIn.addListener(PSEntityPlacements::registerSpawnPlacements);
		forgeBusIn.addListener(PSCommands::registerCommands);
		forgeBusIn.addListener(PSReloadListeners::addReloadListeners);
		//Base
		forgeBusIn.addListener(PSBaseEvents::levelTick);
		//Entity
		forgeBusIn.addListener(PSEntityEvents::joinLevel);
		forgeBusIn.addListener(PSEntityEvents::mobGriefing);
		//Living
		forgeBusIn.addListener(PSLivingEvents::conversionPre);
		forgeBusIn.addListener(PSLivingEvents::conversionPost);
		forgeBusIn.addListener(PSLivingEvents::livingTick);
		forgeBusIn.addListener(PSLivingEvents::experienceDrop);
		forgeBusIn.addListener(PSLivingEvents::finalizeSpawn);
		forgeBusIn.addListener(PSLivingEvents::allowDespawn);
		//Player
		forgeBusIn.addListener(PSPlayerEvents::playerLoggedIn);
		forgeBusIn.addListener(PSPlayerEvents::playerRespawn);
		forgeBusIn.addListener(PSPlayerEvents::playerChangeDimension);
		forgeBusIn.addListener(PSPlayerEvents::playerSleepInBed);
		//Level
		forgeBusIn.addListener(PSLevelEvents::explosionStart);
		//Server
		forgeBusIn.addListener(PSServerEvents::serverStarting);
		forgeBusIn.addListener(PSServerEvents::serverStopping);
	}
	
	private final void commonSetup(final FMLCommonSetupEvent eventIn) {
		PSPacketHandler.registerPackets();
		PSGameRules.registerGameRules();
		LOGGER.info("Finished common setup.");
	}
	
	private final void clientSetup(final FMLClientSetupEvent eventIn) {
		InvasionMusicManager.addMusic(true);
		LOGGER.info("Finished client setup.");
	}
	
	private final void gatherData(final GatherDataEvent eventIn) {
		final DataGenerator generator = eventIn.getGenerator();
		final CompletableFuture<HolderLookup.Provider> lookupProvider = eventIn.getLookupProvider();
		if (eventIn.includeServer())
			generator.addProvider(true, new InvasionTypesProvider(generator.getPackOutput(), lookupProvider));
		LOGGER.info("Generated new data.");
	}
}
