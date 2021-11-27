package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.List;

import dev.theagameplayer.puresuffering.invasion.InvasionType;

public final class InvasionChart {
	private static final ArrayList<InvasionType> USED_LIST = new ArrayList<>();
	private final ArrayList<InvasionType> invasionList = new ArrayList<>();
	private final ArrayList<InvasionRange> rangeList = new ArrayList<>();
	private float total = 0.0F;
	
	public InvasionChart(List<InvasionType> invasionListIn) {
		this.invasionList.addAll(invasionListIn);
		this.calcInvasionRanges();
	}
	
	public static void refresh() {
		USED_LIST.clear();
	}
	
	public InvasionType getInvasionInRange(float numberIn) {
		this.invasionList.removeIf(it -> USED_LIST.contains(it));
		this.calcInvasionRanges();
		InvasionType invasionType = null;
		for (InvasionRange range : this.rangeList) {
			if (range.inRange(numberIn)) {
				invasionType = range.invasionType;
				USED_LIST.add(invasionType);
				break;
			}
		}
		return invasionType;
	}
	
	private void calcInvasionRanges() {
		this.total = 0.0F;
		this.rangeList.clear();
		for (InvasionType invasionType : this.invasionList) {
			float rangeSize = (1.0F/this.invasionList.size())/(invasionType.getRarity() + 1);
			InvasionRange range = new InvasionRange(invasionType, this.total, this.total + rangeSize);
			this.total += rangeSize;
			this.rangeList.add(range);
		}
		for (InvasionRange range : this.rangeList) {
			range.min *= 1.0F/this.total;
			range.max *= 1.0F/this.total;
		}
	}
	
	@Override
	public String toString() {
		return this.rangeList.toString();
	}
	
	private static final class InvasionRange {
		private final InvasionType invasionType;
		private float min, max;
		
		private InvasionRange(InvasionType invasionTypeIn, float minIn, float maxIn) {
			this.invasionType = invasionTypeIn;
			this.min = minIn;
			this.max = maxIn;
		}
		
		public boolean inRange(float numberIn) {
			return numberIn < this.max && numberIn >= this.min;
		}
		
		@Override
		public String toString() {
			return "[ " + this.invasionType + " - " + (this.max - this.min) + " - " + this.min + ", " + this.max + " ]";
		}
	}
}
