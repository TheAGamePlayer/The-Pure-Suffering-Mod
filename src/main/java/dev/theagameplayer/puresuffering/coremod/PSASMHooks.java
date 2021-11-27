package dev.theagameplayer.puresuffering.coremod;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.util.ClientInvasionUtil;
import dev.theagameplayer.puresuffering.util.ServerInvasionUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public final class PSASMHooks {
	//CLIENT
	public static float brightnessHook(float brightnessIn, ClientWorld worldIn) {
		return PSConfigValues.client.canInvasionsChangeBrightness ? ClientInvasionUtil.handleBrightness(brightnessIn, worldIn) : brightnessIn;
	}
	
	//SERVER
	public static int lightLevelHook(int lightLevelIn, World worldIn) {
		return !worldIn.isClientSide && worldIn.dimension() == World.OVERWORLD ? ServerInvasionUtil.handleLightLevel(lightLevelIn, (ServerWorld)worldIn) : lightLevelIn;
	}
}
