package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class ClearInvasionsPacket implements CustomPacketPayload {
	public final void write(final FriendlyByteBuf pBuf) {}

	public static final ClearInvasionsPacket read(final FriendlyByteBuf pBuf) {
		return new ClearInvasionsPacket();
	}
	
	public static final class Handler {
		public static final boolean handle(final ClearInvasionsPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}
		
		private static final void handlePacket(final ClearInvasionsPacket pPacket, final Supplier<Context> pCtx) {
			ClientInvasionSession.clear();
		}
	}
}
