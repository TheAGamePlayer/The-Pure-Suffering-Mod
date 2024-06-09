package dev.theagameplayer.puresuffering.registries.other;

import java.util.function.Predicate;

import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import net.minecraft.server.level.ServerPlayer;

public final class PSEntityPredicates {
	public static final Predicate<ServerPlayer> HYPER_AGGRESSION = player -> {
		return player.isAlive() && !player.isSpectator() && !player.isCreative() && !player.isInvisible() && !player.hasEffect(PSMobEffects.BLESSING);
	};
}
