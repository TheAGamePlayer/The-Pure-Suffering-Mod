package dev.theagameplayer.puresuffering.client;

import dev.theagameplayer.puresuffering.invasion.Invasion;

public final class ClientTransitionHandler {
	private static final float NIGHTMARE_INVASION_DARKNESS = 0.25F;

	//SUN/MOON ALPHA
	public static final float getSunMoonAlpha(final boolean newTextureIn, final float alphaOffsetIn, final long dayTimeIn) {
		float alpha = alphaOffsetIn;
		if (newTextureIn) {
			if (dayTimeIn < Invasion.HALF_TRANSITION) {
				alpha = (1.0F/Invasion.HALF_TRANSITION) * (Invasion.HALF_TRANSITION - dayTimeIn); //1-0
			} else if (dayTimeIn < Invasion.TRANSITION_TIME) {
				alpha = (alphaOffsetIn/Invasion.HALF_TRANSITION) * (dayTimeIn - Invasion.HALF_TRANSITION + 1); //0-1
			} else if (dayTimeIn > 11999L - Invasion.HALF_TRANSITION) {
				alpha = (1.0F/Invasion.HALF_TRANSITION) * (Invasion.HALF_TRANSITION - (12000L - dayTimeIn - 1)); //0-1
			} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
				alpha = (alphaOffsetIn/Invasion.HALF_TRANSITION) * (12000L - dayTimeIn - Invasion.HALF_TRANSITION); //1-0
			}
			return alpha;
		}
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			alpha = (alphaOffsetIn/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			alpha = (alphaOffsetIn/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		}
		return alpha;
	}

	//WEATHER_VISIBILITY
	public static final float getWeatherVisibility(final float visbilityOffsetIn, final long dayTimeIn) {
		float visbility = visbilityOffsetIn;
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			visbility = (visbilityOffsetIn/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			visbility = (visbilityOffsetIn/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		}
		return visbility;
	}

	//SKY_COLOR
	public static final void getSkyColor(final float[] rgbIn, final long dayTimeIn) {
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			rgbIn[0] = (rgbIn[0]/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
			rgbIn[1] = (rgbIn[1]/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
			rgbIn[2] = (rgbIn[2]/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			rgbIn[0] = (rgbIn[0]/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
			rgbIn[1] = (rgbIn[1]/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
			rgbIn[2] = (rgbIn[2]/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		}
	}

	//BRIGHTNESS
	public static final float getBrightness(final float brightnessOffsetIn, final long dayTimeIn) {
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			return (brightnessOffsetIn/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			return (brightnessOffsetIn/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		}
		return brightnessOffsetIn;
	}

	//LIGHT_TEXTURE_DARKNESS
	public static final float getLightTextureDarkness(final long dayTimeIn) {
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			return (NIGHTMARE_INVASION_DARKNESS/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			return (NIGHTMARE_INVASION_DARKNESS/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		}
		return NIGHTMARE_INVASION_DARKNESS;
	}

	//FOG COLOR
	public static final void getFogColor(final float[] rgbIn, final long dayTimeIn) {
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			rgbIn[0] = (rgbIn[0]/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
			rgbIn[1] = (rgbIn[1]/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
			rgbIn[2] = (rgbIn[2]/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			rgbIn[0] = (rgbIn[0]/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
			rgbIn[1] = (rgbIn[1]/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
			rgbIn[2] = (rgbIn[2]/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		}
	}
}
