package dev.theagameplayer.puresuffering.util.text;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.util.InvasionList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.HoverEvent;

public final class InvasionListTextComponent extends TranslatableComponent {
	public InvasionListTextComponent(String keyIn, InvasionList invasionListIn) {
		super(keyIn);
		for (Invasion invasion : invasionListIn) {
			if (!this.siblings.isEmpty())
				this.append(", ");
			MutableComponent component = ComponentUtils.mergeStyles(invasion.getType().getComponent().copy(), Style.EMPTY.withColor(ChatFormatting.GRAY)).append("\n").append(new InvasionText(invasion).getHoverText().withStyle(ChatFormatting.DARK_GRAY));
			MutableComponent component1 = invasion.getType().getComponent().copy().withStyle(style -> {
				return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component));
			});
			this.append(component1);
		}
	}
}
