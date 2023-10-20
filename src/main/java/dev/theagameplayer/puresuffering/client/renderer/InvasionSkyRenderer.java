package dev.theagameplayer.puresuffering.client.renderer;

import java.util.ArrayList;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
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
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public final class InvasionSkyRenderer {
	private static final ResourceLocation DEFAULT_SUN = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation DEFAULT_MOON = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation DEFAULT_END_SKY = new ResourceLocation("textures/environment/end_sky.png");
	private final SkyType skyType;
	private final InvasionDifficulty difficulty;
	private final boolean noTick;
	private ArrayList<ClientInvasion> invasions;
	private ResourceLocation sun, moon, sky;
	private boolean noSunOrMoon;
	private float[][] rgb;
	private float alphaOffset, weatherVisibilityOffset;
	private float alpha, weatherVisibility;

	public InvasionSkyRenderer(final InvasionDifficulty difficultyIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		this.skyType = level.effects().skyType();
		this.difficulty = difficultyIn;
		this.noTick = this.skyType != SkyType.NORMAL;
	}
	
	public final void update(final InvasionSkyRenderInfo primaryInfoIn, final ArrayList<ClientInvasion> invasionsIn) {
		switch (this.skyType) {
		case NONE: return;
		case NORMAL: {
			final ArrayList<InvasionSkyRenderInfo> skyColorList = new ArrayList<>(); //Invasions that change the sky colors
			final ArrayList<InvasionSkyRenderInfo> sunMoonAlphaList = new ArrayList<>(); //Invasions that changes the sun & moon alpha.
			final ArrayList<InvasionSkyRenderInfo> weatherVisibilityList = new ArrayList<>(); //Invasions that are visible in weather
			this.sun = primaryInfoIn.getSunTexture();
			this.moon = primaryInfoIn.getMoonTexture();
			this.noSunOrMoon = false;
			for (final ClientInvasion invasion : invasionsIn) {
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
			this.invasions = invasionsIn;
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
			this.sky = primaryInfoIn.getFixedSkyTexture();
			this.invasions = invasionsIn;
		}
		}
	}
	
	public final void tick(final long dayTimeIn) {
		if (this.noTick) return;
		this.rgb[1] = this.rgb[0].clone();
		for (final ClientInvasion invasion : this.invasions)
			invasion.flickerSkyRGB(this.rgb[1]);
		ClientTransitionHandler.getSkyColor(this.rgb[1], dayTimeIn);
		for (final ClientInvasion invasion : this.invasions)
			this.alphaOffset = invasion.flickerAlpha(this.alphaOffset);
		this.alpha = ClientTransitionHandler.getSunMoonAlpha((this.sun != null || this.moon != null) && !this.noSunOrMoon, this.alphaOffset, dayTimeIn);
		this.weatherVisibility = Mth.clamp(ClientTransitionHandler.getWeatherVisibility(this.weatherVisibilityOffset, dayTimeIn), 0.0F, 1.0F);
	}
	
	public final boolean hasRenderedInvasionSky(final PoseStack poseStackIn, final Matrix4f mat4In, final float partialTicksIn, final Camera camIn, final boolean isFoggyIn, final Runnable fogTickIn) {
		fogTickIn.run();
		if (isFoggyIn) return false;
		final Minecraft mc = Minecraft.getInstance();
		final FogType fogType = camIn.getFluidInCamera();
		if (fogType == FogType.POWDER_SNOW || fogType == FogType.LAVA || mc.levelRenderer.doesMobEffectBlockSky(camIn)) return false;
		final ClientLevel level = mc.level;
		switch (this.skyType) {
		case NORMAL: {
			this.renderInvasionSky(level.getDayTime() % 12000L, poseStackIn, mat4In, partialTicksIn, fogTickIn);
			return true;
		}
		case END: {
			this.renderEndInvasionSkybox(level.getDayTime() % 12000L, poseStackIn, partialTicksIn);
			return true;
		}
		default: return false;
		}
	}

	private final void renderInvasionSky(final long dayTimeIn, final PoseStack poseStackIn, final Matrix4f mat4In, final float partialTicksIn, final Runnable fogTickIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		final float timeOfDay = level.getTimeOfDay(partialTicksIn);
		final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		FogRenderer.levelFogColor();
		RenderSystem.depthMask(false);
		final Vec3 color = level.getSkyColor(mc.gameRenderer.getMainCamera().getPosition(), partialTicksIn);
		RenderSystem.setShaderColor(this.rgb[1][0] + (float)color.x, this.rgb[1][1] + (float)color.y, this.rgb[1][2] + (float)color.z, 1.0F);
		final ShaderInstance shaderInstance = RenderSystem.getShader();
		mc.levelRenderer.skyBuffer.bind();
		mc.levelRenderer.skyBuffer.drawWithShader(poseStackIn.last().pose(), mat4In, shaderInstance);
		VertexBuffer.unbind();
		RenderSystem.enableBlend();
		final float[] aRGB = level.effects().getSunriseColor(timeOfDay, partialTicksIn);
		if (aRGB != null) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			poseStackIn.pushPose();
			poseStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
			final float angle = Mth.sin(level.getSunAngle(partialTicksIn)) < 0.0F ? 180.0F : 0.0F;
			poseStackIn.mulPose(Axis.ZP.rotationDegrees(angle));
			poseStackIn.mulPose(Axis.ZP.rotationDegrees(90.0F));
			final Matrix4f matrix4f = poseStackIn.last().pose();
			bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(aRGB[0], aRGB[1], aRGB[2], aRGB[3]).endVertex();
			for(int j = 0; j < 17; ++j) {
				final float a = (float)j * ((float)Math.PI * 2F) / 16.0F;
				final float x = Mth.sin(a);
				final float yz = Mth.cos(a);
				bufferBuilder.vertex(matrix4f, x * 120.0F, yz * 120.0F, -yz * 40.0F * aRGB[3]).color(aRGB[0], aRGB[1], aRGB[2], 0.0F).endVertex();
			}
			BufferUploader.drawWithShader(bufferBuilder.end());
			poseStackIn.popPose();
		}
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		poseStackIn.pushPose();
		final float alpha = Mth.clamp(this.alpha - level.getRainLevel(partialTicksIn), 0.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha + this.weatherVisibility);
		poseStackIn.mulPose(Axis.YN.rotationDegrees(90.0F));
		poseStackIn.mulPose(Axis.XP.rotationDegrees(timeOfDay * 360.0F));
		final Matrix4f matrix4f2 = poseStackIn.last().pose();
		this.renderSun(bufferBuilder, matrix4f2, 30.0F, dayTimeIn);
		this.renderMoon(bufferBuilder, matrix4f2, 20.0F, dayTimeIn);
		final float f10 = level.getStarBrightness(partialTicksIn) * alpha;
		if (f10 > 0.0F) {
			RenderSystem.setShaderColor(f10, f10, f10, f10);
			FogRenderer.setupNoFog();
			mc.levelRenderer.starBuffer.bind();
			mc.levelRenderer.starBuffer.drawWithShader(poseStackIn.last().pose(), mat4In, GameRenderer.getPositionShader());
			VertexBuffer.unbind();
			fogTickIn.run();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		poseStackIn.popPose();
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		final double dist = mc.player.getEyePosition(partialTicksIn).y - level.getLevelData().getHorizonHeight(level);
		if (dist < 0.0D) {
			poseStackIn.pushPose();
			poseStackIn.translate(0.0D, 12.0D, 0.0D);
			mc.levelRenderer.darkBuffer.bind();
			mc.levelRenderer.darkBuffer.drawWithShader(poseStackIn.last().pose(), mat4In, shaderInstance);
			VertexBuffer.unbind();
			poseStackIn.popPose();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
	}

	private final void renderSun(final BufferBuilder bufferBuilderIn, final Matrix4f matrix4fIn, final float sizeIn, final long dayTimeIn) {
		final boolean flag = dayTimeIn < Invasion.HALF_TRANSITION || dayTimeIn > 11999L - Invasion.HALF_TRANSITION;
		RenderSystem.setShaderTexture(0, (this.sun == null || flag) ? DEFAULT_SUN : this.sun);
		bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -sizeIn, 100.0F, -sizeIn).uv(0.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, sizeIn, 100.0F, -sizeIn).uv(1.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, sizeIn, 100.0F, sizeIn).uv(1.0F, 1.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -sizeIn, 100.0F, sizeIn).uv(0.0F, 1.0F).endVertex();
		BufferUploader.drawWithShader(bufferBuilderIn.end());
	}

	private final void renderMoon(final BufferBuilder bufferBuilderIn, final Matrix4f matrix4fIn, final float sizeIn, final long dayTimeIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		final boolean flag = dayTimeIn < Invasion.HALF_TRANSITION || dayTimeIn > 11999L - Invasion.HALF_TRANSITION;
		RenderSystem.setShaderTexture(0, (this.moon == null || flag) ? DEFAULT_MOON : this.moon);
		final int k = level.getMoonPhase();
		final int l = k % 4;
		final int i1 = k / 4 % 2;
		final float f14 = (float)(l + 0) / 4.0F;
		final float f15 = (float)(i1 + 0) / 2.0F;
		final float f16 = (float)(l + 1) / 4.0F;
		final float f17 = (float)(i1 + 1) / 2.0F;
		bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -sizeIn, -100.0F, sizeIn).uv(f16, f17).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, sizeIn, -100.0F, sizeIn).uv(f14, f17).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, sizeIn, -100.0F, -sizeIn).uv(f14, f15).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -sizeIn, -100.0F, -sizeIn).uv(f16, f15).endVertex();
		BufferUploader.drawWithShader(bufferBuilderIn.end());
	}

	private final void renderEndInvasionSkybox(final long dayTimeIn, final PoseStack poseStackIn, final float partialTicksIn) {
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, this.sky == null ? DEFAULT_END_SKY : this.sky);
		final Tesselator tessellator = Tesselator.getInstance();
		final BufferBuilder bufferBuilder = tessellator.getBuilder();
		for(int i = 0; i < 6; ++i) {
			poseStackIn.pushPose();
			if (this.difficulty.isNightmare()) {
				poseStackIn.mulPose(Axis.XP.rotationDegrees(dayTimeIn));
				poseStackIn.mulPose(Axis.YP.rotationDegrees(dayTimeIn));
				poseStackIn.mulPose(Axis.ZP.rotationDegrees(dayTimeIn));
			}
			if (i == 1) {
				poseStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
			} else if (i == 2) {
				poseStackIn.mulPose(Axis.XP.rotationDegrees(-90.0F));
			} else if (i == 3) {
				poseStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
			} else if (i == 4) {
				poseStackIn.mulPose(Axis.ZP.rotationDegrees(90.0F));
			} else if (i == 5) {
				poseStackIn.mulPose(Axis.ZP.rotationDegrees(-90.0F));
			}
			final Matrix4f matrix4f = poseStackIn.last().pose();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			tessellator.end();
			poseStackIn.popPose();
		}
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}
}
