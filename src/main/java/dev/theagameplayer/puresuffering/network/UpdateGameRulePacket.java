package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class UpdateGameRulePacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateGameRulePacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("update_game_rule"));
	public static final StreamCodec<FriendlyByteBuf, UpdateGameRulePacket> STREAM_CODEC = CustomPacketPayload.codec(UpdateGameRulePacket::write, UpdateGameRulePacket::read);
	final PSGameRules.PSGameRule<?> gameRule;
	final boolean isBool;
	final boolean v1;
	final int v2;

	public UpdateGameRulePacket(final PSGameRules.PSGameRule<?> pGameRule, final boolean pValue) {
		this.gameRule = pGameRule;
		this.isBool = true;
		this.v1 = pValue;
		this.v2 = 0;
	}

	public UpdateGameRulePacket(final PSGameRules.PSGameRule<?> pGameRule, final int pValue) {
		this.gameRule = pGameRule;
		this.isBool = false;
		this.v1 = false;
		this.v2 = pValue;
	}

	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeUtf(this.gameRule.toString());
		pBuf.writeBoolean(this.isBool);
		if (this.isBool) {
			pBuf.writeBoolean(this.v1);
		} else {
			pBuf.writeInt(this.v2);
		}
	}

	public static final UpdateGameRulePacket read(final FriendlyByteBuf pBuf) {
		final PSGameRules.PSGameRule<?> gameRule = PSGameRules.fromString(pBuf.readUtf());
		return pBuf.readBoolean() ? new UpdateGameRulePacket(gameRule, pBuf.readBoolean()) : new UpdateGameRulePacket(gameRule, pBuf.readInt());
	}

	public static final void handle(final UpdateGameRulePacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) return;
			final GameRules.Value<?> value = pPacket.gameRule.getRule(mc.level.getGameRules());
			if (value instanceof GameRules.BooleanValue booleanValue) {
				booleanValue.set(pPacket.v1, null);
			} else if (value instanceof GameRules.IntegerValue integerValue) {
				integerValue.set(pPacket.v2, null);
			}
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
