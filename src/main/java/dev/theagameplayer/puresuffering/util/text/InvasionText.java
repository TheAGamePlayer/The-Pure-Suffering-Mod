package dev.theagameplayer.puresuffering.util.text;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.util.InvasionList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class InvasionText {
	private final boolean isPrimary;
	private final int severity;
	private final int rarity;
	private final int tier;
	private final int mobCap;
	
	public InvasionText(Invasion invasionIn) {
		this.isPrimary = invasionIn.isPrimary();
		this.severity = invasionIn.getSeverity() + 1;
		this.rarity = invasionIn.getType().getRarity() + 1;
		this.tier = invasionIn.getType().getTier() + 1;
		this.mobCap = invasionIn.getMobCap() - 1;
	}
	
	public MutableComponent getHoverText() {
		MutableComponent primaryComponent = Component.translatable("invasion.puresuffering.info1").append(this.isPrimary + ", ");
		MutableComponent severityComponent = Component.translatable("invasion.puresuffering.info2").append(this.severity + "\n");
		MutableComponent infoComponent = Component.translatable("invasion.puresuffering.info3").append("\n");
		MutableComponent rarityComponent = Component.translatable("invasion.puresuffering.info4").append(this.rarity + ", ");
		MutableComponent tierComponent = Component.translatable("invasion.puresuffering.info5").append(this.tier + ", ");
		MutableComponent mobCapComponent = Component.translatable("invasion.puresuffering.info6").append(this.mobCap + "");
		return primaryComponent.append(severityComponent).append(infoComponent).append(rarityComponent).append(tierComponent).append(mobCapComponent);
	}
	
	public static MutableComponent create(String keyIn, InvasionList invasionListIn) {
		MutableComponent component = Component.translatable(keyIn);
		for (Invasion invasion : invasionListIn) {
			if (!component.getSiblings().isEmpty())
				component.append(", ");
			MutableComponent component1 = ComponentUtils.mergeStyles(invasion.getType().getComponent().copy(), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(new InvasionText(invasion).getHoverText().withStyle(ChatFormatting.DARK_GRAY));
			MutableComponent component2 = invasion.getType().getComponent().copy().withStyle(style -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1));
			});
			component.append(component2);
		}
		return component;
	}
}
