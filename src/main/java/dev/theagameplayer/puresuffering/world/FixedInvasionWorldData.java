package dev.theagameplayer.puresuffering.world;

import dev.theagameplayer.puresuffering.spawner.FixedInvasionSpawner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class FixedInvasionWorldData extends InvasionWorldData {
	private final FixedInvasionSpawner spawner = new FixedInvasionSpawner();
	private boolean isFirstCycle;
	private double xpMultiplier;

	public FixedInvasionWorldData(ServerLevel worldIn) {
		super(worldIn);
	}

	public static FixedInvasionWorldData load(ServerLevel worldIn, CompoundTag nbtIn) {
		FixedInvasionWorldData fiwData = new FixedInvasionWorldData(worldIn);
		fiwData.spawner.load(nbtIn.getCompound("Spawner"));
		fiwData.isFirstCycle = nbtIn.getBoolean("IsFirstCycle");
		fiwData.xpMultiplier = nbtIn.getDouble("XPMultiplier");
		fiwData.days = nbtIn.getLong("Days");
		return fiwData;
	}

	@Override
	public CompoundTag save(CompoundTag nbtIn) {
		nbtIn.put("Spawner", this.spawner.save());
		nbtIn.putBoolean("IsFirstCycle", this.isFirstCycle);
		nbtIn.putDouble("XPMultiplier", this.xpMultiplier);
		return super.save(nbtIn);
	}
	
	public FixedInvasionSpawner getInvasionSpawner() {
		return this.spawner;
	}
	
	public boolean isFirstCycle() {
		return this.isFirstCycle;
	}
	
	public void setFirstCycle(boolean isFirstCycleIn) {
		this.isFirstCycle = isFirstCycleIn;
		this.setDirty();
	}
	
	public double getXPMultiplier() {
		return this.xpMultiplier;
	}
	
	public void setXPMultiplier(double xpMultiplierIn) {
		this.xpMultiplier = xpMultiplierIn;
		this.setDirty();
	}
}
