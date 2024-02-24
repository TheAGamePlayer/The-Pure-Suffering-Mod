package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class UpdateXPMultPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("update_xp_mult");
	private final double xpMult;

	public UpdateXPMultPacket(final double xpMultIn) {
		this.xpMult = xpMultIn;
	}

	@Override
	public final void write(final FriendlyByteBuf bufIn) {
		bufIn.writeDouble(this.xpMult);
	}

	public static final UpdateXPMultPacket read(final FriendlyByteBuf bufIn) {
		return new UpdateXPMultPacket(bufIn.readDouble());
	}

	public static final void handle(final UpdateXPMultPacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session == null) return;
			session.setXPMultiplier(packetIn.xpMult);
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}