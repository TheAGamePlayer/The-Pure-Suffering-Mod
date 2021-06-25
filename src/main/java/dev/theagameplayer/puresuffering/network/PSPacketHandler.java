package dev.theagameplayer.puresuffering.network;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.network.packet.UpdateTimePacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
	}
	
	public static void sendToClient(Object msgIn, ServerPlayerEntity playerIn) {
		if (!(playerIn instanceof FakePlayer))
			CHANNEL.sendTo(msgIn, playerIn.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static void sendToAllClients(Object msgIn, ServerWorld worldIn) {
		for (ServerPlayerEntity player : worldIn.players())
			sendToClient(msgIn, player);
	}
}
