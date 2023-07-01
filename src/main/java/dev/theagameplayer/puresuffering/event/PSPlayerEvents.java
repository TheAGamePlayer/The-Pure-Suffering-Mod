package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public final class PSPlayerEvents {
	public static final void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent eventIn) {
		updatePlayer(eventIn);
	}

	public static final void playerRespawn(final PlayerEvent.PlayerRespawnEvent eventIn) {
		if (PSConfigValues.common.hyperAggression)
			eventIn.getEntity().addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), PSConfigValues.common.blessingEffectRespawnDuration, 0));
		updatePlayer(eventIn);
	}

	public static final void playerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent eventIn) {
		if (PSConfigValues.common.hyperAggression)
			eventIn.getEntity().addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), PSConfigValues.common.blessingEffectDimensionChangeDuration, 0));
		updatePlayer(eventIn);
	}

	private static final void updatePlayer(final PlayerEvent eventIn) {
		if (eventIn.getEntity() instanceof ServerPlayer) {
			final ServerPlayer player = (ServerPlayer)eventIn.getEntity();
			final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get((ServerLevel)player.level());
			if (iwData != null) {
				if (!iwData.hasFixedTime()) {
					final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
					tiwData.getInvasionSpawner().getDayInvasions().update(player);
					tiwData.getInvasionSpawner().getNightInvasions().update(player);
				} else {
					final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
					fiwData.getInvasionSpawner().getInvasions().update(player);
				}
			}
			ServerTimeUtil.updateTime(player);
		}
	}

	public static final void playerSleepInBed(final PlayerSleepInBedEvent eventIn) {
		final ServerLevel level = (ServerLevel)eventIn.getEntity().level();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(level);
		if (iwData != null && !iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			if (ServerTimeUtil.isServerDay(level, tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) { //Added day check for Mods that allow sleeping during the day
				for (final Invasion invasion : tiwData.getInvasionSpawner().getDayInvasions()) {
					if (PSConfigValues.common.forceInvasionSleeplessness || invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
						eventIn.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
						return;
					}
				}
			} else if (ServerTimeUtil.isServerNight(level, tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
				for (final Invasion invasion : tiwData.getInvasionSpawner().getNightInvasions()) {
					if (PSConfigValues.common.forceInvasionSleeplessness || invasion.getType().getSeverityInfo().get(invasion.getSeverity()).forcesNoSleep()) {
						eventIn.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
						return;
					}
				}
			}
		}
	}
}
