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
	
	public static ClientInvasionWorldInfo getDayClientInfo(ClientLevel worldIn) {
		if (!DAY_RENDERERS.containsKey(worldIn))
			DAY_RENDERERS.put(worldIn, new ClientInvasionWorldInfo());
		return DAY_RENDERERS.get(worldIn);
	}
	
	public static ClientInvasionWorldInfo getNightClientInfo(ClientLevel worldIn) {
		if (!NIGHT_RENDERERS.containsKey(worldIn))
			NIGHT_RENDERERS.put(worldIn, new ClientInvasionWorldInfo());
		return NIGHT_RENDERERS.get(worldIn);
	}
	
	public static ClientInvasionWorldInfo getFixedClientInfo(ClientLevel worldIn) {
		if (!FIXED_RENDERERS.containsKey(worldIn))
			FIXED_RENDERERS.put(worldIn, new ClientInvasionWorldInfo());
		return FIXED_RENDERERS.get(worldIn);
	}
	
	public InvasionRendererMap getRendererMap() {
		return this.rendererMap;
	}
	
	public boolean isClientTime() {
		return this.isTime;
	}
	
	public void updateClientTime(boolean isTimeIn) {
		this.isTime = isTimeIn;
	}
	
	public int getInvasionsCount() {
		return this.invasionsCount;
	}
	
	public void setInvasionsCount(int invasionsCountIn) {
		this.invasionsCount = invasionsCountIn;
	}
	
	public double getXPMultiplier() {
		return this.xpMult;
	}
	
	public void setXPMultiplier(double xpMultIn) {
		this.xpMult = xpMultIn;
	}
}
