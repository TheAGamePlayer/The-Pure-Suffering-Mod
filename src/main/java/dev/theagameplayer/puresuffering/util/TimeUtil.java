package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import net.minecraft.world.server.ServerWorld;

public final class TimeUtil {
	private static boolean prevDayCheck;
	private static boolean prevNightCheck;
	private static boolean clientIsDay;
	private static boolean clientIsNight;
	
	public static boolean isServerDay(final ServerWorld worldIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && worldIn.getDayTime() % 24000L < 12000L;
		if (prevDayCheck != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(true, result), worldIn);
		prevDayCheck = result;
		return result;
	}

	public static boolean isServerNight(final ServerWorld worldIn) {
		boolean result = !worldIn.dimensionType().hasFixedTime() && !isServerDay(worldIn);
		if (prevNightCheck != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(false, result), worldIn);
		prevNightCheck = result;
		return result;
	}
	
	//Called in UpdateTimePacket
	public static void updateClientDay(boolean isDayIn) {
		clientIsDay = isDayIn;
	}
	
	//Called in UpdateTimePacket
	public static void updateClientNight(boolean isNightIn) {
		clientIsNight = isNightIn;
	}
	
	public static boolean isClientDay() {
		return clientIsDay;
	}
	
	public static boolean isClientNight() {
		return clientIsNight;
	}
	
}
