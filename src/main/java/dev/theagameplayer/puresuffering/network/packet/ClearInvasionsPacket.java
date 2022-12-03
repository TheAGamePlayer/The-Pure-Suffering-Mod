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
	
	public ClearInvasionsPacket(final InvasionListType listTypeIn) {
		this.listType = listTypeIn;
	}
	
	public static final void encode(final ClearInvasionsPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(msgIn.listType);
	}
	
	public static final ClearInvasionsPacket decode(final FriendlyByteBuf bufIn) {
		return new ClearInvasionsPacket(bufIn.readEnum(InvasionListType.class));
	}
	
	public static final class Handler {
		public static final boolean handle(final ClearInvasionsPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static final void handlePacket(final ClearInvasionsPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
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
