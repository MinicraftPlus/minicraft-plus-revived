package com.mojang.ld22;

import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Bed;
import com.mojang.ld22.entity.Enchanter;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Furniture;
import com.mojang.ld22.entity.Inventory;
import com.mojang.ld22.entity.IronLantern;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Workbench;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.SpriteSheet;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.ListItems;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.ItemResource;
import com.mojang.ld22.item.resource.PotionResource;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.DirtTile;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.saveload.Load;
import com.mojang.ld22.saveload.Save;
import com.mojang.ld22.screen.DeadMenu;
import com.mojang.ld22.screen.LevelTransitionMenu;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.Menu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.StartMenu;
import com.mojang.ld22.screen.TitleMenu;
import com.mojang.ld22.screen.WonMenu;
import com.mojang.ld22.screen.WorldGenMenu;
import com.mojang.ld22.screen.WorldSelectMenu;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

public class Game extends Canvas implements Runnable, ActionListener {
	
	private static final long serialVersionUID = 1L;
	public static boolean debug = false;
	private static Random random = new Random();
	private static final int SCALE = 3;
	
	/// RENDERING
	
	public static final String gameDir =
			System.getenv("APPDATA")
					+ "/.playminicraft/mods/Minicraft-Plus"; // The directory in which all the game files are stored; APPDATA is meant for windows...
	public static final String NAME = "Minicraft Plus"; // This is the name on the application window
	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	//does the *scale part mean anything to the graphics, or does java accomodate it?
	
	private BufferedImage image; // creates an image to be displayed on the screen.
	private int[] pixels; // the array of pixels that will be displayed on the screen.
	private int[] colors; // All of the colors, put into an array.
	private Screen screen; // Creates the main screen
	private Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	
	/// MANAGERIAL VARS AND RUNNING
	
	public static int gamespeed = 1;
	
	public double nsPerTick = 1.6666666666666666E7D * (double) gamespeed;
	public int gameTime; // Main value in the timer used on the dead screen.
	public boolean fpscounter; // show fps counter?
	private boolean running; //non-static paused var..? This stores if the game is running or paused (as well).
	
	/// LEVEL AND PLAYER
	
	public static Level[] levels = new Level[5]; // This array is about the different levels.
	public static int currentLevel = 3; // This is the level the player is on. It's set to 3, which is the surface.
	
	public InputHandler input; // input used in Game, Player, and just about all the *Menu classes.
	public Menu menu; // the current menu you are on.
	public Player player; // The Player.
	public Level level; // This is the current level you are on.
	int[] oldlvls; //--not used in this file
	int worldSize; // The size of the world
	
	private int playerDeadTime; // the paused time when you die before the dead menu shows up.
	private int pendingLevelChange; // used to determined if the player should change levels or not.
	private int wonTimer; // the paused time when you win before the win menu shows up.
	public boolean hasWon; // If the player wins this is set to true
	
	/// TIME AND TICKS
	
	public static int tickCount = 0; // Used in the ticking system
	public static boolean tickReset = false;
	public static int time = 0; // Facilites time of day / sunlight.
	public static int dayYeahTick; // Bed? Sunlight?
	
	public static int LoadTime = 3; //Never used?
	public static boolean Load = false; //Never used?
	public static boolean fasttime = false; //fast ticks; this is probably used for sleeping.
	public static boolean paused = false; // If the game is paused.
	
	public boolean isDayNoSleep; //beds and sleeping, I'll guess.
	Timer sunrise, sunset, daytime, nighttime; //?Day-cycle Timers... I wonder how they're used?
	
	/// AUTOSAVE AND NOTIFICATIONS
	
	public static int acs = 25;
	public static int ac = acs;
	public static boolean autosave; //if autosave feature is enabled.
	public static int astime; //stands for Auto-Save Time (interval)
	public static String savedtext = ""; //?to display save msg, along with notifications?
	public static List notifications = new ArrayList();
	
	public int asTick; //? Tracks time throughout the save process.
	public boolean saving; // If the game is performing a save.
	public int savecooldown; // Prevents saving many times too fast, I think.
	public int notetick; // "note"= notifications.
	
	/// SCORE MODE
	
	public static int multiplyer = 1; // Score multiplier
	public static int mtm = 300, ism = 1; // more time stuff for score mode.
	public static int multiplyertime = mtm; // Time left on the current multiplier.
	
	public int scoreTime, newscoreTime; //more for Score mode.
	
	/// MISCELLANEOUS
	
	//used to display "error" messages
	public static int infotime = 120; //duration of message, in... ticks?
	public static boolean infoplank = false, infosbrick = false; // "can only place on planks / stone brick"

	//fishing
	public static boolean truerod = false, isfishing = false; // are you fishing?
	public static int fishingcount = 0; //? how many times you've used a rod?
	
