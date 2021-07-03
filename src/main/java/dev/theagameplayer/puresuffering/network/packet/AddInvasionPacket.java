package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.network.InvasionListType;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class AddInvasionPacket {
	private final InvasionSkyRenderer renderer;
	private final InvasionListType type;
	
	public AddInvasionPacket(InvasionSkyRenderer rendererIn, InvasionListType typeIn) {
		this.renderer = rendererIn;
		this.type = typeIn;
	}
	
	public static void encode(AddInvasionPacket msgIn, PacketBuffer bufIn) {
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
		bufIn.writeEnum(msgIn.type);
	}
	
	public static AddInvasionPacket decode(PacketBuffer bufIn) {
		InvasionSkyRenderer renderer = InvasionSkyRenderer.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new AddInvasionPacket(renderer, bufIn.readEnum(InvasionListType.class));
	}

	public static class Handler {
		public static boolean handle(AddInvasionPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}
		
		private static void handlePacket(AddInvasionPacket msgIn, Supplier<Context> ctxIn) {
			switch (msgIn.type) {
			case DAY:
				ClientInvasionUtil.getDayRenderers().add(msgIn.renderer);
				break;
			case NIGHT:
				ClientInvasionUtil.getNightRenderers().add(msgIn.renderer);
				break;
			case LIGHT:
				ClientInvasionUtil.getLightRenderers().add(msgIn.renderer);
				break;
			}
		}
	}
}
