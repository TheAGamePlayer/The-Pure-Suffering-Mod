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

public final class AddInvasionPacket {
	private final InvasionSkyRenderer renderer;
	private final InvasionListType listType;
	private final boolean isPrimary;

	public AddInvasionPacket(final InvasionSkyRenderer rendererIn, final InvasionListType listTypeIn, final boolean isPrimaryIn) {
		this.renderer = rendererIn;
		this.listType = listTypeIn;
		this.isPrimary = isPrimaryIn;
	}

	public static final void encode(final AddInvasionPacket msgIn, final FriendlyByteBuf bufIn) {
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
		bufIn.writeEnum(msgIn.listType);
		bufIn.writeBoolean(msgIn.isPrimary);
	}

	public static final AddInvasionPacket decode(final FriendlyByteBuf bufIn) {
		final InvasionSkyRenderer renderer = InvasionSkyRenderer.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new AddInvasionPacket(renderer, bufIn.readEnum(InvasionListType.class), bufIn.readBoolean());
	}

	public static final class Handler {
		public static final boolean handle(final AddInvasionPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final AddInvasionPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
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
