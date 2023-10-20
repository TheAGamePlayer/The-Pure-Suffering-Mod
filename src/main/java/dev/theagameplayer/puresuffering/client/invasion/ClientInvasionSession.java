package dev.theagameplayer.puresuffering.client.invasion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import dev.theagameplayer.puresuffering.client.ClientTransitionHandler;
import dev.theagameplayer.puresuffering.client.renderer.InvasionSkyRenderer;
import dev.theagameplayer.puresuffering.client.renderer.ClientEffectsRenderer;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class ClientInvasionSession implements Iterable<ClientInvasion> {
	private static final HashMap<ResourceLocation, ClientInvasionSession> CLIENT_SESSIONS = new HashMap<>();
	private final ArrayList<ClientInvasion> invasions = new ArrayList<>();
	private final InvasionSessionType sessionType;
	private final InvasionDifficulty difficulty;
	private final Style style;
	private final InvasionSkyRenderer invasionSkyRenderer;
	private final ClientEffectsRenderer clientEffectsRenderer;
	private float[][] fogRGB;
	private float[] brightness;
	private boolean brightnessUnchanged;
	private float darkness;
	private int lightLevel;
	private int startTime = 40; //Day Time doesn't always sync, so this is necessary
	private double xpMult;

	public ClientInvasionSession(final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn) {
		this.sessionType = sessionTypeIn;
		this.difficulty = difficultyIn;
		this.style = Style.EMPTY.withBold(difficultyIn.isHyper()).withItalic(difficultyIn.isNightmare());
		this.invasionSkyRenderer = new InvasionSkyRenderer(difficultyIn);
		this.clientEffectsRenderer = new ClientEffectsRenderer(difficultyIn);
	}

	public final ClientInvasion getPrimary() {
		return this.invasions.get(0);
	}

	public final InvasionSessionType getSessionType() {
		return this.sessionType;
	}
	
	public final InvasionDifficulty getDifficulty() {
		return this.difficulty;
	}
	
	public final Style getStyle() {
		return this.style;
	}

	public final InvasionSkyRenderer getInvasionSkyRenderer() {
		return this.invasionSkyRenderer;
	}

	public final ClientEffectsRenderer getClientEffectsRenderer() {
		return this.clientEffectsRenderer;
	}

	public final List<InvasionSkyRenderInfo> getRenderersOf(final Predicate<InvasionSkyRenderInfo> ofIn) {
		return this.invasions.stream().map(ClientInvasion::getSkyRenderInfo).filter(ofIn).toList();
	}

	private final void update() {
		this.invasionSkyRenderer.update(this.getPrimary().getSkyRenderInfo(), this.invasions);
		this.fogRGB = new float[2][3];
		this.brightness = new float[2];
		if (this.difficulty.isNightmare()) {
			for (int i = 0; i < 3; i++) this.fogRGB[0][i] = -1.0F;
			this.brightness[0] = 1.0F;
			this.lightLevel = 15;
		} else {
			final List<InvasionSkyRenderInfo> fogColorRenders = this.getRenderersOf(render -> render.getFogRenderInfo().isFogColorChanged());
			if (!fogColorRenders.isEmpty()) {
				for (final InvasionSkyRenderInfo render : fogColorRenders) {
					final InvasionFogRenderInfo fogRender = render.getFogRenderInfo();
					for (int i = 0; i < 3; i++) 
						this.fogRGB[0][i] += fogRender.getRGBOffset(i) / fogColorRenders.size();
				}
			}
			final List<InvasionSkyRenderInfo> brightnessRenders = this.getRenderersOf(render -> render.isBrightnessChanged());
			if (!brightnessRenders.isEmpty()) {
				for (final InvasionSkyRenderInfo render : brightnessRenders)
					this.brightness[0] += render.getBrightness() / brightnessRenders.size();
			}
			this.brightnessUnchanged = brightnessRenders.isEmpty();
			int lightLevel = 0, lc = 0;
			for (final ClientInvasion invasion : this.invasions) {
				final int ll = invasion.getSkyRenderInfo().getLightLevel();
				if (ll < 0) continue;
				lightLevel += ll;
				lc++;
			}
			this.lightLevel = lc > 0 ? lightLevel/lc : -0;
		}
	}

	public final void tick(final ClientLevel levelIn, final long dayTimeIn) {
		final RandomSource random = levelIn.getRandom();
		this.invasions.get((int)(levelIn.getGameTime() % this.invasions.size())).tick(random, dayTimeIn);
		InvasionMusicManager.tickActive(this.difficulty, random, dayTimeIn);
		if (PSConfigValues.client.useSkyBoxRenderer) this.invasionSkyRenderer.tick(dayTimeIn);
		if (PSConfigValues.client.enableSkyEffects) this.clientEffectsRenderer.tick(random, dayTimeIn, this.startTime);
		this.fogRGB[1] = this.fogRGB[0].clone();
		for (final ClientInvasion invasion : this.invasions)
			invasion.flickerFogRGB(this.fogRGB[1]);
		ClientTransitionHandler.getFogColor(this.fogRGB[1], dayTimeIn);
		this.brightness[1] = this.brightness[0];
		for (final ClientInvasion invasion : this.invasions)
			this.brightness[1] = invasion.flickerBrightness(this.brightness[1]);
		this.brightness[1] = ClientTransitionHandler.getBrightness(this.brightness[1], dayTimeIn);
		this.darkness = ClientTransitionHandler.getLightTextureDarkness(dayTimeIn);
		if (this.startTime < 40) this.startTime++;
	}

	public final float[] getFogRGB() {
		return this.fogRGB[1];
	}
	
	public final float getBrightness() {
		return this.brightness[1];
	}
	
	public final boolean isBrightnessUnchanged() {
		return this.brightnessUnchanged;
	}
	
	public final float getDarkness() {
		return this.darkness;
	}
	
	public final int getLightLevelOrDefault(final int lightLevelIn) {
		return this.lightLevel > -1 ? this.lightLevel : lightLevelIn;
	}

	public final int getStartTime() {
		return this.startTime;
	}

	public final void setStartTimer() {
		this.startTime = 0;
	}

	public final double getXPMultiplier() {
		return this.xpMult;
	}

	public final void setXPMultiplier(final double xpMultIn) {
		this.xpMult = xpMultIn;
	}

	public static final ClientInvasionSession get(final ClientLevel levelIn) {
		return levelIn == null ? null : CLIENT_SESSIONS.get(levelIn.dimension().location());
	}

	public static final void add(final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn, final InvasionSkyRenderInfo rendererIn, final boolean isPrimaryIn, final int severityIn, final int mobCapIn, final int maxSeverityIn, final int rarityIn, final int tierIn, final Component componentIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ResourceLocation dimId = mc.level.dimension().location();
		if (CLIENT_SESSIONS.containsKey(dimId) && !isPrimaryIn) {
			final ClientInvasionSession session = CLIENT_SESSIONS.get(dimId);
			session.invasions.add(new ClientInvasion(rendererIn, isPrimaryIn, severityIn, mobCapIn, maxSeverityIn, rarityIn, tierIn, componentIn));
			session.update();
		} else {
			final ClientInvasionSession session = new ClientInvasionSession(sessionTypeIn, difficultyIn);
			session.invasions.add(new ClientInvasion(rendererIn, isPrimaryIn, severityIn, mobCapIn, maxSeverityIn, rarityIn, tierIn, componentIn));
			session.update();
			CLIENT_SESSIONS.put(dimId, session);
		}
	}

	public static final void remove(final InvasionSkyRenderInfo rendererIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ResourceLocation dimId = mc.level.dimension().location();
		final ClientInvasionSession session = CLIENT_SESSIONS.get(dimId);
		if (session == null) return;
		session.invasions.removeIf(inv -> inv.getSkyRenderInfo().equals(rendererIn));
		if (session.invasions.isEmpty()) {
			CLIENT_SESSIONS.remove(dimId);
			return;
		}
		session.update();
	}

	public static final void clear() {
		CLIENT_SESSIONS.clear();
	}

	public final int size() {
		return this.invasions.size();
	}

	@Override
	public final Iterator<ClientInvasion> iterator() {
		return this.invasions.iterator();
	}
}
