package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class AddInvasionPacket implements CustomPacketPayload {
	private final InvasionSessionType sessionType;
	private final InvasionDifficulty difficulty;
	private final InvasionSkyRenderInfo renderer;
	private final boolean isPrimary;
	private final int severity, mobCap;
	private final int maxSeverity;
	private final int rarity, tier;
	private final Component component;
	private final String customStartMessage;

	public AddInvasionPacket(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty, final Invasion pInvasion) {
		this(pSessionType, pDifficulty, pInvasion.getSeverityInfo().getSkyRenderInfo(), pInvasion.isPrimary(), pInvasion.getSeverity(), pInvasion.getMobCap(), pInvasion.getType().getMaxSeverity(), pInvasion.getType().getRarity(), pInvasion.getType().getTier(), pInvasion.getType().getComponent(), "");
	}

	private AddInvasionPacket(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty, final InvasionSkyRenderInfo pRenderer, final boolean pIsPrimary, final int pSeverity, final int pMobCap, final int pMaxSeverity, final int pRarity, final int pTier, final Component pComponent, final String pCustomStartMessage) {
		this.sessionType = pSessionType;
		this.difficulty = pDifficulty;
		this.renderer = pRenderer;
		this.isPrimary = pIsPrimary;
		this.severity = pSeverity;
		this.mobCap = pMobCap;
		this.maxSeverity = pMaxSeverity;
		this.rarity = pRarity;
		this.tier = pTier;
		this.component = pComponent;
		if (pDifficulty == null) {
			this.customStartMessage = PSConfigValues.common.cancelInvasionStartMessage;
		} else {
			switch(pDifficulty) {
			case DEFAULT:
				this.customStartMessage = PSConfigValues.common.defaultInvasionStartMessage;
				break;
			case HYPER:
				this.customStartMessage = PSConfigValues.common.hyperInvasionStartMessage;
				break;
			case NIGHTMARE:
				this.customStartMessage = PSConfigValues.common.nightmareInvasionStartMessage;
				break;
			default:
				this.customStartMessage = "";
			}
		}
	}

	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeEnum(this.sessionType);
		pBuf.writeEnum(this.difficulty);
		this.renderer.deconstruct().serializeToNetwork(pBuf);
		pBuf.writeResourceLocation(this.renderer.getId());
		pBuf.writeBoolean(this.isPrimary);
		pBuf.writeInt(this.severity);
		pBuf.writeInt(this.mobCap);
		pBuf.writeInt(this.maxSeverity);
		pBuf.writeInt(this.rarity);
		pBuf.writeInt(this.tier);
		pBuf.writeComponent(this.component);
		pBuf.writeUtf(this.customStartMessage);
	}

	public static final AddInvasionPacket read(final FriendlyByteBuf pBuf) {
		final InvasionSessionType sessionType = pBuf.readEnum(InvasionSessionType.class);
		final InvasionDifficulty difficulty = pBuf.readEnum(InvasionDifficulty.class);
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(pBuf).build(pBuf.readResourceLocation());
		return new AddInvasionPacket(sessionType, difficulty, renderer, pBuf.readBoolean(), pBuf.readInt(), pBuf.readInt(), pBuf.readInt(), pBuf.readInt(), pBuf.readInt(), pBuf.readComponent(), pBuf.readUtf());
	}
	
	public static final class Handler {
		public static final boolean handle(final AddInvasionPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final AddInvasionPacket pPacket, final Supplier<Context> pCtx) {
			ClientInvasionSession.add(pPacket.sessionType, pPacket.difficulty, pPacket.renderer, pPacket.isPrimary, pPacket.severity, pPacket.mobCap, pPacket.maxSeverity, pPacket.rarity, pPacket.tier, pPacket.component, pPacket.customStartMessage);
		}
	}
}
