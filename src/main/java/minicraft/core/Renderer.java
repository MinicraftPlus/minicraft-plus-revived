package minicraft.core;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import minicraft.core.CrashHandler.ErrorInfo;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.DotUpdater.TickUpdater;
import minicraft.gfx.Ellipsis.SmoothEllipsis;
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
import minicraft.screen.LoadingDisplay;
import minicraft.screen.Menu;
import minicraft.screen.QuestsDisplay;
import minicraft.screen.RelPos;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Quest;
import minicraft.util.Quest.QuestSeries;

import javax.imageio.ImageIO;

public class Renderer extends Game {
	private Renderer() {}

	public static int HEIGHT = 192;
	public static int WIDTH = 288;
	static float SCALE = 3;

	public static Screen screen; // Creates the main screen

	static Canvas canvas = new Canvas();
	private static BufferedImage image; // Creates an image to be displayed on the screen.

	private static Screen lightScreen; // Creates a front screen to render the darkness in caves (Fog of war).

	public static boolean readyToRenderGameplay = false;
	public static boolean showDebugInfo = false;

	private static Ellipsis ellipsis = new SmoothEllipsis(new TickUpdater());

	private static int potionRenderOffset = 0;

	public static SpriteSheet[] loadDefaultSpriteSheets() {
		SpriteSheet itemSheet, tileSheet, entitySheet, guiSheet, skinsSheet;
		try {
			// These set the sprites to be used.
			itemSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/items.png"))));
			tileSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/tiles.png"))));
			entitySheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/entities.png"))));
			guiSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/gui.png"))));
			skinsSheet = new SpriteSheet(ImageIO.read(Objects.requireNonNull(Game.class.getResourceAsStream("/resources/textures/skins.png"))));
		} catch (NullPointerException e) {
			// If a provided InputStream has no name. (in practice meaning it cannot be found.)
			CrashHandler.crashHandle(e, new ErrorInfo("Sprite Sheet Not Found", ErrorInfo.ErrorType.UNEXPECTED, true, "A sprite sheet was not found."));
			return null;
		} catch (IOException | IllegalArgumentException e) {
			// If there is an error reading the file.
			CrashHandler.crashHandle(e, new ErrorInfo("Sprite Sheet Could Not Be Loaded", ErrorInfo.ErrorType.UNEXPECTED, true, "Could not load a sprite sheet."));
			return null;
		}

		return new SpriteSheet[] { itemSheet, tileSheet, entitySheet, guiSheet, skinsSheet };
	}

