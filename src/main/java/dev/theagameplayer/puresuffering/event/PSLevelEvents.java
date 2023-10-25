package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.level.ExplosionEvent;

public final class PSLevelEvents {
	public static final void explosionStart(final ExplosionEvent.Start eventIn) { //Ghast balls hit by the player will no longer create fire with Anti-Grief
		final Entity entity = eventIn.getExplosion().getDirectSourceEntity();
		if (entity == null || !PSGameRules.INVASION_ANTI_GRIEF.get(eventIn.getLevel())) return;
		if (entity.getPersistentData().contains(Invasion.ANTI_GRIEF)) eventIn.setCanceled(true);
	}
}
