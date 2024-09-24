package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class PSServerEvents {
	public static final void serverStarting(final ServerStartingEvent pEvent) {
		final MinecraftServer server = pEvent.getServer();
		PSConfigValues.resyncCommon();
		for (final ServerLevel level : server.getAllLevels()) {
			PSConfigValues.addLevelValues(level);
			level.getDataStorage().computeIfAbsent(InvasionLevelData.factory(level), InvasionLevelData.getFileId(level.dimensionTypeRegistration()));
		}
	}


	public static final void serverStopping(final ServerStoppingEvent pEvent) {
		PSConfigValues.resyncCommon();
	}
}
