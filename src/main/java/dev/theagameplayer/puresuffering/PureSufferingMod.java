package dev.theagameplayer.puresuffering;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.config.PSConfig;
import dev.theagameplayer.puresuffering.data.InvasionTypesProvider;
import dev.theagameplayer.puresuffering.event.PSClientEvents;
import dev.theagameplayer.puresuffering.event.PSClientSoundEvents;
import dev.theagameplayer.puresuffering.event.PSEntityEvents;
import dev.theagameplayer.puresuffering.event.PSLevelEvents;
import dev.theagameplayer.puresuffering.event.PSLivingEvents;
import dev.theagameplayer.puresuffering.event.PSPlayerEvents;
import dev.theagameplayer.puresuffering.event.PSTickEvents;
import dev.theagameplayer.puresuffering.registries.PSActivities;
import dev.theagameplayer.puresuffering.registries.PSCommandArgumentTypes;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.registries.other.PSCommands;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPlacements;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import dev.theagameplayer.puresuffering.registries.other.PSReloadListeners;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

//TheAGamePlayer was here :>
@Mod(value = PureSufferingMod.MODID)
public final class PureSufferingMod {
	public static final String MODID = "puresuffering";
	public static final String MUSICID = MODID + "music";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@SuppressWarnings("removal")
	public PureSufferingMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		this.registerAll(modEventBus);
		this.createRegistries(modEventBus);
		this.createConfig(modEventBus);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::gatherData);
		if (FMLEnvironment.dist.isClient())
			attachClientEventListeners(modEventBus, MinecraftForge.EVENT_BUS);
		attachCommonEventListeners(modEventBus, MinecraftForge.EVENT_BUS);
	}
	
	public static final ResourceLocation namespace(final String pName) {
		return ResourceLocation.fromNamespaceAndPath(MODID, pName);
	}
	
	private final void createConfig(final IEventBus pModEventBus) {
		PSConfig.initConfig(FMLEnvironment.dist.isClient());
		pModEventBus.addListener(PSConfig::loading);
		pModEventBus.addListener(PSConfig::reloading);
		LOGGER.info("Created mod config.");
	}
	
	private final void createRegistries(final IEventBus pBus) {
		LOGGER.info("Created custom registries.");
	}
	
	private final void registerAll(final IEventBus pBus) {
		PSActivities.ACTIVITY.register(pBus);
		PSCommandArgumentTypes.COMMAND_ARGUMENT_TYPE.register(pBus);
		PSMobEffects.MOB_EFFECT.register(pBus);
		PSSoundEvents.SOUND_EVENT.register(pBus);
		LOGGER.info("Registered all event buses.");
	}
	
	public static final void attachClientEventListeners(final IEventBus pModBus, final IEventBus pForgeBus) {
		//Client
		pModBus.addListener(PSClientEvents::addLayers);
		pForgeBus.addListener(PSClientEvents::loggedIn);
		pForgeBus.addListener(PSClientEvents::loggedOut);
		pForgeBus.addListener(PSClientEvents::debugText);
		pForgeBus.addListener(PSClientEvents::renderLevelStage);
		pForgeBus.addListener(PSClientEvents::screenInitPre);
		pForgeBus.addListener(PSClientEvents::fogColors);
		//Sounds
		pForgeBus.addListener(PSClientSoundEvents::playSound);
		pForgeBus.addListener(PSClientSoundEvents::playSoundSource);
		pForgeBus.addListener(PSClientSoundEvents::playStreamingSource);
		pModBus.addListener(PSClientSoundEvents::soundEngineLoad);
	}

	public static final void attachCommonEventListeners(final IEventBus pModBus, final IEventBus pForgeBus) {
		//Registries
		pModBus.addListener(PSEntityPlacements::registerSpawnPlacements);
		pForgeBus.addListener(PSCommands::registerCommands);
		pForgeBus.addListener(PSReloadListeners::addReloadListeners);
		//Entity
		pForgeBus.addListener(PSEntityEvents::joinLevel);
		pForgeBus.addListener(PSEntityEvents::mobGriefing);
		//Living
		pForgeBus.addListener(PSLivingEvents::finalizeSpawn);
		pForgeBus.addListener(PSLivingEvents::conversionPre);
		pForgeBus.addListener(PSLivingEvents::conversionPost);
		pForgeBus.addListener(PSLivingEvents::death);
		pForgeBus.addListener(PSLivingEvents::experienceDrop);
		pForgeBus.addListener(PSLivingEvents::mobDespawn);
		//pForgeBus.addListener(PSLivingEvents::mobSplit); NOT SUPPORTED BY FORGE ;-;
		//Player
		pForgeBus.addListener(PSPlayerEvents::canPlayerSleep);
		pForgeBus.addListener(PSPlayerEvents::playerLoggedIn);
		pForgeBus.addListener(PSPlayerEvents::playerRespawn);
		pForgeBus.addListener(PSPlayerEvents::playerChangeDimension);
		//Level
		pForgeBus.addListener(PSLevelEvents::explosionStart);
		pForgeBus.addListener(PSLevelEvents::load);
		//Tick
		pForgeBus.addListener(PSTickEvents::levelTickPost);
		pForgeBus.addListener(PSTickEvents::entityTickPost);
	}
	
	private final void commonSetup(final FMLCommonSetupEvent pEvent) {
		PSPackets.registerPackets();
		PSGameRules.registerGameRules();
		LOGGER.info("Finished common setup.");
	}
	
	private final void gatherData(final GatherDataEvent pEvent) {
		final DataGenerator generator = pEvent.getGenerator();
		final CompletableFuture<HolderLookup.Provider> lookupProvider = pEvent.getLookupProvider();
		if (pEvent.includeServer())
			generator.addProvider(true, new InvasionTypesProvider(generator.getPackOutput(), lookupProvider));
		LOGGER.info("Generated new data.");
	}
}
