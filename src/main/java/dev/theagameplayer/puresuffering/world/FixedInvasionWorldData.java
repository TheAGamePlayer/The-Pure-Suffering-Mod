package dev.theagameplayer.puresuffering.world;

import dev.theagameplayer.puresuffering.spawner.FixedInvasionSpawner;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;

public final class FixedInvasionWorldData extends InvasionWorldData {
	private final FixedInvasionSpawner spawner = new FixedInvasionSpawner();
	private boolean isFirstCycle;
	private double xpMultiplier;

	public FixedInvasionWorldData(ServerWorld worldIn) {
		super(worldIn);
	}

	@Override
	public void load(CompoundNBT nbtIn) {
		this.spawner.load(nbtIn.getCompound("Spawner"));
		this.isFirstCycle = nbtIn.getBoolean("IsFirstCycle");
		this.xpMultiplier = nbtIn.getDouble("XPMultiplier");
		super.load(nbtIn);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbtIn) {
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
