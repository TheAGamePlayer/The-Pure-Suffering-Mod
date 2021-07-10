package dev.theagameplayer.puresuffering.util.text;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class InvasionText {
	private int amount;
	private int severity;
	
	public InvasionText(int severityIn) {
		this.amount = 0;
		this.severity = severityIn;
	}
	
	public int getAmount() {
		return this.amount;
	}
	
	public void incrementAmount() {
		this.amount++;
	}
	
	public int getSeverity() {
		return this.severity;
	}
	
	public void setSeverity(int severityIn) {
		this.severity = severityIn;
	}
	
	public IFormattableTextComponent getHoverText() {
		IFormattableTextComponent amountComponent = new TranslationTextComponent("invasion.puresuffering.info1").append(this.amount + "\n");
		IFormattableTextComponent severityComponent = new TranslationTextComponent("invasion.puresuffering.info2").append(this.severity + "");
		return amountComponent.append(severityComponent);
	}
}
