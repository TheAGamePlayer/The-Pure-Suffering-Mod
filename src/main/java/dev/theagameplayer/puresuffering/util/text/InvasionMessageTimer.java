package dev.theagameplayer.puresuffering.util.text;

import java.awt.Color;
import java.util.HashMap;

import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class InvasionMessageTimer {
	private static final HashMap<ServerLevel, InvasionMessageTimer> TIMERS = new HashMap<>();
	private final InvasionListType listType;
	private final HyperType hyperType;
	private final boolean isCanceled;
	private int delay1, delay2;

	private InvasionMessageTimer(final InvasionListType listTypeIn, final HyperType hyperTypeIn, final boolean isCanceledIn) {
		this.listType = listTypeIn;
		this.hyperType = hyperTypeIn;
		this.isCanceled = isCanceledIn;
		this.delay1 = 50; //2.5 seconds
		this.delay2 = 65; //3.25 seconds
	}

	public static final void createTimer(final ServerLevel levelIn, final InvasionListType listTypeIn, final HyperType hyperTypeIn, final boolean isCanceledIn) {
		TIMERS.put(levelIn, new InvasionMessageTimer(listTypeIn, hyperTypeIn, isCanceledIn));
	}

	public static final void tick(final ServerLevel levelIn, final InvasionList invasionsIn) {
		if (!TIMERS.isEmpty()) {
			final InvasionMessageTimer timer = TIMERS.get(levelIn);
			if (timer != null) {
				if (timer.delay1 > 0) {
					timer.delay1--;
				} else if (timer.delay1 == 0) {
					if (timer.isCanceled) {
						for (final ServerPlayer player : levelIn.players())
							player.sendSystemMessage(Component.translatable(timer.listType.getCancelComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
					} else if (!invasionsIn.isEmpty()) {
						for (final ServerPlayer player : levelIn.players())
							player.sendSystemMessage(Component.translatable(timer.hyperType.getStartComponent()).withStyle(Style.EMPTY.withColor(timer.hyperType == HyperType.NIGHTMARE ? (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/2 : ChatFormatting.RED.getColor()).withBold(timer.hyperType != HyperType.DEFAULT).withItalic(timer.hyperType == HyperType.NIGHTMARE)));
					}
					timer.delay1--;
				}
				if (timer.delay2 > 0) {
					timer.delay2--;
				} else {
					if (!timer.isCanceled && !invasionsIn.isEmpty()) {
						for (final ServerPlayer player : levelIn.players())
							player.sendSystemMessage(InvasionText.create("invasion.puresuffering.message0", new Color(timer.hyperType == HyperType.NIGHTMARE ? (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/3 : ChatFormatting.DARK_RED.getColor()), invasionsIn).withStyle(Style.EMPTY.withBold(timer.hyperType != HyperType.DEFAULT).withItalic(timer.hyperType == HyperType.NIGHTMARE)));
					}
					TIMERS.remove(levelIn);
				}
			}
		}
	}
}
