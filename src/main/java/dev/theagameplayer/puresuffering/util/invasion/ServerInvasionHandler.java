package dev.theagameplayer.puresuffering.util.invasion;

import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.level.ServerLevel;

public final class ServerInvasionHandler {
	public static final int handleSkyBrightness(final int lightLevelIn, final ServerLevel levelIn) {
		final InvasionLevelData ilData = InvasionLevelData.get(levelIn);
		if (ilData == null) return lightLevelIn;
		final InvasionSession session = ilData.getInvasionManager().getActiveSession(levelIn);
		if (session == null) return lightLevelIn;
		return session.getLightLevelOrDefault(lightLevelIn);
	}
}
