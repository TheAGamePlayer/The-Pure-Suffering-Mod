package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.resources.sounds.PSMusicSoundInstance;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.client.sounds.InvasionSoundHandler;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;

public final class PSClientSoundEvents {
	public static final void playSound(final PlaySoundEvent eventIn) {
		final SoundInstance sound = eventIn.getOriginalSound();
		if (sound == null || !sound.canPlaySound()) return;
		if (sound.getSource() == SoundSource.MUSIC && InvasionMusicManager.playingMusic() && !sound.getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) {
			eventIn.setSound(null);
			return;
		}
		if (sound instanceof PSMusicSoundInstance) return;
		if (!sound.getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) return; //Needed for playSound command
		final Minecraft mc = Minecraft.getInstance();
		eventIn.setSound(new PSMusicSoundInstance(SoundEvent.createVariableRangeEvent(sound.getLocation()), sound.getSource(), sound.getVolume(), sound.getPitch(), mc.level.getRandom(), sound.getX(), sound.getY(), sound.getZ()));
	}

	public static final void playSoundSource(final PlaySoundSourceEvent eventIn) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		if (session != null && session.getDifficulty().isNightmare())
			InvasionSoundHandler.playSound(eventIn.getChannel().source, 6.0F);
	}

	public static final void playStreamingSource(final PlayStreamingSourceEvent eventIn) {
		final String name = eventIn.getName();
		final boolean flag1 = PSSoundEvents.INFORM_INVASION.getId().getPath().equals(name) || PSSoundEvents.INVASION_AMBIENCE.getId().getPath().equals(name);
		final boolean flag2 = PSSoundEvents.CANCEL_INVASION.getId().getPath().equals(name) || InvasionDifficulty.DEFAULT.getStartSound().getLocation().getPath().equals(name) || InvasionDifficulty.HYPER.getStartSound().getLocation().getPath().equals(name) || InvasionDifficulty.NIGHTMARE.getStartSound().getLocation().getPath().equals(name);
		if (flag1 || flag2) InvasionSoundHandler.playSound(eventIn.getChannel().source, flag1 ? 6.0F : 15.0F);
		if (!eventIn.getSound().getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) return;
		if (eventIn.getSound() instanceof PSMusicSoundInstance psSound && InvasionMusicManager.isMusic(psSound)) InvasionMusicManager.setChannel(eventIn.getChannel());
	}
}
