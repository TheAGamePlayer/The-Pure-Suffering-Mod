package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class AddInvasionPacket {
	private final InvasionSkyRenderer renderer;
	private final boolean isDay;
	private final boolean isPrimary;
	
	public AddInvasionPacket(InvasionSkyRenderer rendererIn, boolean isDayIn, boolean isPrimaryIn) {
		this.renderer = rendererIn;
		this.isDay = isDayIn;
		this.isPrimary = isPrimaryIn;
	}
	
	public static void encode(AddInvasionPacket msgIn, PacketBuffer bufIn) {
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
		bufIn.writeBoolean(msgIn.isDay);
		bufIn.writeBoolean(msgIn.isPrimary);
	}
	
	public static AddInvasionPacket decode(PacketBuffer bufIn) {
		InvasionSkyRenderer renderer = InvasionSkyRenderer.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new AddInvasionPacket(renderer, bufIn.readBoolean(), bufIn.readBoolean());
	}

	public static class Handler {
		public static boolean handle(AddInvasionPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(AddInvasionPacket msgIn, Supplier<Context> ctxIn) {
			if (msgIn.isDay) {
				ClientInvasionUtil.getDayRenderers().add(msgIn.renderer, msgIn.isPrimary);
			} else {
				ClientInvasionUtil.getNightRenderers().add(msgIn.renderer, msgIn.isPrimary);
			}
		}
	}
}
