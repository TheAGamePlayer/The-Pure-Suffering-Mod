package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.invasion.ClientInvasionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;

@Mixin(DimensionSpecialEffects.class)
public final class DimensionSpecialEffectsMixin {
	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;forceBrightLightmap()Z", cancellable = true)
	private final void forceBrightLightmap(final CallbackInfoReturnable<Boolean> pCallback) {
		if (PSConfigValues.client.canInvasionsChangeBrightness) {
			final Minecraft mc = Minecraft.getInstance();
			pCallback.setReturnValue(ClientInvasionHandler.handleBrightLightmap(pCallback.getReturnValueZ(), mc.level));
		}
	}
}
