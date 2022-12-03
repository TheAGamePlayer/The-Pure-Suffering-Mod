package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientInvasionUtil {
	public static final float handleBrightness(final float brightnessIn, final ClientLevel levelIn) {
		if (!levelIn.dimensionType().hasFixedTime()) {
			float brightness = 0.0F;
			final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(levelIn);
			final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(levelIn);
			if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
				final ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (final InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
				return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, levelIn.getDayTime() % 12000L);
			} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
				final ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (final InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
				return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, levelIn.getDayTime() % 12000L);
			}
		} else {
			float brightness = 0.0F;
			final InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(levelIn).getRendererMap();
			if (!fixedRenderers.isEmpty()) {
				final ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				for (final InvasionSkyRenderer renderer : rendererList) {
					brightness += renderer.getBrightness() / rendererList.size();
				}
				return ClientTransitionHandler.tickBrightness(brightnessIn, brightness, levelIn.getDayTime() % 12000L);
			}
		}
		return brightnessIn;
	}
	
	public static final boolean handleLightMap(final boolean resultIn, final ClientLevel levelIn) {
		if (!levelIn.dimensionType().hasFixedTime()) {
			final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(levelIn);
			final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(levelIn);
			if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
				final ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				if (!rendererList.isEmpty())
					return false;
			} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
				final ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				if (!rendererList.isEmpty())
					return false;
			}
		} else {
			final InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(levelIn).getRendererMap();
			if (!fixedRenderers.isEmpty()) {
				final ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
					return renderer.isBrightnessChanged();
				});
				if (!rendererList.isEmpty())
					return false;
			}
		}
		return resultIn;
	}
}
