package dev.theagameplayer.puresuffering.world.level.saveddata;

import java.util.HashMap;

import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;

public final class InvasionLevelData extends SavedData {
	private static final HashMap<ServerLevel, InvasionLevelData> INVASION_DATA = new HashMap<>();
	private final InvasionManager invasionManager;
	private long invasionTime;
	private int xpMult;
	
	public InvasionLevelData(final ServerLevel levelIn) {
		this.invasionManager = new InvasionManager(levelIn.dimensionType().hasFixedTime());
		this.setDirty();
		INVASION_DATA.put(levelIn, this);
	}
	
	public static final String getFileId(final Holder<DimensionType> dimTypeIn) {
		return dimTypeIn.is(BuiltinDimensionTypes.END) ? "invasions_end" : "invasions";
	}
	
	public static final InvasionLevelData get(final ServerLevel levelIn) {
		return INVASION_DATA.get(levelIn);
	}
	
	public final InvasionManager getInvasionManager() {
		return this.invasionManager;
	}
	
	public final long getInvasionTime() {
		return this.invasionTime;
	}
	
	public final void setInvasionTime(final long invasionTimeIn) {
		this.invasionTime = invasionTimeIn;
		this.setDirty();
	}
	
	public final int getXPMultiplier() {
		return this.xpMult;
	}
	
	public final void setXPMultiplier(final int xpMultIn) {
		this.xpMult = xpMultIn;
		this.setDirty();
	}
	
	public static final InvasionLevelData load(final ServerLevel levelIn, final CompoundTag nbtIn) {
		final InvasionLevelData ilData = new InvasionLevelData(levelIn);
		ilData.getInvasionManager().load(levelIn, nbtIn.getCompound("InvasionManager"));
		ilData.invasionTime = nbtIn.getLong("InvasionTime");
		ilData.xpMult = nbtIn.getInt("XPMult");
		return ilData;
	}
	
	@Override
	public CompoundTag save(final CompoundTag nbtIn) {
		nbtIn.put("InvasionManager", this.invasionManager.save());
		nbtIn.putLong("InvasionTime", this.invasionTime);
		nbtIn.putInt("XPMult", this.xpMult);
		return nbtIn;
	}
}
