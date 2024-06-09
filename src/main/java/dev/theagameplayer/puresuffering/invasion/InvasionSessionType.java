package dev.theagameplayer.puresuffering.invasion;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeChangeability;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

public enum InvasionSessionType {
	DAY("Day", (it, modified) -> modified ? it != InvasionTime.DAY : it != InvasionTime.NIGHT, tm -> tm == TimeModifier.DAY_TO_NIGHT, tc -> tc != TimeChangeability.ONLY_NIGHT) {
		@Override
		public final int getMaxPossibleInvasions(final ServerLevel pLevel) {
			return Mth.clamp((int)(pLevel.getDayTime()/(24000L * this.getTierIncreaseDelay(pLevel))) + 1, 0, PSConfigValues.LEVELS.get(pLevel).maxInvasions[0]);
		}

		@Override
		public final int getRarity(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).invasionSessionTypeRarity[0];
		}

		@Override
		public final int getTierIncreaseDelay(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).tierIncreaseDelay[0];
		}

		@Override
		public final boolean canBeCanceled(final ServerLevel pLevel) {
			return PSGameRules.CANCELABLE_INVASIONS.get(pLevel) && PSConfigValues.LEVELS.get(pLevel).cancelableInvasions[0];
		}

		@Override
		public final int getCancelRarity(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).cancelInvasionRarity[0];
		}
	},
	NIGHT("Night", (it, modified) -> modified ? it != InvasionTime.NIGHT : it != InvasionTime.DAY, tm -> tm == TimeModifier.NIGHT_TO_DAY, tc -> tc != TimeChangeability.ONLY_DAY) {
		@Override
		public final int getMaxPossibleInvasions(final ServerLevel pLevel) {
			return Mth.clamp((int)(pLevel.getDayTime()/(24000L * this.getTierIncreaseDelay(pLevel))) + 1, 0, PSConfigValues.LEVELS.get(pLevel).maxInvasions[1]);
		}

		@Override
		public final int getRarity(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).invasionSessionTypeRarity[1];
		}

		@Override
		public final int getTierIncreaseDelay(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).tierIncreaseDelay[1];
		}

		@Override
		public final boolean canBeCanceled(final ServerLevel pLevel) {
			return PSGameRules.CANCELABLE_INVASIONS.get(pLevel) && PSConfigValues.LEVELS.get(pLevel).cancelableInvasions[1];
		}

		@Override
		public final int getCancelRarity(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).cancelInvasionRarity[1];
		}
	},
	FIXED("Fixed", (it, modified) -> true, tm -> false, tc -> true) {
		@Override
		public final int getMaxPossibleInvasions(final ServerLevel pLevel) {
			return Mth.clamp((int)(pLevel.getDayTime()/(24000L * this.getTierIncreaseDelay(pLevel))) + 1, 0, PSConfigValues.LEVELS.get(pLevel).maxInvasions[0]);
		}

		@Override
		public final int getRarity(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).invasionSessionTypeRarity[0];
		}

		@Override
		public final int getTierIncreaseDelay(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).tierIncreaseDelay[0];
		}

		@Override
		public final boolean canBeCanceled(final ServerLevel pLevel) {
			return PSGameRules.CANCELABLE_INVASIONS.get(pLevel) && PSConfigValues.LEVELS.get(pLevel).cancelableInvasions[0];
		}

		@Override
		public final int getCancelRarity(final ServerLevel pLevel) {
			return PSConfigValues.LEVELS.get(pLevel).cancelInvasionRarity[0];
		}
	};
	
	private final String name;
	private final MutableComponent translation;
	private final BiPredicate<InvasionTime, Boolean> isAcceptableTime;
	private final Predicate<TimeModifier> canModifyTime;
	private final Predicate<TimeChangeability> canBeChanged;
	
	private InvasionSessionType(final String pName, final BiPredicate<InvasionTime, Boolean> pIsAcceptableTime, final Predicate<TimeModifier> pCanModifyTime, final Predicate<TimeChangeability> pCanBeChanged) {
		this.name = pName;
		this.translation = Component.translatable("puresuffering.invasionSessionType." + this.toString());
		this.isAcceptableTime = pIsAcceptableTime;
		this.canModifyTime = pCanModifyTime;
		this.canBeChanged = pCanBeChanged;
	}
	
	@Override
	public final String toString() {
		return super.toString().toLowerCase();
	}
	
	public final String getDefaultName() {
		return this.name;
	}
	
	public final String getTranslation() {
		return this.translation.getString(256);
	}
	
	public final boolean isAcceptableTime(final InvasionType pInvasionType, final boolean pIsTimeModified) {
		return this.isAcceptableTime.test(pInvasionType.getInvasionTime(), pIsTimeModified);
	}
	
	public final boolean canModifyTime(final InvasionType pInvasionType) {
		return this.canModifyTime.test(pInvasionType.getTimeModifier());
	}
	
	public final boolean canBeChanged(final InvasionType pInvasionType) {
		return this.canBeChanged.test(pInvasionType.getTimeChangeability());
	}
	
	public abstract int getMaxPossibleInvasions(final ServerLevel pLevel);
	
	public abstract int getRarity(final ServerLevel pLevel);
	
	public abstract int getTierIncreaseDelay(final ServerLevel pLevel);
	
	public abstract boolean canBeCanceled(final ServerLevel pLevel);
	
	public abstract int getCancelRarity(final ServerLevel pLevel);
	
	public static final InvasionSessionType getActive(final ServerLevel pLevel) {
		return pLevel.dimensionType().hasFixedTime() ? FIXED : (pLevel.getDayTime() % 24000L < 12000L ? DAY : NIGHT);
	}
	
	public static final InvasionSessionType getInactive(final ServerLevel pLevel) {
		return pLevel.dimensionType().hasFixedTime() ? FIXED : (pLevel.getDayTime() % 24000L < 12000L ? NIGHT : DAY);
	}
}
