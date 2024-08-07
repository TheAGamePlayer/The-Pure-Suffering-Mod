package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.invasion.ClientInvasionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LightTexture.class)
public final class LightTextureMixin {
	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/renderer/LightTexture;calculateDarknessScale(Lnet/minecraft/world/entity/LivingEntity;FF)F", cancellable = true)
	private final void calculateDarknessScale(final LivingEntity pPlayer, final float pDarkness, final float pPartialTicks, final CallbackInfoReturnable<Float> pCallback) {
		if (PSConfigValues.client.canInvasionsChangeBrightness) {
			final Minecraft mc = Minecraft.getInstance();
			pCallback.setReturnValue(ClientInvasionHandler.handleDarknessScale(pCallback.getReturnValueF(), mc.level));
		}
	}
}
