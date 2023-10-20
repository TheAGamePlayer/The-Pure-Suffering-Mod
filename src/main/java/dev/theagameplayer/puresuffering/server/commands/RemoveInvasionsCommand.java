package dev.theagameplayer.puresuffering.server.commands;

import java.util.stream.Stream;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;

import dev.theagameplayer.puresuffering.commands.arguments.InvasionSessionTypeArgument;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.Invasion.BuildInfo;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.invasion.InvasionTypeHolder;
import dev.theagameplayer.puresuffering.registries.other.PSReloadListeners;
import dev.theagameplayer.puresuffering.util.list.QueuedInvasionList;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public final class RemoveInvasionsCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID_SESSION_TYPE = new SimpleCommandExceptionType(Component.translatable("commands.puresuffering.session_type.invalid"));
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return Component.translatable("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_INVASION_LIST_TYPES = (ctx, suggestionsBuilder) -> {
		return SharedSuggestionProvider.suggest(Stream.of(InvasionSessionType.values()).filter(sessionType -> {
			return ctx.getSource().getLevel().dimensionType().hasFixedTime() ? sessionType == InvasionSessionType.FIXED : sessionType != InvasionSessionType.FIXED;
		}).map(InvasionSessionType::toString), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_CURRENT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Stream<InvasionType> stream = PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes();
		final ServerLevel level = ctx.getSource().getLevel();
		final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
		if (session == null) return Suggestions.empty();
		return SharedSuggestionProvider.suggestResource(stream.filter(it -> contains(session, it)).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Stream<InvasionType> stream = PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
		final InvasionSession session = InvasionLevelData.get(ctx.getSource().getLevel()).getInvasionManager().getSession(sessionType);
		if (session == null) return Suggestions.empty();
		return SharedSuggestionProvider.suggestResource(stream.filter(it -> contains(session, it)).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Stream<InvasionType> stream = PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionManager invasionManager = InvasionLevelData.get(ctx.getSource().getLevel()).getInvasionManager();
		return SharedSuggestionProvider.suggestResource(stream.filter(it -> {
			for (final InvasionSessionType sessionType : invasionManager.getListTypes()) {
				if (contains(invasionManager.getSession(sessionType), it))
					return true;
			}
			return false;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEUED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		final Stream<InvasionType> stream = PSReloadListeners.getInvasionTypeManager().getAllInvasionTypes();
		final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
		final InvasionManager invasionManager = InvasionLevelData.get(ctx.getSource().getLevel()).getInvasionManager();
		final QueuedInvasionList queuedList = invasionManager.getQueued(sessionType);
		if (queuedList == null) return Suggestions.empty();
		return SharedSuggestionProvider.suggestResource(stream.filter(it -> contains(queuedList, it)).map(InvasionType::getId), suggestionsBuilder);
	};

	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("remove").requires(css -> {
			return css.hasPermission(2);
		}).then(Commands.literal("current").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_CURRENT_INVASION_TYPES).executes(ctx -> {
			final ServerLevel level = ctx.getSource().getLevel();
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
			if (session != null) {
				final Invasion invasion = getInvasion(session, ctx, "invasionType");
				session.remove(level, invasion);
				ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success." + (invasion.isPrimary() ? "primary" : "secondary"), InvasionSessionType.getActive(level).getTranslation(), invasion.getType().getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			}
			return 0;
		}))).then(Commands.argument("sessionType", InvasionSessionTypeArgument.sessionType()).suggests(SUGGEST_INVASION_LIST_TYPES).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_INVASION_TYPES).executes(ctx -> {
			final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
			final ServerLevel level = ctx.getSource().getLevel();
			if ((level.dimensionType().hasFixedTime() && sessionType != InvasionSessionType.FIXED) || (!level.dimensionType().hasFixedTime() && sessionType == InvasionSessionType.FIXED)) throw ERROR_INVALID_SESSION_TYPE.create();
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getSession(sessionType);
			if (session != null) {
				final Invasion invasion = getInvasion(session, ctx, "invasionType");
				session.remove(level, invasion);
				ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success." + (invasion.isPrimary() ? "primary" : "secondary"), sessionType.getTranslation(), invasion.getType().getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			}
			return 0;
		}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
			final ServerLevel level = ctx.getSource().getLevel();
			final InvasionManager invasionManager = InvasionLevelData.get(level).getInvasionManager();
			boolean sentMessage = false;
			for (final InvasionSessionType sessionType : invasionManager.getListTypes()) {
				final InvasionSession session = invasionManager.getSession(sessionType);
				if (session == null) continue;
				final Invasion invasion = getInvasion(session, ctx, "invasionType");
				if (contains(session, invasion.getType())) session.remove(level, invasion);
				if (!sentMessage) {
					ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.all." + (invasion.isPrimary() ? "primary" : "secondary"), invasion.getType().getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					sentMessage = true;
				}
			}
			return 0;
		}))).then(Commands.literal("queued").then(Commands.argument("sessionType", InvasionSessionTypeArgument.sessionType()).suggests(SUGGEST_INVASION_LIST_TYPES).then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_INVASION_TYPES).executes(ctx -> {
			final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
			final ServerLevel level = ctx.getSource().getLevel();
			if ((level.dimensionType().hasFixedTime() && sessionType != InvasionSessionType.FIXED) || (!level.dimensionType().hasFixedTime() && sessionType == InvasionSessionType.FIXED)) throw ERROR_INVALID_SESSION_TYPE.create();
			final InvasionManager invasionManager = InvasionLevelData.get(level).getInvasionManager();
			final QueuedInvasionList queuedList = invasionManager.getQueued(sessionType);
			if (queuedList != null) {
				final BuildInfo invasion = getInvasion(queuedList, ctx, "invasionType");
				queuedList.remove(invasion);
				if (queuedList.isEmpty()) invasionManager.setQueued(sessionType, null);
				ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued." + (invasion.isPrimary() ? "primary" : "secondary"), sessionType.getTranslation(), invasion.getType().getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			}
			return 0;
		}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
			final ServerLevel level = ctx.getSource().getLevel();
			final InvasionManager invasionManager = InvasionLevelData.get(level).getInvasionManager();
			boolean sentMessage = false;
			for (final InvasionSessionType sessionType : invasionManager.getListTypes()) {
				final QueuedInvasionList queuedList = invasionManager.getQueued(sessionType);
				if (queuedList == null) continue;
				final BuildInfo invasion = getInvasion(queuedList, ctx, "invasionType");
				queuedList.remove(invasion);
				if (queuedList.isEmpty()) invasionManager.setQueued(sessionType, null);
				if (!sentMessage) {
					ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.remove.success.queued.all." + (invasion.isPrimary() ? "primary" : "secondary"), invasion.getType().getComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
					sentMessage = true;
				}
			}
			return 0;
		}))));
	}

	private static final <ITH extends InvasionTypeHolder> boolean contains(final Iterable<ITH> sessionTypeIn, final InvasionType invasionTypeIn) {
		for (final ITH invasion : sessionTypeIn) {
			if (invasion.getType() == invasionTypeIn) return true;
		}
		return false;
	}

	private static final <ITH extends InvasionTypeHolder> ITH getInvasion(final Iterable<ITH> sessionTypeIn, final CommandContext<CommandSourceStack> ctxIn, final String argIn) throws CommandSyntaxException {
		final ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		final InvasionType invasionType = PSReloadListeners.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			for (final ITH invasion : sessionTypeIn) {
				if (invasion.getType() == invasionType)
					return invasion;
			}
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		}
	}
}
