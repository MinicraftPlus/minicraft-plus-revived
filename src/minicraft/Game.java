package minicraft;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
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
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftConnection;
import minicraft.network.MinicraftServer;
import minicraft.network.MinicraftClient;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.*;

public class Game extends Canvas implements Runnable {
	
	private static Random random = new Random();
	
	public static boolean debug = false;
	public static String localGameDir = "/.playminicraft/mods/Minicraft Plus";
	public static String systemGameDir;
	static {
		String os = System.getProperty("os.name");
		//System.out.println("os name: \"" +os + "\"");
		if(os.toUpperCase().contains("WINDOWS"))
			systemGameDir = System.getenv("APPDATA");
		else
			systemGameDir = System.getProperty("user.home");
		
		//System.out.println("system game dir: " + systemGameDir);
	}
	public static String gameDir; // The directory in which all the game files are stored
	//public static String loadDir = "";
	
	/// MANAGERIAL VARS AND RUNNING
	
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window
	public static final String VERSION = "2.0.1-dev6";
	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	private static float SCALE = 3;
	
	/// MULTIPLAYER
	public static boolean ISONLINE = false;
	public static boolean ISHOST = false; /// this being true doesn't mean this game isn't a client as well; becuase it should be.
	public boolean autoclient = false; // used in the init method; jumps to multiplayer menu as client
	public static MinicraftServer server = null;
	public static MinicraftClient client = null;
	
	/// TIME AND TICKS
	
	public static final int normSpeed = 60; // measured in ticks / second.
	public static float gamespeed = 1; // measured in MULTIPLES OF NORMSPEED.
	public boolean paused = true; // If the game is paused.
	
	public static int tickCount = 0; // The number of ticks since the beginning of the game day.
	public static int time = 0; // Facilites time of day / sunlight.
	public static final int dayLength = 64800; //this value determines how long one game day is.
	public static final int sleepEndTime = dayLength/8; //this value determines when the player "wakes up" in the morning.
	public static final int sleepStartTime = dayLength/2+dayLength/8; //this value determines when the player allowed to sleep.
	//public static int noon = 32400; //this value determines when the sky switches from getting lighter to getting darker.
	
	private static boolean running = false; // This is about more than simply being paused -- it keeps the game loop running.
	public int fra, tik; //these store the number of frames and ticks in the previous second; used for fps, at least.
	public static int gameTime = 0; // This stores the total time (number of ticks) you've been playing your game.
	
	/// RENDERING
	
	private BufferedImage image; // creates an image to be displayed on the screen.
	protected int[] pixels; // the array of pixels that will be displayed on the screen.
	private int[] colors; // All of the colors, put into an array.
	/// these are public, but should not be modified:
	public Screen screen; // Creates the main screen
	public Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	
	/// LEVEL AND PLAYER
	
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.
	
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
	
	public InputHandler input; // input used in Game, Player, and just about all the *Menu classes.
	public Menu menu; // the current menu you are on.
	public Player player; // The Player.
	//public Level level; // This is the current level you are on.
	static int worldSize = 128; // The size of the world
	public static int lvlw = worldSize; // The width of the world
	public static int lvlh = worldSize; // The height of the world
	
	protected int playerDeadTime; // the time after you die before the dead menu shows up.
	protected int pendingLevelChange; // used to determine if the player should change levels or not.
	public boolean gameOver; // If the player wins this is set to true.
	
	/// AUTOSAVE AND NOTIFICATIONS
	
	public static List<String> notifications = new ArrayList<String>();
	public static int notetick; // "note"= notifications.
	
	public static final int astime = 7200; //stands for Auto-Save Time (interval)
	public static int asTick; // The time interval between autosaves.
	public static boolean saving = false; // If the game is performing a save.
	public static int savecooldown; // Prevents saving many times too fast, I think.
	
	/// SCORE MODE
	
	public int multiplier = 1; // Score multiplier
	public static final int mtm = 300; // time given to increase multiplier before it goes back to 1.
	public int multipliertime = mtm; // Time left on the current multiplier.
	
	public static int scoreTime; // time remaining for score mode game.
	
