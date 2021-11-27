package dev.theagameplayer.puresuffering.util.text;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	
	public IFormattableTextComponent getHoverText() {
		IFormattableTextComponent primaryComponent = new TranslationTextComponent("invasion.puresuffering.info1").append(this.isPrimary + ", ");
		IFormattableTextComponent severityComponent = new TranslationTextComponent("invasion.puresuffering.info2").append(this.severity + "\n");
		IFormattableTextComponent infoComponent = new TranslationTextComponent("invasion.puresuffering.info3").append("\n");
		IFormattableTextComponent rarityComponent = new TranslationTextComponent("invasion.puresuffering.info4").append(this.rarity + ", ");
		IFormattableTextComponent tierComponent = new TranslationTextComponent("invasion.puresuffering.info5").append(this.tier + ", ");
		IFormattableTextComponent mobCapComponent = new TranslationTextComponent("invasion.puresuffering.info6").append(this.mobCap + "");
		return primaryComponent.append(severityComponent).append(infoComponent).append(rarityComponent).append(tierComponent).append(mobCapComponent);
	}
}
