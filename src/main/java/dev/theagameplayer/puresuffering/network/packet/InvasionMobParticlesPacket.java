package dev.theagameplayer.puresuffering.network.packet;

import java.util.function.Supplier;

import dev.theagameplayer.puresuffering.invasion.HyperType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public final class InvasionMobParticlesPacket {
	private final HyperType hyperType;
	private final double x, y, z;

	public InvasionMobParticlesPacket(final HyperType hyperTypeIn, final Vec3 posIn) {
		this.hyperType = hyperTypeIn;
		this.x = posIn.x;
		this.y = posIn.y;
		this.z = posIn.z;
	}

	public static final void encode(final InvasionMobParticlesPacket msgIn, final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(msgIn.hyperType);
		bufIn.writeDouble(msgIn.x);
		bufIn.writeDouble(msgIn.y);
		bufIn.writeDouble(msgIn.z);
	}

	public static final InvasionMobParticlesPacket decode(final FriendlyByteBuf bufIn) {
		return new InvasionMobParticlesPacket(bufIn.readEnum(HyperType.class), new Vec3(bufIn.readDouble(), bufIn.readDouble(), bufIn.readDouble()));
	}

	public static final class Handler {
		public static final boolean handle(final InvasionMobParticlesPacket msgIn, final Supplier<Context> ctxIn) {
			ctxIn.get().enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(msgIn, ctxIn));
			});
			return true;
		}

		private static final void handlePacket(final InvasionMobParticlesPacket msgIn, final Supplier<Context> ctxIn) {
			final Minecraft mc = Minecraft.getInstance();
			for (int l = 0; l < msgIn.hyperType.getParticleCount(); ++l) {
				final double x = msgIn.x + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				final double y = msgIn.y + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				final double z = msgIn.z + 0.5D + (mc.level.random.nextDouble() - 0.5D) * 2.0D;
				mc.level.addParticle(msgIn.hyperType.getSpawnParticleType(false), x, y, z, 0.0D, 0.0025D * l, 0.0D);
				mc.level.addParticle(msgIn.hyperType.getSpawnParticleType(true), x, y, z, 0.0D, 0.0025D * l, 0.0D);
			}
		}
	}
}
