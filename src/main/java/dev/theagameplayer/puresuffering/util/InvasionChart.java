package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.List;

import dev.theagameplayer.puresuffering.invasion.InvasionType;

public final class InvasionChart {
	private static final ArrayList<InvasionType> USED_LIST = new ArrayList<>();
	private final ArrayList<InvasionType> invasionList = new ArrayList<>();
	private final ArrayList<InvasionRange> rangeList = new ArrayList<>();
	private float total = 0.0F;
	
	public InvasionChart(final List<InvasionType> invasionListIn) {
		this.invasionList.addAll(invasionListIn);
		this.calcInvasionRanges();
	}
	
	public static final void refresh() {
		USED_LIST.clear();
	}
	
	public final InvasionType getInvasionInRange(final float numberIn) {
		this.invasionList.removeIf(it -> USED_LIST.contains(it));
		this.calcInvasionRanges();
		InvasionType invasionType = null;
		for (final InvasionRange range : this.rangeList) {
			if (range.inRange(numberIn)) {
				invasionType = range.invasionType;
				USED_LIST.add(invasionType);
				break;
			}
		}
		return invasionType;
	}
	
	private final void calcInvasionRanges() {
		this.total = 0.0F;
		this.rangeList.clear();
		for (final InvasionType invasionType : this.invasionList) {
			final float rangeSize = (1.0F/this.invasionList.size())/(invasionType.getRarity() + 1);
			final InvasionRange range = new InvasionRange(invasionType, this.total, this.total + rangeSize);
			this.total += rangeSize;
			this.rangeList.add(range);
		}
		for (final InvasionRange range : this.rangeList) {
			range.min *= 1.0F/this.total;
			range.max *= 1.0F/this.total;
		}
	}
	
	@Override
	public final String toString() {
		return this.rangeList.toString();
	}
	
	private static final class InvasionRange {
		private final InvasionType invasionType;
		private float min, max;
		
		private InvasionRange(final InvasionType invasionTypeIn, final float minIn, final float maxIn) {
			this.invasionType = invasionTypeIn;
			this.min = minIn;
			this.max = maxIn;
		}
		
		public final boolean inRange(final float numberIn) {
			return numberIn < this.max && numberIn >= this.min;
		}
		
		@Override
		public final String toString() {
			return "[ " + this.invasionType + " - " + (this.max - this.min) + " - " + this.min + ", " + this.max + " ]";
		}
	}
}
