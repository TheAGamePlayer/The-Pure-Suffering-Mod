package dev.theagameplayer.puresuffering.invasion;

import net.minecraft.world.entity.EntityType;

public final class ClusterEntitySpawnData {
	private final EntityType<?> entityType;
	private final int minCount, maxCount;
	private final int chance;
	
	public ClusterEntitySpawnData(final EntityType<?> entityTypeIn, final int minCountIn, final int maxCountIn, final int chanceIn) {
		this.entityType = entityTypeIn;
		this.minCount = minCountIn;
		this.maxCount = maxCountIn;
		this.chance = chanceIn;
	}
	
	public final EntityType<?> getEntityType() {
		return this.entityType;
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
}
