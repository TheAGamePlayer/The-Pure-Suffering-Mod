package dev.theagameplayer.puresuffering.registries.other;

import java.util.function.Predicate;

import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class PSEntityPredicates {
	public static final Predicate<Entity> HYPER_AGGRESSION = entity -> {
		return !(entity instanceof Player) || (!entity.isSpectator() && !((Player)entity).isCreative() && !((Player)entity).hasEffect(PSMobEffects.BLESSING.get()));
	};
}
