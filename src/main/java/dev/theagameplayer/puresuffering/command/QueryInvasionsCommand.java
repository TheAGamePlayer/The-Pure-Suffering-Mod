package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
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
			InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
			if (!iwData.hasFixedTime()) {
				TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (!ctx.getSource().getLevel().dimensionType().hasFixedTime()) {
					if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) {
						ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", tiwData.getInvasionSpawner().getDayInvasions()).withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
						return 0;
					} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
						ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", tiwData.getInvasionSpawner().getNightInvasions()).withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
						return 0;
					}
				}
				ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
			} else {
				FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
				if (ctx.getSource().getLevel().dimensionType().hasFixedTime() && !fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
					ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", fiwData.getInvasionSpawner().getInvasions()).withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
					return 0;
				}
				ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
			}
			return 0;
		});
	}
}
