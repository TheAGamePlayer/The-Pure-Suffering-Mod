package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public final class ServerTimeUtil {
	private static boolean prevDayCheck;
	private static boolean prevNightCheck;
	
	public static boolean isServerDay(ServerWorld worldIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && worldIn.getDayTime() % 24000L < 12000L;
		if (prevDayCheck != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(true, result));
		prevDayCheck = result;
		return result;
	}

	public static boolean isServerNight(ServerWorld worldIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && !isServerDay(worldIn);
		if (prevNightCheck != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(false, result));
		prevNightCheck = result;
		return result;
	}
	
	public static void updateTime(ServerPlayerEntity playerIn) {
		ServerWorld world = playerIn.getLevel();
		boolean result = !world.dimensionType().hasFixedTime() && world.getDayTime() % 24000L < 12000L;
		boolean result1 = !world.dimensionType().hasFixedTime() && !result;
		PSPacketHandler.sendToClient(new UpdateTimePacket(true, result), playerIn);
		PSPacketHandler.sendToClient(new UpdateTimePacket(false, result1), playerIn);
	}
}
