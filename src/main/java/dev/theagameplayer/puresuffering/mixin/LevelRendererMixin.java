package dev.theagameplayer.puresuffering.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public final class LevelRendererMixin {
	@Inject(at = @At("HEAD"), method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
	private final void renderSky(final Matrix4f pProjectionMatrix, final Matrix4f pFrustrumMatrix, final float pPartialTick, final Camera pCamera, final boolean pIsFoggy, final Runnable pSkyFogSetup, final CallbackInfo pCallback) {
		if (!PSConfigValues.client.useSkyBoxRenderer) return;
		final Minecraft mc = Minecraft.getInstance();
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		if (session != null && session.getInvasionSkyRenderer().hasRenderedInvasionSky(pProjectionMatrix, pFrustrumMatrix, pPartialTick, pCamera, pIsFoggy, pSkyFogSetup)) pCallback.cancel();
	}
}