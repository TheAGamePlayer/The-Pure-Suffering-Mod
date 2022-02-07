package dev.theagameplayer.puresuffering.command;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.world.FixedInvasionWorldData;
import dev.theagameplayer.puresuffering.world.InvasionWorldData;
import dev.theagameplayer.puresuffering.world.TimedInvasionWorldData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class RemoveInvasionsCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return new TranslationTextComponent("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSource> SUGGEST_CURRENT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData)) {
					return contains(tiwData.getInvasionSpawner().getDayInvasions(), invasionType);
				} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData)) {
					return contains(tiwData.getInvasionSpawner().getNightInvasions(), invasionType);
				}
				return false;
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(fiwData.getInvasionSpawner().getInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getDayInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return ISuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getNightInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return ISuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (iwData.hasFixedTime()) {
			FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(fiwData.getInvasionSpawner().getInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return ISuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_ALL_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getDayInvasions(), invasionType) || contains(tiwData.getInvasionSpawner().getNightInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(fiwData.getInvasionSpawner().getInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_QUEUED_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getQueuedDayInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return ISuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_QUEUED_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (!iwData.hasFixedTime()) {
			TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(tiwData.getInvasionSpawner().getQueuedNightInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return ISuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_QUEUED_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		if (iwData.hasFixedTime()) {
			FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
			return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
				return contains(fiwData.getInvasionSpawner().getQueuedInvasions(), invasionType);
			}).map(InvasionType::getId), suggestionsBuilder);
		}
		return ISuggestionProvider.suggestResource(ImmutableList.<ResourceLocation>of().stream(), suggestionsBuilder);
	};

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("remove")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("current").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_CURRENT_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						if (ServerTimeUtil.isServerDay(ctx.getSource().getLevel(), tiwData)) {
							Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getDayInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getDayInvasions().remove(invasion);
							ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.day." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
						} else if (ServerTimeUtil.isServerNight(ctx.getSource().getLevel(), tiwData)) {
							Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getNightInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getNightInvasions().remove(invasion);
							ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.night." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
						} else {
							ctx.getSource().sendFailure(new TranslationTextComponent("commands.puresuffering.remove.failure").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)));
						}
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getInvasions(), ctx, "invasionType");
						fiwData.getInvasionSpawner().getInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.fixed." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					}
					return 0;
				}))).then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_DAY_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getDayInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getDayInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.day." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_NIGHT_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getNightInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getNightInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.night." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("fixed").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_FIXED_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getInvasions(), ctx, "invasionType");
						fiwData.getInvasionSpawner().getInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.fixed." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						Invasion invasion = null;
						if (contains(tiwData.getInvasionSpawner().getDayInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							invasion = getInvasion(tiwData.getInvasionSpawner().getDayInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getDayInvasions().remove(invasion);
						}
						if (contains(tiwData.getInvasionSpawner().getNightInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							invasion = getInvasion(tiwData.getInvasionSpawner().getNightInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getNightInvasions().remove(invasion);
						}
						if (invasion != null)
							ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						Invasion invasion = null;
						if (contains(fiwData.getInvasionSpawner().getInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							invasion = getInvasion(fiwData.getInvasionSpawner().getInvasions(), ctx, "invasionType");
							fiwData.getInvasionSpawner().getInvasions().remove(invasion);
						}
						if (invasion != null)
							ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					}
					return 0;
				}))).then(Commands.literal("queued").then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_DAY_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedDayInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getQueuedDayInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.day." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_NIGHT_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedNightInvasions(), ctx, "invasionType");
						tiwData.getInvasionSpawner().getQueuedDayInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.night." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("fixed").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_FIXED_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						Invasion invasion = getInvasion(fiwData.getInvasionSpawner().getQueuedInvasions(), ctx, "invasionType");
						fiwData.getInvasionSpawner().getQueuedInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.fixed." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(TextFormatting.RED)), true);
					}
					return 0;
				}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
					InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						Invasion invasion = null;
						if (contains(tiwData.getInvasionSpawner().getQueuedDayInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedDayInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getQueuedDayInvasions().remove(invasion);
						}
						if (contains(tiwData.getInvasionSpawner().getQueuedNightInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							invasion = getInvasion(tiwData.getInvasionSpawner().getQueuedNightInvasions(), ctx, "invasionType");
							tiwData.getInvasionSpawner().getQueuedNightInvasions().remove(invasion);
						}
						if (invasion != null)
							ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
						Invasion invasion = null;
						if (contains(fiwData.getInvasionSpawner().getQueuedInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
							invasion = getInvasion(fiwData.getInvasionSpawner().getQueuedInvasions(), ctx, "invasionType");
							fiwData.getInvasionSpawner().getQueuedInvasions().remove(invasion);
						}
						if (invasion != null)
							ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.all." + (invasion.isPrimary() ? "primary" : "secondary")).append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					}
					return 0;
				}))));
	}

	private static boolean contains(Iterable<Invasion> invasionListIn, InvasionType invasionTypeIn) {
		for (Invasion invasion : invasionListIn) {
			if (invasion.getType() == invasionTypeIn)
				return true;
		}
		return false;
	}

	private static Invasion getInvasion(Iterable<Invasion> invasionListIn, CommandContext<CommandSource> ctxIn, String argIn) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			for (Invasion invasion : invasionListIn) {
				if (invasion.getType() == invasionType)
					return invasion;
			}
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		}
	}
}
