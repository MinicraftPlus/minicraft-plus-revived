package minicraft.core.io;

import minicraft.core.CrashHandler;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
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

	private final DataLine.Info info;
	private final short[] raw;
	private final AudioFormat format;
	private final long length;
	private final int internalBufferSize;
	private final SourceDataLine dataLine;
	private final LinkedList<AudioPointer> pointers = new LinkedList<>();

	private class AudioPointer {
		private int offset = 0;

		public Optional<Short> getData() {
			if (offset == raw.length) return Optional.empty();
			return Optional.of(raw[offset++]);
		}
	}

	private Sound(DataLine.Info info, short[] raw, AudioFormat format, long length) throws LineUnavailableException {
		this.info = info;
		this.raw = raw;
		this.format = format;
		this.length = length;
		dataLine = AudioSystem.getSourceDataLine(format);
		dataLine.open();
//		internalBufferSize = format.getFrameSize() * Math.max(format.getFrameSize() * 32,
//			((int) (format.getFrameRate() / 2)) * format.getFrameSize()); // From SoftMixingSourceDataLine
		internalBufferSize = ((int) (format.getFrameRate() / 2)) * format.getFrameSize();
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
			while ((length = in.read(buf)) != -1) {
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
			} else if (format.getChannels() != 2) {
				Logging.RESOURCEHANDLER_SOUND.error(
					"Audio source \"{}\" in pack \"{}\" is neither mono nor stereo, which is not supported.",
					key, pack);
				return;
			} else {
				raw1 = raw0;
			}

			sounds.put(key, new Sound(info, raw1, new AudioFormat(format.getEncoding(), format.getSampleRate(),
				format.getSampleSizeInBits(), 2, format.getFrameSize() * 2 / format.getChannels(), format.getFrameRate(),
				true, format.properties()), fileFormat.getFrameLength()));
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
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
		sounds.forEach((k, v) -> v.tick0());
	}

	private void tick0() {
		dataLine.start();
		// internalBufferSize - dataLine.available() == used buffer
		// Proceed data and then buffer into the data line.
		// For 2/16/44100, 2940 bytes would be proceeded per tick.
		if (internalBufferSize - dataLine.available() > 4096) return;
		int available = Math.min(dataLine.available(), 4096) / 2; // in 16bit (short)
		if (available <= 0) return; // Skips tick if buffer is large causing latency
		byte[] buf = new byte[available * 2];
		short[] bufShort = new short[available];
		while (available > 0) {
			int mixed = 0;
			int c = 0;
			for (Iterator<AudioPointer> iterator = pointers.iterator(); iterator.hasNext(); ) {
				AudioPointer pointer = iterator.next();
				Optional<Short> d = pointer.getData();
				if (!d.isPresent()) iterator.remove();
				else {
					mixed += d.get();
					c++;
				}
			}

			if (c == 0) break; // No more data to be written
			bufShort[bufShort.length - available] = (short) (mixed / c); // Average mixed
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
