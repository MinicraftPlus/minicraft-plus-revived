package minicraft.util;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public final class Logging {
	public static boolean logTime = false;
	public static boolean logThread = false;
	/** Applied only when debug mode is enabled */
	public static boolean logTrace = false;
	public static boolean fileLogFull = false;

	public static final TaggedLogger UNTAGGED = Logger.tags();
	public static final TaggedLogger GAMEHANDLER = Logger.tag("Game Handler");
	public static final TaggedLogger CRASHHANDLER = Logger.tag("Crash Handler");
	public static final TaggedLogger WORLD = Logger.tag("World");
	public static final TaggedLogger RESOURCEHANDLER = Logger.tag("Resource Handler");
}
