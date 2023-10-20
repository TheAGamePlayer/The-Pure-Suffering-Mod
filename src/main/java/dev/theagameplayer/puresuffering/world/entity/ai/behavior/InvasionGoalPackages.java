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
import net.minecraft.world.entity.ai.behavior.MoveToSkySeeingSpot;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.npc.Villager;

public final class InvasionGoalPackages {
	public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerPackage(final float speedIn) {
		return ImmutableList.of(
				Pair.of(0, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(InvasionGoalPackages::isInvasion), TriggerGate.triggerOneShuffled(ImmutableList.of(Pair.of(MoveToSkySeeingSpot.create(speedIn), 5), Pair.of(VillageBoundRandomStroll.create(speedIn * 1.1F), 2))))),
				Pair.of(2, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(InvasionGoalPackages::isInvasion), LocateHidingPlace.create(24, speedIn * 1.4F, 1))),
				getMinimalLookBehavior());
	}

	private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() { //From VillagerGoalPackages
		return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2), Pair.of(new DoNothing(30, 60), 8))));
	}

	private static final boolean isInvasion(final ServerLevel levelIn, final LivingEntity entityIn) {
		return InvasionLevelData.get(levelIn).getInvasionManager().getActiveSession(levelIn) != null;
	}
}
