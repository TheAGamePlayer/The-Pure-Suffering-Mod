package dev.theagameplayer.puresuffering.client.invasion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.codehaus.plexus.util.FastMap;

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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class ClientInvasionSession implements Iterable<ClientInvasion> {
	private static final FastMap<ResourceLocation, ClientInvasionSession> CLIENT_SESSIONS = new FastMap<>();
	private final ArrayList<ClientInvasion> invasions = new ArrayList<>();
	private final InvasionSessionType sessionType;
	private final InvasionDifficulty difficulty;
	private final String customStartMessage;
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

	public ClientInvasionSession(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty, final String pCustomStartMessage) {
		this.sessionType = pSessionType;
		this.difficulty = pDifficulty;
		this.customStartMessage = pCustomStartMessage;
		this.style = Style.EMPTY.withBold(pDifficulty.isHyper()).withItalic(pDifficulty.isNightmare());
		this.invasionSkyRenderer = new InvasionSkyRenderer(pDifficulty);
		this.clientEffectsRenderer = new ClientEffectsRenderer(pDifficulty);
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
	
	public final MutableComponent getStartMessage(final String pDefaultMessage) {
		return this.customStartMessage.isBlank() ? Component.translatable(pDefaultMessage) : Component.literal(this.customStartMessage);
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

	public final List<InvasionSkyRenderInfo> getRenderersOf(final Predicate<InvasionSkyRenderInfo> pOf) {
		return this.invasions.stream().map(ClientInvasion::getSkyRenderInfo).filter(pOf).toList();
	}

	private final void update() {
		this.invasionSkyRenderer.update(this.getPrimary().getSkyRenderInfo(), this.invasions);
		this.fogRGB = new float[2][3];
		this.brightness = new float[2];
		if (this.difficulty.isNightmare()) {
			for (int i = 0; i < 3; ++i) this.fogRGB[0][i] = -1.0F;
			this.brightness[0] = 1.0F;
			this.lightLevel = 15;
		} else {
			final List<InvasionSkyRenderInfo> fogColorRenders = this.getRenderersOf(render -> render.getFogRenderInfo().isFogColorChanged());
			if (!fogColorRenders.isEmpty()) {
				for (final InvasionSkyRenderInfo render : fogColorRenders) {
					final InvasionFogRenderInfo fogRender = render.getFogRenderInfo();
					for (int i = 0; i < 3; ++i) 
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
				++lc;
			}
			this.lightLevel = lc > 0 ? lightLevel/lc : -0;
		}
	}

	public final void tick(final ClientLevel pLevel, final long pDayTime) {
		final RandomSource random = pLevel.getRandom();
		this.invasions.get((int)(pLevel.getGameTime() % this.invasions.size())).tick(random, pDayTime);
		InvasionMusicManager.tickActive(this.difficulty, random, pDayTime);
		if (PSConfigValues.client.useSkyBoxRenderer) this.invasionSkyRenderer.tick(pDayTime);
		if (PSConfigValues.client.enableSkyEffects) this.clientEffectsRenderer.tick(random, pDayTime, this.startTime);
		this.fogRGB[1] = this.fogRGB[0].clone();
		for (final ClientInvasion invasion : this.invasions)
			invasion.flickerFogRGB(this.fogRGB[1]);
		ClientTransitionHandler.getFogColor(this.fogRGB[1], pDayTime);
		this.brightness[1] = this.brightness[0];
		for (final ClientInvasion invasion : this.invasions)
			this.brightness[1] = invasion.flickerBrightness(this.brightness[1]);
		this.brightness[1] = ClientTransitionHandler.getBrightness(this.brightness[1], pDayTime);
		this.darkness = ClientTransitionHandler.getLightTextureDarkness(pDayTime);
		if (this.startTime < 40) ++this.startTime;
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
	
	public final int getLightLevelOrDefault(final int pLightLevel) {
		return this.lightLevel > -1 ? this.lightLevel : pLightLevel;
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

	public final void setXPMultiplier(final double pXPMult) {
		this.xpMult = pXPMult;
	}

	public static final ClientInvasionSession get(final ClientLevel pLevel) {
		return pLevel == null ? null : CLIENT_SESSIONS.get(pLevel.dimension().location());
	}

	public static final void add(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty, final InvasionSkyRenderInfo pRenderer, final boolean pIsPrimary, final int pSeverity, final int pMobCap, final int pMaxSeverity, final int pRarity, final int pTier, final Component pComponent, final String pCustomStartMessage) {
		final Minecraft mc = Minecraft.getInstance();
		final ResourceLocation dimId = mc.level.dimension().location();
		if (CLIENT_SESSIONS.containsKey(dimId) && !pIsPrimary) {
			final ClientInvasionSession session = CLIENT_SESSIONS.get(dimId);
			session.invasions.add(new ClientInvasion(pRenderer, pIsPrimary, pSeverity, pMobCap, pMaxSeverity, pRarity, pTier, pComponent));
			session.update();
		} else {
			final ClientInvasionSession session = new ClientInvasionSession(pSessionType, pDifficulty, pCustomStartMessage);
			session.invasions.add(new ClientInvasion(pRenderer, pIsPrimary, pSeverity, pMobCap, pMaxSeverity, pRarity, pTier, pComponent));
			session.update();
			CLIENT_SESSIONS.put(dimId, session);
		}
	}

	public static final void remove(final InvasionSkyRenderInfo pRenderer) {
		final Minecraft mc = Minecraft.getInstance();
		final ResourceLocation dimId = mc.level.dimension().location();
		final ClientInvasionSession session = CLIENT_SESSIONS.get(dimId);
		if (session == null) return;
		session.invasions.removeIf(inv -> inv.getSkyRenderInfo().equals(pRenderer));
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
