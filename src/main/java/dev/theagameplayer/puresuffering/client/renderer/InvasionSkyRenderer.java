package dev.theagameplayer.puresuffering.client.renderer;

import java.util.ArrayList;
import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasion;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public final class InvasionSkyRenderer {
	private static final ResourceLocation DEFAULT_SUN = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
	private static final ResourceLocation DEFAULT_MOON = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
	private static final ResourceLocation DEFAULT_END_SKY = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
	private final SkyType skyType;
	private final InvasionDifficulty difficulty;
	private final boolean noTick;
	private ArrayList<ClientInvasion> invasions;
	private ResourceLocation sun, moon, sky;
	private boolean noSunOrMoon;
	private float[][] rgb;
	private float alphaOffset, weatherVisibilityOffset;
	private float alpha, weatherVisibility;

	public InvasionSkyRenderer(final InvasionDifficulty pDifficulty) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		this.skyType = level.effects().skyType();
		this.difficulty = pDifficulty;
		this.noTick = this.skyType != SkyType.NORMAL;
	}
	
	public final void update(final InvasionSkyRenderInfo pPrimaryInfo, final ArrayList<ClientInvasion> pInvasions) {
		switch (this.skyType) {
		case NONE: return;
		case NORMAL: {
			final ArrayList<InvasionSkyRenderInfo> skyColorList = new ArrayList<>(pInvasions.size()); //Invasions that change the sky colors
			final ArrayList<InvasionSkyRenderInfo> sunMoonAlphaList = new ArrayList<>(pInvasions.size()); //Invasions that changes the sun & moon alpha.
			final ArrayList<InvasionSkyRenderInfo> weatherVisibilityList = new ArrayList<>(pInvasions.size()); //Invasions that are visible in weather
			this.sun = pPrimaryInfo.getSunTexture();
			this.moon = pPrimaryInfo.getMoonTexture();
			this.noSunOrMoon = false;
			for (final ClientInvasion invasion : pInvasions) {
				final InvasionSkyRenderInfo renderInfo = invasion.getSkyRenderInfo();
				if (!this.noSunOrMoon)
					this.noSunOrMoon = renderInfo.getSunMoonAlpha() == 0;
				if (renderInfo.getSunMoonAlpha() < 1 && renderInfo.isSunMoonAlphaChanged())
					sunMoonAlphaList.add(renderInfo);
				if (!this.noSunOrMoon && renderInfo.isWeatherVisibilityChanged())
					weatherVisibilityList.add(renderInfo);
				if (!this.difficulty.isNightmare() && renderInfo.isSkyColorChanged())
					skyColorList.add(renderInfo);
			}
			this.invasions = pInvasions;
			this.rgb = new float[2][3];
			if (this.difficulty.isNightmare()) {
				this.rgb[0][0] = -1.0F;
				this.rgb[0][1] = -1.0F;
				this.rgb[0][2] = -1.0F;
			} else {
				for (final InvasionSkyRenderInfo renderInfo : skyColorList) {
					this.rgb[0][0] += renderInfo.getRGBOffset(0) / skyColorList.size();
					this.rgb[0][1] += renderInfo.getRGBOffset(1) / skyColorList.size();
					this.rgb[0][2] += renderInfo.getRGBOffset(2) / skyColorList.size();
				}
			}
			this.alphaOffset = sunMoonAlphaList.isEmpty() ? 1.0F : 0.0F;
			if (!this.noSunOrMoon) {
				for (final InvasionSkyRenderInfo renderInfo : sunMoonAlphaList)
					this.alphaOffset += renderInfo.getSunMoonAlpha() / sunMoonAlphaList.size();
			}
			this.weatherVisibilityOffset = 0.0F;
			if (!this.noSunOrMoon) {
				for (final InvasionSkyRenderInfo renderInfo : weatherVisibilityList)
					this.weatherVisibilityOffset += renderInfo.getWeatherVisibility() / weatherVisibilityList.size();
			}
		}
		case END: {
			this.sky = pPrimaryInfo.getFixedSkyTexture();
			this.invasions = pInvasions;
		}
		}
	}
	
	public final void tick(final long pDayTime) {
		if (this.noTick) return;
		this.rgb[1][0] = this.rgb[0][0];
		this.rgb[1][1] = this.rgb[0][1];
		this.rgb[1][2] = this.rgb[0][2];
		for (final ClientInvasion invasion : this.invasions)
			invasion.flickerSkyRGB(this.rgb[1]);
		ClientTransitionHandler.getSkyColor(this.rgb[1], pDayTime);
		for (final ClientInvasion invasion : this.invasions)
			this.alphaOffset = invasion.flickerAlpha(this.alphaOffset);
		this.alpha = ClientTransitionHandler.getSunMoonAlpha((this.sun != null || this.moon != null) && !this.noSunOrMoon, this.alphaOffset, pDayTime);
		this.weatherVisibility = Mth.clamp(ClientTransitionHandler.getWeatherVisibility(this.weatherVisibilityOffset, pDayTime), 0.0F, 1.0F);
	}
	
	public final boolean hasRenderedInvasionSky(final Matrix4f pFrustrumMatrix, final Matrix4f pProjectionMatrix, final float pPartialTick, final Camera pCamera, final boolean pIsFoggy, final Runnable pSkyFogSetup) {
		pSkyFogSetup.run();
		if (pIsFoggy) return false;
		final Minecraft mc = Minecraft.getInstance();
		final FogType fogType = pCamera.getFluidInCamera();
		if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA || mc.levelRenderer.doesMobEffectBlockSky(pCamera)) return false;
		final ClientLevel level = mc.level;
		final PoseStack poseStack = new PoseStack();
		switch (this.skyType) {
		case NORMAL: {
			poseStack.mulPose(pFrustrumMatrix);
			this.renderInvasionSky(level.getDayTime() % 12000L, poseStack, pProjectionMatrix, pPartialTick, pSkyFogSetup);
			return true;
		}
		case END: {
			poseStack.mulPose(pFrustrumMatrix);
			this.renderEndInvasionSkybox(level.getDayTime() % 12000L, poseStack, pPartialTick);
			return true;
		}
		default: return false;
		}
	}

	private final void renderInvasionSky(final long pDayTime, final PoseStack pPoseStack, final Matrix4f pProjectionMatrix, final float pPartialTick, final Runnable pSkyFogSetup) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		final float timeOfDay = level.getTimeOfDay(pPartialTick);
		FogRenderer.levelFogColor();
		RenderSystem.depthMask(false);
		final Vec3 color = level.getSkyColor(mc.gameRenderer.getMainCamera().getPosition(), pPartialTick);
		RenderSystem.setShaderColor(this.rgb[1][0] + (float)color.x, this.rgb[1][1] + (float)color.y, this.rgb[1][2] + (float)color.z, 1.0F);
		final ShaderInstance shaderInstance = RenderSystem.getShader();
		mc.levelRenderer.skyBuffer.bind();
		mc.levelRenderer.skyBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, shaderInstance);
		VertexBuffer.unbind();
		RenderSystem.enableBlend();
		final float[] aRGB = level.effects().getSunriseColor(timeOfDay, pPartialTick);
		if (aRGB != null) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			pPoseStack.pushPose();
			pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			final float angle = Mth.sin(level.getSunAngle(pPartialTick)) < 0.0F ? 180.0F : 0.0F;
			pPoseStack.mulPose(Axis.ZP.rotationDegrees(angle));
			pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			final Matrix4f matrix4f = pPoseStack.last().pose();
			final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(aRGB[0], aRGB[1], aRGB[2], aRGB[3]);
			for (int j = 0; j < 17; ++j) {
				final float a = (float)j * (Mth.PI * 2F) / 16.0F;
				final float x = Mth.sin(a);
				final float yz = Mth.cos(a);
				bufferBuilder.addVertex(matrix4f, x * 120.0F, yz * 120.0F, -yz * 40.0F * aRGB[3]).setColor(aRGB[0], aRGB[1], aRGB[2], 0.0F);
			}
			BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
			pPoseStack.popPose();
		}
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		pPoseStack.pushPose();
		final float alpha = Mth.clamp(this.alpha - level.getRainLevel(pPartialTick), 0.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha + this.weatherVisibility);
		pPoseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
		pPoseStack.mulPose(Axis.XP.rotationDegrees(timeOfDay * 360.0F));
		final Matrix4f matrix4f2 = pPoseStack.last().pose();
		this.renderSun(matrix4f2, 30.0F, pDayTime);
		this.renderMoon(matrix4f2, 20.0F, pDayTime);
		final float f10 = level.getStarBrightness(pPartialTick) * alpha;
		if (f10 > 0.0F) {
			RenderSystem.setShaderColor(f10, f10, f10, f10);
			FogRenderer.setupNoFog();
			mc.levelRenderer.starBuffer.bind();
			mc.levelRenderer.starBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, GameRenderer.getPositionShader());
			VertexBuffer.unbind();
			pSkyFogSetup.run();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		pPoseStack.popPose();
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		final double dist = mc.player.getEyePosition(pPartialTick).y - level.getLevelData().getHorizonHeight(level);
		if (dist < 0.0D) {
			pPoseStack.pushPose();
			pPoseStack.translate(0.0D, 12.0D, 0.0D);
			mc.levelRenderer.darkBuffer.bind();
			mc.levelRenderer.darkBuffer.drawWithShader(pPoseStack.last().pose(), pProjectionMatrix, shaderInstance);
			VertexBuffer.unbind();
			pPoseStack.popPose();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
	}

	private final void renderSun(final Matrix4f pMatrix4f, final float pSize, final long pDayTime) {
		final boolean flag = pDayTime < Invasion.HALF_TRANSITION || pDayTime > 11999L - Invasion.HALF_TRANSITION;
		RenderSystem.setShaderTexture(0, (this.sun == null || flag) ? DEFAULT_SUN : this.sun);
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.addVertex(pMatrix4f, -pSize, 100.0F, -pSize).setUv(0.0F, 0.0F);
		bufferBuilder.addVertex(pMatrix4f, pSize, 100.0F, -pSize).setUv(1.0F, 0.0F);
		bufferBuilder.addVertex(pMatrix4f, pSize, 100.0F, pSize).setUv(1.0F, 1.0F);
		bufferBuilder.addVertex(pMatrix4f, -pSize, 100.0F, pSize).setUv(0.0F, 1.0F);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
	}

	private final void renderMoon(final Matrix4f pMatrix4f, final float pSize, final long pDayTime) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		final boolean flag = pDayTime < Invasion.HALF_TRANSITION || pDayTime > 11999L - Invasion.HALF_TRANSITION;
		RenderSystem.setShaderTexture(0, (this.moon == null || flag) ? DEFAULT_MOON : this.moon);
		final int k = level.getMoonPhase();
		final int l = k % 4;
		final int i1 = k / 4 % 2;
		final float f14 = (float)(l + 0) / 4.0F;
		final float f15 = (float)(i1 + 0) / 2.0F;
		final float f16 = (float)(l + 1) / 4.0F;
		final float f17 = (float)(i1 + 1) / 2.0F;
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.addVertex(pMatrix4f, -pSize, -100.0F, pSize).setUv(f16, f17);
		bufferBuilder.addVertex(pMatrix4f, pSize, -100.0F, pSize).setUv(f14, f17);
		bufferBuilder.addVertex(pMatrix4f, pSize, -100.0F, -pSize).setUv(f14, f15);
		bufferBuilder.addVertex(pMatrix4f, -pSize, -100.0F, -pSize).setUv(f16, f15);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
	}

	private final void renderEndInvasionSkybox(final long pDayTime, final PoseStack pPoseStack, final float pPartialTick) {
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, this.sky == null ? DEFAULT_END_SKY : this.sky);
		final Tesselator tesselator = Tesselator.getInstance();
		for (int i = 0; i < 6; ++i) {
			pPoseStack.pushPose();
			if (this.difficulty.isNightmare()) {
				pPoseStack.mulPose(Axis.XP.rotationDegrees(pDayTime));
				pPoseStack.mulPose(Axis.YP.rotationDegrees(pDayTime));
				pPoseStack.mulPose(Axis.ZP.rotationDegrees(pDayTime));
			}
			if (i == 1) {
				pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			} else if (i == 2) {
				pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			} else if (i == 3) {
				pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			} else if (i == 4) {
				pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			} else if (i == 5) {
				pPoseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
			}
			final Matrix4f matrix4f = pPoseStack.last().pose();
			final BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(40, 40, 40, 255);
			bufferBuilder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(40, 40, 40, 255);
			bufferBuilder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(40, 40, 40, 255);
			bufferBuilder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(40, 40, 40, 255);
			BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
			pPoseStack.popPose();
		}
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}
}
