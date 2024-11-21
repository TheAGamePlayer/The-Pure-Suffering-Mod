package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class UpdateGameRulePacket implements CustomPacketPayload {
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

	public static final class Handler {
		public static final boolean handle(final UpdateGameRulePacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final UpdateGameRulePacket pPacket, final Supplier<Context> pCtx) {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) return;
			final GameRules.Value<?> value = pPacket.gameRule.getRule(mc.level.getGameRules());
			if (value instanceof GameRules.BooleanValue booleanValue) {
				booleanValue.set(pPacket.v1, null);
			} else if (value instanceof GameRules.IntegerValue integerValue) {
				integerValue.set(pPacket.v2, null);
			}
		}
	}
}
