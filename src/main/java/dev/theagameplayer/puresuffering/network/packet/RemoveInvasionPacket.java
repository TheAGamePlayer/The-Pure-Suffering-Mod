package dev.theagameplayer.puresuffering.network.packet;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public final class RemoveInvasionPacket {
	private final InvasionSkyRenderInfo renderer;

	public RemoveInvasionPacket(final InvasionSkyRenderInfo rendererIn) {
		this.renderer = rendererIn;
	}

	public static final void encode(final RemoveInvasionPacket msgIn, final FriendlyByteBuf bufIn) {
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
	}

	public static final RemoveInvasionPacket decode(final FriendlyByteBuf bufIn) {
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new RemoveInvasionPacket(renderer);
	}

	public static class Handler {
		public static final boolean handle(final RemoveInvasionPacket msgIn, CustomPayloadEvent.Context ctxIn) {
			ctxIn.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final RemoveInvasionPacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ClientInvasionSession.remove(msgIn.renderer);
		}
	}
}
