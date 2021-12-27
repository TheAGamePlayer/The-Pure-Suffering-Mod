package dev.theagameplayer.puresuffering.spawner;

import java.util.ArrayList;
import java.util.Random;
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
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public final class InvasionSpawner {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final InvasionList NIGHT_INVASIONS = new InvasionList(false);
	private static final InvasionList DAY_INVASIONS = new InvasionList(true);
	private static final ArrayList<Invasion> QUEUED_NIGHT_INVASIONS = new ArrayList<>();
	private static final ArrayList<Invasion> QUEUED_DAY_INVASIONS = new ArrayList<>();
	private static int nightInterval = 0, dayInterval = 0;
	private static boolean isNightChangedToDay = false;
	private static boolean isDayChangedToNight = false;

	public static void setNightTimeEvents(ServerWorld worldIn, boolean isCanceledIn, int amountIn, long daysIn) {
		NIGHT_INVASIONS.clear();
		Random random = worldIn.random;
		isNightChangedToDay = false;
		int totalInvasions = !QUEUED_NIGHT_INVASIONS.isEmpty() ? QUEUED_NIGHT_INVASIONS.size() : calculateInvasions(random, amountIn, nightInterval, isCanceledIn, daysIn == 0);
		nightInterval = nightInterval > 0 ? nightInterval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.nightInvasionRarity : random.nextInt(PSConfigValues.common.nightInvasionRarity) + PSConfigValues.common.nightInvasionRarity - (int)(daysIn % PSConfigValues.common.nightInvasionRarity));
		InvasionChart.refresh();
		InvasionChart potentialPrimaryNightInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		InvasionChart potentialSecondaryNightInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		InvasionChart potentialSecondaryDayInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.nightDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY));
		LOGGER.info("Setting Night Invasions: [");
		NIGHT_INVASIONS.setCanceled(QUEUED_NIGHT_INVASIONS.isEmpty() && isCanceledIn);
		if (!QUEUED_NIGHT_INVASIONS.isEmpty()) {
			for (int q = 0; q < QUEUED_NIGHT_INVASIONS.size(); q++) {
				Invasion invasion = QUEUED_NIGHT_INVASIONS.get(q);
				NIGHT_INVASIONS.add(invasion);
				LOGGER.info("Queued Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			QUEUED_NIGHT_INVASIONS.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				InvasionType invasionType = getInvasionType(inv == 0 ? potentialPrimaryNightInvasions : (isNightChangedToDay ? potentialSecondaryDayInvasions : potentialSecondaryNightInvasions), random);
				if (invasionType != null) {
					int severity = random.nextInt(random.nextInt(MathHelper.clamp((int)daysIn/PSConfigValues.common.nightDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					Invasion invasion = new Invasion(invasionType, severity, inv == 0);
					NIGHT_INVASIONS.add(invasion);
					LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					isNightChangedToDay |= invasionType.getTimeModifier() == TimeModifier.NIGHT_TO_DAY;
				}
			}
		}
		LOGGER.info("]");
	}

	public static void setDayTimeEvents(ServerWorld worldIn, boolean isCanceledIn, int amountIn, long daysIn) {
		DAY_INVASIONS.clear();
		Random random = worldIn.random;
		isDayChangedToNight = false;
		int totalInvasions = !QUEUED_DAY_INVASIONS.isEmpty() ? QUEUED_DAY_INVASIONS.size() : calculateInvasions(random, amountIn, dayInterval, isCanceledIn, daysIn == 0);
		dayInterval = dayInterval > 0 ? dayInterval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.dayInvasionRarity : random.nextInt(PSConfigValues.common.dayInvasionRarity) + PSConfigValues.common.dayInvasionRarity - (int)(daysIn % PSConfigValues.common.dayInvasionRarity));
		InvasionChart.refresh();
		InvasionChart potentialPrimaryDayInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		InvasionChart potentialSecondaryDayInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.NIGHT && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		InvasionChart potentialSecondaryNightInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getInvasionTime() != InvasionTime.DAY && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.dayDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT));
		LOGGER.info("Setting Day Invasions: [");
		DAY_INVASIONS.setCanceled(QUEUED_DAY_INVASIONS.isEmpty() && isCanceledIn);
		if (!QUEUED_DAY_INVASIONS.isEmpty()) {
			for (int q = 0; q < QUEUED_DAY_INVASIONS.size(); q++) {
				Invasion invasion = QUEUED_DAY_INVASIONS.get(q);
				DAY_INVASIONS.add(invasion);
				LOGGER.info("Queued Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			QUEUED_DAY_INVASIONS.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				InvasionType invasionType = getInvasionType(inv == 0 ? potentialPrimaryDayInvasions : (isDayChangedToNight ? potentialSecondaryNightInvasions : potentialSecondaryDayInvasions), random);
				if (invasionType != null) {
					int severity = random.nextInt(random.nextInt(MathHelper.clamp((int)daysIn/PSConfigValues.common.dayDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					Invasion invasion = new Invasion(invasionType, severity, inv == 0);
					DAY_INVASIONS.add(invasion);
					LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
					isDayChangedToNight |= invasionType.getTimeModifier() == TimeModifier.DAY_TO_NIGHT;
				}
			}
		}
		LOGGER.info("]");
	}

	private static int calculateInvasions(Random randomIn, int amountIn, int intervalIn, boolean isCanceledIn, boolean isFirstDayIn) {
		return !isFirstDayIn && intervalIn == 0 && amountIn > 0 && !isCanceledIn ? randomIn.nextInt(amountIn) + 1 : 0;
	}

	private static InvasionType getInvasionType(InvasionChart invasionChartIn, Random randomIn) {
		return invasionChartIn.getInvasionInRange(randomIn.nextFloat());
	}

	public static void invasionTick(MinecraftServer serverIn) {
		ServerWorld world = serverIn.overworld();
		if (!world.players().isEmpty() && !NIGHT_INVASIONS.isEmpty() && ServerTimeUtil.isServerNight(serverIn.overworld())) {
			Invasion invasion = NIGHT_INVASIONS.get(serverIn.overworld().getRandom().nextInt(NIGHT_INVASIONS.size()));
			invasion.tick(world);
		} else if (!world.players().isEmpty() && !DAY_INVASIONS.isEmpty() && ServerTimeUtil.isServerDay(serverIn.overworld())) {
			Invasion invasion = DAY_INVASIONS.get(serverIn.overworld().getRandom().nextInt(DAY_INVASIONS.size()));
			invasion.tick(world);
		}
	}

	public static InvasionList getNightInvasions() {
		return NIGHT_INVASIONS;
	}

	public static InvasionList getDayInvasions() {
		return DAY_INVASIONS;
	}
	
	public static ArrayList<Invasion> getQueuedNightInvasions() {
		return QUEUED_NIGHT_INVASIONS;
	}

	public static ArrayList<Invasion> getQueuedDayInvasions() {
		return QUEUED_DAY_INVASIONS;
	}
}
