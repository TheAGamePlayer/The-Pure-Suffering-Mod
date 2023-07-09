package dev.theagameplayer.puresuffering.util;

import java.awt.Color;
import java.util.HashMap;

import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.util.text.InvasionText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class InvasionMessageTimer {
	private static final HashMap<ServerLevel, InvasionMessageTimer> TIMERS = new HashMap<>();
	private final HyperType hyperType;
	private int delay1, delay2;

	private InvasionMessageTimer(final HyperType hyperTypeIn) {
		this.hyperType = hyperTypeIn;
		this.delay1 = 50; //2.5 seconds
		this.delay2 = 65; //3.25 seconds
	}

	public static final void createTimer(final ServerLevel levelIn, final HyperType hyperTypeIn) {
		TIMERS.put(levelIn, new InvasionMessageTimer(hyperTypeIn));
	}

	public static final void tick(final ServerLevel levelIn, final InvasionList invasionsIn) {
		if (!TIMERS.isEmpty() && !invasionsIn.isEmpty()) {
			final InvasionMessageTimer timer = TIMERS.get(levelIn);
			if (timer != null) {
				if (timer.delay1 > 0) {
					timer.delay1--;
				} else if (timer.delay1 == 0) {
					for (final ServerPlayer player : levelIn.players())
						player.sendSystemMessage(Component.translatable(timer.hyperType != HyperType.DEFAULT ? (timer.hyperType == HyperType.NIGHTMARE ? "invasion.puresuffering.message1" : "invasion.puresuffering.message2") : "invasion.puresuffering.message3").withStyle(Style.EMPTY.withColor(timer.hyperType == HyperType.NIGHTMARE ? (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/2 : ChatFormatting.RED.getColor()).withBold(timer.hyperType != HyperType.DEFAULT).withItalic(timer.hyperType == HyperType.NIGHTMARE)));
					timer.delay1--;
				}
				if (timer.delay2 > 0) {
					timer.delay2--;
				} else {
					for (final ServerPlayer player : levelIn.players())
						player.sendSystemMessage(InvasionText.create("invasion.puresuffering.message4", new Color(timer.hyperType == HyperType.NIGHTMARE ? (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/3 : ChatFormatting.DARK_RED.getColor()), invasionsIn).withStyle(Style.EMPTY.withBold(timer.hyperType != HyperType.DEFAULT).withItalic(timer.hyperType == HyperType.NIGHTMARE)));
					TIMERS.remove(levelIn);
				}
			}
		}
	}
}
