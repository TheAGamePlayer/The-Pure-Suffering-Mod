package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SeverityInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType.SpawningSystem;
import dev.theagameplayer.puresuffering.invasion.data.AdditionalEntitySpawnData;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.InvasionMobParticlesPacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSReloadListeners;
import dev.theagameplayer.puresuffering.util.list.SpawnPosChart;
import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;

public final class Invasion implements InvasionTypeHolder {
	public static final int TRANSITION_TIME = 600;
	public static final int HALF_TRANSITION = TRANSITION_TIME/2;
	public static final String INVASION_MOB = PureSufferingMod.MODID.toUpperCase() + "InvasionMob";
	public static final String ANTI_GRIEF = PureSufferingMod.MODID.toUpperCase() + "AntiGrief";
	public static final String DESPAWN_LOGIC = PureSufferingMod.MODID.toUpperCase() + "DespawnLogic";
	private final ArrayList<MobInfo> invasionMobs = new ArrayList<>();
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

	public Invasion(final ServerLevel levelIn, final InvasionType invasionTypeIn, final int severityIn, final boolean isPrimaryIn, final boolean isNaturalIn, final long startTimeIn, final int indexIn) {
		final SeverityInfo info = invasionTypeIn.getSeverityInfo().get(severityIn);
		final int pMobCap = Math.max(0, isPrimaryIn ? PSGameRules.PRIMARY_INVASION_MOB_CAP.get(levelIn) : PSGameRules.SECONDARY_INVASION_MOB_CAP.get(levelIn));
		final int mobCap = info.getFixedMobCap() > 0 ? Mth.clamp(info.getFixedMobCap(), 0, pMobCap) : pMobCap;
		this.invasionType = invasionTypeIn;
		this.isPrimary = isPrimaryIn;
		this.severity = severityIn;
		this.mobCap = (int)(mobCap * info.getMobCapPercentage());
		this.shouldTick = info.getTickDelay() > -1 && (info.getMobSpawnList() != null || invasionTypeIn.getSpawningSystem() != SpawningSystem.DEFAULT);
		this.isNatural = isNaturalIn;
		this.startTime = startTimeIn;
		this.index = indexIn;
	}

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

	public final void replaceMob(final Mob oldMobIn, final Mob mobIn) {
		final UUID uuid = oldMobIn.getUUID();
		for (int i = 0; i < this.invasionMobs.size(); i++) {
			final MobInfo info = this.invasionMobs.get(i);
			if (uuid.equals(info.uuid)) {
				info.uuid = mobIn.getUUID();
				return;
			}
		}
	}

	public final void relocateMob(final Mob mobIn) {
		final UUID uuid = mobIn.getUUID();
		for (int i = 0; i < this.invasionMobs.size(); i++) {
			final MobInfo info = this.invasionMobs.get(i);
			if (uuid.equals(info.uuid)) {
				info.relocate = true;
				return;
			}
		}
	}

	public final boolean hasMob(final Mob mobIn) {
		final UUID uuid = mobIn.getUUID();
		for (int i = 0; i < this.invasionMobs.size(); i++) {
			final MobInfo info = this.invasionMobs.get(i);
			if (uuid.equals(info.uuid)) return true;
		}
		return false;
	}
	
	public final boolean loadMob(final Mob mobIn) {
		final CompoundTag persistentData = mobIn.getPersistentData();
		if (this.invasionMobs.size() < this.mobCap && persistentData.contains(INVASION_MOB) && persistentData.getInt(INVASION_MOB) == this.hashCode()) {
			this.invasionMobs.add(new MobInfo(mobIn.getUUID(), false));
			return true;
		}
		return false;
	}

