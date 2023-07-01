package dev.theagameplayer.puresuffering.command;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.event.PSBaseEvents;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public final class RemoveInvasionsCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return Component.translatable("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_CURRENT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData)) {
					return contains(tiwData.getInvasionSpawner().getDayInvasions(), invasionType);
				} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData)) {
					return contains(tiwData.getInvasionSpawner().getNightInvasions(), invasionType);
				}
				return false;
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(fiwData.getInvasionSpawner().getInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getDayInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return SharedSuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getNightInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return SharedSuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (iwData.hasFixedTime()) {
			final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(fiwData.getInvasionSpawner().getInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return SharedSuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getDayInvasions(), invasionType) || contains(tiwData.getInvasionSpawner().getNightInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(fiwData.getInvasionSpawner().getInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEUED_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getQueuedDayInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return SharedSuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEUED_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getQueuedNightInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return SharedSuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEUED_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = PSBaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (iwData.hasFixedTime()) {
			final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
			return SharedSuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(fiwData.getInvasionSpawner().getQueuedInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return SharedSuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};

	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("remove")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("current").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_CURRENT_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData)) {
							final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getDayInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getDayInvasions().remove(invasion);
							ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.day." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData)) {
							final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getNightInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getNightInvasions().remove(invasion);
							ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.night." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						} else {
							ctx.getSource().sendFailure(Component.translatable("commands.puresuffering.remove.failure").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
						}
					} else {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getInvasions(), ctx, "invasionType");
						fiwData.getInvasionSpawner().getInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.fixed." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					}
					return 0;
				}))).then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_DAY_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getDayInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getDayInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.day." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_NIGHT_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getNightInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getNightInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.night." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("fixed").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_FIXED_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getInvasions(), ctx, "invasionType");
						fiwData.getInvasionSpawner().getInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.fixed." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						if (contains(tiwData.getInvasionSpawner().getDayInvasions(), PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getDayInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getDayInvasions().remove(invasion);
							if (invasion != null)
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						}
						if (contains(tiwData.getInvasionSpawner().getNightInvasions(), PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getNightInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getNightInvasions().remove(invasion);
							if (invasion != null)
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						}
					} else {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						if (contains(fiwData.getInvasionSpawner().getInvasions(), PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							final Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getInvasions(), ctx, "invasionType");
							fiwData.getInvasionSpawner().getInvasions().remove(invasion);
							if (invasion != null)
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						}
					}
					return 0;
				}))).then(Commands.literal("queued").then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_DAY_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedDayInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getQueuedDayInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.day." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_NIGHT_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedNightInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getQueuedDayInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.night." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("fixed").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_FIXED_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getQueuedInvasions(), ctx, "invasionType");
						fiwData.getInvasionSpawner().getQueuedInvasions().remove(invasion);
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.fixed." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						if (contains(tiwData.getInvasionSpawner().getQueuedDayInvasions(), PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedDayInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getQueuedDayInvasions().remove(invasion);
							if (invasion != null)
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						}
						if (contains(tiwData.getInvasionSpawner().getQueuedNightInvasions(), PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							final Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedNightInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getQueuedNightInvasions().remove(invasion);
							if (invasion != null)
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						}
					} else {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						if (contains(fiwData.getInvasionSpawner().getQueuedInvasions(), PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							final Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getQueuedInvasions(), ctx, "invasionType");
							fiwData.getInvasionSpawner().getQueuedInvasions().remove(invasion);
							if (invasion != null)
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
						}
					}
					return 0;
				}))));
	}

	private static final boolean contains(final Iterable<Invasion> invasionListIn, final InvasionType invasionTypeIn) {
		for (final Invasion invasion : invasionListIn) {
			if (invasion.getType() == invasionTypeIn)
				return true;
		}
		return false;
	}

	private static final Invasion getInvasion(final Iterable<Invasion> invasionListIn, final CommandContext<CommandSourceStack> ctxIn, final String argIn) throws CommandSyntaxException {
		final ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		final InvasionType invasionType = PSBaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			for (final Invasion invasion : invasionListIn) {
				if (invasion.getType() == invasionType)
					return invasion;
			}
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		}
	}
}
