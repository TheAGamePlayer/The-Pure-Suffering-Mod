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
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("neoforge", "textures/white.png");
	private final ArrayList<SoundRing> soundRings = new ArrayList<>();
	private final ArrayList<VortexParticle> vortexParticles = new ArrayList<>();
	private final InvasionDifficulty difficulty;
	private final boolean canSeeSky;
	private final int[] delays = new int[2];

	public ClientEffectsRenderer(final InvasionDifficulty pDifficulty) {
		final Minecraft mc = Minecraft.getInstance();
		final DimensionType dimType = mc.level.dimensionType();
		this.difficulty = pDifficulty;
		this.canSeeSky = !dimType.hasCeiling();
	}

	public final void tick(final RandomSource pRandom, final long pDayTime, final int pStartTime) {
		if (pStartTime < 40 && PSConfigValues.client.enableInvasionStartEffects) {
			if (this.delays[0] < 1) {
				if (this.canSeeSky) this.soundRings.add(new SoundRing(this.difficulty, pStartTime));
				this.delays[0] = this.difficulty.getRingDelay();
			} else {
				this.delays[0]--;
			}
		}
		if (this.difficulty.isHyper()) {
			if (this.delays[1] < 1) {
				this.vortexParticles.add(new VortexParticle(pRandom, this.difficulty));
				this.delays[1] = PSConfigValues.client.vortexParticleDelay;
			} else {
				this.delays[1]--;
			}
		}
		this.soundRings.removeIf(ring -> ring.tick());
		this.vortexParticles.removeIf(particle -> particle.tick(pDayTime));
	}

	public final void render(final PoseStack pPoseStack, final float pPartialTicks) { //Uses partial ticks, do computing work on client level tick
		final Minecraft mc = Minecraft.getInstance();
		final float weatherVisibility = mc.level.getRainLevel(pPartialTicks) * 0.15F;
		final long dayTime = mc.level.getDayTime() % 12000L;
		RenderSystem.depthMask(Minecraft.useShaderTransparency());
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		this.soundRings.forEach(ring -> ring.render(pPoseStack, pPartialTicks));
		if (this.difficulty.isHyper())
			this.vortexParticles.forEach(particle -> particle.render(pPoseStack, pPartialTicks, weatherVisibility, dayTime));
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}

	private static final float getAlpha(final float pAlphaOffset, final long pDayTime) {
		float alpha = pAlphaOffset;
		if (pDayTime < Invasion.TRANSITION_TIME) {
			alpha = (pAlphaOffset/Invasion.TRANSITION_TIME) * (pDayTime + 1); //0-1
		} else if (pDayTime > 11999L - Invasion.TRANSITION_TIME) {
			alpha = (pAlphaOffset/Invasion.TRANSITION_TIME) * (12000L - pDayTime); //1-0
		}
		return alpha;
	}

	private static final class SoundRing {
		private final float[] rgb = new float[3];
		private final float[] dist = new float[2];
		private final float[] sizeAlpha = new float[2];
		private final float[] length = new float[4];
		private int ticksAlive;

		private SoundRing(final InvasionDifficulty pDifficulty, final int pStartTime) {
			final PSRGB color = pDifficulty.getInterColor(40, 40 - pStartTime);
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

		private final void render(final PoseStack pPoseStack, final float pPartialTicks) {
			final float dist1 = this.dist[1] + (this.dist[0] - this.dist[1]) * pPartialTicks;
			final float dist2 = -dist1/3;
			final float sizeAlpha = this.sizeAlpha[1] + (this.sizeAlpha[0] - this.sizeAlpha[1]) * pPartialTicks;
			final float length = this.length[1] + (this.length[0] - this.length[1]) * pPartialTicks;
			RenderSystem.setShaderColor(this.rgb[0], this.rgb[1], this.rgb[2], sizeAlpha);
			RenderSystem.setShaderTexture(0, TEXTURE);
			for (final Direction dir : Direction.Plane.HORIZONTAL) {
				pPoseStack.pushPose();
				pPoseStack.mulPose(Axis.YP.rotationDegrees(dir.toYRot()));
				pPoseStack.translate(dist1, dist2, sizeAlpha);
				final Matrix4f matrix4f = pPoseStack.last().pose();
				final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferBuilder.addVertex(matrix4f, -sizeAlpha, 100.0F, -sizeAlpha * length);
				bufferBuilder.addVertex(matrix4f, sizeAlpha, 100.0F, -sizeAlpha * length);
				bufferBuilder.addVertex(matrix4f, sizeAlpha, 100.0F, sizeAlpha * length);
				bufferBuilder.addVertex(matrix4f, -sizeAlpha, 100.0F, sizeAlpha * length);
				BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
				pPoseStack.popPose();
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

		private VortexParticle(final RandomSource pRandom, final InvasionDifficulty pDifficulty) { 
			this.speed = pRandom.nextInt(PSConfigValues.client.maxVortexParticleLifespan - PSConfigValues.client.minVortexParticleLifespan + 1) + PSConfigValues.client.minVortexParticleLifespan;
			this.degreesStart = pRandom.nextInt(360);
			this.degreesFinish = pRandom.nextInt(360) + 180;
			this.maxSize = pRandom.nextFloat() + 0.75F;
			this.maxAlpha = pRandom.nextFloat() * 0.5F + 0.5F;
			final PSRGB color = pDifficulty.getRandomColor(pRandom);
			this.rgb[0] = color.red();
			this.rgb[1] = color.green();
			this.rgb[2] = color.blue();
			this.axis[0] = pDifficulty.isNightmare() ? Axis.YN : Axis.YP;
			this.axis[1] = pRandom.nextBoolean() ? Axis.XP : Axis.XN;
			this.axis[2] = pRandom.nextBoolean() ? Axis.ZP : Axis.ZN;
			this.rot = new float[PSConfigValues.client.useFastEffects ? 2 : pRandom.nextInt(3) + 3][3];
			this.scaleFactor = 1.0F/(this.rot.length - 1);
			this.ticksAlive = this.speed;
		}

		private final boolean tick(final long pDayTime) {
			final float percentAlive = this.ticksAlive/this.speed;
			this.a = this.vortexValue(this.a, this.maxAlpha, pDayTime);
			this.alpha = getAlpha(this.a, pDayTime);
			for (int i = this.rot.length - 1; i > 0; i--) {
				this.rot[i][0] = this.rot[i - 1][0];
				this.rot[i][1] = this.rot[i - 1][1];
				this.rot[i][2] = this.rot[i - 1][2];
			}
			this.rot[0][0] = this.degreesStart + percentAlive * this.degreesFinish;
			this.rot[0][1] = percentAlive > 0.5F ? percentAlive * 180.0F : 180.0F - percentAlive * 180.0F;
			this.rot[0][2] = percentAlive > 0.5F ? 180.0F + percentAlive * 180.0F : 180.0F + percentAlive * 180.0F;
			this.size = this.vortexValue(this.size, this.maxSize, pDayTime);
			this.ticksAlive--;
			return this.ticksAlive < 1;
		}

		private final float vortexValue(final float pCurrent, final float pMax, final long pDayTime) {
			final float result = this.ticksAlive > this.speed/2 ? pMax/this.speed * (this.speed - this.ticksAlive) : pMax/this.speed * this.ticksAlive;
			if (pDayTime < Invasion.HALF_TRANSITION) {
				return result + pCurrent/Invasion.HALF_TRANSITION; //0-1
			} else if (pDayTime > 11999L - Invasion.HALF_TRANSITION) {
				return result - pCurrent/Invasion.HALF_TRANSITION; //1-0
			}
			return result;
		}

		private final void render(final PoseStack pPoseStack, final float pPartialTicks, final float pWeatherVisibility, final long pDayTime) {
			final float alpha = this.alpha - ClientTransitionHandler.getWeatherVisibility(pWeatherVisibility, pDayTime);
			final int length = Minecraft.useFancyGraphics() ? this.rot.length : 2;
			RenderSystem.setShaderTexture(0, TEXTURE);
			for (int i = 1; i < length; i++) {
				final float s = this.scaleFactor * (this.rot.length - i);
				final float size = this.size * s;
				RenderSystem.setShaderColor(this.rgb[0] * s, this.rgb[1] * s, this.rgb[2] * s, alpha * s);
				pPoseStack.pushPose();
				pPoseStack.mulPose(this.axis[0].rotationDegrees(this.rot[i][0] + (this.rot[i - 1][0] - this.rot[i][0]) * pPartialTicks));
				pPoseStack.mulPose(this.axis[1].rotationDegrees(this.rot[i][1] + (this.rot[i - 1][1] - this.rot[i][1]) * pPartialTicks));
				pPoseStack.mulPose(this.axis[2].rotationDegrees(this.rot[i][2] + (this.rot[i - 1][2] - this.rot[i][2]) * pPartialTicks));
				final Matrix4f matrix4f = pPoseStack.last().pose();
				final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferBuilder.addVertex(matrix4f, -size, 100.0F, -size);
				bufferBuilder.addVertex(matrix4f, size, 100.0F, -size);
				bufferBuilder.addVertex(matrix4f, size, 100.0F, size);
				bufferBuilder.addVertex(matrix4f, -size, 100.0F, size);
				BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
				pPoseStack.popPose();
			}
		}
	}
}
