package dev.theagameplayer.puresuffering.client;

import java.util.ArrayList;
import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.util.invasion.InvasionText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class InvasionStartTimer {
	public static InvasionStartTimer timer;
	private final ArrayList<DelayInfo> delays = new ArrayList<>();

	public InvasionStartTimer(final InvasionDifficulty difficultyIn, final ClientInvasionSession sessionIn) {
		this.delays.add(new DelayInfo(session -> { //2.5 seconds
			if (difficultyIn == null) {
				Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("invasion.puresuffering.start.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), false);
			} else if (session != null) {
				Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("invasion.puresuffering.start." + difficultyIn).withStyle(Style.EMPTY.withBold(difficultyIn.isHyper()).withItalic(difficultyIn.isNightmare()).withColor(difficultyIn.getColor(true))), false);
			}
		}, 50));
		this.delays.add(new DelayInfo(session -> { //3.25 seconds
			if (difficultyIn != null && session != null)
				Minecraft.getInstance().getChatListener().handleSystemMessage(InvasionText.create("", difficultyIn.getColor(false), sessionIn).withStyle(sessionIn.getStyle()), false);
		}, 65));
	}

	public static final void tick(final ClientInvasionSession sessionIn) {
		if (timer == null) return;
		timer.delays.removeIf(delayInfo -> {
			if (delayInfo.delay > 0) {
				delayInfo.delay--;
				return false;
			} else {
				delayInfo.info.accept(sessionIn);
				return true;
			}
		});
		if (timer.delays.isEmpty()) timer = null;
	}
	
	private static final class DelayInfo {
		private final Consumer<ClientInvasionSession> info;
		private int delay;
		
		private DelayInfo(final Consumer<ClientInvasionSession> infoIn, final int delayIn) {
			this.info = infoIn;
			this.delay = delayIn;
		}
	}
}
