package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClearInvasionsPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClearInvasionsPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("clear_invasions"));
	public static final StreamCodec<FriendlyByteBuf, ClearInvasionsPacket> STREAM_CODEC = CustomPacketPayload.codec(ClearInvasionsPacket::write, ClearInvasionsPacket::read);

	public final void write(final FriendlyByteBuf pBuf) {}

	public static final ClearInvasionsPacket read(final FriendlyByteBuf pBuf) {
		return new ClearInvasionsPacket();
	}

	public static final void handle(final ClearInvasionsPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			ClientInvasionSession.clear();
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
