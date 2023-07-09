package dev.theagameplayer.puresuffering.event;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.SkyParticle;
import dev.theagameplayer.puresuffering.client.renderer.InvasionFogRenderer;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.client.renderer.entity.layers.HyperChargeLayer;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.util.InvasionRendererMap;
import dev.theagameplayer.puresuffering.world.ClientInvasionWorldInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class PSClientEvents {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;

	public static final void addLayers(final EntityRenderersEvent.AddLayers eventIn) {
		@SuppressWarnings("unchecked")
		final ImmutableList<EntityType<? extends Mob>> entityTypes = ImmutableList.copyOf(
				ForgeRegistries.ENTITY_TYPES.getValues().stream()
				.filter(DefaultAttributes::hasSupplier)
				.map(et -> (EntityType<? extends Mob>)et)
				.collect(Collectors.toList()));
		entityTypes.forEach(et -> {
			MobRenderer<Mob, EntityModel<Mob>> renderer = null;
			try {
				renderer = eventIn.getRenderer(et);
			} catch (final Exception eIn) {
				LOGGER.warn("HyperChargeLayer failed to apply to " + ForgeRegistries.ENTITY_TYPES.getKey(et) + ", perhaps renderer is not instance of LivingEntityRenderer?");
			}
			if (renderer != null)
				renderer.addLayer(new HyperChargeLayer<Mob, EntityModel<Mob>>(renderer));
		});
	}

	public static final void loggedIn(final ClientPlayerNetworkEvent.LoggingIn eventIn) {
		PSConfigValues.resync(PSConfigValues.client);
	}

	public static final void loggedOut(final ClientPlayerNetworkEvent.LoggingOut eventIn) {
		final Minecraft mc = Minecraft.getInstance();
		ClientInvasionWorldInfo.getDayClientInfo(mc.level).getRendererMap().clear();
		ClientInvasionWorldInfo.getNightClientInfo(mc.level).getRendererMap().clear();
		ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap().clear();
		PSConfigValues.resync(PSConfigValues.client);
	}

	public static final void fogColors(final ViewportEvent.ComputeFogColor eventIn) {
		final Minecraft mc = Minecraft.getInstance();
		if (!mc.level.dimensionType().hasFixedTime()) {
			float red = 0.0F, green = 0.0F, blue = 0.0F;
			final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
			final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
			if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
				if (!dayInfo.getRendererMap().containsHyperType(HyperType.NIGHTMARE)) {
					final ArrayList<InvasionSkyRenderer> rendererList = dayInfo.getRendererMap().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (final InvasionSkyRenderer renderer : rendererList) {
						final InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					} 
				} else {
					red = -1.0F;
					green = -1.0F;
					blue = -1.0F;
				}
			} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
				if (!nightInfo.getRendererMap().containsHyperType(HyperType.NIGHTMARE)) {
					final ArrayList<InvasionSkyRenderer> rendererList = nightInfo.getRendererMap().getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (final InvasionSkyRenderer renderer : rendererList) {
						final InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				} else {
					red = -1.0F;
					green = -1.0F;
					blue = -1.0F;
				}
			}
			ClientTransitionHandler.tickFogColor(eventIn, red, green, blue, mc.level.getDayTime() % 12000L);
		} else {
			float red = 0.0F, green = 0.0F, blue = 0.0F;
			final InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap();
			if (!fixedRenderers.isEmpty()) {
				if (!fixedRenderers.containsHyperType(HyperType.NIGHTMARE)) {
					final ArrayList<InvasionSkyRenderer> rendererList = fixedRenderers.getRenderersOf(renderer -> {
						return renderer.getFogRenderer().isFogColorChanged();
					});
					for (final InvasionSkyRenderer renderer : rendererList) {
						InvasionFogRenderer fogRenderer = renderer.getFogRenderer();
						red += fogRenderer.getRedOffset() / rendererList.size();
						green += fogRenderer.getGreenOffset() / rendererList.size();
						blue += fogRenderer.getBlueOffset() / rendererList.size();
					}
				} else {
					red = -1.0F;
					green = -1.0F;
					blue = -1.0F;
				}
			}
			ClientTransitionHandler.tickFogColor(eventIn, red, green, blue, mc.level.getDayTime() % 12000L);
		}
	}

	public static final void customizeGuiOverlayDebugText(final CustomizeGuiOverlayEvent.DebugText eventIn) {
		final Minecraft mc = Minecraft.getInstance();
		if (mc.options.renderDebug) {
			eventIn.getLeft().add("");
			if (!mc.level.dimensionType().hasFixedTime()) {
				final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
				final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
				if (dayInfo.isClientTime()) {
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Day Invasions: " + dayInfo.getInvasionsCount());
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Day Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? dayInfo.getXPMultiplier() + "x" : "Disabled"));
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Night Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? nightInfo.getXPMultiplier() + "x" : "Disabled"));
					return;
				} else if (nightInfo.isClientTime()) {
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Night Invasions: " + nightInfo.getInvasionsCount());
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Night Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? nightInfo.getXPMultiplier() + "x" : "Disabled"));
					eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Day Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? dayInfo.getXPMultiplier() + "x" : "Disabled"));
					return;
				}
			} else {
				final ClientInvasionWorldInfo fixedInfo = ClientInvasionWorldInfo.getFixedClientInfo(mc.level);
				eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Current Invasions: " + fixedInfo.getInvasionsCount());
				eventIn.getLeft().add(ChatFormatting.RED + "[PureSuffering]" + ChatFormatting.RESET + " Invasion XP Multiplier: " + (PSConfigValues.common.useXPMultiplier ? fixedInfo.getXPMultiplier() + "x" : "Disabled"));
			}
		}
	}
	
	public static final void renderLevelStage(final RenderLevelStageEvent eventIn) {
		if (PSConfigValues.client.enableVortexParticles && eventIn.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			final Minecraft mc = Minecraft.getInstance();
			if (!mc.level.dimensionType().hasFixedTime()) {
				final ClientInvasionWorldInfo dayInfo = ClientInvasionWorldInfo.getDayClientInfo(mc.level);
				final ClientInvasionWorldInfo nightInfo = ClientInvasionWorldInfo.getNightClientInfo(mc.level);
				if (dayInfo.isClientTime() && !dayInfo.getRendererMap().isEmpty()) {
					if (dayInfo.getRendererMap().containsHyperType(HyperType.HYPER))
						SkyParticle.renderParticles(eventIn.getPoseStack(), mc.level, mc.level.dayTime() % 12000L, HyperType.HYPER, 0.225F * mc.level.getRainLevel(eventIn.getPartialTick()));
					if (dayInfo.getRendererMap().containsHyperType(HyperType.NIGHTMARE))
						SkyParticle.renderParticles(eventIn.getPoseStack(), mc.level, mc.level.dayTime() % 12000L, HyperType.NIGHTMARE, 0.225F * mc.level.getRainLevel(eventIn.getPartialTick()));
				} else if (nightInfo.isClientTime() && !nightInfo.getRendererMap().isEmpty()) {
					if (nightInfo.getRendererMap().containsHyperType(HyperType.HYPER))
						SkyParticle.renderParticles(eventIn.getPoseStack(), mc.level, mc.level.dayTime() % 12000L, HyperType.HYPER, 0.225F * mc.level.getRainLevel(eventIn.getPartialTick()));
					if (nightInfo.getRendererMap().containsHyperType(HyperType.NIGHTMARE))
						SkyParticle.renderParticles(eventIn.getPoseStack(), mc.level, mc.level.dayTime() % 12000L, HyperType.NIGHTMARE, 0.225F * mc.level.getRainLevel(eventIn.getPartialTick()));
				}
			} else {
				final InvasionRendererMap fixedRenderers = ClientInvasionWorldInfo.getFixedClientInfo(mc.level).getRendererMap();
				if (fixedRenderers.containsHyperType(HyperType.HYPER))
					SkyParticle.renderParticles(eventIn.getPoseStack(), mc.level, mc.level.dayTime() % 12000L, HyperType.HYPER, 0.225F * mc.level.getRainLevel(eventIn.getPartialTick()));
				if (fixedRenderers.containsHyperType(HyperType.NIGHTMARE))
					SkyParticle.renderParticles(eventIn.getPoseStack(), mc.level, mc.level.dayTime() % 12000L, HyperType.NIGHTMARE, 0.225F * mc.level.getRainLevel(eventIn.getPartialTick()));
			}
		}
	}
}
