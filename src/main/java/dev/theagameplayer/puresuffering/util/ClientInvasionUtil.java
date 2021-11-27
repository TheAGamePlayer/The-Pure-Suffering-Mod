package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

public final class ClientInvasionUtil {
	private static final InvasionRendererMap DAY_RENDERERS = new InvasionRendererMap();
	private static final InvasionRendererMap NIGHT_RENDERERS = new InvasionRendererMap();
	
	public static float handleBrightness(float brightnessIn, ClientWorld worldIn) {
		if (worldIn.dimension() == World.OVERWORLD) {
			float brightness = 0.0F;
			if (ClientTimeUtil.isClientDay() && !ClientInvasionUtil.getDayRenderers().isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = ClientInvasionUtil.getDayRenderers().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
			} else if (ClientTimeUtil.isClientNight() && !ClientInvasionUtil.getNightRenderers().isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = ClientInvasionUtil.getNightRenderers().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
			}
			return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, worldIn.getDayTime() % 12000L);
		}
		return brightnessIn;
	}
	
	public static InvasionRendererMap getDayRenderers() {
		return DAY_RENDERERS;
	}
	
	public static InvasionRendererMap getNightRenderers() {
		return NIGHT_RENDERERS;
	}
}
