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

public final class AddInvasionCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return new TranslationTextComponent("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSource> SUGGEST_DAY_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = containsDayChangingInvasion(InvasionSpawner.getQueuedDayInvasions()) ? BaseEvents.getInvasionTypeManager().getNightInvasionTypes() : BaseEvents.getInvasionTypeManager().getDayInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			return !invasionType.isOnlyDuringNight();
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	private static final SuggestionProvider<CommandSource> SUGGEST_NIGHT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getNightInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().map(InvasionType::getId), suggestionsBuilder);
	};
	
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("add")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("day").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_DAY_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", "severity");
					InvasionSpawner.getQueuedDayInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", null);
					InvasionSpawner.getQueuedDayInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})))).then(Commands.literal("night").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_NIGHT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", "severity");
					InvasionSpawner.getQueuedNightInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Invasion invasion = getInvasion(ctx, "invasionType", null);
					InvasionSpawner.getQueuedNightInvasions().add(invasion);
					ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night").append(invasion.getType().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					return 0;
				}))));
	}
	
	private static boolean containsDayChangingInvasion(ArrayList<Invasion> invasionListIn) {
		for (Invasion invasion : invasionListIn) {
			if (invasion.getType().isDayInvasion() && invasion.getType().setsEventsToNight())
				return true;
		}
		return false;
	}
	
	private static Invasion getInvasion(CommandContext<CommandSource> ctxIn, String argIn, String arg1In) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			int severity = arg1In == null ? ctxIn.getSource().getLevel().getRandom().nextInt(invasionType.getMaxSeverity()) + 1 : MathHelper.clamp(IntegerArgumentType.getInteger(ctxIn, arg1In), 1, invasionType.getMaxSeverity());
			return new Invasion(invasionType, severity);
		}
	}
}
