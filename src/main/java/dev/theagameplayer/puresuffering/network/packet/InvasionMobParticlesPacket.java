package dev.theagameplayer.puresuffering.network.packet;

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public final class InvasionMobParticlesPacket {
	private final InvasionDifficulty difficulty;
	private final double x, y, z;
	private final RelocationStatus status;
	
	public InvasionMobParticlesPacket(final InvasionDifficulty difficultyIn, final Vec3 posIn) {
		this(difficultyIn, posIn, RelocationStatus.NONE);
	}
	
	public InvasionMobParticlesPacket(final InvasionDifficulty difficultyIn, final Vec3 posIn, final boolean relocateIn) {
		this(difficultyIn, posIn, relocateIn ? RelocationStatus.TO : RelocationStatus.FROM);
	}
	
	private InvasionMobParticlesPacket(final InvasionDifficulty difficultyIn, final Vec3 posIn, final RelocationStatus statusIn) {
		this.difficulty = difficultyIn;
		this.x = posIn.x;
		this.y = posIn.y;
		this.z = posIn.z;
		this.status = statusIn;
	}

	public static final void encode(final InvasionMobParticlesPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(msgIn.difficulty);
		bufIn.writeDouble(msgIn.x);
		bufIn.writeDouble(msgIn.y);
		bufIn.writeDouble(msgIn.z);
		bufIn.writeEnum(msgIn.status);
	}

	public static final InvasionMobParticlesPacket decode(final FriendlyByteBuf bufIn) {
		return new InvasionMobParticlesPacket(bufIn.readEnum(InvasionDifficulty.class), new Vec3(bufIn.readDouble(), bufIn.readDouble(), bufIn.readDouble()), bufIn.readEnum(RelocationStatus.class));
	}

	public static final class Handler {
		public static final boolean handle(final InvasionMobParticlesPacket msgIn, final CustomPayloadEvent.Context ctxIn) {
			ctxIn.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final InvasionMobParticlesPacket msgIn, final CustomPayloadEvent.Context ctxIn) {
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
		}
	}
	
	private static enum RelocationStatus {
		NONE,
		FROM,
		TO
	}
}
