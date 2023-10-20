package dev.theagameplayer.puresuffering.invasion;

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
import net.minecraftforge.registries.RegistryObject;

public enum InvasionDifficulty {
	DEFAULT("Default", ChatFormatting.RED.getColor(), ChatFormatting.DARK_RED.getColor(), PSSoundEvents.DEFAULT_INVASION_START, ParticleTypes.SMOKE, ParticleTypes.FLAME, 20, 4) {
		@Override
		public final int getHyperCharge(final ServerLevel levelIn, final int tierIn, final boolean isNaturalIn) {
			if (PSGameRules.TIERED_INVASIONS.get(levelIn) && isNaturalIn) {
				final int maxCharge = (int)levelIn.getDayTime()/(24000 * InvasionSessionType.getActive(levelIn).getTierIncreaseDelay(levelIn)) - tierIn;
				if (maxCharge < 1) return 0;
				return levelIn.random.nextInt(levelIn.random.nextInt(Math.min(maxCharge, 3)) + 1) + 1;
			}
			return levelIn.random.nextInt(levelIn.random.nextInt(3) + 1) + 1;
		}

		@Override
		public final boolean isAllowed(final Level levelIn) {
			return levelIn.getDifficulty() != Difficulty.PEACEFUL && PSGameRules.ENABLE_INVASIONS.get(levelIn);
		}

		@Override
		public final int getRarity(final ServerLevel levelIn, final int invasionRarityIn) {
			return invasionRarityIn;
		}

		@Override
		public final int getInvasionCount(final RandomSource randomIn, final int maxInvasionsIn) {
			return randomIn.nextInt(maxInvasionsIn) + 1;
		}
	},
	HYPER("Hyper", ChatFormatting.RED.getColor(), ChatFormatting.DARK_RED.getColor(), PSSoundEvents.HYPER_INVASION_START, ParticleTypes.SOUL, ParticleTypes.FLAME, 25, 2) {
		@Override
		public final int getHyperCharge(final ServerLevel levelIn, final int tierIn, final boolean isNaturalIn) {
			return Math.max(levelIn.random.nextInt(3), levelIn.random.nextInt(3)) + 1;
		}

		@Override
		public final boolean isAllowed(final Level levelIn) {
			return DEFAULT.isAllowed(levelIn) && levelIn.getDifficulty() != Difficulty.EASY && PSGameRules.HYPER_CHARGE.get(levelIn) && PSGameRules.ENABLE_HYPER_INVASIONS.get(levelIn);
		}

		@Override
		public final int getRarity(final ServerLevel levelIn, final int invasionRarityIn) {
			return PSConfigValues.LEVELS.get(levelIn).invasionDifficultyRarity[0];
		}

		@Override
		public final int getInvasionCount(final RandomSource randomIn, final int maxInvasionsIn) {
			return Math.max(randomIn.nextInt(maxInvasionsIn), randomIn.nextInt(maxInvasionsIn)) + 1;
		}
	},
	NIGHTMARE("Nightmare", (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/2, (ChatFormatting.DARK_PURPLE.getColor() + ChatFormatting.DARK_RED.getColor())/3, PSSoundEvents.NIGHTMARE_INVASION_START, ParticleTypes.SCULK_SOUL, ParticleTypes.SOUL_FIRE_FLAME, 30, 1) {
		@Override
		public final int getHyperCharge(final ServerLevel levelIn, final int tierIn, final boolean isNaturalIn) {
			return 4;
		}

		@Override
		public final boolean isAllowed(final Level levelIn) {
			return HYPER.isAllowed(levelIn) && PSGameRules.ENABLE_NIGHTMARE_INVASIONS.get(levelIn);
		}

		@Override
		public final int getRarity(final ServerLevel levelIn, final int invasionRarityIn) {
			return PSConfigValues.LEVELS.get(levelIn).invasionDifficultyRarity[1];
		}

		@Override
		public final int getInvasionCount(final RandomSource randomIn, final int maxInvasionsIn) {
			return Math.max(HYPER.getInvasionCount(randomIn, maxInvasionsIn), randomIn.nextInt(maxInvasionsIn));
		}
	};

	private final String name;
	private final MutableComponent translation;
	private final int color1, color2;
	private final RegistryObject<SoundEvent> startSound;
	private final SimpleParticleType p1, p2;
	private final int particleCount;
	private final int ringDelay;

	private InvasionDifficulty(final String nameIn, final int color1n, final int color2n, final RegistryObject<SoundEvent> startSoundIn, final SimpleParticleType p1n, final SimpleParticleType p2n, final int particleCountIn, final int ringDelayIn) {
		this.name = nameIn;
		this.translation = Component.translatable("puresuffering.invasionDifficulty." + this.toString());
		this.color1 = color1n;
		this.color2 = color2n;
		this.startSound = startSoundIn;
		this.p1 = p1n;
		this.p2 = p2n;
		this.particleCount = particleCountIn;
		this.ringDelay = ringDelayIn;
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
	
	public final int getColor(final boolean isFirstIn) {
		return isFirstIn ? this.color1 : this.color2;
	}
	
	public final PSRGB getInterColor(final int divIn, final int multIn) {
		return new PSRGB(this.color1, this.color2, divIn, multIn);
	}
	
	public final PSRGB getRandomColor(final RandomSource randomIn) {
		return new PSRGB(randomIn, this.color1, this.color2);
	}

	public final SoundEvent getStartSound() {
		return this.startSound.get();
	}

	public final SimpleParticleType getSpawnParticleType(final boolean isFirstIn) {
		return isFirstIn ? this.p1 : this.p2;
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
	
	public abstract int getHyperCharge(final ServerLevel levelIn, final int tierIn, final boolean isNaturalIn);
	
	public abstract boolean isAllowed(final Level levelIn);
	
	public abstract int getRarity(final ServerLevel levelIn, final int invasionRarityIn);
	
	public abstract int getInvasionCount(final RandomSource randomIn, final int maxInvasionsIn);
}
