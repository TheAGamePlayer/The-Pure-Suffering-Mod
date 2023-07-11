package dev.theagameplayer.puresuffering.spawner;

import java.util.ArrayList;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.event.PSBaseEvents;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeChangeability;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import dev.theagameplayer.puresuffering.util.InvasionChart;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;

public final class TimedInvasionSpawner extends AbstractInvasionSpawner {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private final InvasionList nightInvasions = new InvasionList(InvasionListType.NIGHT);
	private final InvasionList dayInvasions = new InvasionList(InvasionListType.DAY);
	private final ArrayList<Invasion> queuedNightInvasions = new ArrayList<>();
	private final ArrayList<Invasion> queuedDayInvasions = new ArrayList<>();
	private final int[] nightIntervals = {-1, -1, -1};
	private final int[] dayIntervals = {-1, -1, -1};
	private int nightCancelInterval = -1;
	private int dayCancelInterval = -1;
	private boolean isNightChangedToDay, isDayChangedToNight;

	public final void setNightInvasions(final ServerLevel levelIn, final long daysIn, final int maxInvasionsIn) {
		this.nightInvasions.clear();
		this.isNightChangedToDay = false;
		this.nightInvasions.setCanceled(false);
		if (!this.queuedNightInvasions.isEmpty()) {
			LOGGER.info("Setting Queued Night Invasions: [");
			for (int q = 0; q < this.queuedNightInvasions.size(); q++) {
				final Invasion invasion = this.queuedNightInvasions.get(q);
				this.nightInvasions.add(invasion);
				LOGGER.info((invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.NIGHTMARE ? "Nightmare " : "Hyper ") : "") + "Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedNightInvasions.clear();
		} else {
			final RandomSource random = levelIn.random;
			final HyperType difficulty = this.calculateInvasionDifficulty(this.nightIntervals, PSConfigValues.common.nightInvasionRarity, PSConfigValues.common.nightDifficultyIncreaseDelay, random, daysIn);
			if (difficulty != null) {
				if (this.nightCancelInterval > 0) {
					this.nightCancelInterval--;
				} else {
					if (this.nightCancelInterval > -1 && daysIn > PSConfigValues.common.nightDifficultyIncreaseDelay)
						this.nightInvasions.setCanceled(this.calculateInvasionCanceled(maxInvasionsIn, PSConfigValues.common.canNightInvasionsBeCanceled, difficulty));
					this.nightCancelInterval = random.nextInt(PSConfigValues.common.nightCancelChance) + PSConfigValues.common.nightCancelChance - (int)(daysIn % PSConfigValues.common.nightCancelChance);
				}
			}
			final int totalInvasions = this.calculateInvasions(random, daysIn, maxInvasionsIn, this.nightInvasions.isCanceled(), difficulty);
			InvasionChart.refresh();
			final InvasionChart potentialPrimaryNightInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
			final InvasionChart potentialSecondaryNightInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
			final InvasionChart potentialSecondaryDayInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY));
			LOGGER.info("Setting Night Invasions: [");
			for (int inv = 0; inv < totalInvasions; inv++) {
				final InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryNightInvasions : (this.isNightChangedToDay ? potentialSecondaryDayInvasions : potentialSecondaryNightInvasions), random);
				if (invasionType != null) {
					final int severity = difficulty != HyperType.DEFAULT ? invasionType.getMaxSeverity() - 1 : random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.nightDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					final Invasion invasion = new Invasion(invasionType, severity, inv == 0, difficulty);
					this.nightInvasions.add(invasion);
					LOGGER.info((difficulty != HyperType.DEFAULT ? (difficulty == HyperType.NIGHTMARE ? "Nightmare " : "Hyper ") : "") + "Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					this.isNightChangedToDay |= invasionType.getTimeModifier() == TimeModifier.NIGHT_TO_DAY;
				} else {
					break;
				}
			}
		}
		LOGGER.info("]");
	}

	public final void setDayInvasions(final ServerLevel levelIn, final long daysIn, final int maxInvasionsIn) {
		this.dayInvasions.clear();
		this.isDayChangedToNight = false;
		this.dayInvasions.setCanceled(false);
		if (!this.queuedDayInvasions.isEmpty()) {
			LOGGER.info("Setting Queued Day Invasions: [");
			for (int q = 0; q < this.queuedDayInvasions.size(); q++) {
				final Invasion invasion = this.queuedDayInvasions.get(q);
				this.dayInvasions.add(invasion);
				LOGGER.info((invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.NIGHTMARE ? "Nightmare " : "Hyper ") : "") + "Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedDayInvasions.clear();
		} else {
			final RandomSource random = levelIn.random;
			final HyperType difficulty = this.calculateInvasionDifficulty(this.dayIntervals, PSConfigValues.common.dayInvasionRarity, PSConfigValues.common.dayDifficultyIncreaseDelay, random, daysIn);
			if (difficulty != null) {
				if (this.dayCancelInterval > 0) {
					this.dayCancelInterval--;
				} else {
					if (this.dayCancelInterval > -1 && daysIn > PSConfigValues.common.dayDifficultyIncreaseDelay)
						this.dayInvasions.setCanceled(this.calculateInvasionCanceled(maxInvasionsIn, PSConfigValues.common.canDayInvasionsBeCanceled, difficulty));
					this.dayCancelInterval = random.nextInt(PSConfigValues.common.dayCancelChance) + PSConfigValues.common.dayCancelChance - (int)(daysIn % PSConfigValues.common.dayCancelChance);
				}
			}
			final int totalInvasions = this.calculateInvasions(random, daysIn, maxInvasionsIn, this.dayInvasions.isCanceled(), difficulty);
			InvasionChart.refresh();
			final InvasionChart potentialPrimaryDayInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
			final InvasionChart potentialSecondaryDayInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
			final InvasionChart potentialSecondaryNightInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT));
			LOGGER.info("Setting Day Invasions: [");
			for (int inv = 0; inv < totalInvasions; inv++) {
				final InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryDayInvasions : (this.isDayChangedToNight ? potentialSecondaryNightInvasions : potentialSecondaryDayInvasions), random);
				if (invasionType != null) {
					final int severity = difficulty != HyperType.DEFAULT ? invasionType.getMaxSeverity() - 1 : random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.dayDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					final Invasion invasion = new Invasion(invasionType, severity, inv == 0, difficulty);
					this.dayInvasions.add(invasion);
					LOGGER.info((difficulty != HyperType.DEFAULT ? (difficulty == HyperType.NIGHTMARE ? "Nightmare " : "Hyper ") : "") + "Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					this.isDayChangedToNight |= invasionType.getTimeModifier() == TimeModifier.DAY_TO_NIGHT;
				} else {
					break;
				}
			}
		}
		LOGGER.info("]");
	}

	@Override
	public final void invasionTick(final MinecraftServer serverIn, final ServerLevel levelIn) {
		final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)InvasionWorldData.getInvasionData().get(levelIn);
		if (!this.nightInvasions.isEmpty() && ServerTimeUtil.isServerNight(levelIn, tiwData)) {
			final Invasion invasion = this.nightInvasions.get(levelIn.getRandom().nextInt(this.nightInvasions.size()));
			final ServerChunkCache chunkSource = levelIn.getChunkSource();
			invasion.tick(levelIn, chunkSource.spawnEnemies, chunkSource.spawnFriendlies);
		} else if (!this.dayInvasions.isEmpty() && ServerTimeUtil.isServerDay(levelIn, tiwData)) {
			final Invasion invasion = this.dayInvasions.get(levelIn.getRandom().nextInt(this.dayInvasions.size()));
			final ServerChunkCache chunkSource = levelIn.getChunkSource();
			invasion.tick(levelIn, chunkSource.spawnEnemies, chunkSource.spawnFriendlies);
		}
	}

	@Override
	public final void load(final CompoundTag nbtIn) {
		final ListTag nightInvasionsNBT = nbtIn.getList("NightInvasions", Tag.TAG_COMPOUND);
		final ListTag dayInvasionsNBT = nbtIn.getList("DayInvasions", Tag.TAG_COMPOUND);
		final ListTag queuedNightInvasionsNBT = nbtIn.getList("QueuedNightInvasions", Tag.TAG_COMPOUND);
		final ListTag queuedDayInvasionsNBT = nbtIn.getList("QueuedDayInvasions", Tag.TAG_COMPOUND);
		for (final Tag inbt : nightInvasionsNBT)
			this.nightInvasions.add(Invasion.load((CompoundTag)inbt));
		for (final Tag inbt : dayInvasionsNBT)
			this.dayInvasions.add(Invasion.load((CompoundTag)inbt));
		for (final Tag inbt : queuedNightInvasionsNBT)
			this.queuedNightInvasions.add(Invasion.load((CompoundTag)inbt));
		for (final Tag inbt : queuedDayInvasionsNBT)
			this.queuedDayInvasions.add(Invasion.load((CompoundTag)inbt));
		this.isNightChangedToDay = nbtIn.getBoolean("IsNightChangedToDay");
		this.isDayChangedToNight = nbtIn.getBoolean("IsDayChangedToNight");
		for (final HyperType hyperType : HyperType.values()) {
			this.nightIntervals[hyperType.ordinal()] = nbtIn.getInt(hyperType.toString() + "NightInterval");
			this.dayIntervals[hyperType.ordinal()] = nbtIn.getInt(hyperType.toString() + "DayInterval");
		}
		this.nightCancelInterval = nbtIn.getInt("NightCancelInterval");
		this.dayCancelInterval = nbtIn.getInt("DayCancelInterval");
	}

	@Override
	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final ListTag nightInvasionsNBT = new ListTag();
		final ListTag dayInvasionsNBT = new ListTag();
		final ListTag queuedNightInvasionsNBT = new ListTag();
		final ListTag queuedDayInvasionsNBT = new ListTag();
		for (final Invasion invasion : this.nightInvasions)
			nightInvasionsNBT.add(invasion.save());
		for (final Invasion invasion : this.dayInvasions)
			dayInvasionsNBT.add(invasion.save());
		for (final Invasion invasion : this.queuedNightInvasions)
			queuedNightInvasionsNBT.add(invasion.save());
		for (final Invasion invasion : this.queuedDayInvasions)
			queuedDayInvasionsNBT.add(invasion.save());
		nbt.put("NightInvasions", nightInvasionsNBT);
		nbt.put("DayInvasions", dayInvasionsNBT);
		nbt.put("QueuedNightInvasions", queuedNightInvasionsNBT);
		nbt.put("QueuedDayInvasions", queuedDayInvasionsNBT);
		nbt.putBoolean("IsNightChangedToDay", this.isNightChangedToDay);
		nbt.putBoolean("IsDayChangedToNight", this.isDayChangedToNight);
		for (final HyperType hyperType : HyperType.values()) {
			nbt.putInt(hyperType.toString() + "NightInterval", this.nightIntervals[hyperType.ordinal()]);
			nbt.putInt(hyperType.toString() + "DayInterval", this.dayIntervals[hyperType.ordinal()]);
		}
		nbt.putInt("NightCancelInterval", this.nightCancelInterval);
		nbt.putInt("DayCancelInterval", this.dayCancelInterval);
		return nbt;
	}

	public final InvasionList getNightInvasions() {
		return this.nightInvasions;
	}

	public final InvasionList getDayInvasions() {
		return this.dayInvasions;
	}

	public final ArrayList<Invasion> getQueuedNightInvasions() {
		return this.queuedNightInvasions;
	}

	public final ArrayList<Invasion> getQueuedDayInvasions() {
		return this.queuedDayInvasions;
	}
}
