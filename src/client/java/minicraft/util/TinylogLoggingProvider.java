package minicraft.util;

import minicraft.util.TinylogLoggingConfiguration.WriterConfig;
import org.tinylog.Level;
import org.tinylog.core.ConfigurationParser;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.core.TinylogContextProvider;
import org.tinylog.core.WritingThread;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.ContextProvider;
import org.tinylog.provider.InternalLogger;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.runtime.RuntimeProvider;
import org.tinylog.writers.ConsoleWriter;
import org.tinylog.writers.FileWriter;
import org.tinylog.writers.Writer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Originally copied from {@link org.tinylog.core.TinylogLoggingProvider}
 */
public class TinylogLoggingProvider implements LoggingProvider {

	private final TinylogContextProvider context;
	private final WritingThread writingThread;
	private final HashMap<Writer, WriterConfig> writers;
	private final EnumSet<LogEntryValue> requiredLogEntryValues;
	private final HashMap<Writer, Boolean> fullStackTraceRequired;
	private final HashMap<String, ConsoleWriter> consoleWriters;
	private final HashMap<String, FileWriter> fileWriters; // Excluding the localization logger
	private final ArrayList<Writer> otherWriters;

	private ConsoleWriter currentConsoleWriter;
	private FileWriter currentFileWriter;

	/**
	 *
	 */
	public TinylogLoggingProvider() {
		TinylogLoggingConfiguration config = new TinylogLoggingConfiguration();
		context = new TinylogContextProvider();

		writers = config.createWriters();
		requiredLogEntryValues = EnumSet.noneOf(LogEntryValue.class);
		fullStackTraceRequired = new HashMap<>();
		consoleWriters = new HashMap<>();
		fileWriters = new HashMap<>();
		otherWriters = new ArrayList<>();
		for (Writer writer : writers.keySet()) {
			if (writer instanceof ConsoleWriter) {
				consoleWriters.put(writers.get(writer).ID, (ConsoleWriter) writer);
			} else if (writer instanceof FileWriter && writers.get(writer).ID.startsWith("writer2")) {
				fileWriters.put(writers.get(writer).ID, (FileWriter) writer);
			} else {
				otherWriters.add(writer);
			}

			Collection<LogEntryValue> logEntryValues = writer.getRequiredLogEntryValues();
			requiredLogEntryValues.addAll(logEntryValues);
			fullStackTraceRequired.put(writer, logEntryValues.contains(LogEntryValue.METHOD) || logEntryValues.contains(LogEntryValue.FILE) || logEntryValues.contains(LogEntryValue.LINE));
		}

		currentConsoleWriter = consoleWriters.get("writer1FFF");
		currentFileWriter = fileWriters.get("writer2");

		try {
			@SuppressWarnings("unchecked")
			Constructor<WritingThread> ctr = (Constructor<WritingThread>) Class.forName("org.tinylog.core.WritingThread").getDeclaredConstructor(Collection.class);
			ctr.setAccessible(true);
			writingThread = ctr.newInstance(writers.keySet());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
		         | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		writingThread.start();

		if (ConfigurationParser.isAutoShutdownEnabled()) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					shutdown();
				} catch (InterruptedException ex) {
					InternalLogger.log(Level.ERROR, ex, "Interrupted while waiting for shutdown");
				}
			}));
		}
	}

	/**
	 * Applying the configuration in {@link Logging}
	 */
	public void init() {
		currentConsoleWriter = consoleWriters.get(String.format("writer1%s%s%s", Logging.logTime ? "T" : "F", Logging.logThread ? "T" : "F", Logging.logTrace ? "T" : "F"));
		currentFileWriter = fileWriters.get("writer2" + (Logging.fileLogFull ? "Full" : ""));
	}

	@Override
	public ContextProvider getContextProvider() {
		return context;
	}

	@Override
	public Level getMinimumLevel() {
		return Level.TRACE;
	}

	@Override
	public Level getMinimumLevel(final String tag) {
		return Level.TRACE; // All tags and levels are logged with writer2.
	}

	@Override
	public boolean isEnabled(final int depth, final String tag, final Level level) {
		return true; // Always enabled.
	}

	@Override
	public void log(final int depth, final String tag, final Level level, final Throwable exception, final MessageFormatter formatter,
	                final Object obj, final Object... arguments) {
		StackTraceElement stackTraceElement;
		if (fullStackTraceRequired.get(currentConsoleWriter) || tag.equals("LOC")) {
			stackTraceElement = RuntimeProvider.getCallerStackTraceElement(depth + 1);
		} else {
			stackTraceElement = null;
		}

		if (stackTraceElement == null) {
			stackTraceElement = new StackTraceElement(RuntimeProvider.getCallerClassName(depth + 1), "<unknown>", null, -1);
		}

		output(stackTraceElement, tag, level, exception, formatter, obj, arguments);
	}

	@Override
	public void log(final String loggerClassName, final String tag, final Level level, final Throwable exception,
	                final MessageFormatter formatter, final Object obj, final Object... arguments) {
		StackTraceElement stackTraceElement;
		if (fullStackTraceRequired.get(currentConsoleWriter) || tag.equals("LOC")) {
			stackTraceElement = RuntimeProvider.getCallerStackTraceElement(loggerClassName);
		} else {
			stackTraceElement = null;
		}

		if (stackTraceElement == null) {
			stackTraceElement = new StackTraceElement(RuntimeProvider.getCallerClassName(loggerClassName), "<unknown>", null, -1);
		}

		output(stackTraceElement, tag, level, exception, formatter, obj, arguments);
	}

	@Override
	public void shutdown() throws InterruptedException {
		if (writingThread == null) {
			for (Writer writer : writers.keySet()) {
				try {
					writer.close();
				} catch (Exception ex) {
					InternalLogger.log(Level.ERROR, ex, "Failed to close writer");
				}
			}
		} else {
			writingThread.shutdown();
			writingThread.join();
		}
	}

	/**
	 * Outputs a log entry to all passed writers.
	 */
	private void output(final StackTraceElement stackTraceElement, final String tag,
	                    final Level level, final Throwable exception, final MessageFormatter formatter, final Object obj,
	                    final Object[] arguments) {

		LogEntry logEntry = TinylogLoggingConfiguration.createLogEntry(stackTraceElement, tag, level, exception, formatter,
			obj, arguments, requiredLogEntryValues, context);

		Consumer<Writer> addToThread = writer -> {
			WriterConfig cfg = writers.get(writer);
			if (cfg.levels.contains(level) && cfg.tags.contains(tag))
				writingThread.add(writer, logEntry);
		};

		addToThread.accept(currentConsoleWriter);
		addToThread.accept(currentFileWriter);
		for (Writer writer : otherWriters)
			addToThread.accept(writer);
	}

	/**
	 * Gets all writers of the provider.
	 * @return All writers
	 */
	public Collection<Writer> getWriters() {
		return writers.keySet();
	}

}
