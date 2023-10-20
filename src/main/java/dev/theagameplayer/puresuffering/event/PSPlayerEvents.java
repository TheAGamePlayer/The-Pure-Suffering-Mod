package dev.theagameplayer.puresuffering.event;

import java.util.Optional;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.SendInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import dev.theagameplayer.puresuffering.registries.PSMobEffects;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public final class PSPlayerEvents {
	public static final void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent eventIn) {
		final ServerPlayer player = (ServerPlayer)eventIn.getEntity();
		PSGameRules.syncToServer(player);
		updatePlayer(player, true, 0);
	}

	public static final void playerRespawn(final PlayerEvent.PlayerRespawnEvent eventIn) {
		final ServerPlayer player = (ServerPlayer)eventIn.getEntity();
		if (eventIn.isEndConquered()) {
			updatePlayer(player, true, PSConfigValues.common.blessingEffectDimensionChangeDuration);
			return;
		}
		final Optional<GlobalPos> deathPos = player.getLastDeathLocation();
		updatePlayer(player, deathPos.isPresent() && !deathPos.get().dimension().equals(player.level().dimension()), PSConfigValues.common.blessingEffectRespawnDuration);
	}

	public static final void playerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent eventIn) {
		if (eventIn.getTo().equals(eventIn.getEntity().level().dimension()))
			updatePlayer((ServerPlayer)eventIn.getEntity(), true, PSConfigValues.common.blessingEffectDimensionChangeDuration);
	}

	private static final void updatePlayer(final ServerPlayer playerIn, final boolean informPlayerIn, final int blessingDurationIn) {
		final InvasionLevelData ilData = InvasionLevelData.get(playerIn.serverLevel());
		final InvasionSession session = ilData.getInvasionManager().getActiveSession(playerIn.serverLevel());
		if (session == null) return;
		session.updateClient(playerIn);
		PSPacketHandler.sendToClient(new UpdateXPMultPacket(Math.log1p(ilData.getXPMultiplier())/Math.E), playerIn);
		if (blessingDurationIn > 0)
			playerIn.addEffect(new MobEffectInstance(PSMobEffects.BLESSING.get(), blessingDurationIn, 0));
		if (informPlayerIn) PSPacketHandler.sendToClient(new SendInvasionsPacket(true), playerIn);
	}

	public static final void playerSleepInBed(final PlayerSleepInBedEvent eventIn) {
		final ServerPlayer player = (ServerPlayer)eventIn.getEntity();
		final InvasionSession session = InvasionLevelData.get(player.serverLevel()).getInvasionManager().getActiveSession(player.serverLevel());
		if (session == null) return;
		if (PSGameRules.FORCE_INVASION_SLEEPLESSNESS.get(player.level()) || session.forcesNoSleep()) {
			if (eventIn.getOptionalPos().isPresent())
				player.setRespawnPosition(player.serverLevel().dimension(), eventIn.getPos(), player.getYRot(), false, true);
			eventIn.setResult(BedSleepingProblem.OTHER_PROBLEM);
			player.displayClientMessage(Component.translatable("block.minecraft.bed.invasion").withStyle(session.getStyle().withColor(session.getDifficulty().getColor(false))), true);
		}
	}
}
