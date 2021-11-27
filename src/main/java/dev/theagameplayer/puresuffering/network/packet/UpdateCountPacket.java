package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.PSEventManager.ClientEvents;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateCountPacket {
	private final int count;
	private final boolean isDay;
	
	public UpdateCountPacket(int countIn, boolean isDayIn) {
		this.count = countIn;
		this.isDay = isDayIn;
	}
	
	public static void encode(UpdateCountPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeInt(msgIn.count);
		bufIn.writeBoolean(msgIn.isDay);
	}
	
	public static UpdateCountPacket decode(PacketBuffer bufIn) {
		return new UpdateCountPacket(bufIn.readInt(), bufIn.readBoolean());
	}

	public static class Handler {
		public static boolean handle(UpdateCountPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(UpdateCountPacket msgIn, Supplier<Context> ctxIn) {
			if (msgIn.isDay) {
				ClientEvents.dayInvasionsCount = msgIn.count;
			} else {
				ClientEvents.nightInvasionsCount = msgIn.count;
			}
		}
	}
}
