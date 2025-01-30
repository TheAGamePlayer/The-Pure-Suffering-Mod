package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class InvasionStartPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<InvasionStartPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("invasion_start"));
	public static final StreamCodec<FriendlyByteBuf, InvasionStartPacket> STREAM_CODEC = CustomPacketPayload.codec(InvasionStartPacket::write, InvasionStartPacket::read);
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

	public static final void handle(final InvasionStartPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			final InvasionDifficulty difficulty = session == null ? null : session.getDifficulty();
			if (PSConfigValues.client.useInvasionSoundEffects)
				mc.player.playSound(difficulty == null ? PSSoundEvents.CANCEL_INVASION.get() : difficulty.getStartSound(), 5.0F, 1.0F);
			InvasionStartTimer.timer = new InvasionStartTimer(difficulty, session, pPacket.notifyPlayers, pPacket.cancelMessage);
			if (session != null && PSConfigValues.client.enableInvasionStartEffects) session.setStartTimer();
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
