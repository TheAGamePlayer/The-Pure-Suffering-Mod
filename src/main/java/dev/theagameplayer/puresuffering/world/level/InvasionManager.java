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
import dev.theagameplayer.puresuffering.network.InvasionStartPacket;
import dev.theagameplayer.puresuffering.network.SendInvasionAmbiencePacket;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import dev.theagameplayer.puresuffering.util.list.InvasionChart;
import dev.theagameplayer.puresuffering.util.list.QueuedInvasionList;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.fml.ModList;

public final class InvasionManager {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private final List<InvasionSessionType> sessionTypes;
	private final InvasionSession[] sessions;
	private final QueuedInvasionList[] queuedInvasions;
	private final int[][] intervals;
	private final int[] cancelIntervals;
	private volatile int activeSession, inactiveSession;
	private volatile long ambienceTime;

	public InvasionManager(final boolean pHasFixedTime) {
		this.sessionTypes = pHasFixedTime ? List.of(InvasionSessionType.FIXED) : List.of(InvasionSessionType.DAY, InvasionSessionType.NIGHT);
		this.sessions = new InvasionSession[this.sessionTypes.size()];
		this.queuedInvasions = new QueuedInvasionList[this.sessionTypes.size()];
		this.intervals = new int[this.sessionTypes.size()][InvasionDifficulty.values().length];
		this.cancelIntervals = new int[this.sessionTypes.size()];
		for (int i = 0; i < this.sessionTypes.size(); ++i) {
			for (final InvasionDifficulty difficulty : InvasionDifficulty.values())
				this.intervals[i][difficulty.ordinal()] = -1;
			this.cancelIntervals[i] = -1;
		}
	}

