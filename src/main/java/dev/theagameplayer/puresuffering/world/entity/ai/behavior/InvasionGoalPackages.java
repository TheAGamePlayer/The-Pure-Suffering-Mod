package dev.theagameplayer.puresuffering.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.npc.Villager;

public final class InvasionGoalPackages {
	public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerPackage(final float pSpeed) {
		return ImmutableList.of(
				Pair.of(2, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(InvasionGoalPackages::isInvasion), LocateHidingPlace.create(24, pSpeed * 1.4F, 1))),
				getMinimalLookBehavior(),
				Pair.of(99, ResetInvasionStatus.create()));
	}

	private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() { //From VillagerGoalPackages
		return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2), Pair.of(new DoNothing(30, 60), 8))));
	}

	private static final boolean isInvasion(final ServerLevel pLevel, final LivingEntity pEntity) {
		return InvasionLevelData.get(pLevel).getInvasionManager().getActiveSession(pLevel) != null;
	}
}
