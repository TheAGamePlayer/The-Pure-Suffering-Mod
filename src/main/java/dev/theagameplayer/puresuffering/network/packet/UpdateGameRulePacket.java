package dev.theagameplayer.puresuffering.network.packet;

import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public final class UpdateGameRulePacket {
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

	public static final void encode(final UpdateGameRulePacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeUtf(msgIn.gameRule.toString());
		bufIn.writeBoolean(msgIn.isBool);
		if (msgIn.isBool) {
			bufIn.writeBoolean(msgIn.v1);
		} else {
			bufIn.writeInt(msgIn.v2);
		}
	}

	public static final UpdateGameRulePacket decode(final FriendlyByteBuf bufIn) {
		final PSGameRules.PSGameRule<?> gameRule = PSGameRules.fromString(bufIn.readUtf());
		return bufIn.readBoolean() ? new UpdateGameRulePacket(gameRule, bufIn.readBoolean()) : new UpdateGameRulePacket(gameRule, bufIn.readInt());
	}

	public static final class Handler {
		public static final boolean handle(final UpdateGameRulePacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ctxIn.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final UpdateGameRulePacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) return;
			final GameRules.Value<?> value = msgIn.gameRule.getRule(mc.level.getGameRules());
			if (value instanceof GameRules.BooleanValue booleanValue) {
				booleanValue.set(msgIn.v1, null);
			} else if (value instanceof GameRules.IntegerValue integerValue) {
				integerValue.set(msgIn.v2, null);
			}
		}
	}
}
