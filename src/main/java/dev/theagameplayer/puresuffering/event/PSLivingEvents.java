package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.other.PSEntityPredicates;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.entity.PSHyperCharge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public final class PSLivingEvents {
	public static final void livingConversion(final LivingConversionEvent.Post eventIn) { //Needed for occasional bugginess
		if (eventIn.getOutcome().getClassification(false) == MobCategory.MONSTER) {
			final CompoundTag persistentData = eventIn.getEntity().getPersistentData();
			final CompoundTag outcomeData = eventIn.getOutcome().getPersistentData();
			if (persistentData.contains("InvasionMob"))
				outcomeData.putString("InvasionMob", persistentData.getString("InvasionMob"));
			if (persistentData.contains("AntiGrief"))
				outcomeData.putString("AntiGrief", persistentData.getString("AntiGrief"));
		}
	}

	public static final void livingTick(final LivingEvent.LivingTickEvent eventIn) {
		if (eventIn.getEntity() instanceof Mob && eventIn.getEntity().getPersistentData().contains("InvasionMob") && (eventIn.getEntity().getLastHurtByMob() == null || !eventIn.getEntity().getLastHurtByMob().isAlive()) && PSConfigValues.common.hyperAggression && !PSConfigValues.common.hyperAggressionBlacklist.contains(eventIn.getEntity().getType().getDescriptionId())) {
			final Mob mob = (Mob)eventIn.getEntity();
			if (mob.getTarget() instanceof Player) return;
			final Player player = mob.level.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), 144.0D, PSEntityPredicates.HYPER_AGGRESSION);
			if (player != null && player.isAlive()) {
				if (mob instanceof AbstractPiglin) { //If your wondering why a mob doesn't get aggressive, its because it didn't use the default targeting...
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, player.getUUID(), 12000L);
				} else if (mob instanceof Hoglin) {
					mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
					mob.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, 12000L);
				} else if (mob instanceof Zoglin) {
					mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
					mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, player, 12000L);
				} else {
					mob.setTarget(player);
				}
			}
		}
	}

	public static final void experienceDrop(final LivingExperienceDropEvent eventIn) {
		final CompoundTag persistentData = eventIn.getEntity().getPersistentData();
		if (PSConfigValues.common.useXPMultiplier && persistentData.contains("InvasionMob")) {
			final ServerLevel serverLevel = (ServerLevel)eventIn.getEntity().level;
			final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
			if (iwData != null) {
				if (!iwData.hasFixedTime()) {
					final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
					if (ServerTimeUtil.isServerDay(serverLevel, tiwData)) {
						tiwData.setDayXPMultiplier(tiwData.getDayXPMultiplier() + 1);
						final double log = Math.log1p(tiwData.getDayXPMultiplier()) / Math.E;
						for (int hc = 0; hc < (eventIn.getEntity() instanceof PSHyperCharge ? ((PSHyperCharge)eventIn.getEntity()).psGetHyperCharge() + 1 : 1); hc++)
							eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.DAY));
					} else if (ServerTimeUtil.isServerNight(serverLevel, tiwData)) {
						tiwData.setNightXPMultiplier(tiwData.getNightXPMultiplier() + 1);
						final double log = Math.log1p(tiwData.getNightXPMultiplier()) / Math.E;
						for (int hc = 0; hc < (eventIn.getEntity() instanceof PSHyperCharge ? ((PSHyperCharge)eventIn.getEntity()).psGetHyperCharge() + 1 : 1); hc++)
							eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
						PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.NIGHT));
					}
				} else {
					final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
					fiwData.setXPMultiplier(fiwData.getXPMultiplier() + 1);
					final double log = Math.log1p(fiwData.getXPMultiplier()) / Math.E;
					for (int hc = 0; hc < (eventIn.getEntity() instanceof PSHyperCharge ? ((PSHyperCharge)eventIn.getEntity()).psGetHyperCharge() + 1 : 1); hc++)
						eventIn.setDroppedExperience((int)(eventIn.getOriginalExperience() * log));
					PSPacketHandler.sendToAllClients(new UpdateXPMultPacket(log, InvasionListType.FIXED));
				}
			}
		}
	}

	public static final void finalizeSpawn(final MobSpawnEvent.FinalizeSpawn eventIn) {
		if (!eventIn.getLevel().isClientSide()) {
			if (eventIn.getSpawnType() == MobSpawnType.NATURAL) {
				final ServerLevel serverLevel = (ServerLevel)eventIn.getLevel();
				final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
				if (iwData != null) {
					final boolean flag = eventIn.getEntity().getClassification(false) == MobCategory.MONSTER;
					if (serverLevel.random.nextInt(10000) < PSConfigValues.common.naturalSpawnChance) {
						eventIn.setResult(Result.DEFAULT);
					} else {
						if (!iwData.hasFixedTime()) {
							final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
							if ((ServerTimeUtil.isServerDay(serverLevel, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) || (ServerTimeUtil.isServerNight(serverLevel, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()))
								eventIn.setResult(Result.DENY);
							if (flag && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty() || !tiwData.getInvasionSpawner().getNightInvasions().isEmpty())
								eventIn.getEntity().getPersistentData().putBoolean("AntiGrief", false);
						} else {
							final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
							if (!fiwData.getInvasionSpawner().getInvasions().isEmpty())
								eventIn.setResult(Result.DENY);
							if (flag && !fiwData.getInvasionSpawner().getInvasions().isEmpty())
								eventIn.getEntity().getPersistentData().putBoolean("AntiGrief", true);
						}
					}
				}
			}
		}
	}

	public static final void allowDespawn(final MobSpawnEvent.AllowDespawn eventIn) {
		if (!eventIn.getLevel().isClientSide() && PSConfigValues.common.shouldMobsDieAtEndOfInvasions && eventIn.getEntity() instanceof Mob) {
			final ServerLevel serverLevel = (ServerLevel)eventIn.getLevel();
			final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(serverLevel);
			final CompoundTag persistentData = eventIn.getEntity().getPersistentData();
			if (iwData != null && persistentData.contains("InvasionMob")) {
				if (!iwData.hasFixedTime()) {
					final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
					if (ServerTimeUtil.isServerDay(serverLevel, tiwData)) {
						for (final Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
							if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
								return;
							}
						}
						eventIn.setResult(Result.ALLOW);
					} else if (ServerTimeUtil.isServerNight(serverLevel, tiwData)) {
						for (final Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
							if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
								return;
							}
						}
						eventIn.setResult(Result.ALLOW);
					}
				} else {
					final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
					for (final Invasion invasion : fiwData.getInvasionSpawner().getInvasions()) {
						if (persistentData.getString("InvasionMob").equals(invasion.getType().getId().toString())) {
							return;
						}
					}
					eventIn.setResult(Result.ALLOW);
				}
			}
		}
	}
}
