package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.util.invasion.ClientInvasionHandler;
import dev.theagameplayer.puresuffering.util.invasion.ServerInvasionHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

@Mixin(Level.class)
public final class LevelMixin {
	@Inject(at = @At("RETURN"), method = "updateSkyBrightness()V")
	private final void updateSkyBrightness(final CallbackInfo pCallback) {
		final Level level = (Level)(Object)this;
		level.skyDarken = level.isClientSide ? ClientInvasionHandler.handleSkyBrightness(level.skyDarken, (ClientLevel)level) : ServerInvasionHandler.handleSkyBrightness(level.skyDarken, (ServerLevel)level);
	}
}
