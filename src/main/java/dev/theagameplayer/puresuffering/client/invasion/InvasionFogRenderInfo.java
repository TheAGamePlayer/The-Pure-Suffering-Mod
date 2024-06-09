package dev.theagameplayer.puresuffering.client.invasion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class InvasionFogRenderInfo {
	private final ResourceLocation id;
	private final boolean[] changes;
	private final float[][] fRGB;
	private final float[] rgb;
	
	public InvasionFogRenderInfo(final ResourceLocation pId, final boolean[] pChanges, final float[][] pFRGB, final float[] pRGB) {
		this.id = pId;
		this.changes = pChanges;
		this.fRGB = pFRGB;
		this.rgb = pRGB;
	}
	
	public final InvasionFogRenderInfo.Builder deconstruct() {
		return new InvasionFogRenderInfo.Builder(this.changes, this.fRGB, this.rgb);
	}
	
	public final ResourceLocation getId() {
		return this.id;
	}
	
	public final boolean doesFogColorFlicker() {
		return this.changes[1];
	}
	
	public final boolean isFogColorChanged() {
		return this.changes[0];
	}
	
	public final float[] getFlickerRGBOffset(final int pIndex) {
		return this.fRGB[pIndex];
	}
	
	public final int getFlickerRGBSize() {
		return this.fRGB.length;
	}
	
	public final float getRGBOffset(final int pValue) {
		return this.rgb[pValue];
	}

	@Override
	public final String toString() {
		return this.id.toString();
	}
	
	public static final class Builder {
		private boolean[] changes = new boolean[2];
		private float[][] fRGB = new float[0][5];
		private float[] rgb = new float[3];
		
		private Builder(final boolean[] pChanges, final float[][] pFRGB, final float[] pRGB) {
			this.changes = pChanges;
			this.fRGB = pFRGB;
			this.rgb = pRGB;
		}
		
		private Builder() {};
		
		public final static InvasionFogRenderInfo.Builder fogRenderer() {
			return new InvasionFogRenderInfo.Builder();
		}
		
		public final InvasionFogRenderInfo.Builder withFlickerRGB(final float pRed, final float pGreen, final float pBlue, final int pMinDelay, final int pMaxDelay) {
			final int l = this.fRGB.length;
			final float[][] fRGB = new float[l + 1][5];
			for (int i1 = 0; i1 < l; i1++) {
				for (int i2 = 0; i2 < 5; i2++)
					fRGB[i1][i2] = this.fRGB[i1][i2];
			}
			this.fRGB = fRGB;
			this.fRGB[l][0] = pRed;
			this.fRGB[l][1] = pGreen;
			this.fRGB[l][2] = pBlue;
			this.fRGB[l][3] = pMinDelay;
			this.fRGB[l][4] = pMaxDelay;
			this.changes[1] = true;
			return this;
		}
		
		public final InvasionFogRenderInfo.Builder withRGB(final float pRed, final float pGreen, final float pBlue) {
			this.rgb[0] = pRed;
			this.rgb[1] = pGreen;
			this.rgb[2] = pBlue;
			this.changes[0] = true;
			return this;
		}
		
		public final InvasionFogRenderInfo build(final ResourceLocation pId) {
			return new InvasionFogRenderInfo(pId, this.changes, this.fRGB, this.rgb);
		}
		
		public final JsonObject serializeToJson() {
			final JsonObject jsonObject = new JsonObject();
			if (this.changes[1]) {
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
			if (this.changes[0]) {
				final JsonArray a = new JsonArray();
				a.add(this.rgb[0]);
				a.add(this.rgb[1]);
				a.add(this.rgb[2]);
				jsonObject.add("RGBOffset", a);
			}
			return jsonObject.entrySet().isEmpty() ? null : jsonObject;
		}
		
		public final static InvasionFogRenderInfo.Builder fromJson(final JsonObject pJsonObject) {
			final boolean[] changes = new boolean[2];
			changes[0] = pJsonObject.has("RGBOffset");
			changes[1] = pJsonObject.has("FlickerRGBOffset");
			final JsonElement fRGBElement = changes[1] ? pJsonObject.get("FlickerRGBOffset") : null;
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
			final JsonElement rgbElement = changes[0] ? pJsonObject.get("RGBOffset") : null;
			final float[] rgb = new float[3];
			if (rgbElement != null && rgbElement.isJsonArray() && !rgbElement.getAsJsonArray().isEmpty()) {
				final JsonArray a = rgbElement.getAsJsonArray();
				rgb[0] = a.get(0).getAsFloat();
				rgb[1] = a.get(1).getAsFloat();
				rgb[2] = a.get(2).getAsFloat();
			}
			return new InvasionFogRenderInfo.Builder(changes, fRGB, rgb);
		}
		
		public final void serializeToNetwork(final FriendlyByteBuf pBuf) {
			pBuf.writeBoolean(this.changes[0]);
			pBuf.writeBoolean(this.changes[1]);
			pBuf.writeInt(this.fRGB.length);
			for (int i = 0; i < this.fRGB.length; i++) {
				pBuf.writeFloat(this.fRGB[i][0]);
				pBuf.writeFloat(this.fRGB[i][1]);
				pBuf.writeFloat(this.fRGB[i][2]);
				pBuf.writeFloat(this.fRGB[i][3]);
				pBuf.writeFloat(this.fRGB[i][4]);
			}
			pBuf.writeFloat(this.rgb[0]);
			pBuf.writeFloat(this.rgb[1]);
			pBuf.writeFloat(this.rgb[2]);
		}
		
		public final static InvasionFogRenderInfo.Builder fromNetwork(final FriendlyByteBuf pBuf) {
			final boolean[] changes = new boolean[2];
			changes[0] = pBuf.readBoolean();
			changes[1] = pBuf.readBoolean();
			final float[][] fRGB = new float[pBuf.readInt()][5];
			for (int i = 0; i < fRGB.length; i++) {
				fRGB[i][0] = pBuf.readFloat();
				fRGB[i][1] = pBuf.readFloat();
				fRGB[i][2] = pBuf.readFloat();
				fRGB[i][3] = pBuf.readFloat();
				fRGB[i][4] = pBuf.readFloat();
			}
			final float[] rgb = new float[3];
			rgb[0] = pBuf.readFloat();
			rgb[1] = pBuf.readFloat();
			rgb[2] = pBuf.readFloat();
			return new InvasionFogRenderInfo.Builder(changes, fRGB, rgb);
		}
	}
}
