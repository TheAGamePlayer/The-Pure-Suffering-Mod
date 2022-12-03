package dev.theagameplayer.puresuffering.client.renderer;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class InvasionFogRenderer {
	private final ResourceLocation id;
	private final boolean fogColorChanged;
	private final float red, green, blue;
	
	public InvasionFogRenderer(final ResourceLocation idIn, final boolean fogColorChangedIn, final float redIn, final float greenIn, final float blueIn) {
		this.fogColorChanged = fogColorChangedIn;
		this.id = idIn;
		this.red = redIn;
		this.green = greenIn;
		this.blue = blueIn;
	}
	
	public final InvasionFogRenderer.Builder deconstruct() {
		return new InvasionFogRenderer.Builder(this.fogColorChanged, this.red, this.green, this.blue);
	}
	
	public final ResourceLocation getId() {
		return this.id;
	}
	
	public final boolean isFogColorChanged() {
		return this.fogColorChanged;
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

	public static final class Builder {
		private boolean fogColorChanged;
		private float red, green, blue;
		
		private Builder(final boolean fogColorChangedIn, final float redIn, final float greenIn, final float blueIn) {
			this.fogColorChanged = fogColorChangedIn;
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
		}
		
		private Builder() {};
		
		public final static InvasionFogRenderer.Builder fogRenderer() {
			return new InvasionFogRenderer.Builder();
		}
		
		public final InvasionFogRenderer.Builder withRGB(final float redIn, final float greenIn, final float blueIn) {
			this.red = redIn;
			this.green = greenIn;
			this.blue = blueIn;
			this.fogColorChanged = true;
			return this;
		}
		
		public final InvasionFogRenderer build(final ResourceLocation idIn) {
			return new InvasionFogRenderer(idIn, this.fogColorChanged, this.red, this.green, this.blue);
		}
		
		public final JsonObject serializeToJson() {
			final JsonObject jsonObject = new JsonObject();
			if (this.fogColorChanged) {
				jsonObject.addProperty("RedOffset", this.red);
				jsonObject.addProperty("GreenOffset", this.green);
				jsonObject.addProperty("BlueOffset", this.blue);
			}
			return jsonObject.entrySet().isEmpty() ? null : jsonObject;
		}
		
		public final static InvasionFogRenderer.Builder fromJson(final JsonObject jsonObjectIn) {
			final boolean fogColorChanged = jsonObjectIn.has("RedOffset") && jsonObjectIn.has("GreenOffset") && jsonObjectIn.has("BlueOffset");
			final float red = fogColorChanged ? jsonObjectIn.get("RedOffset").getAsFloat() : 0.0F;
			final float green = fogColorChanged ? jsonObjectIn.get("GreenOffset").getAsFloat() : 0.0F;
			final float blue = fogColorChanged ? jsonObjectIn.get("BlueOffset").getAsFloat() : 0.0F;
			return new InvasionFogRenderer.Builder(fogColorChanged, red, green, blue);
		}
		
		public final void serializeToNetwork(final FriendlyByteBuf bufIn) {
			bufIn.writeBoolean(this.fogColorChanged);
			bufIn.writeFloat(this.red);
			bufIn.writeFloat(this.green);
			bufIn.writeFloat(this.blue);
		}
		
		public final static InvasionFogRenderer.Builder fromNetwork(final FriendlyByteBuf bufIn) {
			final boolean fogColorChanged = bufIn.readBoolean();
			final float red = bufIn.readFloat();
			final float green = bufIn.readFloat();
			final float blue = bufIn.readFloat();
			return new InvasionFogRenderer.Builder(fogColorChanged, red, green, blue);
		}
	}
}
