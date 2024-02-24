package dev.theagameplayer.puresuffering.client.renderer;

import java.util.ArrayList;
import org.joml.Matrix4f;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.util.PSRGB;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.dimension.DimensionType;

public final class ClientEffectsRenderer {
	private static final ResourceLocation TEXTURE = new ResourceLocation("neoforge", "textures/white.png");
	private final ArrayList<SoundRing> soundRings = new ArrayList<>();
	private final ArrayList<VortexParticle> vortexParticles = new ArrayList<>();
	private final InvasionDifficulty difficulty;
	private final boolean canSeeSky;
	private final int[] delays = new int[2];

	public ClientEffectsRenderer(final InvasionDifficulty difficultyIn) {
		final Minecraft mc = Minecraft.getInstance();
		final DimensionType dimType = mc.level.dimensionType();
		this.difficulty = difficultyIn;
		this.canSeeSky = !dimType.hasCeiling();
	}

	public final void tick(final RandomSource randomIn, final long dayTimeIn, final int startTimeIn) {
		if (startTimeIn < 40 && PSConfigValues.client.enableInvasionStartEffects) {
			if (this.delays[0] < 1) {
				if (this.canSeeSky) this.soundRings.add(new SoundRing(this.difficulty, startTimeIn));
				this.delays[0] = this.difficulty.getRingDelay();
			} else {
				this.delays[0]--;
			}
		}
		if (this.difficulty.isHyper()) {
			if (this.delays[1] < 1) {
				this.vortexParticles.add(new VortexParticle(randomIn, this.difficulty));
				this.delays[1] = PSConfigValues.client.vortexParticleDelay;
			} else {
				this.delays[1]--;
			}
		}
		this.soundRings.removeIf(ring -> ring.tick());
		this.vortexParticles.removeIf(particle -> particle.tick(dayTimeIn));
	}

	public final void render(final PoseStack poseStackIn, final float partialTicksIn) { //Uses partial ticks, do computing work on client level tick
		final Minecraft mc = Minecraft.getInstance();
		final float weatherVisibility = mc.level.getRainLevel(partialTicksIn) * 0.15F;
		final long dayTime = mc.level.getDayTime() % 12000L;
		final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.depthMask(Minecraft.useShaderTransparency());
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		this.soundRings.forEach(ring -> ring.render(bufferBuilder, poseStackIn, partialTicksIn));
		if (this.difficulty.isHyper())
			this.vortexParticles.forEach(particle -> particle.render(bufferBuilder, poseStackIn, partialTicksIn, weatherVisibility, dayTime));
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}

	private static final float getAlpha(final float alphaOffsetIn, final long dayTimeIn) {
		float alpha = alphaOffsetIn;
		if (dayTimeIn < Invasion.TRANSITION_TIME) {
			alpha = (alphaOffsetIn/Invasion.TRANSITION_TIME) * (dayTimeIn + 1); //0-1
		} else if (dayTimeIn > 11999L - Invasion.TRANSITION_TIME) {
			alpha = (alphaOffsetIn/Invasion.TRANSITION_TIME) * (12000L - dayTimeIn); //1-0
		}
		return alpha;
	}

	private static final class SoundRing {
		private final float[] rgb = new float[3];
		private final float[] dist = new float[2];
		private final float[] sizeAlpha = new float[2];
		private final float[] length = new float[4];
		private int ticksAlive;

		private SoundRing(final InvasionDifficulty difficultyIn, final int startTimeIn) {
			final PSRGB color = difficultyIn.getInterColor(40, 40 - startTimeIn);
			this.rgb[0] = color.red();
			this.rgb[1] = color.green();
			this.rgb[2] = color.blue();
			this.ticksAlive = Invasion.HALF_TRANSITION;
		}

		private final boolean tick() {
			this.dist[1] = this.dist[0];
			this.dist[0] = Invasion.HALF_TRANSITION - this.ticksAlive;
			this.sizeAlpha[1] = this.sizeAlpha[0];
			this.sizeAlpha[0] = (float)this.ticksAlive/Invasion.HALF_TRANSITION;
			this.length[1] = this.length[0];
			this.length[0] = this.dist[0]/this.sizeAlpha[0];
			this.ticksAlive--;
			return this.ticksAlive < 1;
		}

		private final void render(final BufferBuilder bufferBuilderIn, final PoseStack poseStackIn, final float partialTicksIn) {
			final float dist1 = this.dist[1] + (this.dist[0] - this.dist[1]) * partialTicksIn;
			final float dist2 = -dist1/3;
			final float sizeAlpha = this.sizeAlpha[1] + (this.sizeAlpha[0] - this.sizeAlpha[1]) * partialTicksIn;
			final float length = this.length[1] + (this.length[0] - this.length[1]) * partialTicksIn;
			RenderSystem.setShaderColor(this.rgb[0], this.rgb[1], this.rgb[2], sizeAlpha);
			RenderSystem.setShaderTexture(0, TEXTURE);
			for (final Direction dir : Direction.Plane.HORIZONTAL) {
				poseStackIn.pushPose();
				poseStackIn.mulPose(Axis.YP.rotationDegrees(dir.toYRot()));
				poseStackIn.translate(dist1, dist2, sizeAlpha);
				final Matrix4f matrix4f = poseStackIn.last().pose();
				bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferBuilderIn.vertex(matrix4f, -sizeAlpha, 100.0F, -sizeAlpha * length).endVertex();
				bufferBuilderIn.vertex(matrix4f, sizeAlpha, 100.0F, -sizeAlpha * length).endVertex();
				bufferBuilderIn.vertex(matrix4f, sizeAlpha, 100.0F, sizeAlpha * length).endVertex();
				bufferBuilderIn.vertex(matrix4f, -sizeAlpha, 100.0F, sizeAlpha * length).endVertex();
				BufferUploader.drawWithShader(bufferBuilderIn.end());
				poseStackIn.popPose();
			}
		}
	}

