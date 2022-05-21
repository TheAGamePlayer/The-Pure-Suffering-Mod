package dev.theagameplayer.puresuffering.util.text;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
		MutableComponent primaryComponent = new TranslatableComponent("invasion.puresuffering.info1").append(this.isPrimary + ", ");
		MutableComponent severityComponent = new TranslatableComponent("invasion.puresuffering.info2").append(this.severity + "\n");
		MutableComponent infoComponent = new TranslatableComponent("invasion.puresuffering.info3").append("\n");
		MutableComponent rarityComponent = new TranslatableComponent("invasion.puresuffering.info4").append(this.rarity + ", ");
		MutableComponent tierComponent = new TranslatableComponent("invasion.puresuffering.info5").append(this.tier + ", ");
		MutableComponent mobCapComponent = new TranslatableComponent("invasion.puresuffering.info6").append(this.mobCap + "");
		return primaryComponent.append(severityComponent).append(infoComponent).append(rarityComponent).append(tierComponent).append(mobCapComponent);
	}
}
