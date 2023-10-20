package dev.theagameplayer.puresuffering.invasion.data;

import net.minecraft.world.entity.EntityType;

public final class AdditionalEntitySpawnData {
	private final EntityType<?> type;
	private final int minCount, maxCount;
	private final int chance;
	private final boolean isSurfaceSpawn;

	public AdditionalEntitySpawnData(final EntityType<?> entityTypeIn, final int minCountIn, final int maxCountIn, final int chanceIn, final boolean isSurfaceSpawnIn) {
		this.type = entityTypeIn;
		this.minCount = minCountIn;
		this.maxCount = maxCountIn;
		this.chance = chanceIn;
		this.isSurfaceSpawn = isSurfaceSpawnIn;
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
