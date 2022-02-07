package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateTimePacket {
	private final boolean isDay;
	private final boolean isTime;
	
	public UpdateTimePacket(boolean isDayIn, boolean isTimeIn) {
		this.isDay = isDayIn;
		this.isTime = isTimeIn;
	}
	
	public static void encode(UpdateTimePacket msgIn, PacketBuffer bufIn) {
		bufIn.writeBoolean(msgIn.isDay);
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
			Minecraft mc = Minecraft.getInstance();
			if (msgIn.isDay) {
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).updateClientTime(msgIn.isTime);
			} else {
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).updateClientTime(msgIn.isTime);
			}
		}
	}
}
