package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SeverityInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.data.AdditionalEntitySpawnData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData.MobTagData;
import dev.theagameplayer.puresuffering.network.InvasionMobParticlesPacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import dev.theagameplayer.puresuffering.registries.other.PSReloadListeners;
import dev.theagameplayer.puresuffering.util.list.SpawnPosChart;
import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.server.level.ServerLevel;

public final class Invasion implements InvasionTypeHolder {
	public static final int TRANSITION_TIME = 600;
	public static final int HALF_TRANSITION = TRANSITION_TIME/2;
	public static final String INVASION_MOB = PureSufferingMod.MODID.toUpperCase() + "InvasionMob";
	public static final String ANTI_GRIEF = PureSufferingMod.MODID.toUpperCase() + "AntiGrief";
	public static final String DESPAWN_LOGIC = PureSufferingMod.MODID.toUpperCase() + "DespawnLogic";
	private final ArrayList<MobInfo> invasionMobs;
	private final InvasionType invasionType;
	private final boolean isPrimary;
	private final int severity, mobCap;
	private final boolean shouldTick;
	private final boolean isNatural;
	private final long startTime;
	private final int index;
	private final SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
	private SpawnData nextSpawnData = new SpawnData();
	private int spawnDelay;

	public Invasion(final ServerLevel pLevel, final InvasionType pInvasionType, final int pSeverity, final boolean pIsPrimary, final boolean pIsNatural, final long pStartTime, final int pIndex) {
		final SeverityInfo info = pInvasionType.getSeverityInfo().get(pSeverity);
		final int pMobCap = Math.max(0, pIsPrimary ? PSGameRules.PRIMARY_INVASION_MOB_CAP.get(pLevel) : PSGameRules.SECONDARY_INVASION_MOB_CAP.get(pLevel));
		final int mobCap = info.getFixedMobCap() > 0 ? Mth.clamp(info.getFixedMobCap(), 0, pMobCap) : pMobCap;
		this.invasionMobs = new ArrayList<>(mobCap + 1);
		this.invasionType = pInvasionType;
		this.isPrimary = pIsPrimary;
		this.severity = pSeverity;
		this.mobCap = (int)(mobCap * info.getMobCapPercentage());
		this.shouldTick = info.getTickDelay() > -1 && (info.getMobSpawnList() != null || pInvasionType.getSpawningSystem() != SpawningSystem.DEFAULT);
		this.isNatural = pIsNatural;
		this.startTime = pStartTime;
		this.index = pIndex;
	}

	@Override
	public final InvasionType getType() {
		return this.invasionType;
	}

	public final boolean isPrimary() {
		return this.isPrimary;
	}

	public final int getSeverity() {
		return this.severity;
	}

	public final int getMobCap() {
		return this.mobCap;
	}

	public final SeverityInfo getSeverityInfo() {
		return this.invasionType.getSeverityInfo().get(this.severity);
	}

	public final void replaceMob(final Mob pMob, final int pIndex) {
		final MobInfo info = this.invasionMobs.get(pIndex);
		info.uuid = pMob.getUUID();
	}

	public final void splitMob(final List<Mob> pChildren) {
		for (final Mob child : pChildren)
			this.invasionMobs.add(new MobInfo(child.getUUID(), false));
	}

	public final void relocateMob(final int pIndex) {
		final MobInfo info = this.invasionMobs.get(pIndex);
		info.relocate = true;
	}

	public final int hasMob(final Mob pMob) {
		final UUID uuid = pMob.getUUID();
		for (int i = 0; i < this.invasionMobs.size(); ++i) {
			final MobInfo info = this.invasionMobs.get(i);
			if (uuid.equals(info.uuid)) return i;
		}
		return -1;
	}
	
	public final boolean hasSameInvasion(final Mob pMob) {
		final CompoundTag persistentData = pMob.getPersistentData();
		return persistentData.contains(INVASION_MOB) && persistentData.getInt(INVASION_MOB) == this.toExtendedString().hashCode();
	}

