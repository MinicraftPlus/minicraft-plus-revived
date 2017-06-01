package minicraft;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*; // TODO these .*'s are unnecessary.
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
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
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftServer;
import minicraft.network.MinicraftClient;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.*;

public class Game extends Canvas implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private static Random random = new Random();
	
	public static boolean debug = false;
	public static String gameDir = "/.playminicraft/mods/Minicraft Plus"; // The directory in which all the game files are stored; APPDATA is meant for windows...
	
	/// MANAGERIAL VARS AND RUNNING
	
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window
	public static final String VERSION = "2.0.0-dev1";
	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	private static float SCALE = 3;
	
	/// MULTIPLAYER
	public static boolean ISONLINE = false;
	public static boolean ISHOST = false;
	// used by server runtime only:
	public static MinicraftServer server = null;
	public List<RemotePlayer> remotePlayers = null;
	// used by client runtimes only:
	public static MinicraftClient client = null;
	
	/// TIME AND TICKS
	
	public static final int normSpeed = 60; // measured in ticks / second.
	public static float gamespeed = 1; // measured in MULTIPLES OF NORMSPEED.
	public static boolean paused = false; // If the game is paused.
	
	public static int tickCount = 0; // The number of ticks since the beginning of the game day.
	public static int time = 0; // Facilites time of day / sunlight.
	public static int dayLength = 64800; //this value determines how long one game day is.
	public static int sleepEndTime = dayLength/8; //this value determines when the player "wakes up" in the morning.
	public static int sleepStartTime = dayLength/2+dayLength/8; //this value determines when the player allowed to sleep.
	//public static int noon = 32400; //this value determines when the sky switches from getting lighter to getting darker.
	
	
	private boolean running; // This is about more than simply being paused -- it keeps the game loop running.
	public int fra, tik; //these store the number of frames and ticks in the previous second; used for fps, at least.
	public int gameTime; // This stores the total time (number of ticks) you've been playing your game.
	
	/// RENDERING
	
	private BufferedImage image; // creates an image to be displayed on the screen.
	private int[] pixels; // the array of pixels that will be displayed on the screen.
	private int[] colors; // All of the colors, put into an array.
	/// these are public, but should not be modified:
	public Screen screen; // Creates the main screen
	public Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	
	/// LEVEL AND PLAYER
	
	public static Level[] levels = new Level[6]; // This array stores the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It defaults to 3, the surface.
	
	public InputHandler input; // input used in Game, Player, and just about all the *Menu classes.
	public Menu menu; // the current menu you are on.
	public Player player; // The Player.
	public Level level; // This is the current level you are on.
	int worldSize; // The size of the world
	
	private int playerDeadTime; // the time after you die before the dead menu shows up.
	private int pendingLevelChange; // used to determine if the player should change levels or not.
	//private int wonTimer; // the paused time when you win before the win menu shows up.
	public boolean gameOver; // If the player wins this is set to true.
	
	/// AUTOSAVE AND NOTIFICATIONS
	
	public static int astime; //stands for Auto-Save Time (interval)
	public static List<String> notifications = new ArrayList<String>();
	
	public int asTick; // The time interval between autosaves.
	public boolean saving; // If the game is performing a save.
	public int savecooldown; // Prevents saving many times too fast, I think.
	public int notetick; // "note"= notifications.
	
	/// SCORE MODE
	
	public static int multiplier = 1; // Score multiplier
	public static int mtm = 300; // time given to increase multiplier before it goes back to 1.
	public static int multipliertime = mtm; // Time left on the current multiplier.
	
	public int scoreTime; // time remaining for score mode game.
	//public int newscoreTime; // time you start with in score mode.
	
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
		running = false;
		input = new InputHandler(this);
		gameTime = 0;
		
		fra = 0; // the frames processed in the previous second
		tik = 0; // the ticks processed in the previous second
		
		colors = new int[256];
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		worldSize = 128;
		
		//newscoreTime = 72000;
		//scoreTime = newscoreTime;
		
		asTick = 0;
		astime = 7200;
		saving = false;
		notetick = 0;
		
		//wonTimer = 0;
		gameOver = false;
	}
	
	// Sets the current menu.
	public void setMenu(Menu menu) {
		this.menu = menu;
		if (debug) System.out.println("setting game menu to " + menu);
		if (menu != null) menu.init(this, input);
	}
	
	public boolean isValidClient() {
		return ISONLINE && !ISHOST && client != null/* && client.done*/;
	}
	public boolean isValidHost() {
		return ISONLINE && ISHOST && server != null/* && server.threadList.size() > 0*/; // i'm debating that last part.
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
	private void init() {
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
		new Load(this); // this loads any saved preferences.
		setMenu(new TitleMenu()); //sets menu to the title screen.
	}
	
	/** This method is used when respawning, and by resetstartGame to reset the vars. It does not generate any new terrain. */
	public void resetGame() {
		playerDeadTime = 0;
		Bed.inBed = false;
		currentLevel = 3;
		asTick = 0;
		notifications.clear();
		
		// adds a new player
		player = new Player(this, input);
		
		// "shouldRespawn" is false on hardcore, or when making a new world.
		if (DeadMenu.shouldRespawn) { // respawn, don't regenerate level.
			//if (debug) System.out.println("Current Level = " + currentLevel);
			
			level = levels[currentLevel];
			player.respawn(level);
			//if (debug) System.out.println("respawned player in current world");
			level.add(player); // adds the player to the current level (always surface here)
		}
	}
	
	/** This method is used to create a brand new world, or to load an existing one from a file. */
	public void resetstartGame() { // this is a full reset; everything.
		DeadMenu.shouldRespawn = false;
		resetGame();
		gameTime = 0;
		Game.gamespeed = 1;
		
		changeTimeOfDay(Time.Morning); // resets tickCount; game starts in the day, so that it's nice and bright.
		gameOver = false;
		
		levels = new Level[6];
		
		scoreTime = ModeMenu.getScoreTime();
		
		LoadingMenu.percentage = 0; // this actually isn't necessary, I think; it's just in case.
		
		if(!ISONLINE) {
			
			if(!WorldSelectMenu.loadworld) {
				worldSize = WorldGenMenu.getSize();
				for (int i = 5; i >= 0; i--) {
					LoadingMenu.percentage = (5-i)*20;
					
					levels[(i - 1 < 0 ? 5 : i - 1)] =
							new Level(worldSize, worldSize, i - 4, (i == 5 ? (Level) null : levels[i]), !WorldSelectMenu.loadworld);
				}
				
				// if resetStartGame is called when not loading a world, add an Iron lantern to level 5, at (984, 984).
				Furniture f = new Lantern(Lantern.Type.IRON);//Items.get("Iron Lantern").furniture;
				f.x = 984;
				f.y = 984;
				levels[5].add(f);
			}
			else
			 	new Load(this, WorldSelectMenu.worldname);
			
			level = levels[currentLevel]; // sets level to the current level (3; surface)
			
			if (!WorldSelectMenu.loadworld) {
				pastDay1 = false;
				player.findStartPos(level); // finds the start level for the player
				level.add(player);
			}
		} else {
			level = null;
		}
		
		DeadMenu.shouldRespawn = true;
		
		if(WorldGenMenu.get("Theme").equals("Hell")) {
			player.inventory.add(Items.get("lava potion"));
		}
		
		//System.out.println("reset started game");
		readyToRenderGameplay = true;
	}
	
	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public void tick() {
		if(isValidClient() && client.isConnected()) {
			//System.out.println("running short tick; sending input");
			
			if (!hasFocus()) {
				input.releaseAll();
			}
			else {
				input.tick();
				
				if(menu != null) {
					paused = true;
					menu.tick();
				} else paused = false;
			}
			
			// TODO I need to modify the player class so that remote players don't register pause, and the player as a client won't register in-game menus like containers and such.
			if(menu == null)
				client.sendCachedInput();
			
			tickCount++;
			
			return;
		}
		
		if (Bed.inBed) {
			// IN BED
			level.remove(Bed.player);
			gamespeed = 30;
			if (tickCount <= sleepStartTime && tickCount >= sleepEndTime) { // it has reached morning.
				level.add(Bed.player);
				gamespeed = 1;
				
				// seems this removes all entities within a certain radius of the player when you get OUT of Bed.
				for (int i = 0; i < level.entities.size(); i++) {
					if (((Entity) level.entities.get(i)).level == levels[currentLevel]) {
						int xd = Bed.player.x - ((Entity) level.entities.get(i)).x;
						int yd = Bed.player.y - ((Entity) level.entities.get(i)).y;
						if (xd * xd + yd * yd < 48 // this comes down to a radius of about half a tile... huh...
								&& level.entities.get(i) instanceof Mob
								&& level.entities.get(i) != Bed.player) {
							level.remove((Entity) level.entities.get(i));
						}
					}
				}
				// finally gets out of bed.
				Bed.inBed = false;
				Bed.player = null;
			}
		}
		
		
		//auto-save tick; marks when to do autosave.
		if(!paused || isValidHost())
			asTick++;
		if (asTick > astime) {
			if (OptionsMenu.autosave && player.health > 0 && !gameOver
				  && levels[currentLevel].entities.contains(player)) {
				new Save(player, WorldSelectMenu.worldname);
			}
			
			asTick = 0;
		}
		
		// Increment tickCount if the game is not paused
		if (!paused || isValidHost()) setTime(tickCount+1);
		if (tickCount == 3600) level.removeAllEnemies();
		
		/// SCORE MODE ONLY
		
		if (ModeMenu.score && (!paused || isValidHost())) {
			if (scoreTime <= 0) { // GAME OVER
				gameOver = true;
				setMenu(new WonMenu(player));
			}
			
			scoreTime--;
			
			if (multiplier > 1) {
				if (multipliertime != 0) multipliertime--;
				if (multipliertime == 0) setMultiplier(1);
			}
			if (multiplier > 50) multiplier = 50;
		}
		
		//This is the general action statement thing! Regulates menus, mostly.
		if (!hasFocus()) {
			input.releaseAll();
		} else {
			if (!player.removed && !gameOver) {
				gameTime++;
			}
			input.tick(); //INPUT TICK; no other class should call this, I think...especially the *Menu classes.
			
			if (menu != null) {
				//a menu is active.
				menu.tick();
				paused = true;
			} else {
				//no menu, currently.
				paused = false;
				
				//if player is alive, but no level change, nothing happens here.
				if (player.removed) {
					//makes delay between death and death menu.
					playerDeadTime++;
					if (playerDeadTime > 60) {
						setMenu(new DeadMenu());
					}
				} else if (pendingLevelChange != 0) {
					setMenu(new LevelTransitionMenu(pendingLevelChange));
					pendingLevelChange = 0;
				}
				
				level.tick();
				Tile.tickCount++;
				
				//for debugging only
				if (debug) {
					if (input.getKey("Shift-0").clicked)
						resetstartGame();
					
					if (input.getKey("1").clicked) changeTimeOfDay(Time.Morning);
					if (input.getKey("2").clicked) changeTimeOfDay(Time.Day);
					if (input.getKey("3").clicked) changeTimeOfDay(Time.Evening);
					if (input.getKey("4").clicked) changeTimeOfDay(Time.Night);
					
					// this should not be needed, since the inventory should not be altered.
					if (input.getKey("shift-g").clicked) {
						Items.fillCreativeInv(player.inventory);
					}
					
					if(input.getKey("ctrl-h").clicked) player.health--;
					
					if (input.getKey("creative").clicked) ModeMenu.updateModeBools(2);
					if (input.getKey("survival").clicked) ModeMenu.updateModeBools(1);
					if (input.getKey("shift-t").clicked) ModeMenu.updateModeBools(4);
					if (ModeMenu.score && input.getKey("ctrl-t").clicked) scoreTime = normSpeed * 5; // 5 seconds
					
					if (input.getKey("equals").clicked) player.moveSpeed++;//= 0.5D;
					if (input.getKey("minus").clicked && player.moveSpeed > 1) player.moveSpeed--;// -= 0.5D;
					
					if (input.getKey("shift-equals").clicked) {
						if(gamespeed < 1) gamespeed *= 2;
						else if(normSpeed*gamespeed < 2000) gamespeed++;
					}
					if (input.getKey("shift-minus").clicked) {
						if(gamespeed > 1) gamespeed--;
						else if(normSpeed*gamespeed > 5) gamespeed /= 2;
					}
					
					if(input.getKey("shift-space").clicked) {
						int tx = player.x >> 4;
						int ty = player.y >> 4;
						System.out.println("current tile: " + levels[currentLevel].getTile(tx, ty).name);
					}
					if(input.getKey("shift-u").clicked) {
						levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Up"));
					}
					if(input.getKey("shift-j").clicked) {
						levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Down"));
					}
				} // end debug only cond.
			} // end "menu-null" conditional
		} // end hasfocus conditional
		
		if(isValidHost() && !hasFocus()) {
			/// this is to keep the game going while online, but with an unfocused window.
			
			/// ticks all the levels with a player on them
			for(Level level: levels)
				if(level.getEntities(Player.class).length > 0)
					level.tick();
			
			Tile.tickCount++;
			if (!player.removed && !gameOver) {
				gameTime++;
			}
		}
		
	} // end tick()
	
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
	
	/** This method changes the level that the player is currently on.
	 * It takes 1 integer variable, which is used to tell the game which direction to go.
	 * For example, 'changeLevel(1)' will make you go up a level,
	 	while 'changeLevel(-1)' will make you go down a level. */
	public void changeLevel(int dir) {
		level.remove(player); // removes the player from the current level.
		currentLevel += dir; // changes the current level by the amount
		if (currentLevel == -1) currentLevel = 5; // fix accidental level underflow
		if (currentLevel == 6) currentLevel = 0; // fix accidental level overflow
		
		level = levels[currentLevel]; // sets the level to the current level
		player.x = (player.x >> 4) * 16 + 8; // sets the player's x coord (to center yourself on the stairs)
		player.y = (player.y >> 4) * 16 + 8; // sets the player's y coord (to center yourself on the stairs)
		level.add(player); // adds the player to the level.
	}
	
	/** renders the current screen */
	//called in game loop, a bit after tick()
	public void render() {
		BufferStrategy bs = getBufferStrategy(); // creates a buffer strategy to determine how the graphics should be buffered.
		if (bs == null) {
			createBufferStrategy(3); // if the buffer strategy is null, then make a new one!
			requestFocus(); // requests the focus of the screen.
			return;
		}
		
		if(readyToRenderGameplay) {
			if(isValidClient() && client.isConnected()) {
				if(client.pixels != null) {
					//System.out.println("rendering new pixels from server on-screen...");
					screen.render(client.pixels); // this just overwrites all the pixels in screen.pixels
					client.pixels = null;
				}
			} else {
				renderLevel();
				renderGui();
				
				if(isValidHost() && server.isConnected()) {
					for(int i = 0; i < server.threadList.size(); i++) {
						RemotePlayer player = server.threadList.get(i).player;
						renderLevelAs(player, player.screen, player.lightScreen);
						renderGuiAs(player, player.screen);
						server.threadList.get(i).sendScreenPixels(player.screen.pixels);
					}
				}
			}
		}
		
		if (menu != null) // renders menu, if present.
			menu.render(screen);
		
		if (!hasFocus()) renderFocusNagger(); // calls the renderFocusNagger() method, which creates the "Click to Focus" message.
		
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
	
	private void renderLevel() { renderLevelAs(player, screen, lightScreen); }
	private void renderLevelAs(Player player, Screen screen, Screen lightScreen) {
		Level level = player.level;
		
		int xScroll = player.x - screen.w / 2; // scrolls the screen in the x axis.
		int yScroll = player.y - (screen.h - 8) / 2; // scrolls the screen in the y axis.
		
		//stop scrolling if the screen is at the ...
		if (xScroll < 16) xScroll = 16; // ...left border.
		if (yScroll < 16) yScroll = 16; // ...top border.
		if (xScroll > level.w * 16 - screen.w - 16) xScroll = level.w * 16 - screen.w - 16; // ...right border.
		if (yScroll > level.h * 16 - screen.h - 16) yScroll = level.h * 16 - screen.h - 16; // ...bottom border.
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
			level.renderLight(lightScreen, xScroll, yScroll); // finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, xScroll, yScroll); // overlays the light screen over the main screen.
		}
	}
	
	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private void renderGui() { renderGuiAs(player, screen); }
	private void renderGuiAs(Player player, Screen screen) {
		/// AH-HA! THIS DRAWS THE BLACK SQUARE!!
		for (int x = 12; x < 29; x++) {
			screen.render(x * 7, screen.h - 8, 0 + 1 * 32, Color.get(0, 0), 0);
		}
		
		int textcol = Color.get(-1, 555);
		if (player.showinfo) { // renders show debug info on the screen.
			ArrayList<String> info = new ArrayList<String>();
			info.add(fra + " fps");
			info.add("day tiks " + tickCount);
			info.add((normSpeed * gamespeed) + " tik/sec");
			info.add("walk spd " + player.moveSpeed);
			info.add("X " + (player.x >> 4));
			info.add("Y " + (player.y >> 4));
			if (ModeMenu.score) info.add("Score " + player.score);
			
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
			
			for(int i = 0; i < info.size(); i++) {
				Font.draw(info.get(i), screen, 1, 2 + i*10, textcol);
			}
		}
		
		// This is the arrow counter. ^ = infinite symbol.
		if (ModeMenu.creative || player.ac >= 10000)
			Font.draw("	x" + "^", screen, 84, screen.h - 16, Color.get(0, 333, 444, 555));
		else
			Font.draw("	x" + player.ac, screen, 84, screen.h - 16, Color.get(0, 555));
		//displays arrow icon
		screen.render(10 * 8 + 4, screen.h - 16, 13 + 5 * 32, Color.get(0, 111, 222, 430), 0);
		
		String msg = "";
		if (saving) msg = "Saving... " + LoadingMenu.percentage + "%";
		else if (Bed.inBed) msg = "Sleeping...";
		
		if(msg.length() > 0)
			Font.drawCentered(msg, screen, screen.h / 2 - 20, Color.get(-1, 555), Color.get(-1, 222));
		
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
			for (int i = 0; i < notifications.size(); i++) {
				String note = ((String) notifications.get(i));
				int x = screen.w / 2 - note.length() * 8 / 2,
				  y = screen.h - 120 - notifications.size()*8 + i * 8;
				Font.draw(note, screen, x, y, Color.get(-1, 555), Color.get(-1, 111));
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
					color = (player.staminaRechargeDelay / 4 % 2 == 0) ?
					  Color.get(-1, 555, 000, 000) : Color.get(-1, 110, 000, 000);
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
		pendingLevelChange = dir;
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
		boolean debug = false;
		String saveDir = System.getenv("APPDATA");
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("--debug")) debug = true;
			if(args[i].equals("--savedir") && i+1 < args.length)
				saveDir = args[i+1];
		}
		Game.debug = debug;
		Game.gameDir = saveDir + Game.gameDir;
		
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
		
		frame.setVisible(true);
		
		game.start(); // Starts the game!
	}
	
	public static Dimension getWindowSize() {
		return new Dimension(new Float(WIDTH * SCALE).intValue(), new Float(HEIGHT * SCALE).intValue());
	}
	
	public void quit() {
		running = false;
	}
}
