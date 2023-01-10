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
	private static final InvasionSkyRenderHandler ISR_HANDLER = new InvasionSkyRenderHandler();
	
	@Inject(at = @At("HEAD"), method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
	public void renderSky(PoseStack poseStackIn, Matrix4f mat4In, float partialTicksIn, Camera camIn, boolean isFoggyIn, Runnable fogTickIn, CallbackInfo callbackIn) {
		Minecraft mc = Minecraft.getInstance();
 		ClientLevel clientWorld = mc.level;
		if (clientWorld != null && PSConfigValues.client.useSkyBoxRenderer) {
 			fogTickIn.run();
 			if (!isFoggyIn) {
 				FogType fogType = camIn.getFluidInCamera();
 				if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA) {
					if (ISR_HANDLER.hasRenderedInvasionSky(partialTicksIn, poseStackIn, mat4In, clientWorld, mc))
 						callbackIn.cancel(); //Will improve mod compat in a future update?
				}
 			}
		}
	}
}