	public final void setInvasions(final ServerLevel pLevel) {
		final InvasionSessionType sessionType = InvasionSessionType.getActive(pLevel); //If crash comes from here, then perhaps sacrifice optimization for function?
		final int index = this.sessionTypes.indexOf(sessionType);
		final long days = pLevel.getDayTime() / 24000L;
		final int maxPossibleInvasions = sessionType.getMaxPossibleInvasions(pLevel);
		boolean isCanceled = false;
		LOGGER.info("[" + pLevel.dimension().location() + "] " + sessionType.getDefaultName() + ": " + days + ", Max Possible Invasions: " + maxPossibleInvasions);
		if (this.sessions[this.activeSession] != null) {
			this.sessions[this.activeSession].clear(pLevel);
			this.sessions[this.activeSession] = null;
		}
		this.activeSession = index;
		this.inactiveSession = this.sessionTypes.indexOf(InvasionSessionType.getInactive(pLevel));
		if (this.queuedInvasions[index] == null) {
			if (maxPossibleInvasions > 0) {
				final RandomSource random = pLevel.getRandom();
				final InvasionDifficulty difficulty = this.calcInvasionDifficulty(index, days, pLevel, sessionType);
				if (difficulty != null) {
					LOGGER.info("Setting " + difficulty.getDefaultName() + " " + sessionType.getDefaultName() + " Invasions: [");
					if (this.cancelIntervals[index] > 0) {
						--this.cancelIntervals[index];
					} else {
						final int cancelRarity = sessionType.getCancelRarity(pLevel);
						if (this.calcInvasionCanceled(index, days, maxPossibleInvasions, pLevel, sessionType, difficulty))
							isCanceled = true;
						this.cancelIntervals[index] = cancelRarity > 0 ? random.nextInt(cancelRarity) + cancelRarity - (int)(days % cancelRarity) : 0;
					}
					if (!isCanceled) {
						final int totalInvasions = this.calcInvasionCount(pLevel, difficulty, random, days, maxPossibleInvasions);
						if (totalInvasions > 0) {
							final int tierIncreaseDelay = sessionType.getTierIncreaseDelay(pLevel);
							boolean[] isTimeModified = new boolean[1]; //A sneaky work around >.>
							final Predicate<InvasionType> potentials = it -> it.getDimensions().contains(pLevel.dimension().location()) && (!PSGameRules.TIERED_INVASIONS.get(pLevel) || days >= it.getTier() * tierIncreaseDelay) && (!ModList.get().isLoaded("gamestages") || it.getGameStages().length < 1 || this.playerHasGameStage(it, pLevel));
							final Predicate<InvasionType> potentialPrimary = it -> sessionType.isAcceptableTime(it, false) && potentials.test(it) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() || PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()));
							final Predicate<InvasionType> potentialSecondary = it -> potentials.test(it) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
							for (int inv = 0; inv < totalInvasions; ++inv) {
								final InvasionType invasionType = this.getInvasionType(new InvasionChart(inv == 0, inv == 0 ? potentialPrimary : it -> sessionType.isAcceptableTime(it, isTimeModified[0]) && potentialSecondary.test(it) && (!isTimeModified[0] || sessionType.canBeChanged(it))), random);
								if (invasionType == null || invasionType.getMaxSeverity() < 1) break;
								if (inv == 0) this.sessions[index] = new InvasionSession(pLevel, sessionType, difficulty, PSGameRules.MOB_KILL_LIMIT.get(pLevel));
								final int severity = difficulty.isHyper() ? invasionType.getMaxSeverity() - 1 : random.nextInt(Mth.clamp(PSGameRules.TIERED_INVASIONS.get(pLevel) ? (int)(days/tierIncreaseDelay - invasionType.getTier()) + 1 : invasionType.getMaxSeverity(), 1, inv == 0 ? invasionType.getMaxSeverity() : this.getSecondarySeverityCap(invasionType, index)));
								final Invasion invasion = new Invasion(pLevel, invasionType, severity, inv == 0, true, pLevel.getDayTime(), inv);
								this.sessions[index].add(pLevel, invasion, false);
								LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
								isTimeModified[0] |= sessionType.canModifyTime(invasionType);
							}
						}
					}
					LOGGER.info("]");
				}
			}
		} else {
			LOGGER.info("Setting Queued " + this.queuedInvasions[index].getDifficulty().getDefaultName() + " " + sessionType.getDefaultName() + " Invasions: [");
			this.sessions[index] = new InvasionSession(pLevel, sessionType, this.queuedInvasions[index].getDifficulty(), PSGameRules.MOB_KILL_LIMIT.get(pLevel));
			for (int q = 0; q < this.queuedInvasions[index].size(); ++q) {
				final Invasion invasion = this.queuedInvasions[index].get(q).build(pLevel, q);
				this.sessions[index].add(pLevel, invasion, false);
				LOGGER.info("Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedInvasions[index] = null;
			LOGGER.info("]");
		}
		this.ambienceTime = this.intervals[this.inactiveSession][0] == 0 ? 2999L + pLevel.random.nextInt(6000) : -1;
		if (isCanceled || this.sessions[index] != null)
			PSPackets.sendToClientsIn(new InvasionStartPacket(PSGameRules.NOTIFY_PLAYERS_ABOUT_INVASIONS.get(pLevel)), pLevel);
	}

	private final InvasionDifficulty calcInvasionDifficulty(final int pIndex, final long pDays, final ServerLevel pLevel, final InvasionSessionType pSessionType) {
		InvasionDifficulty result = null;
		for (final InvasionDifficulty difficulty : InvasionDifficulty.values()) {
			if (!difficulty.isAllowed(pLevel)) break;
			final int i = difficulty.ordinal();
			if (this.intervals[pIndex][i] > 0) {
				--this.intervals[pIndex][i];
				break;
			} else {
				final int rarity = difficulty.getRarity(pLevel, pSessionType.getRarity(pLevel));
				if (this.intervals[pIndex][i] > -1 && (!PSGameRules.TIERED_INVASIONS.get(pLevel) || pDays > pSessionType.getTierIncreaseDelay(pLevel) * (difficulty.isHyper() ? i + 2 : i))) result = difficulty;
				this.intervals[pIndex][i] = PSGameRules.CONSISTENT_INVASIONS.get(pLevel) ? rarity : pLevel.random.nextInt(rarity) + rarity - (int)(pDays % rarity) - 1;
			}
		}
		return result;
	}
	
	private final boolean playerHasGameStage(final InvasionType pInvasionType, final ServerLevel pLevel) {
		for (final ServerPlayer player : pLevel.getPlayers(player -> player.isAlive() && !player.isSpectator())) {
			if (GameStageHelper.hasAllOf(player, pInvasionType.getGameStages())) return true;
		}
		return false;
	}

	private final boolean calcInvasionCanceled(final int pIndex, final long pDays, final int pMaxPossibleInvasions, final ServerLevel pLevel, final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty) {
		return this.cancelIntervals[pIndex] > -1 && (!PSGameRules.TIERED_INVASIONS.get(pLevel) || pDays > pSessionType.getTierIncreaseDelay(pLevel) * 2) && pMaxPossibleInvasions > 1 && pSessionType.canBeCanceled(pLevel) && !pDifficulty.isHyper();
	}

	private final int calcInvasionCount(final ServerLevel pLevel, final InvasionDifficulty pDifficulty, final RandomSource pRandom, final long pDays, final int pMaxInvasions) {
		return pDays > PSGameRules.INVASION_START_DELAY.get(pLevel) - 1 && pMaxInvasions > 0 ? pDifficulty.getInvasionCount(pRandom, pMaxInvasions) : 0;
	}

	private final InvasionType getInvasionType(final InvasionChart pInvasionChart, final RandomSource pRandom) {
		return pInvasionChart.getInvasionInRange(pRandom.nextFloat());
	}

	private final int getSecondarySeverityCap(final InvasionType pInvasionType, int pIndex) {
		final Invasion primary = this.sessions[pIndex].getPrimary();
		final float primaryPercent = (float)(primary.getSeverity() + 1)/primary.getType().getMaxSeverity();
		for (int s = pInvasionType.getMaxSeverity(); s > 0; --s) {
			if ((float)s/pInvasionType.getMaxSeverity() <= primaryPercent)
				return s;
		}
		return 1;
	}

	public final List<InvasionSessionType> getListTypes() {
		return this.sessionTypes;
	}

	public final InvasionSession getActiveSession(final ServerLevel pLevel) {
		return this.sessions[this.sessionTypes.indexOf(InvasionSessionType.getActive(pLevel))];
	}

	public final InvasionSession getSession(final InvasionSessionType pSessionType) {
		return this.sessions[this.sessionTypes.indexOf(pSessionType)];
	}

	public final QueuedInvasionList setQueued(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty) {
		return this.queuedInvasions[this.sessionTypes.indexOf(pSessionType)] = pDifficulty == null ? null : new QueuedInvasionList(pDifficulty);
	}

	public final QueuedInvasionList getQueued(final InvasionSessionType pSessionType) {
		return this.queuedInvasions[this.sessionTypes.indexOf(pSessionType)];
	}

	public final void tick(final ServerLevel pLevel) {
		if (PSGameRules.ENABLE_INVASION_AMBIENCE.get(pLevel) && this.ambienceTime > -1 && pLevel.getDayTime() % 12000L > this.ambienceTime) {
			PSPackets.sendToClientsIn(new SendInvasionAmbiencePacket(), pLevel);
			this.ambienceTime = -1;
		}
		if (this.sessions[this.activeSession] == null) return;
		if (this.sessions[this.activeSession].stopOrTick(pLevel))
			this.sessions[this.activeSession] = null;
	}

	public final void load(final ServerLevel pLevel, final CompoundTag pNbt) {
		final CompoundTag[] sessionsNBT = new CompoundTag[this.sessionTypes.size()];
		final CompoundTag[] queuedInvasionsNBT = new CompoundTag[this.sessionTypes.size()];
		for (int i = 0; i < this.sessionTypes.size(); ++i) {
			final InvasionSessionType sessionType = this.sessionTypes.get(i);
			sessionsNBT[i] = pNbt.getCompound(sessionType.getDefaultName() + "InvasionSessions");
			queuedInvasionsNBT[i] = pNbt.getCompound("Queued" + sessionType.getDefaultName() + "Invasions");
			if (!sessionsNBT[i].isEmpty())
				this.sessions[i] = InvasionSession.load(pLevel, sessionsNBT[i]);
			if (!queuedInvasionsNBT[i].isEmpty())
				this.queuedInvasions[i] = QueuedInvasionList.load(pLevel, queuedInvasionsNBT[i]);
			for (final InvasionDifficulty difficulty : InvasionDifficulty.values())
				this.intervals[i][difficulty.ordinal()] = pNbt.getInt(difficulty.getDefaultName() + sessionType.getDefaultName() + "Interval");
			this.cancelIntervals[i] = pNbt.getInt(sessionType.getDefaultName() + "CancelInterval");
		}
		this.activeSession = pNbt.getInt("ActiveSession");
		this.inactiveSession = pNbt.getInt("InactiveSession");
		this.ambienceTime = pNbt.getLong("AmbienceTime");
	}

	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final CompoundTag[] sessionsNBT = new CompoundTag[this.sessionTypes.size()];
		final CompoundTag[] queuedInvasionsNBT = new CompoundTag[this.sessionTypes.size()];
		for (int i = 0; i < this.sessionTypes.size(); ++i) {
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
