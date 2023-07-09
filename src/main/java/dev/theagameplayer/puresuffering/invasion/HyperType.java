package dev.theagameplayer.puresuffering.invasion;

import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public enum HyperType {
	DEFAULT("Default", PSSoundEvents.DEFAULT_INVASION, ParticleTypes.SMOKE, ParticleTypes.FLAME, 20),
	HYPER("Hyper", PSSoundEvents.HYPER_INVASION, ParticleTypes.SOUL, ParticleTypes.FLAME, 25),
	NIGHTMARE("Nightmare", PSSoundEvents.NIGHTMARE_INVASION, ParticleTypes.SCULK_SOUL, ParticleTypes.SOUL_FIRE_FLAME, 30);
	
	private final String name;
	private final RegistryObject<SoundEvent> startSound;
	private final SimpleParticleType p1, p2;
	private final int particleCount;
	
	private HyperType(final String nameIn, final RegistryObject<SoundEvent> startSoundIn, final SimpleParticleType p1n, final SimpleParticleType p2n, final int particleCountIn) {
		this.name = nameIn;
		this.startSound = startSoundIn;
		this.p1 = p1n;
		this.p2 = p2n;
		this.particleCount = particleCountIn;
	}
	
	@Override
	public final String toString() {
		return this.name;
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
}
