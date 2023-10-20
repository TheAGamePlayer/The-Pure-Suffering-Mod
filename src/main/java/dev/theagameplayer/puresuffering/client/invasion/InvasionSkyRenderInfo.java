package dev.theagameplayer.puresuffering.client.invasion;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class InvasionSkyRenderInfo {
	private final ResourceLocation id;
	private final InvasionFogRenderInfo fogRenderer;
	private final float sunMoonAlpha;
	private final ResourceLocation sun, moon;
	private final ResourceLocation fixedSky;
	private final boolean[] changes;
	private final float[] fVisibility;
	private final float[] fBrightness;
	private final float[][] fRGB;
	private final float weatherVisibility;
	private final float brightness;
	private final int lightLevel;
	private final float[] rgb;

	public InvasionSkyRenderInfo(final ResourceLocation idIn, final InvasionFogRenderInfo fogRendererIn, final float sunMoonAlphaIn, final ResourceLocation sunIn, final ResourceLocation moonIn, final ResourceLocation fixedSkyIn, final boolean[] changesIn, final float[] fVisibilityIn, final float[] fBrightnessIn, final float[][] fRGBIn, final float weatherVisibilityIn, final float brightnessIn, final int lightLevelIn, final float[] rgbIn) {
		this.id = idIn;
		this.fogRenderer = fogRendererIn;
		this.sunMoonAlpha = sunMoonAlphaIn;
		this.sun = sunIn;
		this.moon = moonIn;
		this.fixedSky = fixedSkyIn;
		this.changes = changesIn;
		this.fVisibility = fVisibilityIn;
		this.fBrightness = fBrightnessIn;
		this.fRGB = fRGBIn;
		this.weatherVisibility = weatherVisibilityIn;
		this.brightness = brightnessIn;
		this.lightLevel = lightLevelIn;
		this.rgb = rgbIn;
	}

	public final InvasionSkyRenderInfo.Builder deconstruct() {
		return new InvasionSkyRenderInfo.Builder(this.fogRenderer.deconstruct(), this.sunMoonAlpha, this.sun, this.moon, this.fixedSky, this.changes, this.fVisibility, this.fBrightness, this.fRGB, this.weatherVisibility, this.brightness, this.lightLevel, this.rgb);
	}

	public final ResourceLocation getId() {
		return this.id;
	}

	public final InvasionFogRenderInfo getFogRenderInfo() {
		return this.fogRenderer;
	}

	public final float getSunMoonAlpha() {
		return this.sunMoonAlpha;
	}

	public final ResourceLocation getSunTexture() {
		return this.sun;
	}

	public final ResourceLocation getMoonTexture() {
		return this.moon;
	}

	public final ResourceLocation getFixedSkyTexture() {
		return this.fixedSky;
	}
	
	public final boolean isSunMoonAlphaChanged() {
		return this.changes[6];
	}

	public final boolean doesSunAndMoonVisibilityFlicker() {
		return this.changes[5];
	}

	public final boolean doesBrightnessFlicker() {
		return this.changes[4];
	}

	public final boolean doesSkyColorFlicker() {
		return this.changes[3];
	}

	public final boolean isWeatherVisibilityChanged() {
		return this.changes[2];
	}

	public final boolean isBrightnessChanged() {
		return this.changes[1];
	}

	public final boolean isSkyColorChanged() {
		return this.changes[0];
	}

	public final float[] getFlickerVisibility() {
		return this.fVisibility;
	}
	
	public final float[] getFlickerBrightness() {
		return this.fBrightness;
	}
	
	public final float[] getFlickerRGBOffset(final int indexIn) {
		return this.fRGB[indexIn];
	}
	
	public final int getFlickerRGBSize() {
		return this.fRGB.length;
	}
	
	public final float getWeatherVisibility() {
		return this.weatherVisibility;
	}

	public final float getBrightness() {
		return this.brightness;
	}
	
	public final int getLightLevel() {
		return this.lightLevel;
	}
	
	public final float getRGBOffset(final int valueIn) {
		return this.rgb[valueIn];
	}
	
	@Override
	public final boolean equals(final Object objIn) {
		return this.id.toString().equals(objIn.toString());
	}
	
	@Override
	public final String toString() {
		return this.id.toString();
	}

	public static final class Builder {
		private InvasionFogRenderInfo.Builder fogRenderer = InvasionFogRenderInfo.Builder.fogRenderer();
		private float sunMoonAlpha = 1.0F;
		private ResourceLocation sun, moon;
		private ResourceLocation fixedSky;
		private boolean[] changes = new boolean[7];
		private float[] fVisibility = new float[4];
		private float[] fBrightness = new float[4];
		private float[][] fRGB = new float[0][5];
		private float weatherVisibility = 0.0F;
		private float brightness = 1.0F;
		private int lightLevel = -1;
		private float[] rgb = new float[3];

		private Builder(final InvasionFogRenderInfo.Builder fogRendererIn, final float sunMoonAlphaIn, @Nullable final ResourceLocation sunIn, @Nullable final ResourceLocation moonIn, @Nullable final ResourceLocation fixedSkyIn, final boolean[] changesIn, final float[] fVisibilityIn, final float[] fBrightnessIn, final float[][] fRGBIn, final float weatherVisibilityIn, final float brightnessIn, final int lightLevelIn, final float[] rgbIn) {
			this.fogRenderer = fogRendererIn;
			this.sunMoonAlpha = sunMoonAlphaIn;
			this.sun = sunIn;
			this.moon = moonIn;
			this.fixedSky = fixedSkyIn;
			this.changes = changesIn;
			this.fVisibility = fVisibilityIn;
			this.fBrightness = fBrightnessIn;
			this.fRGB = fRGBIn;
			this.weatherVisibility = weatherVisibilityIn;
			this.brightness = brightnessIn;
			this.lightLevel = lightLevelIn;
			this.rgb = rgbIn;
		}

		private Builder() {};

		public static final InvasionSkyRenderInfo.Builder skyRenderInfo() {
			return new InvasionSkyRenderInfo.Builder();
		}

		public final InvasionSkyRenderInfo.Builder withFog(final InvasionFogRenderInfo.Builder fogRendererIn) {
			this.fogRenderer = fogRendererIn;
			return this;
		}

		public final InvasionSkyRenderInfo.Builder withSunMoonAlpha(final float sunMoonAlphaIn) {
			this.sunMoonAlpha = sunMoonAlphaIn;
			this.changes[6] = true;
			return this;
		}

		public final InvasionSkyRenderInfo.Builder sunTexture(final ResourceLocation sunTextureIn) {
			this.sun = sunTextureIn;
			return this;
		}

		public final InvasionSkyRenderInfo.Builder moonTexture(final ResourceLocation moonTextureIn) {
			this.moon = moonTextureIn;
			return this;
		}

		public final InvasionSkyRenderInfo.Builder fixedSkyTexture(final ResourceLocation fixedTextureIn) {
			this.fixedSky = fixedTextureIn;
			return this;
		}
		
		public final InvasionSkyRenderInfo.Builder withFlickerVisibility(final float minVisibilityIn, final float maxVisibilityIn, final int minDelayIn, final int maxDelayIn) {
			this.fVisibility[0] = minVisibilityIn;
			this.fVisibility[1] = maxVisibilityIn;
			this.fVisibility[2] = minDelayIn;
			this.fVisibility[3] = maxDelayIn;
			this.changes[5] = true;
			return this;
		}
		
		public final InvasionSkyRenderInfo.Builder withFlickerBrightness(final float minBrightnessIn, final float maxBrightnessIn, final int minDelayIn, final int maxDelayIn) {
			this.fBrightness[0] = minBrightnessIn;
			this.fBrightness[1] = maxBrightnessIn;
			this.fBrightness[2] = minDelayIn;
			this.fBrightness[3] = maxDelayIn;
			this.changes[4] = true;
			return this;
		}
		
		public final InvasionSkyRenderInfo.Builder withFlickerRGB(final float redIn, final float greenIn, final float blueIn, final int minDelayIn, final int maxDelayIn) {
			final int l = this.fRGB.length;
			final float[][] fRGB = new float[l + 1][5];
			for (int i1 = 0; i1 < l; i1++) {
				for (int i2 = 0; i2 < 5; i2++)
					fRGB[i1][i2] = this.fRGB[i1][i2];
			}
			this.fRGB = fRGB;
			this.fRGB[l][0] = redIn;
			this.fRGB[l][1] = greenIn;
			this.fRGB[l][2] = blueIn;
			this.fRGB[l][3] = minDelayIn;
			this.fRGB[l][4] = maxDelayIn;
			this.changes[3] = true;
			return this;
		}

		public final InvasionSkyRenderInfo.Builder weatherVisibility(final float weatherVisibilityIn) {
			this.weatherVisibility = weatherVisibilityIn;
			this.changes[2] = true;
			return this;
		}

		public final InvasionSkyRenderInfo.Builder withSkyBrightness(final float brightnessIn) {
			this.brightness = brightnessIn;
			this.changes[1] = true;
			return this;
		}
		
		public final InvasionSkyRenderInfo.Builder withLightLevel(final int lightLevelIn) {
			this.lightLevel = lightLevelIn;
			return this;
		}
		
		public final InvasionSkyRenderInfo.Builder withRGB(final float redIn, final float greenIn, final float blueIn) {
			this.rgb[0] = redIn;
			this.rgb[1] = greenIn;
			this.rgb[2] = blueIn;
			this.changes[0] = true;
			return this;
		}

		public final InvasionSkyRenderInfo build(final ResourceLocation idIn) {
			return new InvasionSkyRenderInfo(idIn, this.fogRenderer.build(idIn), this.sunMoonAlpha, this.sun, this.moon, this.fixedSky, this.changes, this.fVisibility, this.fBrightness, this.fRGB, this.weatherVisibility, this.brightness, this.lightLevel, this.rgb);
		}

		public final JsonObject serializeToJson() {
			final JsonObject jsonObject = new JsonObject();
			final JsonObject fogRendererObject = this.fogRenderer.serializeToJson();
			if (fogRendererObject != null)
				jsonObject.add("FogRenderInfo", fogRendererObject);
			if (this.sunMoonAlpha < 1 && this.changes[6])
				jsonObject.addProperty("SunMoonAlpha", this.sunMoonAlpha);
			if (this.sun != null && this.sunMoonAlpha > 0)
				jsonObject.addProperty("SunTexture", this.sun.toString());
			if (this.moon != null && this.sunMoonAlpha > 0)
				jsonObject.addProperty("MoonTexture", this.moon.toString());
			if (this.fixedSky != null)
				jsonObject.addProperty("FixedSkyTexture", this.fixedSky.toString());
			if (this.changes[5]) {
				final JsonArray a = new JsonArray();
				a.add(this.fVisibility[0]);
				a.add(this.fVisibility[1]);
				a.add(this.fVisibility[2]);
				a.add(this.fVisibility[3]);
				jsonObject.add("FlickerVisibility", a);
			}
			if (this.changes[4]) {
				final JsonArray a = new JsonArray();
				a.add(this.fBrightness[0]);
				a.add(this.fBrightness[1]);
				a.add(this.fBrightness[2]);
				a.add(this.fBrightness[3]);
				jsonObject.add("FlickerBrightness", a);
			}
			if (this.changes[3]) {
				final JsonArray a1 = new JsonArray();
				for (int i = 0; i < this.fRGB.length; i++) {
					final JsonArray a2 = new JsonArray();
					a2.add(this.fRGB[i][0]);
					a2.add(this.fRGB[i][1]);
					a2.add(this.fRGB[i][2]);
					a2.add(this.fRGB[i][3]);
					a2.add(this.fRGB[i][4]);
					a1.add(a2);
				}
				jsonObject.add("FlickerRGBOffset", a1);
			}
			if ((this.sun != null || this.moon != null || this.fixedSky != null) && this.sunMoonAlpha > 0 && this.changes[2])
				jsonObject.addProperty("WeatherVisibility", this.weatherVisibility);
			if (this.changes[1])
				jsonObject.addProperty("Brightness", this.brightness);
			if (this.lightLevel > -1)
				jsonObject.addProperty("LightLevel", this.lightLevel);
			if (this.changes[0]) {
				final JsonArray a = new JsonArray();
				a.add(this.rgb[0]);
				a.add(this.rgb[1]);
				a.add(this.rgb[2]);
				jsonObject.add("RGBOffset", a);
			}
			return jsonObject.entrySet().isEmpty() ? null : jsonObject;
		}

		public static final InvasionSkyRenderInfo.Builder fromJson(final JsonObject jsonObjectIn) {
			final InvasionFogRenderInfo.Builder fogRenderer = jsonObjectIn.has("FogRenderInfo") ? InvasionFogRenderInfo.Builder.fromJson(jsonObjectIn.get("FogRenderInfo").getAsJsonObject()) : InvasionFogRenderInfo.Builder.fogRenderer();
			final boolean[] changes = new boolean[7];
			changes[0] = jsonObjectIn.has("RGBOffset");
			changes[1] = jsonObjectIn.has("Brightness");
			changes[2] = jsonObjectIn.has("WeatherVisibility");
			changes[3] = jsonObjectIn.has("FlickerRGBOffset");
			changes[4] = jsonObjectIn.has("FlickerBrightness");
			changes[5] = jsonObjectIn.has("FlickerVisibility");
			changes[6] = jsonObjectIn.has("SunMoonAlpha");
			final float sunMoonAlpha = changes[6] ? jsonObjectIn.get("SunMoonAlpha").getAsFloat() : 1.0F;
			final ResourceLocation sun = sunMoonAlpha > 0 && jsonObjectIn.has("SunTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("SunTexture").getAsString()) : null;
			final ResourceLocation moon = sunMoonAlpha > 0 && jsonObjectIn.has("MoonTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("MoonTexture").getAsString()) : null;
			final ResourceLocation fixedSky = jsonObjectIn.has("FixedSkyTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("FixedSkyTexture").getAsString()) : null;
			final JsonElement fVisibilityElement = changes[5] ? jsonObjectIn.get("FlickerVisibility") : null;
			final float[] fVisibility = new float[4];
			if (fVisibilityElement != null && fVisibilityElement.isJsonArray() && !fVisibilityElement.getAsJsonArray().isEmpty()) {
				final JsonArray a = fVisibilityElement.getAsJsonArray();
				fVisibility[0] = a.get(0).getAsFloat();
				fVisibility[1] = a.get(1).getAsFloat();
				fVisibility[2] = a.get(2).getAsFloat();
				fVisibility[3] = a.get(3).getAsFloat();
			}
			final JsonElement fBrightnessElement = changes[4] ? jsonObjectIn.get("FlickerBrightness") : null;
			final float[] fBrightness = new float[4];
			if (fBrightnessElement != null && fBrightnessElement.isJsonArray() && !fBrightnessElement.getAsJsonArray().isEmpty()) {
				final JsonArray a = fBrightnessElement.getAsJsonArray();
				fBrightness[0] = a.get(0).getAsFloat();
				fBrightness[1] = a.get(1).getAsFloat();
				fBrightness[2] = a.get(2).getAsFloat();
				fBrightness[3] = a.get(3).getAsFloat();
			}
			final JsonElement fRGBElement = changes[3] ? jsonObjectIn.get("FlickerRGBOffset") : null;
			float[][] fRGB = new float[0][5];
			if (fRGBElement != null && fRGBElement.isJsonArray() && !fRGBElement.getAsJsonArray().isEmpty()) {
				final JsonArray a1 = fRGBElement.getAsJsonArray();
				fRGB = new float[a1.size()][5];
				for (int i = 0; i < a1.size(); i++) {
					final JsonElement e = a1.get(i);
					if (e.isJsonArray() && !e.getAsJsonArray().isEmpty()) {
						final JsonArray a2 = e.getAsJsonArray();
						fRGB[i][0] = a2.get(0).getAsFloat();
						fRGB[i][1] = a2.get(1).getAsFloat();
						fRGB[i][2] = a2.get(2).getAsFloat();
						fRGB[i][3] = a2.get(3).getAsFloat();
						fRGB[i][4] = a2.get(4).getAsFloat();
					}
				}
			}
			final float weatherVisibility = changes[2] ? jsonObjectIn.get("WeatherVisibility").getAsFloat() : 0.0F;
			final float brightness = changes[1] ? 1.0F - jsonObjectIn.get("Brightness").getAsFloat() : 1.0F;
			final int lightLevel = jsonObjectIn.has("LightLevel") ? Mth.clamp(15 - jsonObjectIn.get("LightLevel").getAsInt(), 0, 15) : -1;
			final JsonElement rgbElement = changes[0] ? jsonObjectIn.get("RGBOffset") : null;
			final float[] rgb = new float[3];
			if (rgbElement != null && rgbElement.isJsonArray() && !rgbElement.getAsJsonArray().isEmpty()) {
				final JsonArray a = rgbElement.getAsJsonArray();
				rgb[0] = a.get(0).getAsFloat();
				rgb[1] = a.get(1).getAsFloat();
				rgb[2] = a.get(2).getAsFloat();
			}
			return new InvasionSkyRenderInfo.Builder(fogRenderer, sunMoonAlpha, sun, moon, fixedSky, changes, fVisibility, fBrightness, fRGB, weatherVisibility, brightness, lightLevel, rgb);
		}

		public final void serializeToNetwork(final FriendlyByteBuf bufIn) {
			this.fogRenderer.serializeToNetwork(bufIn);
			bufIn.writeBoolean(this.changes[0]);
			bufIn.writeBoolean(this.changes[1]);
			bufIn.writeBoolean(this.changes[2]);
			bufIn.writeBoolean(this.changes[3]);
			bufIn.writeBoolean(this.changes[4]);
			bufIn.writeBoolean(this.changes[5]);
			bufIn.writeBoolean(this.changes[6]);
			bufIn.writeFloat(this.sunMoonAlpha);
			if (this.sunMoonAlpha > 0) {
				bufIn.writeBoolean(this.sun != null);
				if (this.sun != null)
					bufIn.writeResourceLocation(this.sun);
				bufIn.writeBoolean(this.moon != null);
				if (this.moon != null)
					bufIn.writeResourceLocation(this.moon);
			}
			bufIn.writeBoolean(this.fixedSky != null);
			if (this.fixedSky != null)
				bufIn.writeResourceLocation(this.fixedSky);
			bufIn.writeFloat(this.fVisibility[0]);
			bufIn.writeFloat(this.fVisibility[1]);
			bufIn.writeFloat(this.fVisibility[2]);
			bufIn.writeFloat(this.fVisibility[3]);
			bufIn.writeFloat(this.fBrightness[0]);
			bufIn.writeFloat(this.fBrightness[1]);
			bufIn.writeFloat(this.fBrightness[2]);
			bufIn.writeFloat(this.fBrightness[3]);
			bufIn.writeInt(this.fRGB.length);
			for (int i = 0; i < this.fRGB.length; i++) {
				bufIn.writeFloat(this.fRGB[i][0]);
				bufIn.writeFloat(this.fRGB[i][1]);
				bufIn.writeFloat(this.fRGB[i][2]);
				bufIn.writeFloat(this.fRGB[i][3]);
				bufIn.writeFloat(this.fRGB[i][4]);
			}
			bufIn.writeFloat(this.weatherVisibility);
			bufIn.writeFloat(this.brightness);
			bufIn.writeInt(this.lightLevel);
			bufIn.writeFloat(this.rgb[0]);
			bufIn.writeFloat(this.rgb[1]);
			bufIn.writeFloat(this.rgb[2]);
		}

		public static final InvasionSkyRenderInfo.Builder fromNetwork(final FriendlyByteBuf bufIn) {
			final InvasionFogRenderInfo.Builder fogRenderer = InvasionFogRenderInfo.Builder.fromNetwork(bufIn);
			final boolean[] changes = new boolean[7];
			changes[0] = bufIn.readBoolean();
			changes[1] = bufIn.readBoolean();
			changes[2] = bufIn.readBoolean();
			changes[3] = bufIn.readBoolean();
			changes[4] = bufIn.readBoolean();
			changes[5] = bufIn.readBoolean();
			changes[6] = bufIn.readBoolean();
			final float sunMoonAlpha = bufIn.readFloat();
			ResourceLocation sun = null;
			ResourceLocation moon = null;
			ResourceLocation fixedSky = null;
			if (sunMoonAlpha > 0) {
				if (bufIn.readBoolean())
					sun = bufIn.readResourceLocation();
				if (bufIn.readBoolean())
					moon = bufIn.readResourceLocation();
			}
			if (bufIn.readBoolean())
				fixedSky = bufIn.readResourceLocation();
			final float[] fVisibility = new float[4];
			fVisibility[0] = bufIn.readFloat();
			fVisibility[1] = bufIn.readFloat();
			fVisibility[2] = bufIn.readFloat();
			fVisibility[3] = bufIn.readFloat();
			final float[] fBrightness = new float[4];
			fBrightness[0] = bufIn.readFloat();
			fBrightness[1] = bufIn.readFloat();
			fBrightness[2] = bufIn.readFloat();
			fBrightness[3] = bufIn.readFloat();
			final float[][] fRGB = new float[bufIn.readInt()][5];
			for (int i = 0; i < fRGB.length; i++) {
				fRGB[i][0] = bufIn.readFloat();
				fRGB[i][1] = bufIn.readFloat();
				fRGB[i][2] = bufIn.readFloat();
				fRGB[i][3] = bufIn.readFloat();
				fRGB[i][4] = bufIn.readFloat();
			}
			final float weatherVisibility = bufIn.readFloat();
			final float brightness = bufIn.readFloat();
			final int lightLevel = bufIn.readInt();
			final float[] rgb = new float[3];
			rgb[0] = bufIn.readFloat();
			rgb[1] = bufIn.readFloat();
			rgb[2] = bufIn.readFloat();
			return new InvasionSkyRenderInfo.Builder(fogRenderer, sunMoonAlpha, sun, moon, fixedSky, changes, fVisibility, fBrightness, fRGB, weatherVisibility, brightness, lightLevel, rgb);
		}
	}
}
