package dev.theagameplayer.puresuffering.registries;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.commands.arguments.InvasionDifficultyArgument;
import dev.theagameplayer.puresuffering.commands.arguments.InvasionMethodArgument;
import dev.theagameplayer.puresuffering.commands.arguments.InvasionSessionTypeArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PSCommandArgumentTypes {
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPE = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, PureSufferingMod.MODID);

	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<InvasionSessionTypeArgument, SingletonArgumentInfo<InvasionSessionTypeArgument>.Template>> INVASION_SESSION_TYPE = COMMAND_ARGUMENT_TYPE.register("invasion_list_type", () -> ArgumentTypeInfos.registerByClass(InvasionSessionTypeArgument.class, SingletonArgumentInfo.contextFree(InvasionSessionTypeArgument::sessionType)));
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<InvasionDifficultyArgument, SingletonArgumentInfo<InvasionDifficultyArgument>.Template>> INVASION_DIFFICULTY = COMMAND_ARGUMENT_TYPE.register("invasion_difficulty", () -> ArgumentTypeInfos.registerByClass(InvasionDifficultyArgument.class, SingletonArgumentInfo.contextFree(InvasionDifficultyArgument::difficulty)));
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<InvasionMethodArgument, SingletonArgumentInfo<InvasionMethodArgument>.Template>> INVASION_METHOD = COMMAND_ARGUMENT_TYPE.register("invasion_method", () -> ArgumentTypeInfos.registerByClass(InvasionMethodArgument.class, SingletonArgumentInfo.contextFree(InvasionMethodArgument::method)));
}
