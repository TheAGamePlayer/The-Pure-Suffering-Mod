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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class PSClientEvents {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;

	@SuppressWarnings("unchecked")
	public static final void addLayers(final EntityRenderersEvent.AddLayers pEvent) { //TODO: Redo for mobs not using a mob renderer
		final List<EntityType<? extends Mob>> entityTypes = List.copyOf(
				BuiltInRegistries.ENTITY_TYPE.asLookup().listElements()
				.filter(et -> DefaultAttributes.hasSupplier(et.value()) && et.value().getCategory() == MobCategory.MONSTER)
				.map(et -> (EntityType<? extends Mob>)et.value())
				.collect(Collectors.toList()));
		entityTypes.forEach(et -> {
			final EntityRenderer<?> renderer = pEvent.getRenderer(et);
			if (renderer instanceof MobRenderer mobRenderer) {
				mobRenderer.addLayer(new HyperChargeLayer<Mob, EntityModel<Mob>>(mobRenderer));
				return;
			}
			LOGGER.warn("HyperChargeLayer failed to apply to " + BuiltInRegistries.ENTITY_TYPE.getKey(et) + ", perhaps renderer is not instance of MobRenderer?");
		});
	}

	public static final void loggedIn(final ClientPlayerNetworkEvent.LoggingIn pEvent) {
		PSConfigValues.resyncClient();
		InvasionMusicManager.reloadMusic();
	}

	public static final void loggedOut(final ClientPlayerNetworkEvent.LoggingOut pEvent) {
		PSConfigValues.resyncClient();
		ClientInvasionSession.clear();
	}

	public static final void debugText(final CustomizeGuiOverlayEvent.DebugText pEvent) {
		final Minecraft mc = Minecraft.getInstance();
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
		pEvent.getPoseStack().mulPose(pEvent.getModelViewMatrix());
		session.getClientEffectsRenderer().render(pEvent.getPoseStack(), pEvent.getPartialTick().getGameTimeDeltaPartialTick(false));
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
