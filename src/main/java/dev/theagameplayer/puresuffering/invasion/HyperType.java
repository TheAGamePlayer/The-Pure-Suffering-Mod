package dev.theagameplayer.puresuffering.invasion;

public enum HyperType {
	DEFAULT("Default"),
	HYPER("Hyper"),
	MYSTERY("Mystery");
	
	private final String name;
	
	private HyperType(final String nameIn) {
		this.name = nameIn;
	}
	
	@Override
	public final String toString() {
		return this.name;
	}
}
