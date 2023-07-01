package dev.theagameplayer.puresuffering.util;

import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

public final class ServerTimeUtil {
	public static final boolean isServerDay(final ServerLevel levelIn, final TimedInvasionWorldData dataIn) {
		final boolean result = !levelIn.dimensionType().hasFixedTime() && isDayTime(levelIn);
		if (dataIn.getPrevDayCheck() != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(true, result));
		dataIn.setPrevDayCheck(result);
		return result;
	}
	
	public static final boolean isServerNight(final ServerLevel levelIn, final TimedInvasionWorldData dataIn) {
		final boolean result = !levelIn.dimensionType().hasFixedTime() && !isDayTime(levelIn);
		if (dataIn.getPrevNightCheck() != result)
			PSPacketHandler.sendToAllClients(new UpdateTimePacket(false, result));
		dataIn.setPrevNightCheck(result);
		return result;
	}
	
	private static final boolean isDayTime(final ServerLevel levelIn) {
		return levelIn.getDayTime() % 24000L < 12000L;
	}
	
	public static final void updateTime(final ServerPlayer playerIn) {
		final ServerLevel level = playerIn.serverLevel();
		final boolean result = !level.dimensionType().hasFixedTime() && isDayTime(level);
		final boolean result1 = !level.dimensionType().hasFixedTime() && !isDayTime(level);
		PSPacketHandler.sendToClient(new UpdateTimePacket(true, result), playerIn);
		PSPacketHandler.sendToClient(new UpdateTimePacket(false, result1), playerIn);
	}
}
