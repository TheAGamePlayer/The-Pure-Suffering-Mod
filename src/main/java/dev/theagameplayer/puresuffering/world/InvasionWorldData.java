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
	
	public InvasionWorldData(final ServerLevel levelIn) {
		this.hasFixedTime = levelIn.dimensionType().hasFixedTime();
		this.level = levelIn;
		this.setDirty();
	}

	public static final String getFileId(final Holder<DimensionType> dimTypeIn) {
		return dimTypeIn.is(Level.END.location()) ? "invasions_end" : "invasions";
	}
	
	public static final HashMap<ServerLevel, InvasionWorldData> getInvasionData() {
		return INVASION_DATA;
	}
	
	@Override
	public CompoundTag save(final CompoundTag nbtIn) {
		nbtIn.putLong("Days", this.days);
		return nbtIn;
	}
	
	public final ServerLevel getWorld() {
		return this.level;
	}
	
	public final boolean hasFixedTime() {
		return this.hasFixedTime;
	}
	
	public final long getDays() {
		return this.days;
	}
	
	public final void setDays(final long daysIn) {
		this.days = daysIn;
		this.setDirty();
	}
}
