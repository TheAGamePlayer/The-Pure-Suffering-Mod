package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public final class AddInvasionPacket {
	private final InvasionSkyRenderer renderer;
	private final InvasionListType listType;
	private final boolean isPrimary;

	public AddInvasionPacket(InvasionSkyRenderer rendererIn, InvasionListType listTypeIn, boolean isPrimaryIn) {
		this.renderer = rendererIn;
		this.listType = listTypeIn;
		this.isPrimary = isPrimaryIn;
	}

	public static void encode(AddInvasionPacket msgIn, PacketBuffer bufIn) {
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
		bufIn.writeEnum(msgIn.listType);
		bufIn.writeBoolean(msgIn.isPrimary);
	}

	public static AddInvasionPacket decode(PacketBuffer bufIn) {
		InvasionSkyRenderer renderer = InvasionSkyRenderer.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new AddInvasionPacket(renderer, bufIn.readEnum(InvasionListType.class), bufIn.readBoolean());
	}

	public static class Handler {
		public static boolean handle(AddInvasionPacket msgIn, Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static void handlePacket(AddInvasionPacket msgIn, Supplier<Context> ctxIn) {
			Minecraft mc = Minecraft.getInstance();
			switch (msgIn.listType) {
			case DAY:
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).getRendererMap().add(msgIn.renderer, msgIn.isPrimary);
				break;
			case NIGHT:
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).getRendererMap().add(msgIn.renderer, msgIn.isPrimary);
				break;
			case FIXED:
				ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap().add(msgIn.renderer, msgIn.isPrimary);
				break;
			}
		}
	}
}
