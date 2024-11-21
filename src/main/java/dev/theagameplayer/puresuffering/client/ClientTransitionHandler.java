package dev.theagameplayer.puresuffering.client;

import dev.theagameplayer.puresuffering.invasion.Invasion;

public final class ClientTransitionHandler {
	private static final float NIGHTMARE_INVASION_DARKNESS = 0.25F;

	//SUN/MOON ALPHA
	public static final float getSunMoonAlpha(final boolean pNewTexture, final float pAlphaOffset, final long pDayTime) {
		float alpha = pAlphaOffset;
		if (pNewTexture) {
			if (pDayTime < Invasion.HALF_TRANSITION) {
				alpha = (1.0F/Invasion.HALF_TRANSITION) * (Invasion.HALF_TRANSITION - pDayTime); //1-0
			} else if (pDayTime < Invasion.TRANSITION_TIME) {
				alpha = (pAlphaOffset/Invasion.HALF_TRANSITION) * (pDayTime - Invasion.HALF_TRANSITION + 1); //0-1
			} else if (pDayTime > 11999L - Invasion.HALF_TRANSITION) {
				alpha = (1.0F/Invasion.HALF_TRANSITION) * (Invasion.HALF_TRANSITION - (12000L - pDayTime - 1)); //0-1
			} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
				alpha = (pAlphaOffset/Invasion.HALF_TRANSITION) * (12000L - pDayTime - Invasion.HALF_TRANSITION); //1-0
			}
			return alpha;
		}
		if (pDayTime < Invasion.TRANSITION_TIME) {
			alpha = (pAlphaOffset/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			alpha = (pAlphaOffset/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		}
		return alpha;
	}

	//WEATHER_VISIBILITY
	public static final float getWeatherVisibility(final float pVisibilityOffset, final long pDayTime) {
		float visbility = pVisibilityOffset;
		if (pDayTime < Invasion.TRANSITION_TIME) {
			visbility = (pVisibilityOffset/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			visbility = (pVisibilityOffset/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		}
		return visbility;
	}

	//SKY_COLOR
	public static final void getSkyColor(final float[] pRGB, final long pDayTime) {
		if (pDayTime < Invasion.TRANSITION_TIME) {
			pRGB[0] = (pRGB[0]/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
			pRGB[1] = (pRGB[1]/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
			pRGB[2] = (pRGB[2]/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			pRGB[0] = (pRGB[0]/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
			pRGB[1] = (pRGB[1]/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
			pRGB[2] = (pRGB[2]/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		}
	}

	//BRIGHTNESS
	public static final float getBrightness(final float pBrightnessOffset, final long pDayTime) {
		if (pDayTime < Invasion.TRANSITION_TIME) {
			return (pBrightnessOffset/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			return (pBrightnessOffset/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		}
		return pBrightnessOffset;
	}

	//LIGHT_TEXTURE_DARKNESS
	public static final float getLightTextureDarkness(final long pDayTime) {
		if (pDayTime < Invasion.TRANSITION_TIME) {
			return (NIGHTMARE_INVASION_DARKNESS/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			return (NIGHTMARE_INVASION_DARKNESS/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		}
		return NIGHTMARE_INVASION_DARKNESS;
	}

	//FOG COLOR
	public static final void getFogColor(final float[] pRGB, final long pDayTime) {
		if (pDayTime < Invasion.TRANSITION_TIME) {
			pRGB[0] = (pRGB[0]/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
			pRGB[1] = (pRGB[1]/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
			pRGB[2] = (pRGB[2]/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			pRGB[0] = (pRGB[0]/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
			pRGB[1] = (pRGB[1]/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
			pRGB[2] = (pRGB[2]/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		}
	}
}
