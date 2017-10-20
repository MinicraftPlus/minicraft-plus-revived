package minicraft;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.SplashScreen;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import minicraft.entity.Bed;
import minicraft.entity.Entity;
import minicraft.entity.Furniture;
import minicraft.entity.Lantern;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftClient;
import minicraft.network.MinicraftServer;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.*;

public class Game {
	
	private static Random random = new Random();
	
	public static Game main = null; // holds a reference to the main game instance initialized in the main method.
	public static boolean debug = false;
	public static boolean HAS_GUI = true;
	
	/// MANAGERIAL VARS AND RUNNING
	
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window
	public static final String VERSION = "2.0.4-dev2";
	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	private static float SCALE = 3;
	
	public static final Point CENTER = new Point(WIDTH/2, HEIGHT/2);
	
	public static final String OS;
	public static final String localGameDir;
	public static final String systemGameDir;
	static {
		OS = System.getProperty("os.name").toLowerCase();
		//System.out.println("os name: \"" +os + "\"");
		if(OS.contains("windows")) // windows
			systemGameDir = System.getenv("APPDATA");
		else
			systemGameDir = System.getProperty("user.home");
		
		if(OS.contains("mac") || OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) // mac or linux
			localGameDir = "/.playminicraft/mods/Minicraft_Plus";
		else
			localGameDir = "/playminicraft/mods/Minicraft_Plus"; // windows, probably.
		
		//System.out.println("system game dir: " + systemGameDir);
	}
	public static String gameDir; // The directory in which all the game files are stored
	//public static String loadDir = "";
	
	public static int MAX_FPS = (int)Settings.get("fps");
	
	/// MULTIPLAYER
	public static boolean ISONLINE = false;
	public static boolean ISHOST = false; /// this being true doesn't mean this game isn't a client as well; becuase it should be.
	private static boolean autoclient = false; // used in the init method; jumps to multiplayer menu as client
	public static MinicraftServer server = null;
	public static MinicraftClient client = null;
	
	/// TIME AND TICKS
	
	public static final int normSpeed = 60; // measured in ticks / second.
	public static float gamespeed = 1; // measured in MULTIPLES OF NORMSPEED.
	public static boolean paused = true; // If the game is paused.
	
	public static int tickCount = 0; // The number of ticks since the beginning of the game day.
	private static int time = 0; // Facilites time of day / sunlight.
	public static final int dayLength = 64800; //this value determines how long one game day is.
	public static final int sleepEndTime = dayLength/8; //this value determines when the player "wakes up" in the morning.
	public static final int sleepStartTime = dayLength/2+dayLength/8; //this value determines when the player allowed to sleep.
	//public static int noon = 32400; //this value determines when the sky switches from getting lighter to getting darker.
	
	private static boolean running = false; // This is about more than simply being paused -- it keeps the game loop running.
	public static int fra, tik; //these store the number of frames and ticks in the previous second; used for fps, at least.
	public static int gameTime = 0; // This stores the total time (number of ticks) you've been playing your Game.
	
	/// RENDERING
	
	private static Canvas canvas = new Canvas();
	private static BufferedImage image; // creates an image to be displayed on the screen.
	protected static int[] pixels; // the array of pixels that will be displayed on the screen.
	//private int[] colors; // All of the colors, put into an array.
	/// these are public, but should not be modified:
	public static Screen screen; // Creates the main screen
	public static Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	
	/// LEVEL AND PLAYER
	
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.
	
	public static final int[] idxToDepth = {-3, -2, -1, 0, 1, -4}; /// this is to map the level depths to each level's index in Game's levels array. This must ALWAYS be the same length as the levels array, of course.
	public static final int minLevelDepth, maxLevelDepth;
	static {
		int min, max;
		min = max = idxToDepth[0];
		for(int depth: idxToDepth) {
			if(depth < min)
				min = depth;
			if(depth > max)
				max = depth;
		}
		minLevelDepth = min;
		maxLevelDepth = max;
	}
	
	public static InputHandler input; // input used in Game, Player, and just about all the *Menu classes.
	private static Display menu = null, newMenu = null; // the current menu you are on.
	public static Player player; // The Player.
	//public Level level; // This is the current level you are on.
	private static int worldSize = 128; // The size of the world
	public static int lvlw = worldSize; // The width of the world
	public static int lvlh = worldSize; // The height of the world
	
	private static int playerDeadTime; // the time after you die before the dead menu shows up.
	private static int pendingLevelChange; // used to determine if the player should change levels or not.
	private static boolean gameOver; // If the player wins this is set to true.
	
	/// AUTOSAVE AND NOTIFICATIONS
	
	public static List<String> notifications = new ArrayList<>();
	public static int notetick; // "note"= notifications.
	
	private static final int astime = 7200; //stands for Auto-Save Time (interval)
	public static int asTick; // The time interval between autosaves.
	public static boolean saving = false; // If the game is performing a save.
	public static int savecooldown; // Prevents saving many times too fast, I think.
	
	/// SCORE MODE
	
	private static int multiplier = 1; // Score multiplier
	private static final int mtm = 300; // time given to increase multiplier before it goes back to 1.
	private static int multipliertime = mtm; // Time left on the current multiplier.
	
	public static int scoreTime; // time remaining for score mode Game.
	private static boolean showinfo;
	
	public static boolean pastDay1 = true; // used to prefent mob spawn on surface on day 1.
	public static boolean readyToRenderGameplay = false;
	
	public enum Time {
		Morning (0),
		Day (Game.dayLength/4),
		Evening (Game.dayLength/2),
		Night (Game.dayLength/4*3);
		
		public int tickTime;
		
		Time(int ticks) {
			tickTime = ticks;
		}
	}
	
	
	// Sets the current menu.
	public static void setMenu(Display display) {
		//Display parent = newMenu;
		newMenu = display;
	}
	
	public static void exitMenu() {
		if(menu == null) return; // no action required; cannot exit from no menu
		//newMenu.onExit();
		//if(debug) System.out.println("exiting menu from " + newMenu + " to " + newMenu.getParent());
		newMenu = menu.getParent();
	}
	
	public static Display getMenu() { return menu; }
	
	public static boolean isValidClient() {
		return ISONLINE && client != null;
	}
	public static boolean isConnectedClient() {
		return isValidClient() && client.isConnected();
	}
	
