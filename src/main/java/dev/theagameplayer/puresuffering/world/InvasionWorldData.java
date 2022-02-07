package dev.theagameplayer.puresuffering.world;

import java.util.HashMap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public abstract class InvasionWorldData extends WorldSavedData {
	private static final HashMap<ServerWorld, InvasionWorldData> INVASION_DATA = new HashMap<>();
	private final boolean hasFixedTime;
	private final ServerWorld level;
	protected long days;
	
	public InvasionWorldData(ServerWorld worldIn) {
		super(getFileId(worldIn.dimensionType()));
		this.hasFixedTime = worldIn.dimensionType().hasFixedTime();
		this.level = worldIn;
	}

	@SuppressWarnings("deprecation")
	public static String getFileId(DimensionType dimTypeIn) {
		return "invasions" + dimTypeIn.getFileSuffix();
	}
	
	public static HashMap<ServerWorld, InvasionWorldData> getInvasionData() {
		return INVASION_DATA;
	}
	
	@Override
	public void load(CompoundNBT nbtIn) {
		this.days = nbtIn.getLong("Days");
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbtIn) {
		nbtIn.putLong("Days", this.days);
		return nbtIn;
	}
	
	public ServerWorld getWorld() {
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
