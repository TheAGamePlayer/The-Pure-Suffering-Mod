package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SeverityInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.util.InvasionSpawnerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeMagnifier;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class Invasion {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final ArrayList<Biome> MIXED_BIOMES = new ArrayList<>(ForgeRegistries.BIOMES.getValues());
	private final ArrayList<UUID> invasionMobs = new ArrayList<>();
	private final InvasionType invasionType;
	private final int severity;
	private final boolean isPrimary;
	private final int mobCap;
	private final boolean shouldTick;
	private final ArrayList<InvasionSpawnerEntity> spawnPotentials = new ArrayList<>();
	private InvasionSpawnerEntity nextSpawnData = new InvasionSpawnerEntity();
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

	public void tick(final ServerWorld worldIn) {
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

	protected void tickEntitySpawn(ServerWorld worldIn) {
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
			ChunkPos chunkPos = this.getSpawnChunk(worldIn);
			if (chunkPos == null || !worldIn.isLoaded(chunkPos.getWorldPosition())) return;
			List<Spawners> mobs = this.getMobSpawnList(worldIn, chunkPos);
			if (mobs.size() < 1) return;
			//Spawn Mob Cluster (Different Mobs)
			boolean flag1 = false;
			int clusterSize = worldIn.random.nextInt(this.getSeverityInfo().getClusterSize()) + 1;
			for (int cluster = 0; cluster < clusterSize && this.invasionMobs.size() < this.mobCap; cluster++) {
				Spawners spawners = mobs.get(worldIn.random.nextInt(mobs.size()));
				int groupSize = worldIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
				this.nextSpawnData.getTag().putString("id", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
				CompoundNBT compoundNBT = this.nextSpawnData.getTag();
				Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
				if (!optional.isPresent()) {
					this.delay(worldIn);
					return;
				}
				//Spawn Mob Group (Same Mob)
				for(int count = 0; count < groupSize && this.invasionMobs.size() < this.mobCap; ++count) {
					//Spawn Entity
					BlockPos spawnPos = this.getSpawnPos(worldIn, chunkPos, false);
					if (spawnPos != null && EntitySpawnPlacementRegistry.checkSpawnRules(optional.get(), worldIn, SpawnReason.EVENT, spawnPos, worldIn.getRandom())) {
						Entity entity = EntityType.loadEntityRecursive(compoundNBT, worldIn, (e) -> {
							e.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), e.yRot, e.xRot);
							return e;
						});
						if (entity == null) {
							this.delay(worldIn);
							return;
						}
						entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), worldIn.random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof MobEntity) {
							MobEntity mobEntity = (MobEntity)entity;
							if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
								if (!ForgeEventFactory.doSpecialSpawn(mobEntity, worldIn, (float)mobEntity.getX(), (float)mobEntity.getY(), (float)mobEntity.getZ(), null, SpawnReason.EVENT)) {
									this.spawnInvasionMob(worldIn, mobEntity);
								}
							}
						}
						if (entity instanceof LightningBoltEntity)
							LOGGER.info("LIGHTNING!!!");
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

	protected void spawnInvasionMob(ServerWorld worldIn, MobEntity mobEntityIn) {
		mobEntityIn.getPersistentData().putString("InvasionMob", this.invasionType.getId().toString());
		mobEntityIn.getPersistentData().putBoolean("AntiGrief", worldIn.dimensionType().hasFixedTime());
		mobEntityIn.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(mobEntityIn.blockPosition()), SpawnReason.EVENT, (ILivingEntityData)null, (CompoundNBT)null);
		if (PSConfigValues.common.shouldMobsSpawnWithMaxRange)
			mobEntityIn.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2048.0D);
		this.invasionMobs.add(mobEntityIn.getUUID());
		worldIn.levelEvent(Constants.WorldEvents.MOB_SPAWNER_PARTICLES, mobEntityIn.blockPosition(), 0);
		mobEntityIn.spawnAnim();
	}

	protected final void delay(ServerWorld worldIn) {
		this.spawnDelay = this.getSeverityInfo().getTickDelay();
		if (!this.spawnPotentials.isEmpty()) {
			this.nextSpawnData = WeightedRandom.getRandomItem(worldIn.random, this.spawnPotentials);;
		}
	}

	protected final List<MobSpawnInfo.Spawners> getMobSpawnList(ServerWorld worldIn, ChunkPos chunkPosIn) {
		BlockPos biomePos = this.getSpawnPos(worldIn, chunkPosIn, true);
		ArrayList<MobSpawnInfo.Spawners> originalList = new ArrayList<>(this.getSeverityInfo().getMobSpawnList());
		switch (this.invasionType.getSpawningSystem()) {
		case DEFAULT: break;
		case BIOME_BOOSTED:
			originalList.addAll(this.getBiomeSpawnList(biomePos, worldIn.getChunk(biomePos)));
			break;
		case BIOME_MIXED:
			originalList.addAll(this.getMixedSpawnList(worldIn));
			break;
		}
		ArrayList<MobSpawnInfo.Spawners> newList = new ArrayList<>();
		if (!originalList.isEmpty()) {
			for (Spawners spawners : originalList) {
				for (int w = 0; w < spawners.weight; w++)
					newList.add(spawners);
			}
		}
		return newList;
	}

	private final ArrayList<MobSpawnInfo.Spawners> getBiomeSpawnList(BlockPos posIn, IChunk chunkIn) {
		ArrayList<MobSpawnInfo.Spawners> spawners = new ArrayList<>(DefaultBiomeMagnifier.INSTANCE.getBiome(0L, posIn.getX(), posIn.getY(), posIn.getZ(), chunkIn.getBiomes()).getMobSettings().getMobs(EntityClassification.MONSTER));
		spawners.removeIf(spawner -> {
			ResourceLocation name = spawner.type.getRegistryName();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ArrayList<MobSpawnInfo.Spawners> getMixedSpawnList(ServerWorld worldIn) {
		ArrayList<MobSpawnInfo.Spawners> spawners = new ArrayList<>(MIXED_BIOMES.get(worldIn.random.nextInt(MIXED_BIOMES.size())).getMobSettings().getMobs(EntityClassification.MONSTER));
		spawners.removeIf(spawner -> {
			ResourceLocation name = spawner.type.getRegistryName();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ChunkPos getSpawnChunk(ServerWorld worldIn) {
		int players = worldIn.players().size();
		if (players < 1) return null;
		ServerPlayerEntity player = worldIn.players().get(worldIn.random.nextInt(players));
		ChunkPos chunkPos = worldIn.getChunk(player.blockPosition()).getPos();
		int chunkX = chunkPos.x - 8 + worldIn.random.nextInt(17);
		int chunkZ = chunkPos.z - 8 + worldIn.random.nextInt(17);
		boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(worldIn) : chunkX, flag ? chunkZ + this.getChunkOffset(worldIn) : chunkZ);
		return chunkPos1;
	}

	private final int getChunkOffset(ServerWorld worldIn) {
		int offSet = worldIn.random.nextInt(8) + 1;
		return worldIn.random.nextBoolean() ? offSet : -offSet;
	}

	protected final BlockPos getSpawnPos(ServerWorld worldIn, ChunkPos chunkPosIn, boolean biomeCheckIn) {
		int x = chunkPosIn.getMinBlockX() + worldIn.random.nextInt(16);
		int z = chunkPosIn.getMinBlockZ() + worldIn.random.nextInt(16);
		int surface = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
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
	public static Invasion load(CompoundNBT nbtIn) {
		if (BaseEvents.getInvasionTypeManager().verifyInvasion(nbtIn.getString("InvasionType"))) {
			InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(nbtIn.getString("InvasionType")));
			Invasion invasion = new Invasion(invasionType, nbtIn.getInt("Severity"), nbtIn.getBoolean("IsPrimary"));
			CompoundNBT invasionMobs = nbtIn.getCompound("InvasionMobs");
			for (String name : invasionMobs.getAllKeys())
				invasion.invasionMobs.add(invasionMobs.getUUID(name));
			return invasion;
		}
		return null;
	}

	public CompoundNBT save() {
		CompoundNBT nbt = new CompoundNBT();
		CompoundNBT mobs = new CompoundNBT();
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
