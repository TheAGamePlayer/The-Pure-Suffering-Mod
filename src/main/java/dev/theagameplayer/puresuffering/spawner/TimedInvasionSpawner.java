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
import net.minecraft.server.level.ServerLevel;

public final class TimedInvasionSpawner {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private final InvasionList nightInvasions = new InvasionList(InvasionListType.NIGHT);
	private final InvasionList dayInvasions = new InvasionList(InvasionListType.DAY);
	private final ArrayList<Invasion> queuedNightInvasions = new ArrayList<>();
	private final ArrayList<Invasion> queuedDayInvasions = new ArrayList<>();
	private boolean isNightChangedToDay, isDayChangedToNight;
	private int nightInterval, dayInterval;
	private int hyperNightInterval, hyperDayInterval;
	private int mysteryNightInterval, mysteryDayInterval;

	public final void setNightInvasions(final ServerLevel levelIn, final boolean isCanceledIn, final int amountIn, final long daysIn) {
		this.nightInvasions.clear();
		this.isNightChangedToDay = false;
		final RandomSource random = levelIn.random;
		final int totalInvasions = !this.queuedNightInvasions.isEmpty() ? this.queuedNightInvasions.size() : this.calculateInvasions(random, amountIn, this.nightInterval, isCanceledIn, daysIn == 0);
		this.nightInterval = this.nightInterval > 0 ? this.nightInterval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.nightInvasionRarity : random.nextInt(PSConfigValues.common.nightInvasionRarity) + PSConfigValues.common.nightInvasionRarity - (int)(daysIn % PSConfigValues.common.nightInvasionRarity));
		this.hyperNightInterval = this.hyperNightInterval > 0 ? (this.nightInterval == 0 ? (PSConfigValues.common.hyperInvasions && PSConfigValues.common.hyperCharge ? this.hyperNightInterval - 1 : this.hyperNightInterval) : this.hyperNightInterval) : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.hyperInvasionRarity : random.nextInt(PSConfigValues.common.hyperInvasionRarity) + PSConfigValues.common.hyperInvasionRarity - (int)(daysIn % PSConfigValues.common.hyperInvasionRarity));
		this.mysteryNightInterval = this.mysteryNightInterval > 0 ? (this.hyperNightInterval == 0 ? (PSConfigValues.common.mysteryInvasions && PSConfigValues.common.hyperCharge ? this.mysteryNightInterval - 1 : this.mysteryNightInterval) : this.mysteryNightInterval) : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.mysteryInvasionRarity : random.nextInt(PSConfigValues.common.mysteryInvasionRarity) + PSConfigValues.common.mysteryInvasionRarity - (int)(daysIn % PSConfigValues.common.mysteryInvasionRarity));
		InvasionChart.refresh();
		final InvasionChart potentialPrimaryNightInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		final InvasionChart potentialSecondaryNightInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		final InvasionChart potentialSecondaryDayInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY));
		LOGGER.info("Setting Night Invasions: [");
		this.nightInvasions.setCanceled(this.queuedNightInvasions.isEmpty() && isCanceledIn);
		if (!this.queuedNightInvasions.isEmpty()) {
			for (int q = 0; q < this.queuedNightInvasions.size(); q++) {
				final Invasion invasion = this.queuedNightInvasions.get(q);
				this.nightInvasions.add(invasion);
				LOGGER.info("Queued " + (invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "Mystery " : "Hyper ") : "") + "Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedNightInvasions.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				final InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryNightInvasions : (this.isNightChangedToDay ? potentialSecondaryDayInvasions : potentialSecondaryNightInvasions), random);
				if (invasionType != null) {
					final int severity = this.hyperNightInterval == 0 ? invasionType.getMaxSeverity() - 1 : random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.nightDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					final Invasion invasion = new Invasion(invasionType, severity, inv == 0, this.hyperNightInterval == 0 ? (this.mysteryNightInterval == 0 ? HyperType.MYSTERY : HyperType.HYPER) : HyperType.DEFAULT);
					this.nightInvasions.add(invasion);
					LOGGER.info((this.hyperNightInterval == 0 ? (invasion.getHyperType() == HyperType.MYSTERY ? "Mystery " : "Hyper ") : "") + "Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					this.isNightChangedToDay |= invasionType.getTimeModifier() == TimeModifier.NIGHT_TO_DAY;
				}
			}
		}
		LOGGER.info("]");
	}

	public final void setDayInvasions(final ServerLevel levelIn, final boolean isCanceledIn, final int amountIn, final long daysIn) {
		this.dayInvasions.clear();
		this.isDayChangedToNight = false;
		final RandomSource random = levelIn.random;
		final int totalInvasions = !this.queuedDayInvasions.isEmpty() ? this.queuedDayInvasions.size() : this.calculateInvasions(random, amountIn, this.dayInterval, isCanceledIn, daysIn == 0);
		this.dayInterval = this.dayInterval > 0 ? this.dayInterval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.dayInvasionRarity : random.nextInt(PSConfigValues.common.dayInvasionRarity) + PSConfigValues.common.dayInvasionRarity - (int)(daysIn % PSConfigValues.common.dayInvasionRarity));
		this.hyperDayInterval = this.hyperDayInterval > 0 ? (this.dayInterval == 0 ? (PSConfigValues.common.hyperInvasions && PSConfigValues.common.hyperCharge ? this.hyperDayInterval - 1 : this.hyperDayInterval) : this.hyperDayInterval) : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.hyperInvasionRarity : random.nextInt(PSConfigValues.common.hyperInvasionRarity) + PSConfigValues.common.hyperInvasionRarity - (int)(daysIn % PSConfigValues.common.hyperInvasionRarity));
		this.mysteryDayInterval = this.mysteryDayInterval > 0 ? (this.hyperDayInterval == 0 ? (PSConfigValues.common.mysteryInvasions && PSConfigValues.common.hyperCharge ? this.mysteryDayInterval - 1 : this.mysteryDayInterval) : this.mysteryDayInterval) : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.mysteryInvasionRarity : random.nextInt(PSConfigValues.common.mysteryInvasionRarity) + PSConfigValues.common.mysteryInvasionRarity - (int)(daysIn % PSConfigValues.common.mysteryInvasionRarity));
		InvasionChart.refresh();
		final InvasionChart potentialPrimaryDayInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		final InvasionChart potentialSecondaryDayInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		final InvasionChart potentialSecondaryNightInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT));
		LOGGER.info("Setting Day Invasions: [");
		this.dayInvasions.setCanceled(this.queuedDayInvasions.isEmpty() && isCanceledIn);
		if (!this.queuedDayInvasions.isEmpty()) {
			for (int q = 0; q < this.queuedDayInvasions.size(); q++) {
				final Invasion invasion = this.queuedDayInvasions.get(q);
				this.dayInvasions.add(invasion);
				LOGGER.info("Queued " + (invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "Mystery " : "Hyper ") : "") + "Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedDayInvasions.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				final InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryDayInvasions : (this.isDayChangedToNight ? potentialSecondaryNightInvasions : potentialSecondaryDayInvasions), random);
				if (invasionType != null) {
					final int severity = this.hyperDayInterval == 0 ? invasionType.getMaxSeverity() - 1 : random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.dayDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					final Invasion invasion = new Invasion(invasionType, severity, inv == 0, this.hyperDayInterval == 0 ? (this.mysteryDayInterval == 0 ? HyperType.MYSTERY : HyperType.HYPER) : HyperType.DEFAULT);
					this.dayInvasions.add(invasion);
					LOGGER.info((this.hyperDayInterval == 0 ? (invasion.getHyperType() == HyperType.MYSTERY ? "Mystery " : "Hyper ") : "") + "Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					this.isDayChangedToNight |= invasionType.getTimeModifier() == TimeModifier.DAY_TO_NIGHT;
				}
			}
		}
		LOGGER.info("]");
	}

	private final int calculateInvasions(final RandomSource randomIn, final int amountIn, final int intervalIn, final boolean isCanceledIn, final boolean isFirstDayIn) {
		return !isFirstDayIn && intervalIn == 0 && amountIn > 0 && !isCanceledIn ? randomIn.nextInt(amountIn) + 1 : 0;
	}

	private final InvasionType getInvasionType(final InvasionChart invasionChartIn, final RandomSource randomIn) {
		return invasionChartIn.getInvasionInRange(randomIn.nextFloat());
	}

	public final void invasionTick(final MinecraftServer serverIn, final ServerLevel levelIn) {
		final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)InvasionWorldData.getInvasionData().get(levelIn);
		if (!this.nightInvasions.isEmpty() && ServerTimeUtil.isServerNight(levelIn, tiwData)) {
			final Invasion invasion = this.nightInvasions.get(levelIn.getRandom().nextInt(this.nightInvasions.size()));
			invasion.tick(levelIn);
		} else if (!this.dayInvasions.isEmpty() && ServerTimeUtil.isServerDay(levelIn, tiwData)) {
			final Invasion invasion = this.dayInvasions.get(levelIn.getRandom().nextInt(this.dayInvasions.size()));
			invasion.tick(levelIn);
		}
	}

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
		this.nightInterval = nbtIn.getInt("NightInterval");
		this.dayInterval = nbtIn.getInt("DayInterval");
		this.hyperNightInterval = nbtIn.getInt("HyperNightInterval");
		this.hyperDayInterval = nbtIn.getInt("HyperDayInterval");
		this.mysteryNightInterval = nbtIn.getInt("MysteryNightInterval");
		this.mysteryDayInterval = nbtIn.getInt("MysteryDayInterval");
	}

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
		nbt.putInt("NightInterval", this.nightInterval);
		nbt.putInt("DayInterval", this.dayInterval);
		nbt.putInt("HyperNightInterval", this.hyperNightInterval);
		nbt.putInt("HyperDayInterval", this.hyperDayInterval);
		nbt.putInt("MysteryNightInterval", this.mysteryNightInterval);
		nbt.putInt("MysteryDayInterval", this.mysteryDayInterval);
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
