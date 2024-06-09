package dev.theagameplayer.puresuffering.server.commands;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;

import dev.theagameplayer.puresuffering.commands.arguments.InvasionDifficultyArgument;
import dev.theagameplayer.puresuffering.commands.arguments.InvasionMethodArgument;
import dev.theagameplayer.puresuffering.commands.arguments.InvasionSessionTypeArgument;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import dev.theagameplayer.puresuffering.invasion.Invasion.BuildInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionPriority;
import dev.theagameplayer.puresuffering.invasion.InvasionType.InvasionTime;
import dev.theagameplayer.puresuffering.invasion.InvasionType.TimeModifier;
import dev.theagameplayer.puresuffering.registries.other.PSReloadListeners;
import dev.theagameplayer.puresuffering.util.list.QueuedInvasionList;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public final class AddInvasionsCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID_SESSION_TYPE = new SimpleCommandExceptionType(Component.translatable("commands.puresuffering.session_type.invalid"));
	private static final DynamicCommandExceptionType ERROR_INVALID_DIFFICULTY = new DynamicCommandExceptionType(name -> {
		return Component.translatable("commands.puresuffering.invasion_difficulty.invalid", name);
	});
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return Component.translatable("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final DynamicCommandExceptionType ERROR_INVALID_INVASION_TYPE = new DynamicCommandExceptionType(name -> {
		return Component.translatable("commands.puresuffering.invasion_type.invalid", name);
	});
	private static final DynamicCommandExceptionType ERROR_INVALID_METHOD_1 = new DynamicCommandExceptionType(method -> {
		return Component.translatable("commands.puresuffering.invasion_method.invalid1", method);
	});
	private static final SimpleCommandExceptionType ERROR_INVALID_METHOD_2 = new SimpleCommandExceptionType(Component.translatable("commands.puresuffering.invasion_method.invalid2"));
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_INVASION_SESSION_TYPES = (ctx, suggestionsBuilder) -> {
		final boolean hasFixedTime = ctx.getSource().getLevel().dimensionType().hasFixedTime();
		return SharedSuggestionProvider.suggest(Stream.of(InvasionSessionType.values()).filter(sessionType -> {
			return hasFixedTime ? sessionType == InvasionSessionType.FIXED : sessionType != InvasionSessionType.FIXED;
		}).map(InvasionSessionType::toString), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_INVASION_DIFFICULTIES = (ctx, suggestionsBuilder) -> {
		final ServerLevel level = ctx.getSource().getLevel();
		return SharedSuggestionProvider.suggest(Stream.of(InvasionDifficulty.values()).filter(difficulty -> {
			return difficulty.isAllowed(level);
		}).map(InvasionDifficulty::toString), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
		return SharedSuggestionProvider.suggestResource(getPrimaryInvasionTypes(ctx.getSource().getLevel(), sessionType), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
		final ServerLevel level = ctx.getSource().getLevel();
		final QueuedInvasionList queuedList = InvasionLevelData.get(level).getInvasionManager().getQueued(sessionType);
		if (queuedList == null) return Suggestions.empty();
		return SharedSuggestionProvider.suggestResource(getSecondaryInvasionTypes(ctx.getSource().getLevel(), sessionType, queuedList), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PRIMARY_INVASION_METHODS = (ctx, suggestionsBuilder) -> {
		final InvasionDifficulty difficulty = ctx.getArgument("difficulty", InvasionDifficulty.class);
		return difficulty.isHyper() ? suggestionsBuilder.suggest(InvasionMethod.MAX.toString()).buildFuture() : SharedSuggestionProvider.suggest(Stream.of(InvasionMethod.values()).map(InvasionMethod::toString), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SECONDARY_INVASION_METHODS = (ctx, suggestionsBuilder) -> {
		final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
		final ServerLevel level = ctx.getSource().getLevel();
		final QueuedInvasionList queuedList = InvasionLevelData.get(level).getInvasionManager().getQueued(sessionType);
		if (queuedList == null) return Suggestions.empty();
		final InvasionDifficulty difficulty = queuedList.getDifficulty();
		return difficulty.isHyper() ? suggestionsBuilder.suggest(InvasionMethod.MAX.toString()).buildFuture() : SharedSuggestionProvider.suggest(Stream.of(InvasionMethod.values()).map(InvasionMethod::toString), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SEVERITY = (ctx, suggestionsBuilder) -> {
		final InvasionMethod method = ctx.getArgument("severityMethod", InvasionMethod.class);
		if (method != InvasionMethod.SET) return Suggestions.empty();
		final ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
		final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			final String[] severities = new String[invasionType.getMaxSeverity()];
			for (int s = 0; s < severities.length; s++)
				severities[s] = Integer.toString(s + 1);
			return SharedSuggestionProvider.suggest(severities, suggestionsBuilder);
		}
	};

	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("add").requires(css -> {
			return css.hasPermission(2);
		}).then(Commands.argument("sessionType", InvasionSessionTypeArgument.sessionType()).suggests(SUGGEST_INVASION_SESSION_TYPES)
				.then(literalPrimary("primary", true))
				.then(literalPrimary("secondary", false))
				.then(Commands.literal("random").executes(ctx -> addRandomInvasion(ctx))));
	}

	private static final ArgumentBuilder<CommandSourceStack, ?> literalPrimary(final String pName, final boolean pIsPrimary) {
		final ArgumentBuilder<CommandSourceStack, ?> main = Commands.literal(pName);
		return pIsPrimary ?  main.then(Commands.argument("difficulty", InvasionDifficultyArgument.difficulty()).suggests(SUGGEST_INVASION_DIFFICULTIES).then(literalDifficulty(true))) : main.then(literalDifficulty(false));
	}

	private static final ArgumentBuilder<CommandSourceStack, ?> literalDifficulty(final boolean pIsPrimary) {
		return Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(pIsPrimary ? SUGGEST_PRIMARY_INVASION_TYPES : SUGGEST_SECONDARY_INVASION_TYPES)
				.then(Commands.argument("severityMethod", InvasionMethodArgument.method()).suggests(pIsPrimary ? SUGGEST_PRIMARY_INVASION_METHODS : SUGGEST_SECONDARY_INVASION_METHODS)
						.then(Commands.argument("severity", IntegerArgumentType.integer(1)).suggests(SUGGEST_SEVERITY).executes(ctx -> addInvasion(ctx, true, pIsPrimary))).executes(ctx -> addInvasion(ctx, false, pIsPrimary)));
	}

	private static final int addInvasion(final CommandContext<CommandSourceStack> pCtx, final boolean pSetSeverity, final boolean pIsPrimary) throws CommandSyntaxException {
		final ResourceLocation resourceLocation = pCtx.getArgument("invasionType", ResourceLocation.class);
		final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			final ServerLevel level = pCtx.getSource().getLevel();
			final InvasionSessionType sessionType = pCtx.getArgument("sessionType", InvasionSessionType.class);
			if ((level.dimensionType().hasFixedTime() && sessionType != InvasionSessionType.FIXED) || (!level.dimensionType().hasFixedTime() && sessionType == InvasionSessionType.FIXED)) throw ERROR_INVALID_SESSION_TYPE.create();
			final InvasionMethod method = pCtx.getArgument("severityMethod", InvasionMethod.class);
			if (pSetSeverity && method != InvasionMethod.SET) throw ERROR_INVALID_METHOD_1.create(method);
			if (!pSetSeverity && method == InvasionMethod.SET) throw ERROR_INVALID_METHOD_2.create();
			if (pIsPrimary && !getPrimaryInvasionTypes(level, sessionType).toList().contains(resourceLocation)) throw ERROR_INVALID_INVASION_TYPE.create(invasionType.getComponent().getString());
			final QueuedInvasionList queuedList = pIsPrimary ? InvasionLevelData.get(level).getInvasionManager().setQueued(sessionType, pCtx.getArgument("difficulty", InvasionDifficulty.class)) : InvasionLevelData.get(level).getInvasionManager().getQueued(sessionType);
			if (!pIsPrimary && !getSecondaryInvasionTypes(level, sessionType, queuedList).toList().contains(resourceLocation)) throw ERROR_INVALID_INVASION_TYPE.create(invasionType.getComponent().getString());
			final InvasionDifficulty difficulty = queuedList.getDifficulty();
			if (pIsPrimary && !difficulty.isAllowed(level)) throw ERROR_INVALID_DIFFICULTY.create(difficulty.getTranslation());
			final int severity = method == InvasionMethod.RANDOM ? level.random.nextInt(invasionType.getMaxSeverity()) : Mth.clamp(method == InvasionMethod.MAX ? invasionType.getMaxSeverity() : IntegerArgumentType.getInteger(pCtx, "severity"), 1, invasionType.getMaxSeverity()) - 1;
			final BuildInfo invasion = new BuildInfo(invasionType, difficulty.isHyper() ? invasionType.getMaxSeverity() - 1 : severity, pIsPrimary);
			queuedList.add(invasion);
			pCtx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success." + (pIsPrimary ? "primary" : "secondary"), difficulty.isHyper() ? difficulty.getTranslation() + " " : "", sessionType.getTranslation(), invasionType.getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			return 0;
		}
	}

	private static final int addRandomInvasion(final CommandContext<CommandSourceStack> pCtx) throws CommandSyntaxException {
		final InvasionSessionType sessionType = pCtx.getArgument("sessionType", InvasionSessionType.class);
		final ServerLevel level = pCtx.getSource().getLevel();
		final QueuedInvasionList queuedList = InvasionLevelData.get(level).getInvasionManager().getQueued(sessionType);
		final boolean isPrimary = queuedList == null || queuedList.isEmpty();
		final List<InvasionType> list = PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes().filter(it -> {
			final boolean flag1 = isPrimary ? it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY : it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
			final boolean flag2 = it.getDimensions().contains(level.dimension().location()) && flag1;
			switch (sessionType) {
			case DAY: return flag2 && sessionType.isAcceptableTime(it, !isPrimary && containsDayChangingInvasion(queuedList)) && (isPrimary || !containsDayChangingInvasion(queuedList) || sessionType.canBeChanged(it));
			case NIGHT: return flag2 && sessionType.isAcceptableTime(it, !isPrimary && containsNightChangingInvasion(queuedList)) && (isPrimary || !containsNightChangingInvasion(queuedList) || sessionType.canBeChanged(it));
			default: return flag2;
			}
		}).toList();
		final RandomSource random = level.getRandom();
		final InvasionType invasionType = list.get(random.nextInt(list.size()));
		final InvasionDifficulty[] difficulty = new InvasionDifficulty[] {InvasionDifficulty.DEFAULT};  //Another sneaky work around >.>
		if (isPrimary) { //Primary
			int total = 0;
			final int[] ranges = new int[InvasionDifficulty.values().length];
			for (int i = 0; i < ranges.length; i++) {
				total += InvasionDifficulty.values().length - i;
				ranges[i] = total;
			}
			final int value = random.nextInt(total) + 1;
			for (int i = ranges.length - 1; i > -1; i--) {
				final int range = ranges[i];
				if (value >= range) {
					difficulty[0] = InvasionDifficulty.values()[i];
					break;
				}
			}
			final int severity = difficulty[0].isHyper() ? invasionType.getMaxSeverity() - 1 : random.nextInt(invasionType.getMaxSeverity());
			final BuildInfo invasion = new BuildInfo(invasionType, severity, true);
			InvasionLevelData.get(level).getInvasionManager().setQueued(sessionType, difficulty[0]).add(invasion);
		} else { //Secondary
			difficulty[0] = queuedList.getDifficulty();
			final int severity = difficulty[0].isHyper() ? invasionType.getMaxSeverity() - 1 : random.nextInt(invasionType.getMaxSeverity());
			final BuildInfo invasion = new BuildInfo(invasionType, severity, false);
			queuedList.add(invasion);
		}
		pCtx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.add.success." + (isPrimary ? "primary" : "secondary"), difficulty[0].isHyper() ? difficulty[0].getTranslation() + " " : "", sessionType.getTranslation(), invasionType.getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
		return 0;
	}

	private static final boolean containsDayChangingInvasion(final QueuedInvasionList pQueuedList) {
		for (final BuildInfo invasion : pQueuedList) {
			final InvasionType it = invasion.getType();
			if (it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeModifier() == TimeModifier.DAY_TO_NIGHT)
				return true;
		}
		return false;
	}

	private static final boolean containsNightChangingInvasion(final QueuedInvasionList pQueuedList) {
		for (final BuildInfo invasion : pQueuedList) {
			final InvasionType it = invasion.getType();
			if (it.getInvasionTime() != InvasionTime.DAY && it.getTimeModifier() == TimeModifier.NIGHT_TO_DAY)
				return true;
		}
		return false;
	}
	
	private static final Stream<ResourceLocation> getPrimaryInvasionTypes(final ServerLevel pLevel, final InvasionSessionType pSessionType) {
		return PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes().filter(it -> {
			final boolean flag = it.getDimensions().contains(pLevel.dimension().location()) && it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY;
			return pSessionType.isAcceptableTime(it, false) && flag;
		}).map(InvasionType::getId);
	}
	
	private static final Stream<ResourceLocation> getSecondaryInvasionTypes(final ServerLevel pLevel, final InvasionSessionType pSessionType, final QueuedInvasionList pQueuedList) {
		return PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes().filter(it -> {
			final boolean flag = it.getDimensions().contains(pLevel.dimension().location()) && it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY;
			switch (pSessionType) {
			case DAY: return pSessionType.isAcceptableTime(it, containsDayChangingInvasion(pQueuedList)) && flag && (!containsDayChangingInvasion(pQueuedList) || pSessionType.canBeChanged(it));
			case NIGHT: return pSessionType.isAcceptableTime(it, containsNightChangingInvasion(pQueuedList)) && flag && (!containsNightChangingInvasion(pQueuedList) || pSessionType.canBeChanged(it));
			default: return flag;
			}
		}).map(InvasionType::getId);
	}

	public static enum InvasionMethod {
		SET,
		RANDOM,
		MAX;

		@Override
		public final String toString() {
			return super.toString().toLowerCase();
		}
	}
}
