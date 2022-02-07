package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateXPMultPacket {
	private final double xpMult;
	private final InvasionListType listType;
	
	public UpdateXPMultPacket(double xpMultIn, InvasionListType listTypeIn) {
		this.xpMult = xpMultIn;
		this.listType = listTypeIn;
	}
	
	public static void encode(UpdateXPMultPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeDouble(msgIn.xpMult);
		bufIn.writeEnum(msgIn.listType);
	}
	
	public static UpdateXPMultPacket decode(PacketBuffer bufIn) {
		return new UpdateXPMultPacket(bufIn.readDouble(), bufIn.readEnum(InvasionListType.class));
	}

	public static class Handler {
		public static boolean handle(UpdateXPMultPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(UpdateXPMultPacket msgIn, Supplier<Context> ctxIn) {
			Minecraft mc = Minecraft.getInstance();
			switch (msgIn.listType) {
			case DAY:
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).setXPMultiplier(msgIn.xpMult);
				break;
			case NIGHT:
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).setXPMultiplier(msgIn.xpMult);
				break;
			case FIXED:
				ClientInvasionWorldInfo.getFixedClientInfo(mc.level).setXPMultiplier(msgIn.xpMult);
				break;
			}
		}
	}
}