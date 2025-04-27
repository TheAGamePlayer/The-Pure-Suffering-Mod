package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.config.PSConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

@Mixin(MappedRegistry.class)
public final class MappedRegistryMixin {
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/core/MappedRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;", cancellable = true)
	private final void register(final ResourceKey<Object> pKey, final Object pValue, final RegistrationInfo pRegistrationInfo, final CallbackInfoReturnable<Holder.Reference<Object>> pCallbackInfo) {
		if (pValue instanceof LevelStem stem)
			PSConfig.initLevelConfig(pKey, stem);
	}
}
