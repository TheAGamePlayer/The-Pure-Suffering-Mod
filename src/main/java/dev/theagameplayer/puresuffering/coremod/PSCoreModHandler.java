package dev.theagameplayer.puresuffering.coremod;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.world.World;

public class PSCoreModHandler {
	public static final ArrayList<Pair<InvasionType, Integer>> LIGHT_INVASIONS = new ArrayList<>();
	
	public static float clientDayBrightness(float brightnessIn, World worldIn) {
		if (worldIn.dimension() == World.OVERWORLD && !LIGHT_INVASIONS.isEmpty()) {
			float brightness = 0.0F;
			for (Pair<InvasionType, Integer> pair : LIGHT_INVASIONS) {
				brightness += pair.getLeft().getBrightness() / LIGHT_INVASIONS.size();
			}
			return brightness;
		}
		return brightnessIn;
	}
	
	public static int serverDayBrightness(int lightLevelIn, World worldIn) {
		if (worldIn.dimension() == World.OVERWORLD && !LIGHT_INVASIONS.isEmpty()) {
			int lightLevel = 0;
			for (Pair<InvasionType, Integer> pair : LIGHT_INVASIONS) {
				lightLevel += pair.getLeft().getLightLevel() / LIGHT_INVASIONS.size();
			}
			return lightLevel;
		}
		return lightLevelIn;
	}
}
