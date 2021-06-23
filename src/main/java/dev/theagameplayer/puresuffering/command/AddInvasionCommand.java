package dev.theagameplayer.puresuffering.command;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class AddInvasionCommand {
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_INVASION_TYPE = new DynamicCommandExceptionType(resourceLocation -> {
		return new TranslationTextComponent("commands.puresuffering.invasion_type.invasionTypeNotFound", resourceLocation);
	});
	private static final SuggestionProvider<CommandSource> SUGGEST_NEXT_INVASION_TYPES = (ctx, suggestionsBuilder) -> {
		Collection<InvasionType> collection = BaseEvents.getInvasionTypeManager().getAllInvasionTypes();
		return ISuggestionProvider.suggestResource(collection.stream().filter(invasionType -> {
			if (TimeUtil.isDay(ctx.getSource().getServer().overworld())) {
				return invasionType.isDayInvasion() || (containsDayChangingInvasion(InvasionSpawner.getDayInvasions()) && !invasionType.isDayInvasion());
			} else if (TimeUtil.isNight(ctx.getSource().getServer().overworld())) {
				return !invasionType.isDayInvasion();
			} else {
				return false;
			}
		}).map(InvasionType::getId), suggestionsBuilder);
	};
	
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("add")
				.requires(player -> {
					return player.hasPermission(2);
				}).then(Commands.literal("next").then(Commands.argument("invasionType", ResourceLocationArgument.id()).suggests(SUGGEST_NEXT_INVASION_TYPES).then(Commands.argument("severity", IntegerArgumentType.integer(1)).executes(ctx -> {
					Pair<InvasionType, Integer> pair = getInvasionPair(ctx, "invasionType", "severity");
					if (TimeUtil.isDay(ctx.getSource().getServer().overworld()) && (pair.getLeft().isDayInvasion() || (containsDayChangingInvasion(InvasionSpawner.getDayInvasions()) && !pair.getLeft().isDayInvasion()))) {
						InvasionSpawner.getQueuedInvasions().add(pair);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else if (TimeUtil.isNight(ctx.getSource().getServer().overworld()) && !pair.getLeft().isDayInvasion()) {
						InvasionSpawner.getQueuedInvasions().add(pair);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendFailure(new TranslationTextComponent("commands.puresuffering.add.failure").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)));
					}
					return 0;
				})).then(Commands.literal("random").executes(ctx -> {
					Pair<InvasionType, Integer> pair = getInvasionPair(ctx, "invasionType", null);
					if (TimeUtil.isDay(ctx.getSource().getServer().overworld()) && (pair.getLeft().isDayInvasion() || (containsDayChangingInvasion(InvasionSpawner.getDayInvasions()) && !pair.getLeft().isDayInvasion()))) {
						InvasionSpawner.getQueuedInvasions().add(pair);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.day").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else if (TimeUtil.isNight(ctx.getSource().getServer().overworld()) && !pair.getLeft().isDayInvasion()) {
						InvasionSpawner.getQueuedInvasions().add(pair);
						ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.add.success.night").append(pair.getLeft().getComponent()).append("!").withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)), true);
					} else {
						ctx.getSource().sendFailure(new TranslationTextComponent("commands.puresuffering.add.failure").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED)));
					}
					return 0;
				}))));
	}
	
	private static boolean containsDayChangingInvasion(ArrayList<Pair<InvasionType, Integer>> invasionListIn) {
		for (Pair<InvasionType, Integer> pair : invasionListIn) {
			if (pair.getLeft().isDayInvasion() && pair.getLeft().setsEventsToNight())
				return true;
		}
		return false;
	}
	
	private static Pair<InvasionType, Integer> getInvasionPair(CommandContext<CommandSource> ctxIn, String argIn, String arg1In) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ctxIn.getArgument(argIn, ResourceLocation.class);
		InvasionType invasionType = BaseEvents.getInvasionTypeManager().getInvasionType(resourceLocation);
		if (invasionType == null) {
			throw ERROR_UNKNOWN_INVASION_TYPE.create(resourceLocation);
		} else {
			int severity = arg1In == null ? ctxIn.getSource().getLevel().getRandom().nextInt(invasionType.getMaxSeverity()) + 1 : MathHelper.clamp(IntegerArgumentType.getInteger(ctxIn, arg1In), 1, invasionType.getMaxSeverity());
			return Pair.of(invasionType, severity);
		}
	}
}
