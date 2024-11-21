package dev.theagameplayer.puresuffering.registries;

import java.awt.Color;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.world.effect.BlessingMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PSMobEffects {
	public static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PureSufferingMod.MODID);

	public static final RegistryObject<BlessingMobEffect> BLESSING = MOB_EFFECT.register("blessing", () -> new BlessingMobEffect(MobEffectCategory.BENEFICIAL, Color.WHITE.getRGB()));
}
