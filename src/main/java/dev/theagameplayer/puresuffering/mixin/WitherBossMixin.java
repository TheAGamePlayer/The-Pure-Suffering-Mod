package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Mixin(WitherBoss.class)
public final class WitherBossMixin extends Monster {
	private WitherBossMixin(final EntityType<? extends Monster> pEntityType, final Level pLevel) {
		super(pEntityType, pLevel); //Dummy Constructor
	}

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;checkDespawn()V", cancellable = true)
	private final void checkDespawn(final CallbackInfo pCallback) {
		if (!((WitherBoss)(Object)this).getPersistentData().contains(Invasion.INVASION_MOB)) return;
		super.checkDespawn();
		pCallback.cancel();
	}
}
