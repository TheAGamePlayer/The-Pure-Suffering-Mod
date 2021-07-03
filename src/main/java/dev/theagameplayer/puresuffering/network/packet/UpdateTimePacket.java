package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.ClientTimeUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(UpdateTimePacket msgIn, Supplier<Context> ctxIn) {
			if (msgIn.forDay) {
				ClientTimeUtil.updateClientDay(msgIn.isTime);
			} else {
				ClientTimeUtil.updateClientNight(msgIn.isTime);
			}
		}
	}
}
