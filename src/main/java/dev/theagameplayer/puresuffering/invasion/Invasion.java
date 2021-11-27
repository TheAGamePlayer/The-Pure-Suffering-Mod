package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SeverityInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.util.InvasionSpawnerEntity;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
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
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeMagnifier;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class Invasion {
	private final ArrayList<MobEntity> invasionMobs = new ArrayList<>();
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
		this.shouldTick = info.getTickDelay() > -1 && (invasionTypeIn.getSpawningSystem() == SpawningSystem.BIOME_BOOSTED || (info.getMobSpawnList() != null && invasionTypeIn.getSpawningSystem() != SpawningSystem.BIOME_BOOSTED));
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
		this.invasionMobs.removeIf(mobEntity -> {
			return mobEntity == null || !mobEntity.isAlive();
		});
		if (this.shouldTick)
			this.tickEntitySpawn(worldIn);
	}

	protected void tickEntitySpawn(ServerWorld worldIn) {
		boolean flag = this.invasionMobs.size() < this.mobCap;
		if (flag) {
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
			ChunkPos chunkPos = this.getSpawnChunk(worldIn);
			List<Spawners> mobs = ImmutableList.of();
			switch (this.invasionType.getSpawningSystem()) {
			case DEFAULT:
				mobs = this.getSeverityInfo().getMobSpawnList();
				break;
			case BIOME_BOOSTED:
				BlockPos pos = this.getSpawnPos(worldIn, chunkPos);
				mobs = this.getRoughBiome(pos, worldIn.getChunk(pos)).getMobSettings().getMobs(EntityClassification.MONSTER);
				break;
			}
			if (mobs.size() < 1) return;
			int index = worldIn.random.nextInt(mobs.size());
			Spawners spawners = mobs.get(index);
			int groupSize = worldIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
			this.nextSpawnData.getTag().putString("id", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
			//Spawn Mob Group
			for(int count = 0; count < groupSize && flag; ++count) {
				CompoundNBT compoundNBT = this.nextSpawnData.getTag();
				Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
				if (!optional.isPresent()) {
					this.delay(worldIn);
					return;
				}
				//Spawn Entity
				BlockPos pos = this.getSpawnPos(worldIn, chunkPos);
				if (pos != null && EntitySpawnPlacementRegistry.checkSpawnRules(optional.get(), worldIn, SpawnReason.EVENT, pos, worldIn.getRandom())) {
					Entity entity = EntityType.loadEntityRecursive(compoundNBT, worldIn, (e) -> {
						e.moveTo(pos.getX(), pos.getY(), pos.getZ(), e.yRot, e.xRot);
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
					if (!worldIn.tryAddFreshEntityWithPassengers(entity)) {
						this.delay(worldIn);
						return;
					}
					flag1 = true;
				}
			}
			if (flag1) {
				this.delay(worldIn);
			}
		}
	}

	protected void spawnInvasionMob(ServerWorld worldIn, MobEntity mobEntityIn) {
		mobEntityIn.getPersistentData().putString("InvasionMob", this.invasionType.getId().toString());
		mobEntityIn.getPersistentData().putBoolean("AntiGrief", ServerTimeUtil.isServerDay(worldIn));
		mobEntityIn.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(mobEntityIn.blockPosition()), SpawnReason.EVENT, (ILivingEntityData)null, (CompoundNBT)null);
		if (PSConfigValues.common.hyperAggression && !PSConfigValues.common.hyperAggressionBlacklist.contains(mobEntityIn.getType().getRegistryName().toString()))
			mobEntityIn.setTarget(worldIn.getNearestPlayer(mobEntityIn.getX(), mobEntityIn.getY(), mobEntityIn.getZ(), Integer.MAX_VALUE, true));
		if (PSConfigValues.common.shouldMobsSpawnWithMaxRange)
			mobEntityIn.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2048.0D);
		this.invasionMobs.add(mobEntityIn);
		worldIn.levelEvent(Constants.WorldEvents.MOB_SPAWNER_PARTICLES, mobEntityIn.blockPosition(), 0);
		mobEntityIn.spawnAnim();
	}

	protected final void delay(ServerWorld worldIn) {
		this.spawnDelay = this.getSeverityInfo().getTickDelay();
		if (!this.spawnPotentials.isEmpty()) {
			this.nextSpawnData = WeightedRandom.getRandomItem(worldIn.random, this.spawnPotentials);;
		}
	}

	protected final Biome getRoughBiome(BlockPos posIn, IChunk chunkIn) {
		return DefaultBiomeMagnifier.INSTANCE.getBiome(0L, posIn.getX(), posIn.getY(), posIn.getZ(), chunkIn.getBiomes());
	}

	protected final ChunkPos getSpawnChunk(ServerWorld worldIn) {
		ServerPlayerEntity player = worldIn.players().get(worldIn.random.nextInt(worldIn.players().size()));
		ChunkPos chunkPos = worldIn.getChunk(player.blockPosition()).getPos();
		int chunkX = chunkPos.x - 8 + worldIn.random.nextInt(17);
		int chunkZ = chunkPos.z - 8 + worldIn.random.nextInt(17);
		boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(worldIn) : chunkX, flag ? chunkZ + this.getChunkOffset(worldIn) : chunkZ);
		return chunkPos1;
	}

	protected final int getChunkOffset(ServerWorld worldIn) {
		int offSet = worldIn.random.nextInt(8) + 1;
		boolean flag = worldIn.random.nextBoolean();
		return flag ? offSet : -offSet;
	}

	protected final BlockPos getSpawnPos(ServerWorld worldIn, ChunkPos chunkPosIn) {
		int x = chunkPosIn.getMinBlockX() + worldIn.random.nextInt(16);
		int z = chunkPosIn.getMinBlockZ() + worldIn.random.nextInt(16);
		return new BlockPos(x, worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z), z);
	}

	@Override
	public String toString() {
		return this.invasionType.toString();
	}
}
