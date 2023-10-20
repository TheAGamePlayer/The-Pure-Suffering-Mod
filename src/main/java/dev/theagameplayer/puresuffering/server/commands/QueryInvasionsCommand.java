package dev.theagameplayer.puresuffering.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.SendInvasionsPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class QueryInvasionsCommand {
	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("query").requires(css -> {
			return css.hasPermission(0) && css.isPlayer();
		}).executes(ctx -> {
			PSPacketHandler.sendToClient(new SendInvasionsPacket(false), ctx.getSource().getPlayer());
			return 0;
		});
	}
}
