package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.DimensionRenderInfo;

@Mixin(DimensionRenderInfo.class)
public class DimensionRenderInfoMixin {
	@Inject(at = @At("RETURN"), method = "forceBrightLightmap()Z", cancellable = true)
	private void forceBrightLightmap(CallbackInfoReturnable<Boolean> callbackIn) {
		if (PSConfigValues.client.canInvasionsChangeBrightness) {
			Minecraft mc = Minecraft.getInstance();
			callbackIn.setReturnValue(ClientInvasionUtil.handleLightMap(callbackIn.getReturnValueZ(), mc.level));
		}
	}
}
