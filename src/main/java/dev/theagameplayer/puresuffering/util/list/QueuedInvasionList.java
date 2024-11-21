package dev.theagameplayer.puresuffering.util.list;

import java.util.ArrayList;
import java.util.Iterator;

import dev.theagameplayer.puresuffering.invasion.Invasion.BuildInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

public class QueuedInvasionList implements Iterable<BuildInfo> {
	private final ArrayList<BuildInfo> invasions = new ArrayList<>();
	private final InvasionDifficulty difficulty;
	
	public QueuedInvasionList(final InvasionDifficulty pDifficulty) {
		this.difficulty = pDifficulty;
	}
	
	public final BuildInfo getPrimary() {
		return this.invasions.get(0);
	}
	
	public final InvasionDifficulty getDifficulty() {
		return this.difficulty;
	}
	
	public static final QueuedInvasionList load(final ServerLevel pLevel, final CompoundTag pNbt) {
		final QueuedInvasionList list = new QueuedInvasionList(InvasionDifficulty.values()[pNbt.getInt("Difficulty")]);
		final ListTag invasionsNBT = pNbt.getList("Invasions", Tag.TAG_COMPOUND);
		for (final Tag inbt : invasionsNBT) {
			if (inbt instanceof CompoundTag nbt)
				list.add(BuildInfo.load(pLevel, nbt));
		}
		return list;
	}
	
	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final ListTag invasionsNBT = new ListTag();
		nbt.putInt("Difficulty", this.difficulty.ordinal());
		for (final BuildInfo invasion : this.invasions)
			invasionsNBT.add(invasion.save());
		nbt.put("Invasions", invasionsNBT);
		return nbt;
	}
	
	public final void add(final BuildInfo pInvasion) {
		this.invasions.add(pInvasion);
	}
	
	public final void remove(final BuildInfo pInvasion) {
		this.invasions.remove(pInvasion);
	}
	
	//Array List Functions
    public final int size() {
    	return this.invasions.size();
    }
    
    public final boolean isEmpty() {
    	return this.invasions.isEmpty();
    }
	
    public final BuildInfo get(final int pIndex) {
    	return this.invasions.get(pIndex);
    }
	
    @Override
    public final String toString() {
    	return this.invasions.toString();
    }

	@Override
	public final Iterator<BuildInfo> iterator() {
		return this.invasions.iterator();
	}
}
