package minicraft.core.io;

import minicraft.core.CrashHandler;
import minicraft.util.Logging;
import minicraft.util.MyUtils;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class Sound {
	// Creates sounds from their respective files
	private static final HashMap<String, Sound> sounds = new HashMap<>();
	private static final LinkedList<AudioPointer> pointers = new LinkedList<>();
	private static final AudioFormat STANDARD_FORMAT =
		new AudioFormat(44100, 16, 2, true, true);
	private static final int MAX_BUFFER_SIZE = 4096;
	private static final SourceDataLine dataLine;
	private static final int internalBufferSize;

	/*
	 * Only 2/16/44100 and 1/16/44100 PCM_SIGNED are supported.
	 */

	static {
		try {
			dataLine = AudioSystem.getSourceDataLine(STANDARD_FORMAT);
			dataLine.open();
			// Assume DirectAudioDevice is used
			internalBufferSize = ((int) (STANDARD_FORMAT.getFrameRate() / 2)) * STANDARD_FORMAT.getFrameSize();
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}
	}

	private final short[] raw;

	private class AudioPointer {
		private int offset = 0;

		public Optional<Short> getData() {
			if (offset == raw.length) return Optional.empty();
			return Optional.of(raw[offset++]);
		}
	}

	private Sound(short[] raw) {
		this.raw = raw;
	}

	public static void resetSounds() {
		sounds.clear();
	}

	public static void loadSound(String key, InputStream in, String pack) {
		try {
			AudioInputStream ain = AudioSystem.getAudioInputStream(in);
			AudioFormat format = ain.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);

			if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED ||
				format.getChannels() > 2 || format.getSampleRate() != 44100 ||
				format.getSampleSizeInBits() != 16) {
				Logging.RESOURCEHANDLER_SOUND.error("Unsupported audio format of file \"{}\" in pack \"{}\": {}",
					key, pack, format);
			}

			if (!AudioSystem.isLineSupported(info)) {
				Logging.RESOURCEHANDLER_SOUND.error("ERROR: Audio format of file \"{}\" in pack \"{}\" is not supported: {}", key, pack, AudioSystem.getAudioFileFormat(in));

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
			while ((length = ain.read(buf)) != -1) {
				out.write(buf, 0, length);
			}
			short[] raw0 = new short[out.size()/2];
			ByteBuffer.wrap(out.toByteArray()).order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN)
				.asShortBuffer().get(raw0);
			short[] raw1;
			if (format.getChannels() == 1) {
				raw1 = new short[raw0.length * 2];
				for (int i = 0; i < raw0.length; ++i) {
					raw1[i * 2] = raw0[i];
					raw1[i * 2 + 1] = raw0[i];
				}
			} else if (format.getChannels() != 2) { // This should not be executed.
				Logging.RESOURCEHANDLER_SOUND.error(
					"Audio source \"{}\" in pack \"{}\" is neither mono nor stereo, which is not supported.",
					key, pack);
				return;
			} else {
				raw1 = raw0;
			}

			sounds.put(key, new Sound(raw1));
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

	public static void tick() {
		dataLine.start();
		// internalBufferSize - dataLine.available() == used buffer
		// Proceed data and then buffer into the data line.
		// For 2/16/44100, 2940 bytes would be proceeded per tick.
		if (internalBufferSize - dataLine.available() > MAX_BUFFER_SIZE) return;
		int available = Math.min(dataLine.available(), MAX_BUFFER_SIZE) / 2; // in 16bit (short)
		if (available <= 0) return; // Skips tick if buffer is large causing latency
		byte[] buf = new byte[available * 2];
		short[] bufShort = new short[available];
		while (available > 0) {
			/* Audio Mixing Algorithm
			 * Reference Algorithm: https://stackoverflow.com/a/25102339
			 * And here, converted equations are used: (U_i is an unsigned short value)
			 * Z = U_i, if n = 1
			 * Z = product{U_i} / 32768^{n-1}, if quiet
			 * Z = 2 * sum{U_i} - product{U_i} / 32768^{n-1} - 65536 * (n-1), otherwise
			 */

			boolean quiet = true;
			int n = 0;
			int sum = 0;
			float mul = 1;
			for (Iterator<AudioPointer> iterator = pointers.iterator(); iterator.hasNext(); ) {
				AudioPointer pointer = iterator.next();
				Optional<Short> d = pointer.getData();
				if (!d.isPresent()) iterator.remove();
				else {
					int val = d.get() + 32768; // Turning to unsigned
					if (quiet && val >= 32768) quiet = false;
					mul *= val;
					if (n != 0) mul /= 32768;
					sum += val;
					n++;
				}
			}

			if (n == 0) break; // No more data to be written
			bufShort[bufShort.length - available] = (short) (n == 1 ? sum - 32768 : MyUtils.clamp((int) (quiet ?
				mul : 2 * sum - mul - 65536 * (n - 1)), 0, 65535) - 32768); // Turning back to signed
			available--;
		}

		ByteBuffer.wrap(buf).asShortBuffer().put(bufShort);
		dataLine.write(buf, 0, buf.length);
	}

	public void play() {
		if (!(boolean) Settings.get("sound")) return;
		pointers.add(new AudioPointer());
	}

	/** @deprecated no longer supported, but reserved for future implementation. */
	@Deprecated
	public void loop(int count) {}
}
