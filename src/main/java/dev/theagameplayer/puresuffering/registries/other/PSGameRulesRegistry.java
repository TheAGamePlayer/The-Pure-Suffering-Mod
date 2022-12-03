package dev.theagameplayer.puresuffering.registries.other;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public final class PSGameRulesRegistry {
	private static GameRules.Key<GameRules.BooleanValue> enableInvasions;
	
	public static final void registerGameRules() {
		enableInvasions = GameRules.register(PureSufferingMod.MODID + ":enableInvasions", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
	}
	
	public static final boolean getEnableInvasions(Level worldIn) {
		return worldIn.getGameRules().getBoolean(enableInvasions);
	}
}
