package dev.theagameplayer.puresuffering.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPredicates;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

public final class StartAttackingInvasion { //TODO: Experimentation, currently unused!
	public static final <E extends Mob> BehaviorControl<E> create(final Predicate<E> pCanAttack, final Function<E, Optional<? extends LivingEntity>> pTargetFinder, final BehaviorControl<E> pBackup) {
		return BehaviorBuilder.create(builder -> builder.group(builder.absent(MemoryModuleType.ATTACK_TARGET), builder.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
				.apply(builder, (attackTarget, walkTarget) -> (level, entity, gameTime) -> {
					if (entity.getPersistentData().contains(Invasion.INVASION_MOB) && PSGameRules.HYPER_AGGRESSION.get(level) && !PSConfigValues.common.hyperAggressionBlacklist.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()) && (entity.getLastHurtByMob() == null || !entity.getLastHurtByMob().isAlive())) {
						if (!pCanAttack.test(entity)) {
							return false;
						} else {
							Optional<? extends LivingEntity> optional = pTargetFinder.apply(entity);
							final LivingEntity livingEntity = optional.isPresent() ? optional.get() : getNearestPlayer(level, entity.position());
							if (livingEntity == null || !entity.canAttack(livingEntity)) {
								return false;
							} else {
								final LivingChangeTargetEvent changeTargetEvent = CommonHooks.onLivingChangeTarget(entity, livingEntity, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
								if (changeTargetEvent.isCanceled() || changeTargetEvent.getNewAboutToBeSetTarget() == null) return false;
								attackTarget.set(changeTargetEvent.getNewAboutToBeSetTarget());
								walkTarget.erase();
								return true;
							}
						}
					} else {
						return pBackup.tryStart(level, entity, gameTime);
					}
				}));
	}

	public static final ServerPlayer getNearestPlayer(final ServerLevel pLevel, final Vec3 pPos) {
		ServerPlayer target = null;
		double dist = -1.0F;
		final List<ServerPlayer> players = pLevel.getPlayers(PSEntityPredicates.HYPER_AGGRESSION);
		for (final ServerPlayer player : players) {
			final double d = player.distanceToSqr(pPos);
			if (dist < 0.0F || d < dist) {
				dist = d;
				target = player;
			}
		}
		return target;
	}
}
