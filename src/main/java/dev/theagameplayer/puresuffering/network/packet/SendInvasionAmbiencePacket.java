package dev.theagameplayer.puresuffering.network.packet;

import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public final class SendInvasionAmbiencePacket {
	public static final void encode(final SendInvasionAmbiencePacket msgIn, final FriendlyByteBuf bufIn) {}

	public static final SendInvasionAmbiencePacket decode(final FriendlyByteBuf bufIn) {
		return new SendInvasionAmbiencePacket();
	}

	public static final class Handler {
		public static final boolean handle(final SendInvasionAmbiencePacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ctxIn.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final SendInvasionAmbiencePacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			mc.player.playSound(PSSoundEvents.INVASION_AMBIENCE.get(), 1.0F, mc.level.random.nextFloat() * 0.2F - 0.1F);
		}
	}
}
