package dev.theagameplayer.puresuffering.command;

import java.util.ArrayList;
import java.util.Collection;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public final class AddInvasionsCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return Component.translatable("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsDayChangingInvasion(((TimedInvasionWorldData)iwData).getInvasionSpawner().getQueuedDayInvasions()) ? it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY : it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return !iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsNightChangingInvasion(((TimedInvasionWorldData)iwData).getInvasionSpawner().getQueuedNightInvasions()) ? it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT : it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_FIXED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
		return SharedSuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return iwData.hasFixedTime() && it.getDimensions().contains(ctx.getSource().getLevel().dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("add")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("day").then(Commands.literal("primary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.day.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false);
						tiwData.getInvasionSpawner().getQueuedDayInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.day.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))))).then(Commands.literal("night").then(Commands.literal("primary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.night.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().removeIf(i -> i.isPrimary());
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.night.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.night.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (!iwData.hasFixedTime()) {
						final TimedInvasionWorldData tiwData = (TimedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false);
						tiwData.getInvasionSpawner().getQueuedNightInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.night.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.nonfixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				}))))).then(Commands.literal("fixed").then(Commands.literal("primary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", true);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.fixed.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, true);
						fiwData.getInvasionSpawner().getQueuedInvasions().removeIf(i -> i.isPrimary());
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.fixed.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_FIXED_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", "severity", false);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.fixed.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					final InvasionWorldData iwData = InvasionWorldData.getInvasionData().get(ctx.getSource().getLevel());
					if (iwData.hasFixedTime()) {
						final FixedInvasionWorldData fiwData = (FixedInvasionWorldData)iwData;
						final Invasion invasion = getInvasion(ctx, "invasionType", null, false);
						fiwData.getInvasionSpawner().getQueuedInvasions().add(invasion);
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.add.success.fixed.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendSuccess(Component.translatable("commands.puresuffering.fixed").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)), true);
					}
					return 0;
				})))));
	}

	private static boolean containsDayChangingInvasion(final ArrayList<Invasion> invasionListIn) {
		for (final Invasion invasion : invasionListIn) {
			if (invasion.getType().getInvasionTime() != InvasionTime.NIGHT && invasion.getType().getTimeModifier() == TimeModifier.DAY_TO_NIGHT)
				return true;
		}
		return false;
	}

	private static boolean containsNightChangingInvasion(final ArrayList<Invasion> invasionListIn) {
		for (final Invasion invasion : invasionListIn) {
			if (invasion.getType().getInvasionTime() != InvasionTime.DAY && invasion.getType().getTimeModifier() == TimeModifier.NIGHT_TO_DAY)
				return true;
		}
		return false;
	}

	private static Invasion getInvasion(final CommandContext<CommandSourceStack> ctxIn, final String argIn, final String arg1In, final boolean isPrimaryIn) throws CommandSyntaxException {
		final ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		final InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			final int severity = arg1In == null ? ctxIn.getSource().getLevel().getRandom().nextInt(invasionType.getMaxSeverity()) : Mth.clamp(IntegerArgumentType.getInteger(ctxIn, arg1In), 1, invasionType.getMaxSeverity()) - 1;
			return new Invasion(invasionType, severity, isPrimaryIn);
		}
	}
}
