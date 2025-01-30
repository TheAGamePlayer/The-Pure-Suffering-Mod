package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class InvasionStartPacket implements CustomPacketPayload {
	private final boolean notifyPlayers;
	private final String cancelMessage;

	public InvasionStartPacket(final boolean pNotifyPlayers) {
		this.notifyPlayers = pNotifyPlayers;
		this.cancelMessage = PSConfigValues.common.cancelInvasionStartMessage;
	}
	
	private InvasionStartPacket(final boolean pNotifyPlayers, final String pCancelMessage) {
		this.notifyPlayers = pNotifyPlayers;
		this.cancelMessage = pCancelMessage;
	}
	
	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeBoolean(this.notifyPlayers);
		pBuf.writeUtf(this.cancelMessage);
	}

	public static final InvasionStartPacket read(final FriendlyByteBuf pBuf) {
		return new InvasionStartPacket(pBuf.readBoolean(), pBuf.readUtf());
	}
	
	public static final class Handler {
		public static final boolean handle(final InvasionStartPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final InvasionStartPacket pPacket, final Supplier<Context> pCtx) {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			final InvasionDifficulty difficulty = session == null ? null : session.getDifficulty();
			if (PSConfigValues.client.useInvasionSoundEffects)
				mc.player.playSound(difficulty == null ? PSSoundEvents.CANCEL_INVASION.get() : difficulty.getStartSound(), 5.0F, 1.0F);
			InvasionStartTimer.timer = new InvasionStartTimer(difficulty, session, pPacket.notifyPlayers, pPacket.cancelMessage);
			if (session != null && PSConfigValues.client.enableInvasionStartEffects) session.setStartTimer();
		}
	}
}
