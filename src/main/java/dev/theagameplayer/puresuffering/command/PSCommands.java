package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

public final class PSCommands {
	public static void build(CommandDispatcher<CommandSourceStack> dispatcherIn) {
		dispatcherIn.register(LiteralArgumentBuilder.<CommandSourceStack>literal("puresuffering")
				.then(ClearInvasionsCommand.register())
				.then(AddInvasionsCommand.register())
				.then(RemoveInvasionsCommand.register())
				.then(QueryInvasionsCommand.register()));
	}
}
