package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import net.minecraft.world.server.ServerWorld;

public final class ServerInvasionUtil {
	public static int handleLightLevel(int lightLevelIn, ServerWorld worldIn) {
		if (ServerTimeUtil.isServerDay(worldIn) && !InvasionSpawner.getDayInvasions().isEmpty()) {
			int lightLevel = 0, changed = 0;
			for (Invasion invasion : InvasionSpawner.getDayInvasions()) {
				int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
				if (amount > -1) {
					lightLevel += amount;
					changed++;
				}
			}
			return changed == 0 ? lightLevelIn : lightLevel/changed;
		} else if (ServerTimeUtil.isServerNight(worldIn) && !InvasionSpawner.getNightInvasions().isEmpty()) {
			int lightLevel = 0, changed = 0;
			for (Invasion invasion : InvasionSpawner.getNightInvasions()) {
				int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
				if (amount > -1) {
					lightLevel += amount;
					changed++;
				}
			}
			return changed == 0 ? lightLevelIn : lightLevel/changed;
		}
		return lightLevelIn;
	}
}
