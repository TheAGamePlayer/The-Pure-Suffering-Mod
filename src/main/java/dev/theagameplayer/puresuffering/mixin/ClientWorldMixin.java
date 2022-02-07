package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.client.world.ClientWorld;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	@Inject(at = @At("RETURN"), method = "getSkyDarken(F)F", cancellable = true)
	private void getSkyDarken(float brightnessIn, CallbackInfoReturnable<Float> callbackIn) {
		ClientWorld world = (ClientWorld)(Object)this;
		if (PSConfigValues.client.canInvasionsChangeBrightness)
			callbackIn.setReturnValue(ClientInvasionUtil.handleBrightness(callbackIn.getReturnValueF(), world));
	}
}
