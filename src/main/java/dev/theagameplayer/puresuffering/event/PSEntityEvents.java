package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public final class PSEntityEvents {
	public static final void joinLevel(final EntityJoinLevelEvent eventIn) {
		if (eventIn.getLevel().isClientSide()) return;
		final Entity entity = eventIn.getEntity();
		if (entity instanceof Mob mob) {
			if (eventIn.loadedFromDisk()) {
				final ServerLevel level = (ServerLevel)eventIn.getLevel();
				final InvasionLevelData ilData = InvasionLevelData.get(level);
				if (ilData != null) {
					final InvasionSession session =  ilData.getInvasionManager().getActiveSession(level);
					if (session != null) session.loadMob(mob);
				}
			} else if (mob instanceof PSInvasionMob invasionMob && invasionMob.psGetHyperCharge() > 0) {
				PSInvasionMob.applyHyperEffects(mob);
			}
		}
		Entity owner = null;
		if (entity instanceof OwnableEntity ownableEntity)
			owner = ownableEntity.getOwner();
		if (entity instanceof TraceableEntity traceableEntity)
			owner = traceableEntity.getOwner();
		if (owner != null && owner.getPersistentData().contains(Invasion.ANTI_GRIEF))
			entity.getPersistentData().putBoolean(Invasion.ANTI_GRIEF, owner.getPersistentData().getBoolean(Invasion.ANTI_GRIEF));
		if (PSGameRules.WEAKENED_INVASION_VEXES.get(eventIn.getLevel()) && entity instanceof Vex vex) {
			if (owner != null && owner.getPersistentData().contains(Invasion.INVASION_MOB))
				vex.setLimitedLife(25 + eventIn.getLevel().getRandom().nextInt(65)); //Attempt to fix lag & spawn camping with vexes
		}
	}

	public static final void mobGriefing(final EntityMobGriefingEvent eventIn) {
		final Entity entity = eventIn.getEntity();
		if (!PSGameRules.INVASION_ANTI_GRIEF.get(entity.level())) return;
		if (entity.getPersistentData().contains(Invasion.ANTI_GRIEF))
			eventIn.setResult(Result.DENY);
	}
}