	public static boolean pastDay1 = true; // used to prefent mob spawn on surface on day 1.
	public static boolean readyToRenderGameplay = false;
	
	public static enum Time {
		Morning (0),
		Day (Game.dayLength/4),
		Evening (Game.dayLength/2),
		Night (Game.dayLength/4*3);
		
		public int tickTime;
		private Time(int ticks) {
			tickTime = ticks;
		}
	}
	
	/// *** CONSTRUSTOR *** ///
	public Game() {
		input = new InputHandler(this);
		
		fra = 0; // the frames processed in the previous second
		tik = 0; // the ticks processed in the previous second
		
		colors = new int[256];
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		asTick = 0;
		notetick = 0;
		
		gameOver = false;
	}
	
	/*
	public static final byte[] intToBytes(int num) {
		byte[] bytes = new byte[4];
		bytes[0] = num >> (8*3);
		bytes[1] = num >> (8*2) & 0xff;
		bytes[2] = num >> 8 & 0xff;
		bytes[3] = num & 0xff;
	}
	
	public static final int bytesToInt(byte[] bytes) {
		if(bytes.length != 4) return 0;
		
		int num = 0;
		num += bytes[0] << 24;
		num += bytes[1] << 16;
		num += bytes[2] << 8;
		num += bytes[3];
	}
	*/
	
	// Sets the current menu.
	public void setMenu(Menu menu) {
		this.menu = menu;
		//if (debug) System.out.println("setting game menu to " + menu);
		if (menu != null) menu.init(this, input);
	}
	
	public static final boolean isValidClient() {
		return ISONLINE && client != null;
	}
	public static final boolean isConnectedClient() {
		return isValidClient() && client.isConnected();
	}
	
	public static final boolean isValidServer() {
		return ISONLINE && ISHOST && server != null;
	}
	public static final boolean hasConnectedClients() {
		return isValidServer() && server.hasClients();
	}
	
	/// called after main; main is at bottom.
	public void start() {
		running = true;
		new Thread(this).start(); //calls run()
	}
	
	// This is only called by GameApplet...
	public void stop() {
		running = false;
	}
	
