package dev.theagameplayer.puresuffering.world;

import dev.theagameplayer.puresuffering.spawner.TimedInvasionSpawner;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;

public final class TimedInvasionWorldData extends InvasionWorldData {
	private final TimedInvasionSpawner spawner = new TimedInvasionSpawner();
	private double dayXPMultiplier, nightXPMultiplier;
	private boolean checkedDay, checkedNight;
	private boolean prevDayCheck, prevNightCheck;
	
	public TimedInvasionWorldData(ServerWorld worldIn) {
		super(worldIn);
		this.checkedDay = ServerTimeUtil.isServerNight(worldIn, this);
		this.checkedNight = ServerTimeUtil.isServerDay(worldIn, this);
	}
	
	@Override
	public void load(CompoundNBT nbtIn) {
		this.spawner.load(nbtIn.getCompound("Spawner"));
		this.dayXPMultiplier = nbtIn.getDouble("DayXPMultiplier");
		this.nightXPMultiplier = nbtIn.getDouble("NightXPMultiplier");
		this.checkedDay = nbtIn.getBoolean("CheckedDay");
		this.checkedNight = nbtIn.getBoolean("CheckedNight");
		this.prevDayCheck = nbtIn.getBoolean("PrevDayCheck");
		this.prevNightCheck = nbtIn.getBoolean("PrevNightCheck");
		super.load(nbtIn);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbtIn) {
		nbtIn.put("Spawner", this.spawner.save());
		nbtIn.putDouble("DayXPMultiplier", this.dayXPMultiplier);
		nbtIn.putDouble("NightXPMultiplier", this.nightXPMultiplier);
		nbtIn.putBoolean("CheckedDay", this.checkedDay);
		nbtIn.putBoolean("CheckedNight", this.checkedNight);
		nbtIn.putBoolean("PrevDayCheck", this.prevDayCheck);
		nbtIn.putBoolean("PrevNightCheck", this.prevNightCheck);
		return super.save(nbtIn);
	}
	
	public TimedInvasionSpawner getInvasionSpawner() {
		return this.spawner;
	}
	
	public double getDayXPMultiplier() {
		return this.dayXPMultiplier;
	}
	
	public void setDayXPMultiplier(double dayXPMultiplierIn) {
		this.dayXPMultiplier = dayXPMultiplierIn;
		this.setDirty();
	}
	
	public double getNightXPMultiplier() {
		return this.nightXPMultiplier;
	}
	
	public void setNightXPMultiplier(double nightXPMultiplierIn) {
		this.nightXPMultiplier = nightXPMultiplierIn;
		this.setDirty();
	}
	
	public boolean hasCheckedDay() {
		return this.checkedDay;
	}
	
	public void setCheckedDay(boolean checkedDayIn) {
		this.checkedDay = checkedDayIn;
		this.setDirty();
	}
	
	public boolean hasCheckedNight() {
		return this.checkedNight;
	}
	
	public void setCheckedNight(boolean checkedNightIn) {
		this.checkedNight = checkedNightIn;
		this.setDirty();
	}
	
	public boolean getPrevDayCheck() {
		return this.prevDayCheck;
	}
	
	public void setPrevDayCheck(boolean prevDayCheckIn) {
		this.prevDayCheck = prevDayCheckIn;
		this.setDirty();
	}
	
	public boolean getPrevNightCheck() {
		return this.prevNightCheck;
	}
	
	public void setPrevNightCheck(boolean prevNightCheckIn) {
		this.prevNightCheck = prevNightCheckIn;
		this.setDirty();
	}
}
