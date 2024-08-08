package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.invasion.ClientInvasionHandler;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(ClientLevel.class)
public final class ClientLevelMixin {
	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyDarken(F)F", cancellable = true)
	private final void getSkyDarken(final float pBrightness, final CallbackInfoReturnable<Float> pCallback) {
		if (PSConfigValues.client.canInvasionsChangeBrightness) {
			final ClientLevel level = (ClientLevel)(Object)this;
			pCallback.setReturnValue(ClientInvasionHandler.handleSkyDarken(pCallback.getReturnValueF(), level));
		}
	}
}
