package dev.theagameplayer.puresuffering.util;

import net.minecraft.util.RandomSource;

public final class PSRGB {
	private final float[] rgb = new float[3];
	
	public PSRGB(final int color1n, final int color2n, final int divIn, final int multIn) {
		final int[][] rgb = new int[2][3];
		rgb[0][0] = color1n >> 16 & 255;
		rgb[0][1] = color1n >> 8 & 255;
		rgb[0][2] = color1n & 255;
		rgb[1][0] = color2n >> 16 & 255;
		rgb[1][1] = color2n >> 8 & 255;
		rgb[1][2] = color2n & 255;
		this.rgb[0] = (Math.min(rgb[0][0], rgb[1][0]) + Math.abs(rgb[0][0] - rgb[1][0])/divIn * multIn)/255.0F;
		this.rgb[1] = (Math.min(rgb[0][1], rgb[1][1]) + Math.abs(rgb[0][1] - rgb[1][1])/divIn * multIn)/255.0F;
		this.rgb[2] = (Math.min(rgb[0][2], rgb[1][2]) + Math.abs(rgb[0][2] - rgb[1][2])/divIn * multIn)/255.0F;
	}
	
	public PSRGB(final RandomSource randomIn, final int color1n, final int color2n) {
		final int[][] rgb = new int[2][3];
		rgb[0][0] = color1n >> 16 & 255;
		rgb[0][1] = color1n >> 8 & 255;
		rgb[0][2] = color1n & 255;
		rgb[1][0] = color2n >> 16 & 255;
		rgb[1][1] = color2n >> 8 & 255;
		rgb[1][2] = color2n & 255;
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
