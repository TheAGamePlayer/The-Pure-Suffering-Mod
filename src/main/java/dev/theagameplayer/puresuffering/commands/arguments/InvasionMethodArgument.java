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

import dev.theagameplayer.puresuffering.server.commands.AddInvasionsCommand.InvasionMethod;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class InvasionMethodArgument implements ArgumentType<InvasionMethod> {
	private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(method -> {
		return Component.translatable("argument.puresuffering.invasionMethod.invalid", method);
	});
	
	public static final InvasionMethodArgument method() {
		return new InvasionMethodArgument();
	}

	@Override
	public InvasionMethod parse(final StringReader pReader) throws CommandSyntaxException {
		final String name = pReader.readUnquotedString();
		final InvasionMethod method = InvasionMethod.valueOf(name.toUpperCase());
		if (method == null) throw ERROR_INVALID.createWithContext(pReader, name);
		return method;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> pCtx, final SuggestionsBuilder pSuggestionsBuilder) {
		return SharedSuggestionProvider.suggest(Stream.of(InvasionMethod.values()).map(InvasionMethod::toString), pSuggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return Stream.of(InvasionMethod.values()).map(InvasionMethod::toString).collect(Collectors.toList());
	}
}
