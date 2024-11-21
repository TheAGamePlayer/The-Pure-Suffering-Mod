package dev.theagameplayer.puresuffering.util;

import net.minecraft.util.RandomSource;

public final class PSRGB {
	private final float[] rgb = new float[3];
	
	public PSRGB(final int pColor1, final int pColor2, final int pDiv, final int pMult) {
		final int[][] rgb = new int[2][3];
		rgb[0][0] = pColor1 >> 16 & 255;
		rgb[0][1] = pColor1 >> 8 & 255;
		rgb[0][2] = pColor1 & 255;
		rgb[1][0] = pColor2 >> 16 & 255;
		rgb[1][1] = pColor2 >> 8 & 255;
		rgb[1][2] = pColor2 & 255;
		this.rgb[0] = (Math.min(rgb[0][0], rgb[1][0]) + Math.abs(rgb[0][0] - rgb[1][0])/pDiv * pMult)/255.0F;
		this.rgb[1] = (Math.min(rgb[0][1], rgb[1][1]) + Math.abs(rgb[0][1] - rgb[1][1])/pDiv * pMult)/255.0F;
		this.rgb[2] = (Math.min(rgb[0][2], rgb[1][2]) + Math.abs(rgb[0][2] - rgb[1][2])/pDiv * pMult)/255.0F;
	}
	
	public PSRGB(final RandomSource randomIn, final int pColor1, final int pColor2) {
		final int[][] rgb = new int[2][3];
		rgb[0][0] = pColor1 >> 16 & 255;
		rgb[0][1] = pColor1 >> 8 & 255;
		rgb[0][2] = pColor1 & 255;
		rgb[1][0] = pColor2 >> 16 & 255;
		rgb[1][1] = pColor2 >> 8 & 255;
		rgb[1][2] = pColor2 & 255;
		this.rgb[0] = (Math.min(rgb[0][0], rgb[1][0]) + randomIn.nextInt(Math.abs(rgb[0][0] - rgb[1][0]) + 1))/255.0F;
		this.rgb[1] = (Math.min(rgb[0][1], rgb[1][1]) + randomIn.nextInt(Math.abs(rgb[0][1] - rgb[1][1]) + 1))/255.0F;
		this.rgb[2] = (Math.min(rgb[0][2], rgb[1][2]) + randomIn.nextInt(Math.abs(rgb[0][2] - rgb[1][2]) + 1))/255.0F;
	}

	public final float red() {
		return this.rgb[0];
	}

	public final float green() {
		return this.rgb[1];
	}

	public final float blue() {
		return this.rgb[2];
	}
}
