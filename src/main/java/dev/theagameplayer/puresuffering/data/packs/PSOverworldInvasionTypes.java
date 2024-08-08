package dev.theagameplayer.puresuffering.data.packs;

import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.InvasionFogRenderInfo;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.DayNightCycleRequirement;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeChangeability;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import dev.theagameplayer.puresuffering.invasion.InvasionType.WeatherType;
import dev.theagameplayer.puresuffering.invasion.data.AdditionalEntitySpawnData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class PSOverworldInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public final void accept(final Consumer<InvasionType> pConsumer) {
		InvasionType.Builder.invasionType().withRarity(6).withTier(1).withInvasionTime(InvasionTime.DAY).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withTimeChangeability(TimeChangeability.ONLY_DAY).withDayNightCycleRequirement(DayNightCycleRequirement.NEEDS_CYCLE).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.15F, 0.15F, 0.15F))
						.sunTexture(PureSufferingMod.namespace("textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.15F)
						.withLightLevel(0)
						.withRGB(-0.85F, -0.85F, -0.85F))
				.withTickDelay(30)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.1F, 0.1F, 0.1F))
						.sunTexture(PureSufferingMod.namespace("textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.1F)
						.withLightLevel(0)
						.withRGB(-0.9F, -0.9F, -0.9F))
				.withTickDelay(24)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.05F, 0.05F, 0.05F))
						.sunTexture(PureSufferingMod.namespace("textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.05F)
						.withLightLevel(0)
						.withRGB(-0.95F, -0.95F, -0.95F))
				.withTickDelay(18)
				.setMobCapMultiplier(1.0F))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "solar_eclipse");
		InvasionType.Builder.invasionType().withRarity(6).withTier(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.ONLY_NIGHT).withDayNightCycleRequirement(DayNightCycleRequirement.NEEDS_CYCLE).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.05F, 0, 0))
						.moonTexture(PureSufferingMod.namespace("textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.1F)
						.withLightLevel(0)
						.withRGB(0, -0.1F, -0.1F))
				.withTickDelay(25)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.1F, 0, 0))
						.moonTexture(PureSufferingMod.namespace("textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.2F)
						.withLightLevel(0)
						.withRGB(0, -0.2F, -0.2F))
				.withTickDelay(20)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.15F, 0, 0))
						.moonTexture(PureSufferingMod.namespace("textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.3F)
						.withLightLevel(0)
						.withRGB(0, -0.3F, -0.3F))
				.withTickDelay(15)
				.setMobCapMultiplier(1.0F))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "lunar_eclipse");
		InvasionType.Builder.invasionType().withRarity(9).withTier(2).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withWeatherType(WeatherType.THUNDER).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.1F, 0.1F, 0.1F))
						.withSkyBrightness(0.2F)
						.withLightLevel(0)
						.withRGB(-0.1F, -0.1F, -0.1F))
				.mobSpawnList(new InvasionSpawnerData(EntityType.DROWNED, 10, 1, 3), new InvasionSpawnerData(EntityType.BREEZE, 4, 1, 2))
				.additionalEntitiesList(new AdditionalEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 1, 156, true))
				.withTickDelay(20)
				.withClusterSize(6)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.2F, 0.2F, 0.2F))
						.withSkyBrightness(0.1F)
						.withLightLevel(0)
						.withRGB(-0.2F, -0.2F, -0.2F))
				.mobSpawnList(new InvasionSpawnerData(EntityType.DROWNED, 10, 1, 4), new InvasionSpawnerData(EntityType.BREEZE, 4, 1, 3))
				.additionalEntitiesList(new AdditionalEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 2, 104, true))
				.withTickDelay(16)
				.withClusterSize(8)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.3F, 0.3F, 0.3F))
						.withSkyBrightness(0.0F)
						.withLightLevel(0)
						.withRGB(-0.3F, -0.3F, -0.3F))
				.mobSpawnList(new InvasionSpawnerData(EntityType.DROWNED, 10, 2, 5), new InvasionSpawnerData(EntityType.BREEZE, 4, 2, 4))
				.additionalEntitiesList(new AdditionalEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 3, 52, true))
				.withTickDelay(12)
				.withClusterSize(10)
				.setMobCapMultiplier(1.0F))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "super_storm");
		InvasionType.Builder.invasionType().withRarity(12).withTier(3).withConversionsStopped().withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withWeatherType(WeatherType.CLEAR).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.85F, 0.15F, 0)
								.withFlickerRGB(-0.85F, -0.85F, -0.85F, 0, 350))
						.withSunMoonAlpha(0.15F)
						.withSkyBrightness(0.3F)
						.withLightLevel(0)
						.withRGB(-0.1F, -0.85F, -0.85F))
				.setForcesNoSleep()
				.mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 1, 2, false, true), new InvasionSpawnerData(EntityType.SKELETON, 5, 1, 2, false, true), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 2, false, true), new InvasionSpawnerData(EntityType.PIGLIN, 6, 1, 2, false, true), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 4, 1, 1, false, true), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 1, false, true), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 3, 1, 1, false, true))
				.withTickDelay(18)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.9F, 0.2F, 0)
								.withFlickerRGB(-0.9F, -0.9F, -0.9F, 0, 300))
						.withSunMoonAlpha(0.1F)
						.withSkyBrightness(0.25F)
						.withLightLevel(0)
						.withRGB(-0.15F, -0.9F, -0.9F))
				.setForcesNoSleep()
				.mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 1, 3, false, true), new InvasionSpawnerData(EntityType.SKELETON, 5, 1, 3, false, true), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 2, false, true), new InvasionSpawnerData(EntityType.PIGLIN, 6, 1, 3, false, true), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 4, 1, 1, false, true), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 2, false, true), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 3, 1, 1, false, true), new InvasionSpawnerData(EntityType.GHAST, 3, 1, 1, true, true), new InvasionSpawnerData(EntityType.WITHER_SKELETON, 3, 1, 2, false, true))
				.withTickDelay(15)
				.setMobCapMultiplier(0.75F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.95F, 0.25F, 0)
								.withFlickerRGB(-0.95F, -0.95F, -0.95F, 0, 250))
						.withSunMoonAlpha(0.05F)
						.withSkyBrightness(0.2F)
						.withLightLevel(0)
						.withRGB(-0.2F, -0.95F, -0.95F))
				.setForcesNoSleep()
				.mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 2, 3, false, true), new InvasionSpawnerData(EntityType.SKELETON, 5, 1, 3, false, true), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 3, false, true), new InvasionSpawnerData(EntityType.PIGLIN, 6, 1, 4, false, true), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 4, 1, 2, false, true), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 2, false, true), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 3, 1, 2, false, true), new InvasionSpawnerData(EntityType.GHAST, 3, 1, 2, true, true), new InvasionSpawnerData(EntityType.WITHER_SKELETON, 3, 1, 2, false, true), new InvasionSpawnerData(EntityType.ZOGLIN, 1, 1, 1, false, true))
				.withTickDelay(12)
				.setMobCapMultiplier(0.85F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(1.0F, 0.3F, 0)
								.withFlickerRGB(-1.0F, -1.0F, -1.0F, 0, 200))
						.withSunMoonAlpha(0.0F)
						.withSkyBrightness(0.15F) 
						.withLightLevel(0)
						.withRGB(-0.25F, -1.0F, -1.0F))
				.setForcesNoSleep()
				.mobSpawnList(new InvasionSpawnerData(EntityType.BLAZE, 5, 2, 4, false, true), new InvasionSpawnerData(EntityType.SKELETON, 5, 1, 4, false, true), new InvasionSpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 3, false, true), new InvasionSpawnerData(EntityType.PIGLIN, 6, 2, 4, false, true), new InvasionSpawnerData(EntityType.PIGLIN_BRUTE, 4, 1, 3, false, true), new InvasionSpawnerData(EntityType.HOGLIN, 4, 1, 3, false, true), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 3, 1, 2, false, true), new InvasionSpawnerData(EntityType.GHAST, 3, 1, 3, true, true), new InvasionSpawnerData(EntityType.WITHER_SKELETON, 3, 1, 3, false, true), new InvasionSpawnerData(EntityType.ZOGLIN, 1, 1, 2, false, true))
				.withTickDelay(9)
				.setMobCapMultiplier(1.0F))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "hell_on_earth");
		InvasionType.Builder.invasionType().withRarity(17).withTier(3).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_MIXED).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withWeatherType(WeatherType.UNSTABLE).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.85F, 0.85F, 0.85F)
								.withFlickerRGB(-0.85F, -0.85F, -0.85F, 0, 75)
								.withFlickerRGB(0, -0.85F, -0.85F, 0, 75)
								.withFlickerRGB(-0.85F, 0, -0.85F, 0, 75)
								.withFlickerRGB(-0.85F, -0.85F, 0, 0, 75)
								.withFlickerRGB(-0.85F, 0, 0, 0, 75)
								.withFlickerRGB(0, -0.85F, 0, 0, 75)
								.withFlickerRGB(0, 0, -0.85F, 0, 75))
						.withFlickerVisibility(-1.0F, 0, 0, 75)
						.withFlickerBrightness(-0.3F, 0.7F, 0, 75)
						.withFlickerRGB(0.85F, 0.85F, 0.85F, 0, 75)
						.withFlickerRGB(0, 0.85F, 0.85F, 0, 75)
						.withFlickerRGB(0.85F, 0, 0.85F, 0, 75)
						.withFlickerRGB(0.85F, 0.85F, 0, 0, 75)
						.withFlickerRGB(0.85F, 0, 0, 0, 75)
						.withFlickerRGB(0, 0.85F, 0, 0, 75)
						.withFlickerRGB(0, 0, 0.85F, 0, 75)
						.withSkyBrightness(0.3F)
						.withLightLevel(0)
						.withRGB(-0.85F, -0.85F, -0.85F))
				.setForcesNoSleep()
				.withTickDelay(15)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.9F, 0.9F, 0.9F)
								.withFlickerRGB(-0.9F, -0.9F, -0.9F, 0, 50)
								.withFlickerRGB(0, -0.9F, -0.9F, 0, 50)
								.withFlickerRGB(-0.9F, 0, -0.9F, 0, 50)
								.withFlickerRGB(-0.9F, -0.9F, 0, 0, 50)
								.withFlickerRGB(-0.9F, 0, 0, 0, 50)
								.withFlickerRGB(0, -0.9F, 0, 0, 50)
								.withFlickerRGB(0, 0, -0.9F, 0, 50))
						.withFlickerVisibility(-1.0F, 0, 0, 50)
						.withFlickerBrightness(-0.2F, 0.8F, 0, 50)
						.withFlickerRGB(0.9F, 0.9F, 0.9F, 0, 50)
						.withFlickerRGB(0, 0.9F, 0.9F, 0, 50)
						.withFlickerRGB(0.9F, 0, 0.9F, 0, 50)
						.withFlickerRGB(0.9F, 0.9F, 0, 0, 50)
						.withFlickerRGB(0.9F, 0, 0, 0, 50)
						.withFlickerRGB(0, 0.9F, 0, 0, 50)
						.withFlickerRGB(0, 0, 0.9F, 0, 50)
						.withSkyBrightness(0.2F)
						.withLightLevel(0)
						.withRGB(-0.9F, -0.9F, -0.9F))
				.setForcesNoSleep()
				.withTickDelay(12)
				.setMobCapMultiplier(0.75F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(0.95F, 0.95F, 0.95F)
								.withFlickerRGB(-0.95F, -0.95F, -0.95F, 0, 25)
								.withFlickerRGB(0, -0.95F, -0.95F, 0, 25)
								.withFlickerRGB(-0.95F, 0, -0.95F, 0, 25)
								.withFlickerRGB(-0.95F, -0.95F, 0, 0, 25)
								.withFlickerRGB(-0.95F, 0, 0, 0, 25)
								.withFlickerRGB(0, -0.95F, 0, 0, 25)
								.withFlickerRGB(0, 0, -0.95F, 0, 25))
						.withFlickerVisibility(-1.0F, 0, 0, 25)
						.withFlickerBrightness(-0.1F, 0.9F, 0, 25)
						.withFlickerRGB(0.95F, 0.95F, 0.95F, 0, 25)
						.withFlickerRGB(0, 0.95F, 0.95F, 0, 25)
						.withFlickerRGB(0.95F, 0, 0.95F, 0, 25)
						.withFlickerRGB(0.95F, 0.95F, 0, 0, 25)
						.withFlickerRGB(0.95F, 0, 0, 0, 25)
						.withFlickerRGB(0, 0.95F, 0, 0, 25)
						.withFlickerRGB(0, 0, 0.95F, 0, 25)
						.withSkyBrightness(0.1F)
						.withLightLevel(0)
						.withRGB(-0.95F, -0.95F, -0.95F))
				.setForcesNoSleep()
				.withTickDelay(9)
				.setMobCapMultiplier(0.85F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderInfo(
						InvasionSkyRenderInfo.Builder.skyRenderInfo().withFog(InvasionFogRenderInfo.Builder.fogRenderer()
								.withRGB(1.0F, 1.0F, 1.0F)
								.withFlickerRGB(-1.0F, -1.0F, -1.0F, 0, 0)
								.withFlickerRGB(0, -1.0F, -1.0F, 0, 0)
								.withFlickerRGB(-1.0F, 0, -1.0F, 0, 0)
								.withFlickerRGB(-1.0F, -1.0F, 0, 0, 0)
								.withFlickerRGB(-1.0F, 0, 0, 0, 0)
								.withFlickerRGB(0, -1.0F, 0, 0, 0)
								.withFlickerRGB(0, 0, -1.0F, 0, 0))
						.withFlickerVisibility(-1.0F, 0, 0, 0)
						.withFlickerBrightness(0, 1.0F, 0, 0)
						.withFlickerRGB(1.0F, 1.0F, 1.0F, 0, 0)
						.withFlickerRGB(0, 1.0F, 1.0F, 0, 0)
						.withFlickerRGB(1.0F, 0, 1.0F, 0, 0)
						.withFlickerRGB(1.0F, 1.0F, 0, 0, 0)
						.withFlickerRGB(1.0F, 0, 0, 0, 0)
						.withFlickerRGB(0, 1.0F, 0, 0, 0)
						.withFlickerRGB(0, 0, 1.0F, 0, 0)
						.withSkyBrightness(0.0F)
						.withLightLevel(0)
						.withRGB(-1.0F, -1.0F, -1.0F))
				.setForcesNoSleep()
				.withTickDelay(6)
				.setMobCapMultiplier(1.0F))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "worlds_end");
		InvasionType.Builder.invasionType().withInvasionTime(InvasionTime.NIGHT).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(30).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 12, 1, 3)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 12, 1, 4), new InvasionSpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 12, 2, 6), new InvasionSpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 12, 3, 8), new InvasionSpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1, 3)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 12, 4, 10), new InvasionSpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 2, 4)))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "zombie");
		InvasionType.Builder.invasionType().withRarity(1).withInvasionTime(InvasionTime.NIGHT).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(24).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 5, 1, 2), new InvasionSpawnerData(EntityType.HUSK, 2, 1, 1), new InvasionSpawnerData(EntityType.SKELETON, 4, 1, 2), new InvasionSpawnerData(EntityType.STRAY, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(20).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 5, 1, 3), new InvasionSpawnerData(EntityType.HUSK, 2, 1, 2), new InvasionSpawnerData(EntityType.SKELETON, 4, 1, 3), new InvasionSpawnerData(EntityType.STRAY, 1, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(16).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 5, 2, 4), new InvasionSpawnerData(EntityType.HUSK, 2, 1, 3), new InvasionSpawnerData(EntityType.SKELETON, 4, 2, 4), new InvasionSpawnerData(EntityType.STRAY, 1, 1, 3), new InvasionSpawnerData(EntityType.BOGGED, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 5, 2, 5), new InvasionSpawnerData(EntityType.HUSK, 2, 2, 3), new InvasionSpawnerData(EntityType.SKELETON, 4, 2, 5), new InvasionSpawnerData(EntityType.STRAY, 1, 2, 3), new InvasionSpawnerData(EntityType.BOGGED, 1, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(8).mobSpawnList(new InvasionSpawnerData(EntityType.ZOMBIE, 5, 3, 7), new InvasionSpawnerData(EntityType.HUSK, 2, 2, 4), new InvasionSpawnerData(EntityType.SKELETON, 4, 3, 6), new InvasionSpawnerData(EntityType.STRAY, 1, 2, 4), new InvasionSpawnerData(EntityType.BOGGED, 1, 1, 3)))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "undead");
		InvasionType.Builder.invasionType().withInvasionTime(InvasionTime.NIGHT).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(30).mobSpawnList(new InvasionSpawnerData(EntityType.SPIDER, 3, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(new InvasionSpawnerData(EntityType.SPIDER, 3, 1, 3), new InvasionSpawnerData(EntityType.CAVE_SPIDER, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(new InvasionSpawnerData(EntityType.SPIDER, 3, 2, 4), new InvasionSpawnerData(EntityType.CAVE_SPIDER, 1, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(new InvasionSpawnerData(EntityType.SPIDER, 3, 2, 5), new InvasionSpawnerData(EntityType.CAVE_SPIDER, 1, 1, 3)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(new InvasionSpawnerData(EntityType.SPIDER, 3, 3, 7), new InvasionSpawnerData(EntityType.CAVE_SPIDER, 1, 2, 4)))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "arachnophobia");
		InvasionType.Builder.invasionType().withRarity(3).withTier(1).withInvasionTime(InvasionTime.NIGHT).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(45).mobSpawnList(new InvasionSpawnerData(EntityType.PILLAGER, 13, 1, 2), new InvasionSpawnerData(EntityType.VINDICATOR, 8, 1, 1), new InvasionSpawnerData(EntityType.WITCH, 10, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(36).mobSpawnList(new InvasionSpawnerData(EntityType.PILLAGER, 13, 1, 3), new InvasionSpawnerData(EntityType.VINDICATOR, 8, 1, 1), new InvasionSpawnerData(EntityType.WITCH, 10, 1, 2), new InvasionSpawnerData(EntityType.RAVAGER, 3, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(27).mobSpawnList(new InvasionSpawnerData(EntityType.PILLAGER, 13, 2, 4), new InvasionSpawnerData(EntityType.VINDICATOR, 8, 1, 2), new InvasionSpawnerData(EntityType.WITCH, 10, 1, 3), new InvasionSpawnerData(EntityType.RAVAGER, 3, 1, 1), new InvasionSpawnerData(EntityType.EVOKER, 5, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(new InvasionSpawnerData(EntityType.PILLAGER, 13, 2, 5), new InvasionSpawnerData(EntityType.VINDICATOR, 8, 1, 3), new InvasionSpawnerData(EntityType.WITCH, 10, 1, 4), new InvasionSpawnerData(EntityType.RAVAGER, 3, 1, 2), new InvasionSpawnerData(EntityType.EVOKER, 5, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(9).mobSpawnList(new InvasionSpawnerData(EntityType.PILLAGER, 13, 2, 5), new InvasionSpawnerData(EntityType.VINDICATOR, 8, 1, 4), new InvasionSpawnerData(EntityType.WITCH, 10, 1, 4), new InvasionSpawnerData(EntityType.RAVAGER, 3, 1, 2), new InvasionSpawnerData(EntityType.EVOKER, 5, 1, 3), new InvasionSpawnerData(EntityType.ILLUSIONER, 1, 1, 1, false, true)))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "mega_raid");
		InvasionType.Builder.invasionType().withRarity(1).withInvasionTime(InvasionTime.NIGHT).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(32).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 1, 3), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 1, 1)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(26).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 2, 4), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 1, 2)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(20).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 2, 5), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 2, 4)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(14).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 3, 6), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 2, 5)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(8).mobSpawnList(new InvasionSpawnerData(EntityType.SILVERFISH, 3, 3, 8), new InvasionSpawnerData(EntityType.ENDERMITE, 1, 2, 6)))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "pest");
		InvasionType.Builder.invasionType().withRarity(3).withTier(2).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).severityInfo(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.3F).withTickDelay(35).mobSpawnList(new InvasionSpawnerData(EntityType.SLIME, 7, 1, 1, true, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(30).mobSpawnList(new InvasionSpawnerData(EntityType.SLIME, 7, 1, 2, true, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.5F).withTickDelay(25).mobSpawnList(new InvasionSpawnerData(EntityType.SLIME, 7, 2, 3, true, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(20).mobSpawnList(new InvasionSpawnerData(EntityType.SLIME, 7, 2, 4, true, true), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 1, 1, 1, false, true)),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(15).mobSpawnList(new InvasionSpawnerData(EntityType.SLIME, 7, 2, 5, true, true), new InvasionSpawnerData(EntityType.MAGMA_CUBE, 1, 1, 2, false, true)))
		.dimensions(Level.OVERWORLD.location())
		.save(pConsumer, "slime_time");
	}
}
