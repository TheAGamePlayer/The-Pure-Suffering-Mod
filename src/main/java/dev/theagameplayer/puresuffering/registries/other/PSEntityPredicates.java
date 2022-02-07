package dev.theagameplayer.puresuffering.registries.other;

import java.util.function.Predicate;

import dev.theagameplayer.puresuffering.registries.PSPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class PSEntityPredicates {
	public static final Predicate<Entity> HYPER_AGGRESSION = entity -> {
		return !(entity instanceof PlayerEntity) || !entity.isSpectator() && !((PlayerEntity)entity).isCreative() && !((PlayerEntity)entity).hasEffect(PSPotions.BLESSING.get());
	};
}
