package minicraft.util;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

public final class Logging {
	public static boolean logTime = false;
	public static boolean logThread = false;
	/**
	 * Applied only when debug mode is enabled
	 */
	public static boolean logTrace = false;
	public static boolean logLevel = false;
	public static boolean fileLogFull = false;

	// (These) TaggedLogger would be more preferred (than directly Logger.(?:trace|debug|info|warn|error)).
	public static final TaggedLogger UNTAGGED = Logger.tags();
	public static final TaggedLogger GAMEHANDLER = Logger.tag("Game Handler");
	public static final TaggedLogger CRASHHANDLER = Logger.tag("Crash Handler");
	public static final TaggedLogger WORLD = Logger.tag("World");
	public static final TaggedLogger RESOURCEHANDLER = Logger.tag("Resource Handler");
	public static final TaggedLogger RESOURCEHANDLER_LOCALIZATION = Logger.tag("Resource Handler/Localization");
	public static final TaggedLogger RESOURCEHANDLER_SOUND = Logger.tag("Resource Handler/Sound");
	public static final TaggedLogger RESOURCEHANDLER_SKIN = Logger.tag("Resource Handler/Skin");
	public static final TaggedLogger RESOURCEHANDLER_RESOURCEPACK = Logger.tag("Resource Handler/Resource Pack");
	public static final TaggedLogger SAVELOAD = Logger.tag("SaveLoad");
	public static final TaggedLogger ACHIEVEMENT = Logger.tag("Achievement");
	public static final TaggedLogger ENTITY = Logger.tag("Entity");
	public static final TaggedLogger INVENTORY = Logger.tag("Inventory");
	public static final TaggedLogger ITEMS = Logger.tag("Items");
	public static final TaggedLogger TILES = Logger.tag("Tiles");
	public static final TaggedLogger NETWORK = Logger.tag("Network");
	public static final TaggedLogger QUEST = Logger.tag("Quest");
	public static final TaggedLogger TUTORIAL = Logger.tag("Tutorial");
	public static final TaggedLogger SPRITE = Logger.tag("Sprite");
	public static final TaggedLogger CONTROLLER = Logger.tag("Controller");
	public static final TaggedLogger PLAYER = Logger.tag("Player");

	/**
	 * This is defined dynamically.
	 */
	public static TaggedLogger WORLDNAMED = Logger.tag(null);
}
