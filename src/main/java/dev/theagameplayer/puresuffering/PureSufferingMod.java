package dev.theagameplayer.puresuffering;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.config.PSConfig;
import dev.theagameplayer.puresuffering.data.InvasionTypesProvider;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.registries.other.PSGameRulesRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

//TheAGamePlayer was here :>
@Mod(value = PureSufferingMod.MODID)
public final class PureSufferingMod {
	public static final String MODID = "puresuffering";
	private static final Logger LOGGER = LogManager.getLogger(MODID);

	public PureSufferingMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		this.createConfig();
		this.registerAll(modEventBus);
		this.createRegistries(modEventBus);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::gatherData);
		if (FMLEnvironment.dist.isClient())
			PSEventManager.attachClientEventListeners(modEventBus, MinecraftForge.EVENT_BUS);
		PSEventManager.attachCommonEventListeners(modEventBus, MinecraftForge.EVENT_BUS);
	}
	
	private void createConfig() {
		PSConfig.initConfig();
		LOGGER.info("Created mod config.");
	}
	
	private void registerAll(IEventBus busIn) {
		PSMobEffects.MOB_EFFECTS.register(busIn);
		LOGGER.info("Registered all event buses.");
	}
	
	private void createRegistries(IEventBus busIn) {
		LOGGER.info("Created custom registries.");
	}
	
	private void commonSetup(final FMLCommonSetupEvent eventIn) {
		PSPacketHandler.registerPackets();
		PSGameRulesRegistry.registerGameRules();
		LOGGER.info("Finished common setup.");
	}
	
	private void clientSetup(final FMLClientSetupEvent eventIn) {
		LOGGER.info("Finished client setup.");
	}
	
	private void gatherData(GatherDataEvent eventIn) {
		DataGenerator generator = eventIn.getGenerator();
		//ExistingFileHelper fileHelper = eventIn.getExistingFileHelper();
		if (eventIn.includeServer()) {
			generator.addProvider(new InvasionTypesProvider(generator));
		}
		LOGGER.info("Generated new data.");
	}
}
