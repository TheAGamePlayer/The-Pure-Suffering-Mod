package dev.theagameplayer.puresuffering.command;

import java.awt.Color;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionText;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public final class QueryInvasionsCommand {
	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("query").requires(player -> {
			return player.hasPermission(0);
		}).executes(ctx -> {
			final InvasionWorldData<?> iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
			if (!iwData.hasFixedTime()) {
				final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
				if (!ctx.getSource().getLevel().dimensionType().hasFixedTime()) {
					if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData) && !tiwData.getInvasionSpawner().getDayInvasions().isEmpty()) {
						ctx.getSource().sendSuccess(() -> InvasionText.create("commands.puresuffering.query.invasions", new Color(ChatFormatting.GOLD.getColor()), tiwData.getInvasionSpawner().getDayInvasions()).withStyle(Style.EMPTY.withBold(tiwData.getInvasionSpawner().getDayInvasions().getHyperType() != HyperType.DEFAULT).withItalic(tiwData.getInvasionSpawner().getDayInvasions().getHyperType() == HyperType.NIGHTMARE)), false);
						return 0;
					} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData) && !tiwData.getInvasionSpawner().getNightInvasions().isEmpty()) {
						ctx.getSource().sendSuccess(() -> InvasionText.create("commands.puresuffering.query.invasions", new Color(ChatFormatting.GOLD.getColor()), tiwData.getInvasionSpawner().getNightInvasions()).withStyle(Style.EMPTY.withBold(tiwData.getInvasionSpawner().getNightInvasions().getHyperType() != HyperType.DEFAULT).withItalic(tiwData.getInvasionSpawner().getNightInvasions().getHyperType() == HyperType.NIGHTMARE)), false);
						return 0;
					}
				}
				ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			} else {
				final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
				if (ctx.getSource().getLevel().dimensionType().hasFixedTime() && !fiwData.getInvasionSpawner().getInvasions().isEmpty()) {
					ctx.getSource().sendSuccess(() -> InvasionText.create("commands.puresuffering.query.invasions", new Color(ChatFormatting.GOLD.getColor()), fiwData.getInvasionSpawner().getInvasions()).withStyle(Style.EMPTY.withBold(fiwData.getInvasionSpawner().getInvasions().getHyperType() != HyperType.DEFAULT).withItalic(fiwData.getInvasionSpawner().getInvasions().getHyperType() == HyperType.NIGHTMARE)), false);
					return 0;
				}
				ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)), false);
			}
			return 0;
		});
	}
}
