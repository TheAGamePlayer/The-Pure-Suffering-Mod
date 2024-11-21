package dev.theagameplayer.puresuffering.network;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.other.PSPackets.CustomPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class InvasionMobParticlesPacket implements CustomPacketPayload {
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

	public static final class Handler {
		public static final boolean handle(final InvasionMobParticlesPacket pPacket, final Supplier<Context> pCtx) {
			pCtx.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(pPacket, pCtx));
			});
			return true;
		}

		private static final void handlePacket(final InvasionMobParticlesPacket pPacket, final Supplier<Context> pCtx) {
			final Minecraft mc = Minecraft.getInstance();
			for (int l = 0; l < pPacket.difficulty.getParticleCount(); ++l) {
				final double x = pPacket.x + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				final double y = pPacket.y + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				final double z = pPacket.z + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				if (pPacket.status != RelocationStatus.TO)
					mc.level.addParticle(pPacket.difficulty.getSpawnParticleType(true), x, y, z, 0.0D, 0.0025D * l, 0.0D);
				if (pPacket.status != RelocationStatus.FROM)
					mc.level.addParticle(pPacket.difficulty.getSpawnParticleType(false), x, y, z, 0.0D, 0.0025D * l, 0.0D);
			}
		}
	}
	
	private static enum RelocationStatus {
		NONE,
		FROM,
		TO
	}
}
