package dev.theagameplayer.puresuffering.client.renderer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class InvasionSkyRenderer {
	private final ResourceLocation id;
	private final InvasionFogRenderer fogRenderer;
	private final ResourceLocation sun, moon;
	private final ResourceLocation fixedSky;
	private final boolean weatherVisibilityChanged, brightnessChanged, skyColorChanged;
	private final float weatherVisibility;
	private final float brightness;
	private final float red, green, blue;
	
	public InvasionSkyRenderer(final ResourceLocation idIn, final InvasionFogRenderer fogRendererIn, final ResourceLocation sunIn, final ResourceLocation moonIn, final ResourceLocation fixedSkyIn, final boolean weatherVisibilityChangedIn, final boolean brightnessChangedIn, final boolean skyColorChangedIn, final float weatherVisibilityIn, final float brightnessIn, final float redIn, final float greenIn, final float blueIn) {
		this.fogRenderer = fogRendererIn;
		this.id = idIn;
		this.sun = sunIn;
		this.moon = moonIn;
		this.fixedSky = fixedSkyIn;
		this.weatherVisibilityChanged = weatherVisibilityChangedIn;
		this.brightnessChanged = brightnessChangedIn;
		this.skyColorChanged = skyColorChangedIn;
		this.weatherVisibility = weatherVisibilityIn;
		this.brightness = brightnessIn;
		this.red = redIn;
		this.green = greenIn;
		this.blue = blueIn;
	}
	
	public final InvasionSkyRenderer.Builder deconstruct() {
		return new InvasionSkyRenderer.Builder(this.fogRenderer.deconstruct(), this.sun, this.moon, this.fixedSky, this.weatherVisibilityChanged, this.brightnessChanged, this.skyColorChanged, this.weatherVisibility, this.brightness, this.red, this.green, this.blue);
	}
	
	public final ResourceLocation getId() {
		return this.id;
	}
	
	public final InvasionFogRenderer getFogRenderer() {
		return this.fogRenderer;
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
	
	public final boolean isWeatherVisibilityChanged() {
		return this.weatherVisibilityChanged;
	}
	
	public final boolean isBrightnessChanged() {
		return this.brightnessChanged;
	}
	
	public final boolean isSkyColorChanged() {
		return this.skyColorChanged;
	}
	
	public final float getWeatherVisibility() {
		return this.weatherVisibility;
	}
	
	public final float getBrightness() {
		return this.brightness;
	}
	
	public final float getRedOffset() {
		return this.red;
	}
	
	public final float getGreenOffset() {
		return this.green;
	}
	
	public final float getBlueOffset() {
		return this.blue;
	}
	
	@Override
	public final String toString() {
		return this.getId().toString();
	}
	
	public static final class Builder {
		private InvasionFogRenderer.Builder fogRenderer = InvasionFogRenderer.Builder.fogRenderer();
		private ResourceLocation sun, moon;
		private ResourceLocation fixedSky;
		private boolean weatherVisibilityChanged, brightnessChanged, skyColorChanged;
		private float weatherVisibility;
		private float brightness;
		private float red, green, blue;
		
		private Builder(final InvasionFogRenderer.Builder fogRendererIn, @Nullable final ResourceLocation sunIn, @Nullable final ResourceLocation moonIn, @Nullable final ResourceLocation fixedSkyIn, final boolean weatherVisibilityChangedIn, final boolean brightnessChangedIn, final boolean skyColorChangedIn, final float weatherVisibilityIn, final float brightnessIn, final float redIn, final float greenIn, final float blueIn) {
			this.fogRenderer = fogRendererIn;
			this.sun = sunIn;
			this.moon = moonIn;
			this.fixedSky = fixedSkyIn;
			this.weatherVisibilityChanged = weatherVisibilityChangedIn;
			this.brightnessChanged = brightnessChangedIn;
			this.skyColorChanged = skyColorChangedIn;
			this.weatherVisibility = weatherVisibilityIn;
			this.brightness = brightnessIn;
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
		}
		
		private Builder() {};
		
		public static final InvasionSkyRenderer.Builder skyRenderer() {
			return new InvasionSkyRenderer.Builder();
		}
		
		public final InvasionSkyRenderer.Builder withFog(final InvasionFogRenderer.Builder fogRendererIn) {
			this.fogRenderer = fogRendererIn;
			return this;
		}
		
		public final InvasionSkyRenderer.Builder sunTexture(final ResourceLocation sunTextureIn) {
			this.sun = sunTextureIn;
			return this;
		}
		
		public final InvasionSkyRenderer.Builder moonTexture(final ResourceLocation moonTextureIn) {
			this.moon = moonTextureIn;
			return this;
		}
		
		public final InvasionSkyRenderer.Builder fixedSkyTexture(final ResourceLocation fixedTextureIn) {
			this.fixedSky = fixedTextureIn;
			return this;
		}
		
		public final InvasionSkyRenderer.Builder weatherVisibility(final float weatherVisibilityIn) {
			this.weatherVisibility = weatherVisibilityIn;
			this.weatherVisibilityChanged = true;
			return this;
		}
		
		public final InvasionSkyRenderer.Builder withSkyBrightness(final float brightnessIn) {
			this.brightness = brightnessIn;
			this.brightnessChanged = true;
			return this;
		}
		
		public final InvasionSkyRenderer.Builder withRGB(final float redIn, final float greenIn, final float blueIn) {
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
			this.skyColorChanged = true;
			return this;
		}
		
		public final InvasionSkyRenderer build(final ResourceLocation idIn) {
			return new InvasionSkyRenderer(idIn, this.fogRenderer.build(idIn), this.sun, this.moon, this.fixedSky, this.weatherVisibilityChanged, this.brightnessChanged, this.skyColorChanged, this.weatherVisibility, this.brightness, this.red, this.green, this.blue);
		}
		
		public final JsonObject serializeToJson() {
			final JsonObject jsonObject = new JsonObject();
			final JsonObject fogRendererObject = this.fogRenderer.serializeToJson();
			if (fogRendererObject != null)
				jsonObject.add("FogRenderer", fogRendererObject);
			if (this.sun != null)
				jsonObject.addProperty("SunTexture", this.sun.toString());
			if (this.moon != null)
				jsonObject.addProperty("MoonTexture", this.moon.toString());
			if (this.fixedSky != null)
				jsonObject.addProperty("FixedSkyTexture", this.fixedSky.toString());
			if ((this.sun != null || this.moon != null || this.fixedSky != null) && this.weatherVisibilityChanged)
				jsonObject.addProperty("WeatherVisibility", this.weatherVisibility);
			if (this.brightnessChanged)
				jsonObject.addProperty("Brightness", this.brightness);
			if (this.skyColorChanged) {
				jsonObject.addProperty("RedOffset", this.red);
				jsonObject.addProperty("GreenOffset", this.green);
				jsonObject.addProperty("BlueOffset", this.blue);
			}
			return jsonObject.entrySet().isEmpty() ? null : jsonObject;
		}
		
		public static final InvasionSkyRenderer.Builder fromJson(final JsonObject jsonObjectIn) {
			final InvasionFogRenderer.Builder fogRenderer = jsonObjectIn.has("FogRenderer") ? InvasionFogRenderer.Builder.fromJson(jsonObjectIn.get("FogRenderer").getAsJsonObject()) : InvasionFogRenderer.Builder.fogRenderer();
			final ResourceLocation sun = jsonObjectIn.has("SunTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("SunTexture").getAsString()) : null;
			final ResourceLocation moon = jsonObjectIn.has("MoonTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("MoonTexture").getAsString()) : null;
			final ResourceLocation fixedSky = jsonObjectIn.has("FixedSkyTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("FixedSkyTexture").getAsString()) : null;
			final boolean weatherVisibilityChanged = jsonObjectIn.has("WeatherVisibility");
			final boolean brightnessChanged = jsonObjectIn.has("Brightness");
			final boolean skyColorChanged = jsonObjectIn.has("RedOffset") && jsonObjectIn.has("GreenOffset") && jsonObjectIn.has("BlueOffset");
			final float weatherVisibility = weatherVisibilityChanged ? jsonObjectIn.get("WeatherVisibility").getAsFloat() : 0.0F;
			final float brightness = brightnessChanged ? 1.0F - jsonObjectIn.get("Brightness").getAsFloat() : 1.0F;
			final float red = skyColorChanged ? jsonObjectIn.get("RedOffset").getAsFloat() : 0.0F;
			final float green = skyColorChanged ? jsonObjectIn.get("GreenOffset").getAsFloat() : 0.0F;
			final float blue = skyColorChanged ? jsonObjectIn.get("BlueOffset").getAsFloat() : 0.0F;
			return new InvasionSkyRenderer.Builder(fogRenderer, sun, moon, fixedSky, weatherVisibilityChanged, brightnessChanged, skyColorChanged, weatherVisibility, brightness, red, green, blue);
		}
		
		public final void serializeToNetwork(final FriendlyByteBuf bufIn) {
			this.fogRenderer.serializeToNetwork(bufIn);
			bufIn.writeBoolean(this.sun != null);
			if (this.sun != null)
				bufIn.writeResourceLocation(this.sun);
			bufIn.writeBoolean(this.moon != null);
			if (this.moon != null)
				bufIn.writeResourceLocation(this.moon);
			bufIn.writeBoolean(this.fixedSky != null);
			if (this.fixedSky != null)
				bufIn.writeResourceLocation(this.fixedSky);
			bufIn.writeBoolean(this.weatherVisibilityChanged);
			bufIn.writeBoolean(this.brightnessChanged);
			bufIn.writeBoolean(this.skyColorChanged);
			bufIn.writeFloat(this.weatherVisibility);
			bufIn.writeFloat(this.brightness);
			bufIn.writeFloat(this.red);
			bufIn.writeFloat(this.green);
			bufIn.writeFloat(this.blue);
		}
		
		public static final InvasionSkyRenderer.Builder fromNetwork(final FriendlyByteBuf bufIn) {
			final InvasionFogRenderer.Builder fogRenderer = InvasionFogRenderer.Builder.fromNetwork(bufIn);
			ResourceLocation sun = null;
			ResourceLocation moon = null;
			ResourceLocation fixedSky = null;
			if (bufIn.readBoolean())
				sun = bufIn.readResourceLocation();
			if (bufIn.readBoolean())
				moon = bufIn.readResourceLocation();
			if (bufIn.readBoolean())
				fixedSky = bufIn.readResourceLocation();
			final boolean weatherVisibilityChanged = bufIn.readBoolean();
			final boolean brightnessChanged = bufIn.readBoolean();
			final boolean skyColorChanged = bufIn.readBoolean();
			final float weatherVisibility = bufIn.readFloat();
			final float brightness = bufIn.readFloat();
			final float red = bufIn.readFloat();
			final float green = bufIn.readFloat();
			final float blue = bufIn.readFloat();
			return new InvasionSkyRenderer.Builder(fogRenderer, sun, moon, fixedSky, weatherVisibilityChanged, brightnessChanged, skyColorChanged, weatherVisibility, brightness, red, green, blue);
		}
	}
}
