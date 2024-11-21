package dev.theagameplayer.puresuffering.world.entity;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.invasion.data.InvasionSpawnerData.MobTagData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public interface PSInvasionMob { //Add "PS" to avoid compatibility problems
	static final String HYPER_CHARGE = PureSufferingMod.MODID.toUpperCase() + "HyperCharge";
	
    abstract int psGetHyperCharge();
    
    abstract void psSetHyperCharge(final int pHyperCharge);
    
    abstract void applyNBTTags(final MobTagData[] pNBTTags);
    
    public static void applyHyperEffects(final Mob pMob) {
		hyperAttribute(pMob, Attributes.MAX_HEALTH, 0.375D);
		hyperAttribute(pMob, Attributes.FOLLOW_RANGE, 0.125D);
		hyperAttribute(pMob, Attributes.KNOCKBACK_RESISTANCE, 0.075D);
		hyperAttribute(pMob, Attributes.MOVEMENT_SPEED, 0.08D);
		hyperAttribute(pMob, Attributes.FLYING_SPEED, 0.08D);
		hyperAttribute(pMob, Attributes.ATTACK_DAMAGE, 1.0D/3.0D);
		hyperAttribute(pMob, Attributes.ATTACK_KNOCKBACK, 0.1D);
		hyperAttribute(pMob, Attributes.ARMOR, 0.5D);
		hyperAttribute(pMob, Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.25D);
		hyperAttribute(pMob, ForgeMod.SWIM_SPEED.get(), 0.16D);
		pMob.setHealth(pMob.getMaxHealth());
    }
    
	private static void hyperAttribute(final Mob pMob, final Attribute pAttribute, final double pMult) {
		if (pMob.getAttributes().hasAttribute(pAttribute) && pMob instanceof PSInvasionMob invasionMob)
			pMob.getAttribute(pAttribute).setBaseValue(pMob.getAttributeBaseValue(pAttribute) * (1.0D + pMult * invasionMob.psGetHyperCharge()));
	}
}
