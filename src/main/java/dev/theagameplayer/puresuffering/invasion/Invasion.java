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
import dev.theagameplayer.puresuffering.world.entity.PSHyperCharge;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public final class Invasion {
	private static final ArrayList<Biome> MIXED_BIOMES = new ArrayList<>(ForgeRegistries.BIOMES.getValues());
	private final ArrayList<UUID> invasionMobs = new ArrayList<>();
	private final InvasionType invasionType;
	private final int severity;
	private final boolean isPrimary;
	private final HyperType hyperType;
	private final int mobCap;
	private final boolean shouldTick;
	private final SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
	private SpawnData nextSpawnData = new SpawnData();
	private int spawnDelay;

	public Invasion(final InvasionType invasionTypeIn, final int severityIn, final boolean isPrimaryIn, final HyperType hyperTypeIn) {
		final SeverityInfo info = invasionTypeIn.getSeverityInfo().get(severityIn);
		final int mobCap = isPrimaryIn ? PSConfigValues.common.primaryInvasionMobCap : PSConfigValues.common.secondaryInvasionMobCap;
		this.invasionType = invasionTypeIn;
		this.severity = severityIn;
		this.isPrimary  = isPrimaryIn;
		this.hyperType = hyperTypeIn;
		this.mobCap = (int)(mobCap * info.getMobCapPercentage()) + 1;
		this.shouldTick = info.getTickDelay() > -1 && (invasionTypeIn.getSpawningSystem() != SpawningSystem.DEFAULT || (info.getMobSpawnList() != null && invasionTypeIn.getSpawningSystem() == SpawningSystem.DEFAULT));
	}

	public final InvasionType getType() {
		return this.invasionType;
	}

	public final int getSeverity() {
		return this.severity;
	}

	public final boolean isPrimary() {
		return this.isPrimary;
	}
	
	public final HyperType getHyperType() {
		return this.hyperType;
	}

	public final int getMobCap() {
		return this.mobCap;
	}

	public final SeverityInfo getSeverityInfo() {
		return this.invasionType.getSeverityInfo().get(this.severity);
	}

	public final void tick(final ServerLevel worldIn) {
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

	private final void tickEntitySpawn(final ServerLevel levelIn) {
		if (this.invasionMobs.size() < this.mobCap) {
			//Delay check
			if (this.spawnDelay < 0) {
				this.delay(levelIn);
			}
			if (this.spawnDelay > 0) {
				--this.spawnDelay;
				return;
			}
			//Get Mobs
			if (levelIn.players().size() < 1) return;
			boolean flag1 = false;
			final ChunkPos chunkPos = this.getSpawnChunk(levelIn);
			final List<MobSpawnSettings.SpawnerData> mobs = this.getMobSpawnList(levelIn, chunkPos);
			if (mobs.isEmpty()) return;
			//Spawn Mob Cluster (Different Mobs)
			final int clusterSize = levelIn.random.nextInt(this.getSeverityInfo().getClusterSize()) + 1;
			for (int cluster = 0; cluster < clusterSize && this.invasionMobs.size() < this.mobCap; cluster++) {
				//Spawn Cluster Entities (Entities to be summoned before a mob group is spawned)
				final List<ClusterEntitySpawnData> clusterEntities = this.getSeverityInfo().getClusterEntities();
				if (!clusterEntities.isEmpty() && cluster == 0) {
					final ClusterEntitySpawnData spawnInfo = clusterEntities.get(levelIn.random.nextInt(clusterEntities.size()));
					final int t = levelIn.random.nextInt(spawnInfo.getChance()) == 0 ? levelIn.random.nextIntBetweenInclusive(spawnInfo.getMinCount(), spawnInfo.getMaxCount()) : 0;
					for (int c = 0; c < t; c++)
						this.spawnClusterEntity(this.getSpawnPos(levelIn, chunkPos, false), levelIn, spawnInfo.getEntityType());
				}
				//Spawn Mob Group (Same Mob)
				final MobSpawnSettings.SpawnerData spawners = mobs.get(levelIn.random.nextInt(mobs.size()));
				final int groupSize = levelIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
				this.nextSpawnData.getEntityToSpawn().putString("id", ForgeRegistries.ENTITY_TYPES.getKey(spawners.type).toString());
				final CompoundTag compoundNBT = this.nextSpawnData.getEntityToSpawn();
				final Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
				if (!optional.isPresent()) {
					this.delay(levelIn);
					return;
				}
				for(int count = 0; count < groupSize && this.invasionMobs.size() < this.mobCap; ++count) {
					//Spawn Entity
					final BlockPos spawnPos = this.getSpawnPos(levelIn, chunkPos, false);
					if (spawnPos != null && SpawnPlacements.checkSpawnRules(optional.get(), levelIn, MobSpawnType.EVENT, spawnPos, levelIn.getRandom())) {
						final Entity entity = EntityType.loadEntityRecursive(compoundNBT, levelIn, e -> {
							e.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), e.getYRot(), e.getXRot());
							return e;
						});
						if (entity == null) {
							this.delay(levelIn);
							return;
						}
						entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), levelIn.random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof Mob) {
							final Mob mobEntity = (Mob)entity;
							if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
								if (!ForgeEventFactory.doSpecialSpawn(mobEntity, (LevelAccessor)levelIn, (float)mobEntity.getX(), (float)mobEntity.getY(), (float)mobEntity.getZ(), null, MobSpawnType.EVENT)) {
									this.spawnInvasionMob(levelIn, mobEntity);
								}
							}
						}
						if (!levelIn.tryAddFreshEntityWithPassengers(entity)) {
							this.delay(levelIn);
							return;
						}
						flag1 = true;
					}
				}
			}
			if (flag1) {
				this.delay(levelIn);
			}
		}
	}

	private final void spawnClusterEntity(final BlockPos posIn, final ServerLevel levelIn, final EntityType<?> entityTypeIn) {
		if (Level.isInSpawnableBounds(posIn)) {
			final CompoundTag compoundTag = new CompoundTag();
			compoundTag.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entityTypeIn).toString());
			final Entity entity = EntityType.loadEntityRecursive(compoundTag, levelIn, e -> {
				e.moveTo(posIn.getX(), posIn.getY(), posIn.getZ(), e.getYRot(), e.getXRot());
				return e;
			});
			if (entity != null) {
				if (entity instanceof Mob)
					((Mob)entity).finalizeSpawn(levelIn, levelIn.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, null, null);
				levelIn.tryAddFreshEntityWithPassengers(entity);
			}
		}
	}

	private final void spawnInvasionMob(final ServerLevel worldIn, final Mob mobEntityIn) {
		final boolean hyperCharged = PSConfigValues.common.hyperCharge && PSConfigValues.common.maxHyperCharge > 1 && !PSConfigValues.common.hyperChargeBlacklist.contains(mobEntityIn.getType().getDescriptionId()) && this.hyperType != HyperType.DEFAULT ? true : worldIn.random.nextInt(PSConfigValues.common.hyperChargeChance + 1) <= this.severity;
		mobEntityIn.getPersistentData().putString("InvasionMob", this.invasionType.getId().toString());
		mobEntityIn.getPersistentData().putBoolean("AntiGrief", worldIn.dimensionType().hasFixedTime());
		mobEntityIn.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(mobEntityIn.blockPosition()), MobSpawnType.EVENT, null, null);
		if (hyperCharged && mobEntityIn instanceof PSHyperCharge) {
			final int hyperCharge = this.hyperType != HyperType.DEFAULT ? (this.hyperType == HyperType.MYSTERY ? PSConfigValues.common.maxHyperCharge : worldIn.random.nextInt(PSConfigValues.common.maxHyperCharge > 1 ? PSConfigValues.common.maxHyperCharge - 1 : 1) + 1) : worldIn.random.nextInt(worldIn.random.nextInt(PSConfigValues.common.maxHyperCharge - 1) + 1) + 1;
			((PSHyperCharge)mobEntityIn).psSetHyperCharge(hyperCharge);
			for (final AttributeInstance attribute : mobEntityIn.getAttributes().getSyncableAttributes())
				attribute.setBaseValue(attribute.getBaseValue() * (1.0D + 0.2D * hyperCharge)); //TODO: Keep an eye out for compatability conflicts
		}
		if (PSConfigValues.common.shouldMobsSpawnWithMaxRange)
			mobEntityIn.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(2048.0D);
		this.invasionMobs.add(mobEntityIn.getUUID());
		worldIn.levelEvent(2004, mobEntityIn.blockPosition(), 0); //Mob Spawn Particles
		mobEntityIn.spawnAnim();
	}
	
	private final void delay(final ServerLevel worldIn) {
		this.spawnDelay = this.getSeverityInfo().getTickDelay();
		this.spawnPotentials.getRandom(worldIn.random).ifPresent(entry -> {
			this.nextSpawnData = entry.getData();
		});
	}

	private final List<MobSpawnSettings.SpawnerData> getMobSpawnList(final ServerLevel worldIn, final ChunkPos chunkPosIn) {
		final BlockPos biomePos = this.getSpawnPos(worldIn, chunkPosIn, true);
		final ArrayList<MobSpawnSettings.SpawnerData> originalList = new ArrayList<>(this.getSeverityInfo().getMobSpawnList());
		switch (this.invasionType.getSpawningSystem()) {
		case DEFAULT: break;
		case BIOME_BOOSTED:
			originalList.addAll(this.getBiomeSpawnList(biomePos, worldIn.getChunk(biomePos)));
			break;
		case BIOME_MIXED:
			originalList.addAll(this.getMixedSpawnList(worldIn));
			break;
		}
		final ArrayList<MobSpawnSettings.SpawnerData> newList = new ArrayList<>();
		if (!originalList.isEmpty()) {
			for (final MobSpawnSettings.SpawnerData spawners : originalList) {
				for (int w = 0; w < spawners.getWeight().asInt(); w++)
					newList.add(spawners);
			}
		}
		return newList;
	}

	private final ArrayList<MobSpawnSettings.SpawnerData> getBiomeSpawnList(final BlockPos posIn, final ChunkAccess chunkIn) {
		final ArrayList<MobSpawnSettings.SpawnerData> spawners = new ArrayList<>(chunkIn.getNoiseBiome(posIn.getX(), posIn.getY(), posIn.getZ()).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap());
		spawners.removeIf(spawner -> {
			final ResourceLocation name = spawner.type.getDefaultLootTable();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ArrayList<MobSpawnSettings.SpawnerData> getMixedSpawnList(final ServerLevel worldIn) {
		final ArrayList<MobSpawnSettings.SpawnerData> spawners = new ArrayList<>(MIXED_BIOMES.get(worldIn.random.nextInt(MIXED_BIOMES.size())).getMobSettings().getMobs(MobCategory.MONSTER).unwrap());
		spawners.removeIf(spawner -> {
			final ResourceLocation name = spawner.type.getDefaultLootTable();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	protected final ChunkPos getSpawnChunk(final ServerLevel worldIn) {
		final ServerPlayer player = worldIn.players().get(worldIn.random.nextInt(worldIn.players().size()));
		final ChunkPos chunkPos = worldIn.getChunk(player.blockPosition()).getPos();
		final int chunkX = chunkPos.x - 8 + worldIn.random.nextInt(17);
		final int chunkZ = chunkPos.z - 8 + worldIn.random.nextInt(17);
		final boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		final ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(worldIn) : chunkX, flag ? chunkZ + this.getChunkOffset(worldIn) : chunkZ);
		return chunkPos1;
	}

	private final int getChunkOffset(final ServerLevel worldIn) {
		final int offSet = worldIn.random.nextInt(8) + 1;
		return worldIn.random.nextBoolean() ? offSet : -offSet;
	}

	private final BlockPos getSpawnPos(final ServerLevel worldIn, final ChunkPos chunkPosIn, final boolean biomeCheckIn) {
		final int x = chunkPosIn.getMinBlockX() + worldIn.random.nextInt(16);
		final int z = chunkPosIn.getMinBlockZ() + worldIn.random.nextInt(16);
		final int surface = worldIn.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		if (biomeCheckIn || (!worldIn.dimensionType().hasCeiling() && worldIn.random.nextBoolean()))
			return new BlockPos(x, surface, z);
		final BlockState air = Blocks.AIR.defaultBlockState();
		final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
		final ArrayList<BlockPos> potentialPos = new ArrayList<>();
		for (int y = 0; y < surface; y++) {
			final BlockPos pos = new BlockPos(x, y, z);
			if (worldIn.getBlockState(pos) != air && worldIn.getBlockState(pos) != bedrock && worldIn.getBlockState(pos.above()) == air)
				potentialPos.add(pos.above());
		}
		return potentialPos.size() > 0 ? potentialPos.get(worldIn.random.nextInt(potentialPos.size())) : new BlockPos(x, surface, z);
	}

	@Nullable
	public static final Invasion load(final CompoundTag nbtIn) {
		if (BaseEvents.getInvasionTypeManager().verifyInvasion(nbtIn.getString("InvasionType"))) {
			final InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(nbtIn.getString("InvasionType")));
			final Invasion invasion = new Invasion(invasionType, nbtIn.getInt("Severity"), nbtIn.getBoolean("IsPrimary"), PSConfigValues.common.hyperInvasions ? HyperType.values()[nbtIn.getInt("HyperType")] : HyperType.DEFAULT);
			final CompoundTag invasionMobs = nbtIn.getCompound("InvasionMobs");
			for (final String name : invasionMobs.getAllKeys())
				invasion.invasionMobs.add(invasionMobs.getUUID(name));
			return invasion;
		}
		return null;
	}

	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final CompoundTag mobs = new CompoundTag();
		for (final UUID id : this.invasionMobs)
			mobs.putUUID(id.toString(), id);
		nbt.put("InvasionMobs", mobs);
		nbt.putString("InvasionType", this.invasionType.toString());
		nbt.putInt("Severity", this.severity);
		nbt.putBoolean("IsPrimary", this.isPrimary);
		nbt.putInt("HyperType", this.hyperType.ordinal());
		return nbt;
	}

	@Override
	public final String toString() {
		return this.invasionType.toString();
	}
}
