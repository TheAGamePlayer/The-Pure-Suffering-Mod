package dev.theagameplayer.puresuffering.util.text;

import java.awt.Color;

import dev.theagameplayer.puresuffering.invasion.HyperType;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.util.InvasionList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class InvasionText {
	private final boolean isPrimary;
	private final HyperType hyperType;
	private final int severity;
	private final int rarity;
	private final int tier;
	private final int mobCap;
	
	public InvasionText(final Invasion invasionIn) {
		this.isPrimary = invasionIn.isPrimary();
		this.severity = invasionIn.getSeverity() + 1;
		this.hyperType = invasionIn.getHyperType();
		this.rarity = invasionIn.getType().getRarity() + 1;
		this.tier = invasionIn.getType().getTier() + 1;
		this.mobCap = invasionIn.getMobCap() - 1;
	}
	
	public final MutableComponent getHoverText() {
		final MutableComponent primaryComponent = Component.translatable("invasion.puresuffering.info1").append(this.isPrimary + ", ");
		final MutableComponent severityComponent = Component.translatable("invasion.puresuffering.info2").append(this.severity + "\n");
		final MutableComponent hyperComponent = Component.translatable("invasion.puresuffering.info3").append(this.hyperType + "\n");
		final MutableComponent infoComponent = Component.translatable("invasion.puresuffering.info4").append("\n");
		final MutableComponent rarityComponent = Component.translatable("invasion.puresuffering.info5").append(this.rarity + ", ");
		final MutableComponent tierComponent = Component.translatable("invasion.puresuffering.info6").append(this.tier + ", ");
		final MutableComponent mobCapComponent = Component.translatable("invasion.puresuffering.info7").append(this.mobCap + "");
		return primaryComponent.append(severityComponent).append(hyperComponent).append(infoComponent).append(rarityComponent).append(tierComponent).append(mobCapComponent);
	}
	
	public static final MutableComponent create(final String keyIn, final Color baseIn, final InvasionList invasionListIn) {
		final MutableComponent component = Component.translatable(keyIn).withStyle(Style.EMPTY.withColor(baseIn.getRGB()));
		for (final Invasion invasion : invasionListIn) {
			if (!component.getSiblings().isEmpty())
				component.append(", ").withStyle(Style.EMPTY.withColor(baseIn.getRGB()));
			final MutableComponent component1 = ComponentUtils.mergeStyles(invasion.getType().getComponent().copy(), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(new InvasionText(invasion).getHoverText().withStyle(ChatFormatting.DARK_GRAY));
			final MutableComponent component2 = invasion.getType().getComponent().copy().withStyle(style -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1));
			});
			component.append(component2.withStyle(Style.EMPTY.withColor(new Color(baseIn.getRed(), getInvColor(baseIn.getGreen(), invasion), invasion.getHyperType() == HyperType.MYSTERY ? baseIn.getBlue() : getInvColor(baseIn.getBlue(), invasion)).getRGB()).withUnderlined(invasion.isPrimary())));
		}
		return component;
	}
	
	private static final int getInvColor(final int colorIn, final Invasion invasionIn) {
		final int amount = 100/invasionIn.getType().getMaxSeverity();
		return Mth.clamp(colorIn + invasionIn.getType().getMaxSeverity() * amount - invasionIn.getSeverity() * amount, 0, 255);
	}
}
