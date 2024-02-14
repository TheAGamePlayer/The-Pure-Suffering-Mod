package dev.theagameplayer.puresuffering.network.packet;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.invasion.InvasionSkyRenderInfo;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public final class AddInvasionPacket {
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

	public static final void encode(final AddInvasionPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(msgIn.sessionType);
		bufIn.writeEnum(msgIn.difficulty);
		msgIn.renderer.deconstruct().serializeToNetwork(bufIn);
		bufIn.writeResourceLocation(msgIn.renderer.getId());
		bufIn.writeBoolean(msgIn.isPrimary);
		bufIn.writeInt(msgIn.severity);
		bufIn.writeInt(msgIn.mobCap);
		bufIn.writeInt(msgIn.maxSeverity);
		bufIn.writeInt(msgIn.rarity);
		bufIn.writeInt(msgIn.tier);
		bufIn.writeComponent(msgIn.component);
	}

	public static final AddInvasionPacket decode(final FriendlyByteBuf bufIn) {
		final InvasionSessionType sessionType = bufIn.readEnum(InvasionSessionType.class);
		final InvasionDifficulty difficulty = bufIn.readEnum(InvasionDifficulty.class);
		final InvasionSkyRenderInfo renderer = InvasionSkyRenderInfo.Builder.fromNetwork(bufIn).build(bufIn.readResourceLocation());
		return new AddInvasionPacket(sessionType, difficulty, renderer, bufIn.readBoolean(), bufIn.readInt(), bufIn.readInt(), bufIn.readInt(), bufIn.readInt(), bufIn.readInt(), bufIn.readComponent());
	}

	public static final class Handler {
		public static final boolean handle(final AddInvasionPacket msgIn, CustomPayloadEvent.Context ctxIn) {
			ctxIn.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final AddInvasionPacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ClientInvasionSession.add(msgIn.sessionType, msgIn.difficulty, msgIn.renderer, msgIn.isPrimary, msgIn.severity, msgIn.mobCap, msgIn.maxSeverity, msgIn.rarity, msgIn.tier, msgIn.component);
		}
	}
}
