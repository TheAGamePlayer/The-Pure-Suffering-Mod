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
	public static final MutableComponent getHoverText(final ClientInvasionSession pSession) {
		final MutableComponent sessionTypeComponent = Component.translatable("invasion.puresuffering.sessionType", pSession.getSessionType().getTranslation()).append("\n");
		final MutableComponent difficultyComponent = Component.translatable("invasion.puresuffering.difficultyInfo", pSession.getDifficulty().getTranslation());
		return sessionTypeComponent.append(difficultyComponent);
	}
	
	public static final MutableComponent getHoverText(final ClientInvasion pInvasion) {
		final MutableComponent primaryComponent = Component.translatable("invasion.puresuffering.isPrimaryInfo", pInvasion.isPrimary()).append("\n");
		final MutableComponent severityComponent = Component.translatable("invasion.puresuffering.severityInfo", pInvasion.getSeverity() + 1).append(", ");
		final MutableComponent mobCapComponent = Component.translatable("invasion.puresuffering.mobCapInfo", pInvasion.getMobCap()).append("\n");
		final MutableComponent infoComponent = Component.translatable("invasion.puresuffering.typeInfo").append("\n");
		final MutableComponent rarityComponent = Component.translatable("invasion.puresuffering.rarityInfo", pInvasion.getRarity() + 1).append(", ");
		final MutableComponent tierComponent = Component.translatable("invasion.puresuffering.tierInfo", pInvasion.getTier() + 1);
		return primaryComponent.append(severityComponent).append(mobCapComponent).append(infoComponent).append(rarityComponent).append(tierComponent);
	}
	
	public static final MutableComponent create(final String pKey, final ChatFormatting pBase, final ClientInvasionSession pSession) {
		return create(pKey, pBase.getColor(), pSession);
	}
	
	public static final MutableComponent create(final String pKey, final int pBase, final ClientInvasionSession pSession) {
		final Color color = new Color(pBase);
		final MutableComponent sessionHoverText = ComponentUtils.mergeStyles(Component.translatable("invasion.puresuffering.sessionInfo"), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(getHoverText(pSession).withStyle(ChatFormatting.DARK_GRAY));
		final MutableComponent listText = Component.translatable("invasion.puresuffering.list").withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, sessionHoverText)).withColor(color.getRGB()));
		final MutableComponent sessionText = (pKey.isBlank() ? listText : Component.translatable(pKey).append(listText)).withStyle(Style.EMPTY.withColor(color.getRGB()));
		for (final ClientInvasion invasion : pSession) {
			if (!invasion.isPrimary())
				sessionText.append(", ").withStyle(Style.EMPTY.withColor(color.getRGB()));
			final MutableComponent invasionHoverText = ComponentUtils.mergeStyles(Component.literal("- ").append(invasion.getComponent().copy()).append(" -"), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(getHoverText(invasion).withStyle(ChatFormatting.DARK_GRAY));
			final MutableComponent invasionText = invasion.getComponent().copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, invasionHoverText)));
			sessionText.append(invasionText.withStyle(Style.EMPTY.withColor(getSeverityColor(color, invasion, false, true, !pSession.getDifficulty().isNightmare())).withUnderlined(invasion.isPrimary())));
		}
		return sessionText;
	}
	
	public static final int getSeverityColor(final Color pColor, final ClientInvasion pInvasion, final boolean pR, final boolean pG, final boolean pB) {
		final int denom = 128/pInvasion.getMaxSeverity();
		final int severity = pInvasion.getSeverity();
		final int[] rgb = new int[3];
		rgb[0] = pR ? Mth.clamp(pColor.getRed() + pInvasion.getMaxSeverity() * denom - severity * denom, 0, 255) : pColor.getRed();
		rgb[1] = pG ? Mth.clamp(pColor.getGreen() + pInvasion.getMaxSeverity() * denom - severity * denom, 0, 255) : pColor.getGreen();
		rgb[2] = pB ? Mth.clamp(pColor.getBlue() + pInvasion.getMaxSeverity() * denom - severity * denom, 0, 255) : pColor.getBlue();
		return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
	}
}
