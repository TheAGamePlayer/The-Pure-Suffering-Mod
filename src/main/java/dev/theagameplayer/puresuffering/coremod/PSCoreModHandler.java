package dev.theagameplayer.puresuffering.coremod;

import java.util.ArrayList;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.world.World;

public final class PSCoreModHandler {
	public static final ArrayList<Invasion> LIGHT_INVASIONS = new ArrayList<>();
	
	public static float clientDayBrightness(float brightnessIn, World worldIn) {
		if (worldIn.dimension() == World.OVERWORLD && !LIGHT_INVASIONS.isEmpty()) {
			float brightness = 0.0F;
			for (Invasion invasion : LIGHT_INVASIONS) {
				brightness += invasion.getType().getBrightness() / LIGHT_INVASIONS.size();
			}
			return brightness;
		}
		return brightnessIn;
	}
	
	public static int serverDayBrightness(int lightLevelIn, World worldIn) {
		if (worldIn.dimension() == World.OVERWORLD && !LIGHT_INVASIONS.isEmpty()) {
			int lightLevel = 0;
			for (Invasion invasion : LIGHT_INVASIONS) {
				lightLevel += invasion.getType().getLightLevel() / LIGHT_INVASIONS.size();
			}
			return lightLevel;
		}
		return lightLevelIn;
	}
}
