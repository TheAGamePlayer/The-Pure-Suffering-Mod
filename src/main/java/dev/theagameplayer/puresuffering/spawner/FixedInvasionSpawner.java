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
import dev.theagameplayer.puresuffering.util.InvasionChart;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.InvasionListType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;

public final class FixedInvasionSpawner {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private final InvasionList invasions = new InvasionList(InvasionListType.FIXED);
	private final ArrayList<Invasion> queuedInvasions = new ArrayList<>();
	private int interval;

	public void setInvasions(final ServerLevel levelIn, final boolean isCanceledIn, final int amountIn, final long daysIn) {
		this.invasions.clear();
		final RandomSource random = levelIn.random;
		final int totalInvasions = !this.queuedInvasions.isEmpty() ? this.queuedInvasions.size() : this.calculateInvasions(random, amountIn, this.interval, isCanceledIn, daysIn == 0);
		this.interval = this.interval > 0 ? this.interval - 1 : (PSConfigValues.common.consistentInvasions ? PSConfigValues.common.fixedInvasionRarity : random.nextInt(PSConfigValues.common.fixedInvasionRarity) + PSConfigValues.common.fixedInvasionRarity - (int)(daysIn % PSConfigValues.common.fixedInvasionRarity));
		InvasionChart.refresh();
		final InvasionChart potentialPrimaryInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.fixedDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && (PSConfigValues.common.primaryWhitelist.isEmpty() ? true : PSConfigValues.common.primaryWhitelist.contains(it.getId().toString()))));
		final InvasionChart potentialSecondaryInvasions = new InvasionChart(BaseEvents.getInvasionTypeManager().getInvasionTypesOf(it -> it.getDimensions().contains(levelIn.dimension().location()) && (PSConfigValues.common.tieredInvasions ? daysIn >= it.getTier() * PSConfigValues.common.fixedDifficultyIncreaseDelay : true) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY));
		LOGGER.info("Setting Fixed Invasions: [");
		this.invasions.setCanceled(this.queuedInvasions.isEmpty() && isCanceledIn);
		if (!this.queuedInvasions.isEmpty()) {
			for (int q = 0; q < this.queuedInvasions.size(); q++) {
				final Invasion invasion = this.queuedInvasions.get(q);
				this.invasions.add(invasion);
				LOGGER.info("Queued Invasion " + (q + 1) + ": " + invasion.getType() + " - " + (invasion.getSeverity() + 1));
			}
			this.queuedInvasions.clear();
		} else {
			for (int inv = 0; inv < totalInvasions; inv++) {
				final InvasionType invasionType = this.getInvasionType(inv == 0 ? potentialPrimaryInvasions : potentialSecondaryInvasions, random);
				if (invasionType != null) {
					final int severity = random.nextInt(random.nextInt(Mth.clamp((int)daysIn/PSConfigValues.common.fixedDifficultyIncreaseDelay - invasionType.getTier(), 1, invasionType.getMaxSeverity())) + 1);
					final Invasion invasion = new Invasion(invasionType, severity, inv == 0);
					this.invasions.add(invasion);
					LOGGER.info("Invasion " + (inv + 1) + ": " + invasionType + " - " + (severity + 1));
				}
			}
		}
		LOGGER.info("]");
	}

	private int calculateInvasions(final RandomSource randomIn, final int amountIn, final int intervalIn, final boolean isCanceledIn, final boolean isFirstDayIn) {
		return !isFirstDayIn && intervalIn == 0 && amountIn > 0 && !isCanceledIn ? randomIn.nextInt(amountIn) + 1 : 0;
	}

	private InvasionType getInvasionType(final InvasionChart invasionChartIn, final RandomSource randomIn) {
		return invasionChartIn.getInvasionInRange(randomIn.nextFloat());
	}

	public void invasionTick(final MinecraftServer serverIn, final ServerLevel levelIn) {
		if (!this.invasions.isEmpty()) {
			Invasion invasion = this.invasions.get(levelIn.getRandom().nextInt(this.invasions.size()));
			invasion.tick(levelIn);
		}
	}

	public void load(final CompoundTag nbtIn) {
		final ListTag invasionsNBT = nbtIn.getList("Invasions", Tag.TAG_COMPOUND);
		final ListTag queuedInvasionsNBT = nbtIn.getList("QueuedInvasions", Tag.TAG_COMPOUND);
		for (final Tag inbt : invasionsNBT)
			this.invasions.add(Invasion.load((CompoundTag)inbt));
		for (final Tag inbt : queuedInvasionsNBT)
			this.queuedInvasions.add(Invasion.load((CompoundTag)inbt));
		this.interval = nbtIn.getInt("Interval");
	}

	public CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final ListTag invasionsNBT = new ListTag();
		final ListTag queuedInvasionsNBT = new ListTag();
		for (final Invasion invasion : this.invasions)
			invasionsNBT.add(invasion.save());
		for (final Invasion invasion : this.queuedInvasions)
			invasionsNBT.add(invasion.save());
		nbt.put("Invasions", invasionsNBT);
		nbt.put("QueuedInvasions", queuedInvasionsNBT);
		nbt.putInt("Interval", this.interval);
		return nbt;
	}

	public InvasionList getInvasions() {
		return this.invasions;
	}

	public ArrayList<Invasion> getQueuedInvasions() {
		return this.queuedInvasions;
	}
}
