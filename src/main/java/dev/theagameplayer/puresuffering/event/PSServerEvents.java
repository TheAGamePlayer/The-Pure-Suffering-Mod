package dev.theagameplayer.puresuffering.event;

import java.util.ArrayList;

import dev.theagameplayer.puresuffering.config.PSConfig;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class PSServerEvents {
	private static volatile Thread[] invasionTicker;
	
	public static final void serverStarting(final ServerStartingEvent pEvent) {
		final MinecraftServer server = pEvent.getServer();
		final ArrayList<ResourceKey<Level>> levels = new ArrayList<>(server.levelKeys());
		PSConfigValues.resyncCommon();
		invasionTicker = new Thread[levels.size()];
		for (int l = 0; l < levels.size(); l++) {
			final ServerLevel level = server.getLevel(levels.get(l));
			PSConfig.initLevelConfig(level);
			PSConfigValues.addLevelValues(level);
			final InvasionManager invasionManager = level.getDataStorage().computeIfAbsent(InvasionLevelData.factory(level), InvasionLevelData.getFileId(level.dimensionTypeRegistration())).getInvasionManager();
			invasionTicker[l] = new Thread(() -> invasionManager.tick(server.isSpawningMonsters(), level), "[" + level.dimension().location() + "] Invasion Ticker");
			server.addTickable(invasionTicker[l]);
		}
	}


	public static final void serverStopping(final ServerStoppingEvent pEvent) {
		PSConfigValues.resyncCommon();
		for (final Thread thread : invasionTicker)
			thread.interrupt();
	}
}
