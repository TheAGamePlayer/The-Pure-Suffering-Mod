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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PSPackets {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			PureSufferingMod.namespace("main_network_channel"), 
			() -> PROTOCOL_VERSION, 
			PROTOCOL_VERSION::equals, 
			PROTOCOL_VERSION::equals);
	
	public static final void registerPackets() {
		int id = 0;
		//PLAY DEDICATED SERVER -> CLIENT
		CHANNEL.messageBuilder(AddInvasionPacket.class, id++).encoder(AddInvasionPacket::write).decoder(AddInvasionPacket::read).consumerNetworkThread(AddInvasionPacket.Handler::handle).add();
		CHANNEL.messageBuilder(ClearInvasionsPacket.class, id++).encoder(ClearInvasionsPacket::write).decoder(ClearInvasionsPacket::read).consumerNetworkThread(ClearInvasionsPacket.Handler::handle).add();
		CHANNEL.messageBuilder(InvasionMobParticlesPacket.class, id++).encoder(InvasionMobParticlesPacket::write).decoder(InvasionMobParticlesPacket::read).consumerNetworkThread(InvasionMobParticlesPacket.Handler::handle).add();
		CHANNEL.messageBuilder(SendInvasionsPacket.class, id++).encoder(SendInvasionsPacket::write).decoder(SendInvasionsPacket::read).consumerNetworkThread(SendInvasionsPacket.Handler::handle).add();
		CHANNEL.messageBuilder(InvasionStartPacket.class, id++).encoder(InvasionStartPacket::write).decoder(InvasionStartPacket::read).consumerNetworkThread(InvasionStartPacket.Handler::handle).add();
		CHANNEL.messageBuilder(RemoveInvasionPacket.class, id++).encoder(RemoveInvasionPacket::write).decoder(RemoveInvasionPacket::read).consumerNetworkThread(RemoveInvasionPacket.Handler::handle).add();
		CHANNEL.messageBuilder(SendInvasionAmbiencePacket.class, id++).encoder(SendInvasionAmbiencePacket::write).decoder(SendInvasionAmbiencePacket::read).consumerNetworkThread(SendInvasionAmbiencePacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateGameRulePacket.class, id++).encoder(UpdateGameRulePacket::write).decoder(UpdateGameRulePacket::read).consumerNetworkThread(UpdateGameRulePacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateXPMultPacket.class, id++).encoder(UpdateXPMultPacket::write).decoder(UpdateXPMultPacket::read).consumerNetworkThread(UpdateXPMultPacket.Handler::handle).add();
	}
	
	public static final void sendToClient(final CustomPacketPayload pPacket, final ServerPlayer pPlayer) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> pPlayer), pPacket);
	}
	
	public static final void sendToClientsIn(final CustomPacketPayload pPacket, final ServerLevel levelIn) {
		CHANNEL.send(PacketDistributor.DIMENSION.with(() -> levelIn.dimension()), pPacket);
	}
	
	public static final void sendToAllClients(final CustomPacketPayload pPacket) {
		CHANNEL.send(PacketDistributor.ALL.noArg(), pPacket);
	}
	
	public interface CustomPacketPayload {}
}
