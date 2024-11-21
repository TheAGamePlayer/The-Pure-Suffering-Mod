package dev.theagameplayer.puresuffering.registries;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PSActivities {
	public static final DeferredRegister<Activity> ACTIVITY = DeferredRegister.create(ForgeRegistries.ACTIVITIES, PureSufferingMod.MODID);

	public static final RegistryObject<Activity> INVASION = register("invasion");
	
	private static final RegistryObject<Activity> register(final String pName) {
		return ACTIVITY.register(pName, () -> new Activity(pName));
	}
}
