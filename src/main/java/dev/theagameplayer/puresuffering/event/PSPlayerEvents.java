package dev.theagameplayer.puresuffering.event;

import java.util.Optional;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.network.SendInvasionsPacket;
import dev.theagameplayer.puresuffering.network.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public final class PSPlayerEvents {
	public static final void canPlayerSleep(final PlayerSleepInBedEvent pEvent) {
		final ServerPlayer player = (ServerPlayer)pEvent.getEntity();
		final InvasionSession session = InvasionLevelData.get(player.serverLevel()).getInvasionManager().getActiveSession(player.serverLevel());
		if (session == null) return;
		if (PSGameRules.FORCE_INVASION_SLEEPLESSNESS.get(player.level()) || session.forcesNoSleep()) {
			player.setRespawnPosition(player.serverLevel().dimension(), pEvent.getPos(), player.getYRot(), false, true);
			pEvent.setResult(BedSleepingProblem.OTHER_PROBLEM);
			player.displayClientMessage(Component.translatable("block.minecraft.bed.invasion").withStyle(session.getStyle().withColor(session.getDifficulty().getColor(false))), true);
		}
	}
	
	public static final void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent pEvent) {
		final ServerPlayer player = (ServerPlayer)pEvent.getEntity();
		PSGameRules.syncToServer(player);
		updatePlayer(player, true, 0);
	}

	public static final void playerRespawn(final PlayerEvent.PlayerRespawnEvent pEvent) {
		final ServerPlayer player = (ServerPlayer)pEvent.getEntity();
		if (pEvent.isEndConquered()) {
			updatePlayer(player, true, PSConfigValues.common.blessingEffectDimensionChangeDuration);
			return;
		}
		final Optional<GlobalPos> deathPos = player.getLastDeathLocation();
		updatePlayer(player, deathPos.isPresent() && !deathPos.get().dimension().equals(player.level().dimension()), PSConfigValues.common.blessingEffectRespawnDuration);
	}

	public static final void playerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent pEvent) {
		if (pEvent.getTo().equals(pEvent.getEntity().level().dimension()))
			updatePlayer((ServerPlayer)pEvent.getEntity(), true, PSConfigValues.common.blessingEffectDimensionChangeDuration);
	}

	private static final void updatePlayer(final ServerPlayer playerIn, final boolean informPlayerIn, final int blessingDurationIn) {
		final InvasionLevelData ilData = InvasionLevelData.get(playerIn.serverLevel());
		final InvasionSession session = ilData.getInvasionManager().getActiveSession(playerIn.serverLevel());
		if (session == null) return;
		session.updateClient(playerIn);
		PSPackets.sendToClient(new UpdateXPMultPacket(Math.log1p(ilData.getXPMultiplier())/Math.E), playerIn);
		if (blessingDurationIn > 0)
			playerIn.addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), blessingDurationIn, 0));
		if (informPlayerIn) PSPackets.sendToClient(new SendInvasionsPacket(true), playerIn);
	}
}
