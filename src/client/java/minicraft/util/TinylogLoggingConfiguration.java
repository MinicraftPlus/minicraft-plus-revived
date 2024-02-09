package minicraft.util;

import minicraft.util.TinylogLoggingConfiguration.WriterConfig.TagList;
import org.tinylog.Level;
import org.tinylog.Supplier;
import org.tinylog.configuration.Configuration;
import org.tinylog.configuration.ServiceLoader;
import org.tinylog.core.ConfigurationParser;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.core.TinylogContextProvider;
import org.tinylog.format.MessageFormatter;
import org.tinylog.runtime.RuntimeProvider;
import org.tinylog.runtime.Timestamp;
import org.tinylog.writers.ConsoleWriter;
import org.tinylog.writers.Writer;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TinylogLoggingConfiguration {
	public static class WriterConfig {
		public final String ID;
		public Set<Level> levels;
		public TagList tags;

		public WriterConfig(String ID, Set<Level> levels, TagList tags) {
			this.ID = ID;
			this.levels = levels;
			this.tags = tags;
		}

		public static class TagList {
			private Set<String> tags = null;
			private final boolean all;

			public TagList(boolean all) {
				this.all = all;
			}

			public TagList(Set<String> tags) {
				this.tags = tags;
				all = false;
			}

			public boolean contains(String tag) {
				if (all) {
					return true;
				} else if (tags != null) {
					return tags.contains(tag);
				} else {
					return false;
				}
			}
		}
	}

	public HashMap<Writer, WriterConfig> createWriters() {
		if (RuntimeProvider.getProcessId() == Long.MIN_VALUE) {
			java.util.ServiceLoader.load(Writer.class); // Workaround for ProGuard (see issue #126)
		}

		HashMap<Writer, WriterConfig> writers = new HashMap<>(generateConsoleWriters());

		ServiceLoader<Writer> loader = new ServiceLoader<Writer>(Writer.class, Map.class);

		Map<String, String> writerProperties = Configuration.getSiblings("writer"); // Assert they are all defined.

		for (Entry<String, String> entry : writerProperties.entrySet()) {
			Map<String, String> configuration = Configuration.getChildren(entry.getKey());
			String tag = configuration.get("tag");
			Level level = ConfigurationParser.parse(configuration.get("level"), Level.TRACE);

			String exception = Configuration.get("exception");
			if (exception != null && !configuration.containsKey("exception")) {
				configuration.put("exception", exception);
			}

			configuration.put("ID", entry.getKey());
			configuration.put("writingthread", Boolean.toString(true));

			Writer writer = loader.create(entry.getValue(), configuration);
			if (writer != null) {
				Set<Level> levels = computeLevelsFromMinimum(level);
				TagList tagList;
				if (tag == null || tag.isEmpty()) {
					tagList = new TagList(true);
				} else if (tag.equals("-")) {
					tagList = new TagList(new HashSet<>());
				} else {
					HashSet<String> tags = new HashSet<>();
					String[] tagArray = tag.split(",");
					for (String tagArrayItem : tagArray) {
						tagArrayItem = tagArrayItem.trim();
						tags.add(tagArrayItem);
					}

					tagList = new TagList(tags);
				}

				writers.put(writer, new WriterConfig(entry.getKey(), levels, tagList));
			}
		}

		return writers;
	}

	public Set<Level> computeLevelsFromMinimum(Level minimum) {
		Set<Level> levels = EnumSet.noneOf(Level.class);
		for (Level l : Level.values()) {
			if (l != Level.OFF && l.ordinal() >= minimum.ordinal()) {
				levels.add(l);
			}
		}

		return levels;
	}

	/**
	 * Generate all possible instances of console writer (writer1) with the configuration.
	 */
	public HashMap<ConsoleWriter, WriterConfig> generateConsoleWriters() {
		HashMap<ConsoleWriter, WriterConfig> map = new HashMap<>();
		for (final boolean i : new boolean[]{false, true}) {
			for (final boolean j : new boolean[]{false, true}) {
				for (final boolean k : new boolean[]{false, true}) {
					map.putAll(createConsoleWriter(i, j, k));
				}
			}
		}

		return map;
	}

	/**
	 * Generate a console writer with the configuration.
	 */
	public Map<ConsoleWriter, WriterConfig> createConsoleWriter(boolean logTime, boolean logThread, boolean logTrace) {
		HashMap<String, String> properties = new HashMap<>();
		String ID = String.format("writer1%s%s%s", logTime ? "T" : "F", logThread ? "T" : "F", logTrace ? "T" : "F");

		properties.put("level", logTrace ? "trace" : "debug");
		Level level = ConfigurationParser.parse(properties.get("level"), Level.TRACE);

		properties.put("format", String.format("%s%s[{tag}] {level}: {message}", logTime ? "{date: HH:mm:ss.SSS} " : "", logThread ? "[{thread-id}/{thread}] " : ""));

		properties.put("ID", ID);
		properties.put("writingthread", Boolean.toString(true));

		return Collections.singletonMap(new ConsoleWriter(properties), new WriterConfig(ID, computeLevelsFromMinimum(level), new TagList(true)));
	}

	/**
	 * Creates a new log entry.
	 *
	 * @param stackTraceElement Optional stack trace element of caller
	 * @param tag               Tag name if issued from a tagged logger
	 * @param level             Severity level
	 * @param exception         Caught exception or throwable to log
	 * @param formatter         Formatter for text message
	 * @param obj               Message to log
	 * @param arguments         Arguments for message
	 * @param required          The required log entry value array slice of the tag index of the used tag
	 * @param contextProvider   The context provider
	 * @return Filled log entry
	 */
	public static LogEntry createLogEntry(final StackTraceElement stackTraceElement, final String tag,
										  final Level level, final Throwable exception, final MessageFormatter formatter, final Object obj,
										  final Object[] arguments, final Collection<LogEntryValue> required,
										  final TinylogContextProvider contextProvider) {

		Timestamp timestamp = RuntimeProvider.createTimestamp();
		Thread thread = required.contains(LogEntryValue.THREAD) ? Thread.currentThread() : null;
		Map<String, String> context = required.contains(LogEntryValue.CONTEXT) ? contextProvider.getMapping() : null;

		String className;
		String methodName;
		String fileName;
		int lineNumber;
		if (stackTraceElement == null) {
			className = null;
			methodName = null;
			fileName = null;
			lineNumber = -1;
		} else {
			className = stackTraceElement.getClassName();
			methodName = stackTraceElement.getMethodName();
			fileName = stackTraceElement.getFileName();
			lineNumber = stackTraceElement.getLineNumber();
		}

		String message;
		if (arguments == null || arguments.length == 0) {
			Object evaluatedObject = obj instanceof Supplier<?> ? ((Supplier<?>) obj).get() : obj;
			message = evaluatedObject == null ? null : evaluatedObject.toString();
		} else {
			message = formatter.format((String) obj, arguments);
		}

		return new LogEntry(timestamp, thread, context, className, methodName, fileName, lineNumber, tag, level, message, exception);
	}
}
