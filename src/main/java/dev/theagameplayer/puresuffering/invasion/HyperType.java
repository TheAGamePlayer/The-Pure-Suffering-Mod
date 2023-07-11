package dev.theagameplayer.puresuffering.invasion;

import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public enum HyperType {
	DEFAULT("Default", "invasion.puresuffering.message1", PSSoundEvents.DEFAULT_INVASION_START, ParticleTypes.SMOKE, ParticleTypes.FLAME, 20),
	HYPER("Hyper", "invasion.puresuffering.message2", PSSoundEvents.HYPER_INVASION_START, ParticleTypes.SOUL, ParticleTypes.FLAME, 25),
	NIGHTMARE("Nightmare", "invasion.puresuffering.message3", PSSoundEvents.NIGHTMARE_INVASION_START, ParticleTypes.SCULK_SOUL, ParticleTypes.SOUL_FIRE_FLAME, 30);
	
	private final String name;
	private final String startComponent;
	private final RegistryObject<SoundEvent> startSound;
	private final SimpleParticleType p1, p2;
	private final int particleCount;
	
	private HyperType(final String nameIn, final String startComponentIn, final RegistryObject<SoundEvent> startSoundIn, final SimpleParticleType p1n, final SimpleParticleType p2n, final int particleCountIn) {
		this.name = nameIn;
		this.startComponent = startComponentIn;
		this.startSound = startSoundIn;
		this.p1 = p1n;
		this.p2 = p2n;
		this.particleCount = particleCountIn;
	}
	
	@Override
	public final String toString() {
		return this.name;
	}
	
	public final String getStartComponent() {
		return this.startComponent;
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
