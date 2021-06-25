package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.theagameplayer.puresuffering.PSConfig;
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
	public static final ArrayList<MobEntity> INVASION_MOBS = new ArrayList<>();
	private final InvasionType invasionType;
	private final int severity;
	private final ArrayList<InvasionSpawnerEntity> spawnPotentials = new ArrayList<>();
	private InvasionSpawnerEntity nextSpawnData = new InvasionSpawnerEntity();
	private int spawnDelay;
	
	public Invasion(InvasionType invasionTypeIn, int severityIn) {
		this.invasionType = invasionTypeIn;
		this.severity = severityIn;
	}
	
	public InvasionType getType() {
		return this.invasionType;
	}
	
	public int getSeverity() {
		return this.severity;
	}
	
	public void tick(ServerWorld worldIn, ArrayList<Invasion> invasionListIn) {
		this.tickEntitySpawn(worldIn);
	}
	
	private final void tickEntitySpawn(ServerWorld worldIn) {
		INVASION_MOBS.removeIf(mobEntity -> {
			return mobEntity == null || !mobEntity.isAlive();
		});
		if (this.invasionType.getMobSpawnList() != null && INVASION_MOBS.size() <= PSConfig.COMMON.invasionMobCap.get()) {
			if (this.spawnDelay < 0) {
				this.delay(worldIn);
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
			if (this.invasionType.getMobSpawnList().isEmpty()) {
				BlockPos pos = this.getSpawnPos(worldIn, chunkPos);
				mobs = this.getRoughBiome(pos, worldIn.getChunk(pos)).getMobSettings().getMobs(EntityClassification.MONSTER);
				if (mobs.size() < 1) return;
			} else {
				mobs = this.invasionType.getMobSpawnList().get(this.severity - 1);
			}
			index = worldIn.random.nextInt(mobs.size());
			spawners = mobs.get(index);
			int groupSize = worldIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
			this.nextSpawnData.getTag().putString("id", ForgeRegistries.ENTITIES.getKey(spawners.type).toString());
			for(int count = 0; count < groupSize; ++count) {
				CompoundNBT compoundNBT = this.nextSpawnData.getTag();
				Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
				if (!optional.isPresent()) {
					this.delay(worldIn);
					return;
				}
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
								mobEntity.getPersistentData().putString("InvasionMob", this.invasionType.toString());
								mobEntity.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(entity.blockPosition()), SpawnReason.EVENT, (ILivingEntityData)null, (CompoundNBT)null);
								mobEntity.setTarget(worldIn.getNearestPlayer(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ(), Integer.MAX_VALUE, true));
								if (PSConfig.COMMON.shouldMobsSpawnWithMaxRange.get())
									mobEntity.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2048.0D);
								INVASION_MOBS.add(mobEntity);
							}
						}
					}
					if (!worldIn.tryAddFreshEntityWithPassengers(entity)) {
						this.delay(worldIn);
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
				this.delay(worldIn);
			}
		}
	}

	private void delay(ServerWorld worldIn) {
		int delay = this.invasionType.getTickDelay();
		this.spawnDelay = (delay * (this.invasionType.getMaxSeverity() + 1)) - (delay * this.severity);
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
		return this.invasionType.toString();
	}
}
