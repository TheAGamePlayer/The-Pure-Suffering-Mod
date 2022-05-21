package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

public final class ServerTimeUtil {
	public static boolean isServerDay(ServerLevel worldIn, TimedInvasionWorldData dataIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && isDayTime(worldIn);
		if (dataIn.getPrevDayCheck() != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(true, result));
		dataIn.setPrevDayCheck(result);
		return result;
	}
	
	public static boolean isServerNight(ServerLevel worldIn, TimedInvasionWorldData dataIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && !isDayTime(worldIn);
		if (dataIn.getPrevNightCheck() != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(false, result));
		dataIn.setPrevNightCheck(result);
		return result;
	}
	
	private static boolean isDayTime(ServerLevel worldIn) {
		return worldIn.getDayTime() % 24000L < 12000L;
	}
	
	public static void updateTime(ServerPlayer playerIn) {
		ServerLevel world = playerIn.getLevel();
		boolean result = !world.dimensionType().hasFixedTime() && isDayTime(world);
		boolean result1 = !world.dimensionType().hasFixedTime() && !isDayTime(world);
		PSPacketHandler.sendToClient(new UpdateTimePacket(true, result), playerIn);
		PSPacketHandler.sendToClient(new UpdateTimePacket(false, result1), playerIn);
	}
}
