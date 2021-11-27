package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class ClearInvasionsPacket {
	private final boolean isDay;
	
	public ClearInvasionsPacket(boolean isDayIn) {
		this.isDay = isDayIn;
	}
	
	public static void encode(ClearInvasionsPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeBoolean(msgIn.isDay);
	}
	
	public static ClearInvasionsPacket decode(PacketBuffer bufIn) {
		return new ClearInvasionsPacket(bufIn.readBoolean());
	}
	
	public static class Handler {
		public static boolean handle(ClearInvasionsPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(ClearInvasionsPacket msgIn, Supplier<Context> ctxIn) {
			if (msgIn.isDay) {
				ClientInvasionUtil.getDayRenderers().clear();
			} else {
				ClientInvasionUtil.getNightRenderers().clear();
			}
		}
	}
}
