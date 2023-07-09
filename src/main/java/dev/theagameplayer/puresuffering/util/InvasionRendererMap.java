package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.invasion.HyperType;

public final class InvasionRendererMap implements Iterable<Entry<InvasionSkyRenderer, Pair<Boolean, HyperType>>> {
	private final HashMap<InvasionSkyRenderer, Pair<Boolean, HyperType>> rendererMap = new HashMap<>();

	public final void add(final InvasionSkyRenderer rendererIn, final boolean isPrimaryIn, final HyperType hyperTypeIn) {
		this.rendererMap.put(rendererIn, Pair.of(isPrimaryIn, hyperTypeIn));
	}

	public final void remove(final InvasionSkyRenderer rendererIn) {
		this.rendererMap.remove(rendererIn);
	}

	public final void clear() {
		this.rendererMap.clear();
	}

	public final boolean containsHyperType(final HyperType hyperTypeIn) {
		for (final Pair<Boolean, HyperType> pair : this.rendererMap.values()) {
			if (pair.getLeft() && pair.getRight() == hyperTypeIn)
				return true;
		}
		return false;
	}

	public final ArrayList<InvasionSkyRenderer> getRenderersOf(final Predicate<InvasionSkyRenderer> predIn) {
		final ArrayList<InvasionSkyRenderer> rendererList = new ArrayList<>();
		for (final InvasionSkyRenderer renderer : this.rendererMap.keySet()) {
			if (predIn.test(renderer))
				rendererList.add(renderer);
		}
		return rendererList;
	}

	//HashMap methods
	public final int size() {
		return this.rendererMap.size();
	}

	public final boolean isEmpty() {
		return this.rendererMap.isEmpty();
	}

	public final boolean contains(final InvasionSkyRenderer invasionIn) {
		return this.rendererMap.containsKey(invasionIn);
	}

	public final InvasionSkyRenderer get(final int indexIn) {
		return this.get(indexIn);
	}

	@Override
	public final String toString() {
		return this.rendererMap.toString();
	}

	@Override
	public final Iterator<Entry<InvasionSkyRenderer, Pair<Boolean, HyperType>>> iterator() {
		return this.rendererMap.entrySet().iterator();
	}
}
