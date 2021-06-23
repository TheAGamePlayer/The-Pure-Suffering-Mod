package dev.theagameplayer.puresuffering.util;

import net.minecraft.world.World;

public class TimeUtil {
	public static boolean isDay(World worldIn) {
		return !worldIn.dimensionType().hasFixedTime() && worldIn.getDayTime() % 24000L < 12000L;
	}

	public static boolean isNight(World worldIn) {
		return !worldIn.dimensionType().hasFixedTime() && !isDay(worldIn);
	}
}