	public final void tick(final ServerLevel levelIn, final InvasionDifficulty difficultyIn, final int totalInvasionsIn) {
		//Mob Relocation
		this.invasionMobs.removeIf(info -> {
			final Entity mob = levelIn.getEntity(info.uuid);
			if (info.relocate && mob != null) {
				final int players = levelIn.players().size();
				if (players < 1) return mob == null || !mob.isAlive();
				final ServerPlayer player = levelIn.players().get(levelIn.random.nextInt(players));
				final ChunkPos chunkPos = this.getSpawnChunk(levelIn, player);
				final BlockPos spawnPos = this.getSpawnPos(levelIn, chunkPos, player, mob.getType(), false, true);
				if (mob != null && this.canSpawnMob(levelIn, mob.getType(), spawnPos, mob.getPersistentData().contains(ANTI_GRIEF) && mob.getPersistentData().getBoolean(ANTI_GRIEF))) {
					PSPacketHandler.sendToClientsIn(new InvasionMobParticlesPacket(difficultyIn, mob.position(), false), levelIn);
					mob.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), mob.getYRot(), mob.getXRot());
					if (!mob.isInWall()) {
						PSPacketHandler.sendToClientsIn(new InvasionMobParticlesPacket(difficultyIn, mob.position(), true), levelIn);
						mob.getPersistentData().getIntArray(DESPAWN_LOGIC)[3] = 0; //Prevents mobs from speeding around the map needlessly.
					}
					info.relocate = false;
				}
			}
			return mob == null || !mob.isAlive();
		});
		//Spawn Mobs
		if (this.shouldTick && this.invasionMobs.size() < this.mobCap)
			this.tickMobSpawn(levelIn, difficultyIn, totalInvasionsIn);
		//Spawn Additional Entities
		final List<AdditionalEntitySpawnData> additionalEntities = this.getSeverityInfo().getClusterEntities();
		if (!additionalEntities.isEmpty()) {
			final AdditionalEntitySpawnData spawnInfo = additionalEntities.get(levelIn.random.nextInt(additionalEntities.size()));
			final int players = levelIn.players().size();
			if (players < 1) return;
			final int t = levelIn.random.nextInt(spawnInfo.getChance()) == 0 ? levelIn.random.nextIntBetweenInclusive(spawnInfo.getMinCount(), spawnInfo.getMaxCount()) : 0;
			if (t < 1) return;
			final ServerPlayer player = levelIn.players().get(levelIn.random.nextInt(players));
			final ChunkPos chunkPos = this.getSpawnChunk(levelIn, player);
			for (int c = 0; c < t; c++)
				this.spawnClusterEntity(this.getSpawnPos(levelIn, chunkPos, player, spawnInfo.getEntityType(), spawnInfo.isSurfaceSpawn(), false), levelIn, spawnInfo.getEntityType());
		}
	}

	private final void tickMobSpawn(final ServerLevel levelIn, final InvasionDifficulty difficultyIn, final int totalInvasionsIn) {
		//Delay check
		if (this.spawnDelay < 0)
			this.delay(levelIn, totalInvasionsIn);
		if (this.spawnDelay > 0) {
			--this.spawnDelay;
			return;
		}
		//Get Mobs
		final int players = levelIn.players().size();
		if (players < 1) return;
		final ServerPlayer player = levelIn.players().get(levelIn.random.nextInt(players));
		final ChunkPos chunkPos = this.getSpawnChunk(levelIn, player);
		if (chunkPos == null || !levelIn.isLoaded(chunkPos.getWorldPosition())) return;
		final List<InvasionSpawnerData> mobs = this.getMobSpawnList(levelIn, chunkPos, player);
		if (mobs.isEmpty()) return;
		//Spawn Mob Cluster (Different Mobs)
		boolean flag1 = false;
		final int clusterSize = levelIn.random.nextInt(this.getSeverityInfo().getClusterSize()) + 1;
		for (int cluster = 0; cluster < clusterSize && this.invasionMobs.size() < this.mobCap; cluster++) {
			//Spawn Mob Group (Same Mob)
			final InvasionSpawnerData spawners = mobs.get(levelIn.random.nextInt(mobs.size()));
			final int groupSize = levelIn.random.nextInt(spawners.maxCount - spawners.minCount + 1) + spawners.minCount;
			this.nextSpawnData.getEntityToSpawn().putString("id", ForgeRegistries.ENTITY_TYPES.getKey(spawners.type).toString());
			final CompoundTag compoundNBT = this.nextSpawnData.getEntityToSpawn();
			final Optional<EntityType<?>> optional = EntityType.by(compoundNBT);
			if (!optional.isPresent()) {
				this.delay(levelIn, totalInvasionsIn);
				return;
			}
			for (int count = 0; count < groupSize && this.invasionMobs.size() < this.mobCap; ++count) {
				//Spawn Entity
				final BlockPos spawnPos = this.getSpawnPos(levelIn, chunkPos, player, spawners.type, false, false);
				if (this.canSpawnMob(levelIn, optional.get(), spawnPos, spawners.ignoreSpawnRules)) {
					final Entity entity = EntityType.loadEntityRecursive(compoundNBT, levelIn, e -> {
						e.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), e.getYRot(), e.getXRot());
						return e;
					});
					if (entity == null || entity.isInWall()) {
						this.delay(levelIn, totalInvasionsIn);
						return;
					}
					entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), levelIn.random.nextFloat() * 360.0F, 0.0F);
					if (entity instanceof Mob mob) {
						if (!ForgeEventFactory.checkSpawnPosition(mob, levelIn, MobSpawnType.EVENT)) continue;
						if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", Tag.TAG_STRING))
							this.spawnInvasionMob(levelIn, difficultyIn, mob, spawners.ignoreSpawnRules, spawners.forceDespawn || PSGameRules.MOBS_DIE_AT_END_OF_INVASIONS.get(levelIn));
					}
					if (!levelIn.tryAddFreshEntityWithPassengers(entity)) {
						this.delay(levelIn, totalInvasionsIn);
						return;
					}
					flag1 = true;
				}
			}
		}
		if (flag1)
			this.delay(levelIn, totalInvasionsIn);
		return;
	}

	@SuppressWarnings("unchecked") //It is checked ;)
	private final boolean canSpawnMob(final ServerLevel levelIn, final EntityType<?> entityTypeIn, final BlockPos spawnPosIn, final boolean ignoreSpawnRulesIn) {
		final boolean flag = spawnPosIn != null && entityTypeIn.getCategory() == MobCategory.MONSTER && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(entityTypeIn), levelIn, spawnPosIn, entityTypeIn);
		if (ignoreSpawnRulesIn)
			return flag && Mob.checkMobSpawnRules((EntityType<? extends Mob>)entityTypeIn, levelIn, MobSpawnType.EVENT, spawnPosIn, levelIn.getRandom());
		return flag && SpawnPlacements.checkSpawnRules(entityTypeIn, levelIn, MobSpawnType.EVENT, spawnPosIn, levelIn.getRandom());
	}

	private final void spawnClusterEntity(final BlockPos posIn, final ServerLevel levelIn, final EntityType<?> entityTypeIn) {
		if (!Level.isInSpawnableBounds(posIn)) return;
		final CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entityTypeIn).toString());
		final Entity entity = EntityType.loadEntityRecursive(compoundTag, levelIn, e -> {
			e.moveTo(posIn.getX(), posIn.getY(), posIn.getZ(), e.getYRot(), e.getXRot());
			return e;
		});
		if (entity == null) return;
		entity.getPersistentData().putBoolean(ANTI_GRIEF, false);
		if (entity instanceof Mob mob)
			ForgeEventFactory.onFinalizeSpawn(mob, levelIn, levelIn.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.EVENT, null, null);
		levelIn.tryAddFreshEntityWithPassengers(entity);
	}

	private final void spawnInvasionMob(final ServerLevel levelIn, final InvasionDifficulty difficultyIn, final Mob mobIn, final boolean ignoresSpawnRulesIn, final boolean forceDespawnIn) {
		final boolean hyperCharged = PSGameRules.HYPER_CHARGE.get(levelIn) && !PSConfigValues.common.hyperChargeBlacklist.contains(mobIn.getType().getDescriptionId()) && (difficultyIn.isHyper() || levelIn.random.nextDouble() < PSConfigValues.common.hyperChargeChance * (double)(this.severity + 1)/this.invasionType.getMaxSeverity());
		final CompoundTag persistentData = mobIn.getPersistentData();
		persistentData.putInt(INVASION_MOB, this.hashCode());
		persistentData.putBoolean(ANTI_GRIEF, ignoresSpawnRulesIn);
		persistentData.putIntArray(DESPAWN_LOGIC, new int[forceDespawnIn ? 6 : 4]);
		if (forceDespawnIn) persistentData.getIntArray(DESPAWN_LOGIC)[5] = 100 + levelIn.random.nextInt(101);
		if (hyperCharged && mobIn instanceof PSInvasionMob invasionMob)
			invasionMob.psSetHyperCharge(difficultyIn.getHyperCharge(levelIn, this.invasionType.getTier(), this.isNatural));
		ForgeEventFactory.onFinalizeSpawn(mobIn, levelIn, levelIn.getCurrentDifficultyAt(mobIn.blockPosition()), MobSpawnType.EVENT, null, null);
		this.invasionMobs.add(new MobInfo(mobIn.getUUID(), false));
		PSPacketHandler.sendToClientsIn(new InvasionMobParticlesPacket(difficultyIn, mobIn.position()), levelIn);
		mobIn.spawnAnim();
	}

	private final void delay(final ServerLevel levelIn, final int totalInvasionsIn) {
		this.spawnDelay = this.getSeverityInfo().getTickDelay()/totalInvasionsIn;
		this.spawnPotentials.getRandom(levelIn.random).ifPresent(entry -> {
			this.nextSpawnData = entry.getData();
		});
	}

	private final ArrayList<InvasionSpawnerData> getMobSpawnList(final ServerLevel levelIn, final ChunkPos chunkPosIn, final ServerPlayer playerIn) {
		final ArrayList<InvasionSpawnerData> originalList = new ArrayList<>(this.getSeverityInfo().getMobSpawnList());
		switch (this.invasionType.getSpawningSystem()) {
		case DEFAULT: break;
		case BIOME_BOOSTED: {
			final BlockPos biomePos = this.getSpawnPos(levelIn, chunkPosIn, playerIn, null, false, false);
			originalList.addAll(this.getBiomeSpawnList(biomePos, levelIn.getChunk(biomePos)));
			break;
		}
		case BIOME_MIXED: {
			originalList.addAll(this.getMixedSpawnList(levelIn));
			break;
		}
		}
		final ArrayList<InvasionSpawnerData> newList = new ArrayList<>();
		if (!originalList.isEmpty()) {
			for (final InvasionSpawnerData spawners : originalList) {
				for (int w = 0; w < spawners.getWeight().asInt(); w++)
					newList.add(spawners);
			}
		}
		return newList;
	}

	private final ArrayList<InvasionSpawnerData> getBiomeSpawnList(final BlockPos posIn, final ChunkAccess chunkIn) {
		final ArrayList<InvasionSpawnerData> spawners = InvasionSpawnerData.convertSpawners(chunkIn.getNoiseBiome(posIn.getX(), posIn.getY(), posIn.getZ()).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap());
		spawners.removeIf(spawner -> {
			final ResourceLocation name = spawner.type.getDefaultLootTable();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ArrayList<InvasionSpawnerData> getMixedSpawnList(final ServerLevel levelIn) {
		final Optional<Holder.Reference<LevelStem>> optional = levelIn.registryAccess().registryOrThrow(Registries.LEVEL_STEM).getRandom(levelIn.random);
		if (!optional.isPresent() || optional.isEmpty()) return new ArrayList<>();
		final ArrayList<Holder<Biome>> biomes = new ArrayList<>(optional.get().get().generator().getBiomeSource().possibleBiomes());
		final ArrayList<InvasionSpawnerData> spawners = biomes.size() > 0 ? InvasionSpawnerData.convertSpawners(biomes.get(levelIn.random.nextInt(biomes.size())).get().getMobSettings().getMobs(MobCategory.MONSTER).unwrap()) : new ArrayList<>();
		spawners.removeIf(spawner -> {
			final ResourceLocation name = spawner.type.getDefaultLootTable();
			return PSConfigValues.common.modBiomeBoostedBlacklist.contains(name.getNamespace()) || PSConfigValues.common.mobBiomeBoostedBlacklist.contains(name.toString());
		});
		return spawners;
	}

	private final ChunkPos getSpawnChunk(final ServerLevel levelIn, final ServerPlayer playerIn) {
		final ChunkPos chunkPos = levelIn.getChunk(playerIn.blockPosition()).getPos();
		final int chunkX = chunkPos.x - 8 + levelIn.random.nextInt(17);
		final int chunkZ = chunkPos.z - 8 + levelIn.random.nextInt(17);
		final boolean flag = chunkPos.x == chunkX && chunkPos.z == chunkZ;
		final ChunkPos chunkPos1 = new ChunkPos(flag ? chunkX + this.getChunkOffset(levelIn) : chunkX, flag ? chunkZ + this.getChunkOffset(levelIn) : chunkZ);
		return chunkPos1;
	}

	private final int getChunkOffset(final ServerLevel levelIn) {
		final int offSet = levelIn.random.nextInt(8) + 1;
		return levelIn.random.nextBoolean() ? offSet : -offSet;
	}

	private final BlockPos getSpawnPos(final ServerLevel levelIn, final ChunkPos chunkPosIn, final ServerPlayer playerIn, final EntityType<?> entityTypeIn, final boolean isSurfaceIn, final boolean relocateIn) {
		final int x = chunkPosIn.getMinBlockX() + levelIn.random.nextInt(16);
		final int z = chunkPosIn.getMinBlockZ() + levelIn.random.nextInt(16);
		final int surface = levelIn.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		if (isSurfaceIn) return new BlockPos(x, surface, z);
		final ArrayList<Integer> posList = new ArrayList<>();
		for (int y = levelIn.getMinBuildHeight(); y <= surface; y++) {
			final BlockPos pos = new BlockPos(x, y, z);
			if (entityTypeIn == null ? !levelIn.getBlockState(pos).isAir() && levelIn.getBlockState(pos.above()).isAir() : SpawnPlacements.getPlacementType(entityTypeIn).canSpawnAt(levelIn, pos, entityTypeIn))
				posList.add(pos.getY());
		}
		return new BlockPos(x, SpawnPosChart.getYInRange(posList, playerIn.getBlockY(), levelIn.random.nextFloat(), relocateIn), z);
	}

	public static final Invasion load(final ServerLevel levelIn, final CompoundTag nbtIn) {
		if (!PSReloadListeners.getInvasionTypeManager().verifyInvasion(nbtIn.getString("InvasionType"))) return null;
		final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(nbtIn.getString("InvasionType")));
		final Invasion invasion = new Invasion(levelIn, invasionType, nbtIn.getInt("Severity"), nbtIn.getBoolean("IsPrimary"), nbtIn.getBoolean("IsNatural"), nbtIn.getLong("StartTime"), nbtIn.getInt("Index"));
		invasion.spawnDelay = nbtIn.getInt("SpawnDelay");
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
	public final boolean equals(final Object objIn) {
		if (objIn instanceof Invasion inv)
			return this.invasionType.toString().equals(inv.invasionType.toString()) && this.severity == inv.severity && this.isPrimary == inv.isPrimary && this.isNatural == inv.isNatural && this.startTime == inv.startTime && this.index == inv.index;
		return false;
	}

	private static final class MobInfo {
		private UUID uuid;
		private boolean relocate;

		private MobInfo(final UUID uuidIn, final Boolean relocateIn) {
			this.uuid = uuidIn;
			this.relocate = relocateIn;
		}
	}
	
	public static final class BuildInfo implements InvasionTypeHolder {
		private final InvasionType invasionType;
		private final int severity;
		private final boolean isPrimary;
		
		public BuildInfo(final InvasionType invasionTypeIn, final int severityIn, final boolean isPrimaryIn) {
			this.invasionType = invasionTypeIn;
			this.severity = severityIn;
			this.isPrimary = isPrimaryIn;
		}
		
		@Override
		public final InvasionType getType() {
			return this.invasionType;
		}
		
		public final boolean isPrimary() {
			return this.isPrimary;
		}
		
		public static final BuildInfo load(final ServerLevel levelIn, final CompoundTag nbtIn) {
			final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(ResourceLocation.tryParse(nbtIn.getString("InvasionType")));
			final int severity = nbtIn.getInt("Severity");
			final boolean isPrimary = nbtIn.getBoolean("IsPrimary");
			return new BuildInfo(invasionType, severity, isPrimary);
		}
		
		public final CompoundTag save() {
			final CompoundTag nbt = new CompoundTag();
			nbt.putString("InvasionType", this.invasionType.toString());
			nbt.putInt("Severity", this.severity);
			nbt.putBoolean("IsPrimary", this.isPrimary);
			return nbt;
		}
		
		public final Invasion build(final ServerLevel levelIn, final int indexIn) {
			return new Invasion(levelIn, this.invasionType, this.severity, this.isPrimary, false, levelIn.getDayTime(), indexIn);
		}
	}
}
