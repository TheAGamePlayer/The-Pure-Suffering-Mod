package dev.theagameplayer.puresuffering.client.renderer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

public class InvasionSkyRenderer {
	private final ResourceLocation id;
	private final InvasionFogRenderer fogRenderer;
	private final ResourceLocation sun;
	private final ResourceLocation moon;
	private final ResourceLocation daySky;
	private final ResourceLocation nightSky;
	private final boolean weatherVisibilityChanged;
	private final float weatherVisibility;
	private final boolean skyColorChanged;
	private final float red, green, blue;
	
	public InvasionSkyRenderer(ResourceLocation idIn, InvasionFogRenderer fogRendererIn, ResourceLocation sunIn, ResourceLocation moonIn, ResourceLocation daySkyIn, ResourceLocation nightSkyIn, boolean weatherVisibilityChangedIn, boolean skyColorChangedIn, float weatherVisibilityIn, float redIn, float greenIn, float blueIn) {
		this.fogRenderer = fogRendererIn;
		this.id = idIn;
		this.sun = sunIn;
		this.moon = moonIn;
		this.daySky = daySkyIn;
		this.nightSky = nightSkyIn;
		this.weatherVisibilityChanged = weatherVisibilityChangedIn;
		this.skyColorChanged = skyColorChangedIn;
		this.weatherVisibility = weatherVisibilityIn;
		this.red = redIn;
		this.green = greenIn;
		this.blue = blueIn;
	}
	
	public InvasionSkyRenderer.Builder deconstruct() {
		return new InvasionSkyRenderer.Builder(this.fogRenderer.deconstruct(), this.sun, this.moon, this.daySky, this.nightSky, this.weatherVisibilityChanged, this.skyColorChanged, this.weatherVisibility, this.red, this.green, this.blue);
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
	
	public boolean isSkyColorChanged() {
		return this.skyColorChanged;
	}
	
	public float getWeatherVisibilityIn() {
		return this.weatherVisibility;
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
	
	public static class Builder {
		private InvasionFogRenderer.Builder fogRenderer = InvasionFogRenderer.Builder.fogRenderer();
		private ResourceLocation sun;
		private ResourceLocation moon;
		private ResourceLocation daySky;
		private ResourceLocation nightSky;
		private boolean weatherVisibilityChanged = false;
		private boolean skyColorChanged = false;
		private float weatherVisibility;
		private float red, green, blue;
		
		private Builder(InvasionFogRenderer.Builder fogRendererIn, @Nullable ResourceLocation sunIn, @Nullable ResourceLocation moonIn, @Nullable ResourceLocation daySkyIn, @Nullable ResourceLocation nightSkyIn, boolean weatherVisibilityChangedIn, boolean skyColorChangedIn, float weatherVisibilityIn, float redIn, float greenIn, float blueIn) {
			this.fogRenderer = fogRendererIn;
			this.sun = sunIn;
			this.moon = moonIn;
			this.daySky = daySkyIn;
			this.nightSky = nightSkyIn;
			this.weatherVisibilityChanged = weatherVisibilityChangedIn;
			this.skyColorChanged = skyColorChangedIn;
			this.weatherVisibility = weatherVisibilityIn;
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
			return new InvasionSkyRenderer(idIn, this.fogRenderer.build(idIn), sun, moon, daySky, nightSky, this.weatherVisibilityChanged, this.skyColorChanged, this.weatherVisibility, this.red, this.green, this.blue);
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
				jsonObject.addProperty("DayTexture", this.daySky.toString());
			if (this.nightSky != null)
				jsonObject.addProperty("NightTexture", this.nightSky.toString());
			if ((this.sun != null || this.moon != null || this.daySky != null || this.nightSky != null) && this.weatherVisibilityChanged)
				jsonObject.addProperty("WeatherVisibility", this.weatherVisibility);
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
			ResourceLocation daySky = jsonObjectIn.has("DayTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("DayTexture").getAsString()) : null;
			ResourceLocation nightSky = jsonObjectIn.has("NightTexture") ? ResourceLocation.tryParse(jsonObjectIn.get("NightTexture").getAsString()) : null;
			boolean weatherVisibilityChanged = jsonObjectIn.has("WeatherVisibility");
			boolean skyColorChanged = jsonObjectIn.has("RedOffset") && jsonObjectIn.has("GreenOffset") && jsonObjectIn.has("BlueOffset");
			float weatherVisibility = jsonObjectIn.has("WeatherVisibility") ? jsonObjectIn.get("WeatherVisibility").getAsFloat() : 0.0F;
			float red = jsonObjectIn.has("RedOffset") ? jsonObjectIn.get("RedOffset").getAsFloat() : 0.0F;
			float green = jsonObjectIn.has("GreenOffset") ? jsonObjectIn.get("GreenOffset").getAsFloat() : 0.0F;
			float blue = jsonObjectIn.has("BlueOffset") ? jsonObjectIn.get("BlueOffset").getAsFloat() : 0.0F;
			return new InvasionSkyRenderer.Builder(fogRenderer, sun, moon, daySky, nightSky, weatherVisibilityChanged, skyColorChanged, weatherVisibility, red, green, blue);
		}
	}
}
