package dev.theagameplayer.puresuffering.command;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.event.PSBaseEvents;
import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeChangeability;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public final class AddInvasionsCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return Component.translatable("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsDayChangingInvasion(((TimedInvasionWorldData)iwData).getInvasionSpawner().getQueuedDayInvasions()) ? it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY : it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsNightChangingInvasion(((TimedInvasionWorldData)iwData).getInvasionSpawner().getQueuedNightInvasions()) ? it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT : it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};

	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("add")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("day").then(Commands.literal("primary").then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getRandomInvasion(ctx, iwData, false, true, true);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable(invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "commands.puresuffering.add.success.day.primary.mystery" : "commands.puresuffering.add.success.day.primary.hyper") : "commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
					})).then(Commands.literal("hyper").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.primary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.primary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("mystery").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.primary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.primary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getRandomInvasion(ctx, iwData, false, true, false);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable(invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "commands.puresuffering.add.success.day.primary.mystery" : "commands.puresuffering.add.success.day.primary.hyper") : "commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
					})).then(Commands.literal("hyper").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.secondary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.secondary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("mystery").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.secondary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.secondary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.day.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))))).then(Commands.literal("night").then(Commands.literal("primary").then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getRandomInvasion(ctx, iwData, false, false, true);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable(invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "commands.puresuffering.add.success.day.primary.mystery" : "commands.puresuffering.add.success.day.primary.hyper") : "commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
					})).then(Commands.literal("hyper").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.primary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.primary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("mystery").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.primary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.primary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getRandomInvasion(ctx, iwData, false, false, false);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable(invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "commands.puresuffering.add.success.day.primary.mystery" : "commands.puresuffering.add.success.day.primary.hyper") : "commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
					})).then(Commands.literal("hyper").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.secondary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.HYPER);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.secondary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("mystery").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.secondary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.MYSTERY);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.secondary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.DEFAULT);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.night.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))))).then(Commands.literal("fixed").then(Commands.literal("primary").then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getRandomInvasion(ctx, iwData, true, false, true);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable(invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "commands.puresuffering.add.success.day.primary.mystery" : "commands.puresuffering.add.success.day.primary.hyper") : "commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
					})).then(Commands.literal("hyper").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.HYPER);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.primary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.HYPER);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.primary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("mystery").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.MYSTERY);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.primary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.MYSTERY);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.primary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true, HyperType.DEFAULT);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true, HyperType.DEFAULT);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getRandomInvasion(ctx, iwData, true, false, false);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable(invasion.getHyperType() != HyperType.DEFAULT ? (invasion.getHyperType() == HyperType.MYSTERY ? "commands.puresuffering.add.success.day.primary.mystery" : "commands.puresuffering.add.success.day.primary.hyper") : "commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
					})).then(Commands.literal("hyper").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.HYPER);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.secondary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.HYPER);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.secondary.hyper").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("mystery").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.MYSTERY);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.secondary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.MYSTERY);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.secondary.mystery").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false, HyperType.DEFAULT);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false, HyperType.DEFAULT);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success.fixed.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))));
	}

	private static final boolean containsDayChangingInvasion(final ArrayList<Invasion> invasionListIn) {
		for (final Invasion invasion : invasionListIn) {
			if (invasion.getType().getInvasionTime() != InvasionTime.NIGHT && invasion.getType().getTimeModifier() == TimeModifier.DAY_TO_NIGHT)
				return true;
		}
		return false;
	}

	private static final boolean containsNightChangingInvasion(final ArrayList<Invasion> invasionListIn) {
		for (final Invasion invasion : invasionListIn) {
			if (invasion.getType().getInvasionTime() != InvasionTime.DAY && invasion.getType().getTimeModifier() == TimeModifier.NIGHT_TO_DAY)
				return true;
		}
		return false;
	}

	private static final Invasion getInvasion(final CommandContext<CommandSourceStack> ctxIn, final String argIn, final String arg1In, final boolean isPrimaryIn, final HyperType hyperTypeIn) throws CommandSyntaxException {
		final ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		final InvasionType invasionType = PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			final int severity = arg1In == null ? ctxIn.getSource().getLevel().getRandom().nextInt(invasionType.getMaxSeverity()) : Mth.clamp(IntegerArgumentType.getInteger(ctxIn, arg1In), 1, invasionType.getMaxSeverity()) - 1;
			return new Invasion(invasionType, hyperTypeIn != HyperType.DEFAULT ? invasionType.getMaxSeverity() - 1 : severity, isPrimaryIn, hyperTypeIn);
		}
	}
	
	private static final Invasion getRandomInvasion(final CommandContext<CommandSourceStack> ctxIn, final InvasionWorldData iwDataIn, final boolean hasFixedTimeIn, final boolean isDayIn, final boolean isPrimaryIn) throws CommandSyntaxException {
		final ArrayList<InvasionType> list = new ArrayList<>(PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes());
		list.removeIf(it -> {
			boolean result = false;
			if (hasFixedTimeIn) {
				if (isPrimaryIn) {
					result = it.getDimensions().contains(ctxIn.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY;
				} else {
					result = it.getDimensions().contains(ctxIn.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
				}
			} else {
				if (isDayIn) {
					if (isPrimaryIn) {
						result = it.getDimensions().contains(ctxIn.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT;
					} else {
						result = it.getDimensions().contains(ctxIn.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsDayChangingInvasion(((TimedInvasionWorldData)iwDataIn).getInvasionSpawner().getQueuedDayInvasions()) ? it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY : it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT);
					}
				} else {
					if (isPrimaryIn) {
						result = it.getDimensions().contains(ctxIn.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY;
					} else {
						result = it.getDimensions().contains(ctxIn.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsNightChangingInvasion(((TimedInvasionWorldData)iwDataIn).getInvasionSpawner().getQueuedNightInvasions()) ? it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT : it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY);
					}
				}
			}
			return !result;
		});
		final RandomSource random = ctxIn.getSource().getLevel().getRandom();
		final InvasionType invasionType = list.get(random.nextInt(list.size()));
		final ImmutableList<Boolean> hyper = ImmutableList.of(random.nextBoolean(), random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
		final HyperType hyperType = hyper.contains(true) && random.nextBoolean() ? (hyper.contains(false) ? HyperType.HYPER : HyperType.MYSTERY) : HyperType.DEFAULT;
		final int severity = hyperType != HyperType.DEFAULT ? invasionType.getMaxSeverity() - 1 : random.nextInt(invasionType.getMaxSeverity());
		return new Invasion(invasionType, severity, isPrimaryIn, hyperType);
	}
}
