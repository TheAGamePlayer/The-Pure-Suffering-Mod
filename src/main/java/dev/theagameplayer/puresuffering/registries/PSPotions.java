package dev.theagameplayer.puresuffering.registries;

import java.awt.Color;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.potion.BlessingEffect;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class PSPotions {
	public static final DeferredRegister<Effect> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, PureSufferingMod.MODID);

	public static final RegistryObject<BlessingEffect> BLESSING = POTIONS.register("blessing", () -> new BlessingEffect(EffectType.BENEFICIAL, Color.WHITE.getRGB()));
}
