package dev.theagameplayer.puresuffering.command;

import java.util.HashMap;

import com.mojang.brigadier.builder.ArgumentBuilder;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.invasion.InvasionType;
import dev.theagameplayer.puresuffering.spawner.InvasionSpawner;
import dev.theagameplayer.puresuffering.util.InvasionList;
import dev.theagameplayer.puresuffering.util.ServerTimeUtil;
import dev.theagameplayer.puresuffering.util.text.InvasionListTextComponent;
import dev.theagameplayer.puresuffering.util.text.InvasionText;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class QueryInvasionsCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("query").requires(player -> {
			return player.hasPermission(0);
		}).executes(ctx -> {
			HashMap<InvasionType, InvasionText> map = new HashMap<>();
			if (ServerTimeUtil.isServerDay(ctx.getSource().getServer().overworld()) && !InvasionSpawner.getDayInvasions().isEmpty()) {
				setMap(map, InvasionSpawner.getDayInvasions());
			} else if (ServerTimeUtil.isServerNight(ctx.getSource().getServer().overworld()) && !InvasionSpawner.getNightInvasions().isEmpty()) {
				setMap(map, InvasionSpawner.getNightInvasions());
			} else {
				ctx.getSource().sendSuccess(new TranslationTextComponent("commands.puresuffering.query.none").withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
				return 0;
			}
			ctx.getSource().sendSuccess(new InvasionListTextComponent("commands.puresuffering.query.invasions", map).withStyle(Style.EMPTY.withColor(TextFormatting.GOLD)), false);
			return 0;
		});
	}
	
	private static void setMap(HashMap<InvasionType, InvasionText> mapIn, InvasionList invasionListIn) {
		for (Invasion invasion : invasionListIn) {
			if (!mapIn.containsKey(invasion.getType())) {
				mapIn.put(invasion.getType(), new InvasionText(invasion.getSeverity()));
			} else if (mapIn.get(invasion.getType()).getSeverity() < invasion.getSeverity()) {
				mapIn.get(invasion.getType()).setSeverity(invasion.getSeverity());
			}
			mapIn.get(invasion.getType()).incrementAmount();
		};
	}
}
