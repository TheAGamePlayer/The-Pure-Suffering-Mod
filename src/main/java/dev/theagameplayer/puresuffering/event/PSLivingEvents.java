package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.network.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;

public final class PSLivingEvents {
	public static final void finalizeSpawn(final FinalizeSpawnEvent pEvent) {
		final Mob mob = pEvent.getEntity();
		if (pEvent.getSpawnType() != MobSpawnType.NATURAL || mob.getClassification(false) != MobCategory.MONSTER) return;
		final ServerLevel level = (ServerLevel)pEvent.getLevel();
		if (PSConfigValues.common.naturalSpawnChance > level.random.nextDouble()) return;
		final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
		if (session == null) return;
		pEvent.setSpawnCancelled(true);
	}

	public static final void conversionPre(final LivingConversionEvent.Pre pEvent) {
		if (pEvent.getOutcome() == null || pEvent.getOutcome().getCategory() != MobCategory.MONSTER || !pEvent.getEntity().getPersistentData().contains(Invasion.INVASION_MOB)) return;
		if (pEvent.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
			if (session == null || !session.stopsConversions() || !session.hasMob(mob)) return;
			pEvent.setCanceled(true);
			pEvent.setConversionTimer(12000 - (int)(level.dayTime() % 12000L));
		}
	}

	public static final void conversionPost(final LivingConversionEvent.Post pEvent) {
		if (pEvent.getOutcome() == null || pEvent.getOutcome().getClassification(false) != MobCategory.MONSTER) return;
		if (pEvent.getOutcome() instanceof Mob resultMob) {
			final CompoundTag persistentData = pEvent.getEntity().getPersistentData();
			final CompoundTag outcomeData = resultMob.getPersistentData();
			if (persistentData.contains(Invasion.INVASION_MOB))
				outcomeData.putInt(Invasion.INVASION_MOB, persistentData.getInt(Invasion.INVASION_MOB));
			if (persistentData.contains(Invasion.ANTI_GRIEF))
				outcomeData.putBoolean(Invasion.ANTI_GRIEF, persistentData.getBoolean(Invasion.ANTI_GRIEF));
			if (persistentData.contains(Invasion.DESPAWN_LOGIC))
				outcomeData.putIntArray(Invasion.DESPAWN_LOGIC, persistentData.getIntArray(Invasion.DESPAWN_LOGIC));
			if (pEvent.getEntity() instanceof Mob mob) {
				if (mob instanceof PSInvasionMob im1 && resultMob instanceof PSInvasionMob im2) {
					im2.psSetHyperCharge(im1.psGetHyperCharge());
					PSInvasionMob.applyHyperEffects(mob);
				}
				if (mob.level() instanceof ServerLevel level) {
					final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
					if (session == null || !session.replaceMob(mob, resultMob)) return;
				}
			}
		}
	}

	public static final void death(final LivingDeathEvent pEvent) {
		if (pEvent.getEntity() instanceof Mob mob && pEvent.getSource().getEntity() instanceof Player) {
			if (!mob.getPersistentData().contains(Invasion.INVASION_MOB)) return;
			final ServerLevel level = (ServerLevel)mob.level();
			final InvasionLevelData ilData = InvasionLevelData.get(level);
			final InvasionSession session = ilData.getInvasionManager().getActiveSession(level);
			if (session == null || !session.hasMob(mob)) return;
			if (session.getMobKillLimit() > 0) {
				++session.mobsKilledByPlayer;
				if (session.mobsKilledByPlayer >= session.getMobKillLimit()) {
					session.clear(level);
					return;
				}
			}
			final Invasion invasion = session.getInvasion(mob);
			if (invasion.getSeverityInfo().getMobKillLimit() > 0) {
				++invasion.mobsKilledByPlayer;
				if (invasion.mobsKilledByPlayer >= invasion.getSeverityInfo().getMobKillLimit()) session.remove(level, invasion);
			}
		}
	}

	public static final void experienceDrop(final LivingExperienceDropEvent pEvent) {
		if (pEvent.getEntity() instanceof Mob mob && mob instanceof PSInvasionMob invasionMob) {
			if (!mob.getPersistentData().contains(Invasion.INVASION_MOB)) return;
			final ServerLevel level = (ServerLevel)mob.level();
			final InvasionLevelData ilData = InvasionLevelData.get(level);
			final InvasionSession session = ilData.getInvasionManager().getActiveSession(level);
			if (session == null || !session.hasMob(mob)) {
				pEvent.setDroppedExperience(pEvent.getOriginalExperience() + (pEvent.getOriginalExperience()/3) * invasionMob.psGetHyperCharge());
			} else if (PSGameRules.USE_XP_MULTIPLIER.get(level) && pEvent.getAttackingPlayer() != null) {
				ilData.setXPMultiplier(ilData.getXPMultiplier() + 1);
				final double log = Math.log1p(ilData.getXPMultiplier())/Math.E;
				pEvent.setDroppedExperience((int)(pEvent.getOriginalExperience() * log) + (pEvent.getOriginalExperience()/3) * invasionMob.psGetHyperCharge());
				PSPackets.sendToClientsIn(new UpdateXPMultPacket(log), level);
			}
		}
	}

	public static final void mobDespawn(final MobDespawnEvent pEvent) {
		final Mob mob = pEvent.getEntity();
		final CompoundTag persistentData = mob.getPersistentData();
		if (!mob.isAlive() || !persistentData.contains(Invasion.DESPAWN_LOGIC)) return;
		final ServerLevel level = (ServerLevel)pEvent.getLevel();
		final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
		final int[] despawnLogic = persistentData.getIntArray(Invasion.DESPAWN_LOGIC);
		final boolean flag1 = mob.getNavigation().isStuck(); //TODO: Testing for faults.
		final boolean flag2 = despawnLogic.length == 6;
		if (session != null && session.hasMob(mob)) {
			if (flag1) session.relocateMob(mob);
			if (flag2) despawnLogic[4] = 0;
		}
		if (flag2) {
			if (despawnLogic[4] > despawnLogic[5]) {
				level.broadcastEntityEvent(mob, (byte)60);
				pEvent.setResult(Result.ALLOW);
				return;
			} else {
				++despawnLogic[4];
			}
		}
	}

	public static final void mobSplit(final MobSplitEvent pEvent) {
		final Mob parent = pEvent.getParent();
		final CompoundTag persistentData = parent.getPersistentData();
		for (final Mob child : pEvent.getChildren()) {
			final CompoundTag outcomeData = child.getPersistentData();
			if (persistentData.contains(Invasion.INVASION_MOB))
				outcomeData.putInt(Invasion.INVASION_MOB, persistentData.getInt(Invasion.INVASION_MOB));
			if (persistentData.contains(Invasion.ANTI_GRIEF))
				outcomeData.putBoolean(Invasion.ANTI_GRIEF, persistentData.getBoolean(Invasion.ANTI_GRIEF));
			if (persistentData.contains(Invasion.DESPAWN_LOGIC))
				outcomeData.putIntArray(Invasion.DESPAWN_LOGIC, persistentData.getIntArray(Invasion.DESPAWN_LOGIC));
			if (parent instanceof PSInvasionMob im1 && child instanceof PSInvasionMob im2) {
				im2.psSetHyperCharge(im1.psGetHyperCharge());
				PSInvasionMob.applyHyperEffects(child);
			}
		}
		if (parent.level() instanceof ServerLevel level) {
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
			if (session == null || !session.splitMob(parent, pEvent.getChildren())) return;
		}
	}
}
