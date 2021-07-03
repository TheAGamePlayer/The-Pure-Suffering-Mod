package dev.theagameplayer.puresuffering.client.renderer;

import com.google.gson.JsonObject;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public final class InvasionFogRenderer {
	private final ResourceLocation id;
	private final boolean fogDensityChanged;
	private final boolean fogColorChanged;
	private final float density;
	private final float red, green, blue;
	
	public InvasionFogRenderer(ResourceLocation idIn, boolean fogDensityChangedIn, boolean fogColorChangedIn, float densityIn, float redIn, float greenIn, float blueIn) {
		this.fogDensityChanged = fogDensityChangedIn;
		this.fogColorChanged = fogColorChangedIn;
		this.id = idIn;
		this.density = densityIn;
		this.red = redIn;
		this.green = greenIn;
		this.blue = blueIn;
	}
	
	public InvasionFogRenderer.Builder deconstruct() {
		return new InvasionFogRenderer.Builder(this.fogDensityChanged, this.fogColorChanged, this.density, this.red, this.green, this.blue);
	}
	
	public ResourceLocation getId() {
		return this.id;
	}
	
	public boolean isFogDensityChanged() {
		return this.fogDensityChanged;
	}
	
	public boolean isFogColorChanged() {
		return this.fogColorChanged;
	}
	
	public float getDensityOffset() {
		return this.density;
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
		private boolean fogDensityChanged = false;
		private boolean fogColorChanged = false;
		private float density;
		private float red, green, blue;
		
		private Builder(boolean fogDensityChangedIn, boolean fogColorChangedIn, float densityIn, float redIn, float greenIn, float blueIn) {
			this.fogDensityChanged = fogDensityChangedIn;
			this.fogColorChanged = fogColorChangedIn;
			this.density = densityIn;
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
		}
		
		private Builder() {};
		
		public static InvasionFogRenderer.Builder fogRenderer() {
			return new InvasionFogRenderer.Builder();
		}
		
		public InvasionFogRenderer.Builder density(float densityIn) {
			this.density = densityIn;
			this.fogDensityChanged = true;
			return this;
		}
		
		public InvasionFogRenderer.Builder withRGB(float redIn, float greenIn, float blueIn) {
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
			this.fogColorChanged = true;
			return this;
		}
		
		public InvasionFogRenderer build(ResourceLocation idIn) {
			return new InvasionFogRenderer(idIn, this.fogDensityChanged, this.fogColorChanged, this.density, this.red, this.green, this.blue);
		}
		
		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.fogDensityChanged) {
				jsonObject.addProperty("DensityOffset", this.density);
			}
			if (this.fogColorChanged) {
				jsonObject.addProperty("RedOffset", this.red);
				jsonObject.addProperty("GreenOffset", this.green);
				jsonObject.addProperty("BlueOffset", this.blue);
			}
			return jsonObject.entrySet().isEmpty() ? null : jsonObject;
		}
		
		public static InvasionFogRenderer.Builder fromJson(JsonObject jsonObjectIn) {
			boolean fogDensityChanged = jsonObjectIn.has("DensityOffset");
			boolean fogColorChanged = jsonObjectIn.has("RedOffset") && jsonObjectIn.has("GreenOffset") && jsonObjectIn.has("BlueOffset");
			float density = jsonObjectIn.has("DensityOffset") ? jsonObjectIn.get("DensityOffset").getAsFloat() : 0.0F;
			float red = jsonObjectIn.has("RedOffset") ? jsonObjectIn.get("RedOffset").getAsFloat() : 0.0F;
			float green = jsonObjectIn.has("GreenOffset") ? jsonObjectIn.get("GreenOffset").getAsFloat() : 0.0F;
			float blue = jsonObjectIn.has("BlueOffset") ? jsonObjectIn.get("BlueOffset").getAsFloat() : 0.0F;
			return new InvasionFogRenderer.Builder(fogDensityChanged, fogColorChanged, density, red, green, blue);
		}
		
		public void serializeToNetwork(PacketBuffer bufIn) {
			bufIn.writeBoolean(this.fogDensityChanged);
			bufIn.writeBoolean(this.fogColorChanged);
			bufIn.writeFloat(this.density);
			bufIn.writeFloat(this.red);
			bufIn.writeFloat(this.green);
			bufIn.writeFloat(this.blue);
		}
		
		public static InvasionFogRenderer.Builder fromNetwork(PacketBuffer bufIn) {
			boolean fogDensityChanged = bufIn.readBoolean();
			boolean fogColorChanged = bufIn.readBoolean();
			float density = bufIn.readFloat();
			float red = bufIn.readFloat();
			float green = bufIn.readFloat();
			float blue = bufIn.readFloat();
			return new InvasionFogRenderer.Builder(fogDensityChanged, fogColorChanged, density, red, green, blue);
		}
	}
}
