package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.util.invasion.InvasionText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class SendInvasionsPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("send_invasions");
	private final boolean isNatural;

	public SendInvasionsPacket(final boolean isNaturalIn) {
		this.isNatural = isNaturalIn;
	}

	@Override
	public final void write(final FriendlyByteBuf bufIn) {
		bufIn.writeBoolean(this.isNatural);
	}

	public static final SendInvasionsPacket read(final FriendlyByteBuf bufIn) {
		return new SendInvasionsPacket(bufIn.readBoolean());
	}

	public static final void handle(final SendInvasionsPacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			final Minecraft mc = Minecraft.getInstance();
			if (PSConfigValues.client.useInvasionSoundEffects && packetIn.isNatural)
				mc.player.playSound(PSSoundEvents.INFORM_INVASION.get());
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session != null) {
				mc.getChatListener().handleSystemMessage(InvasionText.create(packetIn.isNatural ? "invasion.puresuffering.inform" : "commands.puresuffering.query.invasions", packetIn.isNatural ? ChatFormatting.GRAY : ChatFormatting.GOLD, session).withStyle(session.getStyle()), false);
			} else if (!packetIn.isNatural) {
				mc.getChatListener().handleSystemMessage(Component.translatable("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			}
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