	public static void initScreen() {
		SpriteSheet[] sheets = loadDefaultSpriteSheets();
		screen = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4]);
		lightScreen = new Screen(sheets[0], sheets[1], sheets[2], sheets[3], sheets[4]);

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		screen.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		canvas.createBufferStrategy(3);
		canvas.requestFocus();
	}


	/** Renders the current screen. Called in game loop, a bit after tick(). */
	public static void render() {
		if (screen == null) return; // No point in this if there's no gui... :P

		if (readyToRenderGameplay) {
			renderLevel();
			if (player.renderGUI) renderGui();
		}

		if (display != null) // Renders menu, if present.
			display.render(screen);

		if (!canvas.hasFocus()) renderFocusNagger(); // Calls the renderFocusNagger() method, which creates the "Click to Focus" message.


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
			for (int y = 0; y < 28; y++)
				for (int x = 0; x < 48; x++) {
					// Creates the background for the sky (and dungeon) level:
					screen.render(x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 2 + 25 * 32, 0, 1);
				}
		}

		level.renderBackground(screen, xScroll, yScroll); // Renders current level background
		level.renderSprites(screen, xScroll, yScroll); // Renders level sprites on screen

		// This creates the darkness in the caves
		if ((currentLevel != 3 || Updater.tickCount < Updater.dayLength/4 || Updater.tickCount > Updater.dayLength/2) && !isMode("minicraft.settings.mode.creative")) {
			lightScreen.clear(0); // This doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
			int brightnessMultiplier = player.potioneffects.containsKey(PotionType.Light) ? 12 : 8; // Brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // Finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, currentLevel, xScroll, yScroll); // Overlays the light screen over the main screen.
		}
	}


	/** Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.) */
	private static void renderGui() {
		// This draws the black square where the selected item would be if you were holding it
		if (!isMode("minicraft.settings.mode.creative") || player.activeItem != null) {
			for (int x = 10; x < 26; x++) {
				screen.render(x * 8, Screen.h - 8, 31, 0, 3);
			}
		}

		// Shows active item sprite and name in bottom toolbar.
		if (player.activeItem != null) {
			player.activeItem.renderHUD(screen, 10 * 8, Screen.h - 8, Color.WHITE);
		}



		// This checks if the player is holding a bow, and shows the arrow counter accordingly.
		if (player.activeItem instanceof ToolItem) {
			if (((ToolItem)player.activeItem).type == ToolType.Bow) {
				int ac = player.getInventory().count(Items.arrowItem);
				// "^" is an infinite symbol.
				if (isMode("minicraft.settings.mode.creative") || ac >= 10000)
					Font.drawBackground("	x" + "^", screen, 84, Screen.h - 16);
				else
					Font.drawBackground("	x" + ac, screen, 84, Screen.h - 16);
				// Displays the arrow icon
				screen.render(10 * 8 + 4, Screen.h - 16, 4 + 3 * 32, 0, 3);
			}
		}

		ArrayList<String> permStatus = new ArrayList<>();
		if (Updater.saving) permStatus.add(Localization.getLocalized("minicraft.display.gui.perm_status.saving", Math.round(LoadingDisplay.getPercentage())));
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
				.setYPos(Screen.h*2/5).setRelTextPos(RelPos.TOP, false);
			Font.drawParagraph(print, screen, style, 0);
		}


		// SCORE MODE ONLY:
		if (isMode("minicraft.settings.mode.score")) {
			int seconds = (int)Math.ceil(Updater.scoreTime / (double)Updater.normSpeed);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;

			int timeCol;
			if (Updater.scoreTime >= 18000) timeCol = Color.get(0, 555);
			else if (Updater.scoreTime >= 3600) timeCol = Color.get(330, 555);
			else timeCol = Color.get(400, 555);

			Font.draw(Localization.getLocalized("minicraft.display.gui.score.time_left", hours > 0 ? hours + "h " : "", minutes, seconds), screen, Screen.w / 2-9 * 8, 2, timeCol);

			String scoreString = Localization.getLocalized("minicraft.display.gui.score.current_score", player.getScore());
			Font.draw(scoreString, screen, Screen.w - Font.textWidth(scoreString) - 2, 3 + 8, Color.WHITE);

			if (player.getMultiplier() > 1) {
				int multColor = player.getMultiplier() < Player.MAX_MULTIPLIER ? Color.get(-1, 540) : Color.RED;
				String mult = "X" + player.getMultiplier();
				Font.draw(mult, screen, Screen.w-Font.textWidth(mult)-2, 4 + 2 * 8, multColor);
			}
		}

		// TOOL DURABILITY STATUS
		if (player.activeItem instanceof ToolItem) {
			// Draws the text
			ToolItem tool = (ToolItem) player.activeItem;
			int dura = tool.dur * 100 / (tool.type.durability * (tool.level + 1));
			int green = (int)(dura * 2.55f); // Let duration show as normal.
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
			for (int i = 0; i < Player.maxStat; i++) {

				// Renders armor
				int armor = player.armor * Player.maxStat / Player.maxArmor;
				if (i <= armor && player.curArmor != null) {
					screen.render(i * 8, Screen.h - 24, (player.curArmor.level - 1) + 9 * 32, 0, 0);
				}

				// Renders your current red hearts, or black hearts for damaged health.
				if (i < player.health) {
					screen.render(i * 8, Screen.h - 16, 0 + 2 * 32, 0, 3);
				} else {
					screen.render(i * 8, Screen.h - 16, 0 + 3 * 32, 0, 3);
				}

				if (player.staminaRechargeDelay > 0) {
					// Creates the white/gray blinking effect when you run out of stamina.
					if (player.staminaRechargeDelay / 4 % 2 == 0) {
						screen.render(i * 8, Screen.h - 8, 1 + 4 * 32, 0, 3);
					} else {
						screen.render(i * 8, Screen.h - 8, 1 + 3 * 32, 0, 3);
					}
				} else {
					// Renders your current stamina, and uncharged gray stamina.
					if (i < player.stamina) {
						screen.render(i * 8, Screen.h - 8, 1 + 2 * 32, 0, 3);
					} else {
						screen.render(i * 8, Screen.h - 8, 1 + 3 * 32, 0, 3);
					}
				}

				// Renders hunger
				if (i < player.hunger) {
					screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2 + 2 * 32, 0, 3);
				} else {
					screen.render(i * 8 + (Screen.w - 80), Screen.h - 16, 2 + 3 * 32, 0, 3);
				}
			}
		}

		renderQuestsDisplay();
		renderDebugInfo();
	}

	private static void renderQuestsDisplay() {
		if (!(boolean) Settings.get("showquests")) return;

		boolean expanding = Game.player.questExpanding > 0;
		int length = expanding ? 5 : 2;
		ArrayList<ListEntry> questsShown = new ArrayList<>();
		ArrayList<Quest> doneQuests = QuestsDisplay.getCompletedQuest();
		HashMap<String, QuestsDisplay.QuestStatus> questStatus = QuestsDisplay.getStatusQuests();
		for (Quest q : QuestsDisplay.getUnlockedQuests()) {
			if (!doneQuests.contains(q)) {
				QuestSeries series = q.getSeries();
				questsShown.add(expanding?
					new StringEntry(Localization.getLocalized(q.id) + " (" + QuestsDisplay.getSeriesQuestsCompleted(series) + "/" + series.getSeriesQuests().size() + ")" + (questStatus.get(q.id) != null ? " | " + questStatus.get(q.id) : ""), series.tutorial ? Color.CYAN : Color.WHITE):
					new StringEntry(Localization.getLocalized(series.id) + " (" + QuestsDisplay.getSeriesQuestsCompleted(series) + "/" + series.getSeriesQuests().size() + ")", series.tutorial ? Color.CYAN : Color.WHITE)
				);
			}
		}

		if (questsShown.size() > 0) {
			potionRenderOffset = 9 + (questsShown.size() > 3 ? 3 : questsShown.size()) * 8 + 8 * 2;
			new Menu.Builder(true, 0, RelPos.RIGHT)
				.setPositioning(new Point(Screen.w - 9, 9), RelPos.BOTTOM_LEFT)
				.setDisplayLength(questsShown.size() > length ? length : questsShown.size())
				.setTitle("Quests")
				.setSelectable(false)
				.setEntries(questsShown)
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

	/** Renders the "Click to focus" box when you click off the screen. */
	private static void renderFocusNagger() {

		String msg = "Click to focus!"; // The message when you click off the screen.

		Updater.paused = true; // Perhaps paused is only used for this.
		int xx = (Screen.w - Font.textWidth(msg)) / 2; // The width of the box
		int yy = (HEIGHT - 8) / 2; // The height of the box
		int w = msg.length(); // Length of message in characters.
		int h = 1;

		// Renders the four corners of the box
		screen.render(xx - 8, yy - 8, 0 + 21 * 32, 0, 3);
		screen.render(xx + w * 8, yy - 8, 0 + 21 * 32, 1, 3);
		screen.render(xx - 8, yy + 8, 0 + 21 * 32, 2, 3);
		screen.render(xx + w * 8, yy + 8, 0 + 21 * 32, 3, 3);

		// Renders each part of the box...
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy - 8, 1 + 21 * 32, 0, 3); // ...Top part
			screen.render(xx + x * 8, yy + 8, 1 + 21 * 32, 2, 3); // ...Bottom part
		}
		for (int y = 0; y < h; y++) {
			screen.render(xx - 8, yy + y * 8, 2 + 21 * 32, 0, 3); // ...Left part
			screen.render(xx + w * 8, yy + y * 8, 2 + 21 * 32, 1, 3); // ...Right part
		}

		// The middle
		for (int x = 0; x < w; x++) {
			screen.render(xx + x * 8, yy, 3 + 21 * 32, 0, 3);
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
