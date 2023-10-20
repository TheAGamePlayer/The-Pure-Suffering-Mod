package dev.theagameplayer.puresuffering.util.invasion;

import java.awt.Color;

import dev.theagameplayer.puresuffering.client.invasion.ClientInvasion;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public final class InvasionText {
	public static final MutableComponent getHoverText(final ClientInvasionSession sessionIn) {
		final MutableComponent sessionTypeComponent = Component.translatable("invasion.puresuffering.sessionType", sessionIn.getSessionType().getTranslation()).append("\n");
		final MutableComponent difficultyComponent = Component.translatable("invasion.puresuffering.difficultyInfo", sessionIn.getDifficulty().getTranslation());
		return sessionTypeComponent.append(difficultyComponent);
	}
	
	public static final MutableComponent getHoverText(final ClientInvasion invasionIn) {
		final MutableComponent primaryComponent = Component.translatable("invasion.puresuffering.isPrimaryInfo", invasionIn.isPrimary()).append("\n");
		final MutableComponent severityComponent = Component.translatable("invasion.puresuffering.severityInfo", invasionIn.getSeverity() + 1).append(", ");
		final MutableComponent mobCapComponent = Component.translatable("invasion.puresuffering.mobCapInfo", invasionIn.getMobCap()).append("\n");
		final MutableComponent infoComponent = Component.translatable("invasion.puresuffering.typeInfo").append("\n");
		final MutableComponent rarityComponent = Component.translatable("invasion.puresuffering.rarityInfo", invasionIn.getRarity() + 1).append(", ");
		final MutableComponent tierComponent = Component.translatable("invasion.puresuffering.tierInfo", invasionIn.getTier() + 1);
		return primaryComponent.append(severityComponent).append(mobCapComponent).append(infoComponent).append(rarityComponent).append(tierComponent);
	}
	
	public static final MutableComponent create(final String keyIn, final ChatFormatting baseIn, final ClientInvasionSession sessionIn) {
		return create(keyIn, baseIn.getColor(), sessionIn);
	}
	
	public static final MutableComponent create(final String keyIn, final int baseIn, final ClientInvasionSession sessionIn) {
		final Color color = new Color(baseIn);
		final MutableComponent sessionHoverText = ComponentUtils.mergeStyles(Component.translatable("invasion.puresuffering.sessionInfo"), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(getHoverText(sessionIn).withStyle(ChatFormatting.DARK_GRAY));
		final MutableComponent listText = Component.translatable("invasion.puresuffering.list").withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sessionHoverText)).withColor(color.getRGB()));
		final MutableComponent sessionText = (keyIn.isBlank() ? listText : Component.translatable(keyIn).append(listText)).withStyle(Style.EMPTY.withColor(color.getRGB()));
		for (final ClientInvasion invasion : sessionIn) {
			if (!invasion.isPrimary())
				sessionText.append(", ").withStyle(Style.EMPTY.withColor(color.getRGB()));
			final MutableComponent invasionHoverText = ComponentUtils.mergeStyles(Component.literal("- ").append(invasion.getComponent().copy()).append(" -"), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(getHoverText(invasion).withStyle(ChatFormatting.DARK_GRAY));
			final MutableComponent invasionText = invasion.getComponent().copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, invasionHoverText)));
			sessionText.append(invasionText.withStyle(Style.EMPTY.withColor(getSeverityColor(color, invasion, false, true, !sessionIn.getDifficulty().isNightmare())).withUnderlined(invasion.isPrimary())));
		}
		return sessionText;
	}
	
	public static final int getSeverityColor(final Color colorIn, final ClientInvasion invasionIn, final boolean rIn, final boolean gIn, final boolean bIn) {
		final int denom = 128/invasionIn.getMaxSeverity();
		final int severity = invasionIn.getSeverity();
		final int[] rgb = new int[3];
		rgb[0] = rIn ? Mth.clamp(colorIn.getRed() + invasionIn.getMaxSeverity() * denom - severity * denom, 0, 255) : colorIn.getRed();
		rgb[1] = gIn ? Mth.clamp(colorIn.getGreen() + invasionIn.getMaxSeverity() * denom - severity * denom, 0, 255) : colorIn.getGreen();
		rgb[2] = bIn ? Mth.clamp(colorIn.getBlue() + invasionIn.getMaxSeverity() * denom - severity * denom, 0, 255) : colorIn.getBlue();
		return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
	}
}
