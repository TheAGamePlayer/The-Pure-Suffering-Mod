package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.registries.PSActivities;
import dev.theagameplayer.puresuffering.world.entity.ai.behavior.InvasionGoalPackages;
import dev.theagameplayer.puresuffering.world.entity.ai.behavior.SetInvasionStatus;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

@Mixin(Villager.class)
public final class VillagerMixin {
	@Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/entity/npc/Villager;registerBrainGoals(Lnet/minecraft/world/entity/ai/Brain;)V")
	private final void registerBrainGoals(final Brain<Villager> pBrain, final CallbackInfo pCallback) {
		pBrain.addActivity(PSActivities.INVASION.value(), InvasionGoalPackages.villagerPackage(0.5F));
		pBrain.availableBehaviorsByPriority.get(0).get(Activity.CORE).add(SetInvasionStatus.create());
	}
}
