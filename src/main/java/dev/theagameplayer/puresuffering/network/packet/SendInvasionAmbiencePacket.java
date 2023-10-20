package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class SendInvasionAmbiencePacket {
	public static final void encode(final SendInvasionAmbiencePacket msgIn, final FriendlyByteBuf bufIn) {}

	public static final SendInvasionAmbiencePacket decode(final FriendlyByteBuf bufIn) {
		return new SendInvasionAmbiencePacket();
	}

	public static final class Handler {
		public static final boolean handle(final SendInvasionAmbiencePacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final SendInvasionAmbiencePacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			mc.player.playSound(PSSoundEvents.INVASION_AMBIENCE.get(), 1.0F, mc.level.random.nextFloat() * 0.2F - 0.1F);
		}
	}
}
