package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class UpdateTimePacket {
	private final boolean isDay;
	private final boolean isTime;
	
	public UpdateTimePacket(final boolean isDayIn, final boolean isTimeIn) {
		this.isDay = isDayIn;
		this.isTime = isTimeIn;
	}
	
	public static final void encode(final UpdateTimePacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeBoolean(msgIn.isDay);
		bufIn.writeBoolean(msgIn.isTime);
	}

	public static final UpdateTimePacket decode(final FriendlyByteBuf bufIn) {
		return new UpdateTimePacket(bufIn.readBoolean(), bufIn.readBoolean());
	}

	public static class Handler {
		public static final boolean handle(final UpdateTimePacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static final void handlePacket(final UpdateTimePacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			if (msgIn.isDay) {
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).updateClientTime(msgIn.isTime);
			} else {
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).updateClientTime(msgIn.isTime);
			}
		}
	}
}
