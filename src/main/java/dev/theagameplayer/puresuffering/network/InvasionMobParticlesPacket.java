package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class InvasionMobParticlesPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<InvasionMobParticlesPacket> TYPE = new CustomPacketPayload.Type<>(PureSufferingMod.namespace("invasion_mob_particles"));
	public static final StreamCodec<FriendlyByteBuf, InvasionMobParticlesPacket> STREAM_CODEC = CustomPacketPayload.codec(InvasionMobParticlesPacket::write, InvasionMobParticlesPacket::read);
	private final InvasionDifficulty difficulty;
	private final double x, y, z;
	private final RelocationStatus status;

	public InvasionMobParticlesPacket(final InvasionDifficulty pDifficulty, final Vec3 pPos) {
		this(pDifficulty, pPos, RelocationStatus.NONE);
	}

	public InvasionMobParticlesPacket(final InvasionDifficulty pDifficulty, final Vec3 pPos, final boolean pRelocate) {
		this(pDifficulty, pPos, pRelocate ? RelocationStatus.TO : RelocationStatus.FROM);
	}

	private InvasionMobParticlesPacket(final InvasionDifficulty pDifficulty, final Vec3 pPos, final RelocationStatus pStatus) {
		this.difficulty = pDifficulty;
		this.x = pPos.x;
		this.y = pPos.y;
		this.z = pPos.z;
		this.status = pStatus;
	}

	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeEnum(this.difficulty);
		pBuf.writeDouble(this.x);
		pBuf.writeDouble(this.y);
		pBuf.writeDouble(this.z);
		pBuf.writeEnum(this.status);
	}

	public static final InvasionMobParticlesPacket read(final FriendlyByteBuf pBuf) {
		return new InvasionMobParticlesPacket(pBuf.readEnum(InvasionDifficulty.class), new Vec3(pBuf.readDouble(), pBuf.readDouble(), pBuf.readDouble()), pBuf.readEnum(RelocationStatus.class));
	}

	public static final void handle(final InvasionMobParticlesPacket msgIn, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			for (int l = 0; l < msgIn.difficulty.getParticleCount(); ++l) {
				final double x = msgIn.x + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				final double y = msgIn.y + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				final double z = msgIn.z + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				if (msgIn.status != RelocationStatus.TO)
					mc.level.addParticle(msgIn.difficulty.getSpawnParticleType(true), x, y, z, 0.0D, 0.0025D * l, 0.0D);
				if (msgIn.status != RelocationStatus.FROM)
					mc.level.addParticle(msgIn.difficulty.getSpawnParticleType(false), x, y, z, 0.0D, 0.0025D * l, 0.0D);
			}
		});
	}

	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	private static enum RelocationStatus {
		NONE,
		FROM,
		TO
	}
}
