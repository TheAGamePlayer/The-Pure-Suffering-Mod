package dev.theagameplayer.puresuffering.registries.other;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.SpawnPlacementRegisterEvent;

public final class PSEntityPlacements {
	public static final void registerSpawnPlacements(final SpawnPlacementRegisterEvent pEvent) {
		//Piglin Brutes & Zoglins have no spawn logic by default
		pEvent.register(EntityType.PIGLIN_BRUTE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (et, level, mobSpawnType, pos, random) -> {
			return !level.getBlockState(pos.below()).is(Blocks.NETHER_WART_BLOCK);
		}, SpawnPlacementRegisterEvent.Operation.OR);
		pEvent.register(EntityType.ZOGLIN, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (et, level, mobSpawnType, pos, random) -> {
			return !level.getBlockState(pos.below()).is(Blocks.NETHER_WART_BLOCK);
		}, SpawnPlacementRegisterEvent.Operation.OR);
	}
}
