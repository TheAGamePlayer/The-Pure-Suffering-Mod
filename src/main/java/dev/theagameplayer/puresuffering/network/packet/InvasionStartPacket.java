package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class InvasionStartPacket {
	public static final void encode(final InvasionStartPacket msgIn, final FriendlyByteBuf bufIn) {}
	
	public static final InvasionStartPacket decode(final FriendlyByteBuf bufIn) {
		return new InvasionStartPacket();
	}
	
	public static final class Handler {
		public static final boolean handle(final InvasionStartPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final InvasionStartPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			final InvasionDifficulty difficulty = session == null ? null : session.getDifficulty();
			if (PSConfigValues.client.useInvasionSoundEffects)
				mc.player.playSound(difficulty == null ? PSSoundEvents.CANCEL_INVASION.get() : difficulty.getStartSound());
			InvasionStartTimer.timer = new InvasionStartTimer(difficulty, session);
			if (session == null || !PSConfigValues.client.enableInvasionStartEffects) return;
			session.setStartTimer();
		}
	}
}
