package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class RemoveInvasionPacket {
	private final InvasionSkyRenderer renderer;
	private final InvasionListType listType;

	public RemoveInvasionPacket(final InvasionSkyRenderer rendererIn, final InvasionListType listTypeIn) {
		this.renderer = rendererIn;
		this.listType = listTypeIn;
	}

	public static void encode(final RemoveInvasionPacket msgIn, final FriendlyByteBuf bufIn) {
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
		bufIn.writeEnum(msgIn.listType);
	}

	public static RemoveInvasionPacket decode(final FriendlyByteBuf bufIn) {
		final InvasionSkyRenderer renderer = InvasionSkyRenderer.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new RemoveInvasionPacket(renderer, bufIn.readEnum(InvasionListType.class));
	}

	public static class Handler {
		public static boolean handle(final RemoveInvasionPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static void handlePacket(final RemoveInvasionPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			switch (msgIn.listType) {
			case DAY:
				ClientInvasionWorldInfo.getDayClientInfo(mc.level).getRendererMap().remove(msgIn.renderer);
				break;
			case NIGHT:
				ClientInvasionWorldInfo.getNightClientInfo(mc.level).getRendererMap().remove(msgIn.renderer);
				break;
			case FIXED:
				ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap().remove(msgIn.renderer);
				break;
			}
		}
	}
}
