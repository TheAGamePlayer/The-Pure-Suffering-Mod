package dev.theagameplayer.puresuffering.coremod;

import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import dev.theagameplayer.puresuffering.util.ServerInvasionUtil;
import net.minecraft.world.World;

public final class PSCoreModHandler {
	//CLIENT
	public static float handleBrightness(float brightnessIn, World worldIn) {
		return ClientInvasionUtil.handleBrightness(brightnessIn, worldIn);
	}
	
	//SERVER
	public static int handleLightLevel(int lightLevelIn, World worldIn) {
		return ServerInvasionUtil.handleLightLevel(lightLevelIn, worldIn);
	}
}
