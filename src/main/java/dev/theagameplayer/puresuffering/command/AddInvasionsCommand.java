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
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class AddInvasionsCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return new TranslationTextComponent("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSource> SUGGEST_PRIMARY_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_PRIMARY_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return it.getInvasionPriority() != InvasionPriority.SECONDARY_ONLY && it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY;
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_SECONDARY_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsDayChangingInvasion(InvasionSpawner.getQueuedDayInvasions()) ? it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY : it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_SECONDARY_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(it -> {
			return it.getInvasionPriority() != InvasionPriority.PRIMARY_ONLY && (containsNightChangingInvasion(InvasionSpawner.getQueuedNightInvasions()) ? it.getInvasionTime() != InvasionTime.NIGHT && it.getTimeChangeability() != TimeChangeability.ONLY_NIGHT : it.getInvasionTime() != InvasionTime.DAY && it.getTimeChangeability() != TimeChangeability.ONLY_DAY);
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("add")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("day").then(Commands.literal("primary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", "severity", true);
					InvasionSpawner.getQueuedDayInvasions().removeIf(i -> i.isPrimary());
					InvasionSpawner.getQueuedDayInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", null, true);
					InvasionSpawner.getQueuedDayInvasions().removeIf(i -> i.isPrimary());
					InvasionSpawner.getQueuedDayInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", "severity", false);
					InvasionSpawner.getQueuedDayInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", null, false);
					InvasionSpawner.getQueuedDayInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))))).then(Commands.literal("night").then(Commands.literal("primary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_PRIMARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", "severity", true);
					InvasionSpawner.getQueuedNightInvasions().removeIf(i -> i.isPrimary());
					InvasionSpawner.getQueuedNightInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", null, true);
					InvasionSpawner.getQueuedNightInvasions().removeIf(i -> i.isPrimary());
					InvasionSpawner.getQueuedNightInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night.primary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})))).then(Commands.literal("secondary").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_SECONDARY_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", "severity", false);
					InvasionSpawner.getQueuedNightInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", null, false);
					InvasionSpawner.getQueuedNightInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night.secondary").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})))));
	}
	
	private static boolean containsDayChangingInvasion(ArrayList<Invasion> invasionListIn) {
		for (Invasion invasion : invasionListIn) {
			if (invasion.getType().getInvasionTime() != InvasionTime.NIGHT && invasion.getType().getTimeModifier() == TimeModifier.DAY_TO_NIGHT)
				return true;
		}
		return false;
	}
	
	private static boolean containsNightChangingInvasion(ArrayList<Invasion> invasionListIn) {
		for (Invasion invasion : invasionListIn) {
			if (invasion.getType().getInvasionTime() != InvasionTime.DAY && invasion.getType().getTimeModifier() == TimeModifier.NIGHT_TO_DAY)
				return true;
		}
		return false;
	}
	
	private static Invasion getInvasion(CommandContext<CommandSource> ctxIn, String argIn, String arg1In, boolean isPrimaryIn) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			int severity = arg1In == null ? ctxIn.getSource().getLevel().getRandom().nextInt(invasionType.getMaxSeverity()) : MathHelper.clamp(IntegerArgumentType.getInteger(ctxIn, arg1In), 1, invasionType.getMaxSeverity()) - 1;
			return new Invasion(invasionType, severity, isPrimaryIn);
		}
	}
}
