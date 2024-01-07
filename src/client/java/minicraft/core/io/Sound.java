package minicraft.core.io;

import minicraft.core.CrashHandler;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Sound {
	// Creates sounds from their respective files
	private static final HashMap<String, Sound> sounds = new HashMap<>();

	private Clip clip; // Creates a audio clip to be played

	private Sound(Clip clip) {
		this.clip = clip;
	}

	public static void resetSounds() {
		sounds.clear();
	}

	public static void loadSound(String key, InputStream in, String pack) {
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, AudioSystem.getAudioFileFormat(in).getFormat());

			if (!AudioSystem.isLineSupported(info)) {
				Logging.RESOURCEHANDLER_SOUND.error("ERROR: Audio format of file \"{}\" in pack \"\" is not supported: {}", key, pack, AudioSystem.getAudioFileFormat(in));

				Logging.RESOURCEHANDLER_SOUND.error("Supported audio formats:");
				Logging.RESOURCEHANDLER_SOUND.error("-source:");
				Line.Info[] sinfo = AudioSystem.getSourceLineInfo(info);
				Line.Info[] tinfo = AudioSystem.getTargetLineInfo(info);
				for (Line.Info value : sinfo) {
					if (value instanceof DataLine.Info) {
						DataLine.Info dataLineInfo = (DataLine.Info) value;
						AudioFormat[] supportedFormats = dataLineInfo.getFormats();
						for (AudioFormat af : supportedFormats)
							Logging.RESOURCEHANDLER_SOUND.error(af);
					}
				}
				Logging.RESOURCEHANDLER_SOUND.error("-target:");
				for (int i = 0; i < tinfo.length; i++) {
					if (tinfo[i] instanceof DataLine.Info) {
						DataLine.Info dataLineInfo = (DataLine.Info) tinfo[i];
						AudioFormat[] supportedFormats = dataLineInfo.getFormats();
						for (AudioFormat af : supportedFormats)
							Logging.RESOURCEHANDLER_SOUND.error(af);
					}
				}

				return;
			}

			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(AudioSystem.getAudioInputStream(in));

			clip.addLineListener(e -> {
				if (e.getType() == LineEvent.Type.STOP) {
					clip.flush();
					clip.setFramePosition(0);
				}
			});

			sounds.put(key, new Sound(clip));

		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Audio Could not Load", CrashHandler.ErrorInfo.ErrorType.REPORT,
				String.format("Could not load audio: %s in pack: %s", key, pack)));
		}
	}

	/**
	 * Recommend {@link #play(String)} and {@link #loop(String, boolean)}.
	 */
	@Nullable
	public static Sound getSound(String key) {
		return sounds.get(key);
	}

	/**
	 * This method does safe check for {@link #play()}.
	 */
	public static void play(String key) {
		Sound sound = sounds.get(key);
		if (sound != null) sound.play();
	}

	/**
	 * This method does safe check for {@link #loop(boolean)}.
	 */
	public static void loop(String key, boolean start) {
		Sound sound = sounds.get(key);
		if (sound != null) sound.loop(start);
	}

	public void play() {
		if (!(boolean) Settings.get("sound") || clip == null) return;

		if (clip.isRunning() || clip.isActive())
			clip.stop();

		clip.start();
	}

	public void loop(boolean start) {
		if (!(boolean) Settings.get("sound") || clip == null) return;

		if (start)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		else
			clip.stop();
	}
}
