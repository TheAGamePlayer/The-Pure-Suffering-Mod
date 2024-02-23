package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class ClearInvasionsPacket {
	public static final void encode(final ClearInvasionsPacket msgIn, final FriendlyByteBuf bufIn) {}
	
	public static final ClearInvasionsPacket decode(final FriendlyByteBuf bufIn) {
		return new ClearInvasionsPacket();
	}
	
	public static final class Handler {
		public static final boolean handle(final ClearInvasionsPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static final void handlePacket(final ClearInvasionsPacket msgIn, final Supplier<Context> ctxIn) {
			ClientInvasionSession.clear();
		}
	}
}
