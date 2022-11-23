package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class UpdateXPMultPacket {
	private final double xpMult;
	private final InvasionListType listType;
	
	public UpdateXPMultPacket(final double xpMultIn, final InvasionListType listTypeIn) {
		this.xpMult = xpMultIn;
		this.listType = listTypeIn;
	}
	
	public static void encode(final UpdateXPMultPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeDouble(msgIn.xpMult);
		bufIn.writeEnum(msgIn.listType);
	}
	
	public static UpdateXPMultPacket decode(final FriendlyByteBuf bufIn) {
		return new UpdateXPMultPacket(bufIn.readDouble(), bufIn.readEnum(InvasionListType.class));
	}

	public static class Handler {
		public static boolean handle(final UpdateXPMultPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(final UpdateXPMultPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
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