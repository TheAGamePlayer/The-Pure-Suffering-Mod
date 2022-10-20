package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import dev.theagameplayer.puresuffering.client.InvasionSkyRenderHandler;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.material.FogType;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(at = @At("HEAD"), method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
	public void renderSky(PoseStack poseStackIn, Matrix4f mat4In, float partialTicksIn, Camera camIn, boolean isFoggyIn, Runnable fogTickIn, CallbackInfo callbackIn) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel clientLevel = mc.level;
		if (clientLevel != null && PSConfigValues.client.useSkyBoxRenderer) {
			fogTickIn.run();
			if (!isFoggyIn) {
				FogType fogType = camIn.getFluidInCamera();
				if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !mc.levelRenderer.doesMobEffectBlockSky(camIn)) {
					InvasionSkyRenderHandler handler = new InvasionSkyRenderHandler(); //I hate the local methods, may find a fix later?
					if (handler.hasRenderedInvasionSky(partialTicksIn, poseStackIn, clientLevel, mc))
						callbackIn.cancel(); //Will improve mod compat in a future update
				}
			}
		}
	}
}