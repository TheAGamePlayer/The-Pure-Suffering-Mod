package dev.theagameplayer.puresuffering.data.packs;

import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class PSMultiDimInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public final void accept(final Consumer<InvasionType> pConsumer) {
		InvasionType.Builder.invasionType().withRarity(6).withTier(2).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.1F).withTickDelay(90).mobSpawnList(new InvasionSpawnerData(EntityType.PHANTOM, 1, 1, 1, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(75).mobSpawnList(new InvasionSpawnerData(EntityType.PHANTOM, 1, 1, 2, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.3F).withTickDelay(60).mobSpawnList(new InvasionSpawnerData(EntityType.PHANTOM, 1, 1, 3, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(45).mobSpawnList(new InvasionSpawnerData(EntityType.PHANTOM, 1, 1, 4, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.5F).withTickDelay(30).mobSpawnList(new InvasionSpawnerData(EntityType.PHANTOM, 1, 2, 5, false, true)))
		.dimensions(Level.OVERWORLD.location(), Level.END.location())
		.save(pConsumer, "phantom_zone");
		InvasionType.Builder.invasionType().withRarity(20).withTier(4).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setFixedMobCap(1).withTickDelay(540).mobSpawnList(new InvasionSpawnerData(EntityType.WARDEN, 1, 1, 1, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setFixedMobCap(2).withTickDelay(360).mobSpawnList(new InvasionSpawnerData(EntityType.WARDEN, 1, 1, 1, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setFixedMobCap(3).withTickDelay(180).mobSpawnList(new InvasionSpawnerData(EntityType.WARDEN, 1, 1, 1, false, true))
				.setForcesNoSleep())
		.dimensions(Level.OVERWORLD.location(), Level.END.location())
		.save(pConsumer, "warden");
	}
}
