package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.TimeUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateTimePacket {
	private final boolean forDay;
	private final boolean isTime;
	
	public UpdateTimePacket(boolean forDayIn, boolean isTimeIn) {
		this.forDay = forDayIn;
		this.isTime = isTimeIn;
	}
	
	public static void encode(UpdateTimePacket msgIn, PacketBuffer bufIn) {
		bufIn.writeBoolean(msgIn.forDay);
		bufIn.writeBoolean(msgIn.isTime);
	}

	public static UpdateTimePacket decode(PacketBuffer bufIn) {
		return new UpdateTimePacket(bufIn.readBoolean(), bufIn.readBoolean());
	}

	public static class Handler {
		public static boolean handle(UpdateTimePacket msgIn, Supplier<Context> ctxIn) {
			if (msgIn.forDay) {
				TimeUtil.updateClientDay(msgIn.isTime);
			} else {
				TimeUtil.updateClientNight(msgIn.isTime);
			}
			return true;
		}
	}
}