	public static boolean isValidServer() {
		return ISONLINE && ISHOST && server != null;
	}
	public static boolean hasConnectedClients() {
		return isValidServer() && server.hasClients();
	}
	
	
	public static boolean isMode(String mode) {
		return ((String)Settings.get("mode")).equalsIgnoreCase(mode);
	}
	/// called after main; main is at bottom.
	/*public static void start() {
		running = true;
		new Thread(this).start(); //calls run()
	}*/
	
	private Game() {} // can't instantiate the Game class.
	
	// This is only called by GameApplet...
	/*public static void stop() {
		running = false;
	}*/
	
	/**
	 * Initialization step, this is called when the game first starts. Sets up the screens.
	 */
	private static void init() {
		/* This sets up the screens, and loads the icons.png spritesheet. */
		try {
			screen = new Screen(new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream
					("/resources/icons.png"))));
			lightScreen = new Screen(new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream
					("/resources/icons.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		screen.pixels = pixels;
		
		Tiles.initTileList();
		
		resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(true); // this loads any saved preferences.
		
		if(autoclient)
			setMenu(new MultiplayerMenu( "localhost"));
		else if(!HAS_GUI)
			setMenu(new WorldSelectMenu());
		else
			setMenu(new TitleMenu()); //sets menu to the title screen.
	}
	
	/** This method is used when respawning, and by initWorld to reset the vars. It does not generate any new terrain. */
	public static void resetGame() {
		if(Game.debug) System.out.println("resetting Game...");
		playerDeadTime = 0;
		currentLevel = 3;
		asTick = 0;
		notifications.clear();
		
		// adds a new player
		if(isValidServer()) {
			player = null;
			return;
		}
		if(player instanceof RemotePlayer) {
			player = new RemotePlayer(true, (RemotePlayer)player);
		} else
			player = new Player(player, input);
		
		if (levels[currentLevel] == null) return;
		
		// "shouldRespawn" is false on hardcore, or when making a new world.
		if (DeadMenu.shouldRespawn) { // respawn, don't regenerate level.
			//if (debug) System.out.println("Current Level = " + currentLevel);
			if(!Game.isValidClient()) {
				Level level = levels[currentLevel];
				player.respawn(level);
				//if (debug) System.out.println("respawned player in current world");
				level.add(player); // adds the player to the current level (always surface here)
			} else {
				Game.client.requestRespawn();
			}
		}
	}
	
	/** This method is used to create a brand new world, or to load an existing one from a file. */
	/** For the loading screen updates to work, it it assumed that *this* is called by a thread *other* than the one rendering the current *menu*. */
	public static void initWorld() { // this is a full reset; everything.
		if(Game.debug) System.out.println("resetting world...");
		
		if(Game.isValidServer()) {
			System.err.println("Cannot initialize world while acting as a server runtime; not running initWorld().");
			return;
		}
		
		DeadMenu.shouldRespawn = false;
		resetGame();
		player = new Player(null, input);
		Bed.inBed = false;
		gameTime = 0;
		Game.gamespeed = 1;
		
		changeTimeOfDay(Time.Morning); // resets tickCount; game starts in the day, so that it's nice and bright.
		gameOver = false;
		
		levels = new Level[6];
		
		scoreTime = (Integer) Settings.get("scoretime");
		
		LoadingDisplay.setPercentage(0); // this actually isn't necessary, I think; it's just in case.
		
		if(!isValidClient()) {
			if(Game.debug) System.out.println("initializing world non-client...");
			
			if(WorldSelectMenu.loadedWorld())
				new Load(WorldSelectMenu.getWorldName());
			else {
				worldSize = (Integer) Settings.get("size");
				
				float loadingInc = 100f / (maxLevelDepth - minLevelDepth + 1); // the .002 is for floating point errors, in case they occur.
				for (int i = maxLevelDepth; i >= minLevelDepth; i--) {
					// i = level depth; the array starts from the top because the parent level is used as a reference, so it should be constructed first. It is expected that the highest level will have a null parent.
					if(Game.debug) System.out.println("loading level " + i + "...");
					levels[lvlIdx(i)] = new Level(worldSize, worldSize, i, levels[lvlIdx(i+1)], !WorldSelectMenu.loadedWorld());
					
					LoadingDisplay.progress(loadingInc);
				}
				
				if(Game.debug) System.out.println("level loading complete.");
				
				// add an Iron lantern to level 5, at (984, 984), when making a new world
				Furniture f = new Lantern(Lantern.Type.IRON);//Items.get("Iron Lantern").furniture;
				f.x = 984;
				f.y = 984;
				levels[lvlIdx(-4)].add(f);
				
				Level level = levels[currentLevel]; // sets level to the current level (3; surface)
				pastDay1 = false;
				player.findStartPos(level, WorldGenMenu.getSeed()); // finds the start level for the player
				level.add(player);
			}
			
			if(Settings.get("Theme").equals("Hell")) {
				player.inventory.add(Items.get("lava potion"));
			}
			readyToRenderGameplay = true;
		} else {
			Game.levels = new Level[6];
			currentLevel = 3;
		}
		
		DeadMenu.shouldRespawn = true;
		
		if(Game.debug) System.out.println("world initialized.");
	}
	
	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public static void tick() {
		if(newMenu != menu) {
			if(menu != null)
				menu.onExit();
				
			//if(debug) System.out.println("setting menu from " + newMenu + " to " + display);
			
			if (newMenu != null && (menu == null || newMenu != menu.getParent()))
				newMenu.init(menu);
			
			menu = newMenu;
		}
		
		Level level = levels[currentLevel];
		if (Bed.inBed && !Game.isValidClient()) {
			// IN BED
			//Bed.player.remove();
			if(gamespeed != 20) {
				gamespeed = 20;
				if(Game.isValidServer()) {
					if (Game.debug) System.out.println("SERVER: setting time for bed");
					server.updateGameVars();
				}
			}
			if(tickCount > sleepEndTime) {
				pastDay1 = true;
				tickCount = 0;
				if(Game.isValidServer())
					server.updateGameVars();
			}
			if (tickCount <= sleepStartTime && tickCount >= sleepEndTime) { // it has reached morning.
				Player playerInBed = Bed.restorePlayer();
				gamespeed = 1;
				if(Game.isValidServer())
					server.updateGameVars();
				
				// seems this removes all entities within a certain radius of the player when you get OUT of Bed.
				for (Entity e: level.getEntityArray()) {
					if (e.getLevel() == levels[currentLevel]) {
						int xd = playerInBed.x - e.x;
						int yd = playerInBed.y - e.y;
						if (xd * xd + yd * yd < 48 && e instanceof Mob && !(e instanceof Player)) {
							// this comes down to a radius of about half a tile... huh...
							level.remove(e);
						}
					}
				}
			}
		}
		
		//auto-save tick; marks when to do autosave.
		if(!paused || isValidServer())
			asTick++;
		if (asTick > astime) {
			if ((boolean)Settings.get("autosave") && player.health > 0 && !gameOver) {
				if(!Game.ISONLINE)
					new Save(WorldSelectMenu.getWorldName());
				else if(Game.isValidServer())
					Game.server.saveWorld();
			}
			
			asTick = 0;
		}
		
		// Increment tickCount if the game is not paused
		if (!paused || isValidServer()) setTime(tickCount+1);
		//if (tickCount == 3600) level.removeAllEnemies(); // because of lifetime, I don't need this.
		
		/// SCORE MODE ONLY
		
		if (Game.isMode("score") && (!paused || isValidServer() && !gameOver)) {
			if (scoreTime <= 0) { // GAME OVER
				gameOver = true;
				setMenu(new EndGameDisplay(player));
			}
			
			scoreTime--;
			
			if (!paused && multiplier > 1) {
				if (multipliertime != 0) multipliertime--;
				if (multipliertime == 0) setMultiplier(1);
			}
			if (multiplier > 50) multiplier = 50;
		}
		
		boolean hadMenu = menu != null;
		if(isValidServer()) {
			/// this is to keep the game going while online, even with an unfocused window.
			input.tick();
			for(Level floor: levels) {
				if(floor == null) continue;
				floor.tick();
			}
			
			Tile.tickCount++;
		}
		
		// This is the general action statement thing! Regulates menus, mostly.
		if (!canvas.hasFocus() && HAS_GUI) {
			input.releaseAll();
		}
		if(canvas.hasFocus() || ISONLINE || !HAS_GUI) {
			if ((isValidServer() || !player.isRemoved()) && !gameOver) {
				gameTime++;
			}
			
			if(!isValidServer() || menu != null && !hadMenu)
				input.tick(); // INPUT TICK; no other class should call this, I think...especially the *Menu classes.
			
			if (menu != null) {
				//a menu is active.
				player.tick(); // it is CRUCIAL that the player is ticked HERE, before the menu is ticked. I'm not quite sure why... the menus break otherwise, though.
				menu.tick(input);
				paused = true;
			} else {
				//no menu, currently.
				paused = false;
				
				if(!Game.isValidServer()) {
					//if player is alive, but no level change, nothing happens here.
					if (player.isRemoved() && readyToRenderGameplay && !Bed.inBed) {
						//makes delay between death and death menu.
						playerDeadTime++;
						if (playerDeadTime > 60) {
							setMenu(new DeadMenu());
						}
					} else if (pendingLevelChange != 0) {
						setMenu(new LevelTransitionDisplay(pendingLevelChange));
						pendingLevelChange = 0;
					}
					
					player.tick(); // ticks the player when there's no menu.
					
					if(level != null) {
						level.tick();
						Tile.tickCount++;
					}
					
					if(!HAS_GUI) startMultiplayerServer();
				}
				else if(Game.isValidServer()) {
					// here is where I should put things like select up/down, backspace to boot, esc to open pause menu, etc.
					if(input.getKey("pause").clicked)
						setMenu(new PauseMenu());
				}
				
				if(menu == null && input.getKey("F3").clicked) { // shows debug info in upper-left
					showinfo = !showinfo;
				}
				
				//for debugging only
				if (debug && HAS_GUI) {
					
					if(!ISONLINE || isValidServer()) {
						/// server-only cheats.
						if (input.getKey("Shift-r").clicked && !isValidServer())
							initWorld(); // this will almost certainaly break in multiplayer, i think...
						
						if (input.getKey("1").clicked) changeTimeOfDay(Time.Morning);
						if (input.getKey("2").clicked) changeTimeOfDay(Time.Day);
						if (input.getKey("3").clicked) changeTimeOfDay(Time.Evening);
						if (input.getKey("4").clicked) changeTimeOfDay(Time.Night);
						
						if (input.getKey("creative").clicked) {Settings.set("mode", "creative");
							Items.fillCreativeInv(player.inventory, false);}
						if (input.getKey("survival").clicked) Settings.set("mode", "survival");
						if (input.getKey("shift-t").clicked) Settings.set("mode", "score");
						if (Game.isMode("score") && input.getKey("ctrl-t").clicked){ scoreTime = normSpeed * 5; // 5 seconds
							if(Game.isValidServer()) server.updateGameVars();
						}
						
						float prevSpeed = gamespeed;
						if (input.getKey("shift-0").clicked)
							gamespeed = 1;
						
						if (input.getKey("shift-equals").clicked) {
							if(gamespeed < 1) gamespeed *= 2;
							else if(normSpeed*gamespeed < 2000) gamespeed++;
						}
						if (input.getKey("shift-minus").clicked) {
							if(gamespeed > 1) gamespeed--;
							else if(normSpeed*gamespeed > 5) gamespeed /= 2;
						}
						if(gamespeed != prevSpeed && isValidServer())
							server.updateGameVars();
					}
					
					
					if(!ISONLINE || isValidClient()) {
						/// client-only cheats, since they are player-specific.
						
						if (input.getKey("shift-g").clicked) // this should not be needed, since the inventory should not be altered.
							Items.fillCreativeInv(player.inventory);
						
						if(input.getKey("ctrl-h").clicked) player.health--;
						
						if (input.getKey("0").clicked) player.moveSpeed = 1;
						if (input.getKey("equals").clicked) player.moveSpeed++;//= 0.5D;
						if (input.getKey("minus").clicked && player.moveSpeed > 1) player.moveSpeed--;// -= 0.5D;
						
						if(input.getKey("shift-u").clicked) {
							levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Up"));
						}
						if(input.getKey("shift-d").clicked) {
							levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Down"));
						}
						
						if(input.getKey("ctrl-p").clicked) {
							/// list all the remote players in the level and their coordinates.
							//System.out.println("searching for players on current level...");
							levels[currentLevel].printEntityLocs(Player.class);
						}
						
						if(Game.isConnectedClient() && input.getKey("alt-t").clicked) {
							// update the tile with the server's value for it.
							Game.client.requestTile(player.getLevel(), player.x >> 4, player.y >> 4);
						}
					}
				} // end debug only cond.
			} // end "menu-null" conditional
		} // end hasfocus conditional
	} // end tick()
	
	public static Entity getEntity(int eid) {
		for(Level level: levels) {
			if(level == null) continue;
			for(Entity e: level.getEntityArray())
				if(e.eid == eid)
					return e;
		}
		
		//if(Game.debug) System.out.println(onlinePrefix()+"couldn't find entity with id " + eid);
		
		return null;
	}
	
	public static int generateUniqueEntityId() {
		int eid;
		int tries = 0; // just in case it gets out of hand.
		do {
			tries++;
			if(tries == 1000)
				System.out.println("note: trying 1000th time to find valid entity id...(will continue)");
			
			eid = random.nextInt();
		} while(!idIsAvaliable(eid));
		
		return eid;
	}
	
	public static boolean idIsAvaliable(int eid) {
		if(eid == 0) return false; // this is reserved for the main player... kind of...
		if(eid < 0) return false; // id's must be positive numbers.
		
		for(Level level: levels) {
			if(level == null) continue;
			for(Entity e: level.getEntityArray()) {
				if(e.eid == eid)
					return false;
			}
		}
		
		return true;
	}
	
	public static String onlinePrefix() {
		if(!Game.ISONLINE) return "";
		String prefix = "From ";
		if(Game.isValidServer())
			prefix += "Server";
		else if(Game.isValidClient())
			prefix += "Client";
		else
			prefix += "nobody";
		
		prefix += ": ";
		return prefix;
	}
	
	public static int getMultiplier() { return multiplier; }
	public static void setMultiplier(int value) {
		multiplier = value;
		multipliertime = mtm;
	}
	public static void addMultiplier(int value) {
		multiplier += value;
		multipliertime = mtm - 5;
	}
	
	/// this is the proper way to change the tickCount.
	public static void setTime(int ticks) {
		if (ticks < Time.Morning.tickTime) ticks = 0; // error correct
		if (ticks < Time.Day.tickTime) time = 0; // morning
		else if (ticks < Time.Evening.tickTime) time = 1; // day
		else if (ticks < Time.Night.tickTime) time = 2; // evening
		else if (ticks < dayLength) time = 3; // night
		else { // back to morning
			time = 0;
			ticks = 0;
			pastDay1 = true;
		}
		tickCount = ticks;
	}
	
	/// this is the proper way to change the time of day.
	public static void changeTimeOfDay(Time t) {
		setTime(t.tickTime);
		if(isValidServer())
			server.updateGameVars();
	}
	// this one works too.
	public static void changeTimeOfDay(int t) {
		Time[] times = Time.values();
		if(t > 0 && t < times.length)
			changeTimeOfDay(times[t]); // it just references the other one.
		else
			System.out.println("time " + t + " does not exist.");
	}
	
	public static Time getTime() {
		Time[] times = Time.values();
		return times[time];
	}
	
	/** This adds a notifcation to all player games. */
	public static void notifyAll(String msg) {
		notifyAll(msg, 0);
	}
	public static void notifyAll(String msg, int notetick) {
		Game.notifications.add(msg);
		Game.notetick = notetick;
		if(isValidServer())
			Game.server.broadcastNotification(msg, notetick);
		else if(isConnectedClient())
			Game.client.sendNotification(msg, notetick);
	}
	
	/** This method changes the level that the player is currently on.
	 * It takes 1 integer variable, which is used to tell the game which direction to go.
	 * For example, 'changeLevel(1)' will make you go up a level,
	 	while 'changeLevel(-1)' will make you go down a level. */
	public static void changeLevel(int dir) {
		if(Game.isValidServer()) {
			System.out.println("server tried to change level.");
			return;
		}
		
		if(Game.isConnectedClient())
			levels[currentLevel].clearEntities(); // clear all the entities from the last level, so that no artifacts remain. They're loaded dynamically, anyway.
		else
			levels[currentLevel].remove(player); // removes the player from the current level.
		
		int nextLevel = currentLevel + dir;
		if (nextLevel <= -1) nextLevel = levels.length-1; // fix accidental level underflow
		if (nextLevel >= levels.length) nextLevel = 0; // fix accidental level overflow
		//level = levels[currentLevel]; // sets the level to the current level
		currentLevel = nextLevel;
		
		player.x = (player.x >> 4) * 16 + 8; // sets the player's x coord (to center yourself on the stairs)
		player.y = (player.y >> 4) * 16 + 8; // sets the player's y coord (to center yourself on the stairs)
		
		if(isConnectedClient()/* && levels[currentLevel] == null*/) {
			readyToRenderGameplay = false;
			Game.client.requestLevel(currentLevel);
		} else
			levels[currentLevel].add(player); // adds the player to the level.
	}
	
	/** This is for a contained way to find the index in the levels array of a level, based on it's depth. This is also helpful because add a new level in the future could change this. */
	public static int lvlIdx(int depth) {
		if(depth > maxLevelDepth) return lvlIdx(minLevelDepth);
		if(depth < minLevelDepth) return lvlIdx(maxLevelDepth);
		
		if(depth == -4) return 5;
		
		return depth + 3;
	}
	
	//private int ePos = 0;
	//private int eposTick = 0;
	private static char[] dots = "   ".toCharArray();
	
	/// just a little thing to make a progressive dot elipses.
	private static String getElipses() {
		//String dots = "";
		int time = tickCount % normSpeed; // sets the "dot clock" to normSpeed.
		int interval = normSpeed / 2; // specifies the time taken for each fill up and empty of the dots.
		int epos = (time % interval) / (interval/dots.length); // transforms time into a number specifying which part of the dots array it is in, by index.
		char set = time < interval ? '.' : ' '; // get the character to set in this cycle.
		
		dots[epos] = set;
		
		return new String(dots);
	}
	
	/** renders the current screen */
	//called in game loop, a bit after tick()
	public static void render() {
		if(!HAS_GUI) return; // no point in this if there's no gui... :P
		
		BufferStrategy bs = canvas.getBufferStrategy(); // creates a buffer strategy to determine how the graphics should be buffered.
		if (bs == null) {
			canvas.createBufferStrategy(3); // if the buffer strategy is null, then make a new one!
			canvas.requestFocus(); // requests the focus of the screen.
			return;
		}
		
		if(readyToRenderGameplay) {
			if(Game.isValidServer()) {
				screen.clear(0);
				Font.drawCentered("Awaiting client connections"+getElipses(), screen, 10, Color.get(-1, 444));
				Font.drawCentered("So far:", screen, 20, Color.get(-1, 444));
				int i = 0;
				for(String playerString: Game.server.getClientInfo()) {
					Font.drawCentered(playerString, screen, 30+i*10, Color.get(-1, 134));
					i++;
				}
				
				renderDebugInfo();
			}
			else {
				//if(debug) System.out.println("rendering level");
				renderLevel();
				renderGui();
			}
		}// else if(debug)
			//System.out.println("not ready to render");
		
		if (menu != null) // renders menu, if present.
			menu.render(screen);
		
		if (!canvas.hasFocus() && !Game.ISONLINE) renderFocusNagger(); // calls the renderFocusNagger() method, which creates the "Click to Focus" message.
		
		Graphics g = bs.getDrawGraphics(); // gets the graphics in which java draws the picture
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // draws the a rect to fill the whole window (to cover last?)
		
		// scales the pixels.
		int ww = getWindowSize().width;
		int hh = getWindowSize().height;
		
		// gets the image offset.
		int xo = (canvas.getWidth() - ww) / 2 + canvas.getParent().getInsets().left;
		int yo = (canvas.getHeight() - hh) / 2 + canvas.getParent().getInsets().top;
		g.drawImage(image, xo, yo, ww, hh, null); //draws the image on the window
		g.dispose(); // releases any system items that are using this method. (so we don't have crappy framerates)
		
		bs.show(); // makes the picture visible. (probably)
	}
	
	private static void renderLevel() {
		Level level = Game.levels[currentLevel];
		if(level == null) return;
		
		int xScroll = player.x - Screen.w / 2; // scrolls the screen in the x axis.
		int yScroll = player.y - (Screen.h - 8) / 2; // scrolls the screen in the y axis.
		
		//stop scrolling if the screen is at the ...
		if (xScroll < 0) xScroll = 0; // ...left border.
		if (yScroll < 0) yScroll = 0; // ...top border.
		if (xScroll > level.w * 16 - Screen.w) xScroll = level.w * 16 - Screen.w; // ...right border.
		if (yScroll > level.h * 16 - Screen.h) yScroll = level.h * 16 - Screen.h; // ...bottom border.
		if (currentLevel > 3) { // if the current level is higher than 3 (which only the sky level (and dungeon) is)
			int col = Color.get(20, 20, 121, 121); // background color.
			for (int y = 0; y < 28; y++)
				for (int x = 0; x < 48; x++) {
					// creates the background for the sky (and dungeon) level:
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 0, col, 0);
				}
		}
		
		level.renderBackground(screen, xScroll, yScroll); // renders current level background
		level.renderSprites(screen, xScroll, yScroll); // renders level sprites on screen
		
		// this creates the darkness in the caves
		if (currentLevel != 5 && (currentLevel != 3 || tickCount < dayLength/4 || tickCount > dayLength/2) && (!Game.isMode("creative") || currentLevel >= 3)) {
			lightScreen.clear(0); // this doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
			int brightnessMultiplier = player.potioneffects.containsKey(PotionType.Light) ? 12 : 8; // brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, currentLevel, xScroll, yScroll); // overlays the light screen over the main screen.
		}
	}
	
	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private static void renderGui() {
		/// AH-HA! THIS DRAWS THE BLACK SQUARE!!
		for (int x = 12; x < 29; x++) {
			screen.render(x * 7, Screen.h - 8, 0 + 1 * 32, Color.get(0, 0), 0);
		}
		
		renderDebugInfo();
		
		// This is the arrow counter. ^ = infinite symbol.
		int ac = player.inventory.count(Items.arrowItem);
		if (Game.isMode("creative") || ac >= 10000)
			Font.draw("	x" + "^", screen, 84, Screen.h - 16, Color.get(0, 333, 444, 555));
		else
			Font.draw("	x" + ac, screen, 84, Screen.h - 16, Color.get(0, 555));
		//displays arrow icon
		screen.render(10 * 8 + 4, Screen.h - 16, 13 + 5 * 32, Color.get(0, 111, 222, 430), 0);
		
		String msg = "";
		if (saving) msg = "Saving... " + Math.round(LoadingDisplay.getPercentage()) + "%";
		else if (Bed.inBed) msg = "Sleeping...";
		
		if(msg.length() > 0)
			new FontStyle(Color.WHITE).setYPos(Screen.h / 2 - 20).setShadowType(Color.DARK_GRAY, false).draw(msg, screen);
		
		/// NOTIFICATIONS
		
		if (notifications.size() > 0 && msg.length() == 0) {
			notetick++;
			if (notifications.size() > 3) { //only show 3 notifs max at one time; erase old notifs.
				notifications = notifications.subList(notifications.size() - 3, notifications.size());
			}
			
			if (notetick > 120) { //display time per notification.
				notifications.remove(0);
				notetick = 0;
			}
			
			// draw each current notification, with shadow text effect.
			FontStyle style = new FontStyle(Color.WHITE).setShadowType(Color.DARK_GRAY, false);
			for (int i = 0; i < notifications.size(); i++) {
				String note = notifications.get(i);
				int y = Screen.h - 120 - notifications.size()*8 + i * 8;
				style.setYPos(y).draw(note, screen);
			}
		}
		
		// SCORE MODE ONLY:
		
		if (Game.isMode("score")) {
			int seconds = (int)Math.ceil(scoreTime / (double)normSpeed);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;
			
			int timeCol;
			if(scoreTime >= 18000) timeCol = Color.get(0, 555);
			else if (scoreTime >= 3600) timeCol = Color.get(330, 555);
			else timeCol = Color.get(400, 555);
			
			Font.draw("Time left " + (hours > 0 ? hours+"h ":"") + minutes + "m " + seconds + "s", screen, Screen.w/2-9*8, 2, timeCol);
			
			String scoreString = "Current score: " + player.score;
			Font.draw(scoreString, screen, Screen.w - Font.textWidth(scoreString)-2, 3 + 8, Color.WHITE);
			
			if(multiplier > 1) {
				int multColor = multiplier < 50 ? Color.get(-1, 540) : Color.RED;
				String mult = "X" + multiplier;
				Font.draw(mult, screen, Screen.w-Font.textWidth(mult)-2, 4 + 2*8, multColor);
			}
		}

		// FISHING ROD STATUS
		if (player.activeItem instanceof ToolItem && ((ToolItem)player.activeItem).type == ToolType.FishingRod) {
			int dura = ((ToolItem)player.activeItem).dur * 100 / ((ToolItem)player.activeItem).type.durability;
			//if (dura > 100) dura = 100;
			Font.draw(dura + "%", screen, 164, Screen.h - 16, Color.get(0, 30));
		}
		
		/// This renders the potions overlay
		if(player.showpotioneffects && player.potioneffects.size() > 0) {
			Map.Entry<PotionType, Integer>[] effects = player.potioneffects.entrySet().toArray(new Map.Entry[0]);
				// the key is potion type, value is remaining potion duration.
			for(int i = 0; i < effects.length; i++) {
				PotionType pType = effects[i].getKey();
				int pTime = effects[i].getValue() / normSpeed;
				int pcol = Color.get(pType.dispColor, 555);
				Font.draw("("+input.getMapping("potionEffects")+" to hide!)", screen, 180, 9, Color.get(0, 555));
				Font.draw(pType + " (" + (pTime / 60) + ":" + (pTime % 60) + ")", screen, 180, 17 + i * Font.textHeight(), pcol);
			}
		}
		
		
		// This is the status icons, like health hearts, stamina bolts, and hunger "burgers".
		if (!Game.isMode("creative")) {
			for (int i = 0; i < 10; i++) {
				int color;
				
				// renders armor
				int armor = player.armor*10 / Player.maxArmor;
				color = (i <= armor && player.curArmor != null) ? player.curArmor.sprite.color : Color.get(-1, -1);
				screen.render(i * 8, Screen.h - 24, 3 + 12 * 32, color, 0);
				
				// renders your current red hearts, or black hearts for damaged health.
				color = (i < player.health) ? Color.get(-1, 200, 500, 533) : Color.get(-1, 100, 000, 000);
				screen.render(i * 8, Screen.h - 16, 0 + 12 * 32, color, 0);
				
				if (player.staminaRechargeDelay > 0) {
					// creates the white/gray blinking effect when you run out of stamina.
					color = (player.staminaRechargeDelay / 4 % 2 == 0) ? Color.get(-1, 555, 000, 000) : Color.get(-1, 110, 000, 000);
					screen.render(i * 8, Screen.h - 8, 1 + 12 * 32, color, 0);
				} else {
					// renders your current stamina, and uncharged gray stamina.
					color = (i < player.stamina) ? Color.get(-1, 220, 550, 553) : Color.get(-1, 110, 000, 000);
					screen.render(i * 8, Screen.h - 8, 1 + 12 * 32, color, 0);
				}
				
				// renders hunger
				color = (i < player.hunger) ? Color.get(-1, 100, 530, 211) : Color.get(-1, 100, 000, 000);
				screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2 + 12 * 32, color, 0);
			}
		}
		
