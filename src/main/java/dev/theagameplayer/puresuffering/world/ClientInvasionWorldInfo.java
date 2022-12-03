package dev.theagameplayer.puresuffering.world;

import java.util.HashMap;

import dev.theagameplayer.puresuffering.util.InvasionRendererMap;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientInvasionWorldInfo {
	private static final HashMap<ClientLevel, ClientInvasionWorldInfo> DAY_RENDERERS = new HashMap<>();
	private static final HashMap<ClientLevel, ClientInvasionWorldInfo> NIGHT_RENDERERS = new HashMap<>();
	private static final HashMap<ClientLevel, ClientInvasionWorldInfo> FIXED_RENDERERS = new HashMap<>();
	private final InvasionRendererMap rendererMap = new InvasionRendererMap();
	private boolean isTime;
	private int invasionsCount;
	private double xpMult;
	
	public static final ClientInvasionWorldInfo getDayClientInfo(final ClientLevel levelIn) {
		if (!DAY_RENDERERS.containsKey(levelIn))
			DAY_RENDERERS.put(levelIn, new ClientInvasionWorldInfo());
		return DAY_RENDERERS.get(levelIn);
	}
	
	public static final ClientInvasionWorldInfo getNightClientInfo(final ClientLevel levelIn) {
		if (!NIGHT_RENDERERS.containsKey(levelIn))
			NIGHT_RENDERERS.put(levelIn, new ClientInvasionWorldInfo());
		return NIGHT_RENDERERS.get(levelIn);
	}
	
	public static final ClientInvasionWorldInfo getFixedClientInfo(final ClientLevel levelIn) {
		if (!FIXED_RENDERERS.containsKey(levelIn))
			FIXED_RENDERERS.put(levelIn, new ClientInvasionWorldInfo());
		return FIXED_RENDERERS.get(levelIn);
	}
	
	public final InvasionRendererMap getRendererMap() {
		return this.rendererMap;
	}
	
	public final boolean isClientTime() {
		return this.isTime;
	}
	
	public final void updateClientTime(final boolean isTimeIn) {
		this.isTime = isTimeIn;
	}
	
	public final int getInvasionsCount() {
		return this.invasionsCount;
	}
	
	public final void setInvasionsCount(final int invasionsCountIn) {
		this.invasionsCount = invasionsCountIn;
	}
	
	public final double getXPMultiplier() {
		return this.xpMult;
	}
	
	public final void setXPMultiplier(final double xpMultIn) {
		this.xpMult = xpMultIn;
	}
}
