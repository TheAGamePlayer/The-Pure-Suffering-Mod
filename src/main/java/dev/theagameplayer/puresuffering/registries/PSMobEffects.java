package dev.theagameplayer.puresuffering.registries;

import java.awt.Color;
import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.world.effect.BlessingMobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PSMobEffects {
	public static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, PureSufferingMod.MODID);

	public static final Supplier<BlessingMobEffect> BLESSING = MOB_EFFECT.register("blessing", () -> new BlessingMobEffect(MobEffectCategory.BENEFICIAL, Color.WHITE.getRGB()));
}
