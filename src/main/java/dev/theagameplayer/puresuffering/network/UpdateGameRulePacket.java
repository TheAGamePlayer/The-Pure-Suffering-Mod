package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class UpdateGameRulePacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("update_game_rule");
	final PSGameRules.PSGameRule<?> gameRule;
	final boolean isBool;
	final boolean v1;
	final int v2;

	public UpdateGameRulePacket(final PSGameRules.PSGameRule<?> gameRuleIn, final boolean valueIn) {
		this.gameRule = gameRuleIn;
		this.isBool = true;
		this.v1 = valueIn;
		this.v2 = 0;
	}

	public UpdateGameRulePacket(final PSGameRules.PSGameRule<?> gameRuleIn, final int valueIn) {
		this.gameRule = gameRuleIn;
		this.isBool = false;
		this.v1 = false;
		this.v2 = valueIn;
	}

	@Override
	public final void write(final FriendlyByteBuf bufIn) {
		bufIn.writeUtf(this.gameRule.toString());
		bufIn.writeBoolean(this.isBool);
		if (this.isBool) {
			bufIn.writeBoolean(this.v1);
		} else {
			bufIn.writeInt(this.v2);
		}
	}

	public static final UpdateGameRulePacket read(final FriendlyByteBuf bufIn) {
		final PSGameRules.PSGameRule<?> gameRule = PSGameRules.fromString(bufIn.readUtf());
		return bufIn.readBoolean() ? new UpdateGameRulePacket(gameRule, bufIn.readBoolean()) : new UpdateGameRulePacket(gameRule, bufIn.readInt());
	}

	public static final void handle(final UpdateGameRulePacket packetIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) return;
			final GameRules.Value<?> value = packetIn.gameRule.getRule(mc.level.getGameRules());
			if (value instanceof GameRules.BooleanValue booleanValue) {
				booleanValue.set(packetIn.v1, null);
			} else if (value instanceof GameRules.IntegerValue integerValue) {
				integerValue.set(packetIn.v2, null);
			}
		});
	}

	@Override
	public final ResourceLocation id() {
		return ID;
	}
}
