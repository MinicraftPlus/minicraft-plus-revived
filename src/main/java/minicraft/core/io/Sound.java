package minicraft.core.io;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import minicraft.core.CrashHandler;
import minicraft.util.Logging;

public class Sound {
	// Creates sounds from their respective files
	public static final Sound playerHurt = Objects.requireNonNull(loadLocalSound("/resources/sound/playerhurt.wav"));
	public static final Sound playerDeath = Objects.requireNonNull(loadLocalSound("/resources/sound/death.wav"));
	public static final Sound monsterHurt = Objects.requireNonNull(loadLocalSound("/resources/sound/monsterhurt.wav"));
	public static final Sound bossDeath = Objects.requireNonNull(loadLocalSound("/resources/sound/bossdeath.wav"));
	public static final Sound fuse = Objects.requireNonNull(loadLocalSound("/resources/sound/fuse.wav"));
	public static final Sound explode = Objects.requireNonNull(loadLocalSound("/resources/sound/explode.wav"));
	public static final Sound pickup = Objects.requireNonNull(loadLocalSound("/resources/sound/pickup.wav"));
	public static final Sound craft = Objects.requireNonNull(loadLocalSound("/resources/sound/craft.wav"));
	public static final Sound back = Objects.requireNonNull(loadLocalSound("/resources/sound/craft.wav"));
	public static final Sound place = Objects.requireNonNull(loadLocalSound("/resources/sound/craft.wav"));
	public static final Sound select = Objects.requireNonNull(loadLocalSound("/resources/sound/select.wav"));
	public static final Sound confirm = Objects.requireNonNull(loadLocalSound("/resources/sound/confirm.wav"));

	private Clip clip; // Creates a audio clip to be played

	public static void init() {} // A way to initialize the class without actually doing anything

	private Sound(Clip clip) {
		this.clip = clip;
	}

	private static Sound loadLocalSound(String name) {
		URL url = Sound.class.getResource(name);
		return loadSound(url);
	}
	public static Sound loadExternalSound(URL url, String defaultName) {
		Sound sound = loadSound(url);
		if (sound == null) {
			return loadLocalSound(defaultName);
		}

		return sound;
	}

	private static Sound loadSound(URL url) {
		try {
			DataLine.Info info = new DataLine.Info(Clip.class, AudioSystem.getAudioFileFormat(url).getFormat());

			if (!AudioSystem.isLineSupported(info)) {
				Logging.RESOURCEHANDLER_SOUND.error("ERROR: Audio format of file \"" + url + "\" is not supported: " + AudioSystem.getAudioFileFormat(url));

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
				for (int i = 0; i < tinfo.length; i++)
				{
					if (tinfo[i] instanceof DataLine.Info)
					{
						DataLine.Info dataLineInfo = (DataLine.Info) tinfo[i];
						AudioFormat[] supportedFormats = dataLineInfo.getFormats();
						for (AudioFormat af: supportedFormats)
							Logging.RESOURCEHANDLER_SOUND.error(af);
					}
				}

				return null;
			}

			Clip clip = (Clip)AudioSystem.getLine(info);
			clip.open(AudioSystem.getAudioInputStream(url));

			clip.addLineListener(e -> {
				if (e.getType() == LineEvent.Type.STOP) {
					clip.flush();
					clip.setFramePosition(0);
				}
			});

			return new Sound(clip);

		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Sound file Could not Load", CrashHandler.ErrorInfo.ErrorType.REPORT, "Could not load sound file " + url));
			return null;
		}
	}

	public void play() {
		if (!(boolean)Settings.get("sound") || clip == null) return;

		if (clip.isRunning() || clip.isActive())
			clip.stop();

		clip.start();
	}

	public void loop(boolean start) {
		if (!(boolean)Settings.get("sound") || clip == null) return;

		if (start)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		else
			clip.stop();
	}
}
