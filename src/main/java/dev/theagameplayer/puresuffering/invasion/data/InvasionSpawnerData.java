package dev.theagameplayer.puresuffering.invasion.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

public final class InvasionSpawnerData extends WeightedEntry.IntrusiveBase { //From MobSpawnSettings$SpawnerData
     public final EntityType<?> type;
     public final int minCount;
     public final int maxCount;
     public final boolean ignoreSpawnRules;
     public final boolean forceDespawn;

     public InvasionSpawnerData(final EntityType<?> typeIn, final int weightIn, final int minCountIn, final int maxCountIn) {
         this(typeIn, Weight.of(weightIn), minCountIn, maxCountIn, false, false);
      }
     
     public InvasionSpawnerData(final EntityType<?> typeIn, final int weightIn, final int minCountIn, final int maxCountIn, final boolean ignoreSpawnRulesIn, final boolean forceDespawnIn) {
        this(typeIn, Weight.of(weightIn), minCountIn, maxCountIn, ignoreSpawnRulesIn, forceDespawnIn);
     }

     public InvasionSpawnerData(final EntityType<?> typeIn, final Weight weightIn, final int minCountIn, final int maxCountIn, final boolean ignoreSpawnRulesIn, final boolean forceDespawnIn) {
        super(weightIn);
        this.type = typeIn.getCategory() == MobCategory.MISC ? EntityType.PIG : typeIn;
        this.minCount = minCountIn;
        this.maxCount = maxCountIn;
        this.ignoreSpawnRules = ignoreSpawnRulesIn;
        this.forceDespawn = forceDespawnIn;
     }

     @Override
     public String toString() {
        return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.getWeight() + ", " + this.ignoreSpawnRules;
     }
     
     public static final ArrayList<InvasionSpawnerData> convertSpawners(final List<MobSpawnSettings.SpawnerData> listIn) {
    	 final ArrayList<InvasionSpawnerData> spawners = new ArrayList<>();
    	 for (final MobSpawnSettings.SpawnerData spawner : listIn)
    		 spawners.add(new InvasionSpawnerData(spawner.type, spawner.getWeight(), spawner.minCount, spawner.maxCount, false, false));
    	 return spawners;
     }
}