	public int fra, tik; //these store the number of frames and ticks in the previous second; used for fps, at least.
	boolean initTick; //--not used in this file
	int hungerTick; //--not used in this file
	int hungerMinusCount;
	int count; //something with colors..?
	boolean reverse; //related to count
	
	/// *** CONSTRUSTOR *** ///
	public Game() {
		
		input = new InputHandler(this);
		colors = new int[256];
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		running = false;
		fpscounter = false;
		
		count = 0;
		reverse = false;
		newscoreTime = 72000;
		scoreTime = newscoreTime;
		
		isDayNoSleep = false;
		gameTime = 0;
		fra = 0; // the frames processed in the previous second
		tik = 0; // the ticks processed in the previous second
		
		worldSize = 128;
		autosave = true;
		asTick = 0;
		astime = 3600;
		saving = false;
		notetick = 0;
		
		wonTimer = 0;
		hasWon = false;
		
		sunrise = new Timer(60000, this);
		sunset = new Timer(60000, this);
		daytime = new Timer(420000, this);
		nighttime = new Timer(240000, this);
	}
	
	// Sets the current menu.
	public void setMenu(Menu menu) {
		this.menu = menu;
		if (menu != null) menu.init(this, input);
	}
	
	// Useful if there were more in here... will be soon.
	public static void changeTime(int t) {
		time = t;
	}
	
	//called after main; main is at bottom.
	public void start() {
		running = true;
		new Thread(this).start(); //calls run()
	}
	
