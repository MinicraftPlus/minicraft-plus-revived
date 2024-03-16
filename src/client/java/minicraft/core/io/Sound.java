package minicraft.core.io;

import minicraft.core.CrashHandler;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.applet.AudioClip;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Sound {
	// Creates sounds from their respective files
	private static final HashMap<String, Sound> sounds = new HashMap<>();

	private final DataLine.Info info;
	private final byte[] raw;
	private final AudioFormat format;
	private final long length;

	private Sound(DataLine.Info info, byte[] raw, AudioFormat format, long length) {
		this.info = info;
		this.raw = raw;
		this.format = format;
		this.length = length;
	}

	public static void resetSounds() {
		sounds.clear();
	}

	public static void loadSound(String key, InputStream in, String pack) {
		try {
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(in);
			AudioFormat format = fileFormat.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);

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

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int length;
			while ((length = in.read(buf)) != -1) {
				out.write(buf, 0, length);
			}
			sounds.put(key, new Sound(info, out.toByteArray(), format, fileFormat.getFrameLength()));

		} catch (UnsupportedAudioFileException | IOException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Audio Could not Load", CrashHandler.ErrorInfo.ErrorType.REPORT,
				String.format("Could not load audio: %s in pack: %s", key, pack)));
		}
	}

	/**
	 * Recommend {@link #play(String)} and {@link #loop(String, int)}.
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
	 * This method does safe check for {@link #loop(int)}.
	 */
	public static void loop(String key, int count) {
		Sound sound = sounds.get(key);
		if (sound != null) sound.loop(count);
	}

	@Nullable
	private Clip createClip() {
		try {
			Clip clip = (Clip) AudioSystem.getLine(info); // Creates an audio clip to be played
			clip.open(new AudioInputStream(new ByteArrayInputStream(raw), format, length));
			clip.addLineListener(e -> {
				if (e.getType() == LineEvent.Type.STOP) {
					clip.flush();
					clip.close();
				}
			});

			return clip;
		} catch (LineUnavailableException | IOException e) {
			Logging.RESOURCEHANDLER_SOUND.error(e, "Unable to create Clip");
			return null;
		}
	}

	public void play() {
		if (!(boolean) Settings.get("sound")) return;

		Clip clip = createClip();
		if (clip != null) {
			clip.start();
		}
	}

	public void loop(int count) {
		if (!(boolean) Settings.get("sound")) return;

		Clip clip = createClip();
		if (clip != null) {
			clip.loop(count);
		}
	}
}
