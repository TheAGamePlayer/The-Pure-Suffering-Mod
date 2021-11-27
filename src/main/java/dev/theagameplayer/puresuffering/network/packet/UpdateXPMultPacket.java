package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.PSEventManager.ClientEvents;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateXPMultPacket {
	private final double xpMult;
	private final boolean isDay;
	
	public UpdateXPMultPacket(double xpMultIn, boolean isDayIn) {
		this.xpMult = xpMultIn;
		this.isDay = isDayIn;
	}
	
	public static void encode(UpdateXPMultPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeDouble(msgIn.xpMult);
		bufIn.writeBoolean(msgIn.isDay);
	}
	
	public static UpdateXPMultPacket decode(PacketBuffer bufIn) {
		return new UpdateXPMultPacket(bufIn.readDouble(), bufIn.readBoolean());
	}

	public static class Handler {
		public static boolean handle(UpdateXPMultPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(UpdateXPMultPacket msgIn, Supplier<Context> ctxIn) {
			if (msgIn.isDay) {
				ClientEvents.dayXPMult = msgIn.xpMult;
			} else {
				ClientEvents.nightXPMult = msgIn.xpMult;
			}
		}
	}
}