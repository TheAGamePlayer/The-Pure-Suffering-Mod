package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPredicates;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.world.entity.ai.behavior.StartAttackingInvasion;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class PSTickEvents {
	public static final void levelTickPost(final LevelTickEvent.Post pEvent) {
		if (pEvent.getLevel().isClientSide()) { //Ticking Invasion Sky Particles & Music
			final ClientLevel level = (ClientLevel)pEvent.getLevel();
			final ClientInvasionSession session = ClientInvasionSession.get(level);
			InvasionStartTimer.tick(session);
			if (session == null) {
				InvasionMusicManager.tickInactive();
				return;
			}
			session.tick(level, level.getDayTime() % 12000L);
		} else { //Assigning Invasions
			final ServerLevel level = (ServerLevel)pEvent.getLevel();
			if (!PSConfigValues.LEVELS.containsKey(level)) return;
			final InvasionLevelData ilData = InvasionLevelData.get(level);
			final InvasionManager invasionManager = ilData.getInvasionManager();
			final long dayTime = level.getDayTime();
			if (dayTime < ilData.getInvasionTime() || dayTime > ilData.getInvasionTime() + 11999L) {
				invasionManager.setInvasions(level);
				ilData.setInvasionTime(dayTime - dayTime % 12000L);
				ilData.setXPMultiplier(0);
			}
			if (!level.getServer().isSpawningMonsters()) return;
			invasionManager.tick(level);
		}
	}

	public static final void entityTickPost(final EntityTickEvent.Post pEvent) {
		if (pEvent.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
			final CompoundTag persistentData = mob.getPersistentData();
			if (persistentData.contains(Invasion.DESPAWN_LOGIC)) {
				final int[] despawnLogic = persistentData.getIntArray(Invasion.DESPAWN_LOGIC);
				final BlockPos pos = mob.blockPosition();
				if (pos.getX() != despawnLogic[0] || pos.getY() != despawnLogic[1] || pos.getZ() != despawnLogic[2]) {
					despawnLogic[0] = pos.getX();
					despawnLogic[1] = pos.getY();
					despawnLogic[2] = pos.getZ();
					despawnLogic[3] = 0;
				} else if (!level.getPlayers(PSEntityPredicates.HYPER_AGGRESSION).isEmpty()) {
					++despawnLogic[3];
				}
			}
			if (persistentData.contains(Invasion.INVASION_MOB) && PSGameRules.HYPER_AGGRESSION.get(level) && !PSConfigValues.common.hyperAggressionBlacklist.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString()) && (mob.getLastHurtByMob() == null || !mob.getLastHurtByMob().isAlive())) {
				final ServerPlayer player = StartAttackingInvasion.getNearestPlayer(level, mob.position());
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
				} else if (mob instanceof Breeze) { //TODO: fix the breeze
					mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, Invasion.TRANSITION_TIME);
				} else {
					mob.setTarget(player);
				}
			}
		}
	}
}
