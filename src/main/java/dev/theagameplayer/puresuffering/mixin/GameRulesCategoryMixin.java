package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.world.level.GameRules.Category;

@Mixin(Category.class)
@Unique
public final class GameRulesCategoryMixin {
	static {
	}
}
