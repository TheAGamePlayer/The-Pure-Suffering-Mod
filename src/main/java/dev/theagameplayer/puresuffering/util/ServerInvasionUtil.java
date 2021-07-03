package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.InvasionListType;
import net.minecraft.world.World;

public final class ServerInvasionUtil {
	private static final InvasionList LIGHT_INVASIONS = new InvasionList(InvasionListType.LIGHT);

	public static int handleLightLevel(int lightLevelIn, World worldIn) {
		if (worldIn.dimension() == World.OVERWORLD && !LIGHT_INVASIONS.isEmpty()) {
			int lightLevel = 0;
			for (Invasion invasion : LIGHT_INVASIONS) {
				lightLevel += invasion.getType().getLightLevel() / LIGHT_INVASIONS.size();
			}
			return lightLevel;
		}
		return lightLevelIn;
	}
	
	public static InvasionList getLightInvasions() {
		return LIGHT_INVASIONS;
	}
}
