package dev.theagameplayer.puresuffering.event;

import java.util.List;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.network.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPredicates;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event.Result;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

public final class PSLivingEvents {
	public static final void conversionPre(final LivingConversionEvent.Pre eventIn) {
		if (eventIn.getOutcome() == null || eventIn.getOutcome().getCategory() != MobCategory.MONSTER || !eventIn.getEntity().getPersistentData().contains(Invasion.INVASION_MOB)) return;
		if (eventIn.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
			if (session == null || !session.stopsConversions() || !session.hasMob(mob)) return;
			eventIn.setCanceled(true);
			eventIn.setConversionTimer(12000 - (int)(level.dayTime() % 12000L));
		}
	}

	public static final void conversionPost(final LivingConversionEvent.Post eventIn) {
		if (eventIn.getOutcome() == null || eventIn.getOutcome().getClassification(false) != MobCategory.MONSTER) return;
		if (eventIn.getOutcome() instanceof Mob resultMob) {
			final CompoundTag persistentData = eventIn.getEntity().getPersistentData();
			final CompoundTag outcomeData = resultMob.getPersistentData();
			if (persistentData.contains(Invasion.INVASION_MOB))
				outcomeData.putInt(Invasion.INVASION_MOB, persistentData.getInt(Invasion.INVASION_MOB));
			if (persistentData.contains(Invasion.ANTI_GRIEF))
				outcomeData.putBoolean(Invasion.ANTI_GRIEF, persistentData.getBoolean(Invasion.ANTI_GRIEF));
			if (persistentData.contains(Invasion.DESPAWN_LOGIC))
				outcomeData.putIntArray(Invasion.DESPAWN_LOGIC, persistentData.getIntArray(Invasion.DESPAWN_LOGIC));
			if (eventIn.getEntity() instanceof Mob mob) {
				if (eventIn.getEntity() instanceof PSInvasionMob im1 && resultMob instanceof PSInvasionMob im2) {
					im2.psSetHyperCharge(im1.psGetHyperCharge());
					PSInvasionMob.applyHyperEffects(mob);
				}
				if (mob.level() instanceof ServerLevel level) {
					final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
					if (session == null || !session.hasMob(mob)) return;
					session.replaceMob(mob, resultMob);
				}
			}
		}
	}

