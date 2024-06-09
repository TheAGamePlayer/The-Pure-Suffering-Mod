package dev.theagameplayer.puresuffering.invasion.data;

import net.minecraft.world.entity.EntityType;

public final class AdditionalEntitySpawnData {
	private final EntityType<?> type;
	private final int minCount, maxCount;
	private final int chance;
	private final boolean isSurfaceSpawn;

	public AdditionalEntitySpawnData(final EntityType<?> pEntityType, final int pMinCount, final int pMaxCount, final int pChance, final boolean pIsSurfaceSpawn) {
		this.type = pEntityType;
		this.minCount = pMinCount;
		this.maxCount = pMaxCount;
		this.chance = pChance;
		this.isSurfaceSpawn = pIsSurfaceSpawn;
	}

	public final EntityType<?> getEntityType() {
		return this.type;
	}

	public final int getMinCount() {
		return this.minCount;
	}

	public final int getMaxCount() {
		return this.maxCount;
	}

	public final int getChance() {
		return this.chance;
	}
	
	public final boolean isSurfaceSpawn() {
		return this.isSurfaceSpawn;
	}
}
