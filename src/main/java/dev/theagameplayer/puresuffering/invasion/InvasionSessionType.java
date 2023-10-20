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
		public final int getMaxPossibleInvasions(final ServerLevel levelIn) {
			return Mth.clamp((int)(levelIn.getDayTime()/(24000L * this.getTierIncreaseDelay(levelIn))) + 1, 0, PSConfigValues.LEVELS.get(levelIn).maxInvasions[0]);
		}

		@Override
		public final int getRarity(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).invasionSessionTypeRarity[0];
		}

		@Override
		public final int getTierIncreaseDelay(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).tierIncreaseDelay[0];
		}

		@Override
		public final boolean canBeCanceled(final ServerLevel levelIn) {
			return PSGameRules.CANCELABLE_INVASIONS.get(levelIn) && PSConfigValues.LEVELS.get(levelIn).cancelableInvasions[0];
		}

		@Override
		public final int getCancelRarity(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).cancelInvasionRarity[0];
		}
	},
	NIGHT("Night", (it, modified) -> modified ? it != InvasionTime.NIGHT : it != InvasionTime.DAY, tm -> tm == TimeModifier.NIGHT_TO_DAY, tc -> tc != TimeChangeability.ONLY_DAY) {
		@Override
		public final int getMaxPossibleInvasions(final ServerLevel levelIn) {
			return Mth.clamp((int)(levelIn.getDayTime()/(24000L * this.getTierIncreaseDelay(levelIn))) + 1, 0, PSConfigValues.LEVELS.get(levelIn).maxInvasions[1]);
		}

		@Override
		public final int getRarity(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).invasionSessionTypeRarity[1];
		}

		@Override
		public final int getTierIncreaseDelay(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).tierIncreaseDelay[1];
		}

		@Override
		public final boolean canBeCanceled(final ServerLevel levelIn) {
			return PSGameRules.CANCELABLE_INVASIONS.get(levelIn) && PSConfigValues.LEVELS.get(levelIn).cancelableInvasions[1];
		}

		@Override
		public final int getCancelRarity(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).cancelInvasionRarity[1];
		}
	},
	FIXED("Fixed", (it, modified) -> true, tm -> false, tc -> true) {
		@Override
		public final int getMaxPossibleInvasions(final ServerLevel levelIn) {
			return Mth.clamp((int)(levelIn.getDayTime()/(24000L * this.getTierIncreaseDelay(levelIn))) + 1, 0, PSConfigValues.LEVELS.get(levelIn).maxInvasions[0]);
		}

		@Override
		public final int getRarity(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).invasionSessionTypeRarity[0];
		}

		@Override
		public final int getTierIncreaseDelay(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).tierIncreaseDelay[0];
		}

		@Override
		public final boolean canBeCanceled(final ServerLevel levelIn) {
			return PSGameRules.CANCELABLE_INVASIONS.get(levelIn) && PSConfigValues.LEVELS.get(levelIn).cancelableInvasions[0];
		}

		@Override
		public final int getCancelRarity(final ServerLevel levelIn) {
			return PSConfigValues.LEVELS.get(levelIn).cancelInvasionRarity[0];
		}
	};
	
	private final String name;
	private final MutableComponent translation;
	private final BiPredicate<InvasionTime, Boolean> isAcceptableTime;
	private final Predicate<TimeModifier> canModifyTime;
	private final Predicate<TimeChangeability> canBeChanged;
	
	private InvasionSessionType(final String nameIn, final BiPredicate<InvasionTime, Boolean> isAcceptableTimeIn, final Predicate<TimeModifier> canModifyTimeIn, final Predicate<TimeChangeability> canBeChangedIn) {
		this.name = nameIn;
		this.translation = Component.translatable("puresuffering.invasionSessionType." + this.toString());
		this.isAcceptableTime = isAcceptableTimeIn;
		this.canModifyTime = canModifyTimeIn;
		this.canBeChanged = canBeChangedIn;
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
	
	public final boolean isAcceptableTime(final InvasionType invasionTypeIn, final boolean isTimeModifiedIn) {
		return this.isAcceptableTime.test(invasionTypeIn.getInvasionTime(), isTimeModifiedIn);
	}
	
	public final boolean canModifyTime(final InvasionType invasionTypeIn) {
		return this.canModifyTime.test(invasionTypeIn.getTimeModifier());
	}
	
	public final boolean canBeChanged(final InvasionType invasionTypeIn) {
		return this.canBeChanged.test(invasionTypeIn.getTimeChangeability());
	}
	
	public abstract int getMaxPossibleInvasions(final ServerLevel levelIn);
	
	public abstract int getRarity(final ServerLevel levelIn);
	
	public abstract int getTierIncreaseDelay(final ServerLevel levelIn);
	
	public abstract boolean canBeCanceled(final ServerLevel levelIn);
	
	public abstract int getCancelRarity(final ServerLevel levelIn);
	
	public static final InvasionSessionType getActive(final ServerLevel levelIn) {
		return levelIn.dimensionType().hasFixedTime() ? FIXED : (levelIn.getDayTime() % 24000L < 12000L ? DAY : NIGHT);
	}
	
	public static final InvasionSessionType getInactive(final ServerLevel levelIn) {
		return levelIn.dimensionType().hasFixedTime() ? FIXED : (levelIn.getDayTime() % 24000L < 12000L ? NIGHT : DAY);
	}
}
