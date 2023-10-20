package dev.theagameplayer.puresuffering.registries.other;

import dev.theagameplayer.puresuffering.data.InvasionTypeManager;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.AddReloadListenerEvent;

public final class PSReloadListeners {
	private static InvasionTypeManager invasionTypeManager;

	public static final void addReloadListeners(final AddReloadListenerEvent eventIn) {
		invasionTypeManager = new InvasionTypeManager(eventIn.getRegistryAccess().registryOrThrow(Registries.LEVEL_STEM));
		eventIn.addListener(invasionTypeManager);
	}

	public static final InvasionTypeManager getInvasionTypeManager() {
		return invasionTypeManager;
	}
}
