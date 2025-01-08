package dev.theagameplayer.puresuffering.util.list;

import java.util.ArrayList;

public final class SpawnPosChart {
	private final ArrayList<YRange> rangeList;

	private SpawnPosChart(final ArrayList<Integer> pYList, final int pPlayerPosY) {
		this.rangeList = new ArrayList<>();
		float total = 0.0f;
		for (final int y : pYList) {
			final float rangeSize = (1.0f/pYList.size())/(Math.abs(y - pPlayerPosY) + 1);
			final YRange range = new YRange(y, total, total + rangeSize);
			total += rangeSize;
			this.rangeList.add(range);
		}
		for (final YRange yRange : this.rangeList) {
			yRange.min *= 1.0f/total;
			yRange.max *= 1.0f/total;
		}
	}
	
	public static final int getYInRange(final ArrayList<Integer> pYList, final int pPlayerPosY, final float pNumber) {
		final SpawnPosChart chart = new SpawnPosChart(pYList, pPlayerPosY);
		for (final YRange range : chart.rangeList) {
			if (range.inRange(pNumber))
				return range.y;
		}
		return pPlayerPosY;
	}

	public static final int getYInRange2(final ArrayList<Integer> pYList, final ArrayList<Integer> pYList2, final int pPlayerPosY, final float pNumber) {
		final SpawnPosChart chart = new SpawnPosChart(pYList, pPlayerPosY);
		for (final YRange range : chart.rangeList) {
			if (range.inRange(pNumber))
				return range.y;
		}
		final SpawnPosChart chart2 = new SpawnPosChart(pYList2, pPlayerPosY);
		for (final YRange range : chart2.rangeList) {
			if (range.inRange(pNumber))
				return range.y;
		}
		return pPlayerPosY;
	}

	@Override
	public final String toString() {
		return this.rangeList.toString();
	}

	private static final class YRange {
		private final int y;
		private float min, max;

		private YRange(final int pY, final float pMin, final float pMax) {
			this.y = pY;
			this.min = pMin;
			this.max = pMax;
		}

		private final boolean inRange(final float pNumber) {
			return pNumber < this.max && pNumber >= this.min;
		}

		@Override
		public final String toString() {
			return "[ " + this.y + " - " + (this.max - this.min) + " - " + this.min + ", " + this.max + " ]";
		}
	}
}
