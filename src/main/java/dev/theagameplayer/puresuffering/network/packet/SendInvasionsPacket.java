package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.util.invasion.InvasionText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class SendInvasionsPacket {
	private final boolean isNatural;
	
	public SendInvasionsPacket(final boolean isNaturalIn) {
		this.isNatural = isNaturalIn;
	}

	public static final void encode(final SendInvasionsPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeBoolean(msgIn.isNatural);
	}

	public static final SendInvasionsPacket decode(final FriendlyByteBuf bufIn) {
		return new SendInvasionsPacket(bufIn.readBoolean());
	}

	public static final class Handler {
		public static final boolean handle(final SendInvasionsPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final SendInvasionsPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			if (PSConfigValues.client.useInvasionSoundEffects && msgIn.isNatural)
				mc.player.playSound(PSSoundEvents.INFORM_INVASION.get());
			final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
			if (session != null) {
				mc.getChatListener().handleSystemMessage(InvasionText.create(msgIn.isNatural ? "invasion.puresuffering.inform" : "commands.puresuffering.query.invasions", msgIn.isNatural ? ChatFormatting.GRAY : ChatFormatting.GOLD, session).withStyle(session.getStyle()), false);
			} else if (!msgIn.isNatural) {
				mc.getChatListener().handleSystemMessage(Component.translatable("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			}
		}
	}
}
