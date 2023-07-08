package minicraft.util;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

/** Note: Untagged loggers should not be used in best practice. */
public final class Logging {
	public static boolean logTime = false;
	public static boolean logThread = false;
	/** Applied only when debug mode is enabled */
	public static boolean logTrace = false;
	public static boolean logLevel = false;
	public static boolean fileLogFull = false;

	// (These) TaggedLogger would be more preferred (than directly (untagged) Logger.(?:trace|debug|info|warn|error)).
	public static final TaggedLogger GAME_HANDLER = Logger.tag("Game Handler");
	public static final TaggedLogger CRASH_HANDLER = Logger.tag("Crash Handler");
	public static final TaggedLogger WORLD = Logger.tag("World");
	public static final TaggedLogger RESOURCE_HANDLER = Logger.tag("Resource Handler");
	public static final TaggedLogger RESOURCE_HANDLER__LOCALIZATION = Logger.tag("Resource Handler/Localization");
	public static final TaggedLogger RESOURCE_HANDLER__SOUND = Logger.tag("Resource Handler/Sound");
	public static final TaggedLogger RESOURCE_HANDLER__SKIN = Logger.tag("Resource Handler/Skin");
	public static final TaggedLogger RESOURCE_HANDLER__RESOURCEPACK = Logger.tag("Resource Handler/Resource Pack");
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
	public static final TaggedLogger GFX = Logger.tag("GFX");

	/** This is defined dynamically. */
	public static TaggedLogger WORLD_NAMED = Logger.tag(null);
}
