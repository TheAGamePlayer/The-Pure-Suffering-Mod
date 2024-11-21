package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class SendInvasionAmbiencePacket implements CustomPacketPayload {
	public final void write(final FriendlyByteBuf pBuf) {}

	public static final SendInvasionAmbiencePacket read(final FriendlyByteBuf pBuf) {
		return new SendInvasionAmbiencePacket();
	}

	public static final class Handler {
		public static final boolean handle(final SendInvasionAmbiencePacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final SendInvasionAmbiencePacket pPacket, final Supplier<Context> pCtx) {
			final Minecraft mc = Minecraft.getInstance();
			mc.player.playSound(PSSoundEvents.INVASION_AMBIENCE.get(), 1.0F, mc.level.random.nextFloat() * 0.2F - 0.1F);
		}
	}
}
