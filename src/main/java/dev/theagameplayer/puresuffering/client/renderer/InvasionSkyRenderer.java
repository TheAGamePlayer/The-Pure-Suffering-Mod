package dev.theagameplayer.puresuffering.client.renderer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public final class InvasionSkyRenderer {
	private final ResourceLocation id;
	private final InvasionFogRenderer fogRenderer;
	private final ResourceLocation sun;
	private final ResourceLocation moon;
	private final ResourceLocation daySky;
	private final ResourceLocation nightSky;
	private final boolean weatherVisibilityChanged, brightnessChanged, skyColorChanged;
	private final float weatherVisibility;
	private final float brightness;
	private final float red, green, blue;
	
	public InvasionSkyRenderer(ResourceLocation idIn, InvasionFogRenderer fogRendererIn, ResourceLocation sunIn, ResourceLocation moonIn, ResourceLocation daySkyIn, ResourceLocation nightSkyIn, boolean weatherVisibilityChangedIn, boolean brightnessChangedIn, boolean skyColorChangedIn, float weatherVisibilityIn, float brightnessIn, float redIn, float greenIn, float blueIn) {
		this.fogRenderer = fogRendererIn;
		this.id = idIn;
		this.sun = sunIn;
		this.moon = moonIn;
		this.daySky = daySkyIn;
		this.nightSky = nightSkyIn;
		this.weatherVisibilityChanged = weatherVisibilityChangedIn;
		this.brightnessChanged = brightnessChangedIn;
		this.skyColorChanged = skyColorChangedIn;
		this.weatherVisibility = weatherVisibilityIn;
		this.brightness = brightnessIn;
		this.red = redIn;
		this.green = greenIn;
		this.blue = blueIn;
	}
	
	public InvasionSkyRenderer.Builder deconstruct() {
		return new InvasionSkyRenderer.Builder(this.fogRenderer.deconstruct(), this.sun, this.moon, this.daySky, this.nightSky, this.weatherVisibilityChanged, this.brightnessChanged, this.skyColorChanged, this.weatherVisibility, this.brightness, this.red, this.green, this.blue);
	}
	
	public ResourceLocation getId() {
		return this.id;
	}
	
	public InvasionFogRenderer getFogRenderer() {
		return this.fogRenderer;
	}
	
	public ResourceLocation getSunTexture() {
		return this.sun;
	}
	
	public ResourceLocation getMoonTexture() {
		return this.moon;
	}
	
	public ResourceLocation getDaySkyTexture() {
		return this.daySky;
	}
	
	public ResourceLocation getNightSkyTexture() {
		return this.nightSky;
	}
	
	public boolean isWeatherVisibilityChanged() {
		return this.weatherVisibilityChanged;
	}
	
	public boolean isBrightnessChanged() {
		return this.brightnessChanged;
	}
	
	public boolean isSkyColorChanged() {
		return this.skyColorChanged;
	}
	
	public float getWeatherVisibility() {
		return this.weatherVisibility;
	}
	
	public float getBrightness() {
		return this.brightness;
	}
	
	public float getRedOffset() {
		return this.red;
	}
	
	public float getGreenOffset() {
		return this.green;
	}
	
	public float getBlueOffset() {
		return this.blue;
	}
	
	@Override
	public String toString() {
		return this.getId().toString();
	}
	
	public static class Builder {
		private InvasionFogRenderer.Builder fogRenderer = InvasionFogRenderer.Builder.fogRenderer();
		private ResourceLocation sun;
		private ResourceLocation moon;
		private ResourceLocation daySky;
		private ResourceLocation nightSky;
		private boolean weatherVisibilityChanged, brightnessChanged, skyColorChanged;
		private float weatherVisibility;
		private float brightness;
		private float red, green, blue;
		
		private Builder(InvasionFogRenderer.Builder fogRendererIn, @Nullable ResourceLocation sunIn, @Nullable ResourceLocation moonIn, @Nullable ResourceLocation daySkyIn, @Nullable ResourceLocation nightSkyIn, boolean weatherVisibilityChangedIn, boolean brightnessChangedIn, boolean skyColorChangedIn, float weatherVisibilityIn, float brightnessIn, float redIn, float greenIn, float blueIn) {
			this.fogRenderer = fogRendererIn;
			this.sun = sunIn;
			this.moon = moonIn;
			this.daySky = daySkyIn;
			this.nightSky = nightSkyIn;
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
		
		public static InvasionSkyRenderer.Builder skyRenderer() {
			return new InvasionSkyRenderer.Builder();
		}
		
		public InvasionSkyRenderer.Builder withFog(InvasionFogRenderer.Builder fogRendererIn) {
			this.fogRenderer = fogRendererIn;
			return this;
		}
		
		public InvasionSkyRenderer.Builder sunTexture(ResourceLocation sunTextureIn) {
			this.sun = sunTextureIn;
			return this;
		}
		
		public InvasionSkyRenderer.Builder moonTexture(ResourceLocation moonTextureIn) {
			this.moon = moonTextureIn;
			return this;
		}
		
		public InvasionSkyRenderer.Builder dayTexture(ResourceLocation dayTextureIn) {
			this.daySky = dayTextureIn;
			return this;
		}
		
		public InvasionSkyRenderer.Builder nightTexture(ResourceLocation nightTextureIn) {
			this.nightSky = nightTextureIn;
			return this;
		}
		
		public InvasionSkyRenderer.Builder weatherVisibility(float weatherVisibilityIn) {
			this.weatherVisibility = weatherVisibilityIn;
			this.weatherVisibilityChanged = true;
			return this;
		}
		
		public InvasionSkyRenderer.Builder withSkyBrightness(float brightnessIn) {
			this.brightness = brightnessIn;
			this.brightnessChanged = true;
			return this;
		}
		
		public InvasionSkyRenderer.Builder withRGB(float redIn, float greenIn, float blueIn) {
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
			this.skyColorChanged = true;
			return this;
		}
		
		public InvasionSkyRenderer build(ResourceLocation idIn) {
			ResourceLocation sun = this.sun;
			ResourceLocation moon = this.moon;
			ResourceLocation daySky = this.daySky;
			ResourceLocation nightSky = this.nightSky;
			return new InvasionSkyRenderer(idIn, this.fogRenderer.build(idIn), sun, moon, daySky, nightSky, this.weatherVisibilityChanged, this.brightnessChanged, this.skyColorChanged, this.weatherVisibility, this.brightness, this.red, this.green, this.blue);
		}
		
		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			JsonObject fogRendererObject = this.fogRenderer.serializeToJson();
			if (fogRendererObject != null)
				jsonObject.add("FogRenderer", fogRendererObject);
			if (this.sun != null)
				jsonObject.addProperty("SunTexture", this.sun.toString());
			if (this.moon != null)
				jsonObject.addProperty("MoonTexture", this.moon.toString());
			if (this.daySky != null)
				jsonObject.addProperty("DaySkyTexture", this.daySky.toString());
			if (this.nightSky != null)
				jsonObject.addProperty("NightSkyTexture", this.nightSky.toString());
			if ((this.sun != null || this.moon != null || this.daySky != null || this.nightSky != null) && this.weatherVisibilityChanged)
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
		
		public static InvasionSkyRenderer.Builder fromJson(JsonObject jsonObjectIn) {
			InvasionFogRenderer.Builder fogRenderer = jsonObjectIn.has("FogRenderer") ? InvasionFogRenderer.Builder.fromJson(jsonObjectIn.get("FogRenderer").getAsJsonObject()) : InvasionFogRenderer.Builder.fogRenderer();
			ResourceLocation sun = jsonObjectIn.has("SunTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("SunTexture").getAsString()) : null;
			ResourceLocation moon = jsonObjectIn.has("MoonTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("MoonTexture").getAsString()) : null;
			ResourceLocation daySky = jsonObjectIn.has("DaySkyTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("DaySkyTexture").getAsString()) : null;
			ResourceLocation nightSky = jsonObjectIn.has("NightSkyTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("NightSkyTexture").getAsString()) : null;
			boolean weatherVisibilityChanged = jsonObjectIn.has("WeatherVisibility");
			boolean brightnessChanged = jsonObjectIn.has("Brightness");
			boolean skyColorChanged = jsonObjectIn.has("RedOffset") && jsonObjectIn.has("GreenOffset") && jsonObjectIn.has("BlueOffset");
			float weatherVisibility = weatherVisibilityChanged ? jsonObjectIn.get("WeatherVisibility").getAsFloat() : 0.0F;
			float brightness = brightnessChanged ? 1.0F - jsonObjectIn.get("Brightness").getAsFloat() : 1.0F;
			float red = skyColorChanged ? jsonObjectIn.get("RedOffset").getAsFloat() : 0.0F;
			float green = skyColorChanged ? jsonObjectIn.get("GreenOffset").getAsFloat() : 0.0F;
			float blue = skyColorChanged ? jsonObjectIn.get("BlueOffset").getAsFloat() : 0.0F;
			return new InvasionSkyRenderer.Builder(fogRenderer, sun, moon, daySky, nightSky, weatherVisibilityChanged, brightnessChanged, skyColorChanged, weatherVisibility, brightness, red, green, blue);
		}
		
		public void serializeToNetwork(PacketBuffer bufIn) {
			this.fogRenderer.serializeToNetwork(bufIn);
			bufIn.writeBoolean(this.sun != null);
			if (this.sun != null)
				bufIn.writeResourceLocation(this.sun);
			bufIn.writeBoolean(this.moon != null);
			if (this.moon != null)
				bufIn.writeResourceLocation(this.moon);
			bufIn.writeBoolean(this.daySky != null);
			if (this.daySky != null)
				bufIn.writeResourceLocation(this.daySky);
			bufIn.writeBoolean(this.nightSky != null);
			if (this.nightSky != null)
				bufIn.writeResourceLocation(this.nightSky);
			bufIn.writeBoolean(this.weatherVisibilityChanged);
			bufIn.writeBoolean(this.brightnessChanged);
			bufIn.writeBoolean(this.skyColorChanged);
			bufIn.writeFloat(this.weatherVisibility);
			bufIn.writeFloat(this.brightness);
			bufIn.writeFloat(this.red);
			bufIn.writeFloat(this.green);
			bufIn.writeFloat(this.blue);
		}
		
		public static InvasionSkyRenderer.Builder fromNetwork(PacketBuffer bufIn) {
			InvasionFogRenderer.Builder fogRenderer = InvasionFogRenderer.Builder.fromNetwork(bufIn);
			ResourceLocation sun = null;
			ResourceLocation moon = null;
			ResourceLocation daySky = null;
			ResourceLocation nightSky = null;
			if (bufIn.readBoolean())
				sun = bufIn.readResourceLocation();
			if (bufIn.readBoolean())
				moon = bufIn.readResourceLocation();
			if (bufIn.readBoolean())
				daySky = bufIn.readResourceLocation();
			if (bufIn.readBoolean())
				nightSky = bufIn.readResourceLocation();
			boolean weatherVisibilityChanged = bufIn.readBoolean();
			boolean brightnessChanged = bufIn.readBoolean();
			boolean skyColorChanged = bufIn.readBoolean();
			float weatherVisibility = bufIn.readFloat();
			float brightness = bufIn.readFloat();
			float red = bufIn.readFloat();
			float green = bufIn.readFloat();
			float blue = bufIn.readFloat();
			return new InvasionSkyRenderer.Builder(fogRenderer, sun, moon, daySky, nightSky, weatherVisibilityChanged, brightnessChanged, skyColorChanged, weatherVisibility, brightness, red, green, blue);
		}
	}
}
