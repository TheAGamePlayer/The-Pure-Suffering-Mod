package dev.theagameplayer.puresuffering.util.text;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.util.InvasionList;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;

public final class InvasionListTextComponent extends TranslationTextComponent {
	public InvasionListTextComponent(String keyIn, InvasionList invasionListIn) {
		super(keyIn);
		for (Invasion invasion : invasionListIn) {
			if (!this.siblings.isEmpty())
				this.append(", ");
			IFormattableTextComponent component = TextComponentUtils.mergeStyles(invasion.getType().getComponent().copy(), Style.EMPTY.withColor(TextFormatting.GRAY)).append("\n").append(new InvasionText(invasion).getHoverText().withStyle(TextFormatting.DARK_GRAY));
			IFormattableTextComponent component1 = invasion.getType().getComponent().copy().withStyle(style -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component));
			});
			this.append(component1);
		}
	}
}
