package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class InvasionSoundPacket {
	private final HyperType hyperType;
	private final boolean isCanceled;

	public InvasionSoundPacket(final HyperType hyperTypeIn, final boolean isCanceledIn) {
		this.hyperType = hyperTypeIn;
		this.isCanceled = isCanceledIn;
	}

	public static final void encode(final InvasionSoundPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(msgIn.hyperType);
		bufIn.writeBoolean(msgIn.isCanceled);
	}

	public static final InvasionSoundPacket decode(final FriendlyByteBuf bufIn) {
		return new InvasionSoundPacket(bufIn.readEnum(HyperType.class), bufIn.readBoolean());
	}

	public static final class Handler {
		public static final boolean handle(final InvasionSoundPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final InvasionSoundPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			if (PSConfigValues.client.useInvasionSoundEffects)
				mc.player.playSound(msgIn.isCanceled ? PSSoundEvents.CANCEL_INVASION.get() : msgIn.hyperType.getStartSound());
		}
	}
}
