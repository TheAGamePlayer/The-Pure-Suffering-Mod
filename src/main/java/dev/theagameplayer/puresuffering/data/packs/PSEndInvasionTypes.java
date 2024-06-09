package dev.theagameplayer.puresuffering.data.packs;

import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.InvasionFogRenderInfo;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.data.AdditionalEntitySpawnData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class PSEndInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public final void accept(final Consumer<InvasionType> pConsumer) {
		InvasionType.Builder.invasionType().withRarity(2).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_MIXED).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer().withRGB(0.15F, 0.15F, 0.15F))
						.fixedSkyTexture(PureSufferingMod.namespace("textures/environment/end_game_sky.png"))
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.2F)
						.withLightLevel(0)
						.withRGB(-0.85F, -0.85F, -0.85F))
				.additionalEntitiesList(new AdditionalEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 1, 312, true))
				.withClusterSize(3)
				.withTickDelay(60)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer().withRGB(0.1F, 0.1F, 0.1F))
						.fixedSkyTexture(PureSufferingMod.namespace("textures/environment/end_game_sky.png"))
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.1F)
						.withLightLevel(0)
						.withRGB(-0.90F, -0.90F, -0.90F))
				.additionalEntitiesList(new AdditionalEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 2, 273, true))
				.withClusterSize(3)
				.withTickDelay(40)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer().withRGB(0.05F, 0.05F, 0.05F))
						.fixedSkyTexture(PureSufferingMod.namespace("textures/environment/end_game_sky.png"))
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.0F)
						.withLightLevel(0)
						.withRGB(-0.95F, -0.95F, -0.95F))
				.additionalEntitiesList(new AdditionalEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 3, 234, true))
				.withClusterSize(3)
				.withTickDelay(20)
				.setMobCapMultiplier(1.0F))
		.dimensions(Level.END.location())
		.save(pConsumer, "end_game");
		InvasionType.Builder.invasionType().withRarity(6).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(30).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 1, 3), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(24).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 2, 4), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(18).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 2, 5), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 2, 4)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(12).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 3, 6), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 2, 5)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 3, 8), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 2, 6)))
		.dimensions(Level.END.location())
		.save(pConsumer, "end_pest");
	}
}
