package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public final class QueryInvasionsCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("query").requires(player -> {
			return player.hasPermission(0);
		}).executes(ctx -> {
			InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
			if (!iwData.hasFixedTime()) {
				TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (!ctx.getSource().getLevel().dimensionType().hasFixedTime()) {
					if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) {
						ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", tiwData.getInvasionSpawner().getDayInvasions()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
						return 0;
					} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
						ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", tiwData.getInvasionSpawner().getNightInvasions()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
						return 0;
					}
				}
				ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			} else {
				FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
				if (ctx.getSource().getLevel().dimensionType().hasFixedTime() && !fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
					ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", fiwData.getInvasionSpawner().getInvasions()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
					return 0;
				}
				ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			}
			return 0;
		});
	}
}
