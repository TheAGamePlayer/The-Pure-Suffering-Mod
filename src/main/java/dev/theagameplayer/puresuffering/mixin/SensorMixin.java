package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

@Mixin(Sensor.class)
public final class SensorMixin {
    //private static final TargetingConditions INVASION_ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat();
    //private static final TargetingConditions INVASION_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().ignoreInvisibilityTesting();
	private static final TargetingConditions INVASION_ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().ignoreLineOfSight();
	private static final TargetingConditions INVASION_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();

	/*@Inject(at = @At("HEAD"), method = "isEntityAttackable(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z", cancellable = true)
	private static final void isEntityAttackable(final LivingEntity pEntity, final LivingEntity pTarget, final CallbackInfoReturnable<Boolean> pCallback) {
		if (pEntity.getPersistentData().contains(Invasion.INVASION_MOB) && pEntity instanceof Breeze)
			pCallback.setReturnValue(pEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? INVASION_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(pEntity, pTarget) : INVASION_ATTACK_TARGET_CONDITIONS.test(pEntity, pTarget));
	}*/
	
	//A temporary fix to an annoying problem with Piglins being unable to have hyper aggression applied?
	@Inject(at = @At("HEAD"), method = "isEntityAttackableIgnoringLineOfSight(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z", cancellable = true)
	private static final void isEntityAttackableIgnoringLineOfSight(final LivingEntity pEntity, final LivingEntity pTarget, final CallbackInfoReturnable<Boolean> pCallback) {
		if (pEntity.getPersistentData().contains(Invasion.INVASION_MOB) && pEntity instanceof AbstractPiglin)
			pCallback.setReturnValue(pEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? INVASION_ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(pEntity, pTarget) : INVASION_ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(pEntity, pTarget));
	}
}
