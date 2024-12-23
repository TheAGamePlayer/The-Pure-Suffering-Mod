package dev.theagameplayer.puresuffering.client.gui.components.toasts;

import java.util.List;

import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;

public final class InvasionMusicToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
	private static final MutableComponent MUSIC_TEXT = Component.translatable("puresuffering.toast.music");
	private final ItemStack recordItem;
	private final MutableComponent name;
	private final int color1, color2;

	public InvasionMusicToast(final String pName, final InvasionDifficulty pDifficulty) {
		final Minecraft mc = Minecraft.getInstance();
		final Item[] records = mc.level.registryAccess().registryOrThrow(Registries.ITEM).stream().filter(item -> {
			return JukeboxSong.fromStack(mc.level.registryAccess(), item.getDefaultInstance()).isPresent();
		}).toArray(Item[]::new);
		this.recordItem = new ItemStack(records[mc.level.random.nextInt(records.length)]);
		this.name = Component.literal(pName);
		this.color1 = pDifficulty.getColor(true);
		this.color2 = pDifficulty.getColor(false);
	}

	public final Visibility render(final GuiGraphics pGuiGraphics, final ToastComponent pToastComponent, final long pTimeSinceLastVisible) {
		final Minecraft mc = pToastComponent.getMinecraft();
		pGuiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		if (pTimeSinceLastVisible < 1500L) {
			final int i = Mth.floor(Mth.clamp((float)(1500L - pTimeSinceLastVisible) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
			pGuiGraphics.drawString(mc.font, MUSIC_TEXT, 30, 11, this.color1 | i, false);
		} else {
			final List<FormattedCharSequence> list = mc.font.split(this.name, 125);
			final int i = Mth.floor(Mth.clamp((float)(pTimeSinceLastVisible - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
            int l = this.height()/2 - list.size() * 9/2;
            for (final FormattedCharSequence formattedCharSequence : list) {
            	pGuiGraphics.drawString(mc.font, formattedCharSequence, 30, l, this.color2 | i, false);
            	l += 9;
            }
		}
		pGuiGraphics.renderFakeItem(this.recordItem, 8, 8);
		return (double)pTimeSinceLastVisible >= 5000.0D * pToastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}
}
