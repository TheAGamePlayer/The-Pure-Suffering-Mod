package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class UpdateXPMultPacket implements CustomPacketPayload {
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

	public static class Handler {
		public static final boolean handle(final UpdateXPMultPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}
		
		private static final void handlePacket(final UpdateXPMultPacket pPacket, final Supplier<Context> pCtx) {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session == null) return;
			session.setXPMultiplier(pPacket.xpMult);
		}
	}
}