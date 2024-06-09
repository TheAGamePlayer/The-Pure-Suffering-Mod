package dev.theagameplayer.puresuffering.util.invasion;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class ClientInvasionHandler {
	public static final float handleSkyDarken(final float pResult, final ClientLevel pLevel) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(pLevel);
		if (session == null) return pResult;
		return Mth.clamp(pResult - session.getBrightness(), 0.0F, 1.0F);
	}

	public static final boolean handleBrightLightmap(final boolean pResult, final ClientLevel pLevel) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(pLevel);
		if (session == null) return pResult;
		if (session.getDifficulty().isNightmare()) return false;
		if (session.isBrightnessUnchanged()) return pResult;
		return false;
	}
	
	public static final int handleSkyBrightness(final int pResult, final ClientLevel pLevel) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(pLevel);
		if (session == null) return pResult;
		return session.getLightLevelOrDefault(pResult);
	}
	
	public static final float handleDarknessScale(final float pResult, final ClientLevel pLevel) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(pLevel);
		if (session == null || !session.getDifficulty().isNightmare()) return pResult;
		return Math.max(0.0F, pResult + session.getDarkness());
	}
}
