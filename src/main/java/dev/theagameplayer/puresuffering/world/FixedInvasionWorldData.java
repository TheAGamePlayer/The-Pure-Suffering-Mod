package dev.theagameplayer.puresuffering.world;

import dev.theagameplayer.puresuffering.spawner.FixedInvasionSpawner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class FixedInvasionWorldData extends InvasionWorldData {
	private final FixedInvasionSpawner spawner = new FixedInvasionSpawner();
	private boolean requiresUpdate, isFirstCycle;
	private double xpMultiplier;

	public FixedInvasionWorldData(final ServerLevel levelIn) {
		super(levelIn);
	}

	public static final FixedInvasionWorldData load(final ServerLevel levelIn, final CompoundTag nbtIn) {
		final FixedInvasionWorldData fiwData = new FixedInvasionWorldData(levelIn);
		fiwData.spawner.load(nbtIn.getCompound("Spawner"));
		fiwData.isFirstCycle = nbtIn.getBoolean("IsFirstCycle");
		fiwData.xpMultiplier = nbtIn.getDouble("XPMultiplier");
		fiwData.days = nbtIn.getLong("Days");
		return fiwData;
	}

	@Override
	public final CompoundTag save(final CompoundTag nbtIn) {
		nbtIn.put("Spawner", this.spawner.save());
		nbtIn.putBoolean("IsFirstCycle", this.isFirstCycle);
		nbtIn.putDouble("XPMultiplier", this.xpMultiplier);
		return super.save(nbtIn);
	}
	
	public final FixedInvasionSpawner getInvasionSpawner() {
		return this.spawner;
	}
	
	public final boolean requiresUpdate() {
		return this.requiresUpdate;
	}
	
	public final void setUpdateRequired(final boolean valueIn) {
		this.requiresUpdate = valueIn;
	}
	
	public final boolean isFirstCycle() {
		return this.isFirstCycle;
	}
	
	public final void setFirstCycle(final boolean isFirstCycleIn) {
		this.isFirstCycle = isFirstCycleIn;
		this.setDirty();
	}
	
	public final double getXPMultiplier() {
		return this.xpMultiplier;
	}
	
	public final void setXPMultiplier(final double xpMultiplierIn) {
		this.xpMultiplier = xpMultiplierIn;
		this.setDirty();
	}
}
