package dev.theagameplayer.puresuffering.registries;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PSSoundEvents {
	public static final DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PureSufferingMod.MODID);
	
	public static final DeferredHolder<SoundEvent, SoundEvent> DEFAULT_INVASION_START = register("default_invasion_start");
	public static final DeferredHolder<SoundEvent, SoundEvent> HYPER_INVASION_START = register("hyper_invasion_start");
	public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTMARE_INVASION_START = register("nightmare_invasion_start");
	public static final DeferredHolder<SoundEvent, SoundEvent> CANCEL_INVASION = register("cancel_invasion");
	public static final DeferredHolder<SoundEvent, SoundEvent> INFORM_INVASION = register("inform_invasion");
	public static final DeferredHolder<SoundEvent, SoundEvent> INVASION_AMBIENCE = register("invasion_ambience");
	
	private static final DeferredHolder<SoundEvent, SoundEvent> register(final String pName) {
		return SOUND_EVENT.register(pName, () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace(pName)));
	}
}
