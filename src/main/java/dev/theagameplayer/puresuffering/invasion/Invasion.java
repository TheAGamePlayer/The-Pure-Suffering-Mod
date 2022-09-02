package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SeverityInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class Invasion {
	private static final ArrayList<Biome> MIXED_BIOMES = new ArrayList<>(ForgeRegistries.BIOMES.getValues());
	private final ArrayList<UUID> invasionMobs = new ArrayList<>();
	private final InvasionType invasionType;
	private final int severity;
	private final boolean isPrimary;
	private final int mobCap;
	private final boolean shouldTick;
	private final SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
	private SpawnData nextSpawnData = new SpawnData();
	private int spawnDelay;

	public Invasion(final InvasionType invasionTypeIn, final int severityIn, final boolean isPrimaryIn) {
		SeverityInfo info = invasionTypeIn.getSeverityInfo().get(severityIn);
		int mobCap = isPrimaryIn ? PSConfigValues.common.primaryInvasionMobCap : PSConfigValues.common.secondaryInvasionMobCap;
		this.invasionType = invasionTypeIn;
		this.severity = severityIn;
		this.isPrimary  = isPrimaryIn;
		this.mobCap = (int)(mobCap * info.getMobCapPercentage()) + 1;
		this.shouldTick = info.getTickDelay() > -1 && (invasionTypeIn.getSpawningSystem() != SpawningSystem.DEFAULT || (info.getMobSpawnList() != null && invasionTypeIn.getSpawningSystem() == SpawningSystem.DEFAULT));
	}

	public InvasionType getType() {
		return this.invasionType;
	}

	public int getSeverity() {
		return this.severity;
	}

	public boolean isPrimary() {
		return this.isPrimary;
	}

	public int getMobCap() {
		return this.mobCap;
	}

	public SeverityInfo getSeverityInfo() {
		return this.invasionType.getSeverityInfo().get(this.severity);
	}

	public void tick(final ServerLevel worldIn) {
		this.invasionMobs.removeIf(uuid -> {
			return worldIn.getEntity(uuid) == null || !worldIn.getEntity(uuid).isAlive();
		});
		switch (this.invasionType.getWeatherType()) {
		case DEFAULT: break;
		case CLEAR:
			if (worldIn.isRaining() || worldIn.isThundering())
				worldIn.setWeatherParameters(600, 0, false, false);
			break;
		case RAIN:
			if (!worldIn.isRaining())
				worldIn.setWeatherParameters(0, 600, true, false);
			break;
		case THUNDER:
			if (!worldIn.isThundering())
				worldIn.setWeatherParameters(0, 600, true, true);
			break;
		}
		if (this.shouldTick)
			this.tickEntitySpawn(worldIn);
	}

	protected void tickEntitySpawn(ServerLevel worldIn) {
		if (this.invasionMobs.size() < this.mobCap) {
			//Delay check
			if (this.spawnDelay < 0) {
				this.delay(worldIn);
			}
			if (this.spawnDelay > 0) {
				--this.spawnDelay;
				return;
			}
			//Get Mobs
			boolean flag1 = false;
			if (worldIn.players().size() < 1) return;
			ChunkPos chunkPos = this.getSpawnChunk(worldIn);
			List<MobSpawnSettings.SpawnerData> mobs = this.getMobSpawnList(worldIn, chunkPos);
			if (mobs.isEmpty()) return;
			//Spawn Mob Cluster (Different Mobs)
			int clusterSize = worldIn.random.nextInt(this.getSeverityInfo().getClusterSize()) + 1;
			for (int cluster = 0; cluster < clusterSize && this.invasionMobs.size() < this.mobCap; cluster++) {
				MobSpawnSettings.SpawnerData spawners = mobs.get(worldIn.random.nextInt(mobs.size()));
				int groupSize = worldIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
				this.nextSpawnData.getEntityToSpawn().putString("id", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
				CompoundTag compoundNBT = this.nextSpawnData.getEntityToSpawn();
				Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
				if (!optional.isPresent()) {
					this.delay(worldIn);
					return;
				}
				//Spawn Mob Group (Same Mob)
				for(int count = 0; count < groupSize && this.invasionMobs.size() < this.mobCap; ++count) {
					//Spawn Entity
					BlockPos spawnPos = this.getSpawnPos(worldIn, chunkPos, false);
					if (spawnPos != null && SpawnPlacements.checkSpawnRules(optional.get(), worldIn, MobSpawnType.EVENT, spawnPos, worldIn.getRandom())) {
						Entity entity = EntityType.loadEntityRecursive(compoundNBT, worldIn, (e) -> {
							e.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), e.getYRot(), e.getXRot());
							return e;
						});
						if (entity == null) {
							this.delay(worldIn);
							return;
						}
						entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), worldIn.random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof Mob) {
							Mob mobEntity = (Mob)entity;
							if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
								if (!ForgeEventFactory.doSpecialSpawn(mobEntity, (LevelAccessor)worldIn, (float)mobEntity.getX(), (float)mobEntity.getY(), (float)mobEntity.getZ(), null, MobSpawnType.EVENT)) {
									this.spawnInvasionMob(worldIn, mobEntity);
								}
							}
						}
						if (!worldIn.tryAddFreshEntityWithPassengers(entity)) {
							this.delay(worldIn);
							return;
						}
						flag1 = true;
					}
				}
			}
			if (flag1) {
				this.delay(worldIn);
			}
		}
	}

	protected void spawnInvasionMob(ServerLevel worldIn, Mob mobEntityIn) {
		mobEntityIn.getPersistentData().putString("InvasionMob", this.invasionType.getId().toString());
		mobEntityIn.getPersistentData().putBoolean("AntiGrief", worldIn.dimensionType().hasFixedTime());
		mobEntityIn.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(mobEntityIn.blockPosition()), MobSpawnType.EVENT, (SpawnGroupData)null, (CompoundTag)null);
		if (PSConfigValues.common.shouldMobsSpawnWithMaxRange)
			mobEntityIn.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2048.0D);
		this.invasionMobs.add(mobEntityIn.getUUID());
		worldIn.levelEvent(2004, mobEntityIn.blockPosition(), 0); //Mob Spawn Particles
		mobEntityIn.spawnAnim();
	}

	protected final void delay(ServerLevel worldIn) {
		this.spawnDelay = this.getSeverityInfo().getTickDelay();
		this.spawnPotentials.getRandom(worldIn.random).ifPresent(entry -> {
			this.nextSpawnData = entry.getData();
		});
	}

	protected final List<MobSpawnSettings.SpawnerData> getMobSpawnList(ServerLevel worldIn, ChunkPos chunkPosIn) {
		BlockPos biomePos = this.getSpawnPos(worldIn, chunkPosIn, true);
		ArrayList<MobSpawnSettings.SpawnerData> originalList = new ArrayList<>(this.getSeverityInfo().getMobSpawnList());
		switch (this.invasionType.getSpawningSystem()) {
		case DEFAULT: break;
		case BIOME_BOOSTED:
			originalList.addAll(this.getBiomeSpawnList(biomePos, worldIn.getChunk(biomePos)));
			break;
		case BIOME_MIXED:
			originalList.addAll(this.getMixedSpawnList(worldIn));
			break;
		}
		ArrayList<MobSpawnSettings.SpawnerData> newList = new ArrayList<>();
		if (!originalList.isEmpty()) {
			for (MobSpawnSettings.SpawnerData spawners : originalList) {
				for (int w = 0; w < spawners.getWeight().asInt(); w++)
					newList.add(spawners);
			}
		}
		return newList;
	}

	private final ArrayList<MobSpawnSettings.SpawnerData> getBiomeSpawnList(BlockPos posIn, ChunkAccess chunkIn) {
		ArrayList<MobSpawnSettings.SpawnerData> spawners = new ArrayList<>(chunkIn.getNoiseBiome(posIn.getX(), posIn.getY(), posIn.getZ()).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap());
		spawners.removeIf(spawner -> {
			ResourceLocation name = spawner.type.getRegistryName();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ArrayList<MobSpawnSettings.SpawnerData> getMixedSpawnList(ServerLevel worldIn) {
		ArrayList<MobSpawnSettings.SpawnerData> spawners = new ArrayList<>(MIXED_BIOMES.get(worldIn.random.nextInt(MIXED_BIOMES.size())).getMobSettings().getMobs(MobCategory.MONSTER).unwrap());
		spawners.removeIf(spawner -> {
			ResourceLocation name = spawner.type.getRegistryName();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	protected final ChunkPos getSpawnChunk(ServerLevel worldIn) {
		ServerPlayer player = worldIn.players().get(worldIn.random.nextInt(worldIn.players().size()));
		ChunkPos chunkPos = worldIn.getChunk(player.blockPosition()).getPos();
		int chunkX = chunkPos.x - 8 + worldIn.random.nextInt(17);
		int chunkZ = chunkPos.z - 8 + worldIn.random.nextInt(17);
		boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(worldIn) : chunkX, flag ? chunkZ + this.getChunkOffset(worldIn) : chunkZ);
		return chunkPos1;
	}

	private final int getChunkOffset(ServerLevel worldIn) {
		int offSet = worldIn.random.nextInt(8) + 1;
		return worldIn.random.nextBoolean() ? offSet : -offSet;
	}

	protected final BlockPos getSpawnPos(ServerLevel worldIn, ChunkPos chunkPosIn, boolean biomeCheckIn) {
		int x = chunkPosIn.getMinBlockX() + worldIn.random.nextInt(16);
		int z = chunkPosIn.getMinBlockZ() + worldIn.random.nextInt(16);
		int surface = worldIn.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
		if (biomeCheckIn || (!worldIn.dimensionType().hasCeiling() && worldIn.random.nextBoolean()))
			return new BlockPos(x, surface, z);
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
		ArrayList<BlockPos> potentialPos = new ArrayList<>();
		for (int y = 0; y < surface; y++) {
			BlockPos pos = new BlockPos(x, y, z);
			if (worldIn.getBlockState(pos) != air && worldIn.getBlockState(pos) != bedrock && worldIn.getBlockState(pos.above()) == air)
				potentialPos.add(pos.above());
		}
		return potentialPos.size() > 0 ? potentialPos.get(worldIn.random.nextInt(potentialPos.size())) : new BlockPos(x, surface, z);
	}

	@Nullable
	public static Invasion load(CompoundTag nbtIn) {
		if (BaseEvents.getInvasionTypeManager().verifyInvasion(nbtIn.getString("InvasionType"))) {
			InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(nbtIn.getString("InvasionType")));
			Invasion invasion = new Invasion(invasionType, nbtIn.getInt("Severity"), nbtIn.getBoolean("IsPrimary"));
			CompoundTag invasionMobs = nbtIn.getCompound("InvasionMobs");
			for (String name : invasionMobs.getAllKeys())
				invasion.invasionMobs.add(invasionMobs.getUUID(name));
			return invasion;
		}
		return null;
	}

	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		CompoundTag mobs = new CompoundTag();
		for (UUID id : this.invasionMobs)
			mobs.putUUID(id.toString(), id);
		nbt.put("InvasionMobs", mobs);
		nbt.putString("InvasionType", this.invasionType.toString());
		nbt.putInt("Severity", this.severity);
		nbt.putBoolean("IsPrimary", this.isPrimary);
		return nbt;
	}

	@Override
	public String toString() {
		return this.invasionType.toString();
	}
}
