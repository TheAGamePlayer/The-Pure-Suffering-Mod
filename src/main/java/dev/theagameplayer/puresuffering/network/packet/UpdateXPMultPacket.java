package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class UpdateXPMultPacket {
	private final double xpMult;
	
	public UpdateXPMultPacket(final double xpMultIn) {
		this.xpMult = xpMultIn;
	}
	
	public static final void encode(final UpdateXPMultPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeDouble(msgIn.xpMult);
	}
	
	public static final UpdateXPMultPacket decode(final FriendlyByteBuf bufIn) {
		return new UpdateXPMultPacket(bufIn.readDouble());
	}

	public static class Handler {
		public static final boolean handle(final UpdateXPMultPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static final void handlePacket(final UpdateXPMultPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session == null) return;
			session.setXPMultiplier(msgIn.xpMult);
		}
	}
}