package dev.theagameplayer.puresuffering.world.level;

import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.InvasionStartPacket;
import dev.theagameplayer.puresuffering.network.packet.SendInvasionAmbiencePacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.util.list.InvasionChart;
import dev.theagameplayer.puresuffering.util.list.QueuedInvasionList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class InvasionManager {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private final List<InvasionSessionType> sessionTypes;
	private final InvasionSession[] sessions;
	private final QueuedInvasionList[] queuedInvasions;
	private final int[][] intervals;
	private final int[] cancelIntervals;
	private volatile int activeSession, inactiveSession;
	private volatile long ambienceTime;

	public InvasionManager(final boolean hasFixedTimeIn) {
		this.sessionTypes = hasFixedTimeIn ? List.of(InvasionSessionType.FIXED) : List.of(InvasionSessionType.DAY, InvasionSessionType.NIGHT);
		this.sessions = new InvasionSession[this.sessionTypes.size()];
		this.queuedInvasions = new QueuedInvasionList[this.sessionTypes.size()];
		this.intervals = new int[this.sessionTypes.size()][InvasionDifficulty.values().length];
		this.cancelIntervals = new int[this.sessionTypes.size()];
		for (int i = 0; i < this.sessionTypes.size(); i++) {
			for (final InvasionDifficulty difficulty : InvasionDifficulty.values())
				this.intervals[i][difficulty.ordinal()] = -1;
			this.cancelIntervals[i] = -1;
		}
	}

	public final void setInvasions(final ServerLevel levelIn) {
		final InvasionSessionType sessionType = InvasionSessionType.getActive(levelIn); //If crash comes from here, then perhaps sacrifice optimization for function?
		final int index = this.sessionTypes.indexOf(sessionType);
		final long days = levelIn.getDayTime() / 24000L;
		final int maxPossibleInvasions = sessionType.getMaxPossibleInvasions(levelIn);
		boolean isCanceled = false;
		LOGGER.info("[" + levelIn.dimension().location() + "] " + sessionType.getDefaultName() + ": " + days + ", Max Possible Invasions: " + maxPossibleInvasions);
		if (this.sessions[this.activeSession] != null) {
			this.sessions[this.activeSession].clear(levelIn);
			this.sessions[this.activeSession] = null;
		}
		this.activeSession = index;
		this.inactiveSession = this.sessionTypes.indexOf(InvasionSessionType.getInactive(levelIn));
		if (this.queuedInvasions[index] == null) {
			if (maxPossibleInvasions > 0) {
				final RandomSource random = levelIn.getRandom();
				final InvasionDifficulty difficulty = this.calcInvasionDifficulty(index, days, levelIn, sessionType);
				if (difficulty != null) {
					LOGGER.info("Setting " + difficulty.getDefaultName() + " " + sessionType.getDefaultName() + " Invasions: [");
					if (this.cancelIntervals[index] > 0) {
						this.cancelIntervals[index]--;
					} else {
						final int cancelRarity = sessionType.getCancelRarity(levelIn);
						if (this.calcInvasionCanceled(index, days, maxPossibleInvasions, levelIn, sessionType, difficulty))
							isCanceled = true;
						this.cancelIntervals[index] = cancelRarity > 0 ? random.nextInt(cancelRarity) + cancelRarity - (int)(days % cancelRarity) : 0;
					}
					if (!isCanceled) {
						final int totalInvasions = this.calcInvasionCount(difficulty, random, days, maxPossibleInvasions);
						if (totalInvasions > 0) {
							final int tierIncreaseDelay = sessionType.getTierIncreaseDelay(levelIn);
							boolean[] isTimeModified = new boolean[1]; //A sneaky work around >.>
							final Predicate<InvasionType> potentials = it -> it.getDimensions().contains(levelIn.dimension().location()) && (!PSGameRules.TIERED_INVASIONS.get(levelIn) || days >= it.getTier() * tierIncreaseDelay);
							final Predicate<InvasionType> potentialPrimary = it -> sessionType.isAcceptableTime(it, false) && potentials.test(it) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() || PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()));
							final Predicate<InvasionType> potentialSecondary = it -> potentials.test(it) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
							for (int inv = 0; inv < totalInvasions; inv++) {
								final InvasionType invasionType = this.getInvasionType(new InvasionChart(inv == 0, inv == 0 ? it -> potentialPrimary.test(it) : it -> sessionType.isAcceptableTime(it, isTimeModified[0]) && potentialSecondary.test(it) && (!isTimeModified[0] || sessionType.canBeChanged(it))), random);
								if (invasionType == null || invasionType.getMaxSeverity() < 1) break;
								if (inv == 0) this.sessions[index] = new InvasionSession(sessionType, difficulty);
								final int severity = difficulty.isHyper() ? invasionType.getMaxSeverity() - 1 : random.nextInt(Mth.clamp(PSGameRules.TIERED_INVASIONS.get(levelIn) ? (int)(days/tierIncreaseDelay - invasionType.getTier()) + 1 : invasionType.getMaxSeverity(), 1, inv == 0 ? invasionType.getMaxSeverity() : this.getSecondarySeverityCap(invasionType, index)));
								final Invasion invasion = new Invasion(levelIn, invasionType, severity, inv == 0, true, levelIn.getDayTime(), inv);
								this.sessions[index].add(levelIn, invasion);
								LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
								isTimeModified[0] |= sessionType.canModifyTime(invasionType);
							}
						}
					}
					LOGGER.info("]");
				}
			}
		} else {
			this.sessions[index] = new InvasionSession(sessionType, this.queuedInvasions[index].getDifficulty());
			LOGGER.info("Setting Queued " + this.sessions[index].getDifficulty().getDefaultName() + " " + sessionType.getDefaultName() + " Invasions: [");
			for (int q = 0; q < this.queuedInvasions[index].size(); q++) {
				final Invasion invasion = this.queuedInvasions[index].get(q).build(levelIn, q);
				this.sessions[index].add(levelIn, invasion);
				LOGGER.info("Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedInvasions[index] = null;
			LOGGER.info("]");
		}
		this.ambienceTime = this.intervals[this.inactiveSession][0] == 0 ? 2999L + levelIn.random.nextInt(6000) : -1;
		if (isCanceled || this.sessions[index] != null)
			PSPacketHandler.sendToClientsIn(new InvasionStartPacket(), levelIn);
	}

	private final InvasionDifficulty calcInvasionDifficulty(final int indexIn, final long daysIn, final ServerLevel levelIn, final InvasionSessionType sessionTypeIn) {
		InvasionDifficulty result = null;
		for (final InvasionDifficulty difficulty : InvasionDifficulty.values()) {
			if (!difficulty.isAllowed(levelIn)) break;
			final int i = difficulty.ordinal();
			if (this.intervals[indexIn][i] > 0) {
				this.intervals[indexIn][i]--;
				break;
			} else {
				final int rarity = difficulty.getRarity(levelIn, sessionTypeIn.getRarity(levelIn));
				if (this.intervals[indexIn][i] > -1 && (!PSGameRules.TIERED_INVASIONS.get(levelIn) || daysIn > sessionTypeIn.getTierIncreaseDelay(levelIn) * (difficulty.isHyper() ? i + 2 : i))) result = difficulty;
				this.intervals[indexIn][i] = PSGameRules.CONSISTENT_INVASIONS.get(levelIn) ? rarity : levelIn.random.nextInt(rarity) + rarity - (int)(daysIn % rarity) - 1;
			}
		}
		return result;
	}

	private final boolean calcInvasionCanceled(final int indexIn, final long daysIn, final int maxPossibleInvasionsIn, final ServerLevel levelIn, final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn) {
		return this.cancelIntervals[indexIn] > -1 && (!PSGameRules.TIERED_INVASIONS.get(levelIn) || daysIn > sessionTypeIn.getTierIncreaseDelay(levelIn) * 2) && maxPossibleInvasionsIn > 1 && sessionTypeIn.canBeCanceled(levelIn) && !difficultyIn.isHyper();
	}

	private final int calcInvasionCount(final InvasionDifficulty difficultyIn, final RandomSource randomIn, final long daysIn, final int maxInvasionsIn) {
		return daysIn > 0 && maxInvasionsIn > 0 ? difficultyIn.getInvasionCount(randomIn, maxInvasionsIn) : 0;
	}

	private final InvasionType getInvasionType(final InvasionChart invasionChartIn, final RandomSource randomIn) {
		return invasionChartIn.getInvasionInRange(randomIn.nextFloat());
	}

	private final int getSecondarySeverityCap(final InvasionType invasionTypeIn, int indexIn) {
		final Invasion primary = this.sessions[indexIn].getPrimary();
		final float primaryPercent = (float)(primary.getSeverity() + 1)/primary.getType().getMaxSeverity();
		for (int s = invasionTypeIn.getMaxSeverity(); s > 0; s--) {
			if ((float)s/invasionTypeIn.getMaxSeverity() <= primaryPercent)
				return s;
		}
		return 1;
	}

	public final List<InvasionSessionType> getListTypes() {
		return this.sessionTypes;
	}

	public final InvasionSession getActiveSession(final ServerLevel levelIn) {
		return this.sessions[this.sessionTypes.indexOf(InvasionSessionType.getActive(levelIn))];
	}

	public final InvasionSession getSession(final InvasionSessionType sessionTypeIn) {
		return this.sessions[this.sessionTypes.indexOf(sessionTypeIn)];
	}

	public final QueuedInvasionList setQueued(final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn) {
		return this.queuedInvasions[this.sessionTypes.indexOf(sessionTypeIn)] = difficultyIn == null ? null : new QueuedInvasionList(difficultyIn);
	}

	public final QueuedInvasionList getQueued(final InvasionSessionType sessionTypeIn) {
		return this.queuedInvasions[this.sessionTypes.indexOf(sessionTypeIn)];
	}

	public final void tick(final boolean spawningMonstersIn, final ServerLevel levelIn) {
		if (!spawningMonstersIn) return;
		if (PSGameRules.ENABLE_INVASION_AMBIENCE.get(levelIn) && this.ambienceTime > -1 && levelIn.getDayTime() % 12000L > this.ambienceTime) {
			PSPacketHandler.sendToClientsIn(new SendInvasionAmbiencePacket(), levelIn);
			this.ambienceTime = -1;
		}
		if (this.sessions[this.activeSession] == null) return;
		if (this.sessions[this.activeSession].stopOrTick(levelIn))
			this.sessions[this.activeSession] = null;
	}

	public final void load(final ServerLevel levelIn, final CompoundTag nbtIn) {
		final CompoundTag[] sessionsNBT = new CompoundTag[this.sessionTypes.size()];
		final CompoundTag[] queuedInvasionsNBT = new CompoundTag[this.sessionTypes.size()];
		for (int i = 0; i < this.sessionTypes.size(); i++) {
			final InvasionSessionType sessionType = this.sessionTypes.get(i);
			sessionsNBT[i] = nbtIn.getCompound(sessionType.getDefaultName() + "InvasionSessions");
			queuedInvasionsNBT[i] = nbtIn.getCompound("Queued" + sessionType.getDefaultName() + "Invasions");
			if (!sessionsNBT[i].isEmpty())
				this.sessions[i] = InvasionSession.load(levelIn, sessionsNBT[i]);
			if (!queuedInvasionsNBT[i].isEmpty())
				this.queuedInvasions[i] = QueuedInvasionList.load(levelIn, queuedInvasionsNBT[i]);
			for (final InvasionDifficulty difficulty : InvasionDifficulty.values())
				this.intervals[i][difficulty.ordinal()] = nbtIn.getInt(difficulty.getDefaultName() + sessionType.getDefaultName() + "Interval");
			this.cancelIntervals[i] = nbtIn.getInt(sessionType.getDefaultName() + "CancelInterval");
		}
		this.activeSession = nbtIn.getInt("ActiveSession");
		this.inactiveSession = nbtIn.getInt("InactiveSession");
		this.ambienceTime = nbtIn.getLong("AmbienceTime");
	}

	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final CompoundTag[] sessionsNBT = new CompoundTag[this.sessionTypes.size()];
		final CompoundTag[] queuedInvasionsNBT = new CompoundTag[this.sessionTypes.size()];
		for (int i = 0; i < this.sessionTypes.size(); i++) {
			final InvasionSessionType sessionType = this.sessionTypes.get(i);
			sessionsNBT[i] = this.sessions[i] == null ? new CompoundTag() : this.sessions[i].save();
			nbt.put(sessionType.getDefaultName() + "InvasionSessions", sessionsNBT[i]);
			queuedInvasionsNBT[i] = this.queuedInvasions[i] == null ? new CompoundTag() : this.queuedInvasions[i].save();
			nbt.put("Queued" + sessionType.getDefaultName() + "Invasions", queuedInvasionsNBT[i]);
			for (final InvasionDifficulty difficulty : InvasionDifficulty.values())
				nbt.putInt(difficulty.getDefaultName() + sessionType.getDefaultName() + "Interval", this.intervals[i][difficulty.ordinal()]);
			nbt.putInt(sessionType.getDefaultName() + "CancelInterval", this.cancelIntervals[i]);
		}
		nbt.putInt("ActiveSession", this.activeSession);
		nbt.putInt("InactiveSession", this.inactiveSession);
		nbt.putLong("AmbienceTime", this.ambienceTime);
		return nbt;
	}
}
