package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

public final class PSServerEvents {
	public static final void serverStarted(final ServerStartedEvent eventIn) {
		if (PSConfigValues.common.multiThreadedInvasions) {
			for (final InvasionWorldData<?> iwData : InvasionWorldData.getInvasionData().values()) {
				eventIn.getServer().addTickable(new Thread(() -> {
					if (!iwData.hasFixedTime()) {
						((TimedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
					} else {
						((FixedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
					}
				}, "Invasion Ticker: " + iwData.getWorld().dimension().location()));
			}
		} else {
			eventIn.getServer().addTickable(new Thread(() -> {
				for (final InvasionWorldData<?> iwData : InvasionWorldData.getInvasionData().values()) {
					if (!iwData.getWorld().players().isEmpty()) {
						if (!iwData.hasFixedTime()) {
							((TimedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
						} else {
							((FixedInvasionWorldData)iwData).getInvasionSpawner().invasionTick(eventIn.getServer(), iwData.getWorld());
						}
					}
				}
			}, "Invasion Ticker"));
		}
	}

	public static final void serverStarting(final ServerStartingEvent eventIn) {
		PSConfigValues.resync(PSConfigValues.common);
		eventIn.getServer().getAllLevels().forEach(level -> {
			final boolean hasFixedTime = level.dimensionType().hasFixedTime();
			InvasionWorldData.getInvasionData().put(level, level.getDataStorage().computeIfAbsent(data -> { 
				return hasFixedTime ? FixedInvasionWorldData.load(level, data) : TimedInvasionWorldData.load(level, data);
			}, () -> {
				return hasFixedTime ? new FixedInvasionWorldData(level) : new TimedInvasionWorldData(level);
			}, InvasionWorldData.getFileId(level.dimensionTypeRegistration())));
		});
	}


	public static final void serverStopping(final ServerStoppingEvent eventIn) {
		PSConfigValues.resync(PSConfigValues.common);
	}
}
