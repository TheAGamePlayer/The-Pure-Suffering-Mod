package dev.theagameplayer.puresuffering.world;

import java.util.HashMap;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public abstract class InvasionWorldData extends SavedData {
	private static final HashMap<ServerLevel, InvasionWorldData> INVASION_DATA = new HashMap<>();
	private final boolean hasFixedTime;
	private final ServerLevel level;
	protected long days;
	
	public InvasionWorldData(ServerLevel worldIn) {
		this.hasFixedTime = worldIn.dimensionType().hasFixedTime();
		this.level = worldIn;
		this.setDirty();
	}

	public static String getFileId(Holder<DimensionType> dimTypeIn) {
		return dimTypeIn.is(Level.END.location()) ? "invasions_end" : "invasions";
	}
	
	public static HashMap<ServerLevel, InvasionWorldData> getInvasionData() {
		return INVASION_DATA;
	}
	
	@Override
	public CompoundTag save(CompoundTag nbtIn) {
		nbtIn.putLong("Days", this.days);
		return nbtIn;
	}
	
	public ServerLevel getWorld() {
		return this.level;
	}
	
	public boolean hasFixedTime() {
		return this.hasFixedTime;
	}
	
	public long getDays() {
		return this.days;
	}
	
	public void setDays(long daysIn) {
		this.days = daysIn;
		this.setDirty();
	}
}
