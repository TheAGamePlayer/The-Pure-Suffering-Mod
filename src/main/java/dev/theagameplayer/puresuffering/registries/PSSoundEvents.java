package dev.theagameplayer.puresuffering.registries;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PSSoundEvents {
	public static final DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PureSufferingMod.MODID);
	
	public static final Supplier<SoundEvent> DEFAULT_INVASION_START = register("default_invasion_start");
	public static final Supplier<SoundEvent> HYPER_INVASION_START = register("hyper_invasion_start");
	public static final Supplier<SoundEvent> NIGHTMARE_INVASION_START = register("nightmare_invasion_start");
	public static final Supplier<SoundEvent> CANCEL_INVASION = register("cancel_invasion");
	public static final Supplier<SoundEvent> INFORM_INVASION = register("inform_invasion");
	public static final Supplier<SoundEvent> INVASION_AMBIENCE = register("invasion_ambience");
	
	private static final Supplier<SoundEvent> register(final String nameIn) {
		return SOUND_EVENT.register(nameIn, () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace(nameIn)));
	}
}
