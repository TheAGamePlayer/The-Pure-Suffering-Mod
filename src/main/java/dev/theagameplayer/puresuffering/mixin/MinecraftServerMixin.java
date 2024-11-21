package dev.theagameplayer.puresuffering.mixin;

import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.config.PSConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.LevelStem;

@Mixin(MinecraftServer.class)
public final class MinecraftServerMixin {
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/MinecraftServer;runServer()V")
	private final void runServer(final CallbackInfo pCallback) {
		@SuppressWarnings("resource")
		final MinecraftServer server = (MinecraftServer)(Object)this;
		final Registry<LevelStem> registry = server.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
		for (final Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet())
			PSConfig.initLevelConfig(entry);
	}
}
