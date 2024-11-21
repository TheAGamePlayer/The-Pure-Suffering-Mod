package dev.theagameplayer.puresuffering.event;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.renderer.entity.layers.HyperChargeLayer;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class PSClientEvents {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;

	@SuppressWarnings("unchecked")
	public static final void addLayers(final EntityRenderersEvent.AddLayers pEvent) { //TODO: Redo for mobs not using a mob renderer
		final List<EntityType<? extends Mob>> entityTypes = List.copyOf(
				ForgeRegistries.ENTITY_TYPES.getValues().stream()
				.filter(et -> DefaultAttributes.hasSupplier(et) && et.getCategory() == MobCategory.MONSTER)
				.map(et -> (EntityType<? extends Mob>)et)
				.collect(Collectors.toList()));
		entityTypes.forEach(et -> {
			MobRenderer<Mob, EntityModel<Mob>> renderer = null;
			try {
				renderer = pEvent.getRenderer(et);
			} catch (final Exception eIn) {
				LOGGER.warn("HyperChargeLayer failed to apply to " + ForgeRegistries.ENTITY_TYPES.getKey(et) + ", perhaps renderer is not instance of MobRenderer?");
			}
			if (renderer != null)
				renderer.addLayer(new HyperChargeLayer<Mob, EntityModel<Mob>>(renderer));
		});
	}

	public static final void loggedIn(final ClientPlayerNetworkEvent.LoggingIn pEvent) {
		InvasionMusicManager.reloadMusic();
	}

	public static final void loggedOut(final ClientPlayerNetworkEvent.LoggingOut pEvent) {
		ClientInvasionSession.clear();
	}

	public static final void debugText(final CustomizeGuiOverlayEvent.DebugText pEvent) {
		final Minecraft mc = Minecraft.getInstance();
		if (!mc.options.renderDebug) return;
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		pEvent.getLeft().add("");
		pEvent.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Invasions: " + (session == null ? 0 : session.size()));
		pEvent.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Invasion XP Multiplier: " + (session == null ? 0 : session.getXPMultiplier()) + "x");
	}

	public static final void renderLevelStage(final RenderLevelStageEvent pEvent) {
		if (!PSConfigValues.client.enableSkyEffects || pEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
		final Minecraft mc = Minecraft.getInstance();
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		if (session == null) return;
		session.getClientEffectsRenderer().render(pEvent.getPoseStack(), pEvent.getPartialTick());
	}

	public static final void screenInitPre(final ScreenEvent.Init.Pre pEvent) {
		if (pEvent.getScreen() instanceof CreateWorldScreen createWorldScreen)
			createWorldScreen.getUiState().setDifficulty(Difficulty.HARD);
	}

	public static final void fogColors(final ViewportEvent.ComputeFogColor pEvent) { //Render Tick
		final FogType fogType = pEvent.getCamera().getFluidInCamera();
		if (fogType != FogType.NONE && fogType != FogType.WATER) return;
		final Entity entity = pEvent.getCamera().getEntity();
		if (FogRenderer.getPriorityFogFunction(entity, (float)pEvent.getPartialTick()) != null) return;
		final Minecraft mc = Minecraft.getInstance();
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		if (session == null) return;
		final float[] rgb = session.getFogRGB();
		pEvent.setRed(pEvent.getRed() + rgb[0]);
		pEvent.setGreen(pEvent.getGreen() + rgb[1]);
		pEvent.setBlue(pEvent.getBlue() + rgb[2]);
	}
}
