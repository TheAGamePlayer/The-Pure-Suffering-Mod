package dev.theagameplayer.puresuffering.client.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import dev.theagameplayer.puresuffering.util.ClientTimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo.FogType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ISkyRenderHandler;

public final class InvasionSkyRenderHandler implements ISkyRenderHandler {
	private static final ResourceLocation DEFAULT_SUN = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation DEFAULT_MOON = new ResourceLocation("textures/environment/moon_phases.png");
	private static final HashMap<InvasionSkyRenderer, Boolean> RENDERER_MAP = new HashMap<>(); //All Invasions
	private static final ArrayList<InvasionSkyRenderer> WEATHER_VISIBILITY_LIST = new ArrayList<>(); //Invasions that are visible in weather
	private static final ArrayList<InvasionSkyRenderer> SKY_COLOR_LIST = new ArrayList<>(); //Invasions that change the sky colors
	private ResourceLocation sunTexture, moonTexture; //Invasion that changes the sun/moon
	private final ISkyRenderHandler skyRenderer;
	
	public InvasionSkyRenderHandler(ISkyRenderHandler skyRendererIn) {
		this.skyRenderer = skyRendererIn;
	}
	
	@Override
	public void render(int ticksIn, float partialTicksIn, MatrixStack matrixStackIn, ClientWorld worldIn, Minecraft mcIn) {
		RENDERER_MAP.clear();
		if (!worldIn.players().isEmpty()) {
			if (ClientTimeUtil.isClientDay() && !ClientInvasionUtil.getDayRenderers().isEmpty()) {
				for (Entry<InvasionSkyRenderer, Boolean> entry : ClientInvasionUtil.getDayRenderers())
					RENDERER_MAP.put(entry.getKey(), entry.getValue());
			} else if (ClientTimeUtil.isClientNight() && !ClientInvasionUtil.getNightRenderers().isEmpty()) {
				for (Entry<InvasionSkyRenderer, Boolean> entry : ClientInvasionUtil.getNightRenderers())
					RENDERER_MAP.put(entry.getKey(), entry.getValue());
			}
		}
		if (!RENDERER_MAP.isEmpty()) {
			if (worldIn.effects().skyType() == FogType.NORMAL) {
				WEATHER_VISIBILITY_LIST.clear();
				SKY_COLOR_LIST.clear();
				this.sunTexture = null;
				this.moonTexture = null;
				for (Entry<InvasionSkyRenderer, Boolean> entry : RENDERER_MAP.entrySet()) {
					InvasionSkyRenderer renderer = entry.getKey();
					if (renderer.getSunTexture() != null && entry.getValue() && ClientTimeUtil.isClientDay())
						this.sunTexture = renderer.getSunTexture();
					if (renderer.getMoonTexture() != null && entry.getValue() && ClientTimeUtil.isClientNight())
						this.moonTexture = renderer.getMoonTexture();
					if (renderer.isWeatherVisibilityChanged())
						WEATHER_VISIBILITY_LIST.add(renderer);
					if (renderer.isSkyColorChanged())
						SKY_COLOR_LIST.add(renderer);
				}
				this.renderInvasionSky(matrixStackIn, mcIn, worldIn, partialTicksIn, worldIn.getDayTime() % 12000L);
				return;
			}
		}
		ISkyRenderHandler renderer = worldIn.effects().getSkyRenderHandler();
		worldIn.effects().setSkyRenderHandler(null);
		if (this.skyRenderer == null) {
			mcIn.levelRenderer.renderSky(matrixStackIn, partialTicksIn);
		} else {
			this.skyRenderer.render(ticksIn, partialTicksIn, matrixStackIn, worldIn, mcIn);
		}
		worldIn.effects().setSkyRenderHandler(renderer);
	}
	
