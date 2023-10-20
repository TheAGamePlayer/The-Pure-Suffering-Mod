package dev.theagameplayer.puresuffering.world.entity;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public interface PSInvasionMob { //Add "PS" to avoid compatibility problems
	static final String HYPER_CHARGE = PureSufferingMod.MODID.toUpperCase() + "HyperCharge";
	
    int psGetHyperCharge();
    
    void psSetHyperCharge(final int hyperChargeIn);
    
    public static void applyHyperEffects(final Mob mobIn) {
		hyperAttribute(mobIn, Attributes.MAX_HEALTH, 0.375D);
		hyperAttribute(mobIn, Attributes.FOLLOW_RANGE, 0.125D);
		hyperAttribute(mobIn, Attributes.KNOCKBACK_RESISTANCE, 0.075D);
		hyperAttribute(mobIn, Attributes.MOVEMENT_SPEED, 0.08D);
		hyperAttribute(mobIn, Attributes.FLYING_SPEED, 0.08D);
		hyperAttribute(mobIn, Attributes.ATTACK_DAMAGE, 1.0D/3.0D);
		hyperAttribute(mobIn, Attributes.ATTACK_KNOCKBACK, 0.1D);
		hyperAttribute(mobIn, Attributes.ARMOR, 0.5D);
		hyperAttribute(mobIn, Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.25D);
		hyperAttribute(mobIn, ForgeMod.SWIM_SPEED.get(), 0.16D);
		mobIn.setHealth(mobIn.getMaxHealth());
    }
    
	private static void hyperAttribute(final Mob mobIn, final Attribute attributeIn, final double multIn) {
		if (mobIn.getAttributes().hasAttribute(attributeIn) && mobIn instanceof PSInvasionMob invasionMob) {
			mobIn.getAttribute(attributeIn).setBaseValue(mobIn.getAttributeBaseValue(attributeIn) * (1.0D + multIn * invasionMob.psGetHyperCharge()));
		}
	}
}
