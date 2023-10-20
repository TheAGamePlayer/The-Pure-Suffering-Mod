package dev.theagameplayer.puresuffering.client.gui.components.toasts;

import java.util.ArrayList;
import java.util.List;

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

public final class InvasionMusicToast implements Toast {
	private static final MutableComponent MUSIC_TEXT = Component.translatable("puresuffering.toast.music");
	private final ItemStack recordItem;
	private final MutableComponent name;
	private final int color1, color2;

	public InvasionMusicToast(final String nameIn, final InvasionDifficulty difficultyIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ArrayList<RecordItem> records = new ArrayList<>();
		mc.level.registryAccess().registryOrThrow(Registries.ITEM).forEach(item -> {
			if (item instanceof RecordItem recordItem)
				records.add(recordItem);
		});
		this.recordItem = new ItemStack(records.get(mc.level.random.nextInt(records.size())));
		this.name = Component.literal(nameIn);
		this.color1 = difficultyIn.getColor(true);
		this.color2 = difficultyIn.getColor(false);
	}

	@Override
	public final Visibility render(final GuiGraphics graphicsIn, final ToastComponent componentIn, final long ticksIn) {
		final Minecraft mc = componentIn.getMinecraft();
		graphicsIn.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());
		if (ticksIn < 1500L) {
			final int i = Mth.floor(Mth.clamp((float)(1500L - ticksIn) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
			graphicsIn.drawString(mc.font, MUSIC_TEXT, 30, 11, this.color1 | i, false);
		} else {
			final List<FormattedCharSequence> list = mc.font.split(this.name, 125);
			final int i = Mth.floor(Mth.clamp((float)(ticksIn - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
            int l = this.height()/2 - list.size() * 9/2;
            for (final FormattedCharSequence formattedCharSequence : list) {
            	graphicsIn.drawString(mc.font, formattedCharSequence, 30, l, this.color2 | i, false);
            	l += 9;
            }
		}
		graphicsIn.renderFakeItem(this.recordItem, 8, 8);
		return (double)ticksIn >= 5000.0D * componentIn.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}
}
