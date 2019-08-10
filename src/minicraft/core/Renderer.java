package minicraft.core;

import javax.imageio.ImageIO;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.DotUpdater.TickUpdater;
import minicraft.gfx.Ellipsis.SmoothEllipsis;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.RelPos;

public class Renderer extends Game {
	private Renderer() {}
	
	public static final int HEIGHT = 192;
	public static final int WIDTH = 288;
	static float SCALE = 3;
	
	public static Screen screen; // Creates the main screen
	
	static Canvas canvas = new Canvas();
	private static BufferedImage image; // creates an image to be displayed on the screen.
	private static int[] pixels; // the array of pixels that will be displayed on the screen.
	
	private static Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).
	
	public static boolean readyToRenderGameplay = false;
	public static boolean showinfo = false;
	
	private static Ellipsis ellipsis = new SmoothEllipsis(new TickUpdater());
	
	static void initScreen() {
		if(!HAS_GUI) return;
		
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		/* This sets up the screens, and loads the different spritesheets. */
		try {
			SpriteSheet itemSheet = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/items.png")));
			SpriteSheet tileSheet = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/tiles.png")));
			SpriteSheet entitySheet = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/entities.png")));
			SpriteSheet guiSheet = new SpriteSheet(ImageIO.read(Game.class.getResourceAsStream("/resources/textures/gui.png")));
			screen = new Screen(itemSheet, tileSheet, entitySheet, guiSheet);
			lightScreen = new Screen(itemSheet, tileSheet, entitySheet, guiSheet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		screen.pixels = pixels;
		
		if(HAS_GUI) {
			canvas.createBufferStrategy(3);
			canvas.requestFocus();
		}
	}
	
	
	/** renders the current screen. Called in game loop, a bit after tick(). */
	public static void render() {
		if(!HAS_GUI || screen == null) return; // no point in this if there's no gui... :P
		
		if(readyToRenderGameplay) {
			if(isValidServer()) {
				screen.clear(0);
				Font.drawCentered("Awaiting client connections"+ ellipsis.updateAndGet(), screen, 10, Color.get(-1, 444));
				Font.drawCentered("So far:", screen, 20, Color.get(-1, 444));
				int i = 0;
				for(String playerString: server.getClientInfo()) {
					Font.drawCentered(playerString, screen, 30+i*10, Color.get(-1, 134));
					i++;
				}
				
				renderDebugInfo();
			}
			else {
				renderLevel();
				renderGui();
			}
		}
		
		if (menu != null) // renders menu, if present.
			menu.render(screen);
		
		if (!canvas.hasFocus() && !ISONLINE) renderFocusNagger(); // calls the renderFocusNagger() method, which creates the "Click to Focus" message.
		
		
		BufferStrategy bs = canvas.getBufferStrategy(); // creates a buffer strategy to determine how the graphics should be buffered.
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
		Level level = levels[currentLevel];
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
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 0, 0);
				}
		}
		
		level.renderBackground(screen, xScroll, yScroll); // renders current level background
		level.renderSprites(screen, xScroll, yScroll); // renders level sprites on screen
		
		// this creates the darkness in the caves
		if ((currentLevel != 3 || Updater.tickCount < Updater.dayLength/4 || Updater.tickCount > Updater.dayLength/2) && !isMode("creative")) {
			lightScreen.clear(0); // this doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
			int brightnessMultiplier = player.potioneffects.containsKey(PotionType.Light) ? 12 : 8; // brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, currentLevel, xScroll, yScroll); // overlays the light screen over the main screen.
		}
	}
	
	
	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private static void renderGui() {
		/// AH-HA! THIS DRAWS THE BLACK SQUARE!!
		for (int x = 12; x < 29; x++)
			screen.render(x * 7, Screen.h - 8, 0 + 1 * 32, 0);
		
		renderDebugInfo();
		
		// This is the arrow counter. ^ = infinite symbol.
		int ac = player.getInventory().count(Items.arrowItem);
		if (isMode("creative") || ac >= 10000)
			Font.draw("	x" + "^", screen, 84, Screen.h - 16, Color.get(0, 333, 444, 555));
		else
			Font.draw("	x" + ac, screen, 84, Screen.h - 16, Color.get(0, 555));
		//displays arrow icon
		screen.render(10 * 8 + 4, Screen.h - 16, 13 + 5 * 32, 0);
		
		ArrayList<String> permStatus = new ArrayList<>();
		if (Updater.saving) permStatus.add("Saving... " + Math.round(LoadingDisplay.getPercentage()) + "%");
		if (Bed.sleeping()) permStatus.add("Sleeping...");
		else if (!Game.isValidServer() && Bed.getPlayersAwake() > 0) {
			int numAwake = Bed.getPlayersAwake();
			if(Bed.inBed(Game.player)) {
				permStatus.add(MyUtils.plural(numAwake, "player") + " still awake");
				permStatus.add(" ");
				permStatus.add("Press " + input.getMapping("exit") + " to cancel");
			}
			else if(Game.isValidClient()) {
				// draw it in a corner
				int total = Game.client.getPlayerCount();
				int sleepCount = total - numAwake;
				if(sleepCount > 0)
					new FontStyle(Color.WHITE).setRelTextPos(RelPos.BOTTOM_LEFT).setAnchor(Screen.w, 0)
				.draw(sleepCount+"/"+total+" players sleeping", screen);
			}
		}
		
		if(permStatus.size() > 0) {
			FontStyle style = new FontStyle(Color.WHITE).setYPos(Screen.h / 2 - 25)
				.setRelTextPos(RelPos.TOP)
				.setShadowType(Color.DARK_GRAY, false);
			
			Font.drawParagraph(permStatus, screen, style, 1);
		}
		
		/// NOTIFICATIONS
		
		if (permStatus.size() == 0 && notifications.size() > 0) {
			Updater.notetick++;
			if (notifications.size() > 3) { //only show 3 notifs max at one time; erase old notifs.
				notifications = notifications.subList(notifications.size() - 3, notifications.size());
			}
			
			if (Updater.notetick > 120) { //display time per notification.
				notifications.remove(0);
				Updater.notetick = 0;
			}
			
			// draw each current notification, with shadow text effect.
			FontStyle style = new FontStyle(Color.WHITE).setShadowType(Color.DARK_GRAY, false)
				.setYPos(Screen.h*2/5).setRelTextPos(RelPos.TOP, false);
			Font.drawParagraph(notifications, screen, style, 0);
		}
		
		
		// SCORE MODE ONLY:
		
		if (isMode("score")) {
			int seconds = (int)Math.ceil(Updater.scoreTime / (double)Updater.normSpeed);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;
			
			int timeCol;
			if(Updater.scoreTime >= 18000) timeCol = Color.get(0, 555);
			else if (Updater.scoreTime >= 3600) timeCol = Color.get(330, 555);
			else timeCol = Color.get(400, 555);
			
			Font.draw("Time left " + (hours > 0 ? hours+"h ":"") + minutes + "m " + seconds + "s", screen, Screen.w/2-9*8, 2, timeCol);
			
			String scoreString = "Current score: " + player.getScore();
			Font.draw(scoreString, screen, Screen.w - Font.textWidth(scoreString)-2, 3 + 8, Color.WHITE);
			
			if(player.getMultiplier() > 1) {
				int multColor = player.getMultiplier() < Player.MAX_MULTIPLIER ? Color.get(-1, 540) : Color.RED;
				String mult = "X" + player.getMultiplier();
				Font.draw(mult, screen, Screen.w-Font.textWidth(mult)-2, 4 + 2*8, multColor);
			}
		}
		
		// TOOL DURABILITY STATUS
		if (player.activeItem instanceof ToolItem) {
			ToolItem tool = (ToolItem) player.activeItem;
			int dura = tool.dur * 100 / (tool.type.durability * (tool.level+1));
			Font.draw(dura + "%", screen, 164, Screen.h - 16, Color.get(0, 30));
		}
		
		/// This renders the potions overlay
		if(player.showpotioneffects && player.potioneffects.size() > 0) {
			Map.Entry<PotionType, Integer>[] effects = player.potioneffects.entrySet().toArray(new Map.Entry[0]);
			// the key is potion type, value is remaining potion duration.
			for(int i = 0; i < effects.length; i++) {
				PotionType pType = effects[i].getKey();
				int pTime = effects[i].getValue() / Updater.normSpeed;
				int pcol = Color.get(pType.dispColor, 555);
				Font.draw("("+input.getMapping("potionEffects")+" to hide!)", screen, 180, 9, Color.get(0, 555));
				Font.draw(pType + " (" + (pTime / 60) + ":" + (pTime % 60) + ")", screen, 180, 17 + i * Font.textHeight(), pcol);
			}
		}
		
		
		// This is the status icons, like health hearts, stamina bolts, and hunger "burgers".
		if (!isMode("creative")) {
			for (int i = 0; i < Player.maxStat; i++) {
				int color;
				
				// renders armor
				int armor = player.armor*Player.maxStat / Player.maxArmor;
				if (i <= armor && player.curArmor != null) {
					screen.render(i * 8, Screen.h - 24, 3 + 12 * 32, 0);
				}
				
				// renders your current red hearts, or black hearts for damaged health.
				if (i < player.health) {
					screen.render(i * 8, Screen.h - 16, 3072 + (0 + 2 * 32), 0);
				} else {
					screen.render(i * 8, Screen.h - 16, 3072 + (0 + 3 * 32), 0);
				}
				
				if (player.staminaRechargeDelay > 0) {
					// creates the white/gray blinking effect when you run out of stamina.
					screen.render(i * 8, Screen.h - 8, 3072 + (1 + 3 * 32), 0);
				} else {
					// renders your current stamina, and uncharged gray stamina.
					if (i < player.stamina) {
						screen.render(i * 8, Screen.h - 8, 3072 + (1 + 2 * 32), 0);
					} else {
						screen.render(i * 8, Screen.h - 8, 3072 + (1 + 3 * 32), 0);
					}
				}
				
				// renders hunger
				color = (i < player.hunger) ? Color.get(-1, 100, 530, 211) : Color.get(-1, 100, 0, 0);
				screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2 + 12 * 32, 0);
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
			info.add("VERSION " + Initializer.VERSION);
			info.add(Initializer.fra + " fps");
			info.add("day tiks " + Updater.tickCount+" ("+Updater.getTime()+")");
			info.add((Updater.normSpeed * Updater.gamespeed) + " tik/sec");
			if(!isValidServer()) {
				info.add("walk spd " + player.moveSpeed);
				info.add("X " + (player.x / 16) + "-" + (player.x % 16));
				info.add("Y " + (player.y / 16) + "-" + (player.y % 16));
				if(levels[currentLevel] != null)
					info.add("Tile " + levels[currentLevel].getTile(player.x>>4, player.y>>4).name);
				if (isMode("score")) info.add("Score " + player.getScore());
			}
			if(levels[currentLevel] != null) {
				if(!isValidClient())
					info.add("Mob Cnt " + levels[currentLevel].mobCount + "/" + levels[currentLevel].maxMobCount);
				else
					info.add("Mob Load Cnt " + levels[currentLevel].mobCount);
			}
			
			/// Displays number of chests left, if on dungeon level.
			if (levels[currentLevel] != null && (isValidServer() || currentLevel == 5 && !isValidClient())) {
				if (levels[5].chestCount > 0)
					info.add("Chests: " + levels[5].chestCount);
				else
					info.add("Chests: Complete!");
			}
			
			if(!isValidServer()) {
				info.add("Hunger stam: " + player.getDebugHunger());
				if(player.armor > 0) {
					info.add("armor: " + player.armor);
					info.add("dam buffer: " + player.armorDamageBuffer);
				}
			}
			
			FontStyle style = new FontStyle(textcol).setShadowType(Color.BLACK, true).setXPos(1);
			if(Game.isValidServer()) {
				style.setYPos(Screen.h).setRelTextPos(RelPos.TOP_RIGHT, true);
				for(int i = 1; i < info.size(); i++) // reverse order
					info.add(0, info.remove(i));
			} else
				style.setYPos(2);
			Font.drawParagraph(info, screen, style, 2);
		}
	}
	
	/** Renders the "Click to focus" box when you click off the screen. */
	private static void renderFocusNagger() {
		String msg = "Click to focus!"; // the message when you click off the screen.
		Updater.paused = true; //perhaps paused is only used for this.
		int xx = (Screen.w - Font.textWidth(msg)) / 2; // the width of the box
		int yy = (HEIGHT - 8) / 2; // the height of the box
		int w = msg.length(); // length of message in characters.
		int h = 1;
		
		// renders the four corners of the box
		screen.render(xx - 8, yy - 8, 0 + 13 * 32, 0);
		screen.render(xx + w * 8, yy - 8, 0 + 13 * 32, 1);
		screen.render(xx - 8, yy + 8, 0 + 13 * 32, 2);
		screen.render(xx + w * 8, yy + 8, 0 + 13 * 32, 3);
		
		// renders each part of the box...
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy - 8, 1 + 13 * 32, 0); // ...top part
			screen.render(xx + x * 8, yy + 8, 1 + 13 * 32, 2); // ...bottom part
		}
		for (int y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 2 + 13 * 32, 0); // ...left part
			screen.render(xx + w * 8, yy + y * 8, 2 + 13 * 32, 1); // ...right part
		}

		// cover up black spots from gaps in text
		screen.render(xx + (w - 7) * 8, yy, 3 + 13 * 32, 0);
		screen.render(xx + (w - 10) * 8, yy, 3 + 13 * 32, 0);
		
		// renders the focus nagger text with a flash effect...
		if ((Updater.tickCount / 20) % 2 == 0) // ...medium yellow color
			Font.draw(msg, screen, xx, yy, Color.get(5, 333));
		else // ...bright yellow color
			Font.draw(msg, screen, xx, yy, Color.get(5, 555));
	}
	
	
	static java.awt.Dimension getWindowSize() {
		return new java.awt.Dimension(new Float(WIDTH * SCALE).intValue(), new Float(HEIGHT * SCALE).intValue());
	}
}