	public final boolean loadMob(final Mob pMob) {
		final CompoundTag persistentData = pMob.getPersistentData();
		if (this.invasionMobs.size() < this.mobCap && persistentData.contains(INVASION_MOB) && persistentData.getInt(INVASION_MOB) == this.toExtendedString().hashCode()) {
			this.invasionMobs.add(new MobInfo(pMob.getUUID(), false));
			return true;
		}
		return false;
	}

	public final void tick(final ServerLevel pLevel, final InvasionDifficulty pDifficulty, final int pTotalInvasions) {
		final ServerPlayer[] players = pLevel.players().stream().filter(player -> !player.isSpectator()).toArray(ServerPlayer[]::new);
		//Mob Relocation
		this.invasionMobs.removeIf(info -> {
			final Entity mob = pLevel.getEntity(info.uuid);
			if (info.relocate && mob != null) {
				if (players.length == 0) return mob == null || !mob.isAlive();
				final ServerPlayer player = players[pLevel.random.nextInt(players.length)];
				final ChunkPos chunkPos = this.getSpawnChunk(pLevel, player);
				final BlockPos spawnPos = this.getMobRelocatePos(pLevel, chunkPos, player, mob);
				if (mob != null && this.isValidLocation(pLevel, mob.getType(), spawnPos, mob.getPersistentData().contains(ANTI_GRIEF) && mob.getPersistentData().getBoolean(ANTI_GRIEF))) {
					PSPackets.sendToClientsIn(new InvasionMobParticlesPacket(pDifficulty, mob.position(), false), pLevel);
					mob.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), mob.getYRot(), mob.getXRot());
					if (!mob.isInWall()) {
						PSPackets.sendToClientsIn(new InvasionMobParticlesPacket(pDifficulty, mob.position(), true), pLevel);
						mob.getPersistentData().getIntArray(DESPAWN_LOGIC)[3] = 0; //Prevents mobs from speeding around the map needlessly.
					}
					info.relocate = false;
				}
			}
			return mob == null || !mob.isAlive();
		});
		//Spawn Mobs
		if (this.shouldTick && this.invasionMobs.size() < this.mobCap)
			this.tickMobSpawn(pLevel, players, pDifficulty, pTotalInvasions);
		//Spawn Additional Entities
		final AdditionalEntitySpawnData[] additionalEntities = this.getSeverityInfo().getAdditionalEntities();
		if (additionalEntities.length > 0) {
			final AdditionalEntitySpawnData spawnInfo = additionalEntities[pLevel.random.nextInt(additionalEntities.length)];
			if (players.length == 0) return;
			final int t = pLevel.random.nextInt(spawnInfo.getChance()) == 0 ? pLevel.random.nextIntBetweenInclusive(spawnInfo.getMinCount(), spawnInfo.getMaxCount()) : 0;
			if (t < 1) return;
			final ServerPlayer player = players[pLevel.random.nextInt(players.length)];
			final ChunkPos chunkPos = this.getSpawnChunk(pLevel, player);
			for (int c = 0; c < t; ++c)
				this.spawnClusterEntity(this.getEntitySpawnPos(pLevel, chunkPos, player, spawnInfo.getEntityType(), spawnInfo.isSurfaceSpawn()), pLevel, spawnInfo.getEntityType());
		}
	}

	private final void tickMobSpawn(final ServerLevel pLevel, final ServerPlayer[] pPlayers, final InvasionDifficulty pDifficulty, final int pTotalInvasions) {
		//Delay check
		if (this.spawnDelay < 0)
			this.delay(pLevel, pTotalInvasions);
		if (this.spawnDelay > 0) {
			--this.spawnDelay;
			return;
		}
		//Get Mobs
		if (pPlayers.length == 0) return;
		final ServerPlayer player = pPlayers[pLevel.random.nextInt(pPlayers.length)];
		final ChunkPos chunkPos = this.getSpawnChunk(pLevel, player);
		if (chunkPos == null || !pLevel.isLoaded(chunkPos.getWorldPosition())) return;
		final List<InvasionSpawnerData> mobs = this.getMobSpawnList(pLevel, chunkPos, player);
		if (mobs.isEmpty()) return;
		//Spawn Mob Cluster (Different Mobs)
		boolean flag1 = false;
		final int clusterSize = pLevel.random.nextInt(this.getSeverityInfo().getClusterSize()) + 1;
		for (int cluster = 0; cluster < clusterSize && this.invasionMobs.size() < this.mobCap; ++cluster) {
			//Spawn Mob Group (Same Mob)
			final InvasionSpawnerData spawners = mobs.get(pLevel.random.nextInt(mobs.size()));
			final int groupSize = pLevel.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
			this.nextSpawnData.getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(spawners.type).toString());
			final CompoundTag compoundTag = this.nextSpawnData.getEntityToSpawn();
			final Optional<EntityType<?>> optional = EntityType.by(compoundTag);
			if (!optional.isPresent()) {
				this.delay(pLevel, pTotalInvasions);
				return;
			}
			for (int count = 0; count < groupSize && this.invasionMobs.size() < this.mobCap; ++count) {
				//Spawn Entity
				final BlockPos spawnPos = this.getMobSpawnPos(pLevel, chunkPos, player, spawners.type, compoundTag);
				if (this.isValidSpawn(pLevel, optional.get(), spawnPos, spawners)) {
					final Entity entity = EntityType.loadEntityRecursive(compoundTag, pLevel, e -> {
						e.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), e.getYRot(), e.getXRot());
						return e;
					});
					if (entity == null || entity.isInWall()) {
						this.delay(pLevel, pTotalInvasions);
						return;
					}
					entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), pLevel.random.nextFloat() * 360.0F, 0.0F);
					if (entity instanceof Mob mob) {
						if (!EventHooks.checkSpawnPosition(mob, pLevel, MobSpawnType.EVENT)) continue;
						if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", Tag.TAG_STRING))
							this.spawnInvasionMob(pLevel, pDifficulty, mob, spawners.ignoreSpawnRules, spawners.forceDespawn || PSGameRules.MOBS_DIE_AT_END_OF_INVASIONS.get(pLevel), spawners.nbtTags, spawners.persistentTags);
					}
					if (!pLevel.tryAddFreshEntityWithPassengers(entity)) {
						this.delay(pLevel, pTotalInvasions);
						return;
					}
					flag1 = true;
				}
			}
		}
		if (flag1)
			this.delay(pLevel, pTotalInvasions);
		return;
	}

	private final boolean isValidSpawn(final ServerLevel pLevel, final EntityType<?> pEntityType, final BlockPos pPos, final InvasionSpawnerData pSpawners) {
		if (pSpawners.acceptableBiomes.length == 0) return this.isValidLocation(pLevel, pEntityType, pPos, pSpawners.ignoreSpawnRules);
		final ResourceLocation biomeLoc = pLevel.getBiome(pPos).getKey().location();
		for (final ResourceLocation loc : pSpawners.acceptableBiomes) {
			if (loc.equals(biomeLoc)) return this.isValidLocation(pLevel, pEntityType, pPos, pSpawners.ignoreSpawnRules);
		}
		return false;
	}
	
	@SuppressWarnings("unchecked") //It is checked ;)
	private final boolean isValidLocation(final ServerLevel pLevel, final EntityType<?> pEntityType, final BlockPos pPos, final boolean pIgnoreSpawnRules) {
		final boolean flag = pPos != null && pEntityType.getCategory() == MobCategory.MONSTER && SpawnPlacements.getPlacementType(pEntityType).isSpawnPositionOk(pLevel, pPos, pEntityType);
		if (pIgnoreSpawnRules) return flag && Mob.checkMobSpawnRules((EntityType<? extends Mob>)pEntityType, pLevel, MobSpawnType.EVENT, pPos, pLevel.getRandom());
		return flag && SpawnPlacements.checkSpawnRules(pEntityType, pLevel, MobSpawnType.EVENT, pPos, pLevel.getRandom());
	}

	private final void spawnClusterEntity(final BlockPos pPos, final ServerLevel pLevel, final EntityType<?> pEntityType) {
		if (!Level.isInSpawnableBounds(pPos)) return;
		final CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(pEntityType).toString());
		final Entity entity = EntityType.loadEntityRecursive(compoundTag, pLevel, e -> {
			e.moveTo(pPos.getX(), pPos.getY(), pPos.getZ(), e.getYRot(), e.getXRot());
			return e;
		});
		if (entity == null) return;
		entity.getPersistentData().putBoolean(ANTI_GRIEF, false);
		if (entity instanceof Mob mob)
			EventHooks.finalizeMobSpawn(mob, pLevel, pLevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, null);
		pLevel.tryAddFreshEntityWithPassengers(entity);
	}

	private final void spawnInvasionMob(final ServerLevel pLevel, final InvasionDifficulty pDifficulty, final Mob pMob, final boolean pIgnoreSpawnRules, final boolean pForceDespawn, final MobTagData[] pNBTTags, final MobTagData[] pPersistentTags) {
		boolean hyperCharged = PSGameRules.HYPER_CHARGE.get(pLevel) && !PSConfigValues.common.hyperChargeBlacklist.contains(pMob.getType().getDescriptionId()) && (pDifficulty.isHyper() || pLevel.random.nextDouble() < PSConfigValues.common.hyperChargeChance * (double)(this.severity + 1)/this.invasionType.getMaxSeverity());
		final CompoundTag persistentData = pMob.getPersistentData();
		persistentData.putInt(INVASION_MOB, this.toExtendedString().hashCode());
		persistentData.putBoolean(ANTI_GRIEF, pIgnoreSpawnRules);
		persistentData.putIntArray(DESPAWN_LOGIC, new int[pForceDespawn ? 6 : 4]);
		if (pForceDespawn) persistentData.getIntArray(DESPAWN_LOGIC)[5] = 100 + pLevel.random.nextInt(101);
		if (pMob instanceof PSInvasionMob invasionMob) {
			invasionMob.applyNBTTags(pNBTTags);
			if (hyperCharged) invasionMob.psSetHyperCharge(pDifficulty.getHyperCharge(pLevel, this.invasionType.getTier(), this.isNatural));
		}
		for (final MobTagData tag : pPersistentTags) tag.addTagToMob(persistentData);
		EventHooks.finalizeMobSpawn(pMob, pLevel, pLevel.getCurrentDifficultyAt(pMob.blockPosition()), MobSpawnType.EVENT, null);
		this.invasionMobs.add(new MobInfo(pMob.getUUID(), false));
		PSPackets.sendToClientsIn(new InvasionMobParticlesPacket(pDifficulty, pMob.position()), pLevel);
		pMob.spawnAnim();
	}

	private final void delay(final ServerLevel pLevel, final int pTotalInvasions) {
		this.spawnDelay = this.getSeverityInfo().getTickDelay()/pTotalInvasions;
		this.spawnPotentials.getRandom(pLevel.random).ifPresent(entry -> {
			this.nextSpawnData = entry.data();
		});
	}

	private final ArrayList<InvasionSpawnerData> getMobSpawnList(final ServerLevel pLevel, final ChunkPos pChunkPos, final ServerPlayer pPlayer) {
		final ArrayList<InvasionSpawnerData> originalList = new ArrayList<>(List.of(this.getSeverityInfo().getMobSpawnList()));
		switch (this.invasionType.getSpawningSystem()) {
		case DEFAULT: break;
		case BIOME_BOOSTED: {
			final BlockPos biomePos = this.getEntitySpawnPos(pLevel, pChunkPos, pPlayer, null, false);
			originalList.addAll(this.getBiomeSpawnList(biomePos, pLevel.getChunk(biomePos)));
			break;
		}
		case BIOME_MIXED: {
			originalList.addAll(this.getMixedSpawnList(pLevel));
			break;
		}
		}
		final ArrayList<InvasionSpawnerData> newList = new ArrayList<>();
		if (!originalList.isEmpty()) {
			for (final InvasionSpawnerData spawners : originalList) {
				for (int w = 0, tw = spawners.getWeight().asInt(); w < tw; ++w)
					newList.add(spawners);
			}
		}
		return newList;
	}

	private final ArrayList<InvasionSpawnerData> getBiomeSpawnList(final BlockPos pPos, final ChunkAccess pChunk) {
		final ArrayList<InvasionSpawnerData> spawners = InvasionSpawnerData.convertSpawners(pChunk.getNoiseBiome(pPos.getX(), pPos.getY(), pPos.getZ()).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap());
		spawners.removeIf(spawner -> {
			final ResourceLocation name = BuiltInRegistries.ENTITY_TYPE.getKey(spawner.type);
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ArrayList<InvasionSpawnerData> getMixedSpawnList(final ServerLevel pLevel) {
		final Optional<Holder.Reference<LevelStem>> optional = pLevel.registryAccess().registryOrThrow(Registries.LEVEL_STEM).getRandom(pLevel.random);
		if (!optional.isPresent() || optional.isEmpty()) return new ArrayList<>();
		final ArrayList<Holder<Biome>> biomes = new ArrayList<>(optional.get().value().generator().getBiomeSource().possibleBiomes());
		final ArrayList<InvasionSpawnerData> spawners = biomes.size() > 0 ? InvasionSpawnerData.convertSpawners(biomes.get(pLevel.random.nextInt(biomes.size())).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap()) : new ArrayList<>();
		spawners.removeIf(spawner -> {
			final ResourceLocation name = BuiltInRegistries.ENTITY_TYPE.getKey(spawner.type);
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ChunkPos getSpawnChunk(final ServerLevel pLevel, final ServerPlayer pPlayer) {
		final ChunkPos chunkPos = pLevel.getChunk(pPlayer.blockPosition()).getPos();
		final int chunkX = chunkPos.x - 8 + pLevel.random.nextInt(17);
		final int chunkZ = chunkPos.z - 8 + pLevel.random.nextInt(17);
		final boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		final ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(pLevel) : chunkX, flag ? chunkZ + this.getChunkOffset(pLevel) : chunkZ);
		return chunkPos1;
	}

	private final int getChunkOffset(final ServerLevel pLevel) {
		final int offSet = pLevel.random.nextInt(8) + 1;
		return pLevel.random.nextBoolean() ? offSet : -offSet;
	}

	private final Path testPath(final PathNavigation pNav, final Player pPlayer, final int pAccuracy, final BlockPos pPos) {
		final BlockPos playerPos = pPlayer.blockPosition();
		final float range = 0; //(float)pPos.distSqr(playerPos); This is backup method if this fails
		if (pNav.mob.getY() < (double)pNav.level.getMinBuildHeight()) {
			return null;
		} else if (pNav.path != null && !pNav.path.isDone() && playerPos.equals(pNav.targetPos)) {
			return null;
		} else {
			pNav.level.getProfiler().push("pathfind");
			final BlockPos blockPos = pNav.mob.blockPosition().above();
			final int i = (int)(range + 16.0F);
			final PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(pNav.level, blockPos.offset(-i, -i, -i), blockPos.offset(i, i, i));
			final Path path = pNav.pathFinder.findPath(pathNavigationRegion, pNav.mob, ImmutableSet.of(playerPos), range, pAccuracy, pNav.maxVisitedNodesMultiplier);
			pNav.level.getProfiler().pop();
			if (path != null && path.getTarget() != null) {
				pNav.targetPos = path.getTarget();
				pNav.reachRange = pAccuracy;
				pNav.resetStuckTimeout();
			}
			return path;
		}
	}

	private final BlockPos getMobSpawnPos(final ServerLevel pLevel, final ChunkPos pChunkPos, final ServerPlayer pPlayer, final EntityType<?> pEntityType, final CompoundTag pCompoundTag) {
		final int x = pChunkPos.getMinBlockX() + pLevel.random.nextInt(16);
		final int z = pChunkPos.getMinBlockZ() + pLevel.random.nextInt(16);
		final ArrayList<Integer> posList = new ArrayList<>();
		for (int y = pPlayer.getBlockY() - 96, yMax = pPlayer.getBlockY() + 97; y < yMax; ++y) {
			final BlockPos pos = new BlockPos(x, y, z);
			if (pEntityType == null ? !pLevel.getBlockState(pos).isAir() && pLevel.getBlockState(pos.above()).isAir() : SpawnPlacements.getPlacementType(pEntityType).isSpawnPositionOk(pLevel, pos, pEntityType)) {
				if (pCompoundTag == null) posList.add(pos.getY());
				final Entity entity = EntityType.loadEntityRecursive(pCompoundTag, pLevel, e -> {
					e.moveTo(pos.getX(), pos.getY(), pos.getZ(), e.getYRot(), e.getXRot());
					return e;
				});
				if (entity instanceof Mob mob) {
					final PathNavigation navigation = mob.getNavigation();
					if (navigation != null && this.testPath(navigation, pPlayer, 0, pos) != null)
						posList.add(pos.getY());
				}
				entity.discard();
			}
		}
		return new BlockPos(x, SpawnPosChart.getYInRange(posList, pPlayer.getBlockY(), pLevel.random.nextFloat()), z);
	}

	private final BlockPos getMobRelocatePos(final ServerLevel pLevel, final ChunkPos pChunkPos, final ServerPlayer pPlayer, final Entity pEntity) {
		final int x = pChunkPos.getMinBlockX() + pLevel.random.nextInt(16);
		final int z = pChunkPos.getMinBlockZ() + pLevel.random.nextInt(16);
		final ArrayList<Integer> posList = new ArrayList<>();
		for (int y = pPlayer.getBlockY() - 96, yMax = pPlayer.getBlockY() + 97; y < yMax; ++y) {
			final BlockPos pos = new BlockPos(x, y, z);
			if (pEntity == null ? !pLevel.getBlockState(pos).isAir() && pLevel.getBlockState(pos.above()).isAir() : SpawnPlacements.getPlacementType(pEntity.getType()).isSpawnPositionOk(pLevel, pos, pEntity.getType())) {
				if (pEntity instanceof Mob mob) {
					final PathNavigation navigation = mob.getNavigation();
					if (navigation != null && this.testPath(navigation, pPlayer, 0, pos) != null)
						posList.add(pos.getY());
				}
			}
		}
		return new BlockPos(x, SpawnPosChart.getYInRange(posList, pPlayer.getBlockY(), pLevel.random.nextFloat()), z);
	}

	private final BlockPos getEntitySpawnPos(final ServerLevel pLevel, final ChunkPos pChunkPos, final ServerPlayer pPlayer, final EntityType<?> pEntityType, final boolean pIsSurface) {
		final int x = pChunkPos.getMinBlockX() + pLevel.random.nextInt(16);
		final int z = pChunkPos.getMinBlockZ() + pLevel.random.nextInt(16);
		if (pIsSurface) return new BlockPos(x, pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
		final ArrayList<Integer> posList = new ArrayList<>();
		for (int y = pPlayer.getBlockY() - 96, yMax = pPlayer.getBlockY() + 97; y < yMax; ++y) {
			final BlockPos pos = new BlockPos(x, y, z);
			if (pEntityType == null ? !pLevel.getBlockState(pos).isAir() && pLevel.getBlockState(pos.above()).isAir() : SpawnPlacements.getPlacementType(pEntityType).isSpawnPositionOk(pLevel, pos, pEntityType))
				posList.add(pos.getY());
		}
		return new BlockPos(x, SpawnPosChart.getYInRange(posList, pPlayer.getBlockY(), pLevel.random.nextFloat()), z);
	}

	public static final Invasion load(final ServerLevel pLevel, final CompoundTag pNbt) {
		if (!PSReloadListeners.getInvasionTypeManager().verifyInvasion(pNbt.getString("InvasionType"))) return null;
		final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(pNbt.getString("InvasionType")));
		final Invasion invasion = new Invasion(pLevel, invasionType, pNbt.getInt("Severity"), pNbt.getBoolean("IsPrimary"), pNbt.getBoolean("IsNatural"), pNbt.getLong("StartTime"), pNbt.getInt("Index"));
		invasion.spawnDelay = pNbt.getInt("SpawnDelay");
		return invasion;
	}

	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		nbt.putString("InvasionType", this.invasionType.toString());
		nbt.putInt("Severity", this.severity);
		nbt.putBoolean("IsPrimary", this.isPrimary);
		nbt.putBoolean("IsNatural", this.isNatural);
		nbt.putLong("StartTime", this.startTime);
		nbt.putInt("Index", this.index);
		nbt.putInt("SpawnDelay", this.spawnDelay);
		return nbt;
	}

	@Override
	public final String toString() {
		return this.invasionType.toString();
	}

	@Override
	public final boolean equals(final Object pObj) {
		if (pObj instanceof Invasion inv)
			return this.toExtendedString().equals(inv.toExtendedString());
		return false;
	}

	public final String toExtendedString() {
		return "InvasionType: " + this.invasionType.toString() + ", Severity: " + this.severity + ", IsPrimary: " + this.isPrimary + ", IsNatural: " + this.isNatural + ", StartTime: " + this.startTime + ", Index: " + this.index;
	}

	private static final class MobInfo {
		private UUID uuid;
		private boolean relocate;

		private MobInfo(final UUID pUUID, final Boolean pRelocate) {
			this.uuid = pUUID;
			this.relocate = pRelocate;
		}
	}

	public static final class BuildInfo implements InvasionTypeHolder {
		private final InvasionType invasionType;
		private final int severity;
		private final boolean isPrimary;

		public BuildInfo(final InvasionType pInvasionType, final int pSeverity, final boolean pIsPrimary) {
			this.invasionType = pInvasionType;
			this.severity = pSeverity;
			this.isPrimary = pIsPrimary;
		}

		@Override
		public final InvasionType getType() {
			return this.invasionType;
		}

		public final boolean isPrimary() {
			return this.isPrimary;
		}

		public static final BuildInfo load(final ServerLevel pLevel, final CompoundTag pNbt) {
			final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(pNbt.getString("InvasionType")));
			final int severity = pNbt.getInt("Severity");
			final boolean isPrimary = pNbt.getBoolean("IsPrimary");
			return new BuildInfo(invasionType, severity, isPrimary);
		}

		public final CompoundTag save() {
			final CompoundTag nbt = new CompoundTag();
			nbt.putString("InvasionType", this.invasionType.toString());
			nbt.putInt("Severity", this.severity);
			nbt.putBoolean("IsPrimary", this.isPrimary);
			return nbt;
		}

		public final Invasion build(final ServerLevel pLevel, final int pIndex) {
			return new Invasion(pLevel, this.invasionType, this.severity, this.isPrimary, false, pLevel.getDayTime(), pIndex);
		}
	}
}
