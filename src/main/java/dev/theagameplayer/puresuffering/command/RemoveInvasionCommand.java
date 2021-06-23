package dev.theagameplayer.puresuffering.command;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.TimeUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class RemoveInvasionCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return new TranslationTextComponent("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSource> SUGGEST_CURRENT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			if (TimeUtil.isDay(ctx.getSource().getServer().overworld())) {
				return contains(InvasionSpawner.getDayInvasions(), invasionType);
			} else if (TimeUtil.isNight(ctx.getSource().getServer().overworld())) {
				return contains(InvasionSpawner.getNightInvasions(), invasionType);
			} else {
				return false;
			}
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(InvasionSpawner.getDayInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(InvasionSpawner.getNightInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_ALL_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(InvasionSpawner.getDayInvasions(), invasionType) || contains(InvasionSpawner.getNightInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_QUEUED_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(InvasionSpawner.getQueuedInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("remove")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("current").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_CURRENT_INVASION_TYPES).executes(ctx -> {
					if (TimeUtil.isDay(ctx.getSource().getServer().overworld())) {
						Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getDayInvasions(), ctx, "invasionType");
						InvasionSpawner.getDayInvasions().remove(pair);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.day").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else if (TimeUtil.isNight(ctx.getSource().getServer().overworld())) {
						Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getNightInvasions(), ctx, "invasionType");
						InvasionSpawner.getNightInvasions().remove(pair);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.night").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendFailure(new TranslationTextComponent("commands.puresuffering.remove.failure").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)));
					}
					return 0;
				}))).then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_DAY_INVASION_TYPES).executes(ctx -> {
					Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getDayInvasions(), ctx, "invasionType");
					InvasionSpawner.getDayInvasions().remove(pair);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.day").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_NIGHT_INVASION_TYPES).executes(ctx -> {
					Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getNightInvasions(), ctx, "invasionType");
					InvasionSpawner.getNightInvasions().remove(pair);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.night").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
					ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
					ITextComponent component = null;
					if (contains(InvasionSpawner.getDayInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
						Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getDayInvasions(), ctx, "invasionType");
						InvasionSpawner.getDayInvasions().remove(pair);
						component = pair.getLeft().getComponent();
					}
					if (contains(InvasionSpawner.getNightInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
						Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getNightInvasions(), ctx, "invasionType");
						InvasionSpawner.getNightInvasions().remove(pair);
						component = pair.getLeft().getComponent();
					}
					if (component != null)
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.all").append(component).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("queued").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_INVASION_TYPES).executes(ctx -> {
					Pair<InvasionType, Integer> pair = getInvasionPair(InvasionSpawner.getQueuedInvasions(), ctx, "invasionType");
					InvasionSpawner.getQueuedInvasions().remove(pair);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})));
	}
	
	private static boolean contains(ArrayList<Pair<InvasionType, Integer>> invasionListIn, InvasionType invasionTypeIn) {
		for (Pair<InvasionType, Integer> pair : invasionListIn) {
			if (pair.getLeft() == invasionTypeIn)
				return true;
		}
		return false;
	}
	
	private static Pair<InvasionType, Integer> getInvasionPair(ArrayList<Pair<InvasionType, Integer>> invasionListIn, CommandContext<CommandSource> ctxIn, String argIn) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			for (Pair<InvasionType, Integer> pair : invasionListIn) {
				if (pair.getLeft() == invasionType)
					return pair;
			}
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		}
	}
}
