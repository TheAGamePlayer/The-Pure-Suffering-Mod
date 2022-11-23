package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.server.level.ServerLevel;

public final class ServerInvasionUtil {
	public static int handleLightLevel(final int lightLevelIn, final ServerLevel levelIn) {
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(levelIn);
		if (iwData != null) {
			if (!iwData.hasFixedTime()) {
				final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (ServerTimeUtil.isServerDay(levelIn, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) {
					int lightLevel = 0, changed = 0;
					for (final Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
						final int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
						if (amount > -1) {
							lightLevel += amount;
							changed++;
						}
					}
					return changed == 0 ? lightLevelIn : lightLevel/changed;
				} else if (ServerTimeUtil.isServerNight(levelIn, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
					int lightLevel = 0, changed = 0;
					for (final Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
						final int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
						if (amount > -1) {
							lightLevel += amount;
							changed++;
						}
					}
					return changed == 0 ? lightLevelIn : lightLevel/changed;
				}
			} else {
				final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
				if (!fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
					int lightLevel = 0, changed = 0;
					for (final Invasion invasion : fiwData.getInvasionSpawner().getInvasions()) {
						final int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
						if (amount > -1) {
							lightLevel += amount;
							changed++;
						}
					}
					return changed == 0 ? lightLevelIn : lightLevel/changed;
				}
			}
		}
		return lightLevelIn;
	}
}
