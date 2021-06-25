package dev.theagameplayer.puresuffering.spawner;

import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.util.TimeUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public final class InvasionSpawner {
	private static final ArrayList<Invasion> NIGHT_INVASIONS = new ArrayList<>();
	private static final ArrayList<Invasion> DAY_INVASIONS = new ArrayList<>();
	private static final ArrayList<Invasion> QUEUED_NIGHT_INVASIONS = new ArrayList<>();
	private static final ArrayList<Invasion> QUEUED_DAY_INVASIONS = new ArrayList<>();
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static boolean isDayChangedToNight = false;
	
	public static void setNightTimeEvents(ServerWorld worldIn, int eventsIn) {
		NIGHT_INVASIONS.clear();
		Invasion.INVASION_MOBS.clear();
		Random random = worldIn.random;
		int events = random.nextInt(eventsIn + 1);
		ArrayList<InvasionType> invasionList = new ArrayList<>(BaseEvents.getInvasionTypeManager().getAllInvasionTypes());
		for (int event = 0; event < events; event++) {
			InvasionType invasionType = getInvasionType(invasionList, random, event, true, false);
			if (invasionType != null && event != events) {
				int severity = random.nextInt(MathHelper.clamp(events, 1, invasionType.getMaxSeverity())) + 1;
				NIGHT_INVASIONS.add(new Invasion(invasionType, severity));
				LOGGER.info("Invasion " + (event + 1) + ": " + invasionType.getId().toString() + " - " + severity);
			}
		}
		if (!QUEUED_NIGHT_INVASIONS.isEmpty()) {
			for (int q = 0; q < QUEUED_NIGHT_INVASIONS.size(); q++) {
				Invasion invasion = QUEUED_NIGHT_INVASIONS.get(q);
				if (!invasion.getType().isRepeatable() && NIGHT_INVASIONS.contains(invasion))
					continue;
				NIGHT_INVASIONS.add(invasion);
			}
			LOGGER.info("Added Queued Night Invasions: " + QUEUED_NIGHT_INVASIONS.toString());
			QUEUED_NIGHT_INVASIONS.clear();
		}
	}
	
	public static void setDayTimeEvents(ServerWorld worldIn, int eventsIn) {
		DAY_INVASIONS.clear();
		Invasion.INVASION_MOBS.clear();
		Random random = worldIn.random;
		int events = random.nextInt(eventsIn + 1);
		isDayChangedToNight = false;
		ArrayList<InvasionType> invasionList = new ArrayList<>(BaseEvents.getInvasionTypeManager().getAllInvasionTypes());
		for (int event = 0; event < events; event++) {
			InvasionType invasionType = getInvasionType(invasionList, random, event, isDayChangedToNight, true);
			if (invasionType != null) {
				int severity = random.nextInt(MathHelper.clamp(events, 1, invasionType.getMaxSeverity())) + 1;
				DAY_INVASIONS.add(new Invasion(invasionType, severity));
				LOGGER.info("Invasion " + (event + 1) + ": " + invasionType.getId().toString() + " - " + severity);
				isDayChangedToNight |= invasionType.setsEventsToNight();
			} else {
				LOGGER.info("Invasion " + (event + 1) + ": NULL");
			}
		}
		if (!QUEUED_DAY_INVASIONS.isEmpty()) {
			for (int q = 0; q < QUEUED_DAY_INVASIONS.size(); q++) {
				Invasion invasion = QUEUED_DAY_INVASIONS.get(q);
				if (!invasion.getType().isRepeatable() && DAY_INVASIONS.contains(invasion))
					continue;
				DAY_INVASIONS.add(invasion);
			}
			LOGGER.info("Added Queued Day Invasions: " + QUEUED_DAY_INVASIONS.toString());
			QUEUED_DAY_INVASIONS.clear();
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
		if (!world.players().isEmpty() && !NIGHT_INVASIONS.isEmpty() && TimeUtil.isServerNight(serverIn.overworld())) {
			Invasion invasion = NIGHT_INVASIONS.get(serverIn.overworld().getRandom().nextInt(NIGHT_INVASIONS.size()));
			invasion.tick(world, NIGHT_INVASIONS);
		} else if (!world.players().isEmpty() && !DAY_INVASIONS.isEmpty() && TimeUtil.isServerDay(serverIn.overworld())) {
			Invasion invasion = DAY_INVASIONS.get(serverIn.overworld().getRandom().nextInt(DAY_INVASIONS.size()));
			invasion.tick(world, DAY_INVASIONS);
		}
	}
	
	public static ArrayList<Invasion> getNightInvasions() {
		return NIGHT_INVASIONS;
	}
	
	public static ArrayList<Invasion> getDayInvasions() {
		return DAY_INVASIONS;
	}
	
	public static ArrayList<Invasion> getQueuedDayInvasions() {
		return QUEUED_DAY_INVASIONS;
	}
	
	public static ArrayList<Invasion> getQueuedNightInvasions() {
		return QUEUED_NIGHT_INVASIONS;
	}
}
