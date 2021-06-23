package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.theagameplayer.puresuffering.PSConfig;
import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.util.InvasionSpawnerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeMagnifier;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class InvasionType {
	private final ResourceLocation id;
	private final Map<Integer, InvasionSkyRenderer> skyRenderer;
	private final Map<Integer, List<Spawners>> mobSpawnList;
	private final boolean isDayInvasion;
	private final boolean forceNoSleep;
	private final boolean setsToNight;
	private final boolean changesDarkness;
	private final boolean isRepeating;
	private final boolean isEnvironmental;
	private final boolean onlyDuringNight;
	private final float brightness;
	private final int lightLevel;
	private final int maxSeverity;
	private final int tickDelay;
	private final int rarity;
	private final ITextComponent component;
	private final ArrayList<InvasionSpawnerEntity> spawnPotentials = new ArrayList<>();
	private InvasionSpawnerEntity nextSpawnData = new InvasionSpawnerEntity();
	private int spawnDelay;

	public InvasionType(ResourceLocation idIn, Map<Integer, InvasionSkyRenderer> skyRendererIn, Map<Integer, List<Spawners>> mobSpawnListIn, boolean isDayInvasionIn, boolean forceNoSleepIn, boolean setsToNightIn, boolean changesDarknessIn, boolean isRepeatingIn, boolean isEnvironmentalIn, boolean onlyDuringNightIn, float brightnessIn, int lightLevelIn, int maxSeverityIn, int tickDelayIn, int rarityIn) {
		this.id = idIn;
		this.skyRenderer = skyRendererIn;
		this.mobSpawnList = mobSpawnListIn;
		this.isDayInvasion = isDayInvasionIn;
		this.forceNoSleep = forceNoSleepIn;
		this.setsToNight = setsToNightIn;
		this.changesDarkness = changesDarknessIn;
		this.isRepeating = isRepeatingIn;
		this.isEnvironmental = isEnvironmentalIn;
		this.onlyDuringNight = onlyDuringNightIn;
		this.brightness = brightnessIn;
		this.lightLevel = lightLevelIn;
		this.maxSeverity = maxSeverityIn;
		this.tickDelay = tickDelayIn;
		this.rarity = rarityIn;
		this.component = new TranslationTextComponent("invasion." + idIn.getNamespace() + "." + idIn.getPath());
	}
	
	public InvasionType.Builder deconstruct() {
		Map<Integer, InvasionSkyRenderer.Builder> skyRenderer = new HashMap<>();
		for (Entry<Integer, InvasionSkyRenderer> entry : this.skyRenderer.entrySet())
			skyRenderer.put(entry.getKey(), entry.getValue().deconstruct());
		return new InvasionType.Builder(skyRenderer, this.mobSpawnList, this.isDayInvasion, this.forceNoSleep, this.setsToNight, this.changesDarkness, this.isRepeating, this.isEnvironmental, this.onlyDuringNight, this.brightness, this.lightLevel, this.maxSeverity, this.tickDelay, this.rarity);
	}
	
	public ResourceLocation getId() {
		return this.id;
	}
	
	public Map<Integer, InvasionSkyRenderer> getSkyRenderer() {
		return this.skyRenderer;
	}
	
	public Map<Integer, List<Spawners>> getMobSpawnList() {
		return this.mobSpawnList;
	}
	
	public boolean isDayInvasion() {
		return this.isDayInvasion;
	}
	
	public boolean forcesNoSleep() {
		return this.forceNoSleep;
	}
	
	public boolean setsEventsToNight() {
		return this.setsToNight;
	}
	
	public boolean changesDarkness() {
		return this.changesDarkness;
	}
	
	public boolean isRepeatable() {
		return this.isRepeating;
	}
	
	public boolean isEnvironmental() {
		return this.isEnvironmental;
	}
	
	public boolean isOnlyDuringNight() {
		return this.onlyDuringNight;
	}
	
	public float getBrightness() {
		return this.brightness;
	}
	
	public int getLightLevel() {
		return this.lightLevel;
	}
	
	public int getMaxSeverity() {
		return this.maxSeverity;
	}
	
	public int getTickDelay() {
		return this.tickDelay;
	}
	
	public int getRarity() {
		return this.rarity;
	}
	
	public ITextComponent getComponent() {
		return this.component;
	}
	
	public void tick(ServerWorld worldIn, ArrayList<Pair<InvasionType, Integer>> invasionListIn, Pair<InvasionType, Integer> pairIn) {
		this.tickEntitySpawn(worldIn, pairIn.getRight());
	}
	
	private final void tickEntitySpawn(ServerWorld worldIn, int severityIn) {
		if (this.getMobSpawnList() != null && this.getTickDelay() > -1) {
			if (this.spawnDelay < 0) {
				this.delay(worldIn, severityIn);
			}
			if (this.spawnDelay > 0) {
				--this.spawnDelay;
				return;
			}
			boolean flag1 = false;
			List<Spawners> mobs;
			int index;
			Spawners spawners;
			ChunkPos chunkPos = this.getSpawnChunk(worldIn);
			if (this.getMobSpawnList().isEmpty()) {
				BlockPos pos = this.getSpawnPos(worldIn, chunkPos);
				mobs = this.getRoughBiome(pos, worldIn.getChunk(pos)).getMobSettings().getMobs(EntityClassification.MONSTER);
				if (mobs.size() < 1) return;
			} else {
				mobs = this.getMobSpawnList().get(severityIn - 1);
			}
			index = worldIn.random.nextInt(mobs.size());
			spawners = mobs.get(index);
			int groupSize = worldIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
			this.nextSpawnData.getTag().putString("id", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
			for(int count = 0; count < groupSize; ++count) {
				CompoundNBT compoundNBT = this.nextSpawnData.getTag();
				Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
				if (!optional.isPresent()) {
					this.delay(worldIn, severityIn);
					return;
				}
				BlockPos pos = this.getSpawnPos(worldIn, chunkPos);
				if (pos != null && EntitySpawnPlacementRegistry.checkSpawnRules(optional.get(), worldIn, SpawnReason.EVENT, pos, worldIn.getRandom())) {
					Entity entity = EntityType.loadEntityRecursive(compoundNBT, worldIn, (e) -> {
						e.moveTo(pos.getX(), pos.getY(), pos.getZ(), e.yRot, e.xRot);
						return e;
					});
					if (entity == null) {
						this.delay(worldIn, severityIn);
						return;
					}
					entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), worldIn.random.nextFloat() * 360.0F, 0.0F);
					if (entity instanceof MobEntity) {
						MobEntity mobEntity = (MobEntity)entity;
						if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
							if (!ForgeEventFactory.doSpecialSpawn(mobEntity, worldIn, (float)mobEntity.getX(), (float)mobEntity.getY(), (float)mobEntity.getZ(), null, SpawnReason.EVENT)) {
								mobEntity.getPersistentData().putString("InvasionMob", this.id.toString());
								mobEntity.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.EVENT, (ILivingEntityData)null, (CompoundNBT)null);
								mobEntity.setTarget(worldIn.getNearestPlayer(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ(), Integer.MAX_VALUE, true));
								if (PSConfig.COMMON.shouldMobsSpawnWithInfiniteRange.get())
									mobEntity.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2048.0D);
							}
						}
					}
					if (!worldIn.tryAddFreshEntityWithPassengers(entity)) {
						this.delay(worldIn, severityIn);
						return;
					}
					worldIn.levelEvent(Constants.WorldEvents.MOB_SPAWNER_PARTICLES, pos, 0);
					if (entity instanceof MobEntity) {
						((MobEntity)entity).spawnAnim();
					}
					flag1 = true;
				}
			}
			if (flag1) {
				this.delay(worldIn, severityIn);
			}
		}
	}
	
	private void delay(ServerWorld worldIn, int severityIn) {
		int delay = this.tickDelay;
		this.spawnDelay = (delay * (this.getMaxSeverity() + 1)) - (delay * severityIn);
		if (!this.spawnPotentials.isEmpty()) {
			this.nextSpawnData = WeightedRandom.getRandomItem(worldIn.random, this.spawnPotentials);;
		}
	}
	
	private Biome getRoughBiome(BlockPos posIn, IChunk chunkIn) {
		return DefaultBiomeMagnifier.INSTANCE.getBiome(0L, posIn.getX(), posIn.getY(), posIn.getZ(), chunkIn.getBiomes());
	}
	
	private ChunkPos getSpawnChunk(ServerWorld worldIn) {
		ServerPlayerEntity player = worldIn.players().get(worldIn.random.nextInt(worldIn.players().size()));
		ChunkPos chunkPos = worldIn.getChunk(player.blockPosition()).getPos();
		int chunkX = chunkPos.x - 8 + worldIn.random.nextInt(17);
		int chunkZ = chunkPos.z - 8 + worldIn.random.nextInt(17);
		boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(worldIn) : chunkX, flag ? chunkZ + this.getChunkOffset(worldIn) : chunkZ);
		return chunkPos1;
	}
	
	private int getChunkOffset(ServerWorld worldIn) {
		int offSet = worldIn.random.nextInt(8) + 1;
		boolean flag = worldIn.random.nextBoolean();
		return flag ? offSet : -offSet;
	}
	
	private BlockPos getSpawnPos(ServerWorld worldIn, ChunkPos chunkPosIn) {
		int x = chunkPosIn.getMinBlockX() + worldIn.random.nextInt(16);
		int z = chunkPosIn.getMinBlockZ() + worldIn.random.nextInt(16);
		return new BlockPos(x, worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z), z);
	}
	
	@Override
	public String toString() {
		return this.getId().toString();
	}
	
	public static class Builder {
		private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
		private Map<Integer, InvasionSkyRenderer.Builder> skyRenderer;
		private Map<Integer, List<Spawners>> mobSpawnList;
		private boolean isDayInvasion = false;
		private boolean forceNoSleep = false;
		private boolean setsToNight = false;
		private boolean changesDarkness = false;
		private boolean isRepeating = true;
		private boolean isEnvironmental = false;
		private boolean onlyDuringNight = false;
		private float brightness;
		private int lightLevel; 
		private int maxSeverity = 0;
		private int tickDelay = 6;
		private int rarity = 0;
		
		private Builder(Map<Integer, InvasionSkyRenderer.Builder> skyRendererIn, Map<Integer, List<Spawners>> mobSpawnListIn, boolean isDayInvasionIn, boolean forceNoSleepIn, boolean setsToNightIn, boolean changesDarknessIn, boolean isRepeatingIn, boolean isEnvironmentalIn, boolean onlyDuringNightIn, float brightnessIn, int lightLevelIn, int maxSeverityIn, int tickDelayIn, int rarityIn) {
			this.skyRenderer = skyRendererIn;
			this.mobSpawnList = mobSpawnListIn;
			this.isDayInvasion = isDayInvasionIn;
			this.forceNoSleep = forceNoSleepIn;
			this.setsToNight = setsToNightIn;
			this.changesDarkness = changesDarknessIn;
			this.isRepeating = isRepeatingIn;
			this.isEnvironmental = isEnvironmentalIn;
			this.onlyDuringNight = onlyDuringNightIn;
			this.brightness = brightnessIn;
			this.lightLevel = lightLevelIn;
			this.maxSeverity = maxSeverityIn;
			this.tickDelay = tickDelayIn;
			this.rarity = rarityIn;
		}
		
		private Builder() {};
		
		public static InvasionType.Builder invasionType() {
			return new InvasionType.Builder();
		}
		
		public InvasionType.Builder skyRenderer(Map<Integer, InvasionSkyRenderer.Builder> skyRendererIn) {
			this.skyRenderer = skyRendererIn;
			this.isEnvironmental = true;
			return this;
		}
		
		public InvasionType.Builder mobSpawnList(Map<Integer, List<Spawners>> mobSpawnListIn) {
			this.mobSpawnList = mobSpawnListIn;
			return this;
		}
		
		public InvasionType.Builder setDayTimeEvent() {
			this.isDayInvasion = true;
			return this;
		}
		
		public InvasionType.Builder setForcesNoSleep() {
			this.forceNoSleep = true;
			return this;
		}
		
		public InvasionType.Builder setToNightEvents() {
			this.setsToNight = true;
			return this;
		}
		
		public InvasionType.Builder withLight(float brightnessIn, int lightLevelIn) {
			this.brightness = brightnessIn;
			this.lightLevel = lightLevelIn;
			this.changesDarkness = true;
			return this;
		}
		
		public InvasionType.Builder setNonRepeatable() {
			this.isRepeating = false;
			return this;
		}
		
		public InvasionType.Builder setOnlyDuringNight() {
			this.onlyDuringNight = true;
			return this;
		}
		
		public InvasionType.Builder maxSeverity(int severityIn) {
			this.maxSeverity = severityIn;
			return this;
		}
		
		public InvasionType.Builder tickDelay(int tickDelayIn) {
			this.tickDelay = tickDelayIn;
			return this;
		}
		
		public InvasionType.Builder withRarity(int rarityIn) {
			this.rarity = rarityIn;
			return this;
		}
		
		public InvasionType save(Consumer<InvasionType> consumerIn, String pathIn) {
			InvasionType invasionType = this.build(new ResourceLocation(PureSufferingMod.MODID, pathIn));
			consumerIn.accept(invasionType);
			return invasionType;
		}
		
		public InvasionType build(ResourceLocation idIn) {
			Map<Integer, InvasionSkyRenderer> skyRenderer = new HashMap<>();
			if (this.skyRenderer != null)
				for (Entry<Integer, InvasionSkyRenderer.Builder> entry : this.skyRenderer.entrySet())
					skyRenderer.put(entry.getKey(), entry.getValue().build(idIn));
			return new InvasionType(idIn, skyRenderer, this.mobSpawnList, this.isDayInvasion, this.forceNoSleep, this.setsToNight, this.changesDarkness, this.isRepeating, this.isEnvironmental, this.onlyDuringNight, this.brightness, this.lightLevel, this.maxSeverity, this.tickDelay, this.rarity);
		}
		
		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.mobSpawnList != null) {
				JsonArray jsonArray = new JsonArray();
				for (List<Spawners> spawnerList : this.mobSpawnList.values()) {
					JsonArray jsonArray1 = new JsonArray();
					for (Spawners spawners : spawnerList) {
						JsonObject jsonObject1 = new JsonObject();
						jsonObject1.addProperty("EntityType", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
						jsonObject1.addProperty("Weight", spawners.weight);
						jsonObject1.addProperty("MinCount", spawners.minCount);
						jsonObject1.addProperty("MaxCount", spawners.maxCount);
						jsonArray1.add(jsonObject1);
					}
					jsonArray.add(jsonArray1);
				}
				jsonObject.add("MobSpawnList", jsonArray);
			}
			if (this.skyRenderer != null) {
				JsonArray jsonArray = new JsonArray();
				for (InvasionSkyRenderer.Builder builder : this.skyRenderer.values()) {
					jsonArray.add(builder.serializeToJson());
				}
				jsonObject.add("SkyRenderer", jsonArray);
			}
			jsonObject.addProperty("IsDayInvasion", this.isDayInvasion);
			jsonObject.addProperty("ForceNoSleep", this.forceNoSleep);
			if (this.isDayInvasion)
				jsonObject.addProperty("SetsEventsToNight", this.setsToNight);
			if (this.changesDarkness) {
				jsonObject.addProperty("Brightness", this.brightness);
				jsonObject.addProperty("LightLevel", this.lightLevel);
			}
			jsonObject.addProperty("IsRepeatable", this.isRepeating);
			if (!this.isDayInvasion)
				jsonObject.addProperty("OnlyDuringNight", this.onlyDuringNight);
			jsonObject.addProperty("MaxSeverity", this.maxSeverity);
			jsonObject.addProperty("TickDelay", this.tickDelay);
			jsonObject.addProperty("Rarity", this.rarity);
			return jsonObject;
		}
		
		public static InvasionType.Builder fromJson(JsonObject jsonObjectIn) {
			boolean isDayInvasion = jsonObjectIn.get("IsDayInvasion").getAsBoolean();
			boolean forceNoSleep = jsonObjectIn.has("ForceNoSleep") ? jsonObjectIn.get("ForceNoSleep").getAsBoolean() : false;
			boolean setsToNight = jsonObjectIn.has("SetsEventsToNight") ? jsonObjectIn.get("SetsEventsToNight").getAsBoolean() : false;
			boolean isDarkInDay = jsonObjectIn.has("Brightness") && jsonObjectIn.has("LightLevel");
			boolean isRepeating = jsonObjectIn.get("IsRepeatable").getAsBoolean();
			boolean isEnvironmental = jsonObjectIn.has("SkyRenderer");
			boolean onlyDuringNight = jsonObjectIn.has("OnlyDuringNight") ? jsonObjectIn.get("OnlyDuringNight").getAsBoolean() : false;
			float brightness = jsonObjectIn.has("Brightness") ? jsonObjectIn.get("Brightness").getAsFloat() : 0.0F;
			int lightLevel = jsonObjectIn.has("LightLevel") ? jsonObjectIn.get("LightLevel").getAsInt() : 0;
			int maxSeverity = jsonObjectIn.get("MaxSeverity").getAsInt();
			int tickDelay = jsonObjectIn.get("TickDelay").getAsInt();
			int rarity = jsonObjectIn.get("Rarity").getAsInt();
			boolean errored = false;
			Map<Integer, List<Spawners>> mobSpawnList = new HashMap<>();
			JsonElement jsonElement = jsonObjectIn.getAsJsonArray("MobSpawnList");
			if (jsonElement != null) {
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					for (int list = 0; list < jsonArray.size(); list++) {
						ArrayList<Spawners> spawnList = new ArrayList<>();
						JsonElement jsonElement1 = jsonArray.get(list);
						if (jsonElement1.isJsonArray()) {
							JsonArray jsonArray1 = jsonElement1.getAsJsonArray();
							for (JsonElement jsonElement2 : jsonArray1) {
								if (jsonElement2.isJsonObject()) {
									JsonObject jsonObject = jsonElement2.getAsJsonObject();
									spawnList.add(new Spawners(ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryParse(jsonObject.get("EntityType").getAsString())), jsonObject.get("Weight").getAsInt(), jsonObject.get("MinCount").getAsInt(), jsonObject.get("MaxCount").getAsInt()));
								} else {
									errored = true;
									break;
								}
							}
						} else {
							errored = true;
							break;
						}
						mobSpawnList.put(list, spawnList);
					}
				} else {
					errored = true;
				}
			}
			Map<Integer, InvasionSkyRenderer.Builder> skyRenderer = new HashMap<>();
			JsonElement jsonElement1 = jsonObjectIn.getAsJsonArray("SkyRenderer");
			if (jsonElement1 != null) {
				if (jsonElement1.isJsonArray()) {
					JsonArray jsonArray = jsonElement1.getAsJsonArray();
					for (int sky = 0; sky < jsonArray.size(); sky++) {
						JsonElement jsonElement2 = jsonArray.get(sky);
						if (jsonElement2.isJsonObject()) {
							skyRenderer.put(sky, InvasionSkyRenderer.Builder.fromJson(jsonElement2.getAsJsonObject()));
						} else {
							errored = true;
							break;
						}
					}
				} else {
					errored = true;
				}
			}
			if (errored) {
				LOGGER.error("JsonElement is incorrectly setup: " + jsonElement.toString() + ". Therefore InvasionType wasn't registered!");
			}
			return new InvasionType.Builder(skyRenderer, mobSpawnList, isDayInvasion, forceNoSleep, setsToNight, isDarkInDay, isRepeating, isEnvironmental, onlyDuringNight, brightness, lightLevel, maxSeverity, tickDelay, rarity);
		}
	}
}
