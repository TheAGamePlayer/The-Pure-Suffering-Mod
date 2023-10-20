package dev.theagameplayer.puresuffering.util.invasion;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class ClientInvasionHandler {
	public static final float handleSkyDarken(final float resultIn, final ClientLevel levelIn) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(levelIn);
		if (session == null) return resultIn;
		return Mth.clamp(resultIn - session.getBrightness(), 0.0F, 1.0F);
	}

	public static final boolean handleBrightLightmap(final boolean resultIn, final ClientLevel levelIn) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(levelIn);
		if (session == null) return resultIn;
		if (session.getDifficulty().isNightmare()) return false;
		if (session.isBrightnessUnchanged()) return resultIn;
		return false;
	}
	
	public static final int handleSkyBrightness(final int resultIn, final ClientLevel levelIn) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(levelIn);
		if (session == null) return resultIn;
		return session.getLightLevelOrDefault(resultIn);
	}
	
	public static final float handleDarknessScale(final float resultIn, final ClientLevel levelIn) { //Render Tick
		final ClientInvasionSession session = ClientInvasionSession.get(levelIn);
		if (session == null || !session.getDifficulty().isNightmare()) return resultIn;
		return Math.max(0.0F, resultIn + session.getDarkness());
	}
}
