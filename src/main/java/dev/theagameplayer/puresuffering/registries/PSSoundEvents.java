package dev.theagameplayer.puresuffering.registries;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PSSoundEvents {
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PureSufferingMod.MODID);
	
	public static final RegistryObject<SoundEvent> DEFAULT_INVASION_START = SOUND_EVENTS.register("default_invasion_start", () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace("default_invasion_start")));
	public static final RegistryObject<SoundEvent> HYPER_INVASION_START = SOUND_EVENTS.register("hyper_invasion_start", () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace("hyper_invasion_start")));
	public static final RegistryObject<SoundEvent> NIGHTMARE_INVASION_START = SOUND_EVENTS.register("nightmare_invasion_start", () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace("nightmare_invasion_start")));
	public static final RegistryObject<SoundEvent> CANCEL_INVASION = SOUND_EVENTS.register("cancel_invasion", () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace("cancel_invasion")));
}
