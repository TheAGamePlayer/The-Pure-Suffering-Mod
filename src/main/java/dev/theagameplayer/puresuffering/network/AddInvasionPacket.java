package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class AddInvasionPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("add_invasion");
	private final InvasionSessionType sessionType;
	private final InvasionDifficulty difficulty;
	private final InvasionSkyRenderInfo renderer;
	private final boolean isPrimary;
	private final int severity, mobCap;
	private final int maxSeverity;
	private final int rarity, tier;
	private final Component component;

	public AddInvasionPacket(final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn, final Invasion invasionIn) {
		this(sessionTypeIn, difficultyIn, invasionIn.getSeverityInfo().getSkyRenderInfo(), invasionIn.isPrimary(), invasionIn.getSeverity(), invasionIn.getMobCap(), invasionIn.getType().getMaxSeverity(), invasionIn.getType().getRarity(), invasionIn.getType().getTier(), invasionIn.getType().getComponent());
	}

	private AddInvasionPacket(final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn, final InvasionSkyRenderInfo rendererIn, final boolean isPrimaryIn, final int severityIn, final int mobCapIn, final int maxSeverityIn, final int rarityIn, final int tierIn, final Component componentIn) {
		this.sessionType = sessionTypeIn;
		this.difficulty = difficultyIn;
		this.renderer = rendererIn;
		this.isPrimary = isPrimaryIn;
		this.severity = severityIn;
		this.mobCap = mobCapIn;
		this.maxSeverity = maxSeverityIn;
		this.rarity = rarityIn;
		this.tier = tierIn;
		this.component = componentIn;
	}

	@Override
	public final void write(final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(this.sessionType);
		bufIn.writeEnum(this.difficulty);
		this.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(this.renderer.getId());
		bufIn.writeBoolean(this.isPrimary);
		bufIn.writeInt(this.severity);
		bufIn.writeInt(this.mobCap);
		bufIn.writeInt(this.maxSeverity);
		bufIn.writeInt(this.rarity);
		bufIn.writeInt(this.tier);
		bufIn.writeComponent(this.component);
	}

	public static final AddInvasionPacket read(final FriendlyByteBuf bufIn) {
		final InvasionSessionType sessionType = bufIn.readEnum(InvasionSessionType.class);
		final InvasionDifficulty difficulty = bufIn.readEnum(InvasionDifficulty.class);
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new AddInvasionPacket(sessionType, difficulty, renderer, bufIn.readBoolean(), bufIn.readInt(), bufIn.readInt(), bufIn.readInt(), bufIn.readInt(), bufIn.readInt(), bufIn.readComponent());
	}

	public static final void handle(final AddInvasionPacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			ClientInvasionSession.add(packetIn.sessionType, packetIn.difficulty, packetIn.renderer, packetIn.isPrimary, packetIn.severity, packetIn.mobCap, packetIn.maxSeverity, packetIn.rarity, packetIn.tier, packetIn.component);
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