	public static final void livingTick(final LivingEvent.LivingTickEvent eventIn) {
		if (eventIn.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
			final CompoundTag persistentData = mob.getPersistentData();
			final boolean flag1 = PSGameRules.HYPER_AGGRESSION.get(level) && !PSConfigValues.common.hyperAggressionBlacklist.contains(mob.getType().getDescriptionId());
			final boolean flag2 = mob.getLastHurtByMob() == null || !mob.getLastHurtByMob().isAlive();
			if (persistentData.contains(Invasion.DESPAWN_LOGIC)) {
				final int[] despawnLogic = persistentData.getIntArray(Invasion.DESPAWN_LOGIC);
				final BlockPos pos = mob.blockPosition();
				if (pos.getX() != despawnLogic[0] || pos.getY() != despawnLogic[1] || pos.getZ() != despawnLogic[2]) {
					despawnLogic[0] = pos.getX();
					despawnLogic[1] = pos.getY();
					despawnLogic[2] = pos.getZ();
					despawnLogic[3] = 0;
				} else if (getNearestPlayer(level, mob.position(), true) != null) {
					despawnLogic[3]++;
				}
			}
			if (persistentData.contains(Invasion.INVASION_MOB) && flag1 && flag2) {
				final ServerPlayer player = getNearestPlayer(level, mob.position(), true);
				if (player == null) {
					mob.setTarget(null);
					return;
				}
				if (player.equals(mob.getTarget())) return;
				if (mob instanceof AbstractPiglin) { //If your wondering why a mob doesn't get aggressive, its because it didn't use the default targeting...
					mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, player.getUUID(), Invasion.TRANSITION_TIME);
				} else if (mob instanceof Hoglin) {
					mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
					mob.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, Invasion.TRANSITION_TIME);
				} else if (mob instanceof Zoglin) {
					mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, Invasion.TRANSITION_TIME);
				} else if (mob instanceof Warden warden) { //Warden requires special method due to anger logic
					warden.increaseAngerAt(player, 80, !player.equals(warden.getTarget())); //Golems will be a weakness to invasion Wardens. Fix Maybe?
				} else {
					mob.setTarget(player);
				}
			}
		}
	}

	private static final ServerPlayer getNearestPlayer(final ServerLevel levelIn, final Vec3 posIn, final boolean hyperAggressionIn) {
		ServerPlayer target = null;
		double dist = -1.0F;
		final List<ServerPlayer> players = hyperAggressionIn ? levelIn.getPlayers(PSEntityPredicates.HYPER_AGGRESSION) : levelIn.players();
		for (final ServerPlayer player : players) {
			final double d = player.distanceToSqr(posIn);
			if (dist < 0.0F || d < dist) {
				dist = d;
				target = player;
			}
		}
		return target;
	}

	public static final void experienceDrop(final LivingExperienceDropEvent eventIn) {
		if (eventIn.getEntity() instanceof Mob mob && mob instanceof PSInvasionMob invasionMob) {
			final ServerLevel level = (ServerLevel)mob.level();
			final InvasionLevelData ilData = InvasionLevelData.get(level);
			final InvasionSession session = ilData.getInvasionManager().getActiveSession(level);
			if (session == null || !session.hasMob(mob)) {
				eventIn.setDroppedExperience(eventIn.getOriginalExperience() + eventIn.getOriginalExperience()/3 * invasionMob.psGetHyperCharge());
			} else if (PSGameRules.USE_XP_MULTIPLIER.get(level) && eventIn.getAttackingPlayer() != null) {
				ilData.setXPMultiplier(ilData.getXPMultiplier() + 1);
				final double log = Math.log1p(ilData.getXPMultiplier())/Math.E;
				eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log) + eventIn.getOriginalExperience()/3 * invasionMob.psGetHyperCharge());
				PSPackets.sendToClientsIn(new UpdateXPMultPacket(log), level);
			}
		}
	}

	public static final void finalizeSpawn(final MobSpawnEvent.FinalizeSpawn eventIn) {
		final Mob mob = eventIn.getEntity();
		if (eventIn.getSpawnType() != MobSpawnType.NATURAL || mob.getClassification(false) != MobCategory.MONSTER) return;
		final ServerLevel level = (ServerLevel)eventIn.getLevel();
		if (PSConfigValues.common.naturalSpawnChance < level.random.nextDouble()) return;
		final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
		if (session == null) return;
		eventIn.setSpawnCancelled(true);
	}

	public static final void allowDespawn(final MobSpawnEvent.AllowDespawn eventIn) {
		final Mob mob = eventIn.getEntity();
		final CompoundTag persistentData = mob.getPersistentData();
		if (!mob.isAlive() || !persistentData.contains(Invasion.DESPAWN_LOGIC)) return;
		final ServerLevel level = (ServerLevel)eventIn.getLevel();
		final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
		final int[] despawnLogic = persistentData.getIntArray(Invasion.DESPAWN_LOGIC);
		final boolean flag1 = despawnLogic[3] > 150 || mob.isInWall();
		final boolean flag2 = despawnLogic.length == 6;
		if (session != null && session.hasMob(mob)) {
			if (flag1) session.relocateMob(mob);
			if (flag2) despawnLogic[4] = 0;
		}
		if (flag2) {
			if (despawnLogic[4] > despawnLogic[5]) {
				level.broadcastEntityEvent(mob, (byte)60);
				eventIn.setResult(Result.ALLOW);
				return;
			} else {
				despawnLogic[4]++;
			}
		}
	}
}
