package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SendInvasionAmbiencePacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SendInvasionAmbiencePacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("send_invasion_ambience"));
	public static final StreamCodec<FriendlyByteBuf, SendInvasionAmbiencePacket> STREAM_CODEC = CustomPacketPayload.codec(SendInvasionAmbiencePacket::write, SendInvasionAmbiencePacket::read);

	public final void write(final FriendlyByteBuf pBuf) {}

	public static final SendInvasionAmbiencePacket read(final FriendlyByteBuf pBuf) {
		return new SendInvasionAmbiencePacket();
	}

	public static final void handle(final SendInvasionAmbiencePacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			mc.player.playSound(PSSoundEvents.INVASION_AMBIENCE.get(), 3.0F, mc.level.random.nextFloat() * 0.2F - 0.1F);
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
