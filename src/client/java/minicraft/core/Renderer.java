package minicraft.core;

import minicraft.core.CrashHandler.ErrorInfo;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.DotUpdater.TickUpdater;
import minicraft.gfx.Ellipsis.SmoothEllipsis;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.WateringCanItem;
import minicraft.level.Level;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.Menu;
import minicraft.screen.QuestsDisplay;
import minicraft.screen.RelPos;
import minicraft.screen.TutorialDisplayHandler;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Quest;
import minicraft.util.Quest.QuestSeries;

import javax.imageio.ImageIO;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Renderer extends Game {
	private Renderer() {
	}

	public static int HEIGHT = 192;
	public static int WIDTH = 288;
	static float SCALE = 3;

	public static Screen screen; // Creates the main screen
	public static SpriteLinker spriteLinker = new SpriteLinker(); // The sprite linker for sprites

	static Canvas canvas = new Canvas();
	private static BufferedImage image; // Creates an image to be displayed on the screen.

	private static Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).

	public static boolean readyToRenderGameplay = false;
	public static boolean showDebugInfo = false;

	private static Ellipsis ellipsis = new SmoothEllipsis(new TickUpdater());

	private static int potionRenderOffset = 0;

	private static LinkedSprite hudSheet;

	public static MinicraftImage loadDefaultSkinSheet() {
		MinicraftImage skinsSheet;
		try {
			// These set the sprites to be used.
			skinsSheet = new MinicraftImage(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/skins.png"))));
		} catch (NullPointerException e) {
			// If a provided InputStream has no name. (in practice meaning it cannot be found.)
			CrashHandler.crashHandle(e, new ErrorInfo("Sprite Sheet Not Found", ErrorInfo.ErrorType.UNEXPECTED, true, "A sprite sheet was not found."));
			return null;
		} catch (IOException | IllegalArgumentException e) {
			// If there is an error reading the file.
			CrashHandler.crashHandle(e, new ErrorInfo("Sprite Sheet Could Not be Loaded", ErrorInfo.ErrorType.UNEXPECTED, true, "Could not load a sprite sheet."));
			return null;
		}

		return skinsSheet;
	}

	public static void initScreen() {
		screen = new Screen();
		lightScreen = new Screen();

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		screen.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		hudSheet = new LinkedSprite(SpriteType.Gui, "hud");

		canvas.createBufferStrategy(3);
	}


	/**
	 * Renders the current screen. Called in game loop, a bit after tick().
	 */
	public static void render() {
		if (screen == null) return; // No point in this if there's no gui... :P

		if (readyToRenderGameplay) {
			renderLevel();
			if (player.renderGUI) renderGui();
		}

		if (currentDisplay != null) // Renders menu, if present.
			currentDisplay.render(screen);

		if (!canvas.hasFocus())
			renderFocusNagger(); // Calls the renderFocusNagger() method, which creates the "Click to Focus" message.


		BufferStrategy bs = canvas.getBufferStrategy(); // Creates a buffer strategy to determine how the graphics should be buffered.
		Graphics g = bs.getDrawGraphics(); // Gets the graphics in which java draws the picture
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Draws a rect to fill the whole window (to cover last?)

		// Scale the pixels.
		int ww = getWindowSize().width;
		int hh = getWindowSize().height;

		// Get the image offset.
		int xOffset = (canvas.getWidth() - ww) / 2 + canvas.getParent().getInsets().left;
		int yOffset = (canvas.getHeight() - hh) / 2 + canvas.getParent().getInsets().top;

		// Draw the image on the window.
		g.drawImage(image, xOffset, yOffset, ww, hh, null);

		// Release any system items that are using this method. (so we don't have crappy framerates)
		g.dispose();

		// Make the picture visible.
		bs.show();

		// Screen capturing.
		if (Updater.screenshot > 0) {
			new File(Game.gameDir + "/screenshots/").mkdirs();
			int count = 1;
			LocalDateTime datetime = LocalDateTime.now();
			String stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(datetime);
			File file = new File(String.format("%s/screenshots/%s.png", Game.gameDir, stamp));
			while (file.exists()) {
				file = new File(String.format("%s/screenshots/%s_%s.png", Game.gameDir, stamp, count));
				count++;
			}

			try { // https://stackoverflow.com/a/4216635
				int w = image.getWidth();
				int h = image.getHeight();
				BufferedImage before = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				before.getRaster().setRect(image.getData());
				int scale = (Integer) Settings.get("screenshot");
				// BufferedImage after = BigBufferedImage.create(scale * w, scale * h, BufferedImage.TYPE_INT_RGB);
				AffineTransform at = new AffineTransform();
				at.scale(scale, scale); // Setting the scaling.
				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

				// Use this solution without larger scales which use up a lot of memory.
				// With scale 20, up to around 360MB overall RAM use.
				BufferedImage after = scaleOp.filter(before, null);
				ImageIO.write(after, "png", file);
			} catch (IOException e) {
				CrashHandler.errorHandle(e);
			}

			Updater.screenshot--;
		}
	}


	private static void renderLevel() {
		Level level = levels[currentLevel];
		if (level == null) return;

		int xScroll = player.x - Screen.w / 2; // Scrolls the screen in the x axis.
		int yScroll = player.y - (Screen.h - 8) / 2; // Scrolls the screen in the y axis.

		// Stop scrolling if the screen is at the ...
		if (xScroll < 0) xScroll = 0; // ...Left border.
		if (yScroll < 0) yScroll = 0; // ...Top border.
		if (xScroll > level.w * 16 - Screen.w) xScroll = level.w * 16 - Screen.w; // ...Right border.
		if (yScroll > level.h * 16 - Screen.h) yScroll = level.h * 16 - Screen.h; // ...Bottom border.
		if (currentLevel > 3) { // If the current level is higher than 3 (which only the sky level (and dungeon) is)
			MinicraftImage cloud = spriteLinker.getSheet(SpriteType.Tile, "cloud_background");
			for (int y = 0; y < 28; y++)
				for (int x = 0; x < 48; x++) {
					// Creates the background for the sky (and dungeon) level:
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 0, 0, 0, cloud);
				}
		}

		level.renderBackground(screen, xScroll, yScroll); // Renders current level background
		level.renderSprites(screen, xScroll, yScroll); // Renders level sprites on screen

		// This creates the darkness in the caves
		if ((currentLevel != 3 || Updater.tickCount < Updater.dayLength / 4 || Updater.tickCount > Updater.dayLength / 2) && !isMode("minicraft.settings.mode.creative")) {
			lightScreen.clear(0); // This doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
			int brightnessMultiplier = player.potioneffects.containsKey(PotionType.Light) ? 12 : 8; // Brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // Finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, currentLevel, xScroll, yScroll); // Overlays the light screen over the main screen.
		}
	}


	/**
	 * Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.)
	 */
	private static void renderGui() {
		// This draws the black square where the selected item would be if you were holding it
		if (!isMode("minicraft.settings.mode.creative") || player.activeItem != null) {
			for (int x = 10; x < 26; x++) {
				screen.render(x * 8, Screen.h - 8, 5, 2, 0, hudSheet.getSheet());
			}
		}

		// Shows active item sprite and name in bottom toolbar.
		if (player.activeItem != null) {
			player.activeItem.renderHUD(screen, 10 * 8, Screen.h - 8, Color.WHITE);
		}


		// This checks if the player is holding a bow, and shows the arrow counter accordingly.
		if (player.activeItem instanceof ToolItem) {
			if (((ToolItem) player.activeItem).type == ToolType.Bow) {
				int ac = player.getInventory().count(Items.arrowItem);
				// "^" is an infinite symbol.
				if (isMode("minicraft.settings.mode.creative") || ac >= 10000)
					Font.drawBackground("	x" + "^", screen, 84, Screen.h - 16);
				else
					Font.drawBackground("	x" + ac, screen, 84, Screen.h - 16);
				// Displays the arrow icon
				screen.render(10 * 8 + 4, Screen.h - 16, 4, 1, 0, hudSheet.getSheet());
			}
		}

		ArrayList<String> permStatus = new ArrayList<>();
		if (Updater.saving)
			permStatus.add(Localization.getLocalized("minicraft.display.gui.perm_status.saving", Math.round(LoadingDisplay.getPercentage())));
		if (Bed.sleeping()) permStatus.add(Localization.getLocalized("minicraft.display.gui.perm_status.sleeping"));
		if (Bed.inBed(Game.player)) {
			permStatus.add(Localization.getLocalized("minicraft.display.gui.perm_status.sleep_cancel", input.getMapping("exit")));
		}

		if (permStatus.size() > 0) {
			FontStyle style = new FontStyle(Color.WHITE).setYPos(Screen.h / 2 - 25)
				.setRelTextPos(RelPos.TOP)
				.setShadowType(Color.DARK_GRAY, false);

			Font.drawParagraph(permStatus, screen, style, 1);
		}

		// NOTIFICATIONS

		Updater.updateNoteTick = false;
		if (permStatus.size() == 0 && notifications.size() > 0) {
			Updater.updateNoteTick = true;
			if (notifications.size() > 3) { // Only show 3 notifs max at one time; erase old notifs.
				notifications = notifications.subList(notifications.size() - 3, notifications.size());
			}

			if (Updater.notetick > 180) { // Display time per notification.
				notifications.remove(0);
				Updater.notetick = 0;
			}
			List<String> print = new ArrayList<>();
			for (String n : notifications) {
				for (String l : Font.getLines(n, Screen.w, Screen.h, 0))
					print.add(l);
			}

			// Draw each current notification, with shadow text effect.
			FontStyle style = new FontStyle(Color.WHITE).setShadowType(Color.DARK_GRAY, false)
				.setYPos(Screen.h * 2 / 5).setRelTextPos(RelPos.TOP, false);
			Font.drawParagraph(print, screen, style, 0);
		}


		// SCORE MODE ONLY:
		if (isMode("minicraft.settings.mode.score")) {
			int seconds = (int) Math.ceil(Updater.scoreTime / (double) Updater.normSpeed);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;

			int timeCol;
			if (Updater.scoreTime >= 18000) timeCol = Color.get(0, 555);
			else if (Updater.scoreTime >= 3600) timeCol = Color.get(330, 555);
			else timeCol = Color.get(400, 555);

			Font.draw(Localization.getLocalized("minicraft.display.gui.score.time_left", hours > 0 ? hours + "h " : "", minutes, seconds), screen, Screen.w / 2 - 9 * 8, 2, timeCol);

			String scoreString = Localization.getLocalized("minicraft.display.gui.score.current_score", player.getScore());
			Font.draw(scoreString, screen, Screen.w - Font.textWidth(scoreString) - 2, 3 + 8, Color.WHITE);

			if (player.getMultiplier() > 1) {
				int multColor = player.getMultiplier() < Player.MAX_MULTIPLIER ? Color.get(-1, 540) : Color.RED;
				String mult = "X" + player.getMultiplier();
				Font.draw(mult, screen, Screen.w - Font.textWidth(mult) - 2, 4 + 2 * 8, multColor);
			}
		}

		// TOOL DURABILITY STATUS
		if (player.activeItem instanceof ToolItem) {
			// Draws the text
			ToolItem tool = (ToolItem) player.activeItem;
			int dura = tool.dur * 100 / (tool.type.durability * (tool.level + 1));
			int green = (int) (dura * 2.55f); // Let duration show as normal.
			Font.drawBackground(dura + "%", screen, 164, Screen.h - 16, Color.get(1, 255 - green, green, 0));
		}

		// WATERING CAN CONTAINER STATUS
		if (player.activeItem instanceof WateringCanItem) {
			// Draws the text
			WateringCanItem tin = (WateringCanItem) player.activeItem;
			int dura = tin.content * 100 / tin.CAPACITY;
			int green = (int) (dura * 2.55f); // Let duration show as normal.
			Font.drawBackground(dura + "%", screen, 164, Screen.h - 16, Color.get(1, 255 - green, green, 0));
		}

		// This renders the potions overlay
		if (player.showpotioneffects && player.potioneffects.size() > 0) {

			@SuppressWarnings("unchecked")
			Map.Entry<PotionType, Integer>[] effects = player.potioneffects.entrySet().toArray(new Map.Entry[0]);

			// The key is potion type, value is remaining potion duration.
			if (!player.simpPotionEffects) {
				for (int i = 0; i < effects.length; i++) {
					PotionType pType = effects[i].getKey();
					int pTime = effects[i].getValue() / Updater.normSpeed;
					int minutes = pTime / 60;
					int seconds = pTime % 60;
					Font.drawBackground(Localization.getLocalized("minicraft.display.gui.potion_effects.hide_hint", input.getMapping("potionEffects")), screen, 180, 9);
					Font.drawBackground(Localization.getLocalized("minicraft.display.gui.potion_effects.potion_dur", pType, minutes, seconds), screen, 180, 17 + i * Font.textHeight() + potionRenderOffset, pType.dispColor);
				}
			} else {
				for (int i = 0; i < effects.length; i++) {
					PotionType pType = effects[i].getKey();
					Font.drawBackground(pType.toString().substring(0, 1), screen, Screen.w - 17 - (effects.length - 1 - i) * 8, 9, pType.dispColor);
				}
			}
		}

		// This is the status icons, like health hearts, stamina bolts, and hunger "burgers".
		if (!isMode("minicraft.settings.mode.creative")) {
			for (int i = 1; i <= 30; i++) {
				// Renders your current red default hearts, golden hearts for 20 HP, obsidian hearts for 30 HP, or black hearts for damaged health.
				if (i < 11) {
					screen.render((i - 1) * 8, Screen.h - 16, 0, 1, 0, hudSheet.getSheet()); // Empty Hearts
				}
				if (i < player.health + 1 && i < 11) {
					screen.render((i - 1) * 8, Screen.h - 16, 0, 0, 0, hudSheet.getSheet());  // Red Hearts
				}
				if (i < player.health + 1 && i < 21 && i >= 11) {
					screen.render((i - 11) * 8, Screen.h - 16, 0, 2, 0, hudSheet.getSheet()); // Yellow Hearts
				}
				if (i < player.health + 1 && i >= 21) {
					screen.render((i - 21) * 8, Screen.h - 16, 0, 3, 0, hudSheet.getSheet()); // Obsidian Hearts
				}
			}
			for (int i = 0; i < Player.maxStat; i++) {

				// Renders armor
				int armor = player.armor * Player.maxStat / Player.maxArmor;
				if (i <= armor && player.curArmor != null) {
					screen.render(i * 8, Screen.h - 24, player.curArmor.sprite);
				}

				if (player.staminaRechargeDelay > 0) {
					// Creates the white/gray blinking effect when you run out of stamina.
					if (player.staminaRechargeDelay / 4 % 2 == 0) {
						screen.render(i * 8, Screen.h - 8, 1, 2, 0, hudSheet.getSheet());
					} else {
						screen.render(i * 8, Screen.h - 8, 1, 1, 0, hudSheet.getSheet());
					}
				} else {
					// Renders your current stamina, and uncharged gray stamina.
					if (i < player.stamina) {
						screen.render(i * 8, Screen.h - 8, 1, 0, 0, hudSheet.getSheet());
					} else {
						screen.render(i * 8, Screen.h - 8, 1, 1, 0, hudSheet.getSheet());
					}
				}

				// Renders hunger
				if (i < player.hunger) {
					screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2, 0, 0, hudSheet.getSheet());
				} else {
					screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2, 1, 0, hudSheet.getSheet());
				}
			}
		}

		// Renders the bossbar
		if (!player.isRemoved()) {
			if (AirWizard.active && (player.getLevel().depth == 1)) {
				AirWizard boss = AirWizard.entity;
				renderBossbar((int) ((((float) boss.health) / boss.maxHealth) * 100), "Air wizard");
			} else if (ObsidianKnight.active && (player.getLevel().depth == -4)) {
				ObsidianKnight boss = ObsidianKnight.entity;
				renderBossbar((int) ((((float) boss.health) / boss.maxHealth) * 100), "Obsidian Knight");
			}
		}

		TutorialDisplayHandler.render(screen);
		renderQuestsDisplay();
		renderDebugInfo();
	}

	public static void renderBossbar(int length, String title) {

		int x = Screen.w / 4 - 24;
		int y = Screen.h / 8 - 24;

		int max_bar_length = 100;
		int bar_length = length; // Bossbar size.

		int INACTIVE_BOSSBAR = 4; // sprite x position
		int ACTIVE_BOSSBAR = 5; // sprite x position


		screen.render(x + (max_bar_length * 2), y, 0, INACTIVE_BOSSBAR, 1, hudSheet.getSheet()); // left corner

		// The middle
		for (int bx = 0; bx < max_bar_length; bx++) {
			for (int by = 0; by < 1; by++) {
				screen.render(x + bx * 2, y + by * 8, 3, INACTIVE_BOSSBAR, 0, hudSheet.getSheet());
			}
		}

		screen.render(x - 5, y, 0, ACTIVE_BOSSBAR, 0, hudSheet.getSheet()); // right corner

		for (int bx = 0; bx < bar_length; bx++) {
			for (int by = 0; by < 1; by++) {
				screen.render(x + bx * 2, y + by * 8, 3, ACTIVE_BOSSBAR, 0, hudSheet.getSheet());
			}
		}

		Font.drawCentered(title, screen, y + 8, Color.WHITE);
	}

	private static void renderQuestsDisplay() {
		if (!TutorialDisplayHandler.inQuests()) return;
		if (!(boolean) Settings.get("showquests")) return;

		boolean expanding = Game.player.questExpanding > 0;
		int length = expanding ? 5 : 2;
		ArrayList<ListEntry> questsShown = new ArrayList<>();
		HashSet<Quest> quests = QuestsDisplay.getDisplayableQuests();
		for (Quest q : quests) {
			QuestSeries series = q.getSeries();

			questsShown.add(!expanding ?
				new StringEntry(Localization.getLocalized(q.key), Color.WHITE, false) :
				new StringEntry(q.shouldAllCriteriaBeCompleted() && q.getTotalNumCriteria() > 1 ?
					String.format("%s (%d/%d)", Localization.getLocalized(series.key), q.getNumCriteriaCompleted(), q.getTotalNumCriteria()) :
					Localization.getLocalized(series.key), Color.WHITE, false)
			);

			if (questsShown.size() >= length) break;
		}

		if (questsShown.size() > 0) {
			potionRenderOffset = 9 + (Math.min(questsShown.size(), 3)) * 8 + 8 * 2;
			new Menu.Builder(true, 0, RelPos.RIGHT, questsShown)
				.setPositioning(new Point(Screen.w - 9, 9), RelPos.BOTTOM_LEFT)
				.setTitle("Quests")
				.createMenu()
				.render(screen);
		} else {
			potionRenderOffset = 0;
		}
	}

	private static void renderDebugInfo() {
		// Should not localize debug info.

		int textcol = Color.WHITE;

		if (showDebugInfo) { // Renders show debug info on the screen.
			ArrayList<String> info = new ArrayList<>();
			info.add("VERSION: " + Initializer.VERSION);
			info.add(Initializer.fra + " fps");
			info.add("Day tiks: " + Updater.tickCount + " (" + Updater.getTime() + ")");
			info.add((Updater.normSpeed * Updater.gamespeed) + " tps");

			info.add("walk spd: " + player.moveSpeed);
			info.add("X: " + (player.x / 16) + "-" + (player.x % 16));
			info.add("Y: " + (player.y / 16) + "-" + (player.y % 16));
			if (levels[currentLevel] != null)
				info.add("Tile: " + levels[currentLevel].getTile(player.x >> 4, player.y >> 4).name);
			if (isMode("minicraft.settings.mode.score")) info.add("Score: " + player.getScore());

			if (levels[currentLevel] != null) {
				info.add("Mob Cnt: " + levels[currentLevel].mobCount + "/" + levels[currentLevel].maxMobCount);
			}

			// Displays number of chests left, if on dungeon level.
			if (levels[currentLevel] != null && currentLevel == 5) {
				if (levels[5].chestCount > 0)
					info.add("Chests: " + levels[5].chestCount);
				else
					info.add("Chests: Complete!");
			}


			info.add("Hunger stam: " + player.getDebugHunger());
			if (player.armor > 0) {
				info.add("Armor: " + player.armor);
				info.add("Dam buffer: " + player.armorDamageBuffer);
			}

			if (levels[currentLevel] != null) {
				info.add("Seed: " + levels[currentLevel].getSeed());
			}

			FontStyle style = new FontStyle(textcol).setShadowType(Color.BLACK, true).setXPos(1);
			style.setYPos(2);
			Font.drawParagraph(info, screen, style, 2);
		}
	}

	/**
	 * Renders the "Click to focus" box when you click off the screen.
	 */
	private static void renderFocusNagger() {

		String msg = "Click to focus!"; // The message when you click off the screen.

		Updater.paused = true; // Perhaps paused is only used for this.
		int xx = (Screen.w - Font.textWidth(msg)) / 2; // The width of the box
		int yy = (HEIGHT - 8) / 2; // The height of the box
		int w = msg.length(); // Length of message in characters.
		int h = 1;

		// Renders the four corners of the box
		screen.render(xx - 8, yy - 8, 0, 6, 0, hudSheet.getSheet());
		screen.render(xx + w * 8, yy - 8, 0, 6, 1, hudSheet.getSheet());
		screen.render(xx - 8, yy + 8, 0, 6, 2, hudSheet.getSheet());
		screen.render(xx + w * 8, yy + 8, 0, 6, 3, hudSheet.getSheet());

		// Renders each part of the box...
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy - 8, 1, 6, 0, hudSheet.getSheet()); // ...Top part
			screen.render(xx + x * 8, yy + 8, 1, 6, 2, hudSheet.getSheet()); // ...Bottom part
		}
		for (int y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 2, 6, 0, hudSheet.getSheet()); // ...Left part
			screen.render(xx + w * 8, yy + y * 8, 2, 6, 1, hudSheet.getSheet()); // ...Right part
		}

		// The middle
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy, 3, 6, 0, hudSheet.getSheet());
		}

		// Renders the focus nagger text with a flash effect...
		if ((Updater.tickCount / 20) % 2 == 0) // ...Medium yellow color
			Font.draw(msg, screen, xx, yy, Color.get(1, 153));
		else // ...Bright yellow color
			Font.draw(msg, screen, xx, yy, Color.get(5, 255));
	}


	static java.awt.Dimension getWindowSize() {
		return new java.awt.Dimension((int) (WIDTH * SCALE), (int) (HEIGHT * SCALE));
	}
}
