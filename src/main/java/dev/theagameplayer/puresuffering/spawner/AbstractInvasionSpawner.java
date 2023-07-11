package dev.theagameplayer.puresuffering.spawner;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.util.InvasionChart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public abstract class AbstractInvasionSpawner {
	protected final HyperType calculateInvasionDifficulty(final int[] intervalsIn, final int invasionRarityIn, final int difficultyIncreaseDelayIn, final RandomSource randomIn, final long daysIn) {
		HyperType result = null;
		if (intervalsIn[HyperType.DEFAULT.ordinal()] > 0) {
			intervalsIn[HyperType.DEFAULT.ordinal()]--;
		} else {
			if (PSConfigValues.common.hyperInvasions && PSConfigValues.common.hyperCharge) {
				if (intervalsIn[HyperType.HYPER.ordinal()] > 0) {
					intervalsIn[HyperType.HYPER.ordinal()]--;
				} else {
					if (intervalsIn[HyperType.NIGHTMARE.ordinal()] > 0) {
						intervalsIn[HyperType.NIGHTMARE.ordinal()]--;
					} else {
						if (result == null && intervalsIn[HyperType.NIGHTMARE.ordinal()] > -1 && daysIn > difficultyIncreaseDelayIn * HyperType.NIGHTMARE.ordinal()) result = HyperType.NIGHTMARE;
						intervalsIn[HyperType.NIGHTMARE.ordinal()] = PSConfigValues.common.consistentInvasions ? PSConfigValues.common.nightmareInvasionRarity : randomIn.nextInt(PSConfigValues.common.nightmareInvasionRarity) + PSConfigValues.common.nightmareInvasionRarity - (int)(daysIn % PSConfigValues.common.nightmareInvasionRarity) - 1;
					}
					if (result == null && intervalsIn[HyperType.HYPER.ordinal()] > -1 && daysIn > difficultyIncreaseDelayIn * HyperType.HYPER.ordinal()) result = HyperType.HYPER;
					intervalsIn[HyperType.HYPER.ordinal()] = PSConfigValues.common.consistentInvasions ? PSConfigValues.common.hyperInvasionRarity : randomIn.nextInt(PSConfigValues.common.hyperInvasionRarity) + PSConfigValues.common.hyperInvasionRarity - (int)(daysIn % PSConfigValues.common.hyperInvasionRarity) - 1;
				}
			}
			if (result == null && intervalsIn[HyperType.DEFAULT.ordinal()] > -1) result = HyperType.DEFAULT;
			intervalsIn[HyperType.DEFAULT.ordinal()] = PSConfigValues.common.consistentInvasions ? invasionRarityIn : randomIn.nextInt(invasionRarityIn) + invasionRarityIn - (int)(daysIn % invasionRarityIn) - 1;
		}
		return result;
	}
	
	protected final boolean calculateInvasionCanceled(final int maxInvasionsIn, final boolean canBeCanceledIn, final HyperType difficultyIn) {
		return maxInvasionsIn > 1 && canBeCanceledIn && difficultyIn == HyperType.DEFAULT;
	}

	protected final int calculateInvasions(final RandomSource randomIn, final long daysIn, final int maxInvasionsIn, final boolean isCanceledIn, final HyperType difficultyIn) {
		return daysIn > 0 && maxInvasionsIn > 0 && !isCanceledIn && difficultyIn != null ? randomIn.nextInt(maxInvasionsIn) + 1 : 0;
	}

	protected final InvasionType getInvasionType(final InvasionChart invasionChartIn, final RandomSource randomIn) {
		return invasionChartIn.getInvasionInRange(randomIn.nextFloat());
	}

	public abstract void invasionTick(final MinecraftServer serverIn, final ServerLevel levelIn);

	public abstract void load(final CompoundTag nbtIn);

	public abstract CompoundTag save();
}
