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

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class InvasionDifficultyArgument implements ArgumentType<InvasionDifficulty> {
	private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(difficulty -> {
		return Component.translatable("argument.puresuffering.invasionDifficulty.invalid", difficulty);
	});
	
	public static final InvasionDifficultyArgument difficulty() {
		return new InvasionDifficultyArgument();
	}

	@Override
	public final InvasionDifficulty parse(final StringReader readerIn) throws CommandSyntaxException {
		final String name = readerIn.readUnquotedString();
		final InvasionDifficulty difficulty = InvasionDifficulty.valueOf(name.toUpperCase());
		if (difficulty == null) throw ERROR_INVALID.createWithContext(readerIn, name);
		return difficulty;
	}

	@Override
	public final <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctxIn, final SuggestionsBuilder suggestionsBuilderIn) {
		return SharedSuggestionProvider.suggest(Stream.of(InvasionDifficulty.values()).map(InvasionDifficulty::toString), suggestionsBuilderIn);
	}

	@Override
	public final Collection<String> getExamples() {
		return Stream.of(InvasionDifficulty.values()).map(InvasionDifficulty::toString).collect(Collectors.toList());
	}
}
