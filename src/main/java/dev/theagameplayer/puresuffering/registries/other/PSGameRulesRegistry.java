package dev.theagameplayer.puresuffering.registries.other;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public final class PSGameRulesRegistry {
	private static GameRules.RuleKey<GameRules.BooleanValue> enableInvasions;
	
	public static void registerGameRules() {
		enableInvasions = GameRules.register(PureSufferingMod.MODID + ":enableInvasions", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
	}
	
	public static boolean getEnableInvasions(World worldIn) {
		return worldIn.getGameRules().getBoolean(enableInvasions);
	}
}
