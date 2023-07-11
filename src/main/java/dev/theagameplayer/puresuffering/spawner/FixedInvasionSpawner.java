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
import dev.theagameplayer.puresuffering.util.InvasionChart;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;

public final class FixedInvasionSpawner extends AbstractInvasionSpawner {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private final InvasionList invasions = new InvasionList(InvasionListType.FIXED);
	private final ArrayList<Invasion> queuedInvasions = new ArrayList<>();
	private final int[] intervals = {-1, -1, -1};
	private int cancelInterval = -1;

	public final void setInvasions(final ServerLevel levelIn, final long daysIn, final int maxInvasionsIn) {
		this.invasions.clear();
		this.invasions.setCanceled(false);
		if (!this.queuedInvasions.isEmpty()) {
			LOGGER.info("Setting Queued Fixed Invasions: [");
			for (int q = 0; q < this.queuedInvasions.size(); q++) {
				final Invasion invasion = this.queuedInvasions.get(q);
				this.invasions.add(invasion);
				LOGGER.info((invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.NIGHTMARE ? "Nightmare " : "Hyper ") : "") + "Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedInvasions.clear();
		} else {
			final RandomSource random = levelIn.random;
			final HyperType difficulty = this.calculateInvasionDifficulty(this.intervals, PSConfigValues.common.fixedInvasionRarity, PSConfigValues.common.fixedDifficultyIncreaseDelay, random, daysIn);
			if (difficulty != null) {
				if (this.cancelInterval > 0) {
					this.cancelInterval--;
				} else {
					if (this.cancelInterval > -1 && daysIn > PSConfigValues.common.fixedDifficultyIncreaseDelay)
						this.invasions.setCanceled(this.calculateInvasionCanceled(maxInvasionsIn, PSConfigValues.common.canFixedInvasionsBeCanceled, difficulty));
					this.cancelInterval = random.nextInt(PSConfigValues.common.fixedCancelChance) + PSConfigValues.common.fixedCancelChance - (int)(daysIn % PSConfigValues.common.fixedCancelChance);
				}
			}
			final int totalInvasions = this.calculateInvasions(random, daysIn, maxInvasionsIn, this.invasions.isCanceled(), difficulty);
			InvasionChart.refresh();
			final InvasionChart potentialPrimaryInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.fixedDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
			final InvasionChart potentialSecondaryInvasions = new InvasionChart(PSBaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.fixedDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
			LOGGER.info("Setting Fixed Invasions: [");
			for (int inv = 0; inv < totalInvasions; inv++) {
				final InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryInvasions : potentialSecondaryInvasions, random);
				if (invasionType != null) {
					final int severity = difficulty != HyperType.DEFAULT ? invasionType.getMaxSeverity() - 1 : random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.fixedDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					final Invasion invasion = new Invasion(invasionType, severity, inv == 0, difficulty);
					this.invasions.add(invasion);
					LOGGER.info((difficulty != HyperType.DEFAULT ? (difficulty == HyperType.NIGHTMARE ? "Nightmare " : "Hyper ") : "") + "Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
				} else {
					break;
				}
			}
		}
		LOGGER.info("]");
	}

	@Override
	public final void invasionTick(final MinecraftServer serverIn, final ServerLevel levelIn) {
		if (!this.invasions.isEmpty()) {
			final Invasion invasion = this.invasions.get(levelIn.getRandom().nextInt(this.invasions.size()));
			final ServerChunkCache chunkSource = levelIn.getChunkSource();
			invasion.tick(levelIn, chunkSource.spawnEnemies, chunkSource.spawnFriendlies);
		}
	}

	@Override
	public final void load(final CompoundTag nbtIn) {
		final ListTag invasionsNBT = nbtIn.getList("Invasions", Tag.TAG_COMPOUND);
		final ListTag queuedInvasionsNBT = nbtIn.getList("QueuedInvasions", Tag.TAG_COMPOUND);
		for (final Tag inbt : invasionsNBT)
			this.invasions.add(Invasion.load((CompoundTag)inbt));
		for (final Tag inbt : queuedInvasionsNBT)
			this.queuedInvasions.add(Invasion.load((CompoundTag)inbt));
		for (final HyperType hyperType : HyperType.values())
			this.intervals[hyperType.ordinal()] = nbtIn.getInt(hyperType.toString() + "Interval");
		this.cancelInterval = nbtIn.getInt("CancelInterval");
	}

	@Override
	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final ListTag invasionsNBT = new ListTag();
		final ListTag queuedInvasionsNBT = new ListTag();
		for (final Invasion invasion : this.invasions)
			invasionsNBT.add(invasion.save());
		for (final Invasion invasion : this.queuedInvasions)
			invasionsNBT.add(invasion.save());
		nbt.put("Invasions", invasionsNBT);
		nbt.put("QueuedInvasions", queuedInvasionsNBT);
		for (final HyperType hyperType : HyperType.values())
			nbt.putInt(hyperType.toString() + "Interval", this.intervals[hyperType.ordinal()]);
		nbt.putInt("CancelInterval", this.cancelInterval);
		return nbt;
	}

	public final InvasionList getInvasions() {
		return this.invasions;
	}

	public final ArrayList<Invasion> getQueuedInvasions() {
		return this.queuedInvasions;
	}
}
