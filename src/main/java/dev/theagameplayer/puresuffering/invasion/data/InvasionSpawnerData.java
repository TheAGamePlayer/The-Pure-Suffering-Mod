package dev.theagameplayer.puresuffering.invasion.data;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
	public final ResourceLocation[] acceptableBiomes;
	public final MobTagData[] nbtTags, persistentTags;

	public InvasionSpawnerData(final EntityType<?> pType, final int pWeight, final int pMinCount, final int pMaxCount) {
		this(pType, pWeight, pMinCount, pMaxCount, false, false, new ResourceLocation[0], new MobTagData[0], new MobTagData[0]);
	}
	
	public InvasionSpawnerData(final EntityType<?> pType, final int pWeight, final int pMinCount, final int pMaxCount, final boolean pIgnoreSpawnRules, final boolean pForceDespawn) {
		this(pType, pWeight, pMinCount, pMaxCount, pIgnoreSpawnRules, pForceDespawn, new ResourceLocation[0], new MobTagData[0], new MobTagData[0]);
	}

	public InvasionSpawnerData(final EntityType<?> pType, final int pWeight, final int pMinCount, final int pMaxCount, final boolean pIgnoreSpawnRules, final boolean pForceDespawn, final ResourceLocation[] pAcceptableBiomes, final MobTagData[] pNBTTags, final MobTagData[] pPersistentTags) {
		super(Weight.of(pWeight));
		if (pType == null || pType.getCategory() == MobCategory.MISC) throw new NullPointerException("Spawning an entity type of null or of a 'Misc' category will result in undefined behavior.");
		this.type = pType;
		this.minCount = pMinCount;
		this.maxCount = pMaxCount;
		this.ignoreSpawnRules = pIgnoreSpawnRules;
		this.forceDespawn = pForceDespawn;
		this.acceptableBiomes = pAcceptableBiomes;
		this.nbtTags = pNBTTags;
		this.persistentTags = pPersistentTags;
	}

	@Override
	public String toString() {
		return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.getWeight() + ", " + this.ignoreSpawnRules + ", " + this.forceDespawn + ", " + this.acceptableBiomes + ", " + this.nbtTags + ", " + this.persistentTags;
	}

	public static final ArrayList<InvasionSpawnerData> convertSpawners(final List<MobSpawnSettings.SpawnerData> pList) {
		final ArrayList<InvasionSpawnerData> spawners = new ArrayList<>(pList.size());
		for (final MobSpawnSettings.SpawnerData spawner : pList)
			spawners.add(new InvasionSpawnerData(spawner.type, spawner.getWeight().asInt(), spawner.minCount, spawner.maxCount, false, false, new ResourceLocation[0], new MobTagData[0], new MobTagData[0]));
		return spawners;
	}

	public static final class MobTagData {
		public final MobTagType tagType;
		public final String name;
		public final Object value;

		public MobTagData(final MobTagType pTagType, final String pName, final Object pValue) {
			this.tagType = pTagType;
			this.name = pName;
			this.value = pValue;
		}
		
		public final JsonObject addTagToJson() {
			final JsonObject tagData = new JsonObject();
			tagData.addProperty("Id", this.tagType.ordinal());
			tagData.addProperty("Name", this.name);
			switch(this.tagType) {
			case TAG_BOOLEAN:
				tagData.addProperty("Value", (boolean)this.value);
				break;
			case TAG_BYTE:
				tagData.addProperty("Value", (byte)this.value);
				break;
			case TAG_SHORT:
				tagData.addProperty("Value", (short)this.value);
				break;
			case TAG_INT:
				tagData.addProperty("Value", (int)this.value);
				break;
			case TAG_LONG:
				tagData.addProperty("Value", (long)this.value);
				break;
			case TAG_FLOAT:
				tagData.addProperty("Value", (float)this.value);
				break;
			case TAG_DOUBLE:
				tagData.addProperty("Value", (double)this.value);
				break;
			case TAG_STRING:
				tagData.addProperty("Value", (String)this.value);
				break;
			case TAG_BYTE_ARRAY:
				final JsonArray byteArray = new JsonArray();
				for (final byte b : (byte[])this.value)
					byteArray.add(b);
				tagData.add("Value", byteArray);
				break;
			case TAG_INT_ARRAY:
				final JsonArray intArray = new JsonArray();
				for (final int i : (int[])this.value)
					intArray.add(i);
				tagData.add("Value", intArray);
				break;
			case TAG_LONG_ARRAY:
				final JsonArray longArray = new JsonArray();
				for (final long l : (long[])this.value)
					longArray.add(l);
				tagData.add("Value", longArray);
				break;
			default:
				break;
			}
			return tagData;
		}
		
		public static final MobTagData addTagData(final JsonObject pTagData) {
			switch(MobTagType.values()[pTagData.get("Id").getAsInt()]) {
			case TAG_BOOLEAN: return new MobTagData(MobTagType.TAG_BOOLEAN, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsBoolean());
			case TAG_BYTE: return new MobTagData(MobTagType.TAG_BYTE, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsByte());
			case TAG_SHORT: return new MobTagData(MobTagType.TAG_SHORT, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsShort());
			case TAG_INT: return new MobTagData(MobTagType.TAG_INT, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsInt());
			case TAG_LONG: return new MobTagData(MobTagType.TAG_LONG, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsLong());
			case TAG_FLOAT: return new MobTagData(MobTagType.TAG_FLOAT, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsFloat());
			case TAG_DOUBLE: return new MobTagData(MobTagType.TAG_DOUBLE, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsDouble());
			case TAG_STRING: return new MobTagData(MobTagType.TAG_STRING, pTagData.get("Name").getAsString(), pTagData.get("Value").getAsString());
			case TAG_BYTE_ARRAY:
				final JsonArray jbArray = pTagData.get("Value").getAsJsonArray();
				final byte[] byteArray = new byte[jbArray.size()];
				for (int b = 0; b < jbArray.size(); b++)
					byteArray[b] = jbArray.get(b).getAsByte();
				return new MobTagData(MobTagType.TAG_BYTE_ARRAY, pTagData.get("Name").getAsString(), byteArray);
			case TAG_INT_ARRAY:
				final JsonArray jiArray = pTagData.get("Value").getAsJsonArray();
				final int[] intArray = new int[jiArray.size()];
				for (int b = 0; b < jiArray.size(); b++)
					intArray[b] = jiArray.get(b).getAsInt();
				return new MobTagData(MobTagType.TAG_INT_ARRAY, pTagData.get("Name").getAsString(), intArray);
			case TAG_LONG_ARRAY:
				final JsonArray jsonArray = pTagData.get("Value").getAsJsonArray();
				final long[] longArray = new long[jsonArray.size()];
				for (int l = 0; l < jsonArray.size(); l++)
					longArray[l] = jsonArray.get(l).getAsLong();
				return new MobTagData(MobTagType.TAG_LONG_ARRAY, pTagData.get("Name").getAsString(), longArray);
			default: return null;
			}
		}

		public final void addTagToMob(final CompoundTag pData) {
			switch(this.tagType) {
			case TAG_BOOLEAN:
				pData.putBoolean(this.name, (boolean)this.value);
				break;
			case TAG_BYTE:
				pData.putByte(this.name, (byte)this.value);
				break;
			case TAG_SHORT:
				pData.putShort(this.name, (short)this.value);
				break;
			case TAG_INT:
				pData.putInt(this.name, (int)this.value);
				break;
			case TAG_LONG:
				pData.putLong(this.name, (long)this.value);
				break;
			case TAG_FLOAT:
				pData.putFloat(this.name, (float)this.value);
				break;
			case TAG_DOUBLE:
				pData.putDouble(this.name, (double)this.value);
				break;
			case TAG_STRING:
				pData.putString(this.name, (String)this.value);
				break;
			case TAG_BYTE_ARRAY:
				pData.putByteArray(this.name, (byte[])this.value);
				break;
			case TAG_INT_ARRAY:
				pData.putIntArray(this.name, (int[])this.value);
				break;
			case TAG_LONG_ARRAY:
				pData.putLongArray(this.name, (long[])this.value);
				break;
			default:
				break;
			}
		}

		@Override
		public String toString() {
			return "TagType: [" + this.tagType + "], Name: " + this.name;
		}
	}

	public static enum MobTagType {
		TAG_BOOLEAN, //0
		TAG_BYTE, //1
		TAG_SHORT, //2
		TAG_INT, //3
		TAG_LONG, //4
		TAG_FLOAT, //5
		TAG_DOUBLE, //6
		TAG_STRING, //7
		TAG_BYTE_ARRAY, //8
		TAG_INT_ARRAY, //9
		TAG_LONG_ARRAY; //10

		@Override
		public String toString() {
			return "Id: " + this.ordinal();
		}
	}
}
