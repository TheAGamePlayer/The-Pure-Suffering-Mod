package dev.theagameplayer.puresuffering.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;

public final class InvasionSpawnerEntity extends WeightedRandom.Item {
	private final CompoundNBT tag;

	public InvasionSpawnerEntity() {
		super(1);
		this.tag = new CompoundNBT();
		this.tag.putString("id", "minecraft:pig");
	}
	
	public InvasionSpawnerEntity(CompoundNBT nbtIn) {
		this(nbtIn.contains("Weight", 99) ? nbtIn.getInt("Weight") : 1, nbtIn.getCompound("Entity"));
	}

	public InvasionSpawnerEntity(int weightIn, CompoundNBT nbtIn) {
		super(weightIn);
		this.tag = nbtIn;
		ResourceLocation resourcelocation = ResourceLocation.tryParse(nbtIn.getString("id"));
		if (resourcelocation != null) {
			nbtIn.putString("id", resourcelocation.toString());
		} else {
			nbtIn.putString("id", "minecraft:pig");
		}
	}

	public CompoundNBT save() {
		CompoundNBT compoundNBT = new CompoundNBT();
		compoundNBT.put("Entity", this.tag);
		compoundNBT.putInt("Weight", this.weight);
		return compoundNBT;
	}

	public CompoundNBT getTag() {
		return this.tag;
	}
}
