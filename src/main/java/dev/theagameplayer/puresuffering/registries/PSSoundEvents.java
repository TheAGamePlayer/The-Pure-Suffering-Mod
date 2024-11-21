package dev.theagameplayer.puresuffering.registries;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PSSoundEvents {
	public static final DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PureSufferingMod.MODID);
	
	public static final RegistryObject<SoundEvent> DEFAULT_INVASION_START = register("default_invasion_start");
	public static final RegistryObject<SoundEvent> HYPER_INVASION_START = register("hyper_invasion_start");
	public static final RegistryObject<SoundEvent> NIGHTMARE_INVASION_START = register("nightmare_invasion_start");
	public static final RegistryObject<SoundEvent> CANCEL_INVASION = register("cancel_invasion");
	public static final RegistryObject<SoundEvent> INFORM_INVASION = register("inform_invasion");
	public static final RegistryObject<SoundEvent> INVASION_AMBIENCE = register("invasion_ambience");
	
	private static final RegistryObject<SoundEvent> register(final String pName) {
		return SOUND_EVENT.register(pName, () -> SoundEvent.createVariableRangeEvent(PureSufferingMod.namespace(pName)));
	}
}
