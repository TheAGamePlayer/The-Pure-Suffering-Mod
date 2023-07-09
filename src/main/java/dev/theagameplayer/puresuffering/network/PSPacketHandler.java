package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.InvasionMobParticlesPacket;
import dev.theagameplayer.puresuffering.network.packet.InvasionSoundPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateCountPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PSPacketHandler {
	private static final String PROTOCAL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			PureSufferingMod.namespace("main_network_channel"), 
			() -> PROTOCAL_VERSION, 
			PROTOCAL_VERSION::equals, 
			PROTOCAL_VERSION::equals);
	
	public static final void registerPackets() { //Using message builder instead of register message due to log spam of "Unknown custom packet identifier: puresuffering:main_network_channel"
		int id = 0;
		//PLAY DEDICATED SERVER -> CLIENT
		CHANNEL.messageBuilder(UpdateTimePacket.class, id++).encoder(UpdateTimePacket::encode).decoder(UpdateTimePacket::decode).consumerMainThread(UpdateTimePacket.Handler::handle).add();
		CHANNEL.messageBuilder(AddInvasionPacket.class, id++).encoder(AddInvasionPacket::encode).decoder(AddInvasionPacket::decode).consumerMainThread(AddInvasionPacket.Handler::handle).add();
		CHANNEL.messageBuilder(ClearInvasionsPacket.class, id++).encoder(ClearInvasionsPacket::encode).decoder(ClearInvasionsPacket::decode).consumerMainThread(ClearInvasionsPacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateCountPacket.class, id++).encoder(UpdateCountPacket::encode).decoder(UpdateCountPacket::decode).consumerMainThread(UpdateCountPacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateXPMultPacket.class, id++).encoder(UpdateXPMultPacket::encode).decoder(UpdateXPMultPacket::decode).consumerMainThread(UpdateXPMultPacket.Handler::handle).add();
		CHANNEL.messageBuilder(InvasionSoundPacket.class, id++).encoder(InvasionSoundPacket::encode).decoder(InvasionSoundPacket::decode).consumerMainThread(InvasionSoundPacket.Handler::handle).add();
		CHANNEL.messageBuilder(InvasionMobParticlesPacket.class, id++).encoder(InvasionMobParticlesPacket::encode).decoder(InvasionMobParticlesPacket::decode).consumerMainThread(InvasionMobParticlesPacket.Handler::handle).add();
	}
	
	public static final void sendToClient(final Object msgIn, final ServerPlayer playerIn) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> playerIn), msgIn);
	}
	
	public static final void sendToAllClients(final Object msgIn) {
		CHANNEL.send(PacketDistributor.ALL.noArg(), msgIn);
	}
}
