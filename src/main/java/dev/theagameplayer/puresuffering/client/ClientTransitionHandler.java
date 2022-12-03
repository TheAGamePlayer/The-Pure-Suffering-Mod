package dev.theagameplayer.puresuffering.client;

import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;

public final class ClientTransitionHandler {
	public static final int TRANSITION_TIME = 600, HALF_TRANSITION = TRANSITION_TIME/2;
	
	//SUN_ALPHA
	public static final float tickSunMoonAlpha(final float sunMoonAlphaIncIn, final long dayTimeIn) {
		float sunMoonAlpha = 0.0F;
		if (dayTimeIn < HALF_TRANSITION) {
			sunMoonAlpha = (sunMoonAlphaIncIn/HALF_TRANSITION) * (HALF_TRANSITION - dayTimeIn); //1-0
		} else if (dayTimeIn < TRANSITION_TIME) {
			sunMoonAlpha = (sunMoonAlphaIncIn/HALF_TRANSITION) * (dayTimeIn - HALF_TRANSITION + 1); //0-1
		} else if (dayTimeIn > 11999L - HALF_TRANSITION) {
			sunMoonAlpha = (sunMoonAlphaIncIn/HALF_TRANSITION) * (HALF_TRANSITION - (12000L - dayTimeIn - 1)); //0-1
		} else if (dayTimeIn > 11999L - TRANSITION_TIME) {
			sunMoonAlpha = (sunMoonAlphaIncIn/HALF_TRANSITION) * (12000L - dayTimeIn - HALF_TRANSITION); //1-0
		} else {
			sunMoonAlpha = sunMoonAlphaIncIn;
		}
		return sunMoonAlpha;
	}

	//WEATHER_VISIBILITY
	public static final float tickWeatherVisibility(final float weatherVisbilityIncIn, final long dayTimeIn) {
		float weatherVisbility = 0.0F;
		if (dayTimeIn < TRANSITION_TIME) {
			weatherVisbility = (weatherVisbilityIncIn/TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - TRANSITION_TIME) {
			weatherVisbility = (weatherVisbilityIncIn/TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		} else {
			weatherVisbility = weatherVisbilityIncIn;
		}
		return weatherVisbility;
	}
	
	//SKY_COLOR
	public static final float tickSkyColor(final float skyColorIn, final float skyColorIncIn, final long dayTimeIn) {
		float skyColor = 0.0F;
		if (dayTimeIn < TRANSITION_TIME) {
			skyColor = (skyColorIncIn/TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - TRANSITION_TIME) {
			skyColor = (skyColorIncIn/TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		} else {
			skyColor = skyColorIncIn;
		}
		return skyColorIn + skyColor;
	}

	//BRIGHTNESS
	public static final float tickBrightness(final float brightnessIn, final float brightnessIncIn, final long dayTimeIn) {
		float brightness = 0.0F;
		if (dayTimeIn < TRANSITION_TIME) {
			brightness = (brightnessIncIn/TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - TRANSITION_TIME) {
			brightness = (brightnessIncIn/TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		} else {
			brightness = brightnessIncIn;
		}
		return Mth.clamp(brightnessIn - brightness, 0.0F, 1.0F);
	}

	//FOG COLOR
	public static final void tickFogColor(final ViewportEvent.ComputeFogColor eventIn, final float fogRedIncIn, final float fogGreenIncIn, final float fogBlueIncIn, final long dayTimeIn) {
		float fogRed = 0.0F, fogGreen = 0.0F, fogBlue = 0.0F;
		if (dayTimeIn < TRANSITION_TIME) {
			fogRed = (fogRedIncIn/TRANSITION_TIME) * (dayTimeIn + 1); //0-1
			fogGreen = (fogGreenIncIn/TRANSITION_TIME) * (dayTimeIn + 1); //0-1
			fogBlue = (fogBlueIncIn/TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - TRANSITION_TIME) {
			fogRed = (fogRedIncIn/TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
			fogGreen = (fogGreenIncIn/TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
			fogBlue = (fogBlueIncIn/TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		} else {
			fogRed = fogRedIncIn;
			fogGreen = fogGreenIncIn;
			fogBlue = fogBlueIncIn;
		}
		eventIn.setRed(eventIn.getRed() + fogRed);
		eventIn.setGreen(eventIn.getGreen() + fogGreen);
		eventIn.setBlue(eventIn.getBlue() + fogBlue);
	}
}
