package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class ClearInvasionsPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("clear_invasions");

	@Override
	public final void write(final FriendlyByteBuf bufIn) {}

	public static final ClearInvasionsPacket read(final FriendlyByteBuf bufIn) {
		return new ClearInvasionsPacket();
	}

	public static final void handle(final ClearInvasionsPacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			ClientInvasionSession.clear();
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