	@SuppressWarnings("deprecation")
	private void renderInvasionSky(MatrixStack matrixStackIn, Minecraft mcIn, ClientWorld worldIn, float partialTicksIn, long dayTimeIn) {
		RenderSystem.disableTexture();
		Vector3d vector3d = worldIn.getSkyColor(mcIn.gameRenderer.getMainCamera().getBlockPosition(), partialTicksIn);
		float r = 0.0F, g = 0.0F, b = 0.0F;
		for (InvasionSkyRenderer renderer : SKY_COLOR_LIST) {
			r += renderer.getRedOffset() / SKY_COLOR_LIST.size();
			g += renderer.getGreenOffset() / SKY_COLOR_LIST.size();
			b += renderer.getBlueOffset() / SKY_COLOR_LIST.size();
		}
		float f = ClientTransitionHandler.tickSkyColor((float)vector3d.x, r, dayTimeIn);
		float f1 = ClientTransitionHandler.tickSkyColor((float)vector3d.y, g, dayTimeIn);
		float f2 = ClientTransitionHandler.tickSkyColor((float)vector3d.z, b, dayTimeIn);
		FogRenderer.levelFogColor();
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
		RenderSystem.depthMask(false);
		RenderSystem.enableFog();
		RenderSystem.color3f(f, f1, f2);
		mcIn.levelRenderer.skyBuffer.bind();
		mcIn.levelRenderer.skyFormat.setupBufferState(0L);
		mcIn.levelRenderer.skyBuffer.draw(matrixStackIn.last().pose(), 7);
		VertexBuffer.unbind();
		mcIn.levelRenderer.skyFormat.clearBufferState();
		RenderSystem.disableFog();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		float[] afloat = worldIn.effects().getSunriseColor(worldIn.getTimeOfDay(partialTicksIn), partialTicksIn);
		if (afloat != null) {
			RenderSystem.disableTexture();
			RenderSystem.shadeModel(7425);
			matrixStackIn.pushPose();
			matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			float f3 = MathHelper.sin(worldIn.getSunAngle(partialTicksIn)) < 0.0F ? 180.0F : 0.0F;
			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(f3));
			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
			float f4 = afloat[0];
			float f5 = afloat[1];
			float f6 = afloat[2];
			Matrix4f matrix4f = matrixStackIn.last().pose();
			bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3] / RENDERER_MAP.size()).endVertex();
			for(int j = 0; j <= 16; ++j) {
				float f7 = (float)j * ((float)Math.PI * 2F) / 16.0F;
				float f8 = MathHelper.sin(f7);
				float f9 = MathHelper.cos(f7);
				bufferbuilder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
			}
			bufferbuilder.end();
			WorldVertexBufferUploader.end(bufferbuilder);
			matrixStackIn.popPose();
			RenderSystem.shadeModel(7424);
		}
		RenderSystem.enableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		matrixStackIn.pushPose();
		float f11 = MathHelper.clamp(((this.sunTexture == null && ClientTimeUtil.isClientDay()) || (this.moonTexture == null && ClientTimeUtil.isClientNight()) ? 1.0F : ClientTransitionHandler.tickSunMoonAlpha(1.0F, dayTimeIn)) - worldIn.getRainLevel(partialTicksIn), 0.0F, 1.0F);
		float f12 = 0.0F;
		for (InvasionSkyRenderer renderer : WEATHER_VISIBILITY_LIST) {
			f12 += renderer.getWeatherVisibility() / WEATHER_VISIBILITY_LIST.size();
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, f11 + MathHelper.clamp(ClientTransitionHandler.tickWeatherVisibility(f12, dayTimeIn), 0.0F, 1.0F));
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(worldIn.getTimeOfDay(partialTicksIn) * 360.0F));
		Matrix4f matrix4f1 = matrixStackIn.last().pose();
		this.renderSun(mcIn, bufferbuilder, matrix4f1, 30.0F, dayTimeIn);
		this.renderMoon(mcIn, bufferbuilder, matrix4f1, worldIn, 20.0F, dayTimeIn);
		RenderSystem.disableTexture();
		float f10 = worldIn.getStarBrightness(partialTicksIn) * f11;
		if (f10 > 0.0F) {
			RenderSystem.color4f(f10, f10, f10, f10 / RENDERER_MAP.size());
			mcIn.levelRenderer.starBuffer.bind();
			mcIn.levelRenderer.skyFormat.setupBufferState(0L);
			mcIn.levelRenderer.starBuffer.draw(matrixStackIn.last().pose(), 7);
			VertexBuffer.unbind();
			mcIn.levelRenderer.skyFormat.clearBufferState();
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableFog();
		matrixStackIn.popPose();
		RenderSystem.disableTexture();
		RenderSystem.color3f(0.0F, 0.0F, 0.0F);
		double d0 = mcIn.player.getEyePosition(partialTicksIn).y - worldIn.getLevelData().getHorizonHeight();
		if (d0 < 0.0D) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0.0D, 12.0D, 0.0D);
			mcIn.levelRenderer.darkBuffer.bind();
			mcIn.levelRenderer.skyFormat.setupBufferState(0L);
			mcIn.levelRenderer.darkBuffer.draw(matrixStackIn.last().pose(), 7);
			VertexBuffer.unbind();
			mcIn.levelRenderer.skyFormat.clearBufferState();
			matrixStackIn.popPose();
		}
		if (worldIn.effects().hasGround()) {
			RenderSystem.color3f(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
		} else {
			RenderSystem.color3f(f, f1, f2);
		}
		RenderSystem.enableTexture();
		RenderSystem.depthMask(true);
		RenderSystem.disableFog();
	}
	
	private void renderSun(Minecraft mcIn, BufferBuilder bufferBuilderIn, Matrix4f matrix4fIn, float f13In, long dayTimeIn) {
		boolean flag = dayTimeIn < ClientTransitionHandler.HALF_TRANSITION || dayTimeIn > 11999L - ClientTransitionHandler.HALF_TRANSITION;
		mcIn.textureManager.bind(this.sunTexture == null || flag ? DEFAULT_SUN : this.sunTexture);
		bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -f13In, 100.0F, -f13In).uv(0.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, 100.0F, -f13In).uv(1.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, 100.0F, f13In).uv(1.0F, 1.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -f13In, 100.0F, f13In).uv(0.0F, 1.0F).endVertex();
		bufferBuilderIn.end();
		WorldVertexBufferUploader.end(bufferBuilderIn);
	}
	
	private void renderMoon(Minecraft mcIn, BufferBuilder bufferBuilderIn, Matrix4f matrix4fIn, ClientWorld worldIn, float f13In, long dayTimeIn) {
		boolean flag = dayTimeIn < ClientTransitionHandler.HALF_TRANSITION || dayTimeIn > 11999L - ClientTransitionHandler.HALF_TRANSITION;
		mcIn.textureManager.bind(this.moonTexture == null || flag ? DEFAULT_MOON : this.moonTexture);
		int k = worldIn.getMoonPhase();
		int l = k % 4;
		int i1 = k / 4 % 2;
		float f14 = (float)(l + 0) / 4.0F;
		float f15 = (float)(i1 + 0) / 2.0F;
		float f16 = (float)(l + 1) / 4.0F;
		float f17 = (float)(i1 + 1) / 2.0F;
		bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -f13In, -100.0F, f13In).uv(f16, f17).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, -100.0F, f13In).uv(f14, f17).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, -100.0F, -f13In).uv(f14, f15).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -f13In, -100.0F, -f13In).uv(f16, f15).endVertex();
		bufferBuilderIn.end();
		WorldVertexBufferUploader.end(bufferBuilderIn);
	}
}
