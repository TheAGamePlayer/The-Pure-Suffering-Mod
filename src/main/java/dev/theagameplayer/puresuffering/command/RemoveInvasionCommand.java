package dev.theagameplayer.puresuffering.command;

import java.util.Collection;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.theagameplayer.puresuffering.PSEventManager.BaseEvents;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.ServerInvasionUtil;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class RemoveInvasionCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return new TranslationTextComponent("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSource> SUGGEST_CURRENT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			if (ServerTimeUtil.isServerDay(ctx.getSource().getServer().overworld())) {
				return contains(InvasionSpawner.getDayInvasions(), invasionType);
			} else if (ServerTimeUtil.isServerNight(ctx.getSource().getServer().overworld())) {
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
	private static final SuggestionProvider<CommandSource> SUGGEST_QUEUED_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(InvasionSpawner.getQueuedDayInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_QUEUED_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return contains(InvasionSpawner.getQueuedNightInvasions(), invasionType);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("remove")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("current").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_CURRENT_INVASION_TYPES).executes(ctx -> {
					if (ServerTimeUtil.isServerDay(ctx.getSource().getServer().overworld())) {
						Invasion invasion = getInvasion(InvasionSpawner.getDayInvasions(), ctx, "invasionType");
						InvasionSpawner.getDayInvasions().remove(invasion);
						if (invasion.getType().getLightLevel() != 0)
							ServerInvasionUtil.getLightInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.day").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else if (ServerTimeUtil.isServerNight(ctx.getSource().getServer().overworld())) {
						Invasion invasion = getInvasion(InvasionSpawner.getNightInvasions(), ctx, "invasionType");
						InvasionSpawner.getNightInvasions().remove(invasion);
						if (invasion.getType().getLightLevel() != 0)
							ServerInvasionUtil.getLightInvasions().remove(invasion);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.night").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendFailure(new TranslationTextComponent("commands.puresuffering.remove.failure").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)));
					}
					return 0;
				}))).then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_DAY_INVASION_TYPES).executes(ctx -> {
					Invasion invasion = getInvasion(InvasionSpawner.getDayInvasions(), ctx, "invasionType");
					InvasionSpawner.getDayInvasions().remove(invasion);
					if (ServerTimeUtil.isServerDay(ctx.getSource().getServer().overworld()) && invasion.getType().getLightLevel() != 0)
						ServerInvasionUtil.getLightInvasions().remove(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.day").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_NIGHT_INVASION_TYPES).executes(ctx -> {
					Invasion invasion = getInvasion(InvasionSpawner.getNightInvasions(), ctx, "invasionType");
					InvasionSpawner.getNightInvasions().remove(invasion);
					if (ServerTimeUtil.isServerNight(ctx.getSource().getServer().overworld()) && invasion.getType().getLightLevel() != 0)
						ServerInvasionUtil.getLightInvasions().remove(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.night").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("all").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_ALL_INVASION_TYPES).executes(ctx -> {
					ResourceLocation resourceLocation = ctx.getArgument("invasionType", ResourceLocation.class);
					ITextComponent component = null;
					if (contains(InvasionSpawner.getDayInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
						Invasion invasion = getInvasion(InvasionSpawner.getDayInvasions(), ctx, "invasionType");
						InvasionSpawner.getDayInvasions().remove(invasion);
						if (invasion.getType().getLightLevel() != 0)
							ServerInvasionUtil.getLightInvasions().remove(invasion);
						component = invasion.getType().getComponent();
					}
					if (contains(InvasionSpawner.getNightInvasions(), BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation))) {
						Invasion invasion = getInvasion(InvasionSpawner.getNightInvasions(), ctx, "invasionType");
						InvasionSpawner.getNightInvasions().remove(invasion);
						if (invasion.getType().getLightLevel() != 0)
							ServerInvasionUtil.getLightInvasions().remove(invasion);
						component = invasion.getType().getComponent();
					}
					if (component != null)
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.all").append(component).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("queued").then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_DAY_INVASION_TYPES).executes(ctx -> {
					Invasion invasion = getInvasion(InvasionSpawner.getQueuedDayInvasions(), ctx, "invasionType");
					InvasionSpawner.getQueuedDayInvasions().remove(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.day").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_QUEUED_NIGHT_INVASION_TYPES).executes(ctx -> {
					Invasion invasion = getInvasion(InvasionSpawner.getQueuedNightInvasions(), ctx, "invasionType");
					InvasionSpawner.getQueuedDayInvasions().remove(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.remove.success.queued.night").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
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