		/// CURRENT ITEM
		if (player.activeItem != null) // shows active item sprite and name in bottom toolbar, if one exists.
			player.activeItem.renderInventory(screen, 12 * 7, Screen.h - 8, false);
	}
	
	private static void renderDebugInfo() {
		int textcol = Color.WHITE;
		if (showinfo) { // renders show debug info on the screen.
			ArrayList<String> info = new ArrayList<>();
			info.add("VERSION " + VERSION);
			info.add(fra + " fps");
			info.add("day tiks " + tickCount);
			info.add((normSpeed * gamespeed) + " tik/sec");
			if(!Game.isValidServer()) {
				info.add("walk spd " + player.moveSpeed);
				info.add("X " + (player.x / 16) + "-" + (player.x % 16));
				info.add("Y " + (player.y / 16) + "-" + (player.y % 16));
				if(Game.levels[currentLevel] != null)
					info.add("Tile " + Game.levels[currentLevel].getTile(player.x>>4, player.y>>4).name);
				if (Game.isMode("score")) info.add("Score " + player.score);
			}
			if(Game.levels[currentLevel] != null) {
				if(!Game.isValidClient())
					info.add("Mob Cnt " + Game.levels[currentLevel].mobCount + "/" + Game.levels[currentLevel].maxMobCount);
				else
					info.add("Mob Load Cnt " + Game.levels[currentLevel].mobCount);
			}
			
			/// Displays number of chests left, if on dungeon level.
			if (Game.levels[currentLevel] != null && (Game.isValidServer() || currentLevel == 5 && !Game.isValidClient())) {
				if (levels[5].chestcount > 0)
					info.add("Chests: " + levels[5].chestcount);
				else
					info.add("Chests: Complete!");
			}
			
			if(!Game.isValidServer()) {
				if(player.armor > 0) {
					info.add("armor: " + player.armor);
					info.add("dam buffer: " + player.armorDamageBuffer);
				}
				
				//info.add("steps: " + player.stepCount);
				if(debug)
					info.add("micro-hunger:" + player.hungerStamCnt);
				//info.add("health regen:" + player.hungerStamCnt);
			}
			
			FontStyle style = new FontStyle(textcol).setShadowType(Color.BLACK, true).setXPos(1);
			for(int i = 0; i < info.size(); i++) {
				style.setYPos(2 + i*10).draw(info.get(i), screen);
			}
		}
	}
	
	/** Renders the "Click to focus" box when you click off the screen. */
	private static void renderFocusNagger() {
		String msg = "Click to focus!"; // the message when you click off the screen.
		paused = true; //perhaps paused is only used for this.
		int xx = Font.centerX(msg, 0, Screen.w); // the width of the box
		int yy = (HEIGHT - 8) / 2; // the height of the box
		int w = msg.length(); // length of message in characters.
		int h = 1;
		int txtcolor = Color.get(-1, 1, 5, 445);
		
		// renders the four corners of the box
		screen.render(xx - 8, yy - 8, 0 + 13 * 32, txtcolor, 0);
		screen.render(xx + w * 8, yy - 8, 0 + 13 * 32, txtcolor, 1);
		screen.render(xx - 8, yy + 8, 0 + 13 * 32, txtcolor, 2);
		screen.render(xx + w * 8, yy + 8, 0 + 13 * 32, txtcolor, 3);
		
		// renders each part of the box...
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy - 8, 1 + 13 * 32, txtcolor, 0); // ...top part
			screen.render(xx + x * 8, yy + 8, 1 + 13 * 32, txtcolor, 2); // ...bottom part
		}
		for (int y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 2 + 13 * 32, txtcolor, 0); // ...left part
			screen.render(xx + w * 8, yy + y * 8, 2 + 13 * 32, txtcolor, 1); // ...right part
		}
		
		//renders the focus nagger text with a flash effect...
		if ((tickCount / 20) % 2 == 0) // ...medium yellow color
			Font.draw(msg, screen, xx, yy, Color.get(5, 333));
		else // ...bright yellow color
			Font.draw(msg, screen, xx, yy, Color.get(5, 555));
	}
	
	/** This method is called when you interact with stairs, this will give you the transition effect. While changeLevel(int) just changes the level. */
	public static void scheduleLevelChange(int dir) {
		// same as changeLevel(). Call scheduleLevelChange(1) if you want to go up 1 level,
		// or call with -1 to go down by 1.
		if(!Game.isValidServer())
			pendingLevelChange = dir;
	}
	
	public static void startMultiplayerServer() {
		if(Game.debug) System.out.println("starting multiplayer server...");
		
		if(HAS_GUI) {
			// here is where we need to start the new client.
			String jarFilePath = "";
			try {
				java.net.URI uri = Game.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				//if (Game.debug) System.out.println("jar path: " + uri.getPath());
				//if (Game.debug) System.out.println("jar string: " + uri.toString());
				jarFilePath = uri.getPath();
				if(OS.contains("windows") && jarFilePath.startsWith("/"))
					jarFilePath = jarFilePath.substring(1);
			} catch(URISyntaxException ex) {
				System.err.println("problem with jar file URI syntax.");
				ex.printStackTrace();
			}
			List<String> arguments = new ArrayList<>();
			arguments.add("java");
			arguments.add("-jar");
			arguments.add(jarFilePath);
			
			if(debug)
				arguments.add("--debug");
			
			// this will just always be added.
			arguments.add("--savedir");
			arguments.add(systemGameDir);
			
			arguments.add("--localclient");
			
			/// this *should* start a new JVM from the running jar file...
			try {
				new ProcessBuilder(arguments).inheritIO().start();
			} catch(IOException ex) {
				System.err.println("problem starting new jar file process:");
				ex.printStackTrace();
			}
		}
		// now that that's done, let's turn *this* running JVM into a server:
		server = new MinicraftServer();
		
		/// load up any saved config options for the server.
		new Load(WorldSelectMenu.getWorldName(), server);
	}
	
	/**
	 * This is the main loop that runs the Game. It:
	 *	-keeps track of the amount of time that has passed
	 *	-fires the ticks needed to run the game
	 *	-fires the command to render out the screen.
	 */
	public static void run() {
		running = true;
		
		long lastTime = System.nanoTime();
		long lastRender = System.nanoTime();
		double unprocessed = 0;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		
		//calls setMenu with new TitleMenu (and does other things)
		init();
		
		//main game loop? calls tick() and render().
		if(!HAS_GUI)
			(new ConsoleReader()).start();
		while (running) {
			long now = System.nanoTime();
			double nsPerTick = 1E9D / normSpeed; // nanosecs per sec divided by ticks per sec = nanosecs per tick
			if(menu == null) nsPerTick /= gamespeed;
			unprocessed += (now - lastTime) / nsPerTick; //figures out the unprocessed time between now and lastTime.
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) { // If there is unprocessed time, then tick.
				//if(debug) System.out.println("ticking...");
				ticks++;
				tick(); // calls the tick method (in which it calls the other tick methods throughout the code.
				unprocessed--;
				shouldRender = true; // sets shouldRender to be true... maybe tick() could make it false?
			}
			
			try {
				Thread.sleep(2); // makes a small pause for 2 milliseconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (shouldRender && (now - lastRender) / 1.0E9 > 1.0 / MAX_FPS) {
				frames++;
				lastRender = System.nanoTime();
				render();
			}
			
			if (System.currentTimeMillis() - lastTimer1 > 1000) { //updates every 1 second
				lastTimer1 += 1000; // adds a second to the timer
				
				fra = frames; //saves total frames in last second
				tik = ticks; //saves total ticks in last second
				frames = 0; //resets frames
				ticks = 0; //resets ticks; ie, frames and ticks only are per second
			}
		}
		
		if (Game.debug) System.out.println("main game loop ended; terminating application...");
		System.exit(0);
	}
	
	/// * The main method! * ///
	public static void main(String[] args) {
		
		/*Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				String exceptionTrace = "Exception in thread " + t + ":P\n";
				exceptionTrace += Game.getExceptionTrace(e);
				System.err.println(exceptionTrace);
				javax.swing.JOptionPane.showInternalMessageDialog(null, exceptionTrace, "Fatal Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		});*/
		
		
		boolean debug = false;
		boolean autoclient = false;
		boolean autoserver = false;
		
		String saveDir = Game.systemGameDir;
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("--debug"))
				debug = true;
			if(args[i].equals("--savedir") && i+1 < args.length) {
				i++;
				saveDir = args[i];
			}
			if(args[i].equals("--localclient"))
				autoclient = true;
			if(args[i].equals("--server")) {
				autoserver = true;
				if(i+1 < args.length) {
					i++;
					WorldSelectMenu.setWorldName(args[i]);
				}
			}
		}
		Game.debug = debug;
		HAS_GUI = !autoserver;
		
		//if(HAS_GUI) {
			/*SplashScreen splash = SplashScreen.getSplashScreen();
			if (splash != null) {
				// TODO maybe in the future, I can make it so that the window is not displayed until the title menu is first rendered?
			}*/
		//}
		
		Game.gameDir = saveDir + Game.localGameDir;
		if(Game.debug) System.out.println("determined gameDir: " + Game.gameDir);
		
		String prevLocalGameDir = "/.playminicraft/mods/Minicraft Plus";
		File testFile = new File(systemGameDir + localGameDir);
		File testFileOld = new File(systemGameDir + prevLocalGameDir);
		if(!testFile.exists() && testFileOld.exists()) {
			// rename the old folders to the new scheme
			testFile.mkdirs();
			if(OS.contains("windows")) {
				try {
					java.nio.file.Files.setAttribute(testFile.toPath(), "dos:hidden", true);
				} catch (java.io.IOException ex) {
					System.err.println("couldn't make game folder hidden on windows:");
					ex.printStackTrace();
				}
			}
			
			File[] files = getAllFiles(testFileOld).toArray(new File[0]);
			for(File file: files) {
				//testFile
				File newFile = new File(file.getPath().replace(testFileOld.getPath(), testFile.getPath()));
				//System.out.println("new file: " + newFile.getPath());
				if(file.isDirectory()) newFile.mkdirs(); // these should be unnecessary.
				else file.renameTo(newFile);
				//File main = new File(testFileOld.getPath()+"/Preferences.miniplussave");
			}
			
			deleteAllFiles(testFileOld);
			
			testFile = new File(systemGameDir + ".playminicraft");
			if(OS.contains("windows") && testFile.exists())
				deleteAllFiles(testFile);
		}
		
		if(HAS_GUI) {
			canvas.setMinimumSize(new java.awt.Dimension(1, 1));
			canvas.setPreferredSize(getWindowSize());
			JFrame frame = new JFrame(Game.NAME);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout()); // sets the layout of the window
			frame.add(canvas, BorderLayout.CENTER); // Adds the game (which is a canvas) to the center of the screen.
			frame.pack(); //squishes everything into the preferredSize.
			
			try {
				BufferedImage logo = ImageIO.read(Game.class.getResourceAsStream("/resources/logo.png"));
				frame.setIconImage(logo);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			frame.setLocationRelativeTo(null); // the window will pop up in the middle of the screen when launched.
			
			frame.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					float w = frame.getWidth() - frame.getInsets().left - frame.getInsets().right;
					float h = frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom;
					Game.SCALE = Math.min(w / Game.WIDTH, h / Game.HEIGHT);
				}
			});
			
			frame.addWindowListener(new WindowListener() {
				public void windowActivated(WindowEvent e) {}
				public void windowDeactivated(WindowEvent e) {}
				public void windowIconified(WindowEvent e) {}
				public void windowDeiconified(WindowEvent e) {}
				public void windowOpened(WindowEvent e) {}
				public void windowClosed(WindowEvent e) {System.out.println("window closed");}
				public void windowClosing(WindowEvent e) {
					System.out.println("window closing");
					if(Game.isConnectedClient())
						Game.client.endConnection();
					if(Game.isValidServer())
						Game.server.endConnection();
					
					Game.quit();
				}
			});
			
			frame.setVisible(true);
			
			/*canvas.createBufferStrategy(3);
			BufferStrategy bs = canvas.getBufferStrategy();
			Graphics g = bs.getDrawGraphics();
			g.drawString("Loading menus...", Game.WIDTH/4, Game.HEIGHT/2);
			g.dispose();
			bs.show();*/
		}
		
		Game.autoclient = autoclient; // this will make the game automatically jump to the MultiplayerMenu, and attempt to connect to localhost.
		
		input = new InputHandler();
		
		fra = 0; // the frames processed in the previous second
		tik = 0; // the ticks processed in the previous second
		
		//colors = new int[256];
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		asTick = 0;
		notetick = 0;
		
		showinfo = false;
		gameOver = false;
		
		run(); // Starts the game!
	}
	
	public static Canvas getCanvas() { return canvas; }
	
	public static java.awt.Dimension getWindowSize() {
		return new java.awt.Dimension(new Float(WIDTH * SCALE).intValue(), new Float(HEIGHT * SCALE).intValue());
	}
	
	public static void quit() {
		if(Game.isConnectedClient()) Game.client.endConnection();
		if(Game.isValidServer()) Game.server.endConnection();
		running = false;
	}
	
	private static List<File> getAllFiles(File top) {
		List<File> files = new ArrayList<File>();
		if(top == null) {
			System.err.println("GAME: cannot search files of null folder.");
			return files;
		}
		if(!top.isDirectory()) {
			files.add(top);
			return files;
		} else
			files.add(top);
		
		File[] subfiles = top.listFiles();
		if(subfiles != null)
			for(File subfile: subfiles)
				files.addAll(getAllFiles(subfile));
		
		return files;
	}
	
	private static void deleteAllFiles(File top) {
		if(top == null) return;
		if(top.isDirectory()) {
			File[] subfiles = top.listFiles();
			if(subfiles != null)
				for (File subfile : subfiles)
					deleteAllFiles(subfile);
		}
		//noinspection ResultOfMethodCallIgnored
		top.delete();
	}
	
	/**
	 * Provides a String representation of the provided Throwable's stack trace
	 * that is extracted via PrintStream.
	 *
	 * @param throwable Throwable/Exception from which stack trace is to be
	 *	extracted.
	 * @return String with provided Throwable's stack trace.
	 */
	public static String getExceptionTrace(final Throwable throwable) {
		final java.io.ByteArrayOutputStream bytestream = new java.io.ByteArrayOutputStream();
		final java.io.PrintStream printStream = new java.io.PrintStream(bytestream);
		throwable.printStackTrace(printStream);
		String exceptionStr;
		try {
			exceptionStr = bytestream.toString("UTF-8");
		}
		catch(Exception ex) {
			exceptionStr = "Unavailable";
		}
		return exceptionStr;
	}
}
