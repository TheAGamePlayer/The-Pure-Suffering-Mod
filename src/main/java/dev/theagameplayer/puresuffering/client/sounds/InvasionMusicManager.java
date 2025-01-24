package dev.theagameplayer.puresuffering.client.sounds;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FastMap;

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
	private static final FastMap<InvasionDifficulty, ArrayList<PSMusicInfo>> PS_MUSIC = new FastMap<>();
	private static PSMusicSoundInstance music;
	private static Channel channel;
	private static int mIndex = -1;

	public static final void addMusic(final SoundManager pSoundManager, final boolean pLog) {
		final Path gamePath = FMLPaths.GAMEDIR.get();
		final Path musicPath = Paths.get(gamePath.toAbsolutePath().toString(), PureSufferingMod.MUSICID);
		try {
			Files.createDirectory(musicPath);
		} catch (final FileAlreadyExistsException exceptionIn) {
			if (pLog) LOGGER.info("Music directory for " + PureSufferingMod.MUSICID + " already exists!");
		} catch (final IOException exceptionIn) {
			LOGGER.error("Failed to create " + PureSufferingMod.MUSICID + " music directory!", exceptionIn);
		}
		for (final InvasionDifficulty difficulty : InvasionDifficulty.values()) {
			final Path hyperPath = Paths.get(musicPath.toAbsolutePath().toString(), difficulty.toString());
			try {
				Files.createDirectory(hyperPath);
			} catch (final FileAlreadyExistsException exceptionIn) {
				if (pLog) LOGGER.info("Music directory for " + PureSufferingMod.MUSICID + "-" + difficulty.toString() + " already exists!");
			} catch (final IOException exceptionIn) {
				LOGGER.error("Failed to create " + PureSufferingMod.MUSICID + "-" + difficulty.toString()  + " music directory!", exceptionIn);
				continue;
			}
			final File[] files = hyperPath.toFile().listFiles();
			final ArrayList<PSMusicInfo> hyperList = new ArrayList<>(files.length);
			for (final File file : files) {
				final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(PureSufferingMod.MUSICID, difficulty.toString() + "/" + file.getName().toLowerCase().replaceAll("\\s+", "_").replaceAll(".ogg", ""));
				final Sound sound = new Sound(location, ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, true, false, 16);
				final WeighedSoundEvents soundEvent = new WeighedSoundEvents(location, "puresuffering.subtitle.music");
				if (!validateSoundOgg(sound, file.getName())) continue;
				soundEvent.addSound(sound);
				hyperList.add(new PSMusicInfo(location, file.toPath(), file.getName().replaceAll(".ogg", "")));
				pSoundManager.registry.put(location, soundEvent);
			}
			if (hyperList.isEmpty()) continue;
			PS_MUSIC.put(difficulty, hyperList);
		}
	}

	private static final boolean validateSoundOgg(final Sound pSound, final String pName) { //From SoundManager
		final ResourceLocation resourceLocation = pSound.getPath();
		if (!pName.endsWith(".ogg")) {
			LOGGER.warn("File {} is not '.ogg', cannot add it to music {}", resourceLocation, pName);
			return false;
		} else {
			return true;
		}
	}

	public static final void reloadMusic() {
		LOGGER.info("Reloading Invasion Music!");
		addMusic(Minecraft.getInstance().getSoundManager(), false);
	}

	public static final Path getMusic(final ResourceLocation pLoc) {
		if (PS_MUSIC.isEmpty()) return null;
		for (final InvasionDifficulty difficulty : InvasionDifficulty.values()) {
			if (PS_MUSIC.get(difficulty) == null) continue;
			for (final PSMusicInfo info : PS_MUSIC.get(difficulty)) {
				if (pLoc.equals(info.id))
					return info.path;
			}
		}
		return null;
	}

	public static final void tickActive(final InvasionDifficulty pDifficulty, final RandomSource pRandom, final long pDayTime) {
		if (PS_MUSIC.isEmpty()) return;
		final Minecraft mc = Minecraft.getInstance();
		if (PS_MUSIC.containsKey(pDifficulty) && mc.options.getSoundSourceVolume(SoundSource.MUSIC) > 0) {
			final SoundManager soundManager = mc.getSoundManager();
			final SoundEngine soundEngine = soundManager.soundEngine;
			for (final SoundInstance sound : soundEngine.instanceBySource.get(SoundSource.MUSIC)) { //Stopping Vanilla music at all costs.
				if (sound.getLocation().getNamespace().equals(PureSufferingMod.MUSICID)) continue;
				soundEngine.stop(sound);
			}
			if (music != null && soundManager.isActive(music)) {
				if (channel == null) return;
				channel.setVolume(Math.min((float)(12000L - pDayTime)/Invasion.HALF_TRANSITION, mc.options.getSoundSourceVolume(SoundSource.MUSIC)));
			} else if (pDayTime > Invasion.HALF_TRANSITION && pDayTime < 12000L - Invasion.HALF_TRANSITION) {
				final ArrayList<PSMusicInfo> hyperList = PS_MUSIC.get(pDifficulty);
				if (mIndex == -1) mIndex = pRandom.nextInt(hyperList.size());
				mIndex %= hyperList.size();
				final PSMusicInfo info = hyperList.get(mIndex);
				music = new PSMusicSoundInstance(SoundEvent.createVariableRangeEvent(info.id));
				++mIndex;
				soundManager.play(music);
				mc.getToasts().addToast(new InvasionMusicToast(info.name, pDifficulty));
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

	public static final boolean isMusic(final PSMusicSoundInstance pSound) {
		return pSound.equals(music);
	}

	public static final void setChannel(final Channel pChannel) {
		channel = pChannel;
	}

	private static final class PSMusicInfo {
		private final ResourceLocation id;
		private final Path path;
		private final String name;

		private PSMusicInfo(final ResourceLocation pId, final Path pPath, final String pName) {
			this.id = pId;
			this.path = pPath;
			this.name = pName;
		}
	}
}
