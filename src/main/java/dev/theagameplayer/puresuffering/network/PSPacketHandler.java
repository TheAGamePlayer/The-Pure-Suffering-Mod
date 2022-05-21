package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateCountPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateXPMultPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public final class PSPacketHandler {
	private static final String PROTOCAL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(PureSufferingMod.MODID, "main_network_channel"), 
			() -> PROTOCAL_VERSION, 
			PROTOCAL_VERSION::equals, 
			PROTOCAL_VERSION::equals);
	
	public static void registerPackets() {
		int id = 0;
		//PLAY DEDICATED SERVER -> CLIENT
		CHANNEL.messageBuilder(UpdateTimePacket.class, id++).encoder(UpdateTimePacket::encode).decoder(UpdateTimePacket::decode).consumer(UpdateTimePacket.Handler::handle).add();
		CHANNEL.messageBuilder(AddInvasionPacket.class, id++).encoder(AddInvasionPacket::encode).decoder(AddInvasionPacket::decode).consumer(AddInvasionPacket.Handler::handle).add();
		CHANNEL.messageBuilder(ClearInvasionsPacket.class, id++).encoder(ClearInvasionsPacket::encode).decoder(ClearInvasionsPacket::decode).consumer(ClearInvasionsPacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateCountPacket.class, id++).encoder(UpdateCountPacket::encode).decoder(UpdateCountPacket::decode).consumer(UpdateCountPacket.Handler::handle).add();
		CHANNEL.messageBuilder(UpdateXPMultPacket.class, id++).encoder(UpdateXPMultPacket::encode).decoder(UpdateXPMultPacket::decode).consumer(UpdateXPMultPacket.Handler::handle).add();
	}
	
	public static void sendToClient(Object msgIn, ServerPlayer playerIn) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> playerIn), msgIn);
	}
	
	public static void sendToAllClients(Object msgIn) {
		CHANNEL.send(PacketDistributor.ALL.noArg(), msgIn);
	}
}
