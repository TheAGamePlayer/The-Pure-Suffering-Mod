package dev.theagameplayer.puresuffering.world.level.saveddata;

import java.util.HashMap;

import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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

	private InvasionLevelData(final ServerLevel pLevel) {
		this.invasionManager = new InvasionManager(pLevel.dimensionType().hasFixedTime());
		this.setDirty();
		INVASION_DATA.put(pLevel, this);
	}

	public static final SavedData.Factory<InvasionLevelData> factory(final ServerLevel pLevel) {
		return new SavedData.Factory<>(() -> {
			return new InvasionLevelData(pLevel);
		}, (nbt, provider) -> {
			return load(pLevel, nbt);
		});
	}

	public static final String getFileId(final Holder<DimensionType> pDimType) {
		return pDimType.is(BuiltinDimensionTypes.END) ? "invasions_end" : "invasions";
	}

	public static final InvasionLevelData get(final ServerLevel pLevel) {
		return INVASION_DATA.get(pLevel);
	}

	public final InvasionManager getInvasionManager() {
		return this.invasionManager;
	}

	public final long getInvasionTime() {
		return this.invasionTime;
	}

	public final void setInvasionTime(final long pInvasionTime) {
		this.invasionTime = pInvasionTime;
		this.setDirty();
	}

	public final int getXPMultiplier() {
		return this.xpMult;
	}

	public final void setXPMultiplier(final int pXPMult) {
		this.xpMult = pXPMult;
		this.setDirty();
	}

	public static final InvasionLevelData load(final ServerLevel pLevel, final CompoundTag pNbt) {
		final InvasionLevelData ilData = new InvasionLevelData(pLevel);
		ilData.getInvasionManager().load(pLevel, pNbt.getCompound("InvasionManager"));
		ilData.invasionTime = pNbt.getLong("InvasionTime");
		ilData.xpMult = pNbt.getInt("XPMult");
		return ilData;
	}

	@Override
	public CompoundTag save(final CompoundTag pNbt, final HolderLookup.Provider pProvider) {
		pNbt.put("InvasionManager", this.invasionManager.save());
		pNbt.putLong("InvasionTime", this.invasionTime);
		pNbt.putInt("XPMult", this.xpMult);
		return pNbt;
	}
}