	/**
	 * Initialization step, this is called when the game first starts. Sets up the screens.
	 */
	protected void init() {
		/* This sets up the screens, and loads the icons.png spritesheet. */
		try {
			screen = new Screen(WIDTH, HEIGHT, new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/icons.png"))));
			lightScreen = new Screen(WIDTH, HEIGHT, new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/icons.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		screen.pixels = pixels;
		
		Tiles.initTileList();
		
		resetGame(); // "half"-starts a new game, to set up initial variables
		player.eid = 0;
		new Load(this); // this loads any saved preferences.
		
		if(autoclient)
			setMenu(new MultiplayerMenu(this, "localhost"));
		else
			setMenu(new TitleMenu()); //sets menu to the title screen.
	}
	
	/** This method is used when respawning, and by initWorld to reset the vars. It does not generate any new terrain. */
	public void resetGame() {
		if(Game.debug) System.out.println("resetting game...");
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
			player = new RemotePlayer(this, true, (RemotePlayer)player);
		} else
			player = new Player(this, input);
		
		// "shouldRespawn" is false on hardcore, or when making a new world.
		if (DeadMenu.shouldRespawn) { // respawn, don't regenerate level.
			//if (debug) System.out.println("Current Level = " + currentLevel);
			
			Level level = levels[currentLevel];
			player.respawn(level);
			//if (debug) System.out.println("respawned player in current world");
			level.add(player); // adds the player to the current level (always surface here)
		}
	}
	
	/** This method is used to create a brand new world, or to load an existing one from a file. */
	/** For the loading screen updates to work, it it assumed that *this* is called by a thread *other* than the one rendering the current *menu*. */
	public void initWorld() { // this is a full reset; everything.
		if(Game.debug) System.out.println("resetting world...");
		
		if(Game.isValidServer()) {
			System.err.println("Cannot initialize world while acting as a server runtime; not running initWorld().");
			return;
		}
		
		DeadMenu.shouldRespawn = false;
		resetGame();
		Bed.inBed = false;
		gameTime = 0;
		Game.gamespeed = 1;
		
		changeTimeOfDay(Time.Morning); // resets tickCount; game starts in the day, so that it's nice and bright.
		gameOver = false;
		
		levels = new Level[6];
		
		scoreTime = ModeMenu.getScoreTime();
		
		LoadingMenu.percentage = 0; // this actually isn't necessary, I think; it's just in case.
		
		if(!isValidClient()) {
			if(WorldSelectMenu.loadworld)
				new Load(this, WorldSelectMenu.worldname);
			else {
				worldSize = WorldGenMenu.getSize();
				
				double loadingInc = 100.0 / (maxLevelDepth - minLevelDepth + 1); // the .002 is for floating point errors, in case they occur.
				for (int i = maxLevelDepth; i >= minLevelDepth; i--) {
					// i = level depth; the array starts from the top because the parent level is used as a reference, so it should be constructed first. It is expected that the highest level will have a null parent.
					
					levels[lvlIdx(i)] = new Level(this, worldSize, worldSize, i, levels[lvlIdx(i+1)], !WorldSelectMenu.loadworld);
				
					LoadingMenu.percentage += loadingInc;
				}
				
				// add an Iron lantern to level 5, at (984, 984), when making a new world
				Furniture f = new Lantern(Lantern.Type.IRON);//Items.get("Iron Lantern").furniture;
				f.x = 984;
				f.y = 984;
				levels[lvlIdx(-4)].add(f);
				
				Level level = levels[currentLevel]; // sets level to the current level (3; surface)
				pastDay1 = false;
				player.findStartPos(level); // finds the start level for the player
				level.add(player);
			}
			
			if(WorldGenMenu.get("Theme").equals("Hell")) {
				player.inventory.add(Items.get("lava potion"));
			}
			readyToRenderGameplay = true;
		} else {
			Game.levels = new Level[6];
			currentLevel = 3;
		}
		
		DeadMenu.shouldRespawn = true;
	}
	
	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public void tick() {
		Level level = levels[currentLevel];
		if (Bed.inBed && !Game.isValidClient()) {
			// IN BED
			level.remove(Bed.player);
			gamespeed = 20;
			if(tickCount > sleepEndTime) {
				pastDay1 = true;
				tickCount = 0;
			}
			if (tickCount <= sleepStartTime && tickCount >= sleepEndTime) { // it has reached morning.
				level.add(Bed.player);
				gamespeed = 1;
				
				// seems this removes all entities within a certain radius of the player when you get OUT of Bed.
				for (Entity e: level.getEntityArray()) {
					if (e.level == levels[currentLevel]) {
						int xd = Bed.player.x - e.x;
						int yd = Bed.player.y - e.y;
						if (xd * xd + yd * yd < 48 && e instanceof Mob && e != Bed.player) {
							// this comes down to a radius of about half a tile... huh...
							level.remove(e);
						}
					}
				}
				// finally gets out of bed.
				Bed.inBed = false;
				Bed.player = null;
			}
		}
		
		
		//auto-save tick; marks when to do autosave.
		if(!paused || isValidServer())
			asTick++;
		if (asTick > astime) {
			if (OptionsMenu.autosave && player.health > 0 && !gameOver) {
				if(!Game.ISONLINE)
					new Save(player, WorldSelectMenu.worldname);
				else if(Game.isValidServer())
					Game.server.saveWorld();
			}
			
			asTick = 0;
		}
		
		// Increment tickCount if the game is not paused
		if (!paused || isValidServer()) setTime(tickCount+1);
		if (tickCount == 3600) level.removeAllEnemies();
		
		/// SCORE MODE ONLY
		
		if (ModeMenu.score && (!paused || isValidServer() && !gameOver)) {
			if (scoreTime <= 0) { // GAME OVER
				gameOver = true;
				setMenu(new WonMenu(player));
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
			//boolean ticked = false;
			for(Level floor: levels) {
				if(floor == null) continue;
				//if(floor.getEntitiesOfClass(Player.class).length > 0) {
					//if(Game.debug) System.out.println("Server is ticking level " + floor.depth);
					floor.tick();
				//	ticked = true;
				//}
			}
			
			/*if(!ticked) {
				System.out.println("SERVER did not tick any levels, becuase no players were found.");
			}*/
			
			Tile.tickCount++;
		}// else if(isValidClient())
		//	player.tick();
		
		// This is the general action statement thing! Regulates menus, mostly.
		if (!hasFocus()) {
			input.releaseAll();
		}
		if(hasFocus() || ISONLINE) {
			if ((isValidServer() || !player.removed) && !gameOver) {
				gameTime++;
			}
			
			if(!isValidServer() || menu != null && !hadMenu)
				input.tick(); // INPUT TICK; no other class should call this, I think...especially the *Menu classes.
			
			if (menu != null) {
				//a menu is active.
				menu.tick();
				paused = true;
			} else {
				//no menu, currently.
				paused = false;
				
				if(!Game.isValidServer()) {
					//if player is alive, but no level change, nothing happens here.
					if (player.removed && readyToRenderGameplay) {
						//makes delay between death and death menu.
						//if (debug) System.out.println("player is dead.");
						playerDeadTime++;
						if (playerDeadTime > 60) {
							setMenu(new DeadMenu());
						}
					} else if (pendingLevelChange != 0) {
						setMenu(new LevelTransitionMenu(pendingLevelChange));
						pendingLevelChange = 0;
					}
					
					if(!isValidServer() && level != null) {
						level.tick();
						Tile.tickCount++;
					}
					
					if(isValidClient())
						player.tick();
				}
				else if(Game.isValidServer()) {
					// here is where I should put things like select up/down, backspace to boot, esc to open pause menu, etc.
					if(input.getKey("pause").clicked)
						setMenu(new PauseMenu(null));
				}
				
				//for debugging only
				if (debug) {
					
					if(!ISONLINE || isValidServer()) {
						/// server-only cheats.
						if (input.getKey("Shift-r").clicked)
							initWorld(); // this will almost certainaly break in multiplayer, i think...
						
						if (input.getKey("1").clicked) changeTimeOfDay(Time.Morning);
						if (input.getKey("2").clicked) changeTimeOfDay(Time.Day);
						if (input.getKey("3").clicked) changeTimeOfDay(Time.Evening);
						if (input.getKey("4").clicked) changeTimeOfDay(Time.Night);
						
						if (input.getKey("creative").clicked) ModeMenu.updateModeBools(2);
						if (input.getKey("survival").clicked) ModeMenu.updateModeBools(1);
						if (input.getKey("shift-t").clicked) ModeMenu.updateModeBools(4);
						if (ModeMenu.score && input.getKey("ctrl-t").clicked) scoreTime = normSpeed * 5; // 5 seconds
						
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
						if(input.getKey("shift-j").clicked) {
							levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Down"));
						}
						
						if(input.getKey("ctrl-p").clicked) {
							/// list all the remote players in the level and their coordinates.
							//System.out.println("searching for players on current level...");
							levels[currentLevel].printEntityLocs(Player.class);
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
	
	public void setMultiplier(int value) {
		multiplier = value;
		multipliertime = mtm;
	}
	public void addMultiplier(int value) {
		multiplier += value;
		multipliertime = mtm - 5;
	}
	
	/// this is the proper way to change the tickCount.
	public static void setTime(int ticks) {
		//if(Game.debug && Game.isConnectedClient()) System.out.println("setting time to " + ticks);
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
		else if(isValidClient())
			Game.client.sendNotification(msg, notetick);
	}
	
	/** This method changes the level that the player is currently on.
	 * It takes 1 integer variable, which is used to tell the game which direction to go.
	 * For example, 'changeLevel(1)' will make you go up a level,
	 	while 'changeLevel(-1)' will make you go down a level. */
	public void changeLevel(int dir) {
		if(Game.isValidServer()) {
			System.out.println("server tried to change level.");
			return;
		}
		levels[currentLevel].remove(player); // removes the player from the current level.
		
		currentLevel += dir;
		if (currentLevel == -1) currentLevel = 5; // fix accidental level underflow
		if (currentLevel == 6) currentLevel = 0; // fix accidental level overflow
		//level = levels[currentLevel]; // sets the level to the current level
		
		player.x = (player.x >> 4) * 16 + 8; // sets the player's x coord (to center yourself on the stairs)
		player.y = (player.y >> 4) * 16 + 8; // sets the player's y coord (to center yourself on the stairs)
		
		if(isValidClient() && levels[currentLevel] == null)
			Game.client.requestLevel(currentLevel);
		else
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
	private char[] dots = "   ".toCharArray();
	
	/// just a little thing to make a progressive dot elipses.
	private String getElipses() {
		//String dots = "";
		int time = tickCount % normSpeed; // sets the "dot clock" to normSpeed.
		int interval = normSpeed / 2; // specifies the time taken for each fill up and empty of the dots.
		int epos = (time % interval) / (interval/dots.length); // transforms time into a number specifying which part of the dots array it is in, by index.
		char set = time < interval ? '.' : ' '; // get the character to set in this cycle.
		
		dots[epos] = set;
		/*
		for(int i = 0; i < 3; i++) {
			if (epos == 1) dots += chars[0];
		}
		*/
		/*eposTick++;
		if(eposTick >= Game.normSpeed) {
			eposTick = 0;
			//ePos++;
		}*/
		//if(ePos >= 3) ePos = 0;
		
		return new String(dots);
	}
	
	/** renders the current screen */
	//called in game loop, a bit after tick()
	public void render() {
		BufferStrategy bs = null;
		bs = getBufferStrategy(); // creates a buffer strategy to determine how the graphics should be buffered.
		if (bs == null) {
			createBufferStrategy(3); // if the buffer strategy is null, then make a new one!
			requestFocus(); // requests the focus of the screen.
			return;
		}
		
		if(readyToRenderGameplay) {
			if(Game.isValidServer()) {
				screen.clear(0);
				Font.drawCentered("Awaiting client connections"+getElipses(), screen, 10, Color.get(-1, 444));
				Font.drawCentered("So far:", screen, 20, Color.get(-1, 444));
				int i = 0;
				for(String name: Game.server.getClientNames()) {
					Font.drawCentered(name, screen, 30+i*10, Color.get(-1, 134));
					i++;
				}
			}
			else {
				renderLevel();
				renderGui();
			}
		}
		
		if (menu != null) // renders menu, if present.
			menu.render(screen);
		
		if (!hasFocus() && !Game.ISONLINE) renderFocusNagger(); // calls the renderFocusNagger() method, which creates the "Click to Focus" message.
		
		Graphics g = bs.getDrawGraphics(); // gets the graphics in which java draws the picture
		g.fillRect(0, 0, getWidth(), getHeight()); // draws the a rect to fill the whole window (to cover last?)
		
		// scales the pixels.
		int ww = getWindowSize().width;
		int hh = getWindowSize().height;
		// gets the image offset.
		int xo = (getWidth() - ww) / 2 + getParent().getInsets().left;
		int yo = (getHeight() - hh) / 2 + getParent().getInsets().top;
		g.drawImage(image, xo, yo, ww, hh, null); //draws the image on the window
		g.dispose(); // releases any system items that are using this method. (so we don't have crappy framerates)
		bs.show(); // makes the picture visible. (probably)
	}
	
	private void renderLevel() {
		Level level = Game.levels[currentLevel];
		if(level == null) return;
		
		int xScroll = player.x - screen.w / 2; // scrolls the screen in the x axis.
		int yScroll = player.y - (screen.h - 8) / 2; // scrolls the screen in the y axis.
		
		//if (debug && isValidClient()) System.out.println("player coords at render: "+player.x+","+player.y);
		
		//stop scrolling if the screen is at the ...
		if (xScroll < 0) xScroll = 0; // ...left border.
		if (yScroll < 0) yScroll = 0; // ...top border.
		if (xScroll > level.w * 16 - screen.w) xScroll = level.w * 16 - screen.w; // ...right border.
		if (yScroll > level.h * 16 - screen.h) yScroll = level.h * 16 - screen.h; // ...bottom border.
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
		if (currentLevel != 5 && (currentLevel != 3 || tickCount < dayLength/4 || tickCount > dayLength/2) && (!ModeMenu.creative || currentLevel >= 3)) {
			lightScreen.clear(0); // this doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
			int brightnessMultiplier = player.potioneffects.containsKey(PotionType.Light) ? 12 : 8; // brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, currentLevel, xScroll, yScroll); // overlays the light screen over the main screen.
		}
	}
	
	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private void renderGui() {
		/// AH-HA! THIS DRAWS THE BLACK SQUARE!!
		for (int x = 12; x < 29; x++) {
			screen.render(x * 7, screen.h - 8, 0 + 1 * 32, Color.get(0, 0), 0);
		}
		
		int textcol = Color.get(-1, 555);
		if (player.showinfo) { // renders show debug info on the screen.
			ArrayList<String> info = new ArrayList<String>();
			info.add("VERSION " + VERSION);
			info.add(fra + " fps");
			info.add("day tiks " + tickCount);
			info.add((normSpeed * gamespeed) + " tik/sec");
			info.add("walk spd " + player.moveSpeed);
			info.add("X " + (player.x >> 4));
			info.add("Y " + (player.y >> 4));
			info.add("Tile " + Game.levels[currentLevel].getTile(player.x>>4, player.y>>4).name);
			if (ModeMenu.score) info.add("Score " + player.score);
			
			info.add("Mob Cnt " + Game.levels[currentLevel].mobCount);
			
			
			/// Displays number of chests left, if on dungeon level.
			if (currentLevel == 5) {
				if (levels[currentLevel].chestcount > 0) {
					info.add("Chests: " + levels[currentLevel].chestcount);
				} else {
					info.add("Chests: Complete!");
				}
			}
			
			if(player.armor > 0) {
				info.add("armor: " + player.armor);
				info.add("dam buffer: " + player.armorDamageBuffer);
			}
			
			//info.add("steps: " + player.stepCount);
			info.add("hungerstam:" + player.hungerStamCnt);
			
			FontStyle style = new FontStyle(textcol).setShadowType(Color.get(-1, 000), true).setXPos(1);
			for(int i = 0; i < info.size(); i++) {
				style.setYPos(2 + i*10).draw(info.get(i), screen);
			}
		}
		
		// This is the arrow counter. ^ = infinite symbol.
		int ac = player.inventory.count(Items.get("arrow"));
		if (ModeMenu.creative || ac >= 10000)
			Font.draw("	x" + "^", screen, 84, screen.h - 16, Color.get(0, 333, 444, 555));
		else
			Font.draw("	x" + ac, screen, 84, screen.h - 16, Color.get(0, 555));
		//displays arrow icon
		screen.render(10 * 8 + 4, screen.h - 16, 13 + 5 * 32, Color.get(0, 111, 222, 430), 0);
		
		String msg = "";
		if (saving) msg = "Saving... " + Math.round(LoadingMenu.percentage) + "%";
		else if (Bed.inBed) msg = "Sleeping...";
		
		if(msg.length() > 0)
			new FontStyle(Color.get(-1, 555)).setYPos(screen.h / 2 - 20).setShadowType(Color.get(-1, 222), false).draw(msg, screen);
		
		/// NOTIFICATIONS
		
		if (notifications.size() > 0 && msg.length() == 0) {
			notetick++;
			if (notifications.size() > 3) { //only show 3 notifs max at one time; erase old notifs.
				notifications = notifications.subList(notifications.size() - 3, notifications.size());
			}
			
			if (notetick > 600) { //display time per notification.
				notifications.remove(0);
				notetick = 0;
			}
			
			// draw each current notification, with shadow text effect.
			FontStyle style = new FontStyle(Color.get(-1, 555)).setShadowType(Color.get(-1, 222), false);
			for (int i = 0; i < notifications.size(); i++) {
				String note = ((String) notifications.get(i));
				//int x = screen.w / 2 - note.length() * 8 / 2,
				int y = screen.h - 120 - notifications.size()*8 + i * 8;
				style.setYPos(y).draw(note, screen);
				//Font.draw(note, screen, x, y, Color.get(-1, 555), Color.get(-1, 111));
			}
		}
		
		// SCORE MODE ONLY:
		
		if (ModeMenu.score) {
			int seconds = (int)Math.ceil(scoreTime / (double)normSpeed);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;
			
			int timeCol;
			if(scoreTime >= 18000) timeCol = Color.get(0, 555);
			else if (scoreTime >= 3600) timeCol = Color.get(330, 555);
			else timeCol = Color.get(400, 555);
			
			Font.draw("Time left " + (hours > 0 ? hours+"h ":"") + minutes + "m " + seconds + "s", screen, screen.w/2-9*8, 2, timeCol);
			
			String scoreString = "Current score: " + player.score;
			Font.draw(scoreString, screen, screen.w - Font.textWidth(scoreString)-2, 3 + 8, Color.get(-1, 555));
			
			if(multiplier > 1) {
				int multColor = multiplier < 50 ? Color.get(-1, 540) : Color.get(-1, 500);
				String mult = "X" + multiplier;
				Font.draw(mult, screen, screen.w-Font.textWidth(mult)-2, 4 + 2*8, multColor);
			}
		}

		// FISHING ROD STATUS
		if (player.activeItem instanceof ToolItem && ((ToolItem)player.activeItem).type == ToolType.FishingRod) {
			int dura = ((ToolItem)player.activeItem).dur * 100 / ((ToolItem)player.activeItem).type.durability;
			//if (dura > 100) dura = 100;
			Font.draw(dura + "%", screen, 164, screen.h - 16, Color.get(0, 30));
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
				Font.draw(pType + " (" + (pTime / 60) + ":" + (pTime % 60) + ")", screen, 180, 17 + i * 8, pcol);
			}
		}
		
		
		// This is the status icons, like health hearts, stamina bolts, and hunger "burgers".
		if (!ModeMenu.creative) {
			for (int i = 0; i < 10; i++) {
				int color;
				
				// renders armor
				int armor = player.armor*10/player.maxArmor;
				color = (i <= armor && player.curArmor != null) ? player.curArmor.sprite.color : Color.get(-1, -1);
				screen.render(i * 8, screen.h - 24, 3 + 12 * 32, color, 0);
				
				// renders your current red hearts, or black hearts for damaged health.
				color = (i < player.health) ? Color.get(-1, 200, 500, 533) : Color.get(-1, 100, 000, 000);
				screen.render(i * 8, screen.h - 16, 0 + 12 * 32, color, 0);
				
				if (player.staminaRechargeDelay > 0) {
					// creates the white/gray blinking effect when you run out of stamina.
					color = (player.staminaRechargeDelay / 4 % 2 == 0) ? Color.get(-1, 555, 000, 000) : Color.get(-1, 110, 000, 000);
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, color, 0);
				} else {
					// renders your current stamina, and uncharged gray stamina.
					color = (i < player.stamina) ? Color.get(-1, 220, 550, 553) : Color.get(-1, 110, 000, 000);
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, color, 0);
				}
				
				// renders hunger
				color = (i < player.hunger) ? Color.get(-1, 100, 530, 211) : Color.get(-1, 100, 000, 000);
				screen.render(i * 8 + (screen.w - 80), screen.h - 16, 2 + 12 * 32, color, 0);
			}
		}
		
		/// CURRENT ITEM
		if (player.activeItem != null) // shows active item sprite and name in bottom toolbar, if one exists.
			player.activeItem.renderInventory(screen, 12 * 7, screen.h - 8, false);
	}
	
	/** Renders the "Click to focus" box when you click off the screen. */
	private void renderFocusNagger() {
		String msg = "Click to focus!"; // the message when you click off the screen.
		paused = true; //perhaps paused is only used for this.
		int xx = Font.centerX(msg, 0, screen.w); // the width of the box
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
	public void scheduleLevelChange(int dir) {
		// same as changeLevel(). Call scheduleLevelChange(1) if you want to go up 1 level,
		// or call with -1 to go down by 1.
		if(!Game.isValidServer())
			pendingLevelChange = dir;
	}
	
	public void startMultiplayerServer() {
		// here is where we need to start the new client.
		String jarFilePath = "";
		try {
			jarFilePath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch(URISyntaxException ex) {
			System.err.println("problem with jar file URI syntax.");
			ex.printStackTrace();
		}
		List<String> arguments = new ArrayList<String>();
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
			System.err.println("problem starting new jar file process.");
			ex.printStackTrace();
		}
		
		// now that that's done, let's turn *this* running JVM into a server:
		server = new MinicraftServer(this);
		if (debug) System.out.println("server started. valid: " + isValidServer() + "; is online: "+ISONLINE+"; is host:" + ISHOST + "; server not null: " + (server!=null));
	}
	
	/**
	 * This is the main loop that runs the game. It:
	 *	-keeps track of the amount of time that has passed
	 *	-fires the ticks needed to run the game
	 *	-fires the command to render out the screen.
	 */
	public void run() {
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		
		//calls setMenu with new TitleMenu (and does other things)
		init();
		
		//main game loop? calls tick() and render().
		while (running) {
			long now = System.nanoTime();
			double nsPerTick = 1E9D / normSpeed; // nanosecs per sec divided by ticks per sec = nanosecs per tick
			if(menu == null) nsPerTick /= gamespeed;
			unprocessed += (now - lastTime) / nsPerTick; //figures out the unprocessed time between now and lastTime.
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) { // If there is unprocessed time, then tick.
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
			
			if (shouldRender) {
				frames++;
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
	}
	
	/// * The main method! * ///
	public static void main(String[] args) {
		
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				String exceptionTrace = "Exception in thread " + t + ":P\n";
				exceptionTrace += Game.getExceptionTrace(e);
				System.err.println(exceptionTrace);
				javax.swing.JOptionPane.showInternalMessageDialog(null, exceptionTrace, "Fatal Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		});
		
		
		boolean debug = false;
		boolean autoclient = false;
		
		String saveDir = Game.systemGameDir;
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("--debug"))
				debug = true;
			if(args[i].equals("--savedir") && i+1 < args.length)
				saveDir = args[i+1];
			if(args[i].equals("--localclient"))
				autoclient = true;
		}
		Game.debug = debug;
		if(Game.debug) System.out.println("determined save folder: " + saveDir);
		Game.gameDir = saveDir + Game.localGameDir;
		
		/*loadDir = gameDir + "/saves/";
		File testFile = new File(loadDir);
		if(!testFile.exists()) {
			// load from the previous folder instead.
			String newlocation = System.getenv("APPDATA")+"/"+Game.localGameDir + "/saves/";
			if((new File(newlocation)).exists())
				loadDir = newlocation;
		}
		if(Game.debug) System.out.println("load dir: " + loadDir);
		*/
		Game game = new Game();
		game.setMinimumSize(new Dimension(1, 1));
		game.setPreferredSize(getWindowSize());
		JFrame frame = new JFrame(Game.NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout()); // sets the layout of the window
		frame.add(game, BorderLayout.CENTER); // Adds the game (which is a canvas) to the center of the screen.
		frame.pack(); //squishes everything into the preferredSize.
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
			public void windowClosed(WindowEvent e) {
				if(Game.isValidClient())
					Game.client.endConnection();
				if(Game.isValidServer())
					Game.server.endConnection();
				
				game.quit();
			}
			public void windowClosing(WindowEvent e) {}
		});
		
		frame.setVisible(true);
		
		game.autoclient = autoclient; // this will make the game automatically jump to the MultiplayerMenu, and attempt to connect to localhost.
		
		game.start(); // Starts the game!
	}
	
	public static Dimension getWindowSize() {
		return new Dimension(new Float(WIDTH * SCALE).intValue(), new Float(HEIGHT * SCALE).intValue());
	}
	
	public void quit() {
		running = false;
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
		String exceptionStr = "";
		try {
			exceptionStr = bytestream.toString("UTF-8");
		}
		catch(Exception ex) {
			exceptionStr = "Unavailable";
		}
		return exceptionStr;
	}
}
