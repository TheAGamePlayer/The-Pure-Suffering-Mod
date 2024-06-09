
package dev.theagameplayer.puresuffering.registries.other;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.server.commands.AddInvasionsCommand;
import dev.theagameplayer.puresuffering.server.commands.ClearInvasionsCommand;
import dev.theagameplayer.puresuffering.server.commands.CycleCommand;
import dev.theagameplayer.puresuffering.server.commands.QueryInvasionsCommand;
import dev.theagameplayer.puresuffering.server.commands.RemoveInvasionsCommand;
import dev.theagameplayer.puresuffering.server.commands.SyncCommand;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class PSCommands {
	public static final void registerCommands(final RegisterCommandsEvent pEvent) {
		pEvent.getDispatcher().register(Commands.literal(PureSufferingMod.MODID)
				.then(ClearInvasionsCommand.register())
				.then(AddInvasionsCommand.register())
				.then(RemoveInvasionsCommand.register())
				.then(QueryInvasionsCommand.register())
				.then(CycleCommand.register())
				.then(SyncCommand.register()));
	}
}
