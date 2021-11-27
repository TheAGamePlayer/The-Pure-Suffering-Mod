package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class QueryInvasionsCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("query").requires(player -> {
			return player.hasPermission(0);
		}).executes(ctx -> {
			if (ServerTimeUtil.isServerDay(ctx.getSource().getServer().overworld()) && !InvasionSpawner.getDayInvasions().isEmpty()) {
				ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", InvasionSpawner.getDayInvasions()).withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
			} else if (ServerTimeUtil.isServerNight(ctx.getSource().getServer().overworld()) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", InvasionSpawner.getNightInvasions()).withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
			} else {
				ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
			}
			return 0;
		});
	}
}
