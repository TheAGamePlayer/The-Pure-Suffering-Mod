package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import dev.theagameplayer.puresuffering.util.invasion.InvasionText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class SendInvasionsPacket implements CustomPacketPayload {
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

	public static final class Handler {
		public static final boolean handle(final SendInvasionsPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final SendInvasionsPacket pPacket, final Supplier<Context> pCtx) {
			final Minecraft mc = Minecraft.getInstance();
			if (PSConfigValues.client.useInvasionSoundEffects && pPacket.isNatural)
				mc.player.playSound(PSSoundEvents.INFORM_INVASION.get(), 3.0F, 1.0F);
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session != null) {
				mc.getChatListener().handleSystemMessage(InvasionText.create(pPacket.isNatural ? "invasion.puresuffering.inform" : "commands.puresuffering.query.invasions", pPacket.isNatural ? ChatFormatting.GRAY : ChatFormatting.GOLD, session).withStyle(session.getStyle()), false);
			} else if (!pPacket.isNatural) {
				mc.getChatListener().handleSystemMessage(Component.translatable("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			}
		}
	}
}
