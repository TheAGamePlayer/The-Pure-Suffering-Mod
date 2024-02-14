package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.InvasionMobParticlesPacket;
import dev.theagameplayer.puresuffering.network.packet.SendInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.InvasionStartPacket;
import dev.theagameplayer.puresuffering.network.packet.RemoveInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.SendInvasionAmbiencePacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateGameRulePacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class PSPacketHandler {
	public static final SimpleChannel CHANNEL = ChannelBuilder.named(PureSufferingMod.namespace("main_network_channel")).simpleChannel();
	
	public static final void registerPackets() { //Using message builder instead of register message due to log spam of "Unknown custom packet identifier: puresuffering:main_network_channel"
		int id = 0;
		//PLAY DEDICATED SERVER -> CLIENT
		CHANNEL.messageBuilder(AddInvasionPacket.class, id++).encoder(AddInvasionPacket::encode).decoder(AddInvasionPacket::decode).consumerNetworkThread(AddInvasionPacket.Handler::handle).add();
		CHANNEL.messageBuilder(RemoveInvasionPacket.class, id++).encoder(RemoveInvasionPacket::encode).decoder(RemoveInvasionPacket::decode).consumerNetworkThread(RemoveInvasionPacket.Handler::handle).add();
		CHANNEL.messageBuilder(ClearInvasionsPacket.class, id++).encoder(ClearInvasionsPacket::encode).decoder(ClearInvasionsPacket::decode).consumerNetworkThread(ClearInvasionsPacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateXPMultPacket.class, id++).encoder(UpdateXPMultPacket::encode).decoder(UpdateXPMultPacket::decode).consumerNetworkThread(UpdateXPMultPacket.Handler::handle).add();
		CHANNEL.messageBuilder(SendInvasionsPacket.class, id++).encoder(SendInvasionsPacket::encode).decoder(SendInvasionsPacket::decode).consumerNetworkThread(SendInvasionsPacket.Handler::handle).add();
		CHANNEL.messageBuilder(InvasionStartPacket.class, id++).encoder(InvasionStartPacket::encode).decoder(InvasionStartPacket::decode).consumerNetworkThread(InvasionStartPacket.Handler::handle).add();
		CHANNEL.messageBuilder(InvasionMobParticlesPacket.class, id++).encoder(InvasionMobParticlesPacket::encode).decoder(InvasionMobParticlesPacket::decode).consumerNetworkThread(InvasionMobParticlesPacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateGameRulePacket.class, id++).encoder(UpdateGameRulePacket::encode).decoder(UpdateGameRulePacket::decode).consumerNetworkThread(UpdateGameRulePacket.Handler::handle).add();
		CHANNEL.messageBuilder(SendInvasionAmbiencePacket.class, id++).encoder(SendInvasionAmbiencePacket::encode).decoder(SendInvasionAmbiencePacket::decode).consumerNetworkThread(SendInvasionAmbiencePacket.Handler::handle).add();
	}
	
	public static final void sendToClient(final Object msgIn, final ServerPlayer playerIn) {
		CHANNEL.send(msgIn, PacketDistributor.PLAYER.with(playerIn));
	}
	
	public static final void sendToClientsIn(final Object msgIn, final ServerLevel levelIn) {
		CHANNEL.send(msgIn, PacketDistributor.DIMENSION.with(levelIn.dimension()));
	}
	
	public static final void sendToAllClients(final Object msgIn) {
		CHANNEL.send(msgIn, PacketDistributor.ALL.noArg());
	}
}
