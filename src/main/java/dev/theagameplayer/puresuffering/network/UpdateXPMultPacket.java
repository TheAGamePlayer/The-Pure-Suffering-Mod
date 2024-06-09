package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class UpdateXPMultPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateXPMultPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("update_xp_mult"));
	public static final StreamCodec<FriendlyByteBuf, UpdateXPMultPacket> STREAM_CODEC = CustomPacketPayload.codec(UpdateXPMultPacket::write, UpdateXPMultPacket::read);
	private final double xpMult;

	public UpdateXPMultPacket(final double pXPMult) {
		this.xpMult = pXPMult;
	}

	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeDouble(this.xpMult);
	}

	public static final UpdateXPMultPacket read(final FriendlyByteBuf pBuf) {
		return new UpdateXPMultPacket(pBuf.readDouble());
	}

	public static final void handle(final UpdateXPMultPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session == null) return;
			session.setXPMultiplier(pPacket.xpMult);
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}