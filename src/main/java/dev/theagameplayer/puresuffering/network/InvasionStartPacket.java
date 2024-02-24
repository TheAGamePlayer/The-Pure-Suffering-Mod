package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class InvasionStartPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("invasion_start");

	@Override
	public final void write(final FriendlyByteBuf bufIn) {}

	public static final InvasionStartPacket read(final FriendlyByteBuf bufIn) {
		return new InvasionStartPacket();
	}

	public static final void handle(final InvasionStartPacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			final Minecraft mc = Minecraft.getInstance();
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			final InvasionDifficulty difficulty = session == null ? null : session.getDifficulty();
			if (PSConfigValues.client.useInvasionSoundEffects)
				mc.player.playSound(difficulty == null ? PSSoundEvents.CANCEL_INVASION.get() : difficulty.getStartSound());
			InvasionStartTimer.timer = new InvasionStartTimer(difficulty, session);
			if (session == null || !PSConfigValues.client.enableInvasionStartEffects) return;
			session.setStartTimer();
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
