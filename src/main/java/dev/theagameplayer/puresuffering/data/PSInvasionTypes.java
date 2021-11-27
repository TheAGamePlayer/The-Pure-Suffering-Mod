package dev.theagameplayer.puresuffering.data;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeChangeability;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;

public final class PSInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public void accept(Consumer<InvasionType> consumerIn) {
		InvasionType.Builder.invasionType().withRarity(7).withTier(1).withInvasionTime(InvasionTime.DAY).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.DAY_TO_NIGHT).withTimeChangeability(TimeChangeability.ONLY_DAY).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.1F, 0.1F, 0.1F))
						.sunTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/solar_eclipse_sun.png"))
						.withSkyBrightness(0.1F)
						.withRGB(-0.9F, -0.9F, -0.9F))
				.setForcesNoSleep()
				.withLightLevel(15)
				.withTickDelay(18)))
		.save(consumerIn, "solar_eclipse");
		InvasionType.Builder.invasionType().withRarity(7).withTier(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.PRIMARY_ONLY).withSpawningSystem(SpawningSystem.BIOME_BOOSTED).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.ONLY_NIGHT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().skyRenderer(
						InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.05F, 0, 0))
						.moonTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/lunar_eclipse_moon.png"))
						.weatherVisibility(0.1F))
				.withTickDelay(15)))
		.save(consumerIn, "lunar_eclipse");
		InvasionType.Builder.invasionType().withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.DEFAULT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(30).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 4), new Spawners(EntityType.ZOMBIE_VILLAGER, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 6), new Spawners(EntityType.ZOMBIE_VILLAGER, 3, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 3, 8), new Spawners(EntityType.ZOMBIE_VILLAGER, 4, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 4, 10), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 2, 4)))))
		.save(consumerIn, "zombie");
		InvasionType.Builder.invasionType().withRarity(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.DEFAULT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(24).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 2), new Spawners(EntityType.HUSK, 7, 1, 1), new Spawners(EntityType.SKELETON, 15, 1, 2), new Spawners(EntityType.STRAY, 5, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(20).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 3), new Spawners(EntityType.HUSK, 8, 1, 2), new Spawners(EntityType.SKELETON, 15, 1, 3), new Spawners(EntityType.STRAY, 6, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(16).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 4), new Spawners(EntityType.HUSK, 9, 1, 3), new Spawners(EntityType.SKELETON, 15, 2, 4), new Spawners(EntityType.STRAY, 7, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 5), new Spawners(EntityType.HUSK, 9, 2, 3), new Spawners(EntityType.SKELETON, 15, 2, 5), new Spawners(EntityType.STRAY, 7, 2, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(8).mobSpawnList(ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 3, 7), new Spawners(EntityType.HUSK, 10, 2, 4), new Spawners(EntityType.SKELETON, 15, 3, 6), new Spawners(EntityType.STRAY, 8, 2, 4)))))
		.save(consumerIn, "undead");
		InvasionType.Builder.invasionType().withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.DEFAULT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(30).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SPIDER, 6, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.7F).withTickDelay(24).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SPIDER, 6, 1, 3), new Spawners(EntityType.CAVE_SPIDER, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SPIDER, 6, 2, 4), new Spawners(EntityType.CAVE_SPIDER, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.9F).withTickDelay(12).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SPIDER, 6, 2, 5), new Spawners(EntityType.CAVE_SPIDER, 2, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(6).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SPIDER, 6, 3, 7), new Spawners(EntityType.CAVE_SPIDER, 2, 2, 4)))))
		.save(consumerIn, "arachnophobia");
		InvasionType.Builder.invasionType().withRarity(5).withTier(2).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.DEFAULT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.1F).withTickDelay(90).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(75).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.3F).withTickDelay(60).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 3))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(45).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 4))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.5F).withTickDelay(30).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 2, 5)))))
		.save(consumerIn, "phantom_zone");
		InvasionType.Builder.invasionType().withRarity(3).withTier(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.DEFAULT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(45).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PILLAGER, 10, 1, 2), new Spawners(EntityType.VINDICATOR, 10, 1, 1), new Spawners(EntityType.WITCH, 10, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(36).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PILLAGER, 10, 1, 3), new Spawners(EntityType.VINDICATOR, 11, 1, 1), new Spawners(EntityType.WITCH, 10, 1, 2), new Spawners(EntityType.RAVAGER, 2, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(27).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PILLAGER, 10, 2, 4), new Spawners(EntityType.VINDICATOR, 12, 1, 2), new Spawners(EntityType.WITCH, 10, 1, 3), new Spawners(EntityType.RAVAGER, 3, 1, 1), new Spawners(EntityType.EVOKER, 4, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(18).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PILLAGER, 10, 2, 5), new Spawners(EntityType.VINDICATOR, 13, 1, 3), new Spawners(EntityType.WITCH, 10, 1, 4), new Spawners(EntityType.RAVAGER, 4, 1, 2), new Spawners(EntityType.EVOKER, 5, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(9).mobSpawnList(ImmutableList.of(new Spawners(EntityType.PILLAGER, 10, 2, 5), new Spawners(EntityType.VINDICATOR, 15, 1, 4), new Spawners(EntityType.WITCH, 10, 1, 4), new Spawners(EntityType.RAVAGER, 4, 1, 2), new Spawners(EntityType.EVOKER, 6, 1, 3), new Spawners(EntityType.ILLUSIONER, 1, 1, 1)))))
		.save(consumerIn, "mega_raid");
		InvasionType.Builder.invasionType().withRarity(1).withInvasionTime(InvasionTime.NIGHT).withInvasionPriority(InvasionPriority.BOTH).withSpawningSystem(SpawningSystem.DEFAULT).withTimeModifier(TimeModifier.NONE).withTimeChangeability(TimeChangeability.DEFAULT).severityInfo(ImmutableList.of(
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.2F).withTickDelay(32).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SILVERFISH, 10, 1, 3), new Spawners(EntityType.ENDERMITE, 8, 1, 1))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.4F).withTickDelay(26).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SILVERFISH, 12, 2, 4), new Spawners(EntityType.ENDERMITE, 8, 1, 2))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.6F).withTickDelay(20).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SILVERFISH, 15, 2, 5), new Spawners(EntityType.ENDERMITE, 9, 2, 4))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(0.8F).withTickDelay(14).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SILVERFISH, 18, 3, 6), new Spawners(EntityType.ENDERMITE, 9, 2, 5))),
				InvasionType.SeverityInfo.Builder.severityInfo().setMobCapMultiplier(1.0F).withTickDelay(8).mobSpawnList(ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 3, 8), new Spawners(EntityType.ENDERMITE, 10, 2, 6)))))
		.save(consumerIn, "pest");
	}
}
