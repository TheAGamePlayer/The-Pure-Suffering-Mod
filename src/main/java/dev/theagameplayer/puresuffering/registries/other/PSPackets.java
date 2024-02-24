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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public final class PSPackets {
	public static final void registerPackets(final RegisterPayloadHandlerEvent eventIn) {
		final IPayloadRegistrar registrar = eventIn.registrar(PureSufferingMod.MODID);
		registrar.play(AddInvasionPacket.ID, AddInvasionPacket::read, handler -> handler.client(AddInvasionPacket::handle));
		registrar.play(ClearInvasionsPacket.ID, ClearInvasionsPacket::read, handler -> handler.client(ClearInvasionsPacket::handle));
		registrar.play(InvasionMobParticlesPacket.ID, InvasionMobParticlesPacket::read, handler -> handler.client(InvasionMobParticlesPacket::handle));
		registrar.play(SendInvasionsPacket.ID, SendInvasionsPacket::read, handler -> handler.client(SendInvasionsPacket::handle));
		registrar.play(InvasionStartPacket.ID, InvasionStartPacket::read, handler -> handler.client(InvasionStartPacket::handle));
		registrar.play(RemoveInvasionPacket.ID, RemoveInvasionPacket::read, handler -> handler.client(RemoveInvasionPacket::handle));
		registrar.play(SendInvasionAmbiencePacket.ID, SendInvasionAmbiencePacket::read, handler -> handler.client(SendInvasionAmbiencePacket::handle));
		registrar.play(UpdateGameRulePacket.ID, UpdateGameRulePacket::read, handler -> handler.client(UpdateGameRulePacket::handle));
		registrar.play(UpdateXPMultPacket.ID, UpdateXPMultPacket::read, handler -> handler.client(UpdateXPMultPacket::handle));
	}
	
	public static final void sendToClient(final CustomPacketPayload packetIn, final ServerPlayer playerIn) {
		PacketDistributor.PLAYER.with(playerIn).send(packetIn);
	}
	
	public static final void sendToClientsIn(final CustomPacketPayload packetIn, final ServerLevel levelIn) {
		PacketDistributor.DIMENSION.with(levelIn.dimension()).send(packetIn);
	}
	
	public static final void sendToAllClients(final CustomPacketPayload packetIn) {
		PacketDistributor.ALL.with(null).send(packetIn);
	}
}
