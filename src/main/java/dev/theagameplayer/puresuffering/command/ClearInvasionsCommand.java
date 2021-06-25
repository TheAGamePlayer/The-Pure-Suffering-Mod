package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.TimeUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class ClearInvasionsCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("clear")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("current").executes(ctx -> {
					if (TimeUtil.isServerDay(ctx.getSource().getServer().overworld())) {
						InvasionSpawner.getDayInvasions().clear();
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.day").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else if (TimeUtil.isServerNight(ctx.getSource().getServer().overworld())) {
						InvasionSpawner.getNightInvasions().clear();
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.night").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendFailure(new TranslationTextComponent("commands.puresuffering.clear.failure").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)));
					}
					return 0;
				})).then(Commands.literal("day").executes(ctx -> {
					InvasionSpawner.getDayInvasions().clear();
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.day").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("night").executes(ctx -> {
					InvasionSpawner.getNightInvasions().clear();
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.night").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("all").executes(ctx -> {
					InvasionSpawner.getDayInvasions().clear();
					InvasionSpawner.getNightInvasions().clear();
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.all").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("queued").then(Commands.literal("day").executes(ctx -> {
					InvasionSpawner.getQueuedDayInvasions().clear();
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.queued.day").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("night").executes(ctx -> {
					InvasionSpawner.getQueuedNightInvasions().clear();
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.clear.success.queued.night").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})));
	}

}
