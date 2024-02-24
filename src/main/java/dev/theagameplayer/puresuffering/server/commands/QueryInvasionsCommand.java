package dev.theagameplayer.puresuffering.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.network.SendInvasionsPacket;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class QueryInvasionsCommand {
	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("query").requires(css -> {
			return css.hasPermission(0) && css.isPlayer();
		}).executes(ctx -> {
			PSPackets.sendToClient(new SendInvasionsPacket(false), ctx.getSource().getPlayer());
			return 0;
		});
	}
}
