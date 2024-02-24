package dev.theagameplayer.puresuffering.registries;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PSActivities {
	public static final DeferredRegister<Activity> ACTIVITY = DeferredRegister.create(BuiltInRegistries.ACTIVITY, PureSufferingMod.MODID);

	public static final Supplier<Activity> INVASION = register("invasion");
	
	private static final Supplier<Activity> register(final String nameIn) {
		return ACTIVITY.register(nameIn, () -> new Activity(nameIn));
	}
}
