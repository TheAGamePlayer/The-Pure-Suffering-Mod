package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class SendInvasionAmbiencePacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("send_invasion_ambience");

	@Override
	public final void write(final FriendlyByteBuf bufIn) {}

	public static final SendInvasionAmbiencePacket read(final FriendlyByteBuf bufIn) {
		return new SendInvasionAmbiencePacket();
	}

	public static final void handle(final SendInvasionAmbiencePacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			final Minecraft mc = Minecraft.getInstance();
			mc.player.playSound(PSSoundEvents.INVASION_AMBIENCE.get(), 1.0F, mc.level.random.nextFloat() * 0.2F - 0.1F);
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
