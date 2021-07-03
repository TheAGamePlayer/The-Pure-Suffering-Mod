package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.network.InvasionListType;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class ClearInvasionsPacket {
	private final InvasionListType type;
	
	public ClearInvasionsPacket(InvasionListType typeIn) {
		this.type = typeIn;
	}
	
	public static void encode(ClearInvasionsPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeEnum(msgIn.type);
	}
	
	public static ClearInvasionsPacket decode(PacketBuffer bufIn) {
		return new ClearInvasionsPacket(bufIn.readEnum(InvasionListType.class));
	}
	
	public static class Handler {
		public static boolean handle(ClearInvasionsPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(ClearInvasionsPacket msgIn, Supplier<Context> ctxIn) {
			switch (msgIn.type) {
			case DAY:
				ClientInvasionUtil.getDayRenderers().clear();
				break;
			case NIGHT:
				ClientInvasionUtil.getNightRenderers().clear();
				break;
			case LIGHT:
				ClientInvasionUtil.getLightRenderers().clear();
				break;
			}
		}
	}
}
