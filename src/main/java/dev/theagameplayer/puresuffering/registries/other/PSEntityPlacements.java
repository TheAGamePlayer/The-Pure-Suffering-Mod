package dev.theagameplayer.puresuffering.registries.other;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

public final class PSEntityPlacements {
	public static final void registerSpawnPlacements(final SpawnPlacementRegisterEvent eventIn) {
		//Piglin Brutes & Zoglins have no spawn logic by default
		eventIn.register(EntityType.PIGLIN_BRUTE, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (et, level, mobSpawnType, pos, random) -> {
			return !level.getBlockState(pos.below()).is(Blocks.NETHER_WART_BLOCK);
		}, SpawnPlacementRegisterEvent.Operation.OR);
		eventIn.register(EntityType.ZOGLIN, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (et, level, mobSpawnType, pos, random) -> {
			return !level.getBlockState(pos.below()).is(Blocks.NETHER_WART_BLOCK);
		}, SpawnPlacementRegisterEvent.Operation.OR);
	}
}
