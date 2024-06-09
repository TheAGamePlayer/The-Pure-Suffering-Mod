package dev.theagameplayer.puresuffering.registries.other;

import dev.theagameplayer.puresuffering.data.InvasionTypeManager;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class PSReloadListeners {
	private static InvasionTypeManager invasionTypeManager;

	public static final void addReloadListeners(final AddReloadListenerEvent pEvent) {
		invasionTypeManager = new InvasionTypeManager(pEvent.getRegistryAccess().registryOrThrow(Registries.LEVEL_STEM));
		pEvent.addListener(invasionTypeManager);
	}

	public static final InvasionTypeManager getInvasionTypeManager() {
		return invasionTypeManager;
	}
}
