package com.mojang.ld22;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import javax.swing.Timer;
import java.awt.event.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.io.*;

import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Enchanter;
import com.mojang.ld22.entity.Furniture;
import com.mojang.ld22.entity.Inventory;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.IronLantern;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Workbench;
import com.mojang.ld22.entity.bed;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.SpriteSheet;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.ItemResource;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.item.ListItems;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.levelgen.LevelGen;
import com.mojang.ld22.level.tile.DirtTile;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.saveload.Save;
import com.mojang.ld22.saveload.Load;
import com.mojang.ld22.screen.DeadMenu;
import com.mojang.ld22.screen.LevelTransitionMenu;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.Menu;
import com.mojang.ld22.screen.StartMenu;
import com.mojang.ld22.screen.TitleMenu;
import com.mojang.ld22.screen.WonMenu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.InfoMenu;
import com.mojang.ld22.screen.WorldGenMenu;
import com.mojang.ld22.screen.WorldSelectMenu;

public class Game extends Canvas implements Runnable, ActionListener{
	
	private static final long serialVersionUID = 1L;
	private static Random random = new Random();
	public static final String gameDir = System.getenv("APPDATA") + "/.playminicraft/mods/Minicraft-Plus";
	public static final String NAME = "Minicraft Plus";
	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	private static final int SCALE = 3;
	
	public static int gamespeed = 1;
	public double nsPerTick = 1.6666666666666666E7D * (double)gamespeed;
	
	public static int Time = 0;
	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	private boolean running = false;
	public boolean fpscounter = false;
	boolean initTick;
	public static boolean tickReset = false;
	int hungerTick;
	private Screen screen;
	private Screen lightScreen;
	private InputHandler input = new InputHandler(this);
	int newscoreTime = 72000;
	int count = 0;
	boolean reverse = false;
	public int scoreTime = newscoreTime;
	
	public static int multiplyer = 1;
	public static int mtm = 300;
	public static int multiplyertime = mtm;
	
	public static int ism = 1;
	
	int[] oldlvls; 
	private int[] colors = new int[256];
	public static int tickCount = 0;
	public int gameTime = 0;
	public boolean isDayNoSleep = false;
	public int fra = 0;
	public int tik = 0;
	public static int dayYeahTick;
	
	public static boolean Load = false;
	public static boolean fasttime = false;
	public static int LoadTime = 3;
	
	public static boolean paused = false;
	
	public static int infotime = 120;
	public static boolean infoplank = false;
	public static boolean infosbrick = false;
	
	public static boolean truerod = false;
	public static boolean isfishing = false;
	public static int fishingcount = 0;
	
	public Level level;
	public static Level[] levels = new Level[5];
	static public int currentLevel = 3;
	int hungerMinusCount;
	public Player player;
	
	int l = 128;
	public static int acs = 25;
	public static int ac = acs;
	public static boolean autosave = true;
	public int asTick = 0;
	public static int astime = 3600; //asTime stands for Auto-Save Time! (interval)
	public static String savedtext = "";
	public static List notifications = new ArrayList();
	public boolean saving = false;
	public int savecooldown;
	public int notetick = 0;
	
	public Menu menu;
	private int playerDeadTime;
	private int pendingLevelChange;
	private int wonTimer = 0;
	public boolean hasWon = false;
	
	Timer sunrise = new Timer(60000, this);
	Timer sunset = new Timer(60000, this);
	Timer daytime = new Timer(420000, this);
	Timer nighttime = new Timer(240000, this);
	
	public void setMenu(Menu menu) {
		this.menu = menu;
		if (menu != null) menu.init(this, input);
	}
	
	public static void changeTime(int t) {
		Time = t;
	}
	
	//called after main; main is at bottom.
	public void start() {
		running = true;
		new Thread(this).start(); //calls run()
	}
	
