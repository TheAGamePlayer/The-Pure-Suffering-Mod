package dev.theagameplayer.puresuffering.client.invasion;

import org.apache.commons.lang3.BooleanUtils;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public final class ClientInvasion {
	private final InvasionSkyRenderInfo renderer;
	private final boolean isPrimary;
	private final int severity, mobCap;
	private final int maxSeverity;
	private final int rarity, tier;
	private final Component component;
	private final boolean[] ticks = new boolean[4];
	private final boolean noTick;
	private final int[] flickerDelays = new int[4];
	private final int[][] flickerCooldown = new int[4][2];
	private final float[][] flicker = new float[4][0];

	public ClientInvasion(final InvasionSkyRenderInfo rendererIn, final boolean isPrimaryIn, final int severityIn, final int mobCapIn, final int maxSeverityIn, final int rarityIn, final int tierIn, final Component componentIn) {
		this.renderer = rendererIn;
		this.isPrimary = isPrimaryIn;
		this.severity = severityIn;
		this.mobCap = mobCapIn;
		this.maxSeverity = maxSeverityIn;
		this.rarity = rarityIn;
		this.tier = tierIn;
		this.component = componentIn;
		this.ticks[0] = rendererIn.getFogRenderInfo().doesFogColorFlicker();
		this.ticks[1] = rendererIn.doesSunAndMoonVisibilityFlicker();
		this.ticks[2] = rendererIn.doesBrightnessFlicker();
		this.ticks[3] = rendererIn.doesSkyColorFlicker();
		this.noTick = !BooleanUtils.or(this.ticks);
	}

	public final InvasionSkyRenderInfo getSkyRenderInfo() {
		return this.renderer;
	}

	public final boolean isPrimary() {
		return this.isPrimary;
	}
	
	public final int getSeverity() {
		return this.severity;
	}
	
	public final int getMobCap() {
		return this.mobCap;
	}
	
	public final int getMaxSeverity() {
		return this.maxSeverity;
	}
	
	public final int getRarity() {
		return this.rarity;
	}
	
	public final int getTier() {
		return this.tier;
	}
	
	public final Component getComponent() {
		return this.component;
	}

	public final void tick(final RandomSource randomIn, final long dayTimeIn) {
		if (this.noTick) return;
		if (PSConfigValues.client.enableSkyFlickering && dayTimeIn > Invasion.HALF_TRANSITION && dayTimeIn < 12000L - Invasion.HALF_TRANSITION) {
			if (this.ticks[0] && this.flickerCooldown[3][1] < 1) {
				if (this.flickerDelays[3] < 0) {
					final int index = randomIn.nextInt(this.renderer.getFogRenderInfo().getFlickerRGBSize());
					final float[] fRGB = this.renderer.getFogRenderInfo().getFlickerRGBOffset(index);
					this.flickerDelays[3] = randomIn.nextIntBetweenInclusive((int)fRGB[3], (int)fRGB[4]);
					this.flicker[3] = new float[] {fRGB[0], fRGB[1], fRGB[2]};
					this.flickerCooldown[3][0] = 40 + randomIn.nextInt(61);
					this.flickerCooldown[3][1] = this.flickerCooldown[3][0];
				} else {
					this.flickerDelays[3]--;
				}
			}
			if (this.ticks[1] && this.flickerCooldown[2][1] < 1) {
				if (this.flickerDelays[2] < 0) {
					final float[] fVisibility = this.renderer.getFlickerVisibility();
					this.flickerDelays[2] = randomIn.nextIntBetweenInclusive((int)fVisibility[2], (int)fVisibility[3]);
					this.flicker[2] = new float[] {fVisibility[0] + randomIn.nextFloat() * (fVisibility[1] - fVisibility[0])};
					this.flickerCooldown[2][0] = 40 + randomIn.nextInt(61);
					this.flickerCooldown[2][1] = this.flickerCooldown[2][0];
				} else {
					this.flickerDelays[2]--;
				}
			}
			if (this.ticks[2] && this.flickerCooldown[1][1] < 1) {
				if (this.flickerDelays[1] < 0) {
					final float[] fBrightness = this.renderer.getFlickerBrightness();
					this.flickerDelays[1] = randomIn.nextIntBetweenInclusive((int)fBrightness[2], (int)fBrightness[3]);
					this.flicker[1] = new float[] {fBrightness[0] + randomIn.nextFloat() * (fBrightness[1] - fBrightness[0])};
					this.flickerCooldown[1][0] = 40 + randomIn.nextInt(61);
					this.flickerCooldown[1][1] = this.flickerCooldown[1][0];
				} else {
					this.flickerDelays[1]--;
				}
			}
			if (this.ticks[3] && this.flickerCooldown[0][1] < 1) {
				if (this.flickerDelays[0] < 0) {
					final int index = randomIn.nextInt(this.renderer.getFlickerRGBSize());
					final float[] fRGB = this.renderer.getFlickerRGBOffset(index);
					this.flickerDelays[0] = randomIn.nextIntBetweenInclusive((int)fRGB[3], (int)fRGB[4]);
					this.flicker[0] = new float[] {fRGB[0], fRGB[1], fRGB[2]};
					this.flickerCooldown[0][0] = 40 + randomIn.nextInt(61);
					this.flickerCooldown[0][1] = this.flickerCooldown[0][0];
				} else {
					this.flickerDelays[0]--;
				}
			}
		}
	}

	public final void flickerFogRGB(final float[] rgbIn) {
		if (this.flicker[3].length == 0) return;
		rgbIn[0] += this.flicker[3][0]/this.flickerCooldown[3][0] * this.flickerCooldown[3][1];
		rgbIn[1] += this.flicker[3][1]/this.flickerCooldown[3][0] * this.flickerCooldown[3][1];
		rgbIn[2] += this.flicker[3][2]/this.flickerCooldown[3][0] * this.flickerCooldown[3][1];
		this.flickerCooldown[3][1]--;
		if (this.flickerCooldown[3][1] < 1) this.flicker[3] = new float[0];
	}

	public final float flickerAlpha(final float alphaIn) {
		if (this.flicker[2].length == 0) return alphaIn;
		final float alpha = this.flicker[2][0]/this.flickerCooldown[2][0] * this.flickerCooldown[2][1];
		this.flickerCooldown[2][1]--;
		if (this.flickerCooldown[2][1] < 1) this.flicker[2] = new float[0];
		return alphaIn + alpha;
	}

	public final float flickerBrightness(final float brightnessIn) {
		if (this.flicker[1].length == 0) return brightnessIn;
		final float brightness = this.flicker[1][0]/this.flickerCooldown[1][0] * this.flickerCooldown[1][1];
		this.flickerCooldown[1][1]--;
		if (this.flickerCooldown[1][1] < 1) this.flicker[1] = new float[0];
		return brightnessIn - brightness;
	}

	public final void flickerSkyRGB(final float[] rgbIn) {
		if (this.flicker[0].length == 0) return;
		rgbIn[0] += this.flicker[0][0]/this.flickerCooldown[0][0] * this.flickerCooldown[0][1];
		rgbIn[1] += this.flicker[0][1]/this.flickerCooldown[0][0] * this.flickerCooldown[0][1];
		rgbIn[2] += this.flicker[0][2]/this.flickerCooldown[0][0] * this.flickerCooldown[0][1];
		this.flickerCooldown[0][1]--;
		if (this.flickerCooldown[0][1] < 1) this.flicker[0] = new float[0];
	}
}
