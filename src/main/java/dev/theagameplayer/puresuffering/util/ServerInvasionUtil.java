package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.world.server.ServerWorld;

public final class ServerInvasionUtil {
	public static int handleLightLevel(int lightLevelIn, ServerWorld worldIn) {
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(worldIn);
		if (iwData != null) {
			if (!iwData.hasFixedTime()) {
				TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (ServerTimeUtil.isServerDay(worldIn, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) {
					int lightLevel = 0, changed = 0;
					for (Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
						int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
						if (amount > -1) {
							lightLevel += amount;
							changed++;
						}
					}
					return changed == 0 ? lightLevelIn : lightLevel/changed;
				} else if (ServerTimeUtil.isServerNight(worldIn, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
					int lightLevel = 0, changed = 0;
					for (Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
						int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
						if (amount > -1) {
							lightLevel += amount;
							changed++;
						}
					}
					return changed == 0 ? lightLevelIn : lightLevel/changed;
				}
			} else {
				FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
				if (!fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
					int lightLevel = 0, changed = 0;
					for (Invasion invasion : fiwData.getInvasionSpawner().getInvasions()) {
						int amount = invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getLightLevel();
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