	// This is only called by GameApplet...
	public void stop() {
		running = false;
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
		double nsPerTick = 1000000000.0 / 60; // 60ns per tick
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		
		//calls setMenu with new TitleMenu (and does other things)
		init();
		
		//main game loop? calls tick() and render().
		while (running) {
			long now = System.nanoTime();
			unprocessed +=
					(now - lastTime) / nsPerTick; //figures out the unprocessed time between now and lastTime.
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) { // If there is unprocessed time, then tick.
				ticks++;
				tick(); // calls the tick method (in which it calls the other tick methods throughout the code.
				unprocessed -= 1;
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
				//if(com.mojang.ld22.Game.debug) System.out.println(ticks + " ticks, " + frames + " fps");
				Font.draw(ticks + " ticks, " + frames + " fps",
				  screen, screen.w, screen.h, Color.get(0, 555, 555, 555));
				fra = frames; //saves total frames in last second
				tik = ticks; //saves total ticks in last second
				frames = 0; //resets frames
				ticks = 0; //resets ticks; ie, frames and ticks only are per second
			}
		}
	}
	
	/**
	 * Initialization step, this is called when the game first starts. Sets up the colors and the
	 * screens.
	 */
	private void init() {
		int pp = 0;
		/* This loop below creates the 216 colors in minicraft. */
		for (int r = 0; r < 6; r++) {
			for (int g = 0; g < 6; g++) {
				for (int b = 0; b < 6; b++) {
					int rr = (r * 255 / 5);
					int gg = (g * 255 / 5);
					int bb = (b * 255 / 5);
					int mid = (rr * 30 + gg * 59 + bb * 11) / 100;
					
					int r1 = ((rr + mid * 1) / 2) * 230 / 255 + 10;
					int g1 = ((gg + mid * 1) / 2) * 230 / 255 + 10;
					int b1 = ((bb + mid * 1) / 2) * 230 / 255 + 10;
					colors[pp++] = r1 << 16 | g1 << 8 | b1;
				}
			}
		}
		
		/* This sets up the screens, and loads the icons.png spritesheet. */
		try {
			screen =
					new Screen(WIDTH, HEIGHT,
							new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/icons.png"))));
			lightScreen =
					new Screen(WIDTH,HEIGHT,
							new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/icons.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		resetGame(); // starts a new game, to set up initial variables
		setMenu(new TitleMenu()); //sets menu to the title screen.
	}

	/** This resets the game; well, only deadmenu calls this, though. the rest call resetStartGame. */
	public void resetGame() {
		// Resets all values
		playerDeadTime = 0;
		wonTimer = 0;
		gameTime = 0;
		Player.hasSetHome = false;
		Player.canGoHome = false;
		hasWon = false;
		currentLevel = 3;
		
		// adds a new player
		player = new Player(this, input);
		
		// "shudrespawn" returns false if on hardcore, or making a new world. if true, it keeps your world and spawn pos
		if (DeadMenu.shudrespawn) { // respawn, don't regenerate level.
			//if(com.mojang.ld22.Game.debug) System.out.println("Current Level = " + currentLevel + "																					 ");
			//currentLevel = 3;
			level = levels[currentLevel];
			player.respawn(level);
		//	System.out.println("respawned player in resetGame");
			level.add(player); // adds the player to the current level (always surface here)
		} else {
			// new game, regenerate... just the surface level?
			levels[3] = new Level(worldSize, worldSize, 0, levels[4]);
			
			level = levels[currentLevel]; // Set level variable to the surface (b/c currentlevel is always 3)

			DeadMenu.shudrespawn = true; // player should respawn on death
			player.findStartPos(level); // finds the start position for the player
			//System.out.println("spawned player in new surface, resetGame");
		}
		
		//System.out.println("rG PLAYER SPAWN TILE ID: " + level.getTile(player.spawnx, player.spawny).id);
		//System.out.println("rG PLAYER TILE ID: " + level.getTile(player.x / 16, player.y / 16).id+"\n");
	}
	
	// OH! This always generates a new world! the other one doesn't; but it may generate a new surface level.
	public void resetstartGame() {
		playerDeadTime = 0;
		wonTimer = 0;
		gameTime = 0;
		Player.hasSetHome = false;
		Player.canGoHome = false;
		Bed.hasBedSet = false; //no bed

		if (!StartMenu.hasSetDiff) StartMenu.diff = 2;

		tickReset = true; //indirect way of resetting tickCount? why..?
		hasWon = false;

		ListItems.items.clear(); //remove all the old item objects
		new ListItems(); //make a fresh set
		player = new Player(this, input); //very important that this is AFTER the previous 2 statements.

		levels = new Level[6]; // 6?
		currentLevel = 3;
		ac = acs;

		if (WorldGenMenu.sized == WorldGenMenu.sizeNorm) worldSize = 128;
		else if (WorldGenMenu.sized == WorldGenMenu.sizeBig) worldSize = 256;
		else if (WorldGenMenu.sized == WorldGenMenu.sizeHuge) worldSize = 512;

		if (ModeMenu.score) {
			scoreTime = newscoreTime;
			ism = 1;
		}

		Player.score = 0;

		/// LOOOOOADING..

		if (WorldSelectMenu.loadworld) {
			try {
				BufferedReader f = new BufferedReader(new FileReader(
				  gameDir + "/saves/" + WorldSelectMenu.worldname + "/Level3.miniplussave"));
				this.worldSize = Integer.parseInt(f.readLine().substring(0, 3));
			} catch (FileNotFoundException var4) {
				var4.printStackTrace();
			} catch (NumberFormatException var5) {
				var5.printStackTrace();
			} catch (IOException var6) {
				var6.printStackTrace();
			}
		}
		
		//This should replace that massive thing below...
		LoadingMenu.percentage = 0;
		for (int i = 5; i >= 0; i--) {
			if (!WorldSelectMenu.loadworld) LoadingMenu.percentage = (5-i)*20;
			else LoadingMenu.percentage += 5; //just make sure they think something is happening... ;D
			
			levels[(i - 1 < 0 ? 5 : i - 1)] =
					new Level(this.worldSize, this.worldSize, i - 4, (i == 5 ? (Level) null : levels[i]));
		}

		/*
		if(!WorldSelectMenu.loadworld)
			LoadingMenu.percentage = 0;
		else
			LoadingMenu.percentage += 5; //just make sure they think something is happening... ;D

		levels[4] = new Level(this.worldSize, this.worldSize, 1, (Level)null);
		if(!WorldSelectMenu.loadworld)
			LoadingMenu.percentage = 20;
		else
			LoadingMenu.percentage += 5;

		levels[3] = new Level(this.worldSize, this.worldSize, 0, levels[4]);
		if(!WorldSelectMenu.loadworld)
			LoadingMenu.percentage = 40;
		else
			LoadingMenu.percentage += 5;

		levels[2] = new Level(this.worldSize, this.worldSize, -1, levels[3]);
		if(!WorldSelectMenu.loadworld)
			LoadingMenu.percentage = 60;
		else
			LoadingMenu.percentage += 5;

		levels[1] = new Level(this.worldSize, this.worldSize, -2, levels[2]);
		if(!WorldSelectMenu.loadworld)
			LoadingMenu.percentage = 80;
		else
			LoadingMenu.percentage += 5;

		levels[0] = new Level(this.worldSize, this.worldSize, -3, levels[1]);
		if(!WorldSelectMenu.loadworld)
			LoadingMenu.percentage = 100;
		else
			LoadingMenu.percentage += 5;

		levels[5] = new Level(this.worldSize, this.worldSize, -4, levels[0]);
		*/
		
		// if resetStartGame is called when not loading a world, add an Iron lantern at (984, 984).
			// it seems this is only called for the dungeon level.
		if (!WorldSelectMenu.loadworld) {
			FurnitureItem f1 = new FurnitureItem(new IronLantern());
			Furniture f = f1.furniture;
			f.x = 984;
			f.y = 984;
			levels[5].add(f);
		}
		
		LoadingMenu.percentage = 0;
		/* already taken care of in Player.java
		if (!ModeMenu.creative) {// survival inventory only
			player.inventory.add(new FurnitureItem(new Enchanter()));
			player.inventory.add(new FurnitureItem(new Workbench()));
		}*/
		
		level = levels[currentLevel]; // sets level to the current level (3; surface)
		player.respawn(level); // finds the start level for the player
		currentLevel = 3; //? sets next currentlevel, maybe?
		level.add(player);
		
		if (WorldSelectMenu.loadworld) {
			new Load(this, WorldSelectMenu.worldname);
		} else { //should execute only on dungeon level, based on above.
			tickCount = 0;
			//System.out.println("TILE SPAWN ID: " + level.getTile(player.spawnx, player.spawny).id);
			//System.out.println("TILE ID: " + level.getTile(player.x / 16, player.y / 16).id+"\n");
			//if (level.getTile(player.x / 16, player.y / 16) != Tile.sand)
				//level.setTile(player.x / 16, player.y / 16, Tile.grass, 0);
		}
		
		DeadMenu.shudrespawn = true;
		
		if(WorldGenMenu.theme == WorldGenMenu.hell) {
			player.inventory.add(new ResourceItem(Resource.lavapotion));
		}
		
	}
	
	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public void tick() {
		if (Bed.hasBedSet) {
			// IN BED
			level.remove(player); //oh noes! jk. :D
			nsPerTick = 781250.0D;
			if(com.mojang.ld22.Game.debug) System.out.println("SLEEPING... tickCount: " + tickCount);
			if (isDayNoSleep) {
				level.add(player);
				nsPerTick = 1.6666666666666666E7D;
				
				//seems this removes all entities within a certain radius of the player when you get in Bed.
				for (int i = 0; i < level.entities.size(); ++i) {
					if (((Entity) level.entities.get(i)).level == levels[currentLevel]) {
						int xd = level.player.x - ((Entity) level.entities.get(i)).x;
						int yd = level.player.y - ((Entity) level.entities.get(i)).y;
						if (xd * xd + yd * yd < 48
								&& level.entities.get(i) instanceof Mob
								&& level.entities.get(i) != player) {
							level.remove((Entity) level.entities.get(i));
						}
					}
				}
				// so this must be set to true again every tick while in bed...
				Bed.hasBedSet = false;
			}
		}
		
		// tickReset is set to true when..?
		if (tickReset) {
			tickCount = 0;
			tickReset = false;
		}
		
		// Increment tickCount if the game is not paused
		if (!paused) tickCount++;
		
		//auto-save tick; keeps track of time relative to auto-save sequence.
		asTick++;
		if (asTick >= astime / 8) {
			savedtext = "";
		}
		
		//for autosave feature (more)
		if (asTick > astime) {
			if (autosave
					&& player.health > 0
					&& !hasWon
					&& levels[currentLevel].entities.contains(player)) {
				new Save(player, WorldSelectMenu.worldname);
			}
			
			asTick = 0;
		}
		
		//int tickSunR = 0;
		
		if (tickCount < 54000) isDayNoSleep = true;
		if (tickCount >= 54000) isDayNoSleep = false;
		
		//duplicate?
		if (tickReset) {
			tickCount = 0;
			tickReset = false;
		}
		
		if (tickCount == 0) time = 0;
		
		if (tickCount == 3600) level.removeAllEnemies();
		
		if (tickCount == 7200) time = 1;
		
		//4800
		if (tickCount == 36000) time = 2;
		
		//5400
		if (tickCount == 43200) time = 3;
		
		//7600
		if (tickCount == 64800) {
			time = 0;
			tickCount = 0;
		}
		
		/// SCORE MODE ONLY
		
		if (ModeMenu.score) {
			if (!paused) scoreTime--;
			
			if (scoreTime < 1 && !player.removed) {
				setMenu(new WonMenu());
				if(com.mojang.ld22.Game.debug) System.out.println(player.score);
				//Extra score from drops.
				player.score =
						player.score + (Inventory.scored(Resource.cloth) * (random.nextInt(2) + 1) * ism);
				player.score =
						player.score + (Inventory.scored(Resource.slime) * (random.nextInt(2) + 1) * ism);
				player.score =
						player.score + (Inventory.scored(Resource.bone) * (random.nextInt(2) + 1) * ism);
				player.score =
						player.score + (Inventory.scored(Resource.gunp) * (random.nextInt(2) + 1) * ism);
				player.score += (Inventory.scored(Resource.bookant)
										* (random.nextInt(2) + 1)
										* (random.nextInt(2) + 1)
										* ism);
				player.remove();
			}
			
			if (multiplyer > 1) {
				if (multiplyertime != 0) multiplyertime--;
				if (multiplyertime == 0) {
					multiplyer = 1;
					multiplyertime = mtm = 300;
				}
			}
			if (multiplyer > 50) multiplyer = 50;
		}
		
		
		//display "can only place on" messages.
		if (infoplank || infosbrick) {
			if (infotime > 0) {
				infotime--;
			} else {
				infoplank = false;
				infosbrick = false;
				infotime = 120;
			}
		}
		
		//what's this for?
		if (!reverse) {
			count++;
			if (count == 25) reverse = true;
		} else {
			count--;
			if (count == 0) reverse = false;
		}
		
		//if(com.mojang.ld22.Game.debug) System.out.println(tickCount);
		//if(com.mojang.ld22.Game.debug) System.out.println(Bed.hasBeenTrigged);
		
		//This is the general action statement thing! Regulates menus, mostly.
		if (!hasFocus()) {
			input.releaseAll();
		} else {
			if (!player.removed && !hasWon) {
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

				//I'm guessing that this is like DeadMenu, but you can't respawn.
				if (wonTimer > 0) {
					if (--wonTimer == 0) {
						wonTimer = 60 * 3;
						hasWon = true;
						daytime.stop();
						nighttime.stop();
						sunrise.stop();
						sunset.stop();
						setMenu(new WonMenu());
					}
				}
				
				//still in "no active menu" conditional:
				level.tick();
				Tile.tickCount++;
				
				if (input.getKey("Shift").clicked && input.getKey("0").clicked) {
					
				}
			} //end "menu-null" conditional
		} //end hasfocus conditional
	} //end tick()
	
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

	public static void Fishing(Level level, int x, int y, Player player) {
		isfishing = true;
		int fcatch = random.nextInt(90);
		
		if (ItemResource.dur == 0) player.activeItem.isDepleted();
		
		if (fcatch <= 8) {
			if(com.mojang.ld22.Game.debug) System.out.println("Caught a Fish!");
			level.add(new ItemEntity(new ResourceItem(Resource.rawfish), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
			isfishing = false;
		}
		
		if (fcatch == 25 || fcatch == 43 || fcatch == 32 || fcatch == 15 || fcatch == 42) {
			if(com.mojang.ld22.Game.debug) System.out.println("Caught some slime?");
			level.add(new ItemEntity(new ResourceItem(Resource.slime), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
			isfishing = false;
		}

		if (fcatch == 56) {
			if(com.mojang.ld22.Game.debug) System.out.println("Rare Armor!");
			level.add(
					new ItemEntity(
							new ResourceItem(Resource.larmor),
							x + random.nextInt(11) - 5,
							y + random.nextInt(11) - 5));
			isfishing = false;
		} else {
			if(com.mojang.ld22.Game.debug) System.out.println("FAIL!");
			isfishing = false;
		}
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

		int xScroll = player.x - screen.w / 2; // scrolls the screen in the x axis.
		int yScroll = player.y - (screen.h - 8) / 2; // scrolls the screen in the y axis.
		
		//stop scrolling if the screen is at the ...
		if (xScroll < 16) xScroll = 16; // ...left border.
		if (yScroll < 16) yScroll = 16; // ...top border.
		if (xScroll > level.w * 16 - screen.w - 16) xScroll = level.w * 16 - screen.w - 16; // ...right border.
		if (yScroll > level.h * 16 - screen.h - 16) yScroll = level.h * 16 - screen.h - 16; // ...bottom border.
		if (currentLevel > 3) { // if the current level is higher than 3 (which only the sky level is)
			int col = Color.get(20, 20, 121, 121); // background color.
			for (int y = 0; y < 28; y++)
				for (int x = 0; x < 48; x++) {
					// creates the background for the sky level:
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 0, col, 0);
				}
		}

		level.renderBackground(screen, xScroll, yScroll); // renders current level background
		level.renderSprites(screen, xScroll, yScroll); // renders level sprites on screen

		/*
		int col0 = Color.get(-1, 555, 555, 555);
		int col1 = Color.get(-1, -1, -1, -1);

		int col = 0;
		int colVis = Color.get(-1, 555, 555, 555);
		int colTran = Color.get(-1, -1, -1, -1);
		int colSleep = 0;
		if (Bed.hasBeenTrigged) {
			if (isDayNoSleep)
				colSleep = colVis;
			else
				colSleep = colTran;

			if (dayYeahTick == 600)
				colSleep = colTran;
		}

		col = fpscounter ? col0:col1; //pointless...
		*/
		
		// this creates the darkness in the caves
		if (!ModeMenu.creative && currentLevel < 3) {
			lightScreen.clear(0); // clears the light screen to a black color
			level.renderLight(lightScreen, xScroll, yScroll); // finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, xScroll, yScroll); // overlays the light screen over the main screen.
		}

		renderGui(); //renders the GUI.

		if (!hasFocus()) renderFocusNagger(); // calls the renderFocusNagger() method, which creates the "Click to Focus" message.

		for (int y = 0; y < screen.h; y++) {
			for (int x = 0; x < screen.w; x++) {
				// loops through all the pixels on the screen
				int cc = screen.pixels[x + y * screen.w]; // finds a pixel on the screen.
				if (cc < 255) pixels[x + y * WIDTH] = colors[cc]; // colors the pixel appropriately.
			}
		}
		
		Graphics g = bs.getDrawGraphics(); // gets the graphics in which java draws the picture
		g.fillRect(0, 0, getWidth(), getHeight()); // draws the a rect to fill the whole window (to cover last?)
		
		// scales the pixel size.
		int ww = WIDTH * SCALE;
		int hh = HEIGHT * SCALE;
		// gets the image offset.
		int xo = (getWidth() - ww) / 2;
		int yo = (getHeight() - hh) / 2;
		g.drawImage(image, xo, yo, ww, hh, null); //draws the image on the window
		g.dispose(); // releases any system resources that are using this method. (so we don't have crappy framerates)
		bs.show(); // makes the picture visible. (probably)
	}
	
	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private void renderGui() {
		//need to figure this part out.
		int xfps; // game fps?
		int txlevel;
		for (xfps = 0; xfps < 2; xfps++) {
			for (txlevel = 0; txlevel < 29; txlevel++) {
				screen.render(txlevel * 7, screen.h - 16 + xfps * 8, 384, Color.get(-1, -1, -1, -1), 0);
			}
		}
		
		for (xfps = 1; xfps < 2; xfps++) {
			for (txlevel = 12; txlevel < 29; txlevel++) {
				screen.render(txlevel * 7, screen.h - 16 + xfps * 8, 32, Color.get(0, 0, 0, 0), 0);
			}
		}
		/* this seems like a duplicate.
		for (xfps = 1; xfps < 2; xfps++) {
			for (txlevel = 12; txlevel < 14; txlevel++) {
				screen.render(txlevel * 7, screen.h - 16 + xfps * 8, 32, Color.get(0, 0, 0, 0), 0);
			}
		}
		*/
		if (saving) {
			//if(com.mojang.ld22.Game.debug) System.out.println("SAVING GAME...");
			String loadingText = "Saving... " + LoadingMenu.percentage + "%";
			int xPos = screen.centertext(loadingText);
			/*screen.w / 2 - loadingText.length() * 4*/
			Font.draw(loadingText, screen, xPos+1, screen.h / 2 - 32+1, Color.get(-1, 111, 111, 111));
			Font.draw(loadingText, screen, xPos, screen.h / 2 - 32, Color.get(-1, 4, 4, 4));
		}
		
		xfps = fra; //aha! fra is used to determine the fps!
		txlevel = Player.xx / 16; // but what's this?
		int tylevel = Player.yy / 16; // ...and this?
		int col0 = Color.get(-1, 555, 555, 555);
		if (player.showinfo) { // renders show debug info on the screen.
			Font.draw(xfps + " fps", screen, 1, screen.h - 190, col0);
			Font.draw("X " + txlevel, screen, 1, screen.h - 180, col0);
			Font.draw("Y " + tylevel, screen, 1, screen.h - 170, col0);
			/// SCORE MODE DEBUG ONLY:
			if (ModeMenu.score) {
				Font.draw("Score " + Player.score, screen, 1, screen.h - 160, col0);
				if (currentLevel == 5) {
					if (levels[currentLevel].chestcount > 0) {
						Font.draw(
								"Chests: " + levels[currentLevel].chestcount, screen, 1, screen.h - 150, col0);
					} else {
						Font.draw("Chests: Complete!", screen, 1, screen.h - 150, col0);
					}
				}
			} else if (currentLevel == 5) {
				if (levels[currentLevel].chestcount > 0) {
					Font.draw("Chests: " + levels[currentLevel].chestcount, screen, 1, screen.h - 160, col0);
				} else {
					Font.draw("Chests: Complete!", screen, 1, screen.h - 160, col0);
				}
			}
			//end score debug
		}
		
		/// Render AirWizard health bar: (I need to update this)
		
		int awh = AirWizard.healthstat / 20;
		if (awh == 0) // This just turns 0% into 1% to avoid confunsion.
		awh = 1;
		
		// draw AirWizard health if on the sky level, and health > 0.
		if (currentLevel == 4 && AirWizard.healthstat > 0) {
			Font.draw("AirWizard Health " + awh + "%",
			  screen, 84, screen.h - (ModeMenu.score ? 180 : 190), Color.get(100, 50, 50, 50));
		}
		
		// This is the arrow counter. ^ = infinite symbol.
		if (ModeMenu.creative || ac >= 10000)
			Font.draw("	x" + "^", screen, 84, screen.h - 16, Color.get(0, 333, 444, 555));
		else
			Font.draw("	x" + ac, screen, 84, screen.h - 16, Color.get(0, 555, 555, 555));
		//displays arrow icon
		screen.render(10 * 8 + 4, screen.h - 16, 13 + 5 * 32, Color.get(0, 111, 222, 430), 0);
		
		if (Bed.hasBedSet) { // twice for the shadow text effect
			Font.draw("Sleeping...", screen, screen.w / 2 + 1 - 44, screen.h - 119, Color.get(-1, 222, 222, 222));
			Font.draw("Sleeping...", screen, screen.w / 2 - 44, screen.h - 120, Color.get(-1, 555, 555, 555));
		}
		
		/// NOTIFICATIONS
		
		if (infoplank) notifications.add("Can only be placed on planks!"); //, screen, 30, screen.h - 130 , Color.get(000, 555, 555, 555));
		if (infosbrick) notifications.add("Can only be placed on stone brick!"); //, screen, 20, screen.h - 130 , Color.get(000, 555, 555, 555));
		
		if (notifications.size() > 0) {
			notetick++;
			if (notifications.size() > 3) { //only show 3 notifs max at one time; erase old notifs?
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
				  y = screen.h - 120 - i * 8;
				Font.draw(note, screen, x + 1, y + 1, Color.get(-1, 111, 111, 111));
				Font.draw(note, screen, x, y, Color.get(-1, 555, 555, 555));
			}
		}
		
		// SCORE MODE ONLY:
		
		if (ModeMenu.score) {
			int cols = Color.get(330, 555, 555, 555);
			int seconds = scoreTime / 60;
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;
			if(scoreTime > 18000) cols = Color.get(000, 555, 555, 555);
			else if(scoreTime < 3600) {
				if (count <= 5) cols = Color.get(500, 555, 555, 555);
				else if (count <= 10) cols = Color.get(400, 555, 555, 555);
				else if (count <= 15) cols = Color.get(300, 555, 555, 555);
				else if (count <= 20) cols = Color.get(200, 555, 555, 555);
				else if (count <= 25) cols = Color.get(100, 555, 555, 555);
			}
			
			Font.draw("Time left " + minutes + "m " + seconds + "s", screen, 84, screen.h - 190, cols);
			/*
			if (scoreTime > 18000) {
				Font.draw("Time left " + minutes + "m " + seconds + "s",
				  screen, 84, screen.h - 190, Color.get(000, 555, 555, 555));
			} else if (scoreTime < 3600) {
				Font.draw("Time left " + minutes + "m " + seconds + "s",
				  screen, 84, screen.h - 190, cols);
			} else {
				Font.draw("Time left " + minutes + "m " + seconds + "s",
				  screen, 84, screen.h - 190, Color.get(330, 555, 555, 555));
			}
			*/
			if(multiplyer > 1) {
				int multColor = multiplyer < 50 ? Color.get(-1, 540, 540, 540) : Color.get(-1, 500, 500, 500);
				Font.draw("X" + multiplyer, screen, 260, screen.h - 190, multColor);
			}
			
			Font.draw(multiplyertime + " " + mtm + "", screen, 230, screen.h - 180, Color.get(-1, 5, 5, 5));
		}

		// FISHING ROD STATUS
		if (player.activeItem != null && truerod) {
			int dura = (ItemResource.dur * 7);
			if (dura > 100) dura = 100;
			Font.draw(dura + "%", screen, 164, screen.h - 16, Color.get(0, 30, 30, 30));
		}

		
		if(player.potioneffects.size() > 0) {
			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(player.showpotioneffects) {
					int pcol = Color.get(PotionResource.potionColor((String)player.potioneffects.get(i)), 555, 555, 555);
					Font.draw("("+input.getPhysKey("potionEffects")+" to hide!)", screen, 180, screen.h - 183, Color.get(0, 555, 555, 555));
					Font.draw((String)player.potioneffects.get(i) + " (" + ((Integer)player.potioneffectstime.get(i)).intValue() / 60 / 60 + ":" + (((Integer)player.potioneffectstime.get(i)).intValue() / 60 - 60 * (((Integer)player.potioneffectstime.get(i)).intValue() / 60 / 60)) + ")", screen, 180, screen.h - (175 - i * 8), pcol);
				}
			}
		}
		
		
		// This is the status icons, like health hearts, stamina bolts, and hunger burgers.
		if (!ModeMenu.creative) {
			for (int i = 0; i < 10; i++) {
				int color;
				
				// renders your current red hearts, or black hearts for damaged health.
				color = (i < player.health) ? Color.get(-1, 200, 500, 533) : Color.get(-1, 100, 000, 000);
				screen.render(i * 8, screen.h - 16, 0 + 12 * 32, color, 0);
				
				// renders hunger
				color = (i < player.hunger) ? Color.get(-1, 100, 530, 211) : Color.get(-1, 100, 000, 000);
				screen.render(i * 8 + 208, screen.h - 16, 2 + 12 * 32, color, 0);
				
				// renders armor
				color = (i < player.maxArmor) ? Color.get(-1, 333, 444, 555) : Color.get(-1, -1, -1, -1);
				screen.render(i * 8 + 208, screen.h - 8, 3 + 12 * 32, color, 0);

				/*
				if (i < player.health)
					screen.render(i * 8, screen.h - 16, 0 + 12 * 32, Color.get(-1, 200, 500, 533), 0);
				else
					screen.render(i * 8, screen.h - 16, 0 + 12 * 32, Color.get(-1, 100, 000, 000), 0);

				if (i < player.hunger)
					screen.render(i * 8 + 208, screen.h - 16, 2 + 12 * 32, Color.get(-1, 100, 530, 211), 0);
				else
					screen.render(i * 8 + 208, screen.h - 16, 2 + 12 * 32, Color.get(-1, 100, 000, 000), 0);

				if (i < player.maxArmor)
					screen.render(i * 8 + 208, screen.h - 8, 3 + 12 * 32, Color.get(-1, 333, 444, 555), 0);
				else
					screen.render(i * 8 + 208, screen.h - 8, 3 + 12 * 32, , 0);
				*/
				
				if (player.staminaRechargeDelay > 0) {
					// creates the white/gray blinking effect when you run out of stamina.
					color = (player.staminaRechargeDelay / 4 % 2 == 0) ?
					  Color.get(-1, 555, 000, 000) : Color.get(-1, 110, 000, 000);
					
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, color, 0);
					/*if (player.staminaRechargeDelay / 4 % 2 == 0)
						screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 555, 000, 000), 0);
					else
						screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 110, 000, 000), 0);*/
				} else {
					// renders your current stamina, and uncharged gray stamina.
					color = (i < player.stamina) ? Color.get(-1, 220, 550, 553) : Color.get(-1, 110, 000, 000);
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, color, 0);
					/*if (i < player.stamina)
						screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 220, 550, 553), 0);
					else
						screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 110, 000, 000), 0);*/
				}
			}
		}
		
		if (player.activeItem != null) // shows active item sprite and name in bottom toolbar, if one exists.
			player.activeItem.renderInventory(screen, 12 * 7, screen.h - 8);

		if (menu != null) // renders menu, if present.
			menu.render(screen);
	}
	
	/** Renders the "Click to focus" box when you click off the screen. */
	private void renderFocusNagger() {
		String msg = "Click to focus!"; // the message when you click off the screen.
		paused = true; //perhaps paused is only used for this.
		int xx = Menu.centertext(msg); // the width of the box
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
			Font.draw(msg, screen, xx, yy, Color.get(5, 333, 333, 333));
		else // ...bright yellow color
			Font.draw(msg, screen, xx, yy, Color.get(5, 555, 555, 555));
	}
	
	/** This method is called when you interact with stairs, this will give you the transition effect. While changeLevel(int) just changes the level. */
	public void scheduleLevelChange(int dir) {
		// same as changeLevel(). Call scheduleLevelChange(1) if you want to go up 1 level,
		// or call with -1 to go down by 1.
		pendingLevelChange = dir;
	}
	
	/// * The main method! * ///
	public static void main(String[] args) {
		boolean debug = (args != null && args.length > 0 && args[0].equals("--debug"));
		Game.debug = debug;
		Game game = new Game();
		game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		JFrame frame = new JFrame(Game.NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout()); // sets the layout of the window
		frame.add(game, BorderLayout.CENTER); // Adds the game (which is a canvas) to the center of the screen.
		frame.pack(); //squishes everything into the preferredSize.
		frame.setResizable(false); // prevents the user from resizing the window.
		frame.setLocationRelativeTo(null); // the window will pop up in the middle of the screen when launched.
		frame.setVisible(true);
		
		game.start(); // Starts the game!
	}
	
	/** This is called when the player has won the game, obviously. */
	public void won() {
		wonTimer = 60 * 3; // the pause time before the win menu shows up.
		hasWon = true; //confirms that the player has, indeed, won the game.
		// On win, stops the day-night cycle.
		daytime.stop();
		nighttime.stop();
		sunrise.stop();
		sunset.stop();
	}

	//?
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
