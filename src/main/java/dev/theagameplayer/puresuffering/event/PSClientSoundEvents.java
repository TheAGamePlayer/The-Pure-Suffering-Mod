package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.invasion.ClientInvasionSession;
import dev.theagameplayer.puresuffering.client.resources.sounds.PSMusicSoundInstance;
import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import dev.theagameplayer.puresuffering.client.sounds.InvasionSoundHandler;
import dev.theagameplayer.puresuffering.config.PSConfigValues;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.registries.PSSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;

public final class PSClientSoundEvents {
	public static final void playSound(final PlaySoundEvent pEvent) {
		final SoundInstance sound = pEvent.getOriginalSound();
		if (sound == null || !sound.canPlaySound()) return;
		if (sound.getSource() == SoundSource.MUSIC && (PSConfigValues.client.disableNonPSMusic || InvasionMusicManager.playingMusic()) && !sound.getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) {
			pEvent.setSound(null);
			return;
		}
		if (sound instanceof PSMusicSoundInstance || !sound.getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) return; //Needed for playSound command
		final Minecraft mc = Minecraft.getInstance();
		pEvent.setSound(new PSMusicSoundInstance(SoundEvent.createVariableRangeEvent(sound.getLocation()), sound.getSource(), sound.getVolume(), sound.getPitch(), mc.level.getRandom(), sound.getX(), sound.getY(), sound.getZ()));
	}

	public static final void playSoundSource(final PlaySoundSourceEvent pEvent) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientInvasionSession session = ClientInvasionSession.get(mc.level);
		if (session != null && session.getDifficulty().isNightmare())
			InvasionSoundHandler.playSound(pEvent.getChannel().source, 6.0F);
	}

	public static final void playStreamingSource(final PlayStreamingSourceEvent pEvent) {
		final String name = pEvent.getName();
		final boolean flag1 = PSSoundEvents.INFORM_INVASION.get().getLocation().getPath().equals(name) || PSSoundEvents.INVASION_AMBIENCE.get().getLocation().getPath().equals(name);
		final boolean flag2 = PSSoundEvents.CANCEL_INVASION.get().getLocation().getPath().equals(name) || InvasionDifficulty.DEFAULT.getStartSound().getLocation().getPath().equals(name) || InvasionDifficulty.HYPER.getStartSound().getLocation().getPath().equals(name) || InvasionDifficulty.NIGHTMARE.getStartSound().getLocation().getPath().equals(name);
		if (flag1 || flag2) InvasionSoundHandler.playSound(pEvent.getChannel().source, flag1 ? 6.0F : 15.0F);
		if (!pEvent.getSound().getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) return;
		if (pEvent.getSound() instanceof PSMusicSoundInstance psSound && InvasionMusicManager.isMusic(psSound)) InvasionMusicManager.setChannel(pEvent.getChannel());
	}
	
	public static final void soundEngineLoad(final SoundEngineLoadEvent pEvent) {
		InvasionMusicManager.addMusic(pEvent.getEngine().soundManager, true);
	}
}
