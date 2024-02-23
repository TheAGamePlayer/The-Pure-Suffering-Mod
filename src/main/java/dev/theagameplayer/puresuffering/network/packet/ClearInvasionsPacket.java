package dev.theagameplayer.puresuffering.network.packet;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public final class ClearInvasionsPacket {
	public static final void encode(final ClearInvasionsPacket msgIn, final FriendlyByteBuf bufIn) {}
	
	public static final ClearInvasionsPacket decode(final FriendlyByteBuf bufIn) {
		return new ClearInvasionsPacket();
	}
	
	public static final class Handler {
		public static final boolean handle(final ClearInvasionsPacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ctxIn.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static final void handlePacket(final ClearInvasionsPacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ClientInvasionSession.clear();
		}
	}
}
