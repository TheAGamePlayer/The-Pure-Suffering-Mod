package dev.theagameplayer.puresuffering.registries.other;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.network.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.InvasionMobParticlesPacket;
import dev.theagameplayer.puresuffering.network.InvasionStartPacket;
import dev.theagameplayer.puresuffering.network.RemoveInvasionPacket;
import dev.theagameplayer.puresuffering.network.SendInvasionAmbiencePacket;
import dev.theagameplayer.puresuffering.network.SendInvasionsPacket;
import dev.theagameplayer.puresuffering.network.UpdateGameRulePacket;
import dev.theagameplayer.puresuffering.network.UpdateXPMultPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PSPackets {
	public static final void registerPackets(final RegisterPayloadHandlersEvent pEvent) {
		final PayloadRegistrar registrar = pEvent.registrar(PureSufferingMod.MODID).versioned("1.0.0").optional();
		registrar.playToClient(AddInvasionPacket.TYPE, AddInvasionPacket.STREAM_CODEC, AddInvasionPacket::handle);
		registrar.playToClient(ClearInvasionsPacket.TYPE, ClearInvasionsPacket.STREAM_CODEC, ClearInvasionsPacket::handle);
		registrar.playToClient(InvasionMobParticlesPacket.TYPE, InvasionMobParticlesPacket.STREAM_CODEC, InvasionMobParticlesPacket::handle);
		registrar.playToClient(SendInvasionsPacket.TYPE, SendInvasionsPacket.STREAM_CODEC, SendInvasionsPacket::handle);
		registrar.playToClient(InvasionStartPacket.TYPE, InvasionStartPacket.STREAM_CODEC, InvasionStartPacket::handle);
		registrar.playToClient(RemoveInvasionPacket.TYPE, RemoveInvasionPacket.STREAM_CODEC, RemoveInvasionPacket::handle);
		registrar.playToClient(SendInvasionAmbiencePacket.TYPE, SendInvasionAmbiencePacket.STREAM_CODEC, SendInvasionAmbiencePacket::handle);
		registrar.playToClient(UpdateGameRulePacket.TYPE, UpdateGameRulePacket.STREAM_CODEC, UpdateGameRulePacket::handle);
		registrar.playToClient(UpdateXPMultPacket.TYPE, UpdateXPMultPacket.STREAM_CODEC, UpdateXPMultPacket::handle);
	}
	
	public static final void sendToClient(final CustomPacketPayload pPacket, final ServerPlayer pPlayer) {
		PacketDistributor.sendToPlayer(pPlayer, pPacket);
	}
	
	public static final void sendToClientsIn(final CustomPacketPayload pPacket, final ServerLevel levelIn) {
		PacketDistributor.sendToPlayersInDimension(levelIn, pPacket);
	}
	
	public static final void sendToAllClients(final CustomPacketPayload pPacket) {
		PacketDistributor.sendToAllPlayers(pPacket);
	}
}
