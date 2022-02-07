package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.util.ServerInvasionUtil;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(World.class)
public class WorldMixin {
	@Inject(at = @At("RETURN"), method = "updateSkyBrightness()V")
	private void updateSkyBrightness(CallbackInfo callbackIn) {
		World world = (World)(Object)this;
		if (!world.isClientSide)
			world.skyDarken = ServerInvasionUtil.handleLightLevel(world.skyDarken, (ServerWorld)world);
	}
}
