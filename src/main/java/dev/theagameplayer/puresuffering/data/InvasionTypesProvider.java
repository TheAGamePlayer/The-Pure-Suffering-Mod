package dev.theagameplayer.puresuffering.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

public class InvasionTypesProvider implements IDataProvider {
	private static final Logger LOGGER = LogManager.getLogger(PureSufferingMod.MODID);
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
	private final DataGenerator generator;
	private final List<Consumer<Consumer<InvasionType>>> tabs = ImmutableList.of(new PSInvasionTypes());

	public InvasionTypesProvider(DataGenerator generatorIn) {
		this.generator = generatorIn;
	}
	
	@Override
	public void run(DirectoryCache cacheIn) throws IOException {
		Path path = this.generator.getOutputFolder();
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<InvasionType> consumer = (invasionType) -> {
			if (!set.add(invasionType.getId())) {
				throw new IllegalStateException("Duplicate invasion type " + invasionType.getId());
			} else {
				Path path1 = createPath(path, invasionType);
				try {
					IDataProvider.save(GSON, cacheIn, invasionType.deconstruct().serializeToJson(), path1);
				} catch (IOException ioexception) {
					LOGGER.error("Couldn't save invasion type {}", path1, ioexception);
				}
			}
		};
		for(Consumer<Consumer<InvasionType>> consumer1 : this.tabs) {
			consumer1.accept(consumer);
		}
	}
	
	private static Path createPath(Path pathIn, InvasionType invasionTypeIn) {
		return pathIn.resolve("generated_data/" + invasionTypeIn.getId().getNamespace() + "/invasion_types/" + invasionTypeIn.getId().getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Invasion Types";
	}
}
