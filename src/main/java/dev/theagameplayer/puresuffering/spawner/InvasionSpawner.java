package dev.theagameplayer.puresuffering.spawner;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.util.TimeUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public class InvasionSpawner {
	private static final ArrayList<Pair<InvasionType, Integer>> NIGHT_INVASIONS = new ArrayList<>();
	private static final ArrayList<Pair<InvasionType, Integer>> DAY_INVASIONS = new ArrayList<>();
	private static final ArrayList<Pair<InvasionType, Integer>> QUEUED_INVASIONS = new ArrayList<>();
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static boolean isDayChangedToNight = false;
	private static int invasionToTick = 0;
	
	public static void setNightTimeEvents(ServerWorld worldIn, int eventsIn) {
		NIGHT_INVASIONS.clear();
		Random random = worldIn.random;
		int events = random.nextInt(eventsIn + 1);
		ArrayList<InvasionType> invasionList = new ArrayList<>(BaseEvents.getInvasionTypeManager().getAllInvasionTypes());
		for (int event = 0; event < events; event++) {
			InvasionType invasionType = getInvasionType(invasionList, random, event, true, false);
			if (invasionType != null && event != events) {
				int severity = random.nextInt(MathHelper.clamp(events, 1, invasionType.getMaxSeverity())) + 1;
				NIGHT_INVASIONS.add(Pair.of(invasionType, severity));
				LOGGER.info("Invasion " + (event + 1) + ": " + invasionType.getId().toString() + " - " + severity);
			}
		}
		if (!QUEUED_INVASIONS.isEmpty()) {
			NIGHT_INVASIONS.addAll(QUEUED_INVASIONS);
			LOGGER.info("Added Queued Night Invasions: " + QUEUED_INVASIONS.toString());
			QUEUED_INVASIONS.clear();
		}
	}
	
	public static void setDayTimeEvents(ServerWorld worldIn, int eventsIn) {
		DAY_INVASIONS.clear();
		Random random = worldIn.random;
		int events = random.nextInt(eventsIn + 1);
		isDayChangedToNight = false;
		ArrayList<InvasionType> invasionList = new ArrayList<>(BaseEvents.getInvasionTypeManager().getAllInvasionTypes());
		for (int event = 0; event < events; event++) {
			InvasionType invasionType = getInvasionType(invasionList, random, event, isDayChangedToNight, true);
			if (invasionType != null) {
				int severity = random.nextInt(MathHelper.clamp(events, 1, invasionType.getMaxSeverity())) + 1;
				DAY_INVASIONS.add(Pair.of(invasionType, severity));
				LOGGER.info("Invasion " + (event + 1) + ": " + invasionType.getId().toString() + " - " + severity);
				isDayChangedToNight |= invasionType.setsEventsToNight();
			} else {
				LOGGER.info("Invasion " + (event + 1) + ": NULL");
			}
		}
		if (!QUEUED_INVASIONS.isEmpty()) {
			DAY_INVASIONS.addAll(QUEUED_INVASIONS);
			LOGGER.info("Added Queued Day Invasions: " + QUEUED_INVASIONS.toString());
			QUEUED_INVASIONS.clear();
		}
	}
	
	private static InvasionType getInvasionType(ArrayList<InvasionType> invasionListIn, Random randomIn, int eventIn, boolean nightInvasionIn, boolean isByChanceIn) {
		InvasionType invasionType = invasionListIn.get(randomIn.nextInt(invasionListIn.size()));
		if (invasionType.isDayInvasion() != nightInvasionIn) {
			if (randomIn.nextInt(invasionType.getRarity() + 1) != 0 || (invasionType.isOnlyDuringNight() && isByChanceIn))
				return getInvasionType(invasionListIn, randomIn, eventIn, nightInvasionIn, isByChanceIn);
			if (!invasionType.isRepeatable() && invasionListIn.contains(invasionType))
				invasionListIn.remove(invasionType);
			return invasionType;
		} else {
			return isByChanceIn ? null : getInvasionType(invasionListIn, randomIn, eventIn, nightInvasionIn, isByChanceIn);
		}
	}
	
	public static void invasionTick(MinecraftServer serverIn) {
		ServerWorld world = serverIn.overworld();
		if (!world.players().isEmpty() && !NIGHT_INVASIONS.isEmpty() && TimeUtil.isNight(serverIn.overworld())) {
			if (NIGHT_INVASIONS.size() <= invasionToTick)
				invasionToTick = 0;
			Pair<InvasionType, Integer> invasion = NIGHT_INVASIONS.get(invasionToTick);
			invasion.getLeft().tick(world, NIGHT_INVASIONS, invasion);
		} else if (!world.players().isEmpty() && !DAY_INVASIONS.isEmpty() && TimeUtil.isDay(serverIn.overworld())) {
			if (DAY_INVASIONS.size() <= invasionToTick)
				invasionToTick = 0;
			Pair<InvasionType, Integer> invasion = DAY_INVASIONS.get(invasionToTick);
			invasion.getLeft().tick(world, DAY_INVASIONS, invasion);
		}
		if ((!NIGHT_INVASIONS.isEmpty() && TimeUtil.isNight(serverIn.overworld())) || (!DAY_INVASIONS.isEmpty() && TimeUtil.isDay(serverIn.overworld()))) {
			invasionToTick++;
		} else {
			invasionToTick = 0;
		}
	}
	
	public static ArrayList<Pair<InvasionType, Integer>> getNightInvasions() {
		return NIGHT_INVASIONS;
	}
	
	public static ArrayList<Pair<InvasionType, Integer>> getDayInvasions() {
		return DAY_INVASIONS;
	}
	
	public static ArrayList<Pair<InvasionType, Integer>> getQueuedInvasions() {
		return QUEUED_INVASIONS;
	}
	
	public static boolean isDayChangedToNight() {
		return isDayChangedToNight;
	}
}
