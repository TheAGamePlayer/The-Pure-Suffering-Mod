package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.util.ServerInvasionUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

@Mixin(Level.class)
public class LevelMixin {
	@Inject(at = @At("RETURN"), method = "updateSkyBrightness()V")
	private void updateSkyBrightness(CallbackInfo callbackIn) {
		Level level = (Level)(Object)this;
		if (!level.isClientSide)
			level.skyDarken = ServerInvasionUtil.handleLightLevel(level.skyDarken, (ServerLevel)level);
	}
}
