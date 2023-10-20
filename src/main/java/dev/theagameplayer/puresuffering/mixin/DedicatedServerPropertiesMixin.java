package dev.theagameplayer.puresuffering.mixin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.Difficulty;

@Mixin(DedicatedServerProperties.class)
public final class DedicatedServerPropertiesMixin {
	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;fromFile(Ljava/nio/file/Path;)Lnet/minecraft/server/dedicated/DedicatedServerProperties;", cancellable = true)
	private static final void fromFile(final Path pathIn, final CallbackInfoReturnable<DedicatedServerProperties> callbackIn) {
		final Properties properties = DedicatedServerProperties.loadFromFile(pathIn);
		if (!Files.exists(pathIn)) properties.setProperty("difficulty", Difficulty.HARD.getKey());
		callbackIn.setReturnValue(new DedicatedServerProperties(properties));
	}
}
