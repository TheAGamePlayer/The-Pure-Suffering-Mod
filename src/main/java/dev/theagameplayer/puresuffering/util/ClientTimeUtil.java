package dev.theagameplayer.puresuffering.util;

public final class ClientTimeUtil {
	private static boolean clientIsDay;
	private static boolean clientIsNight;

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
