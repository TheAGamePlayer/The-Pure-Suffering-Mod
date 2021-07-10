package dev.theagameplayer.puresuffering.util.text;

import java.util.Map;
import java.util.Map.Entry;

import dev.theagameplayer.puresuffering.invasion.InvasionType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;

public final class InvasionListTextComponent extends TranslationTextComponent {
	public InvasionListTextComponent(String keyIn, Map<InvasionType, InvasionText> invasionMapIn) {
		super(keyIn);
		for (Entry<InvasionType, InvasionText> entry : invasionMapIn.entrySet()) {
			if (!this.siblings.isEmpty())
				this.append(", ");
			IFormattableTextComponent component = TextComponentUtils.mergeStyles(entry.getKey().getComponent().copy(), Style.EMPTY.withColor(TextFormatting.GRAY)).append("\n").append(entry.getValue().getHoverText().withStyle(TextFormatting.DARK_GRAY));
			IFormattableTextComponent component1 = entry.getKey().getComponent().copy().withStyle(style -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component));
			});
			this.append(component1);
		}
	}
}
