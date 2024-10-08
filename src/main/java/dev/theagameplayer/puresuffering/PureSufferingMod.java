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
import dev.theagameplayer.puresuffering.event.PSServerEvents;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;

//TheAGamePlayer was here :>
@Mod(PureSufferingMod.MODID)
public final class PureSufferingMod {
	public static final String MODID = "puresuffering";
	public static final String MUSICID = MODID + "music";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static ModContainer MC;

	public PureSufferingMod(final ModContainer pModContainer, final IEventBus pModEventBus) {
		this.registerAll(pModEventBus);
		this.createRegistries(pModEventBus);
		this.createConfig(pModContainer, pModEventBus);
		pModEventBus.addListener(this::commonSetup);
		pModEventBus.addListener(this::gatherData);
		if (FMLEnvironment.dist.isClient())
			attachClientEventListeners(pModEventBus, NeoForge.EVENT_BUS);
		attachCommonEventListeners(pModEventBus, NeoForge.EVENT_BUS);
	}
	
	public static final ResourceLocation namespace(final String pName) {
		return ResourceLocation.fromNamespaceAndPath(MODID, pName);
	}
	
	private final void createConfig(final ModContainer pModContainer, final IEventBus pModEventBus) {
		MC = pModContainer;
		final boolean flag = FMLEnvironment.dist.isClient();
		PSConfig.initConfig(pModContainer, flag);
		if (flag) pModContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
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
		pModBus.addListener(PSPackets::registerPackets);
		pModBus.addListener(PSEntityPlacements::registerSpawnPlacements);
		pForgeBus.addListener(PSCommands::registerCommands);
		pForgeBus.addListener(PSReloadListeners::addReloadListeners);
		//Entity
		pForgeBus.addListener(PSEntityEvents::joinLevel);
		pForgeBus.addListener(PSEntityEvents::mobGriefing);
		//Living
		pForgeBus.addListener(PSLivingEvents::conversionPre);
		pForgeBus.addListener(PSLivingEvents::conversionPost);
		pForgeBus.addListener(PSLivingEvents::experienceDrop);
		pForgeBus.addListener(PSLivingEvents::finalizeSpawn);
		pForgeBus.addListener(PSLivingEvents::mobDespawn);
		pForgeBus.addListener(PSLivingEvents::mobSplit);
		//Player
		pForgeBus.addListener(PSPlayerEvents::canPlayerSleep);
		pForgeBus.addListener(PSPlayerEvents::playerLoggedIn);
		pForgeBus.addListener(PSPlayerEvents::playerRespawn);
		pForgeBus.addListener(PSPlayerEvents::playerChangeDimension);
		//Level
		pForgeBus.addListener(PSLevelEvents::explosionStart);
		//Server
		pForgeBus.addListener(PSServerEvents::serverStarting);
		//Tick
		pForgeBus.addListener(PSTickEvents::levelTickPost);
		pForgeBus.addListener(PSTickEvents::entityTickPost);
	}
	
	private final void commonSetup(final FMLCommonSetupEvent pEvent) {
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