	private static final class VortexParticle {
		private final float speed;
		private final float degreesStart, degreesFinish;
		private final float maxSize, maxAlpha;
		private final float[] rgb = new float[3];
		private final Axis[] axis = new Axis[3];
		private final float[][] rot;
		private final float scaleFactor;
		private float size, a, alpha;
		private float ticksAlive;

		private VortexParticle(final RandomSource randomIn, final InvasionDifficulty difficultyIn) { 
			this.speed = randomIn.nextInt(PSConfigValues.client.maxVortexParticleLifespan - PSConfigValues.client.minVortexParticleLifespan + 1) + PSConfigValues.client.minVortexParticleLifespan;
			this.degreesStart = randomIn.nextInt(360);
			this.degreesFinish = randomIn.nextInt(360) + 180;
			this.maxSize = randomIn.nextFloat() + 0.75F;
			this.maxAlpha = randomIn.nextFloat() * 0.5F + 0.5F;
			final PSRGB color = difficultyIn.getRandomColor(randomIn);
			this.rgb[0] = color.red();
			this.rgb[1] = color.green();
			this.rgb[2] = color.blue();
			this.axis[0] = difficultyIn.isNightmare() ? Axis.YN : Axis.YP;
			this.axis[1] = randomIn.nextBoolean() ? Axis.XP : Axis.XN;
			this.axis[2] = randomIn.nextBoolean() ? Axis.ZP : Axis.ZN;
			this.rot = new float[PSConfigValues.client.useFastEffects ? 2 : randomIn.nextInt(3) + 3][3];
			this.scaleFactor = 1.0F/(this.rot.length - 1);
			this.ticksAlive = this.speed;
		}

		private final boolean tick(final long dayTimeIn) {
			final float percentAlive = this.ticksAlive/this.speed;
			this.a = this.vortexValue(this.a, this.maxAlpha, dayTimeIn);
			this.alpha = getAlpha(this.a, dayTimeIn);
			for (int i = this.rot.length - 1; i > 0; i--) {
				this.rot[i][0] = this.rot[i - 1][0];
				this.rot[i][1] = this.rot[i - 1][1];
				this.rot[i][2] = this.rot[i - 1][2];
			}
			this.rot[0][0] = this.degreesStart + percentAlive * this.degreesFinish;
			this.rot[0][1] = percentAlive > 0.5F ? percentAlive * 180.0F : 180.0F - percentAlive * 180.0F;
			this.rot[0][2] = percentAlive > 0.5F ? 180.0F + percentAlive * 180.0F : 180.0F + percentAlive * 180.0F;
			this.size = this.vortexValue(this.size, this.maxSize, dayTimeIn);
			this.ticksAlive--;
			return this.ticksAlive < 1;
		}

		private final float vortexValue(final float currentIn, final float maxIn, final long dayTimeIn) {
			final float result = this.ticksAlive > this.speed/2 ? maxIn/this.speed * (this.speed - this.ticksAlive) : maxIn/this.speed * this.ticksAlive;
			if (dayTimeIn < Invasion.HALF_TRANSITION) {
				return result + currentIn/Invasion.HALF_TRANSITION; //0-1
			} else if (dayTimeIn > 11999L - Invasion.HALF_TRANSITION) {
				return result - currentIn/Invasion.HALF_TRANSITION; //1-0
			}
			return result;
		}

		private final void render(final BufferBuilder bufferBuilderIn, final PoseStack poseStackIn, final float partialTicksIn, final float weatherVisibilityIn, final long dayTimeIn) {
			final float alpha = this.alpha - ClientTransitionHandler.getWeatherVisibility(weatherVisibilityIn, dayTimeIn);
			final int length = Minecraft.useFancyGraphics() ? this.rot.length : 2;
			RenderSystem.setShaderTexture(0, TEXTURE);
			for (int i = 1; i < length; i++) {
				final float s = this.scaleFactor * (this.rot.length - i);
				final float size = this.size * s;
				RenderSystem.setShaderColor(this.rgb[0] * s, this.rgb[1] * s, this.rgb[2] * s, alpha * s);
				poseStackIn.pushPose();
				poseStackIn.mulPose(this.axis[0].rotationDegrees(this.rot[i][0] + (this.rot[i - 1][0] - this.rot[i][0]) * partialTicksIn));
				poseStackIn.mulPose(this.axis[1].rotationDegrees(this.rot[i][1] + (this.rot[i - 1][1] - this.rot[i][1]) * partialTicksIn));
				poseStackIn.mulPose(this.axis[2].rotationDegrees(this.rot[i][2] + (this.rot[i - 1][2] - this.rot[i][2]) * partialTicksIn));
				final Matrix4f matrix4f = poseStackIn.last().pose();
				bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferBuilderIn.vertex(matrix4f, -size, 100.0F, -size).endVertex();
				bufferBuilderIn.vertex(matrix4f, size, 100.0F, -size).endVertex();
				bufferBuilderIn.vertex(matrix4f, size, 100.0F, size).endVertex();
				bufferBuilderIn.vertex(matrix4f, -size, 100.0F, size).endVertex();
				BufferUploader.drawWithShader(bufferBuilderIn.end());
				poseStackIn.popPose();
			}
		}
	}
}
