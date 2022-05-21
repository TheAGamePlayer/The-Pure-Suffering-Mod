package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class ClearInvasionsPacket {
	private final InvasionListType listType;
	
	public ClearInvasionsPacket(InvasionListType listTypeIn) {
		this.listType = listTypeIn;
	}
	
	public static void encode(ClearInvasionsPacket msgIn, FriendlyByteBuf bufIn) {
		bufIn.writeEnum(msgIn.listType);
	}
	
	public static ClearInvasionsPacket decode(FriendlyByteBuf bufIn) {
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
			Minecraft mc = Minecraft.getInstance();
			switch (msgIn.listType) {
			case DAY:
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).getRendererMap().clear();
				break;
			case NIGHT:
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).getRendererMap().clear();
				break;
			case FIXED:
				ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap().clear();
				break;
			}
		}
	}
}
