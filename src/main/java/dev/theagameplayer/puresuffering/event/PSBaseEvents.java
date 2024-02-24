package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.client.InvasionStartTimer;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.TickEvent;

public final class PSBaseEvents {
	public static final void levelTick(final TickEvent.LevelTickEvent eventIn) {
		if (eventIn.phase == TickEvent.Phase.START) return;
		if (eventIn.side.isClient()) { //Ticking Invasion Sky Particles & Music
			final ClientLevel level = (ClientLevel)eventIn.level;
			final ClientInvasionSession session = ClientInvasionSession.get(level);
			InvasionStartTimer.tick(session);
			if (session == null) {
				InvasionMusicManager.tickInactive();
				return;
			}
			session.tick(level, level.getDayTime() % 12000L);
		} else { //Assigning Invasions
			final ServerLevel level = (ServerLevel)eventIn.level;
			final InvasionLevelData ilData = InvasionLevelData.get(level);
			final InvasionManager invasionManager = ilData.getInvasionManager();
			final long dayTime = level.getDayTime();
			if (dayTime < ilData.getInvasionTime() || dayTime > ilData.getInvasionTime() + 11999L) {
				invasionManager.setInvasions(level);
				ilData.setInvasionTime(dayTime - dayTime % 12000L);
				ilData.setXPMultiplier(0);
			}
		}
	}
}
