package dev.theagameplayer.puresuffering.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.registries.other.PSGameRules;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SyncCommand {
	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("sync").requires(css -> {
			return css.hasPermission(2);
		}).then(Commands.literal("gamerules").executes(ctx -> {
			PSGameRules.syncToConfig(ctx.getSource().getServer());
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.sync.gamerules"), true);
			return 0;
		}));
	}
}
