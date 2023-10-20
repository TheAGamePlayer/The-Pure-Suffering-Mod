package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfig;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

public final class PSServerEvents {
	public static final void serverStarting(final ServerStartingEvent eventIn) {
		final MinecraftServer server = eventIn.getServer();
		PSConfigValues.resyncCommon();
		server.getAllLevels().forEach(level -> {
			PSConfig.initLevelConfig(level);
			PSConfigValues.addLevelValues(level);
			final InvasionManager invasionManager = level.getDataStorage().computeIfAbsent(data -> {
				return InvasionLevelData.load(level, data);
			}, () -> {
				return new InvasionLevelData(level);
			}, InvasionLevelData.getFileId(level.dimensionTypeRegistration())).getInvasionManager();
			server.addTickable(new Thread(() -> invasionManager.tick(server.isSpawningMonsters(), level), "[" + level.dimension().location() + "] Invasion Ticker"));
		});
	}


	public static final void serverStopping(final ServerStoppingEvent eventIn) {
		PSConfigValues.resyncCommon();
	}
}
