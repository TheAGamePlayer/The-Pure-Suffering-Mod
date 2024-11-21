package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class RemoveInvasionPacket implements CustomPacketPayload {
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

	public static class Handler {
		public static final boolean handle(final RemoveInvasionPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final RemoveInvasionPacket pPacket, final Supplier<Context> pCtx) {
			ClientInvasionSession.remove(pPacket.renderer);
		}
	}
}
