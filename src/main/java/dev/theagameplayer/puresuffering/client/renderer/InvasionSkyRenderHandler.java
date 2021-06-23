package dev.theagameplayer.puresuffering.client.renderer;

import java.util.ArrayList;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.TimeUtil;
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

public class InvasionSkyRenderHandler implements ISkyRenderHandler {
	private static final ResourceLocation DEFAULT_SUN = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation DEFAULT_MOON = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ArrayList<Pair<InvasionType, Integer>> INVASION_LIST = new ArrayList<>(); //All Invasions
	private static final ArrayList<Pair<InvasionType, Integer>> SUN_LIST = new ArrayList<>(); //Invasions that change the sun
	private static final ArrayList<Pair<InvasionType, Integer>> MOON_LIST = new ArrayList<>(); //Invasions that change the moon
	private static final ArrayList<Pair<InvasionType, Integer>> WEATHER_VISIBILITY_LIST = new ArrayList<>(); //Invasions that are visible in weather
	private static final ArrayList<Pair<InvasionType, Integer>> SKY_COLOR_LIST = new ArrayList<>(); //Invasions that change the sky color
	private final ISkyRenderHandler skyRenderer;
	
	public InvasionSkyRenderHandler(ISkyRenderHandler skyRendererIn) {
		this.skyRenderer = skyRendererIn;
	}
	
	@Override
	public void render(int ticksIn, float partialTicksIn, MatrixStack matrixStackIn, ClientWorld worldIn, Minecraft mcIn) {
		INVASION_LIST.clear();
		if (!worldIn.players().isEmpty()) {
			if (TimeUtil.isDay(worldIn) && !InvasionSpawner.getDayInvasions().isEmpty()) {
				INVASION_LIST.addAll(ImmutableList.copyOf(InvasionSpawner.getDayInvasions().stream().filter(pair -> { 
					return pair.getLeft().isEnvironmental();
				}).iterator()));
			} else if (TimeUtil.isNight(worldIn) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				INVASION_LIST.addAll(ImmutableList.copyOf(InvasionSpawner.getNightInvasions().stream().filter(pair -> { 
					return pair.getLeft().isEnvironmental();
				}).iterator()));
			}
		}
		if (!INVASION_LIST.isEmpty()) {
			if (worldIn.effects().skyType() == FogType.NORMAL) {
				SUN_LIST.clear();
				MOON_LIST.clear();
				WEATHER_VISIBILITY_LIST.clear();
				for (Pair<InvasionType, Integer> pair : INVASION_LIST) {
					if (!pair.getLeft().getSkyRenderer().isEmpty()) {
						if (pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getSunTexture() != null)
							SUN_LIST.add(pair);
						if (pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getMoonTexture() != null)
							MOON_LIST.add(pair);
						if (pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).isWeatherVisibilityChanged())
							WEATHER_VISIBILITY_LIST.add(pair);
						if (pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).isSkyColorChanged())
							SKY_COLOR_LIST.add(pair);
					}
				}
				this.renderInvasionSky(matrixStackIn, mcIn, worldIn, partialTicksIn);
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
	private void renderInvasionSky(MatrixStack matrixStackIn, Minecraft mcIn, ClientWorld worldIn, float partialTicksIn) {
		RenderSystem.disableTexture();
		Vector3d vector3d = worldIn.getSkyColor(mcIn.gameRenderer.getMainCamera().getBlockPosition(), partialTicksIn);
		float f = (float)vector3d.x;
		float f1 = (float)vector3d.y;
		float f2 = (float)vector3d.z;
		for (Pair<InvasionType, Integer> pair : SKY_COLOR_LIST) {
			InvasionSkyRenderer skyRenderer = pair.getLeft().getSkyRenderer().get(pair.getRight() - 1);
			f += skyRenderer.getRedOffset() / SKY_COLOR_LIST.size();
			f1 += skyRenderer.getGreenOffset() / SKY_COLOR_LIST.size();
			f2 += skyRenderer.getBlueOffset() / SKY_COLOR_LIST.size();
		}
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
			bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3] / INVASION_LIST.size()).endVertex();
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
		float f11 = 1.0F - worldIn.getRainLevel(partialTicksIn);
		float f12 = 0.0F;
		for (Pair<InvasionType, Integer> pair : WEATHER_VISIBILITY_LIST) {
			f12 = pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getWeatherVisibilityIn() / WEATHER_VISIBILITY_LIST.size();
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, f11 + MathHelper.clamp(f12, 0.0F, 1.0F));
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(worldIn.getTimeOfDay(partialTicksIn) * 360.0F));
		Matrix4f matrix4f1 = matrixStackIn.last().pose();
		float f13 = 30.0F;
		if (SUN_LIST.isEmpty()) {
			renderSun(mcIn, bufferbuilder, matrix4f1, f13, DEFAULT_SUN);
		}
		else {
			for (Pair<InvasionType, Integer> pair : SUN_LIST) {
				renderSun(mcIn, bufferbuilder, matrix4f1, f13, pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getSunTexture());
			}
		}
		f13 = 20.0F;
		if (MOON_LIST.isEmpty()) {
			renderMoon(mcIn, bufferbuilder, matrix4f1, worldIn, f13, DEFAULT_MOON);
		}
		else {
			for (Pair<InvasionType, Integer> pair : MOON_LIST) {
				renderMoon(mcIn, bufferbuilder, matrix4f1, worldIn, f13, pair.getLeft().getSkyRenderer().get(pair.getRight() - 1).getMoonTexture());
			}
		}
		RenderSystem.disableTexture();
		float f10 = worldIn.getStarBrightness(partialTicksIn) * f11;
		if (f10 > 0.0F) {
			RenderSystem.color4f(f10, f10, f10, f10 / INVASION_LIST.size());
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
	
	private static void renderSun(Minecraft mcIn, BufferBuilder bufferBuilderIn, Matrix4f matrix4fIn, float f13In, ResourceLocation textureIn) {
		mcIn.textureManager.bind(textureIn);
		bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilderIn.vertex(matrix4fIn, -f13In, 100.0F, -f13In).uv(0.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, 100.0F, -f13In).uv(1.0F, 0.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, f13In, 100.0F, f13In).uv(1.0F, 1.0F).endVertex();
		bufferBuilderIn.vertex(matrix4fIn, -f13In, 100.0F, f13In).uv(0.0F, 1.0F).endVertex();
		bufferBuilderIn.end();
		WorldVertexBufferUploader.end(bufferBuilderIn);
	}
	
	private static void renderMoon(Minecraft mcIn, BufferBuilder bufferBuilderIn, Matrix4f matrix4fIn, ClientWorld worldIn, float f13In, ResourceLocation textureIn) {
		mcIn.textureManager.bind(textureIn);
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
