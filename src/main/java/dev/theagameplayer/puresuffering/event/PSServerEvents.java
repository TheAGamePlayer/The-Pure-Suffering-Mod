package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraftforge.event.server.ServerStartingEvent;

public final class PSServerEvents {
	public static final void serverStarting(final ServerStartingEvent pEvent) {
		final MinecraftServer server = pEvent.getServer();
		final Registry<LevelStem> registry = server.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
		for (final ServerLevel level : server.getAllLevels()) {
			if (registry.getOptional(level.dimension().location()).isEmpty()) continue;
			PSConfigValues.addLevelValues(level);
			InvasionLevelData.factory(level);
		}
	}
}
