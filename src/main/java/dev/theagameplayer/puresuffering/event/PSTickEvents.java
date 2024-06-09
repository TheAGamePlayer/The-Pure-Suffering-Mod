package dev.theagameplayer.puresuffering.event;

import java.util.List;

import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPredicates;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;
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
			final InvasionLevelData ilData = InvasionLevelData.get(level);
			final InvasionManager invasionManager = ilData.getInvasionManager();
			final long dayTime = level.getDayTime();
			if (dayTime < ilData.getInvasionTime() || dayTime > ilData.getInvasionTime() + 11999L) {
				invasionManager.setInvasions(level);
				ilData.setInvasionTime(dayTime - dayTime % 12000L);
				ilData.setXPMultiplier(0);
			}
		}
	}
	
	public static final void entityTickPost(final EntityTickEvent.Post pEvent) {
		if (pEvent.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
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
				} else if (getNearestPlayer(level, mob.position()) != null) {
					despawnLogic[3]++;
				}
			}
			if (persistentData.contains(Invasion.INVASION_MOB) && flag1 && flag2) {
				final ServerPlayer player = getNearestPlayer(level, mob.position());
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
	
	private static final ServerPlayer getNearestPlayer(final ServerLevel pLevel, final Vec3 pPos) {
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
