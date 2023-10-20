package dev.theagameplayer.puresuffering.client.resources.sounds;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.mojang.blaze3d.audio.OggAudioStream;

import dev.theagameplayer.puresuffering.client.sounds.InvasionMusicManager;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class PSMusicSoundInstance extends SimpleSoundInstance { //For MusicManager
	public PSMusicSoundInstance(final SoundEvent soundEventIn) {
		super(soundEventIn.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
	}
	
	public PSMusicSoundInstance(final SoundEvent soundEventIn, final SoundSource sourceIn, final float volumeIn, final float pitchIn, final RandomSource randomIn, final double xIn, final double yIn, final double zIn) {
		super(soundEventIn, sourceIn, volumeIn, pitchIn, randomIn, xIn, yIn, zIn);
	}

	@Override
	public final CompletableFuture<AudioStream> getStream(final SoundBufferLibrary soundBuffersIn, final Sound soundIn, final boolean loopingIn) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return new OggAudioStream(Files.newInputStream(InvasionMusicManager.getMusic(this.location)));
			} catch (final IOException exceptionIn) {
				throw new CompletionException(exceptionIn);
			}
		}, Util.backgroundExecutor());
	}
}
