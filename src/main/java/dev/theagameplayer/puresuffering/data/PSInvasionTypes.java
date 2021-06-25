package dev.theagameplayer.puresuffering.data;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;

public final class PSInvasionTypes implements Consumer<Consumer<InvasionType>> {
	@Override
	public void accept(Consumer<InvasionType> consumerIn) {
		InvasionType.Builder.invasionType().skyRenderer(ImmutableMap.of(
				1, InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().withRGB(0.1F, 0.1F, 0.1F))
				.sunTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/solar_eclipse_sun.png")).withRGB(-0.9F, -0.9F, -0.9F)
				)).mobSpawnList(ImmutableMap.of()).setDayTimeEvent().setForcesNoSleep().setToNightEvents().withLight(0.3F, 15).setNonRepeatable().maxSeverity(1).tickDelay(12).withRarity(7).save(consumerIn, "solar_eclipse");
		InvasionType.Builder.invasionType().skyRenderer(ImmutableMap.of(
				1, InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().density(0.085F).withRGB(0.05F, 0, 0))
				.moonTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/lunar_eclipse_moon.png")).weatherVisibility(0.1F)
				)).mobSpawnList(ImmutableMap.of()).setNonRepeatable().setOnlyDuringNight().maxSeverity(1).tickDelay(9).withRarity(7).save(consumerIn, "lunar_eclipse");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 4), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 4), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 1, 1)),
				3, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 6), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 1, 2)),
				4, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 3, 8), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 1, 3)),
				5, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 4, 10), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 2, 5))
				)).maxSeverity(5).save(consumerIn, "zombie");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 4), new Spawners(EntityType.HUSK, 10, 1, 3), new Spawners(EntityType.SKELETON, 15, 1, 3), new Spawners(EntityType.STRAY, 8, 1, 2)),
				2, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 4), new Spawners(EntityType.HUSK, 10, 1, 3), new Spawners(EntityType.SKELETON, 15, 1, 4), new Spawners(EntityType.STRAY, 8, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 6), new Spawners(EntityType.HUSK, 10, 2, 4), new Spawners(EntityType.SKELETON, 15, 2, 5), new Spawners(EntityType.STRAY, 8, 1, 3)),
				4, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 3, 8), new Spawners(EntityType.HUSK, 10, 2, 5), new Spawners(EntityType.SKELETON, 15, 2, 6), new Spawners(EntityType.STRAY, 8, 2, 4)),
				5, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 4, 10), new Spawners(EntityType.HUSK, 10, 3, 7), new Spawners(EntityType.SKELETON, 15, 3, 8), new Spawners(EntityType.STRAY, 8, 2, 6))
				)).maxSeverity(5).save(consumerIn, "undead");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.SPIDER, 20, 1, 2), new Spawners(EntityType.CAVE_SPIDER, 10, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.SPIDER, 20, 1, 3), new Spawners(EntityType.CAVE_SPIDER, 10, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.SPIDER, 20, 2, 4), new Spawners(EntityType.CAVE_SPIDER, 10, 1, 3)),
				4, ImmutableList.of(new Spawners(EntityType.SPIDER, 20, 2, 5), new Spawners(EntityType.CAVE_SPIDER, 10, 2, 4)),
				5, ImmutableList.of(new Spawners(EntityType.SPIDER, 20, 3, 7), new Spawners(EntityType.CAVE_SPIDER, 10, 2, 5))
				)).maxSeverity(5).save(consumerIn, "anthropod");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.PHANTOM, 20, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.PHANTOM, 20, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.PHANTOM, 20, 1, 3)),
				4, ImmutableList.of(new Spawners(EntityType.PHANTOM, 20, 2, 4)),
				5, ImmutableList.of(new Spawners(EntityType.PHANTOM, 20, 2, 5))
				)).maxSeverity(5).withRarity(3).save(consumerIn, "phantom_zone");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 1, 2), new Spawners(EntityType.VINDICATOR, 10, 1, 1), new Spawners(EntityType.EVOKER, 5, 1, 1), new Spawners(EntityType.VEX, 15, 1, 2), new Spawners(EntityType.WITCH, 10, 1, 1), new Spawners(EntityType.RAVAGER, 3, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 1, 3), new Spawners(EntityType.VINDICATOR, 10, 1, 1), new Spawners(EntityType.EVOKER, 5, 1, 1), new Spawners(EntityType.VEX, 15, 1, 3), new Spawners(EntityType.WITCH, 10, 1, 2), new Spawners(EntityType.RAVAGER, 3, 1, 1)),
				3, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 2, 4), new Spawners(EntityType.VINDICATOR, 10, 1, 2), new Spawners(EntityType.EVOKER, 5, 1, 1), new Spawners(EntityType.VEX, 15, 1, 5), new Spawners(EntityType.WITCH, 10, 1, 3), new Spawners(EntityType.RAVAGER, 3, 1, 1)),
				4, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 3, 6), new Spawners(EntityType.VINDICATOR, 10, 1, 3), new Spawners(EntityType.EVOKER, 5, 1, 2), new Spawners(EntityType.VEX, 15, 1, 6), new Spawners(EntityType.WITCH, 10, 1, 4), new Spawners(EntityType.RAVAGER, 3, 1, 2)),
				5, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 4, 8), new Spawners(EntityType.VINDICATOR, 10, 2, 5), new Spawners(EntityType.EVOKER, 5, 1, 2), new Spawners(EntityType.VEX, 15, 1, 8), new Spawners(EntityType.WITCH, 10, 1, 5), new Spawners(EntityType.RAVAGER, 3, 1, 2))
				)).maxSeverity(5).withRarity(2).save(consumerIn, "mega_raid");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 1, 4), new Spawners(EntityType.ENDERMITE, 5, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 2, 4), new Spawners(EntityType.ENDERMITE, 5, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 2, 6), new Spawners(EntityType.ENDERMITE, 5, 2, 5)),
				4, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 3, 8), new Spawners(EntityType.ENDERMITE, 5, 2, 6)),
				5, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 4, 10), new Spawners(EntityType.ENDERMITE, 5, 3, 8))
				)).maxSeverity(5).withRarity(1).save(consumerIn, "pest");
	}
}
