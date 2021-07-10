package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

public final class PSCommands {
	public static void build(CommandDispatcher<CommandSource> dispatcherIn) {
		dispatcherIn.register(LiteralArgumentBuilder.<CommandSource>literal("puresuffering")
				.then(ClearInvasionsCommand.register())
				.then(RemoveInvasionCommand.register())
				.then(AddInvasionCommand.register())
				.then(QueryInvasionsCommand.register()));
	}
}
