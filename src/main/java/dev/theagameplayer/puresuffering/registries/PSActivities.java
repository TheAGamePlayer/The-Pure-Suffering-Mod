package dev.theagameplayer.puresuffering.registries;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PSActivities {
	public static final DeferredRegister<Activity> ACTIVITY = DeferredRegister.create(BuiltInRegistries.ACTIVITY, PureSufferingMod.MODID);

	public static final DeferredHolder<Activity, Activity> INVASION = register("invasion");
	
	private static final DeferredHolder<Activity, Activity> register(final String pName) {
		return ACTIVITY.register(pName, () -> new Activity(pName));
	}
}
