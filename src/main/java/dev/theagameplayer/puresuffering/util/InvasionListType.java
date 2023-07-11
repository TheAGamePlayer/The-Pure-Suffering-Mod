package dev.theagameplayer.puresuffering.util;

public enum InvasionListType {
	DAY("invasion.puresuffering.day.cancel"),
	NIGHT("invasion.puresuffering.night.cancel"),
	FIXED("invasion.puresuffering.fixed.cancel");
	
	private final String cancelComponent;
	
	private InvasionListType(final String cancelComponentIn) {
		this.cancelComponent = cancelComponentIn;
	}
	
	public final String getCancelComponent() {
		return this.cancelComponent;
	}
}
