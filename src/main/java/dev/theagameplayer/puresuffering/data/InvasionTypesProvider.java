package dev.theagameplayer.puresuffering.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class InvasionTypesProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final List<Consumer<Consumer<InvasionType>>> tabs = ImmutableList.of(new PSInvasionTypes());
	private final CompletableFuture<HolderLookup.Provider> registries;

	public InvasionTypesProvider(final PackOutput packOutputIn, final CompletableFuture<HolderLookup.Provider> lookupProviderIn) {
		this.pathProvider = packOutputIn.createPathProvider(PackOutput.Target.DATA_PACK, "invasion_types");
		this.registries = lookupProviderIn;
	}

	@Override
	public final CompletableFuture<?> run(final CachedOutput cacheIn) {
		return this.registries.thenCompose(path -> {
			final Set<ResourceLocation> set = Sets.newHashSet();
			final ArrayList<CompletableFuture<?>> list = new ArrayList<>();
			final Consumer<InvasionType> consumer = (invasionType) -> {
				if (!set.add(invasionType.getId())) {
					throw new IllegalStateException("Duplicate invasion type " + invasionType.getId());
				} else {
					final Path path1 = this.pathProvider.json(invasionType.getId());
					list.add(DataProvider.saveStable(cacheIn, invasionType.deconstruct().serializeToJson(), path1));
				}
			};
			for (final Consumer<Consumer<InvasionType>> consumer1 : this.tabs) {
				consumer1.accept(consumer);
			}
			return CompletableFuture.allOf(list.toArray((p_253393_) -> {
				return new CompletableFuture[p_253393_];
			}));
		});
	}

	@Override
	public final String getName() {
		return "Invasion Types";
	}
}
