package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import net.minecraft.world.World;

public final class ClientInvasionUtil {
	private static final ArrayList<InvasionSkyRenderer> NIGHT_RENDERERS = new ArrayList<>();
	private static final ArrayList<InvasionSkyRenderer> DAY_RENDERERS = new ArrayList<>();
	private static final ArrayList<InvasionSkyRenderer> LIGHT_RENDERERS = new ArrayList<>();

	public static float handleBrightness(float brightnessIn, World worldIn) {
		if (worldIn.dimension() == World.OVERWORLD && !LIGHT_RENDERERS.isEmpty()) {
			float brightness = 0.0F;
			for (InvasionSkyRenderer renderer : LIGHT_RENDERERS) {
				brightness += renderer.getBrightness() / LIGHT_RENDERERS.size();
			}
			return brightness;
		}
		return brightnessIn;
	}
	
	public static ArrayList<InvasionSkyRenderer> getNightRenderers() {
		return NIGHT_RENDERERS;
	}
	
	public static ArrayList<InvasionSkyRenderer> getDayRenderers() {
		return DAY_RENDERERS;
	}
	
	public static ArrayList<InvasionSkyRenderer> getLightRenderers() {
		return LIGHT_RENDERERS;
	}
}
