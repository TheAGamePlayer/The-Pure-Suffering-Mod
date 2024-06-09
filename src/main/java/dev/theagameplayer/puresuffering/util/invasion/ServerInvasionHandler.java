package dev.theagameplayer.puresuffering.util.invasion;

import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.level.ServerLevel;

public final class ServerInvasionHandler {
	public static final int handleSkyBrightness(final int pLightLevel, final ServerLevel pLevel) {
		final InvasionLevelData ilData = InvasionLevelData.get(pLevel);
		if (ilData == null) return pLightLevel;
		final InvasionSession session = ilData.getInvasionManager().getActiveSession(pLevel);
		if (session == null) return pLightLevel;
		return session.getLightLevelOrDefault(pLightLevel);
	}
}
