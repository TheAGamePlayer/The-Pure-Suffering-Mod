package dev.theagameplayer.puresuffering.client.sounds;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.audio.Channel;

import dev.theagameplayer.puresuffering.PureSufferingMod;
import dev.theagameplayer.puresuffering.client.gui.components.toasts.InvasionMusicToast;
import dev.theagameplayer.puresuffering.client.resources.sounds.PSMusicSoundInstance;
import dev.theagameplayer.puresuffering.invasion.InvasionDifficulty;
import dev.theagameplayer.puresuffering.invasion.Invasion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.neoforged.fml.loading.FMLPaths;

public final class InvasionMusicManager {
	private static final Logger LOGGER = PureSufferingMod.LOGGER;
	private static final HashMap<InvasionDifficulty, ArrayList<PSMusicInfo>> PS_MUSIC = new HashMap<>();
	private static PSMusicSoundInstance music;
	private static Channel channel;
	private static int mIndex = -1;

	public static final void addMusic(final boolean logIn) {
		final Minecraft mc = Minecraft.getInstance();
		final SoundManager soundManager = mc.getSoundManager();
		final Path gamePath = FMLPaths.GAMEDIR.get();
		final Path musicPath = Paths.get(gamePath.toAbsolutePath().toString(), PureSufferingMod.MUSICID);
		try {
			Files.createDirectory(musicPath);
		} catch (final FileAlreadyExistsException exceptionIn) {
			if (logIn) LOGGER.info("Music directory for " + PureSufferingMod.MUSICID + " already exists!");
		} catch (final IOException exceptionIn) {
			LOGGER.error("Failed to create " + PureSufferingMod.MUSICID + " music directory!", exceptionIn);
		}
		for (final InvasionDifficulty difficulty : InvasionDifficulty.values()) {
			final Path hyperPath = Paths.get(musicPath.toAbsolutePath().toString(), difficulty.toString());
			try {
				Files.createDirectory(hyperPath);
			} catch (final FileAlreadyExistsException exceptionIn) {
				if (logIn) LOGGER.info("Music directory for " + PureSufferingMod.MUSICID + "-" + difficulty.toString() + " already exists!");
			} catch (final IOException exceptionIn) {
				LOGGER.error("Failed to create " + PureSufferingMod.MUSICID + "-" + difficulty.toString()  + " music directory!", exceptionIn);
				continue;
			}
			final ArrayList<PSMusicInfo> hyperList = new ArrayList<>();
			for (final File file : new File(hyperPath.toString()).listFiles()) {
				final ResourceLocation location = new ResourceLocation(PureSufferingMod.MUSICID, difficulty.toString() + "/" + file.getName().toLowerCase().replaceAll("\\s+", "_").replaceAll(".ogg", ""));
				final Sound sound = new Sound(location.toString(), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, true, false, 16);
				final WeighedSoundEvents soundEvent = new WeighedSoundEvents(location, "puresuffering.subtitle.music");
				if (!validateSoundOgg(sound, file.getName())) continue;
				soundEvent.addSound(sound);
				hyperList.add(new PSMusicInfo(location, file.toPath(), file.getName().replaceAll(".ogg", "")));
				soundManager.registry.put(location, soundEvent);
			}
			if (!hyperList.isEmpty())
				PS_MUSIC.put(difficulty, hyperList);
		}
	}

	private static final boolean validateSoundOgg(final Sound soundIn, final String nameIn) { //From SoundManager
		final ResourceLocation resourceLocation = soundIn.getPath();
		if (!nameIn.endsWith(".ogg")) {
			LOGGER.warn("File {} is not '.ogg', cannot add it to music {}", resourceLocation, nameIn);
			return false;
		} else {
			return true;
		}
	}

	public static final void reloadMusic() {
		LOGGER.info("Reloading Invasion Music!");
		addMusic(false);
	}

	public static final Path getMusic(final ResourceLocation locIn) {
		if (PS_MUSIC.isEmpty()) return null;
		for (final InvasionDifficulty difficulty : InvasionDifficulty.values()) {
			if (PS_MUSIC.get(difficulty) == null) continue;
			for (final PSMusicInfo info : PS_MUSIC.get(difficulty)) {
				if (locIn.equals(info.id))
					return info.path;
			}
		}
		return null;
	}

	public static final void tickActive(final InvasionDifficulty difficultyIn, final RandomSource randomIn, final long dayTimeIn) {
		if (PS_MUSIC.isEmpty()) return;
		final Minecraft mc = Minecraft.getInstance();
		if (PS_MUSIC.containsKey(difficultyIn) && mc.options.getSoundSourceVolume(SoundSource.MUSIC) > 0) {
			final SoundManager soundManager = mc.getSoundManager();
			final SoundEngine soundEngine = soundManager.soundEngine;
			for (final SoundInstance sound : soundEngine.instanceBySource.get(SoundSource.MUSIC)) { //Stopping Vanilla music at all costs.
				if (sound.getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) continue;
				soundEngine.stop(sound);
			}
			if (music != null && soundManager.isActive(music)) {
				if (channel == null) return;
				channel.setVolume(Math.min((float)(12000L - dayTimeIn)/Invasion.HALF_TRANSITION, 1.0F));
			} else if (dayTimeIn > Invasion.HALF_TRANSITION && dayTimeIn < 12000L - Invasion.HALF_TRANSITION) {
				final ArrayList<PSMusicInfo> hyperList = PS_MUSIC.get(difficultyIn);
				if (mIndex == -1) mIndex = randomIn.nextInt(hyperList.size());
				mIndex %= hyperList.size();
				final PSMusicInfo info = hyperList.get(mIndex);
				music = new PSMusicSoundInstance(SoundEvent.createVariableRangeEvent(info.id));
				mIndex++;
				soundManager.play(music);
				mc.getToasts().addToast(new InvasionMusicToast(info.name, difficultyIn));
			}
		} else {
			tickInactive();
		}
	}
	
	public static final void tickInactive() {
		if (!playingMusic()) return;
		final Minecraft mc = Minecraft.getInstance();
		mc.getSoundManager().stop(music);
		music = null;
		channel = null;
	}

	public static final boolean playingMusic() {
		return music != null && Minecraft.getInstance().getSoundManager().isActive(music);
	}
	
	public static final boolean isMusic(final PSMusicSoundInstance soundIn) {
		return soundIn.equals(music);
	}
	
	public static final void setChannel(final Channel channelIn) {
		channel = channelIn;
	}

	private static final class PSMusicInfo {
		private final ResourceLocation id;
		private final Path path;
		private final String name;

		private PSMusicInfo(final ResourceLocation idIn, final Path pathIn, final String nameIn) {
			this.id = idIn;
			this.path = pathIn;
			this.name = nameIn;
		}
	}
}
