package dev.theagameplayer.puresuffering.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
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
import net.minecraft.world.phys.Vec3;

public final class InvasionSkyRenderHandler {
	private static final ResourceLocation DEFAULT_SUN = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation DEFAULT_MOON = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation DEFAULT_END_SKY = new ResourceLocation("textures/environment/end_sky.png");
	private final HashMap<InvasionSkyRenderer, Boolean> rendererMap = new HashMap<>(); //All Invasions
	private final ArrayList<InvasionSkyRenderer> weatherVisibilityList = new ArrayList<>(); //Invasions that are visible in weather
	private final ArrayList<InvasionSkyRenderer> skyColorList = new ArrayList<>(); //Invasions that change the sky colors
	private ResourceLocation sunTexture, moonTexture; //Invasion that changes the sun/moon
	private ResourceLocation skyTexture; //Invasion that changes the sky

	public final boolean hasRenderedInvasionSky(final float partialTicksIn, final PoseStack poseStackIn, final Matrix4f mat4In, final ClientLevel levelIn, final Minecraft mcIn) {
		this.rendererMap.clear();
		if (!levelIn.players().isEmpty()) {
			if (!levelIn.dimensionType().hasFixedTime()) {
				final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mcIn.level);
				final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mcIn.level);
				if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
					for (final Entry<InvasionSkyRenderer, Boolean> entry : dayInfo.getRendererMap())
						this.rendererMap.put(entry.getKey(), entry.getValue());
				} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
					for (final Entry<InvasionSkyRenderer, Boolean> entry : nightInfo.getRendererMap())
						this.rendererMap.put(entry.getKey(), entry.getValue());
				}
			} else {
				for (Entry<InvasionSkyRenderer, Boolean> entry : ClientInvasionWorldInfo.getFixedClientInfo(mcIn.level).getRendererMap())
					this.rendererMap.put(entry.getKey(), entry.getValue());
			}
		}
		if (this.rendererMap.isEmpty()) return false;
		final Camera cam = mcIn.gameRenderer.getMainCamera();
		final boolean flag = levelIn.effects().isFoggyAt(Mth.floor(cam.getPosition().x()), Mth.floor(cam.getPosition().y())) || mcIn.gui.getBossOverlay().shouldCreateWorldFog();
		final Runnable fogTick = () -> FogRenderer.setupFog(cam, FogRenderer.FogMode.FOG_SKY, mcIn.gameRenderer.getRenderDistance(), flag, partialTicksIn);
		final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mcIn.level);
		final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mcIn.level);
		if (levelIn.effects().skyType() == SkyType.NORMAL) {
			this.weatherVisibilityList.clear();
			this.skyColorList.clear();
			this.sunTexture = null;
			this.moonTexture = null;
			for (final Entry<InvasionSkyRenderer, Boolean> entry : this.rendererMap.entrySet()) {
				final InvasionSkyRenderer renderer = entry.getKey();
				if (renderer.getSunTexture() != null && entry.getValue() && dayInfo.isClientTime())
					this.sunTexture = renderer.getSunTexture();
				if (renderer.getMoonTexture() != null && entry.getValue() && nightInfo.isClientTime())
					this.moonTexture = renderer.getMoonTexture();
				if (renderer.isWeatherVisibilityChanged())
					this.weatherVisibilityList.add(renderer);
				if (renderer.isSkyColorChanged())
					this.skyColorList.add(renderer);
			}
			this.renderInvasionSky(poseStackIn, mat4In, mcIn, levelIn, fogTick, partialTicksIn, levelIn.getDayTime() % 12000L);
			return true;
		} else if (levelIn.effects().skyType() == SkyType.END) {
			for (final Entry<InvasionSkyRenderer, Boolean> entry : this.rendererMap.entrySet()) {
				final InvasionSkyRenderer renderer = entry.getKey();
				if (renderer.getFixedSkyTexture() != null && entry.getValue())
					this.skyTexture = renderer.getFixedSkyTexture();
			}
			this.renderEndInvasionSkybox(poseStackIn, levelIn, partialTicksIn);
			return true;
		}
		return false;
	}

	private final void renderInvasionSky(final PoseStack poseStackIn, final Matrix4f mat4In, final Minecraft mcIn, final ClientLevel levelIn, final Runnable fogTickIn, final float partialTicksIn, final long dayTimeIn) {
		final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(levelIn);
		final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(levelIn);
		final Vec3 vec3 = levelIn.getSkyColor(mcIn.gameRenderer.getMainCamera().getPosition(), partialTicksIn);
		float r = 0.0F, g = 0.0F, b = 0.0F;
		for (final InvasionSkyRenderer renderer : this.skyColorList) {
			r += renderer.getRedOffset() / this.skyColorList.size();
			g += renderer.getGreenOffset() / this.skyColorList.size();
			b += renderer.getBlueOffset() / this.skyColorList.size();
		}
		final float f = ClientTransitionHandler.tickSkyColor((float)vec3.x, r, dayTimeIn);
		final float f1 = ClientTransitionHandler.tickSkyColor((float)vec3.y, g, dayTimeIn);
		final float f2 = ClientTransitionHandler.tickSkyColor((float)vec3.z, b, dayTimeIn);
		final BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.disableTexture();
		FogRenderer.levelFogColor();
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(f, f1, f2, 1.0F);
		final ShaderInstance shaderInstance = RenderSystem.getShader();
		mcIn.levelRenderer.skyBuffer.bind();
		mcIn.levelRenderer.skyBuffer.drawWithShader(poseStackIn.last().pose(), mat4In, shaderInstance);
		VertexBuffer.unbind();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		final float[] afloat = levelIn.effects().getSunriseColor(levelIn.getTimeOfDay(partialTicksIn), partialTicksIn);
		if (afloat != null) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.disableTexture();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			poseStackIn.pushPose();
			poseStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
			final float f3 = Mth.sin(levelIn.getSunAngle(partialTicksIn)) < 0.0F ? 180.0F : 0.0F;
			poseStackIn.mulPose(Axis.ZP.rotationDegrees(f3));
			poseStackIn.mulPose(Axis.ZP.rotationDegrees(90.0F));
			final float f4 = afloat[0];
			final float f5 = afloat[1];
			final float f6 = afloat[2];
			final Matrix4f matrix4f = poseStackIn.last().pose();
			bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3] / this.rendererMap.size()).endVertex();
			for(int j = 0; j <= 16; ++j) {
				final float f7 = (float)j * ((float)Math.PI * 2F) / 16.0F;
				final float f8 = Mth.sin(f7);
				final float f9 = Mth.cos(f7);
				bufferBuilder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
			}
			BufferUploader.drawWithShader(bufferBuilder.end());
			poseStackIn.popPose();
		}
		RenderSystem.enableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		poseStackIn.pushPose();
		final float f11 = Mth.clamp(((this.sunTexture == null && dayInfo.isClientTime()) || (this.moonTexture == null && nightInfo.isClientTime()) ? 1.0F : ClientTransitionHandler.tickSunMoonAlpha(1.0F, dayTimeIn)) - levelIn.getRainLevel(partialTicksIn), 0.0F, 1.0F);
		float f12 = 0.0F;
		for (final InvasionSkyRenderer renderer : this.weatherVisibilityList)
			f12 += renderer.getWeatherVisibility() / this.weatherVisibilityList.size();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11 + Mth.clamp(ClientTransitionHandler.tickWeatherVisibility(f12, dayTimeIn), 0.0F, 1.0F));
		poseStackIn.mulPose(Axis.YP.rotationDegrees(-90.0F));
		poseStackIn.mulPose(Axis.XP.rotationDegrees(levelIn.getTimeOfDay(partialTicksIn) * 360.0F));
		final Matrix4f matrix4f1 = poseStackIn.last().pose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		this.renderSun(bufferBuilder, matrix4f1, 30.0F, dayTimeIn);
		this.renderMoon(bufferBuilder, matrix4f1, levelIn, 20.0F, dayTimeIn);
		RenderSystem.disableTexture();
		final float f10 = levelIn.getStarBrightness(partialTicksIn) * f11;
		if (f10 > 0.0F) {
			RenderSystem.setShaderColor(f10, f10, f10, f10 / this.rendererMap.size());
			FogRenderer.setupNoFog();
			mcIn.levelRenderer.starBuffer.bind();
			mcIn.levelRenderer.starBuffer.drawWithShader(poseStackIn.last().pose(), mat4In, GameRenderer.getPositionShader());
			VertexBuffer.unbind();
			fogTickIn.run();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		poseStackIn.popPose();
		RenderSystem.disableTexture();
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		final double d0 = mcIn.player.getEyePosition(partialTicksIn).y - levelIn.getLevelData().getHorizonHeight(levelIn);
		if (d0 < 0.0D) {
			poseStackIn.pushPose();
			poseStackIn.translate(0.0D, 12.0D, 0.0D);
			mcIn.levelRenderer.darkBuffer.bind();
			mcIn.levelRenderer.darkBuffer.drawWithShader(poseStackIn.last().pose(), mat4In, shaderInstance);
			VertexBuffer.unbind();
			poseStackIn.popPose();
		}
		if (levelIn.effects().hasGround()) {
			RenderSystem.setShaderColor(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F, 1.0F);
		} else {
			RenderSystem.setShaderColor(f, f1, f2, 1.0F);
		}
		RenderSystem.enableTexture();
		RenderSystem.depthMask(true);
	}

	private final void renderSun(final BufferBuilder bufferBuilderIn, final Matrix4f matrix4fIn, final float f13In, final long dayTimeIn) {
		final boolean flag = dayTimeIn < ClientTransitionHandler.HALF_TRANSITION || dayTimeIn > 11999L - ClientTransitionHandler.HALF_TRANSITION;
		RenderSystem.setShaderTexture(0, (this.sunTexture == null || flag) ? DEFAULT_SUN : this.sunTexture);
		bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -f13In, 100.0F, -f13In).uv(0.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, 100.0F, -f13In).uv(1.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, 100.0F, f13In).uv(1.0F, 1.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -f13In, 100.0F, f13In).uv(0.0F, 1.0F).endVertex();
		BufferUploader.drawWithShader(bufferBuilderIn.end());
	}

	private final void renderMoon(final BufferBuilder bufferBuilderIn, final Matrix4f matrix4fIn, final ClientLevel levelIn, final float f13In, final long dayTimeIn) {
		final boolean flag = dayTimeIn < ClientTransitionHandler.HALF_TRANSITION || dayTimeIn > 11999L - ClientTransitionHandler.HALF_TRANSITION;
		RenderSystem.setShaderTexture(0, (this.moonTexture == null || flag) ? DEFAULT_MOON : this.moonTexture);
		final int k = levelIn.getMoonPhase();
		final int l = k % 4;
		final int i1 = k / 4 % 2;
		final float f14 = (float)(l + 0) / 4.0F;
		final float f15 = (float)(i1 + 0) / 2.0F;
		final float f16 = (float)(l + 1) / 4.0F;
		final float f17 = (float)(i1 + 1) / 2.0F;
		bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -f13In, -100.0F, f13In).uv(f16, f17).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, -100.0F, f13In).uv(f14, f17).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, -100.0F, -f13In).uv(f14, f15).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -f13In, -100.0F, -f13In).uv(f16, f15).endVertex();
		BufferUploader.drawWithShader(bufferBuilderIn.end());
	}

	private final void renderEndInvasionSkybox(final PoseStack matrixStackIn, final ClientLevel levelIn, final float partialTicksIn) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, this.skyTexture == null ? DEFAULT_END_SKY : this.skyTexture);
		final Tesselator tessellator = Tesselator.getInstance();
		final BufferBuilder bufferBuilder = tessellator.getBuilder();
		for(int i = 0; i < 6; ++i) {
			matrixStackIn.pushPose();
			if (i == 1) {
				matrixStackIn.mulPose(Axis.XP.rotationDegrees(90.0F));
			} else if (i == 2) {
				matrixStackIn.mulPose(Axis.XP.rotationDegrees(-90.0F));
			} else if (i == 3) {
				matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
			} else if (i == 4) {
				matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90.0F));
			} else if (i == 5) {
				matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-90.0F));
			}
			final Matrix4f matrix4f = matrixStackIn.last().pose();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			tessellator.end();
			matrixStackIn.popPose();
		}
		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
