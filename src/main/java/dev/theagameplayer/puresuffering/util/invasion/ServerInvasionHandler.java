package dev.theagameplayer.puresuffering.util.invasion;

import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ServerInvasionHandler {
	public static final int handleSkyBrightness(final int pLightLevel, final Level pLevel) {
		final ServerLevel level = (ServerLevel)pLevel;
		final InvasionLevelData ilData = InvasionLevelData.get(level);
		if (ilData == null) return pLightLevel;
		final InvasionSession session = ilData.getInvasionManager().getActiveSession(level);
		if (session == null) return pLightLevel;
		return session.getLightLevelOrDefault(pLightLevel);
	}
}
