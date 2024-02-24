package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.GameRules.Category;
import net.neoforged.neoforge.common.IExtensibleEnum;

@Mixin(Category.class)
public final class GameRulesCategoryMixin implements IExtensibleEnum {
	static {
		create("PURE_SUFFERING", "gamerule.category.puresuffering");
	}
	
	private static final Category create(final String nameIn, final String descriptionIdIn) {
		throw new IllegalStateException("Enum not extended");
	}
}
