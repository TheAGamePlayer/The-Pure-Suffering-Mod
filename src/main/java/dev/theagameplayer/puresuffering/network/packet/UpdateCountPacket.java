package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class UpdateCountPacket {
	private final int count;
	private final InvasionListType listType;
	
	public UpdateCountPacket(int countIn, InvasionListType listTypeIn) {
		this.count = countIn;
		this.listType = listTypeIn;
	}
	
	public static void encode(UpdateCountPacket msgIn, PacketBuffer bufIn) {
		bufIn.writeInt(msgIn.count);
		bufIn.writeEnum(msgIn.listType);
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
			Minecraft mc = Minecraft.getInstance();
			switch (msgIn.listType) {
			case DAY:
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).setInvasionsCount(msgIn.count);
				break;
			case NIGHT:
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).setInvasionsCount(msgIn.count);
				break;
			case FIXED:
				ClientInvasionWorldInfo.getFixedClientInfo(mc.level).setInvasionsCount(msgIn.count);
				break;
			}
		}
	}
}
