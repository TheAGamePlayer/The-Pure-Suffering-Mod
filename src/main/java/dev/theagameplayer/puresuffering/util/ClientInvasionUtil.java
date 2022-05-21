package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientInvasionUtil {
	public static float handleBrightness(float brightnessIn, ClientLevel worldIn) {
		if (!worldIn.dimensionType().hasFixedTime()) {
			float brightness = 0.0F;
			ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(worldIn);
			ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(worldIn);
			if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
				return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, worldIn.getDayTime() % 12000L);
			} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
				return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, worldIn.getDayTime() % 12000L);
			}
		} else {
			float brightness = 0.0F;
			InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(worldIn).getRendererMap();
			if (!fixedRenderers.isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
				return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, worldIn.getDayTime() % 12000L);
			}
		}
		return brightnessIn;
	}
	
	public static boolean handleLightMap(boolean resultIn, ClientLevel worldIn) {
		if (!worldIn.dimensionType().hasFixedTime()) {
			ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(worldIn);
			ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(worldIn);
			if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				if (!rendererList.isEmpty())
					return false;
			} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				if (!rendererList.isEmpty())
					return false;
			}
		} else {
			InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(worldIn).getRendererMap();
			if (!fixedRenderers.isEmpty()) {
				ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				if (!rendererList.isEmpty())
					return false;
			}
		}
		return resultIn;
	}
}
