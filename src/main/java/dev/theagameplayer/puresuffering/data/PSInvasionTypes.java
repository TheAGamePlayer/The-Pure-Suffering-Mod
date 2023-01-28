package dev.theagameplayer.puresuffering.data;

import java.util.List;
import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.invasion.ClusterEntitySpawnData;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeChangeability;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import dev.theagameplayer.puresuffering.invasion.InvasionType.WeatherType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

public final class PSInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public final void accept(Consumer<InvasionType> consumerIn) {
		//OVERWORLD
		InvasionType.Builder.invasionType().withRarity(7).withTier(1).withInvasionTime(InvasionTime.DAY).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withTimeChangeability(TimeChangeability.ONLY_DAY).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.15F, 0.15F, 0.15F))
						.sunTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.15F)
						.withRGB(-0.85F, -0.85F, -0.85F))
				.setForcesNoSleep()
				.withLightLevel(15)
				.withTickDelay(30)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.1F, 0.1F, 0.1F))
						.sunTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.1F)
						.withRGB(-0.9F, -0.9F, -0.9F))
				.setForcesNoSleep()
				.withLightLevel(15)
				.withTickDelay(24)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.05F, 0.05F, 0.05F))
						.sunTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.05F)
						.withRGB(-0.95F, -0.95F, -0.95F))
				.setForcesNoSleep()
				.withLightLevel(15)
				.withTickDelay(18)
				.setMobCapMultiplier(1.0F)))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "solar_eclipse");
		InvasionType.Builder.invasionType().withRarity(7).withTier(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.ONLY_NIGHT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.05F, 0, 0))
						.moonTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.1F)
						.withRGB(0, -0.1F, -0.1F))
				.withTickDelay(25)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.1F, 0, 0))
						.moonTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.2F)
						.withRGB(0, -0.2F, -0.2F))
				.withTickDelay(20)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.15F, 0, 0))
						.moonTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.3F)
						.withRGB(0, -0.3F, -0.3F))
				.withTickDelay(15)
				.setMobCapMultiplier(1.0F)))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "lunar_eclipse");
		InvasionType.Builder.invasionType().withRarity(10).withTier(2).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withTimeChangeability(TimeChangeability.DEFAULT).withWeatherType(WeatherType.THUNDER).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.1F, 0.1F, 0.1F))
						.withSkyBrightness(0.2F)
						.withRGB(-0.1F, -0.1F, -0.1F))
				.mobSpawnList(List.of(new SpawnerData(EntityType.DROWNED, 10, 1, 3)))
				.clusterEntitiesList(List.of(new ClusterEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 1, 12)))
				.withLightLevel(15)
				.withTickDelay(20)
				.withClusterSize(6)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.2F, 0.2F, 0.2F))
						.withSkyBrightness(0.1F)
						.withRGB(-0.2F, -0.2F, -0.2F))
				.mobSpawnList(List.of(new SpawnerData(EntityType.DROWNED, 10, 1, 4)))
				.clusterEntitiesList(List.of(new ClusterEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 2, 8)))
				.withLightLevel(15)
				.withTickDelay(16)
				.withClusterSize(8)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.3F, 0.3F, 0.3F))
						.withSkyBrightness(0.0F)
						.withRGB(-0.3F, -0.3F, -0.3F))
				.mobSpawnList(List.of(new SpawnerData(EntityType.DROWNED, 10, 2, 5)))
				.clusterEntitiesList(List.of(new ClusterEntitySpawnData(EntityType.LIGHTNING_BOLT, 0, 3, 4)))
				.withLightLevel(15)
				.withTickDelay(12)
				.withClusterSize(10)
				.setMobCapMultiplier(1.0F)))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "super_storm");
		InvasionType.Builder.invasionType().withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(30).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 12, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 12, 1, 4), new SpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 12, 2, 6), new SpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 12, 3, 8), new SpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 12, 4, 10), new SpawnerData(EntityType.ZOMBIE_VILLAGER, 1, 2, 4)))))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "zombie");
		InvasionType.Builder.invasionType().withRarity(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(24).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 5, 1, 2), new SpawnerData(EntityType.HUSK, 2, 1, 1), new SpawnerData(EntityType.SKELETON, 4, 1, 2), new SpawnerData(EntityType.STRAY, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(20).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 5, 1, 3), new SpawnerData(EntityType.HUSK, 2, 1, 2), new SpawnerData(EntityType.SKELETON, 4, 1, 3), new SpawnerData(EntityType.STRAY, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(16).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 5, 2, 4), new SpawnerData(EntityType.HUSK, 2, 1, 3), new SpawnerData(EntityType.SKELETON, 4, 2, 4), new SpawnerData(EntityType.STRAY, 1, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 5, 2, 5), new SpawnerData(EntityType.HUSK, 2, 2, 3), new SpawnerData(EntityType.SKELETON, 4, 2, 5), new SpawnerData(EntityType.STRAY, 1, 2, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(8).mobSpawnList(List.of(new SpawnerData(EntityType.ZOMBIE, 5, 3, 7), new SpawnerData(EntityType.HUSK, 2, 2, 4), new SpawnerData(EntityType.SKELETON, 4, 3, 6), new SpawnerData(EntityType.STRAY, 1, 2, 4)))))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "undead");
		InvasionType.Builder.invasionType().withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(30).mobSpawnList(List.of(new SpawnerData(EntityType.SPIDER, 3, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(List.of(new SpawnerData(EntityType.SPIDER, 3, 1, 3), new SpawnerData(EntityType.CAVE_SPIDER, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(List.of(new SpawnerData(EntityType.SPIDER, 3, 2, 4), new SpawnerData(EntityType.CAVE_SPIDER, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(List.of(new SpawnerData(EntityType.SPIDER, 3, 2, 5), new SpawnerData(EntityType.CAVE_SPIDER, 1, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(List.of(new SpawnerData(EntityType.SPIDER, 3, 3, 7), new SpawnerData(EntityType.CAVE_SPIDER, 1, 2, 4)))))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "arachnophobia");
		InvasionType.Builder.invasionType().withRarity(5).withTier(2).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.SECONDARY_ONLY).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.1F).withTickDelay(90).mobSpawnList(List.of(new SpawnerData(EntityType.PHANTOM, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(75).mobSpawnList(List.of(new SpawnerData(EntityType.PHANTOM, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.3F).withTickDelay(60).mobSpawnList(List.of(new SpawnerData(EntityType.PHANTOM, 1, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(45).mobSpawnList(List.of(new SpawnerData(EntityType.PHANTOM, 1, 1, 4))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.5F).withTickDelay(30).mobSpawnList(List.of(new SpawnerData(EntityType.PHANTOM, 1, 2, 5)))))
		.dimensions(List.of(Level.OVERWORLD.location(), Level.END.location()))
		.save(consumerIn, "phantom_zone");
		InvasionType.Builder.invasionType().withRarity(3).withTier(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(45).mobSpawnList(List.of(new SpawnerData(EntityType.PILLAGER, 15, 1, 2), new SpawnerData(EntityType.VINDICATOR, 8, 1, 1), new SpawnerData(EntityType.WITCH, 10, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(36).mobSpawnList(List.of(new SpawnerData(EntityType.PILLAGER, 15, 1, 3), new SpawnerData(EntityType.VINDICATOR, 8, 1, 1), new SpawnerData(EntityType.WITCH, 10, 1, 2), new SpawnerData(EntityType.RAVAGER, 3, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(27).mobSpawnList(List.of(new SpawnerData(EntityType.PILLAGER, 15, 2, 4), new SpawnerData(EntityType.VINDICATOR, 8, 1, 2), new SpawnerData(EntityType.WITCH, 10, 1, 3), new SpawnerData(EntityType.RAVAGER, 3, 1, 1), new SpawnerData(EntityType.EVOKER, 5, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(List.of(new SpawnerData(EntityType.PILLAGER, 15, 2, 5), new SpawnerData(EntityType.VINDICATOR, 8, 1, 3), new SpawnerData(EntityType.WITCH, 10, 1, 4), new SpawnerData(EntityType.RAVAGER, 3, 1, 2), new SpawnerData(EntityType.EVOKER, 5, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(9).mobSpawnList(List.of(new SpawnerData(EntityType.PILLAGER, 15, 2, 5), new SpawnerData(EntityType.VINDICATOR, 8, 1, 4), new SpawnerData(EntityType.WITCH, 10, 1, 4), new SpawnerData(EntityType.RAVAGER, 3, 1, 2), new SpawnerData(EntityType.EVOKER, 5, 1, 3), new SpawnerData(EntityType.ILLUSIONER, 1, 1, 1)))))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "mega_raid");
		InvasionType.Builder.invasionType().withRarity(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(32).mobSpawnList(List.of(new SpawnerData(EntityType.SILVERFISH, 3, 1, 3), new SpawnerData(EntityType.ENDERMITE, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(26).mobSpawnList(List.of(new SpawnerData(EntityType.SILVERFISH, 3, 2, 4), new SpawnerData(EntityType.ENDERMITE, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(20).mobSpawnList(List.of(new SpawnerData(EntityType.SILVERFISH, 3, 2, 5), new SpawnerData(EntityType.ENDERMITE, 1, 2, 4))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(14).mobSpawnList(List.of(new SpawnerData(EntityType.SILVERFISH, 3, 3, 6), new SpawnerData(EntityType.ENDERMITE, 1, 2, 5))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(8).mobSpawnList(List.of(new SpawnerData(EntityType.SILVERFISH, 3, 3, 8), new SpawnerData(EntityType.ENDERMITE, 1, 2, 6)))))
		.dimensions(List.of(Level.OVERWORLD.location()))
		.save(consumerIn, "pest");
		//NETHER
		InvasionType.Builder.invasionType().withRarity(14).withTier(2).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo()
				.setMobCapMultiplier(0.6F)
				.withTickDelay(45),
				InvasionType.SeverityInfo.Builder.severityInfo()
				.setMobCapMultiplier(0.8F)
				.withTickDelay(30),
				InvasionType.SeverityInfo.Builder.severityInfo()
				.setMobCapMultiplier(1.0F)
				.withTickDelay(15)))
		.dimensions(List.of(Level.NETHER.location()))
		.save(consumerIn, "nether_again");
		InvasionType.Builder.invasionType().withRarity(10).withTier(1).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(30).mobSpawnList(List.of(new SpawnerData(EntityType.BLAZE, 5, 1, 2), new SpawnerData(EntityType.SKELETON, 6, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(25).mobSpawnList(List.of(new SpawnerData(EntityType.BLAZE, 5, 1, 2), new SpawnerData(EntityType.SKELETON, 6, 1, 3), new SpawnerData(EntityType.MAGMA_CUBE, 4, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(20).mobSpawnList(List.of(new SpawnerData(EntityType.BLAZE, 5, 1, 3), new SpawnerData(EntityType.SKELETON, 6, 1, 4), new SpawnerData(EntityType.MAGMA_CUBE, 4, 1, 2), new SpawnerData(EntityType.WITHER_SKELETON, 2, 1, 1), new SpawnerData(EntityType.GHAST, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(15).mobSpawnList(List.of(new SpawnerData(EntityType.BLAZE, 5, 2, 3), new SpawnerData(EntityType.SKELETON, 6, 2, 4), new SpawnerData(EntityType.MAGMA_CUBE, 4, 1, 2), new SpawnerData(EntityType.WITHER_SKELETON, 2, 1, 2), new SpawnerData(EntityType.GHAST, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(10).mobSpawnList(List.of(new SpawnerData(EntityType.BLAZE, 5, 2, 4), new SpawnerData(EntityType.SKELETON, 6, 2, 5), new SpawnerData(EntityType.MAGMA_CUBE, 4, 1, 3), new SpawnerData(EntityType.WITHER_SKELETON, 2, 1, 2), new SpawnerData(EntityType.GHAST, 1, 1, 3)))))
		.dimensions(List.of(Level.NETHER.location()))
		.save(consumerIn, "blazing_inferno");
		InvasionType.Builder.invasionType().withRarity(8).withTier(1).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(28).mobSpawnList(List.of(new SpawnerData(EntityType.PIGLIN, 7, 1, 1), new SpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 1), new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 2), new SpawnerData(EntityType.HOGLIN, 4, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(List.of(new SpawnerData(EntityType.PIGLIN, 7, 1, 2), new SpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 1), new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 2), new SpawnerData(EntityType.HOGLIN, 4, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(20).mobSpawnList(List.of(new SpawnerData(EntityType.PIGLIN, 7, 1, 2), new SpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 1), new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 3), new SpawnerData(EntityType.HOGLIN, 4, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(16).mobSpawnList(List.of(new SpawnerData(EntityType.PIGLIN, 7, 1, 3), new SpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 2), new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 3), new SpawnerData(EntityType.HOGLIN, 4, 1, 2), new SpawnerData(EntityType.ZOGLIN, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(12).mobSpawnList(List.of(new SpawnerData(EntityType.PIGLIN, 7, 1, 3), new SpawnerData(EntityType.PIGLIN_BRUTE, 2, 1, 2), new SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 1, 4), new SpawnerData(EntityType.HOGLIN, 4, 1, 3), new SpawnerData(EntityType.ZOGLIN, 1, 1, 2)))))
		.dimensions(List.of(Level.NETHER.location()))
		.save(consumerIn, "pigs_galore");
		//END
		InvasionType.Builder.invasionType().withRarity(14).withTier(3).withInvasionTime(InvasionTime.BOTH).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_MIXED).severityInfo(List.of(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.15F, 0.15F, 0.1F))
						.withSkyBrightness(0.2F)
						.withRGB(-0.85F, -0.85F, -0.85F))
				.withLightLevel(15)
				.withClusterSize(3)
				.withTickDelay(60)
				.setMobCapMultiplier(0.6F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.1F, 0.1F, 0.1F))
						.withSkyBrightness(0.1F)
						.withRGB(-0.90F, -0.90F, -0.90F))
				.withLightLevel(15)
				.withClusterSize(3)
				.withTickDelay(40)
				.setMobCapMultiplier(0.8F),
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.05F, 0.05F, 0.05F))
						.withSkyBrightness(0.0F)
						.withRGB(-0.95F, -0.95F, -0.95F))
				.withLightLevel(15)
				.withClusterSize(3)
				.withTickDelay(20)
				.setMobCapMultiplier(1.0F)))
		.dimensions(List.of(Level.END.location()))
		.save(consumerIn, "end_game");
	}
}
