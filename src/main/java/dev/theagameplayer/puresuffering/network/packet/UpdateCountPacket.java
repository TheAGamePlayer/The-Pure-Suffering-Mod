package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class UpdateCountPacket {
	private final int count;
	private final InvasionListType listType;
	
	public UpdateCountPacket(final int countIn, final InvasionListType listTypeIn) {
		this.count = countIn;
		this.listType = listTypeIn;
	}
	
	public static final void encode(final UpdateCountPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeInt(msgIn.count);
		bufIn.writeEnum(msgIn.listType);
	}
	
	public static final UpdateCountPacket decode(final FriendlyByteBuf bufIn) {
		return new UpdateCountPacket(bufIn.readInt(), bufIn.readEnum(InvasionListType.class));
	}

	public static class Handler {
		public static final boolean handle(final UpdateCountPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static final void handlePacket(final UpdateCountPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
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
