package dev.theagameplayer.puresuffering.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import dev.theagameplayer.puresuffering.data.packs.PSEndInvasionTypes;
import dev.theagameplayer.puresuffering.data.packs.PSMultiDimInvasionTypes;
import dev.theagameplayer.puresuffering.data.packs.PSNetherInvasionTypes;
import dev.theagameplayer.puresuffering.data.packs.PSOverworldInvasionTypes;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class InvasionTypesProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final List<Consumer<Consumer<InvasionType>>> tabs = List.of(new PSOverworldInvasionTypes(), new PSNetherInvasionTypes(), new PSEndInvasionTypes(), new PSMultiDimInvasionTypes());
	private final CompletableFuture<HolderLookup.Provider> registries;

	public InvasionTypesProvider(final PackOutput pPackOutput, final CompletableFuture<HolderLookup.Provider> pLookupProvider) {
		this.pathProvider = pPackOutput.createPathProvider(PackOutput.Target.DATA_PACK, "invasion_types");
		this.registries = pLookupProvider;
	}

	@Override
	public final CompletableFuture<?> run(final CachedOutput pCache) {
		return this.registries.thenCompose(path -> {
			final HashSet<ResourceLocation> set = new HashSet<>();
			final ArrayList<CompletableFuture<?>> list = new ArrayList<>();
			final Consumer<InvasionType> consumer = (invasionType) -> {
				if (!set.add(invasionType.getId())) {
					throw new IllegalStateException("Duplicate invasion type " + invasionType.getId());
				} else {
					final Path path1 = this.pathProvider.json(invasionType.getId());
					list.add(DataProvider.saveStable(pCache, invasionType.deconstruct().serializeToJson(), path1));
				}
			};
			for (final Consumer<Consumer<InvasionType>> consumer1 : this.tabs) consumer1.accept(consumer);
			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		});
	}

	@Override
	public final String getName() {
		return "Invasion Types";
	}
}
