package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public final class ServerTimeUtil {
	public static boolean isServerDay(ServerWorld worldIn, TimedInvasionWorldData dataIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && isDayTime(worldIn);
		if (dataIn.getPrevDayCheck() != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(true, result));
		dataIn.setPrevDayCheck(result);
		return result;
	}
	
	public static boolean isServerNight(ServerWorld worldIn, TimedInvasionWorldData dataIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && !isDayTime(worldIn);
		if (dataIn.getPrevNightCheck() != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(false, result));
		dataIn.setPrevNightCheck(result);
		return result;
	}
	
	private static boolean isDayTime(ServerWorld worldIn) {
		return worldIn.getDayTime() % 24000L < 12000L;
	}
	
	public static void updateTime(ServerPlayerEntity playerIn) {
		ServerWorld world = playerIn.getLevel();
		boolean result = !world.dimensionType().hasFixedTime() && isDayTime(world);
		boolean result1 = !world.dimensionType().hasFixedTime() && !isDayTime(world);
		PSPacketHandler.sendToClient(new UpdateTimePacket(true, result), playerIn);
		PSPacketHandler.sendToClient(new UpdateTimePacket(false, result1), playerIn);
	}
}
