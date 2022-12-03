package dev.theagameplayer.puresuffering.world;

import dev.theagameplayer.puresuffering.spawner.TimedInvasionSpawner;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class TimedInvasionWorldData extends InvasionWorldData {
	private final TimedInvasionSpawner spawner = new TimedInvasionSpawner();
	private double dayXPMultiplier, nightXPMultiplier;
	private boolean checkedDay, checkedNight;
	private boolean prevDayCheck, prevNightCheck;
	
	public TimedInvasionWorldData(final ServerLevel levelIn) {
		super(levelIn);
		this.checkedDay = ServerTimeUtil.isServerNight(levelIn, this);
		this.checkedNight = ServerTimeUtil.isServerDay(levelIn, this);
	}
	
	public static final TimedInvasionWorldData load(final ServerLevel levelIn, final CompoundTag nbtIn) {
		final TimedInvasionWorldData tiwData = new TimedInvasionWorldData(levelIn);
		tiwData.spawner.load(nbtIn.getCompound("Spawner"));
		tiwData.dayXPMultiplier = nbtIn.getDouble("DayXPMultiplier");
		tiwData.nightXPMultiplier = nbtIn.getDouble("NightXPMultiplier");
		tiwData.checkedDay = nbtIn.getBoolean("CheckedDay");
		tiwData.checkedNight = nbtIn.getBoolean("CheckedNight");
		tiwData.prevDayCheck = nbtIn.getBoolean("PrevDayCheck");
		tiwData.prevNightCheck = nbtIn.getBoolean("PrevNightCheck");
		tiwData.days = nbtIn.getLong("Days");
		return tiwData;
	}

	@Override
	public final CompoundTag save(final CompoundTag nbtIn) {
		nbtIn.put("Spawner", this.spawner.save());
		nbtIn.putDouble("DayXPMultiplier", this.dayXPMultiplier);
		nbtIn.putDouble("NightXPMultiplier", this.nightXPMultiplier);
		nbtIn.putBoolean("CheckedDay", this.checkedDay);
		nbtIn.putBoolean("CheckedNight", this.checkedNight);
		nbtIn.putBoolean("PrevDayCheck", this.prevDayCheck);
		nbtIn.putBoolean("PrevNightCheck", this.prevNightCheck);
		return super.save(nbtIn);
	}
	
	public final TimedInvasionSpawner getInvasionSpawner() {
		return this.spawner;
	}
	
	public final double getDayXPMultiplier() {
		return this.dayXPMultiplier;
	}
	
	public final void setDayXPMultiplier(final double dayXPMultiplierIn) {
		this.dayXPMultiplier = dayXPMultiplierIn;
		this.setDirty();
	}
	
	public final double getNightXPMultiplier() {
		return this.nightXPMultiplier;
	}
	
	public final void setNightXPMultiplier(final double nightXPMultiplierIn) {
		this.nightXPMultiplier = nightXPMultiplierIn;
		this.setDirty();
	}
	
	public final boolean hasCheckedDay() {
		return this.checkedDay;
	}
	
	public final void setCheckedDay(final boolean checkedDayIn) {
		this.checkedDay = checkedDayIn;
		this.setDirty();
	}
	
	public final boolean hasCheckedNight() {
		return this.checkedNight;
	}
	
	public final void setCheckedNight(final boolean checkedNightIn) {
		this.checkedNight = checkedNightIn;
		this.setDirty();
	}
	
	public final boolean getPrevDayCheck() {
		return this.prevDayCheck;
	}
	
	public final void setPrevDayCheck(final boolean prevDayCheckIn) {
		this.prevDayCheck = prevDayCheckIn;
		this.setDirty();
	}
	
	public final boolean getPrevNightCheck() {
		return this.prevNightCheck;
	}
	
	public final void setPrevNightCheck(final boolean prevNightCheckIn) {
		this.prevNightCheck = prevNightCheckIn;
		this.setDirty();
	}
}
