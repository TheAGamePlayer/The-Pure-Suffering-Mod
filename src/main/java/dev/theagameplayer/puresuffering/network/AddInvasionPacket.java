package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AddInvasionPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AddInvasionPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("add_invasion"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AddInvasionPacket> STREAM_CODEC = CustomPacketPayload.codec(AddInvasionPacket::write, AddInvasionPacket::read);
	private final InvasionSessionType sessionType;
	private final InvasionDifficulty difficulty;
	private final InvasionSkyRenderInfo renderer;
	private final boolean isPrimary;
	private final int severity, mobCap;
	private final int maxSeverity;
	private final int rarity, tier;
	private final Component component;

	public AddInvasionPacket(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty, final Invasion pInvasion) {
		this(pSessionType, pDifficulty, pInvasion.getSeverityInfo().getSkyRenderInfo(), pInvasion.isPrimary(), pInvasion.getSeverity(), pInvasion.getMobCap(), pInvasion.getType().getMaxSeverity(), pInvasion.getType().getRarity(), pInvasion.getType().getTier(), pInvasion.getType().getComponent());
	}

	private AddInvasionPacket(final InvasionSessionType pSessionType, final InvasionDifficulty pDifficulty, final InvasionSkyRenderInfo pRenderer, final boolean pIsPrimary, final int pSeverity, final int pMobCap, final int pMaxSeverity, final int pRarity, final int pTier, final Component pComponent) {
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
	}

	public final void write(final RegistryFriendlyByteBuf pBuf) {
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
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(pBuf, this.component);
	}

	public static final AddInvasionPacket read(final RegistryFriendlyByteBuf pBuf) {
		final InvasionSessionType sessionType = pBuf.readEnum(InvasionSessionType.class);
		final InvasionDifficulty difficulty = pBuf.readEnum(InvasionDifficulty.class);
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(pBuf).build(pBuf.readResourceLocation());
		return new AddInvasionPacket(sessionType, difficulty, renderer, pBuf.readBoolean(), pBuf.readInt(), pBuf.readInt(), pBuf.readInt(), pBuf.readInt(), pBuf.readInt(), ComponentSerialization.TRUSTED_STREAM_CODEC.decode(pBuf));
	}

	public static final void handle(final AddInvasionPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			ClientInvasionSession.add(pPacket.sessionType, pPacket.difficulty, pPacket.renderer, pPacket.isPrimary, pPacket.severity, pPacket.mobCap, pPacket.maxSeverity, pPacket.rarity, pPacket.tier, pPacket.component);
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
