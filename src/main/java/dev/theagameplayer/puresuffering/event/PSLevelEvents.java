package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public final class PSLevelEvents {
	public static final void explosionStart(final ExplosionEvent.Start pEvent) { //Ghast balls hit by the player will no longer create fire with Anti-Grief
		final Entity entity = pEvent.getExplosion().getDirectSourceEntity();
		if (entity == null || !PSGameRules.INVASION_ANTI_GRIEF.get(pEvent.getLevel())) return;
		if (entity.getPersistentData().contains(Invasion.ANTI_GRIEF)) pEvent.setCanceled(true);
	}
	
	public static final void load(final LevelEvent.Load pEvent) {
		if (pEvent.getLevel() instanceof ServerLevel level) {
			if (level.getServer().registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM).getOptional(level.dimension().location()).isEmpty()) return;
			PSConfigValues.addLevelValues(level);
			level.getDataStorage().computeIfAbsent(InvasionLevelData.factory(level), InvasionLevelData.getFileId(level.dimensionTypeRegistration()));
		}
	}
}
