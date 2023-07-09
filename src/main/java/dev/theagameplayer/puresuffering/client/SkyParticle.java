package dev.theagameplayer.puresuffering.client;

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

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SkyParticle {
	private static final ResourceLocation SKY_PARTICLE = new ResourceLocation("forge", "textures/white.png");
	private static final ArrayList<SkyParticle> SKY_PARTICLES = new ArrayList<>();
	private static int particleDelay;
	private final float speed, halfSpeed;
	private final float degreesStart, degreesFinish;
	private final float maxSize, maxAlpha;
	private final Axis xAxis, zAxis;
	private float ticksAlive;
	private float size, alpha;
	
	private SkyParticle(final ClientLevel levelIn) {
		this.speed = levelIn.random.nextInt(PSConfigValues.client.maxVortexParticleLifespan - PSConfigValues.client.minVortexParticleLifespan + 1) + PSConfigValues.client.minVortexParticleLifespan;
		this.halfSpeed = this.speed/2;
		this.degreesStart = levelIn.random.nextInt(360);
		this.degreesFinish = levelIn.random.nextInt(360) + 180;
		this.maxSize = levelIn.random.nextFloat() + 0.75F;
		this.maxAlpha = levelIn.random.nextFloat() * 0.5F + 0.5F;
		this.xAxis = levelIn.random.nextBoolean() ? Axis.XP : Axis.XN;
		this.zAxis = levelIn.random.nextBoolean() ? Axis.ZP : Axis.ZN;
		this.ticksAlive = this.speed;
		particleDelay = PSConfigValues.client.vortexParticleSpread;
	}
	
	public static final void renderParticles(final PoseStack poseStackIn, final ClientLevel levelIn, final long dayTimeIn, final HyperType hyperTypeIn, final float weatherVisibilityIn) {
		if (dayTimeIn > ClientTransitionHandler.HALF_TRANSITION && dayTimeIn < 12000L - ClientTransitionHandler.HALF_TRANSITION && particleDelay < 0)
			SKY_PARTICLES.add(new SkyParticle(levelIn));
		particleDelay--;
		final float r = hyperTypeIn == HyperType.NIGHTMARE ? 0.375F : 0.75F;
		final float g = hyperTypeIn == HyperType.NIGHTMARE ? 0.0625F : 0.0F;
		final float b = hyperTypeIn == HyperType.NIGHTMARE ? 0.5F : 0.0F;
		final Axis yAxis = hyperTypeIn == HyperType.NIGHTMARE ? Axis.YN : Axis.YP;
		final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		SKY_PARTICLES.removeIf(particle -> {
			poseStackIn.pushPose();
			final float percentAlive = particle.ticksAlive/particle.speed;
			particle.alpha = vortexValue(particle, particle.alpha, particle.maxAlpha, dayTimeIn);
			RenderSystem.setShaderColor(r, g, b, particle.alpha - weatherVisibilityIn);
			poseStackIn.mulPose(yAxis.rotationDegrees(particle.degreesStart + percentAlive * particle.degreesFinish));
			poseStackIn.mulPose(particle.xAxis.rotationDegrees(percentAlive > 0.5F ? percentAlive * 180.0F : 180.0F - percentAlive * 180.0F));
			poseStackIn.mulPose(particle.zAxis.rotationDegrees(percentAlive > 0.5F ? 180.0F - percentAlive * 180.0F : 180.0F - percentAlive * 180.0F));
			final Matrix4f matrix4f1 = poseStackIn.last().pose();
			RenderSystem.setShaderTexture(0, SKY_PARTICLE);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(matrix4f1, -particle.size, 100.0F, -particle.size).uv(0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(matrix4f1, particle.size, 100.0F, -particle.size).uv(1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(matrix4f1, particle.size, 100.0F, particle.size).uv(1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(matrix4f1, -particle.size, 100.0F, particle.size).uv(0.0F, 1.0F).endVertex();
			BufferUploader.drawWithShader(bufferBuilder.end());
			poseStackIn.popPose();
			particle.ticksAlive--;
			particle.size = vortexValue(particle, particle.size, particle.maxSize, dayTimeIn);
			if (particle.ticksAlive < 1) return true;
			return false;
		});
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(true);
	}
	
	private static final float vortexValue(final SkyParticle particleIn, final float currentIn, final float maxIn, final long dayTimeIn) {
		final float result = particleIn.ticksAlive > particleIn.halfSpeed ? maxIn/particleIn.speed * (particleIn.speed - particleIn.ticksAlive) : maxIn/particleIn.speed * particleIn.ticksAlive;
		if (dayTimeIn < ClientTransitionHandler.HALF_TRANSITION) {
			return result + currentIn/ClientTransitionHandler.HALF_TRANSITION; //0-1
		} else if (dayTimeIn > 11999L - ClientTransitionHandler.HALF_TRANSITION) {
			return result - currentIn/ClientTransitionHandler.HALF_TRANSITION; //1-0
		}
		return result;
	}
}
