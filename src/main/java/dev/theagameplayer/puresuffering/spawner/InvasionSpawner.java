package dev.theagameplayer.puresuffering.spawner;

import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.PSConfig;
import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.network.InvasionListType;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public final class InvasionSpawner {
	private static final InvasionList NIGHT_INVASIONS = new InvasionList(InvasionListType.NIGHT);
	private static final InvasionList DAY_INVASIONS = new InvasionList(InvasionListType.DAY);
	private static final ArrayList<Invasion> QUEUED_NIGHT_INVASIONS = new ArrayList<>();
	private static final ArrayList<Invasion> QUEUED_DAY_INVASIONS = new ArrayList<>();
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static boolean isDayChangedToNight = false;
	
	public static void setNightTimeEvents(ServerWorld worldIn, int eventsIn) {
		NIGHT_INVASIONS.clear();
		Invasion.INVASION_MOBS.clear();
		Random random = worldIn.random;
		int events = random.nextInt(PSConfig.COMMON.nightInvasionRarity.get()) == 0 ? random.nextInt(eventsIn + 1) : 0;
		ArrayList<InvasionType> invasionList = new ArrayList<>(BaseEvents.getInvasionTypeManager().getNightInvasionTypes());
		LOGGER.info("Setting Night Invasions: [");
		for (int event = 0; event < events; event++) {
			InvasionType invasionType = getInvasionType(invasionList, random, event, false);
			if (invasionType != null) {
				int severity = random.nextInt(random.nextInt(MathHelper.clamp(events, 1, invasionType.getMaxSeverity())) + 1) + 1;
				Invasion invasion = new Invasion(invasionType, severity);
				NIGHT_INVASIONS.add(invasion);
				LOGGER.info("Invasion " + (event + 1) + ": " + invasionType + " - " + severity);
			} else {
				LOGGER.info("Invasion " + (event + 1) + ": NULL");
			}
		}
		if (!QUEUED_NIGHT_INVASIONS.isEmpty()) {
			for (int q = 0; q < QUEUED_NIGHT_INVASIONS.size(); q++) {
				Invasion invasion = QUEUED_NIGHT_INVASIONS.get(q);
				if (!invasion.getType().isRepeatable() && NIGHT_INVASIONS.contains(invasion))
					continue;
				NIGHT_INVASIONS.add(invasion);
				LOGGER.info("Queued Invasion " + (events + 1) + ": " + invasion.getType() + " - " + invasion.getSeverity());
				events++;
			}
			QUEUED_NIGHT_INVASIONS.clear();
		}
		LOGGER.info("]");
	}
	
	public static void setDayTimeEvents(ServerWorld worldIn, int eventsIn) {
		DAY_INVASIONS.clear();
		Invasion.INVASION_MOBS.clear();
		Random random = worldIn.random;
		int events = random.nextInt(PSConfig.COMMON.dayInvasionRarity.get()) == 0 ? random.nextInt(eventsIn + 1) : 0;
		isDayChangedToNight = false;
		ArrayList<InvasionType> invasionList = new ArrayList<>(BaseEvents.getInvasionTypeManager().getDayInvasionTypes());
		ArrayList<InvasionType> invasionList1 = new ArrayList<>(BaseEvents.getInvasionTypeManager().getNightInvasionTypes());
		LOGGER.info("Setting Day Invasions: [");
		for (int event = 0; event < events; event++) {
			InvasionType invasionType = getInvasionType(isDayChangedToNight ? invasionList1 : invasionList, random, event, true);
			if (invasionType != null) {
				int severity = random.nextInt(random.nextInt(MathHelper.clamp(events, 1, invasionType.getMaxSeverity())) + 1) + 1;
				Invasion invasion = new Invasion(invasionType, severity);
				DAY_INVASIONS.add(invasion);
				LOGGER.info("Invasion " + (event + 1) + ": " + invasionType + " - " + severity);
				isDayChangedToNight |= invasionType.setsEventsToNight();
			} else {
				LOGGER.info("Invasion " + (event + 1) + ": NULL");
			}
		};
		if (!QUEUED_DAY_INVASIONS.isEmpty()) {
			for (int q = 0; q < QUEUED_DAY_INVASIONS.size(); q++) {
				Invasion invasion = QUEUED_DAY_INVASIONS.get(q);
				if (!invasion.getType().isRepeatable() && DAY_INVASIONS.contains(invasion))
					continue;
				DAY_INVASIONS.add(invasion);
				LOGGER.info("Queued Invasion " + (events + 1) + ": " + invasion.getType() + " - " + invasion.getSeverity());
				events++;
			}
			QUEUED_DAY_INVASIONS.clear();
		}
		LOGGER.info("]");
	}
	
	private static InvasionType getInvasionType(ArrayList<InvasionType> invasionListIn, Random randomIn, int eventIn, boolean isDayIn) {
		InvasionType invasionType = invasionListIn.get(randomIn.nextInt(invasionListIn.size()));
		int chance = isDayIn ? randomIn.nextInt(invasionListIn.size()) : 0;
		if (chance == 0 && !(eventIn == 0 && !invasionType.getSkyRenderer().isEmpty())) {
			if (randomIn.nextInt(invasionType.getRarity() + 1) != 0 || (invasionType.isOnlyDuringNight() && isDayIn))
				return getInvasionType(invasionListIn, randomIn, eventIn, isDayIn);
			if (!invasionType.isRepeatable() && invasionListIn.contains(invasionType))
				invasionListIn.remove(invasionType);
			return invasionType;
		} else {
			return null;
		}
	}
	
	public static void invasionTick(MinecraftServer serverIn) {
		ServerWorld world = serverIn.overworld();
		if (!world.players().isEmpty() && !NIGHT_INVASIONS.isEmpty() && ServerTimeUtil.isServerNight(serverIn.overworld())) {
			Invasion invasion = NIGHT_INVASIONS.get(serverIn.overworld().getRandom().nextInt(NIGHT_INVASIONS.size()));
			invasion.tick(world, NIGHT_INVASIONS);
		} else if (!world.players().isEmpty() && !DAY_INVASIONS.isEmpty() && ServerTimeUtil.isServerDay(serverIn.overworld())) {
			Invasion invasion = DAY_INVASIONS.get(serverIn.overworld().getRandom().nextInt(DAY_INVASIONS.size()));
			invasion.tick(world, DAY_INVASIONS);
		}
	}
	
	public static InvasionList getNightInvasions() {
		return NIGHT_INVASIONS;
	}
	
	public static InvasionList getDayInvasions() {
		return DAY_INVASIONS;
	}
	
	public static ArrayList<Invasion> getQueuedDayInvasions() {
		return QUEUED_DAY_INVASIONS;
	}
	
	public static ArrayList<Invasion> getQueuedNightInvasions() {
		return QUEUED_NIGHT_INVASIONS;
	}
}
