package dev.theagameplayer.puresuffering.server.commands;

import java.util.stream.Stream;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.commands.arguments.InvasionSessionTypeArgument;
import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.util.list.QueuedInvasionList;
import dev.theagameplayer.puresuffering.world.level.InvasionManager;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.ChatFormatting;

public final class ClearInvasionsCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID_SESSION_TYPE = new SimpleCommandExceptionType(Component.translatable("commands.puresuffering.session_type.invalid"));
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_INVASION_LIST_TYPES = (ctx, suggestionsBuilder) -> {
		return SharedSuggestionProvider.suggest(Stream.of(InvasionSessionType.values()).filter(sessionType -> {
			return ctx.getSource().getLevel().dimensionType().hasFixedTime() ? sessionType == InvasionSessionType.FIXED : sessionType != InvasionSessionType.FIXED;
		}).map(InvasionSessionType::toString), suggestionsBuilder);
	};

	public static final ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clear").requires(css -> {
			return css.hasPermission(2);
		}).then(Commands.literal("current").executes(ctx -> {
			final ServerLevel level = ctx.getSource().getLevel();
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
			if (session != null) session.clear(level);
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.clear.success", InvasionSessionType.getActive(level)).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			return 0;
		})).then(Commands.argument("sessionType", InvasionSessionTypeArgument.sessionType()).suggests(SUGGEST_INVASION_LIST_TYPES).executes(ctx -> {
			final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
			final ServerLevel level = ctx.getSource().getLevel();
			if ((level.dimensionType().hasFixedTime() && sessionType != InvasionSessionType.FIXED) || (!level.dimensionType().hasFixedTime() && sessionType == InvasionSessionType.FIXED)) throw ERROR_INVALID_SESSION_TYPE.create();
			final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getSession(sessionType);
			if (session != null) session.clear(level);
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.clear.success", sessionType.getTranslation()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			return 0;
		})).then(Commands.literal("all").executes(ctx -> {
			final ServerLevel level = ctx.getSource().getLevel();
			final InvasionManager invasionManager = InvasionLevelData.get(level).getInvasionManager();
			for (final InvasionSessionType sessionType : invasionManager.getListTypes()) {
				final InvasionSession session = invasionManager.getSession(sessionType);
				if (session != null) session.clear(level);
			}
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.clear.success.all").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			return 0;
		})).then(Commands.literal("queued").then(Commands.argument("sessionType", InvasionSessionTypeArgument.sessionType()).suggests(SUGGEST_INVASION_LIST_TYPES).executes(ctx -> {
			final InvasionSessionType sessionType = ctx.getArgument("sessionType", InvasionSessionType.class);
			final ServerLevel level = ctx.getSource().getLevel();
			if ((level.dimensionType().hasFixedTime() && sessionType != InvasionSessionType.FIXED) || (!level.dimensionType().hasFixedTime() && sessionType == InvasionSessionType.FIXED)) throw ERROR_INVALID_SESSION_TYPE.create();
			final InvasionManager invasionManager = InvasionLevelData.get(level).getInvasionManager();
			if (invasionManager.getQueued(sessionType) != null) invasionManager.setQueued(sessionType, null);
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.clear.success.queued", sessionType.getTranslation()).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			return 0;
		})).then(Commands.literal("all").executes(ctx -> {
			final ServerLevel level = ctx.getSource().getLevel();
			final InvasionManager invasionManager = InvasionLevelData.get(level).getInvasionManager();
			for (final InvasionSessionType sessionType : invasionManager.getListTypes()) {
				final QueuedInvasionList queuedList = invasionManager.getQueued(sessionType);
				if (queuedList != null) invasionManager.setQueued(sessionType, null);
			}
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.puresuffering.clear.success.queued.all").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
			return 0;
		})));
	}
}
