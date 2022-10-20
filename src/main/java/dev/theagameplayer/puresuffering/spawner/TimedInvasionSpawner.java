package dev.theagameplayer.puresuffering.spawner;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
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
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private final InvasionList nightInvasions = new InvasionList(InvasionListType.NIGHT);
	private final InvasionList dayInvasions = new InvasionList(InvasionListType.DAY);
	private final ArrayList<Invasion> queuedNightInvasions = new ArrayList<>();
	private final ArrayList<Invasion> queuedDayInvasions = new ArrayList<>();
	private boolean isNightChangedToDay, isDayChangedToNight;
	private int nightInterval, dayInterval;

	public void setNightInvasions(ServerLevel worldIn, boolean isCanceledIn, int amountIn, long daysIn) {
		this.nightInvasions.clear();
		RandomSource random = worldIn.random;
		this.isNightChangedToDay = false;
		int totalInvasions = !this.queuedNightInvasions.isEmpty() ? this.queuedNightInvasions.size() : this.calculateInvasions(random, amountIn, this.nightInterval, isCanceledIn, daysIn == 0);
		this.nightInterval = this.nightInterval > 0 ? this.nightInterval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.nightInvasionRarity : random.nextInt(PSConfigValues.common.nightInvasionRarity) + PSConfigValues.common.nightInvasionRarity - (int)(daysIn % PSConfigValues.common.nightInvasionRarity));
		InvasionChart.refresh();
		InvasionChart potentialPrimaryNightInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(worldIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		InvasionChart potentialSecondaryNightInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(worldIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		InvasionChart potentialSecondaryDayInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(worldIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY));
		LOGGER.info("Setting Night Invasions: [");
		this.nightInvasions.setCanceled(this.queuedNightInvasions.isEmpty() && isCanceledIn);
		if (!this.queuedNightInvasions.isEmpty()) {
			for (int q = 0; q < this.queuedNightInvasions.size(); q++) {
				Invasion invasion = this.queuedNightInvasions.get(q);
				this.nightInvasions.add(invasion);
				LOGGER.info("Queued Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedNightInvasions.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryNightInvasions : (this.isNightChangedToDay ? potentialSecondaryDayInvasions : potentialSecondaryNightInvasions), random);
				if (invasionType != null) {
					int severity = random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.nightDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					Invasion invasion = new Invasion(invasionType, severity, inv == 0);
					this.nightInvasions.add(invasion);
					LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					this.isNightChangedToDay |= invasionType.getTimeModifier() == TimeModifier.NIGHT_TO_DAY;
				}
			}
		}
		LOGGER.info("]");
	}

	public void setDayInvasions(ServerLevel worldIn, boolean isCanceledIn, int amountIn, long daysIn) {
		this.dayInvasions.clear();
		RandomSource random = worldIn.random;
		this.isDayChangedToNight = false;
		int totalInvasions = !this.queuedDayInvasions.isEmpty() ? this.queuedDayInvasions.size() : this.calculateInvasions(random, amountIn, this.dayInterval, isCanceledIn, daysIn == 0);
		this.dayInterval = this.dayInterval > 0 ? this.dayInterval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.dayInvasionRarity : random.nextInt(PSConfigValues.common.dayInvasionRarity) + PSConfigValues.common.dayInvasionRarity - (int)(daysIn % PSConfigValues.common.dayInvasionRarity));
		InvasionChart.refresh();
		InvasionChart potentialPrimaryDayInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(worldIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		InvasionChart potentialSecondaryDayInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && it.getDimensions().contains(worldIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		InvasionChart potentialSecondaryNightInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && it.getDimensions().contains(worldIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT));
		LOGGER.info("Setting Day Invasions: [");
		this.dayInvasions.setCanceled(this.queuedDayInvasions.isEmpty() && isCanceledIn);
		if (!this.queuedDayInvasions.isEmpty()) {
			for (int q = 0; q < this.queuedDayInvasions.size(); q++) {
				Invasion invasion = this.queuedDayInvasions.get(q);
				this.dayInvasions.add(invasion);
				LOGGER.info("Queued Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedDayInvasions.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryDayInvasions : (this.isDayChangedToNight ? potentialSecondaryNightInvasions : potentialSecondaryDayInvasions), random);
				if (invasionType != null) {
					int severity = random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.dayDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					Invasion invasion = new Invasion(invasionType, severity, inv == 0);
					this.dayInvasions.add(invasion);
					LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					this.isDayChangedToNight |= invasionType.getTimeModifier() == TimeModifier.DAY_TO_NIGHT;
				}
			}
		}
		LOGGER.info("]");
	}

	private int calculateInvasions(RandomSource randomIn, int amountIn, int intervalIn, boolean isCanceledIn, boolean isFirstDayIn) {
		return !isFirstDayIn && intervalIn == 0 && amountIn > 0 && !isCanceledIn ? randomIn.nextInt(amountIn) + 1 : 0;
	}

	private InvasionType getInvasionType(InvasionChart invasionChartIn, RandomSource randomIn) {
		return invasionChartIn.getInvasionInRange(randomIn.nextFloat());
	}

	public void invasionTick(MinecraftServer serverIn, ServerLevel worldIn) {
		TimedInvasionWorldData tiwData = (TimedInvasionWorldData)InvasionWorldData.getInvasionData().get(worldIn);
		if (!this.nightInvasions.isEmpty() && ServerTimeUtil.isServerNight(worldIn, tiwData)) {
			Invasion invasion = this.nightInvasions.get(worldIn.getRandom().nextInt(this.nightInvasions.size()));
			invasion.tick(worldIn);
		} else if (!this.dayInvasions.isEmpty() && ServerTimeUtil.isServerDay(worldIn, tiwData)) {
			Invasion invasion = this.dayInvasions.get(worldIn.getRandom().nextInt(this.dayInvasions.size()));
			invasion.tick(worldIn);
		}
	}

	public void load(CompoundTag nbtIn) {
		ListTag nightInvasionsNBT = nbtIn.getList("NightInvasions", Tag.TAG_COMPOUND);
		ListTag dayInvasionsNBT = nbtIn.getList("DayInvasions", Tag.TAG_COMPOUND);
		ListTag queuedNightInvasionsNBT = nbtIn.getList("QueuedNightInvasions", Tag.TAG_COMPOUND);
		ListTag queuedDayInvasionsNBT = nbtIn.getList("QueuedDayInvasions", Tag.TAG_COMPOUND);
		for (Tag inbt : nightInvasionsNBT)
			this.nightInvasions.add(Invasion.load((CompoundTag)inbt));
		for (Tag inbt : dayInvasionsNBT)
			this.dayInvasions.add(Invasion.load((CompoundTag)inbt));
		for (Tag inbt : queuedNightInvasionsNBT)
			this.queuedNightInvasions.add(Invasion.load((CompoundTag)inbt));
		for (Tag inbt : queuedDayInvasionsNBT)
			this.queuedDayInvasions.add(Invasion.load((CompoundTag)inbt));
		this.isNightChangedToDay = nbtIn.getBoolean("IsNightChangedToDay");
		this.isDayChangedToNight = nbtIn.getBoolean("IsDayChangedToNight");
		this.nightInterval = nbtIn.getInt("NightInterval");
		this.dayInterval = nbtIn.getInt("DayInterval");
	}

	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		ListTag nightInvasionsNBT = new ListTag();
		ListTag dayInvasionsNBT = new ListTag();
		ListTag queuedNightInvasionsNBT = new ListTag();
		ListTag queuedDayInvasionsNBT = new ListTag();
		for (Invasion invasion : this.nightInvasions)
			nightInvasionsNBT.add(invasion.save());
		for (Invasion invasion : this.dayInvasions)
			dayInvasionsNBT.add(invasion.save());
		for (Invasion invasion : this.queuedNightInvasions)
			queuedNightInvasionsNBT.add(invasion.save());
		for (Invasion invasion : this.queuedDayInvasions)
			queuedDayInvasionsNBT.add(invasion.save());
		nbt.put("NightInvasions", nightInvasionsNBT);
		nbt.put("DayInvasions", dayInvasionsNBT);
		nbt.put("QueuedNightInvasions", queuedNightInvasionsNBT);
		nbt.put("QueuedDayInvasions", queuedDayInvasionsNBT);
		nbt.putBoolean("IsNightChangedToDay", this.isNightChangedToDay);
		nbt.putBoolean("IsDayChangedToNight", this.isDayChangedToNight);
		nbt.putInt("NightInterval", this.nightInterval);
		nbt.putInt("DayInterval", this.dayInterval);
		return nbt;
	}

	public InvasionList getNightInvasions() {
		return this.nightInvasions;
	}

	public InvasionList getDayInvasions() {
		return this.dayInvasions;
	}

	public ArrayList<Invasion> getQueuedNightInvasions() {
		return this.queuedNightInvasions;
	}

	public ArrayList<Invasion> getQueuedDayInvasions() {
		return this.queuedDayInvasions;
	}
}
