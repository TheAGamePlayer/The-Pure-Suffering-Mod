package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.PSEventManager.ClientEvents;
import dev.theagameplayer.puresuffering.network.InvasionListType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateCountPacket {
	private final int count;
	private final InvasionListType type;
	
	public UpdateCountPacket(int countIn, InvasionListType typeIn) {
		this.count = countIn;
		this.type = typeIn;
	}
	
	public static void encode(UpdateCountPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeInt(msgIn.count);
		bufIn.writeEnum(msgIn.type);
	}
	
	public static UpdateCountPacket decode(PacketBuffer bufIn) {
		return new UpdateCountPacket(bufIn.readInt(), bufIn.readEnum(InvasionListType.class));
	}

	public static class Handler {
		public static boolean handle(UpdateCountPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(UpdateCountPacket msgIn, Supplier<Context> ctxIn) {
			switch (msgIn.type) {
			case DAY:
				ClientEvents.dayInvasionsCount = msgIn.count;
				break;
			case NIGHT:
				ClientEvents.nightInvasionsCount = msgIn.count;
				break;
			case LIGHT:
				break;
			}
		}
	}
}
