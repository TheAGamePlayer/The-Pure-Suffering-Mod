package dev.theagameplayer.puresuffering.client;

import java.util.ArrayList;
import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.util.invasion.InvasionText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;

public final class InvasionStartTimer {
	public static InvasionStartTimer timer;
	private final ArrayList<DelayInfo> delays = new ArrayList<>();

	public InvasionStartTimer(final InvasionDifficulty pDifficulty, final ClientInvasionSession pSession, final boolean pNotifyPlayers) {
		this.delays.add(new DelayInfo(session -> { //2.5 seconds
			if (!pNotifyPlayers) return;
			if (pDifficulty == null) {
				Minecraft.getInstance().getChatListener().handleSystemMessage(pSession.getStartMessage("invasion.puresuffering.start.cancel").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), false);
			} else if (session != null) {
				Minecraft.getInstance().getChatListener().handleSystemMessage(pSession.getStartMessage("invasion.puresuffering.start." + pDifficulty).withStyle(Style.EMPTY.withBold(pDifficulty.isHyper()).withItalic(pDifficulty.isNightmare()).withColor(pDifficulty.getColor(true))), false);
			}
		}, 50));
		this.delays.add(new DelayInfo(session -> { //3.25 seconds
			if (!pNotifyPlayers) return;
			if (pDifficulty != null && session != null)
				Minecraft.getInstance().getChatListener().handleSystemMessage(InvasionText.create("", pDifficulty.getColor(false), pSession).withStyle(pSession.getStyle()), false);
		}, 65));
	}

	public static final void tick(final ClientInvasionSession pSession) {
		if (timer == null) return;
		timer.delays.removeIf(delayInfo -> {
			if (delayInfo.delay > 0) {
				delayInfo.delay--;
				return false;
			} else {
				delayInfo.info.accept(pSession);
				return true;
			}
		});
		if (timer.delays.isEmpty()) timer = null;
	}
	
	private static final class DelayInfo {
		private final Consumer<ClientInvasionSession> info;
		private int delay;
		
		private DelayInfo(final Consumer<ClientInvasionSession> pInfo, final int pDelay) {
			this.info = pInfo;
			this.delay = pDelay;
		}
	}
}
