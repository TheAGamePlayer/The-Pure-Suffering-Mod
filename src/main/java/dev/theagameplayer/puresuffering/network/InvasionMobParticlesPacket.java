package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public final class InvasionMobParticlesPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = PureSufferingMod.namespace("invasion_mob_particles");
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

	@Override
	public final void write(final FriendlyByteBuf bufIn) {
		bufIn.writeEnum(this.difficulty);
		bufIn.writeDouble(this.x);
		bufIn.writeDouble(this.y);
		bufIn.writeDouble(this.z);
		bufIn.writeEnum(this.status);
	}

	public static final InvasionMobParticlesPacket read(final FriendlyByteBuf bufIn) {
		return new InvasionMobParticlesPacket(bufIn.readEnum(InvasionDifficulty.class), new Vec3(bufIn.readDouble(), bufIn.readDouble(), bufIn.readDouble()), bufIn.readEnum(RelocationStatus.class));
	}

	public static final void handle(final InvasionMobParticlesPacket msgIn, final PlayPayloadContext ctxIn) {
		ctxIn.workHandler().execute(() -> {
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
	public final ResourceLocation id() {
		return ID;
	}

	private static enum RelocationStatus {
		NONE,
		FROM,
		TO
	}
}
