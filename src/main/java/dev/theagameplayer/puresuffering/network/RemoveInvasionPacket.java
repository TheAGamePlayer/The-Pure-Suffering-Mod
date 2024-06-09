package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RemoveInvasionPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RemoveInvasionPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("remove_invasion"));
	public static final StreamCodec<FriendlyByteBuf, RemoveInvasionPacket> STREAM_CODEC = CustomPacketPayload.codec(RemoveInvasionPacket::write, RemoveInvasionPacket::read);
	private final InvasionSkyRenderInfo renderer;

	public RemoveInvasionPacket(final InvasionSkyRenderInfo pRenderer) {
		this.renderer = pRenderer;
	}

	public final void write(final FriendlyByteBuf pBuf) {
		this.renderer.deconstruct().serializeToNetwork(pBuf);
		pBuf.writeResourceLocation(this.renderer.getId());
	}

	public static final RemoveInvasionPacket read(final FriendlyByteBuf pBuf) {
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(pBuf).build(pBuf.readResourceLocation());
		return new RemoveInvasionPacket(renderer);
	}

	public static final void handle(final RemoveInvasionPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			ClientInvasionSession.remove(pPacket.renderer);
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