	public void stop() {
		running = false;
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / 60;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		
		//calls setMenu with new TitleMenu (and does other things)
		init();
		
		//main game loop? calls tick() and render().
		while (running) {
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) {
				ticks++;
				tick();
				unprocessed -= 1;
				shouldRender = true;
			}
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (shouldRender) {
				frames++;
				render();
			}
			
			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 += 1000;
				//System.out.println(ticks + " ticks, " + frames + " fps");
				//Font.draw(ticks + " ticks, " + frames + " fps", screen, screen.w, screen.h, Color.get(0, 555, 555, 555));
				fra = frames;
				tik = ticks;
				frames = 0;
				ticks = 0;
			}
		}
	}
	
	private void init() {
		int pp = 0;
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
		try {
			screen = new Screen(WIDTH, HEIGHT, new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/icons.png"))));
			lightScreen = new Screen(WIDTH, HEIGHT, new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/icons.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		resetGame();
		setMenu(new TitleMenu());
	}
	
	public void resetGame() {
		playerDeadTime = 0;
		wonTimer = 0;
		gameTime = 0;
		Player.hasSetHome = false;
		Player.canGoHome = false;
		hasWon = false;
		
		// adds a new player
		player = new Player(this, input);
		//this is a boolean in deadmenu that returns if the player has died, this way you don't respawn in a new world & it saves your spawn pos
		if (DeadMenu.shudrespawn == true) {	
			//System.out.print("Current Level = " + currentLevel + "                                           ");
			currentLevel = 3;
			level = levels[currentLevel];
			 player.respawn(level);
			level.add(player);
		}
		else {
			levels[3] = new Level(l, l, 0, levels[4]);
			
			level = levels[currentLevel];
			if (currentLevel == 3)
			currentLevel = 3;
			if (currentLevel != 3)
			currentLevel = 3;
			DeadMenu.shudrespawn = true; 
			player.findStartPos(level);
		}
	}
	
	public void resetstartGame() {
		playerDeadTime = 0;
		wonTimer = 0;
		gameTime = 0;
		Player.hasSetHome = false;
		Player.canGoHome = false;
		bed.hasBedSet = false;
		if(!StartMenu.hasSetDiff)
			StartMenu.diff = 2;
		
		tickReset = true;
		hasWon = false;
		
		player = new Player(this, input);
		ListItems.items.clear();
		new ListItems();
		
		levels = new Level[6];	
		currentLevel = 3;
		ac = acs;
		
		if (WorldGenMenu.sized == WorldGenMenu.sizeNorm){
			l = 128;
		}else if (WorldGenMenu.sized == WorldGenMenu.sizeBig){
			l = 256;
		}else if (WorldGenMenu.sized == WorldGenMenu.sizeHuge){
			l = 512;
		}
		
		if (ModeMenu.score){
			scoreTime = newscoreTime;
			ism = 1;
		}
		
		Player.score = 0;
		
		/* Old code; keeping until I know new code works.
		levels[4] = new Level(l, l, 1, null);
		levels[3] = new Level(l, l, 0, levels[4]);
		levels[2] = new Level(l, l, -1, levels[3]);
		levels[1] = new Level(l, l, -2, levels[2]);
		levels[0] = new Level(l, l, -3, levels[1]);
		levels[5] = new Level(l, l, -4, levels[0]);
		
		level = levels[currentLevel];
		player.respawn(level);
		currentLevel = 3;
		level.add(player);
		//System.out.println("Ready!");
		DeadMenu.shudrespawn = true; 
		SunRtd();
		*/
		if(WorldSelectMenu.loadworld) {
			try {
				BufferedReader f = new BufferedReader(new FileReader(gameDir + "/saves/" + WorldSelectMenu.worldname + "/Level3.miniplussave"));
				this.l = Integer.parseInt(f.readLine().substring(0, 3));
			} catch (FileNotFoundException var4) {
				var4.printStackTrace();
			} catch (NumberFormatException var5) {
				var5.printStackTrace();
			} catch (IOException var6) {
				var6.printStackTrace();
			}
		}

		if(!WorldSelectMenu.loadworld) {
			LoadingMenu.percentage = 0;
		} else {
			LoadingMenu.percentage += 5;
		}

		levels[4] = new Level(this.l, this.l, 1, (Level)null);
		if(!WorldSelectMenu.loadworld) {
			LoadingMenu.percentage = 20;
		} else {
			LoadingMenu.percentage += 5;
		}

		levels[3] = new Level(this.l, this.l, 0, levels[4]);
		if(!WorldSelectMenu.loadworld) {
			LoadingMenu.percentage = 40;
		} else {
			LoadingMenu.percentage += 5;
		}

		levels[2] = new Level(this.l, this.l, -1, levels[3]);
		if(!WorldSelectMenu.loadworld) {
			LoadingMenu.percentage = 60;
		} else {
			LoadingMenu.percentage += 5;
		}

		levels[1] = new Level(this.l, this.l, -2, levels[2]);
		if(!WorldSelectMenu.loadworld) {
			LoadingMenu.percentage = 80;
		} else {
			LoadingMenu.percentage += 5;
		}

		levels[0] = new Level(this.l, this.l, -3, levels[1]);
		if(!WorldSelectMenu.loadworld) {
			LoadingMenu.percentage = 100;
		} else {
			LoadingMenu.percentage += 5;
		}

		levels[5] = new Level(this.l, this.l, -4, levels[0]);
		if(!WorldSelectMenu.loadworld) {
			FurnitureItem f1 = new FurnitureItem(new IronLantern());
			Furniture l = f1.furniture;
			l.x = 984;
			l.y = 984;
			levels[5].add(l);
		}

		LoadingMenu.percentage = 0;
		if(!ModeMenu.creative) {
			this.player.inventory.add(new FurnitureItem(new Enchanter()));
			this.player.inventory.add(new FurnitureItem(new Workbench()));
		}

		this.level = levels[currentLevel];
		this.player.respawn(this.level);
		currentLevel = 3;
		this.level.add(this.player);
		if(WorldSelectMenu.loadworld) {
			new Load(this, WorldSelectMenu.worldname);
		} else {
			tickCount = 0;
			if(this.level.getTile(this.player.x / 16, this.player.y / 16) != Tile.sand) {
				this.level.setTile(this.player.x / 16, this.player.y / 16, Tile.grass, 0);
			}
		}

		DeadMenu.shudrespawn = true;
		/*if(WorldGenMenu.theme == WorldGenMenu.hell) {
			this.player.inventory.add(new ResourceItem(Resource.lavapotion));
		}*/

	}
	
	public void SunRtd(){
		if (Time == 0){
			daytime.stop();
			nighttime.stop();
			sunrise.stop();
			sunset.stop();
			sunrise.start();
			//System.out.println("Starting! (Time0)");
			if (DirtTile.dirtc == 0){
			}
			if (DirtTile.dirtc == 1){
			DirtTile.dirtc--;
			}
		}
		if (Time == 1){
			Time--;
			daytime.stop();
			nighttime.stop();
			sunrise.stop();
			sunset.stop();
			sunrise.start();
			//System.out.println("Starting! (Time1)");
			if (DirtTile.dirtc == 0){
			}
			if (DirtTile.dirtc == 1){
			DirtTile.dirtc--;
			}
		}
		if (Time == 2){
			Time--;
			Time--;
			daytime.stop();
			nighttime.stop();
			sunrise.stop();
			sunset.stop();
			sunrise.start();
			//System.out.println("Starting! (Time2)");
			if (DirtTile.dirtc == 0){
			}
			if (DirtTile.dirtc == 1){
			DirtTile.dirtc--;
			}
		}
		if (Time == 3){
			Time--;
			Time--;
			Time--;
			daytime.stop();
			nighttime.stop();
			sunrise.stop();
			sunset.stop();
			sunrise.start();
			//System.out.println("Starting! (Time3)");
			if (DirtTile.dirtc == 0){
			}
			if (DirtTile.dirtc == 1){
			DirtTile.dirtc--;
			}
		}
	}
	
	public void tick() {
		if (tickReset == true){ tickCount = 0; tickReset = false;}
		if (!paused){
		tickCount++;		
		}
		
		asTick++;
		if(asTick >= astime / 8) {
			savedtext = "";
		}
		
		if(asTick > astime) {
			if(autosave && player.health > 0 && !hasWon && levels[currentLevel].entities.contains(player)) {
				new Save(player, WorldSelectMenu.worldname);
			}
			
			asTick = 0;
		}
		
		int tickSunR = 0 ;
		//int tickDayT = 420000;
		//int tickSunSet = 60000;
		//int tickNight = 240000;
		//remember the 0 dilly XD
		if (tickCount < 54000) {
			isDayNoSleep = true;
		}
		if (tickCount >=54000) {
			isDayNoSleep = false;
		}
			
		if (tickReset) {tickCount = 0;{
		tickReset = false;
		}
		}
		if (tickCount == 0) {
			Time = 0;
			tickSunR = tickSunR + 7200;
		}
		if (tickCount == 7200) {
			Time = 1;
			tickSunR = tickSunR + 28800;
			
		}
		//4800
		if (tickCount == 36000) {
			Time = 2;
			tickSunR = tickSunR + 7200;
		
		}
		//5400
		if (tickCount == 43200) {
			Time = 3;
			tickSunR = tickSunR + 21600;
		
		}
		//7600
		if (tickCount == 64800) {
			Time = 0;
			tickCount = 0;
		}
		
		if (ModeMenu.score){
			if (!paused){
			scoreTime--;
			}
	
			if (scoreTime < 1 && !player.removed){
				setMenu(new WonMenu());
				System.out.print(player.score);
				//Extra score from drops.
				player.score = player.score + (Inventory.scored(Resource.cloth) * (random.nextInt(2) + 1) * ism);
				player.score = player.score + (Inventory.scored(Resource.slime) * (random.nextInt(2) + 1) * ism);
				player.score = player.score + (Inventory.scored(Resource.bone) * (random.nextInt(2) + 1) * ism);
				player.score = player.score + (Inventory.scored(Resource.gunp) * (random.nextInt(2) + 1) * ism);
				player.score = player.score + (Inventory.scored(Resource.bookant) * (random.nextInt(2) + 1) * (random.nextInt(2) + 1) * ism);
				player.remove();
			}
			
		
		if (multiplyer > 1){
		if (multiplyertime != 0){
		multiplyertime--;
		}
		if (multiplyertime == 0){
		multiplyer = 1;
		multiplyertime = mtm = 300;
		}
		}
		if (multiplyer > 50){
			multiplyer = 50;
		}
		}
		
		if (infoplank || infosbrick){
			if (infotime > 0){
				infotime--;
			} else {
				infoplank = false;
				infosbrick = false;
				infotime = 120;
			}
		}
		
		if (reverse == false){
			count++;
			if (count == 25){
				reverse = true;
			}
			} else if (reverse == true){
				count--;
				if (count == 0){
					reverse = false;
				}
			}
		
		//System.out.println(tickCount);
		//System.out.println(bed.hasBeenTrigged);
		if (!hasFocus()) {
			input.releaseAll();
		} else {
			if (!player.removed && !hasWon) gameTime++;
			
			input.tick();
			if (menu != null) {
				menu.tick();
				paused = true;
			} else {
				paused = false;
				if (player.removed) {
					playerDeadTime++;
					if (playerDeadTime > 60) {
						setMenu(new DeadMenu());
					}
				} else {
					if (pendingLevelChange != 0) {
						setMenu(new LevelTransitionMenu(pendingLevelChange));
						pendingLevelChange = 0;
					}
				}
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
				level.tick();
				Tile.tickCount++;
			}
		}
	}
	
	public void changeLevel(int dir) {
		level.remove(player);
		currentLevel += dir;
		if (currentLevel == -1){
			currentLevel = 5;
		}
		if (currentLevel == 6){
			currentLevel = 0;
		}
		level = levels[currentLevel];
		player.x = (player.x >> 4) * 16 + 8;
		player.y = (player.y >> 4) * 16 + 8;
		level.add(player);
		
	}
	
	public static void Fishing(Level level, int x, int y, Player player){
	isfishing = true;
	int fcatch = random.nextInt(90);
	
	if (ItemResource.dur == 0){
		player.activeItem.isDepleted();
	}
	
	if (fcatch <= 8){
		System.out.print("Caught a Fish!");
		level.add(new ItemEntity(new ResourceItem(Resource.rawfish), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		isfishing = false;
	}
	if (fcatch == 25 || fcatch == 43 || fcatch == 72){
		System.out.print("Caught some slime?");
		level.add(new ItemEntity(new ResourceItem(Resource.slime), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		isfishing = false;
	}if (fcatch == 56){
		System.out.print("Rare Armor!");
		level.add(new ItemEntity(new ResourceItem(Resource.larmor), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		isfishing = false;
	}else {
		System.out.print("FAIL!");
		isfishing = false;
	}
	
	}
	
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			requestFocus();
			return;
		}
		
		int xScroll = player.x - screen.w / 2;
		int yScroll = player.y - (screen.h - 8) / 2;
		if (xScroll < 16) xScroll = 16;
		if (yScroll < 16) yScroll = 16;
		if (xScroll > level.w * 16 - screen.w - 16) xScroll = level.w * 16 - screen.w - 16;
		if (yScroll > level.h * 16 - screen.h - 16) yScroll = level.h * 16 - screen.h - 16;
		if (currentLevel > 3) {
			int col = Color.get(20, 20, 121, 121);
			for (int y = 0; y < 28; y++)
				for (int x = 0; x < 48; x++) {
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 0, col, 0);
				}
		}
		
		level.renderBackground(screen, xScroll, yScroll);
		level.renderSprites(screen, xScroll, yScroll);
		
		
		int col0 = Color.get(-1, 555, 555, 555);
		int col1 = Color.get(-1, -1, -1, -1);
		
		int col = 0;
		int colVis = Color.get(-1, 555, 555, 555);
		int colTran = Color.get(-1, -1, -1, -1);
		int colSleep = 0;
		if (bed.hasBeenTrigged)
		{
		if (isDayNoSleep) {
			colSleep = colVis;
		}
		else {
			colSleep = colTran;
		}
		if (dayYeahTick == 600) {
			colSleep = colTran;
		}
		}
		if (fpscounter == false){
			col = col1;
		}
		if (fpscounter == true){
			col = col0;
		}
		
		
		
		
		
		if (ModeMenu.creative == false)
		{
		if (currentLevel < 3) {
			lightScreen.clear(0);
			level.renderLight(lightScreen, xScroll, yScroll);
			screen.overlay(lightScreen, xScroll, yScroll);
		}
		}
		
		renderGui();
		
		if (!hasFocus()) renderFocusNagger();
		
		for (int y = 0; y < screen.h; y++) {
			for (int x = 0; x < screen.w; x++) {
				int cc = screen.pixels[x + y * screen.w];
				if (cc < 255) pixels[x + y * WIDTH] = colors[cc];
			}
		}
		
		Graphics g = bs.getDrawGraphics();
		g.fillRect(0, 0, getWidth(), getHeight());
		
		
		//Remeber to make a zoom feature!!!!!!!!!!!!!!!!!!!!!!!!
		
		
		int ww = WIDTH * 3;
		int hh = HEIGHT * 3;
		int xo = (getWidth() - ww) / 2;
		int yo = (getHeight() - hh) / 2;
		g.drawImage(image, xo, yo, ww, hh, null);
		g.dispose();
		bs.show();
	}
	
	private void renderGui() {
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 29; x++) {
				screen.render(x * 7, screen.h - 16 + y * 8, 0 + 12 * 32, Color.get(-1, -1, -1, -1), 0);
			}
		}
		for (int y = 1; y < 2; y++) {
			for (int x = 12; x < 29; x++) {
				screen.render(x * 7, screen.h - 16 + y * 8, 0 + 1 * 32, Color.get(0, 0, 0, 0), 0);
			}
		}
		for (int y = 1; y < 2; y++) {
			for (int x = 12; x < 14; x++) {
				screen.render(x * 7, screen.h - 16 + y * 8, 0 + 1 * 32, Color.get(0, 0, 0, 0), 0);
			}
		}
		
		if(saving) {
			Font.draw("Saving... " + LoadingMenu.percentage + "%", screen, screen.w / 2 - ("Saving... " + LoadingMenu.percentage + "%").length() * 4 + 1, screen.h / 2 - 32 + 1, Color.get(-1, 111, 111, 111));
			Font.draw("Saving... " + LoadingMenu.percentage + "%", screen, screen.w / 2 - ("Saving... " + LoadingMenu.percentage + "%").length() * 4, screen.h / 2 - 32, Color.get(-1, 4, 4, 4));
		}
		
		int xfps = fra;
		int txlevel = Player.xx / 16;
		int tylevel = Player.yy / 16;
		int col0 = Color.get(-1, 555, 555, 555);
		Font.draw(xfps +" fps", screen, 1, screen.h - 190 , col0);
		Font.draw("X " + txlevel, screen, 1, screen.h - 180 , col0);
		Font.draw("Y " + tylevel, screen, 1, screen.h - 170 , col0);
		//Font.draw("Time " + (tickCount/60), screen, 1, screen.h - 160 , col0);
		if (ModeMenu.score){
		Font.draw("Score " + player.score, screen, 1, screen.h - 160 , col0);
		}		
		int awh = (AirWizard.healthstat / 20);
		if (awh == 0){//This just turns 0% into 1% to avoid confunsion.
			awh = 1;
		}
		if (currentLevel == 4){
			if (AirWizard.healthstat > 0){
				if (!ModeMenu.score){
			Font.draw("AirWizard Health " + awh + "%", screen, 84, screen.h - 190 , Color.get(100, 50, 50, 50));
				} else  {
			Font.draw("AirWizard Health " + awh + "%", screen, 84, screen.h - 180 , Color.get(100, 50, 50, 50));		
				}
			}
		}
		
		// This is the arrow counter. ^ = infinite symbol.
		if (ModeMenu.creative == false){
		if (ac >= 10000){
		Font.draw("  x" + "^", screen, 84, screen.h - 16 , Color.get(0, 333, 444, 555));
		} else if (ac < 10000){
		Font.draw("  x" + ac, screen, 84, screen.h - 16 , Color.get(0, 555, 555, 555));
		}
		}else if (ModeMenu.creative != false){
		Font.draw("  x" + "^", screen, 84, screen.h - 16 , Color.get(0, 333, 444, 555));
		}
		
		int cols = Color.get(300, 555, 555, 555);
		int seconds = scoreTime/ 60;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;
		if (count <= 5){
			cols = Color.get(500, 555, 555, 555);
		}
		if (count <= 10 && count > 5){
			cols = Color.get(400, 555, 555, 555);
		}
		if (count <= 15 && count > 10){
			cols = Color.get(300, 555, 555, 555);
		}
		if (count <= 20 && count > 15){
			cols = Color.get(200, 555, 555, 555);
		}
		if (count <= 25 && count > 20){
			cols = Color.get(100, 555, 555, 555);
		}
		
		if (infoplank){
			Font.draw("Can only be placed on planks!", screen, 30, screen.h - 130 , Color.get(000, 555, 555, 555));
		}
		if (infosbrick){
			Font.draw("Can only be placed on st.brick!", screen, 20, screen.h - 130 , Color.get(000, 555, 555, 555));
		}
		
		if (ModeMenu.score){
			if (scoreTime > 18000){
			Font.draw("Time left " + minutes + "m " + seconds + "s", screen, 84, screen.h - 190 , Color.get(000, 555, 555, 555));
			} else if (scoreTime < 3600){
			Font.draw("Time left " + minutes + "m " + seconds + "s", screen, 84, screen.h - 190 , cols);
			} else {
			Font.draw("Time left " + minutes + "m " + seconds + "s", screen, 84, screen.h - 190 , Color.get(330, 555, 555, 555));
			}
			if (multiplyer > 1 && multiplyer < 50){
				Font.draw("X" + multiplyer, screen, 260, screen.h - 190 , Color.get(-1, 540, 540, 540));
			} else if (multiplyer > 49){
				Font.draw("X" + multiplyer, screen, 260, screen.h - 190 , Color.get(-1, 500, 500, 500));
			}
			Font.draw(multiplyertime + " " + mtm + "", screen, 230, screen.h - 180 , Color.get(-1, 5, 5, 5));
		}
		
		if (player.activeItem != null && truerod){
			int dura = (ItemResource.dur * 7);
			if (dura > 100) dura = 100;
			Font.draw(dura + "%", screen, 164, screen.h - 16 , Color.get(0, 30, 30, 30));
		}
		
		screen.render(11 * 8, screen.h - 17, 13 + 5 * 32, Color.get(-1, 111, 222, 430), 0);//arrow icon
		
		for (int i = 0; i < 10; i++) {
			if (ModeMenu.creative == false) {
				
			
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
				screen.render(i * 8 + 208, screen.h - 8, 3 + 12 * 32, Color.get(-1, -1, -1, -1), 0);
			
			if (player.staminaRechargeDelay > 0) {
				if (player.staminaRechargeDelay / 4 % 2 == 0)
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 555, 000, 000), 0);
				else
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 110, 000, 000), 0);
			} else {
				if (i < player.stamina)
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 220, 550, 553), 0);
				else
					screen.render(i * 8, screen.h - 8, 1 + 12 * 32, Color.get(-1, 110, 000, 000), 0);
			}
		}
		}
		if (player.activeItem != null) {
			player.activeItem.renderInventory(screen, 12 * 7, screen.h - 8);
		}
		
		if (menu != null) {
			menu.render(screen);
		}
	}
	
	private void renderFocusNagger() {
		String msg = "Click to focus!";
		paused = true;
		int xx = (WIDTH - msg.length() * 8) / 2;
		int yy = (HEIGHT - 8) / 2;
		int w = msg.length();
		int h = 1;
		
		screen.render(xx - 8, yy - 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
		screen.render(xx + w * 8, yy - 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 1);
		screen.render(xx - 8, yy + 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 2);
		screen.render(xx + w * 8, yy + 8, 0 + 13 * 32, Color.get(-1, 1, 5, 445), 3);
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy - 8, 1 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
			screen.render(xx + x * 8, yy + 8, 1 + 13 * 32, Color.get(-1, 1, 5, 445), 2);
		}
		for (int y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 2 + 13 * 32, Color.get(-1, 1, 5, 445), 0);
			screen.render(xx + w * 8, yy + y * 8, 2 + 13 * 32, Color.get(-1, 1, 5, 445), 1);
		}
		
		if ((tickCount / 20) % 2 == 0) {
			Font.draw(msg, screen, xx, yy, Color.get(5, 333, 333, 333));
		} else {
			Font.draw(msg, screen, xx, yy, Color.get(5, 555, 555, 555));
		}
	}
	
	public void scheduleLevelChange(int dir) {
		pendingLevelChange = dir;
	}
	
	public static void main(String[] args) {
		Game game = new Game();
		game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		JFrame frame = new JFrame(Game.NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(game, BorderLayout.CENTER);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		game.start();
		
	}
	
	
	public void won() {
		wonTimer = 60 * 3;
		hasWon = true;
		daytime.stop();
		nighttime.stop();
		sunrise.stop();
		sunset.stop();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
	