package dev.theagameplayer.puresuffering.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public final class LevelRendererMixin {
	@Inject(at = @At("HEAD"), method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
	private final void renderSky(final PoseStack poseStackIn, final Matrix4f mat4In, final float partialTicksIn, final Camera camIn, final boolean isFoggyIn, final Runnable fogTickIn, final CallbackInfo callbackIn) {
		if (!PSConfigValues.client.useSkyBoxRenderer) return;
		final Minecraft mc = Minecraft.getInstance();
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		if (session != null && session.getInvasionSkyRenderer().hasRenderedInvasionSky(poseStackIn, mat4In, partialTicksIn, camIn, isFoggyIn, fogTickIn)) callbackIn.cancel();
	}
}