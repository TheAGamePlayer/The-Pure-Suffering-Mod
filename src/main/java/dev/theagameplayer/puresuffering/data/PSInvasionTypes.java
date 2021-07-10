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
				.sunTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/solar_eclipse_sun.png")).withSkyBrightness(0.1F).withRGB(-0.9F, -0.9F, -0.9F)
				)).mobSpawnList(ImmutableMap.of()).setDayTimeEvent().setForcesNoSleep().setToNightEvents().withLight(15).setNonRepeatable().tickDelay(18).withRarity(7).save(consumerIn, "solar_eclipse");
		InvasionType.Builder.invasionType().skyRenderer(ImmutableMap.of(
				1, InvasionSkyRenderer.Builder.skyRenderer().withFog(InvasionFogRenderer.Builder.fogRenderer().density(0.085F).withRGB(0.05F, 0, 0))
				.moonTexture(new ResourceLocation(PureSufferingMod.MODID, "textures/environment/lunar_eclipse_moon.png")).weatherVisibility(0.1F)
				)).mobSpawnList(ImmutableMap.of()).setNonRepeatable().setOnlyDuringNight().tickDelay(15).withRarity(7).save(consumerIn, "lunar_eclipse");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 4), new Spawners(EntityType.ZOMBIE_VILLAGER, 2, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 4), new Spawners(EntityType.ZOMBIE_VILLAGER, 3, 1, 1)),
				3, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 6), new Spawners(EntityType.ZOMBIE_VILLAGER, 3, 1, 2)),
				4, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 3, 8), new Spawners(EntityType.ZOMBIE_VILLAGER, 4, 1, 3)),
				5, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 4, 10), new Spawners(EntityType.ZOMBIE_VILLAGER, 5, 2, 5))
				)).save(consumerIn, "zombie");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 1, 4), new Spawners(EntityType.HUSK, 7, 1, 2), new Spawners(EntityType.SKELETON, 15, 1, 3), new Spawners(EntityType.STRAY, 5, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 5), new Spawners(EntityType.HUSK, 8, 1, 3), new Spawners(EntityType.SKELETON, 15, 1, 4), new Spawners(EntityType.STRAY, 6, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 2, 6), new Spawners(EntityType.HUSK, 9, 2, 4), new Spawners(EntityType.SKELETON, 15, 2, 5), new Spawners(EntityType.STRAY, 7, 1, 3)),
				4, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 3, 8), new Spawners(EntityType.HUSK, 9, 2, 5), new Spawners(EntityType.SKELETON, 15, 2, 6), new Spawners(EntityType.STRAY, 7, 2, 4)),
				5, ImmutableList.of(new Spawners(EntityType.ZOMBIE, 20, 4, 10), new Spawners(EntityType.HUSK, 10, 3, 7), new Spawners(EntityType.SKELETON, 15, 3, 8), new Spawners(EntityType.STRAY, 8, 2, 6))
				)).withRarity(1).save(consumerIn, "undead");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.SPIDER, 3, 1, 2)),
				2, ImmutableList.of(new Spawners(EntityType.SPIDER, 3, 1, 3), new Spawners(EntityType.CAVE_SPIDER, 1, 1, 1)),
				3, ImmutableList.of(new Spawners(EntityType.SPIDER, 3, 2, 4), new Spawners(EntityType.CAVE_SPIDER, 1, 1, 2)),
				4, ImmutableList.of(new Spawners(EntityType.SPIDER, 3, 2, 5), new Spawners(EntityType.CAVE_SPIDER, 2, 1, 3)),
				5, ImmutableList.of(new Spawners(EntityType.SPIDER, 3, 3, 7), new Spawners(EntityType.CAVE_SPIDER, 2, 2, 4))
				)).save(consumerIn, "arachnophobia");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 1, 3)),
				4, ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 2, 4)),
				5, ImmutableList.of(new Spawners(EntityType.PHANTOM, 1, 2, 5))
				)).tickDelay(30).withRarity(5).save(consumerIn, "phantom_zone");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 1, 2), new Spawners(EntityType.VINDICATOR, 10, 1, 1), new Spawners(EntityType.EVOKER, 5, 1, 1), new Spawners(EntityType.WITCH, 10, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 1, 3), new Spawners(EntityType.VINDICATOR, 11, 1, 1), new Spawners(EntityType.EVOKER, 5, 1, 1), new Spawners(EntityType.WITCH, 10, 1, 2), new Spawners(EntityType.RAVAGER, 3, 1, 1)),
				3, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 2, 4), new Spawners(EntityType.VINDICATOR, 12, 1, 2), new Spawners(EntityType.EVOKER, 6, 1, 2), new Spawners(EntityType.VEX, 10, 1, 2), new Spawners(EntityType.WITCH, 10, 1, 3), new Spawners(EntityType.RAVAGER, 3, 1, 1)),
				4, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 3, 6), new Spawners(EntityType.VINDICATOR, 13, 1, 3), new Spawners(EntityType.EVOKER, 6, 1, 2), new Spawners(EntityType.VEX, 12, 1, 3), new Spawners(EntityType.WITCH, 10, 1, 4), new Spawners(EntityType.RAVAGER, 4, 1, 2)),
				5, ImmutableList.of(new Spawners(EntityType.PILLAGER, 20, 4, 8), new Spawners(EntityType.VINDICATOR, 15, 2, 5), new Spawners(EntityType.EVOKER, 7, 1, 3), new Spawners(EntityType.VEX, 15, 1, 5), new Spawners(EntityType.WITCH, 10, 1, 5), new Spawners(EntityType.RAVAGER, 5, 1, 2), new Spawners(EntityType.ILLUSIONER, 1, 1, 1))
				)).tickDelay(8).withRarity(3).save(consumerIn, "mega_raid");
		InvasionType.Builder.invasionType().mobSpawnList(ImmutableMap.of(
				1, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 10, 1, 4), new Spawners(EntityType.ENDERMITE, 5, 1, 1)),
				2, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 12, 2, 4), new Spawners(EntityType.ENDERMITE, 6, 1, 2)),
				3, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 15, 2, 6), new Spawners(EntityType.ENDERMITE, 6, 2, 5)),
				4, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 18, 3, 8), new Spawners(EntityType.ENDERMITE, 7, 2, 6)),
				5, ImmutableList.of(new Spawners(EntityType.SILVERFISH, 20, 4, 10), new Spawners(EntityType.ENDERMITE, 8, 3, 8))
				)).withRarity(1).save(consumerIn, "pest");
	}
}
