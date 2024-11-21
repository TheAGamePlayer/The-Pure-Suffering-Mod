package dev.theagameplayer.puresuffering.data.packs;

import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.client.invasion.InvasionFogRenderInfo;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class PSNetherInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public final void accept(final Consumer<InvasionType> pConsumer) {
		InvasionType.Builder.invasionType().withRarity(6).withTier(2).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo()
				.skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer().withRGB(-0.85F, -0.85F, -0.85F))
						.withRGB(-0.85F, -0.85F, -0.85F)
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.2F)
						.withLightLevel(0))
				.setMobCapMultiplier(0.6F)
				.withTickDelay(45),
				InvasionType.SeverityInfo.Builder.severityInfo()
				.skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer().withRGB(-0.90F, -0.90F, -0.90F))
						.withRGB(-0.90F, -0.90F, -0.90F)
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.1F)
						.withLightLevel(0))
				.setMobCapMultiplier(0.8F)
				.withTickDelay(30),
				InvasionType.SeverityInfo.Builder.severityInfo()
				.skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer().withRGB(-0.95F, -0.95F, -0.95F))
						.withRGB(-0.95F, -0.95F, -0.95F)
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.0F)
						.withLightLevel(0))
				.setMobCapMultiplier(1.0F)
				.withTickDelay(15))
		.dimensions(Level.NETHER.location())
		.save(pConsumer, "nether_again");
		InvasionType.Builder.invasionType().withRarity(4).withTier(1).withInvasionTime(InvasionTime.BOTH).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(30).mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 1, 2), new InvasionSpawnerData(EntityType.SKELETON, 6, 1, 3)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(25).mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 1, 2), new InvasionSpawnerData(EntityType.SKELETON, 6, 1, 3), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 4, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(20).mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 1, 3), new InvasionSpawnerData(EntityType.SKELETON, 6, 1, 4), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 4, 1, 2), new InvasionSpawnerData(EntityType.WITHER_SKELETON, 2, 1, 1), new InvasionSpawnerData(EntityType.GHAST, 1, 1, 1, true, false)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(15).mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 2, 3), new InvasionSpawnerData(EntityType.SKELETON, 6, 2, 4), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 4, 1, 2), new InvasionSpawnerData(EntityType.WITHER_SKELETON, 2, 1, 2), new InvasionSpawnerData(EntityType.GHAST, 1, 1, 2, true, false)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(10).mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 2, 4), new InvasionSpawnerData(EntityType.SKELETON, 6, 2, 5), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 4, 1, 3), new InvasionSpawnerData(EntityType.WITHER_SKELETON, 2, 1, 2), new InvasionSpawnerData(EntityType.GHAST, 1, 1, 3, true, false)))
		.dimensions(Level.NETHER.location())
		.save(pConsumer, "inferno");
		InvasionType.Builder.invasionType().withRarity(3).withInvasionTime(InvasionTime.BOTH).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(28).mobSpawnList(new InvasionSpawnerData(EntityType.PIGLIN, 7, 1, 1), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 2), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(new InvasionSpawnerData(EntityType.PIGLIN, 7, 1, 2), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 1), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 2), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(20).mobSpawnList(new InvasionSpawnerData(EntityType.PIGLIN, 7, 1, 2), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 1), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 3), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(16).mobSpawnList(new InvasionSpawnerData(EntityType.PIGLIN, 7, 1, 3), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 2), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 3), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 2), new InvasionSpawnerData(EntityType.ZOGLIN, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(12).mobSpawnList(new InvasionSpawnerData(EntityType.PIGLIN, 7, 1, 3), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 2), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 4), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 3), new InvasionSpawnerData(EntityType.ZOGLIN, 1, 1, 2)))
		.dimensions(Level.NETHER.location())
		.save(pConsumer, "porkinator");
		InvasionType.Builder.invasionType().withRarity(4).withTier(1).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(80).mobSpawnList(new InvasionSpawnerData(EntityType.GHAST, 1, 1, 1, true, false)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(65).mobSpawnList(new InvasionSpawnerData(EntityType.GHAST, 1, 1, 2, true, false)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.5F).withTickDelay(50).mobSpawnList(new InvasionSpawnerData(EntityType.GHAST, 1, 1, 3, true, false)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(35).mobSpawnList(new InvasionSpawnerData(EntityType.GHAST, 1, 1, 4, true, false)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(20).mobSpawnList(new InvasionSpawnerData(EntityType.GHAST, 1, 2, 5, true, false)))
		.dimensions(Level.NETHER.location())
		.save(pConsumer, "ghasts_galore");
		InvasionType.Builder.invasionType().withRarity(69).withTier(5).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setFixedMobCap(1).withTickDelay(1200).mobSpawnList(new InvasionSpawnerData(EntityType.WITHER, 1, 1, 1, false, true))
				.setForcesNoSleep())
		.dimensions(Level.NETHER.location())
		.save(pConsumer, "wither");
	}
}
