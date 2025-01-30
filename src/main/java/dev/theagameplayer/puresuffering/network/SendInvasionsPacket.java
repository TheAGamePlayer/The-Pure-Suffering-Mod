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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SendInvasionsPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SendInvasionsPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("send_invasions"));
	public static final StreamCodec<FriendlyByteBuf, SendInvasionsPacket> STREAM_CODEC = CustomPacketPayload.codec(SendInvasionsPacket::write, SendInvasionsPacket::read);
	private final boolean isNatural;

	public SendInvasionsPacket(final boolean pIsNatural) {
		this.isNatural = pIsNatural;
	}

	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeBoolean(this.isNatural);
	}

	public static final SendInvasionsPacket read(final FriendlyByteBuf pBuf) {
		return new SendInvasionsPacket(pBuf.readBoolean());
	}

	public static final void handle(final SendInvasionsPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			if (PSConfigValues.client.useInvasionSoundEffects && pPacket.isNatural)
				mc.player.playSound(PSSoundEvents.INFORM_INVASION.get(), 3.0F, 1.0F);
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session != null) {
				mc.getChatListener().handleSystemMessage(InvasionText.create(pPacket.isNatural ? "invasion.puresuffering.inform" : "commands.puresuffering.query.invasions", pPacket.isNatural ? ChatFormatting.GRAY : ChatFormatting.GOLD, session).withStyle(session.getStyle()), false);
			} else if (!pPacket.isNatural) {
				mc.getChatListener().handleSystemMessage(Component.translatable("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			}
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
