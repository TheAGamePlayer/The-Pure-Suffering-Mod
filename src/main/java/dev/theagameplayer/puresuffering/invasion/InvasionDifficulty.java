package dev.theagameplayer.puresuffering.invasion;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.util.PSRGB;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

public enum InvasionDifficulty {
	DEFAULT("Default", ChatFormatting.RED.getColor(), ChatFormatting.DARK_RED.getColor(), PSSoundEvents.DEFAULT_INVASION_START, ParticleTypes.SMOKE, ParticleTypes.FLAME, 20, 4) {
		@Override
		public final int getHyperCharge(final ServerLevel pLevel, final int pTier, final boolean pIsNatural) {
			if (PSGameRules.TIERED_INVASIONS.get(pLevel) && pIsNatural) {
				final int maxCharge = (int)pLevel.getDayTime()/(24000 * InvasionSessionType.getActive(pLevel).getTierIncreaseDelay(pLevel)) - pTier;
				if (maxCharge < 1) return 0;
				return pLevel.random.nextInt(pLevel.random.nextInt(Math.min(maxCharge, 3)) + 1) + 1;
			}
			return pLevel.random.nextInt(pLevel.random.nextInt(3) + 1) + 1;
		}

		@Override
		public final boolean isAllowed(final Level pLevel) {
			return pLevel.getDifficulty() != Difficulty.PEACEFUL && PSGameRules.ENABLE_INVASIONS.get(pLevel);
		}

		@Override
		public final int getRarity(final ServerLevel pLevel, final int pInvasionRarity) {
			return pInvasionRarity;
		}

		@Override
		public final int getInvasionCount(final RandomSource pRandom, final int pMaxInvasions) {
			return pRandom.nextInt(pMaxInvasions) + 1;
		}
	},
	HYPER("Hyper", ChatFormatting.RED.getColor(), ChatFormatting.DARK_RED.getColor(), PSSoundEvents.HYPER_INVASION_START, ParticleTypes.SOUL, ParticleTypes.FLAME, 25, 2) {
		@Override
		public final int getHyperCharge(final ServerLevel pLevel, final int pTier, final boolean pIsNatural) {
			return Math.max(pLevel.random.nextInt(3), pLevel.random.nextInt(3)) + 1;
		}

		@Override
		public final boolean isAllowed(final Level pLevel) {
			return DEFAULT.isAllowed(pLevel) && pLevel.getDifficulty() != Difficulty.EASY && PSGameRules.HYPER_CHARGE.get(pLevel) && PSGameRules.ENABLE_HYPER_INVASIONS.get(pLevel);
		}

		@Override
		public final int getRarity(final ServerLevel pLevel, final int pInvasionRarity) {
			return PSConfigValues.LEVELS.get(pLevel).invasionDifficultyRarity[0];
		}

		@Override
		public final int getInvasionCount(final RandomSource pRandom, final int pMaxInvasions) {
			return Math.max(pRandom.nextInt(pMaxInvasions), pRandom.nextInt(pMaxInvasions)) + 1;
		}
	},
	NIGHTMARE("Nightmare", (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/2, (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/3, PSSoundEvents.NIGHTMARE_INVASION_START, ParticleTypes.SCULK_SOUL, ParticleTypes.SOUL_FIRE_FLAME, 30, 1) {
		@Override
		public final int getHyperCharge(final ServerLevel pLevel, final int pTier, final boolean pIsNatural) {
			return 4;
		}

		@Override
		public final boolean isAllowed(final Level pLevel) {
			return HYPER.isAllowed(pLevel) && PSGameRules.ENABLE_NIGHTMARE_INVASIONS.get(pLevel);
		}

		@Override
		public final int getRarity(final ServerLevel pLevel, final int pInvasionRarity) {
			return PSConfigValues.LEVELS.get(pLevel).invasionDifficultyRarity[1];
		}

		@Override
		public final int getInvasionCount(final RandomSource pRandom, final int pMaxInvasions) {
			return Math.max(HYPER.getInvasionCount(pRandom, pMaxInvasions), pRandom.nextInt(pMaxInvasions));
		}
	};

	private final String name;
	private final MutableComponent translation;
	private final int color1, color2;
	private final Supplier<SoundEvent> startSound;
	private final SimpleParticleType p1, p2;
	private final int particleCount;
	private final int ringDelay;

	private InvasionDifficulty(final String pName, final int pColor1, final int pColor2, final Supplier<SoundEvent> pStartSound, final SimpleParticleType pP1, final SimpleParticleType pP2, final int pParticleCount, final int pRingDelay) {
		this.name = pName;
		this.translation = Component.translatable("puresuffering.invasionDifficulty." + this.toString());
		this.color1 = pColor1;
		this.color2 = pColor2;
		this.startSound = pStartSound;
		this.p1 = pP1;
		this.p2 = pP2;
		this.particleCount = pParticleCount;
		this.ringDelay = pRingDelay;
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
	
	public final int getColor(final boolean pIsFirst) {
		return pIsFirst ? this.color1 : this.color2;
	}
	
	public final PSRGB getInterColor(final int pDiv, final int pMult) {
		return new PSRGB(this.color1, this.color2, pDiv, pMult);
	}
	
	public final PSRGB getRandomColor(final RandomSource pRandom) {
		return new PSRGB(pRandom, this.color1, this.color2);
	}

	public final SoundEvent getStartSound() {
		return this.startSound.get();
	}

	public final SimpleParticleType getSpawnParticleType(final boolean pIsFirst) {
		return pIsFirst ? this.p1 : this.p2;
	}

	public final int getParticleCount() {
		return this.particleCount;
	}
	
	public final int getRingDelay() {
		return this.ringDelay;
	}
	
	public final boolean isHyper() {
		return this != DEFAULT;
	}
	
	public final boolean isNightmare() {
		return this.ordinal() > HYPER.ordinal();
	}
	
	public abstract int getHyperCharge(final ServerLevel pLevel, final int pTier, final boolean pIsNatural);
	
	public abstract boolean isAllowed(final Level pLevel);
	
	public abstract int getRarity(final ServerLevel pLevel, final int pInvasionRarity);
	
	public abstract int getInvasionCount(final RandomSource pRandom, final int pMaxInvasions);
}
