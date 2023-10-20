package dev.theagameplayer.puresuffering.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class CycleCommand {
	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("cycle").requires(css -> {
					return css.hasPermission(2);
				}).executes(ctx -> {
					final long dayTime = ctx.getSource().getLevel().getDayTime();
					final long addTime = 12000L - dayTime % 12000L;
					for (final ServerLevel level : ctx.getSource().getServer().getAllLevels())
						level.setDayTime(level.getDayTime() + addTime);
					ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.cycle", dayTime + addTime), true);
					return (int)addTime;
				});
	}
}
