package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class RemoveInvasionPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("remove_invasion");
	private final InvasionSkyRenderInfo renderer;

	public RemoveInvasionPacket(final InvasionSkyRenderInfo rendererIn) {
		this.renderer = rendererIn;
	}

	@Override
	public final void write(final FriendlyByteBuf bufIn) {
		this.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(this.renderer.getId());
	}

	public static final RemoveInvasionPacket read(final FriendlyByteBuf bufIn) {
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new RemoveInvasionPacket(renderer);
	}

	public static final void handle(final RemoveInvasionPacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			ClientInvasionSession.remove(packetIn.renderer);
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
