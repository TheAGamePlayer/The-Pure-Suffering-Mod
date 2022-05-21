package dev.theagameplayer.puresuffering.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public final class ClearInvasionsCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clear")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("current").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData)) {
							tiwData.getInvasionSpawner().getDayInvasions().clear();
							ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.day").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData)) {
							tiwData.getInvasionSpawner().getNightInvasions().clear();
							ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.night").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						} else {
							ctx.getSource().sendFailure(new TranslatableComponent("commands.puresuffering.clear.failure").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
						}
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					}
					return 0;
				})).then(Commands.literal("day").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getDayInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.day").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("night").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getNightInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.night").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("fixed").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("all").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getDayInvasions().clear();
						tiwData.getInvasionSpawner().getNightInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.all").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.all").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					}
					return 0;
				})).then(Commands.literal("queued").then(Commands.literal("day").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getQueuedDayInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.queued.day").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("night").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getQueuedNightInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.queued.night").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("fixed").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getQueuedInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.queued.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("all").executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						tiwData.getInvasionSpawner().getQueuedDayInvasions().clear();
						tiwData.getInvasionSpawner().getQueuedNightInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.queued.all").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						fiwData.getInvasionSpawner().getQueuedInvasions().clear();
						ctx.getSource().sendSuccess(new TranslatableComponent("commands.puresuffering.clear.success.queued.all").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					}
					return 0;
				})));
	}

}
