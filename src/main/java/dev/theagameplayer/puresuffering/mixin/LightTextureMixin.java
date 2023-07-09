package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LightTexture.class)
public final class LightTextureMixin {
	@Inject(at = @At("RETURN"), method = "calculateDarknessScale(Lnet/minecraft/world/entity/LivingEntity;FF)F", cancellable = true)
	private final void calculateDarknessScale(final LivingEntity playerIn, final float darknessIn, final float partialTicksIn, final CallbackInfoReturnable<Float> callbackIn) {
		if (PSConfigValues.client.canInvasionsChangeBrightness) {
			final Minecraft mc = Minecraft.getInstance();
			callbackIn.setReturnValue(ClientInvasionUtil.handleLightTextureDarkness(callbackIn.getReturnValueF(), mc.level));
		}
	}
}
