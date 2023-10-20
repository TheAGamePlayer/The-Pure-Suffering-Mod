package dev.theagameplayer.puresuffering.util.list;

import java.util.ArrayList;

public final class SpawnPosChart {
	private final ArrayList<Integer> yList = new ArrayList<>();
	private final ArrayList<YRange> rangeList;
	private final int defaultY;

	private SpawnPosChart(final ArrayList<Integer> posListIn, final int playerPosYIn, final boolean relocateIn) {
		this.yList.addAll(posListIn);
		if (relocateIn) {
			this.rangeList = null;
			final int[] closestY = new int[2];
			for (int i = 0; i < this.yList.size(); ++i) {
				final int y = this.yList.get(i);
				final int dist = Math.abs(y - playerPosYIn);
				if (i == 0 || dist < closestY[1]) {
					closestY[0] = y;
					closestY[1] = dist;
				}
			}
			this.defaultY = closestY[0];
		} else {
			this.rangeList = new ArrayList<>();
			this.defaultY = playerPosYIn;
			float total = 0.0f;
			for (final int y : this.yList) {
				final float rangeSize = 1.0f/this.yList.size()/(Math.abs(y - playerPosYIn) + 1);
				total += rangeSize;
				this.rangeList.add(new YRange(y, total, total + rangeSize));
			}
			for (final YRange yRange : this.rangeList) {
				yRange.min *= 1.0f/total;
				yRange.max *= 1.0f/total;
			}
		}
	}

	public static final int getYInRange(final ArrayList<Integer> yListIn, final int playerPosYIn, final float numberIn, final boolean relocateIn) {
		final SpawnPosChart chart = new SpawnPosChart(yListIn, playerPosYIn, relocateIn);
		if (!relocateIn) {
			for (final YRange range : chart.rangeList) {
				if (range.inRange(numberIn))
					return range.y;
			}
		}
		return chart.defaultY;
	}

	@Override
	public final String toString() {
		return this.rangeList.toString();
	}

	private static final class YRange {
		private final int y;
		private float min, max;

		private YRange(final int yIn, final float minIn, final float maxIn) {
			this.y = yIn;
			this.min = minIn;
			this.max = maxIn;
		}

		private final boolean inRange(final float numberIn) {
			return numberIn < this.max && numberIn >= this.min;
		}

		@Override
		public final String toString() {
			return "[ " + this.y + " - " + (this.max - this.min) + " - " + this.min + ", " + this.max + " ]";
		}
	}
}
