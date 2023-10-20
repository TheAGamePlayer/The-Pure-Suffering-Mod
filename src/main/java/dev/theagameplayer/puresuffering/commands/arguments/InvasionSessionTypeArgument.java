package dev.theagameplayer.puresuffering.commands.arguments;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.theagameplayer.puresuffering.invasion.InvasionSessionType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class InvasionSessionTypeArgument implements ArgumentType<InvasionSessionType> {
	private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(sessionType -> {
		return Component.translatable("argument.puresuffering.sessionTypeType.invalid", sessionType);
	});
	
	public static final InvasionSessionTypeArgument sessionType() {
		return new InvasionSessionTypeArgument();
	}

	@Override
	public final InvasionSessionType parse(final StringReader readerIn) throws CommandSyntaxException {
		final String name = readerIn.readUnquotedString();
		final InvasionSessionType sessionType = InvasionSessionType.valueOf(name.toUpperCase());
		if (sessionType == null) throw ERROR_INVALID.createWithContext(readerIn, name);
		return sessionType;
	}

	@Override
	public final <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctxIn, final SuggestionsBuilder suggestionsBuilderIn) {
		return SharedSuggestionProvider.suggest(Stream.of(InvasionSessionType.values()).map(InvasionSessionType::toString), suggestionsBuilderIn);
	}

	@Override
	public final Collection<String> getExamples() {
		return Stream.of(InvasionSessionType.values()).map(InvasionSessionType::toString).collect(Collectors.toList());
	}
}
