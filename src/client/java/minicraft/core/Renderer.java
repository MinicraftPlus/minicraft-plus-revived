package minicraft.core;

import minicraft.core.CrashHandler.ErrorInfo;
import minicraft.core.io.InputHandler;
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
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
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
import minicraft.screen.entry.SelectableStringEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.Quest;
import minicraft.util.Quest.QuestSeries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.util.Arrays;
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
				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);

				// Use this solution without larger scales which use up a lot memory.
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
					screen.render(null, x * 8 - ((xScroll / 4) & 7), y * 8 - ((yScroll / 4) & 7), 0, 0, 0, cloud);
				}
		}

		level.renderBackground(screen, xScroll, yScroll); // Renders current level background
		level.renderSprites(screen, xScroll, yScroll); // Renders level sprites on screen

		// This creates the darkness in the caves
		if ((currentLevel != 3 || Updater.tickCount < Updater.dayLength / 4 || Updater.tickCount > Updater.dayLength / 2) && !isMode("minicraft.displays.world_create.options.game_mode.creative")) {
			lightScreen.clear(0); // This doesn't mean that the pixel will be black; it means that the pixel will be DARK, by default; lightScreen is about light vs. dark, not necessarily a color. The light level it has is compared with the minimum light values in dither to decide whether to leave the cell alone, or mark it as "dark", which will do different things depending on the game level and time of day.
			int brightnessMultiplier = player.potioneffects.containsKey(PotionType.Light) ? 12 : 8; // Brightens all light sources by a factor of 1.5 when the player has the Light potion effect. (8 above is normal)
			level.renderLight(lightScreen, xScroll, yScroll, brightnessMultiplier); // Finds (and renders) all the light from objects (like the player, lanterns, and lava).
			screen.overlay(lightScreen, currentLevel, xScroll, yScroll); // Overlays the light screen over the main screen.
		}
	}

	private static final ItemHotBarRenderer itemHotBarRenderer = new ItemHotBarRenderer();

	private static final class ItemHotBarRenderer {
		private static final int Y_CORNER = Screen.h - 12;
		private static final int SPRITE_X_CORNER = 84;
		private static final int NAME_X_CORNER = 96;
		private static final int NAME_SLOT_WIDTH = 104;

		private @Nullable Item itemHeld;
		private @Nullable ItemRenderingUnit renderingUnit;

		private static class ItemRenderingUnit extends Screen.EntryRenderingUnit {
			private interface ItemRenderingModel {
				static ItemRenderingModel getModel(@NotNull Item item) {
					if (item instanceof StackableItem) {
						return new StackableItemRenderingModel((StackableItem) item);
					} else if (item instanceof ToolItem) {
						return new ToolItemRenderingModel((ToolItem) item);
					} else if (item instanceof WateringCanItem) {
						return new WateringCanItemRenderingModel((WateringCanItem) item);
					} else {
						return new GeneralItemRenderingMode(item);
					}
				}

				int getNameLeftBound();
				int getNameSlotWidth();
				String getName();
				void renderExtra(Screen screen);
				void renderBackground(Screen screen, int x, int y);
				boolean matches(@NotNull Item item);
			}

			private static class GeneralItemRenderingMode implements ItemRenderingModel {
				private final String name;

				public GeneralItemRenderingMode(@NotNull Item item) { // For general non-stackable items
					name = item.getDisplayName();
				}

				public int getNameLeftBound() {
					return NAME_X_CORNER;
				}

				public int getNameSlotWidth() {
					return NAME_SLOT_WIDTH;
				}

				public String getName() {
					return name;
				}

				@Override
				public void renderExtra(Screen screen) {}

				@Override
				public void renderBackground(Screen screen, int x, int y) {
					for (int xx = 0; xx < 15; ++xx)
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, x + xx * 8, y + yy * 8, xx, 10 + yy, 0, hudSheet.getSheet());
				}

				@Override
				public boolean matches(@NotNull Item item) {
					return name.equals(item.getDisplayName());
				}
			}

			private static class StackableItemRenderingModel implements ItemRenderingModel {
				private final String name;
				private final int amount;
				private final String amountStr; // length: 1-3

				public StackableItemRenderingModel(@NotNull StackableItem item) {
					amountStr = (amount = item.count) < 1000 ? String.valueOf(amount) : "999";
					name = item.getDisplayNameUndecorated();
				}

				@Override
				public int getNameLeftBound() {
					return NAME_X_CORNER + (amountStr.length() + 1) * MinicraftImage.boxWidth;
				}

				@Override
				public int getNameSlotWidth() {
					return NAME_SLOT_WIDTH - (amountStr.length() + 1) * MinicraftImage.boxWidth;
				}

				@Override
				public String getName() {
					return name;
				}

				@Override
				public void renderExtra(Screen screen) {
					Font.draw(amountStr, screen, NAME_X_CORNER, Y_CORNER);
					if (amount > 999) { // true then length must be 3
						screen.render(null, 120, Screen.h - 2 * 8,
							15, 10, 0, hudSheet.getSheet());
						screen.render(null, 120, Screen.h - 8,
							15, 11, 0, hudSheet.getSheet());
					}
				}

				@Override
				public void renderBackground(Screen screen, int x, int y) {
					int i = amountStr.length();
					for (int xx = 0; xx < 5; ++xx)
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, x + xx * 8, y + yy * 8,
								xx, 10 + i * 2 + yy, 0, hudSheet.getSheet());
					for (int xx = 5; xx < 15; ++xx)
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, x + xx * 8, y + yy * 8,
								xx, 10 + yy, 0, hudSheet.getSheet());
				}

				@Override
				public boolean matches(@NotNull Item item) {
					return item instanceof StackableItem && ((StackableItem) item).count == amount &&
						name.equals(item.getDisplayNameUndecorated());
				}
			}

			private static abstract class DurableItemRenderingModel implements ItemRenderingModel {
				private final String name;

				// For items that the status can be expressed in percentages
				public DurableItemRenderingModel(@NotNull Item item) {
					name = item.getDisplayName();
				}

				protected abstract int getPercentage();

				@Override
				public int getNameLeftBound() {
//					return NAME_X_CORNER + 5 * 8;
					return NAME_X_CORNER + 3 * 8;
				}

				@Override
				public int getNameSlotWidth() {
//					return NAME_SLOT_WIDTH - 5 * 8;
					return NAME_SLOT_WIDTH - 3 * 8;
				}

				@Override
				public String getName() {
					return name;
				}

				@Override
				public void renderExtra(Screen screen) {
					int v = getPercentage();
					int green = (int) (v * 2.55f); // Let duration show as normal.
					int color = Color.get(1, 255 - green, green, 0);
//					String s = Localization.getLocalized("minicraft.display.gui.item_durability", v);
//					Font.draw(s, screen, NAME_X_CORNER + (4 - s.length()) * 8, Y_CORNER, color);
					int d = v;
					for (int x = 0; x < 20; ++x) {
						if (d == 0) break;
						int g = Math.min(d, 5);
						d -= g;
						--g;
						int col = Color.get(1, 255 - 255 * g / 4, 255 * g / 4, 0);
						for (int y = 0; y < 8; ++y) {
							screen.pixels[NAME_X_CORNER + x + (Y_CORNER + y) * Screen.w] = col;
						}
					}
					int l = Math.round(v / 12.5f);
					for (int i = 0; i < l; ++i) {
						screen.pixels[SPRITE_X_CORNER + 8 + (Y_CORNER + 7 - i) * Screen.w] = color;
					}
				}

				@Override
				public void renderBackground(Screen screen, int x, int y) {
//					for (int xx = 0; xx < 6; ++xx)
//						for (int yy = 0; yy < 2; ++yy)
//							screen.render(null, x + xx * 8, y + yy * 8,
//								5 + xx, 12 + yy, 0, hudSheet.getSheet());
//					for (int xx = 6; xx < 15; ++xx)
//						for (int yy = 0; yy < 2; ++yy)
//							screen.render(null, x + xx * 8, y + yy * 8,
//								xx, 10 + yy, 0, hudSheet.getSheet());
					for (int xx = 0; xx < 4; ++xx)
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, x + xx * 8, y + yy * 8,
								5 + xx, 14 + yy, 0, hudSheet.getSheet());
					for (int xx = 4; xx < 15; ++xx)
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, x + xx * 8, y + yy * 8,
								xx, 10 + yy, 0, hudSheet.getSheet());
				}

				@Override
				public abstract boolean matches(@NotNull Item item);
			}

			private static class ToolItemRenderingModel extends DurableItemRenderingModel {
				private final ToolType type;
				private final int level;
				private final int dur;
				private final int percentage;

				public ToolItemRenderingModel(@NotNull ToolItem item) {
					super(item);
					type = item.type;
					level = item.level;
					dur = item.dur;
					percentage = dur * 100 / (type.durability * (level + 1));
				}

				@Override
				protected int getPercentage() {
					return percentage;
				}

				@Override
				public void renderExtra(Screen screen) {
					super.renderExtra(screen);
					// Renders arrow counter
					if (type == ToolType.Bow) {
						int ac = player.getInventory().count(Items.arrowItem);
						String s = isMode("minicraft.displays.world_create.options.game_mode.creative") || ac >= 10000 ?
							"^" : String.valueOf(ac); // "^" is an infinite symbol. TODO Use of "^" -> "∞"
						for (int xx = 0; xx < 3; ++xx)
							for (int yy = 0; yy < 2; ++yy)
								screen.render(null, 88 + xx * 8, Screen.h - 3 * 8 + yy * 8,
									9 + xx, 14 + yy, 0, hudSheet.getSheet());
						for (int i = 0; i < s.length() - 1; ++i)
							for (int yy = 0; yy < 2; ++yy)
								screen.render(null, 112 + i * 8, Screen.h - 3 * 8 + yy * 8,
									12, 14 + yy, 0, hudSheet.getSheet());
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, 112 + (s.length() - 1) * 8, Screen.h - 3 * 8 + yy * 8,
								13, 14 + yy, 0, hudSheet.getSheet());
						for (int yy = 0; yy < 2; ++yy)
							screen.render(null, 120 + (s.length() - 1) * 8, Screen.h - 3 * 8 + yy * 8,
								14, 14 + yy, 0, hudSheet.getSheet());
						// Displays the arrow icon
						screen.render(null, 91, Screen.h - 3 * 8 + 4, 4, 1, 0, hudSheet.getSheet());
						Font.draw(s, screen, 109, Screen.h - 3 * 8 + 4);
					}
				}

				@Override
				public boolean matches(@NotNull Item item) {
					return item instanceof ToolItem && ((ToolItem) item).type == type &&
						((ToolItem) item).level == level && ((ToolItem) item).dur == dur;
				}
			}

			private static class WateringCanItemRenderingModel extends DurableItemRenderingModel {
				private final int val;
				private final int percentage;

				public WateringCanItemRenderingModel(@NotNull WateringCanItem item) {
					super(item);
					val = item.content;
					percentage = val * 100 / item.CAPACITY;
				}

				@Override
				protected int getPercentage() {
					return percentage;
				}

				@Override
				public boolean matches(@NotNull Item item) {
					return item instanceof WateringCanItem && ((WateringCanItem) item).content == val;
				}
			}

			private class StringRenderingEntry implements Screen.ScreenEntry {
				private final EntryScrollingTicker ticker;

				public StringRenderingEntry() {
					ticker = new HorizontalScrollerScrollingTicker(-1);
				}

				@Override
				public int getWidth() {
					return Font.textWidth(renderingModel.getName());
				}

				@Override
				public void tick(InputHandler input) {}

				@Override
				public void tickScrollingTicker(SelectableStringEntry.EntryXAccessor accessor) {
					ticker.tick(accessor);
				}

				@Override
				public boolean isScrollingTickerSet() {
					return true;
				}

				@Override
				public void render(Screen screen, Screen.@Nullable RenderingLimitingModel limitingModel, int x, int y, boolean selected) {
					Font.draw(limitingModel, renderingModel.getName(), screen, x, y);
				}

				@Override
				public void render(Screen screen, Screen.@Nullable RenderingLimitingModel limitingModel, int x, int y, boolean selected, String contain, int containColor) {
					render(screen, limitingModel, x, y, selected); // The remaining parameters are ignored.
				}
			}

			private class ItemNameRenderingLimitingModel extends EntryLimitingModel {
				@Override
				public int getLeftBound() {
					return renderingModel.getNameLeftBound();
				}

				@Override
				public int getRightBound() {
					return renderingModel.getNameLeftBound() + renderingModel.getNameSlotWidth() - 1;
				}

				@Override
				public int getTopBound() {
					return Y_CORNER;
				}

				@Override
				public int getBottomBound() {
					return Y_CORNER + MinicraftImage.boxWidth - 1;
				}
			}

			private class ItemNameRenderingXAccessor extends EntryXAccessor {
				@Override
				public int getWidth() {
					return Font.textWidth(renderingModel.getName());
				}
			}

			public final ItemNameRenderingLimitingModel limitingModel = new ItemNameRenderingLimitingModel();
			public final ItemNameRenderingXAccessor accessor = new ItemNameRenderingXAccessor();
			public final Rectangle entryBounds;

			private final StringRenderingEntry delegate;
			private final Sprite sprite;
			private final ItemRenderingModel renderingModel;

			public ItemRenderingUnit(@NotNull Item item) {
				super(RelPos.CENTER);
				sprite = item.sprite.getSprite();
				renderingModel = ItemRenderingModel.getModel(item);
				entryBounds = new Rectangle(renderingModel.getNameLeftBound(), Y_CORNER, renderingModel.getNameSlotWidth(),
					MinicraftImage.boxWidth, Rectangle.CORNER_DIMS);
				delegate = new StringRenderingEntry();
				if (delegate.getWidth() > entryBounds.getWidth()) // Shifts to the left bound when the box is exceeded.
					resetRelativeAnchorsSynced(RelPos.LEFT);
			}

			public boolean matches(@NotNull Item item) {
				return item.sprite.getSprite() == sprite && renderingModel.matches(item);
			}

			@Override
			protected Rectangle getEntryBounds() {
				return entryBounds;
			}

			@Override
			public ItemNameRenderingLimitingModel getLimitingModel() {
				return limitingModel;
			}

			@Override
			protected EntryXAccessor getXAccessor() {
				return accessor;
			}

			@Override
			public StringRenderingEntry getDelegate() {
				return delegate;
			}

			@Override
			public void render(Screen screen, int y, boolean selected) {
				renderingModel.renderBackground(screen, 88, Screen.h - 2 * 8);
				super.render(screen, y, selected);
				screen.render(null, SPRITE_X_CORNER, Y_CORNER, sprite); // Fixed position
				renderingModel.renderExtra(screen);
			}

			@Override
			public void render(Screen screen, int y, boolean selected, String contain, int containColor) {
				render(screen, y, selected); // The remaining parameters are ignored.
			}
		}

		public void tick() {
			if (itemHeld != player.activeItem) {
				if (player.activeItem == null) {
					itemHeld = null;
					renderingUnit = null;
				} else {
					renderingUnit = new ItemRenderingUnit(itemHeld = player.activeItem);
				}
			} else if (itemHeld == null) {
				renderingUnit = null;
			} else if (renderingUnit != null && !renderingUnit.matches(itemHeld)) {
				renderingUnit = new ItemRenderingUnit(itemHeld); // Refresh
			}

			if (renderingUnit != null)
				renderingUnit.tick(input);
		}

		public void render() {
			if (renderingUnit != null)
				renderingUnit.render(screen, Y_CORNER, true);
		}
	}

	static void tickHotBar() {
		itemHotBarRenderer.tick();
	}

	/**
	 * Renders the main game GUI (hearts, Stamina bolts, name of the current item, etc.)
	 */
	private static void renderGui() {
		// Item held rendering frame
		if (player.activeItem != null) {
			for (int x = 0; x < 16; ++x)
				for (int y = 0; y < 2; ++y)
					screen.render(null, 80 + x * 8, Screen.h - 2 * 8 + y * 8, x, 7 + y, 0, hudSheet.getSheet());
			// Shows active item sprite and name in bottom toolbar.
			itemHotBarRenderer.render();
		} else {
			for (int x = 0; x < 16; ++x)
				screen.render(null, 80 + x * 8, Screen.h - 8, x, 9, 0, hudSheet.getSheet());
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
			for (Localization.LocalizationString n : notifications) {
				print.addAll(Arrays.asList(Font.getLines(n.toString(), Screen.w, Screen.h, 0)));
			}

			// Draw each current notification, with shadow text effect.
			FontStyle style = new FontStyle(Color.WHITE).setShadowType(Color.DARK_GRAY, false)
				.setYPos(Screen.h * 2 / 5).setRelTextPos(RelPos.TOP, false);
			Font.drawParagraph(print, screen, style, 0);
		}


		// SCORE MODE ONLY:
		if (isMode("minicraft.displays.world_create.options.game_mode.score")) {
			int seconds = (int) Math.ceil(Updater.scoreTime / (double) Updater.normSpeed);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes %= 60;
			seconds %= 60;

			int timeCol;
			if (Updater.scoreTime >= 18000) timeCol = Color.get(0, 555);
			else if (Updater.scoreTime >= 3600) timeCol = Color.get(330, 555);
			else timeCol = Color.get(400, 555);

			Font.draw(Localization.getLocalized("minicraft.display.gui.score.time_left",
				Localization.getLocalized(hours > 0 ? "minicraft.display.gui.score.time_left.time_hms" :
					minutes > 0 ? "minicraft.display.gui.score.time_left.time_ms" :
					"minicraft.display.gui.score.time_left.time_s", hours, minutes, seconds)),
				screen, Screen.w / 2 - 9 * 8, 2, timeCol);

			String scoreString = Localization.getLocalized("minicraft.display.gui.score.current_score", player.getScore());
			Font.draw(scoreString, screen, Screen.w - Font.textWidth(scoreString) - 2, 3 + 8, Color.WHITE);

			if (player.getMultiplier() > 1) {
				int multColor = player.getMultiplier() < Player.MAX_MULTIPLIER ? Color.get(-1, 540) : Color.RED;
				String mult = Localization.getLocalized("minicraft.display.gui.score_multiplier", player.getMultiplier());
				Font.draw(mult, screen, Screen.w - Font.textWidth(mult) - 2, 4 + 2 * 8, multColor);
			}
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
		if (!isMode("minicraft.displays.world_create.options.game_mode.creative")) {
			for (int i = 1; i <= 30; i++) {
				// Renders your current red default hearts, golden hearts for 20 HP, obsidian hearts for 30 HP, or black hearts for damaged health.
				if (i < 11) {
					screen.render(null, (i - 1) * 8, Screen.h - 8, 0, 1, 0, hudSheet.getSheet()); // Empty Hearts
				}
				if (i < player.health + 1 && i < 11) {
					screen.render(null, (i - 1) * 8, Screen.h - 8, 0, 0, 0, hudSheet.getSheet());  // Red Hearts
				}
				if (i < player.health + 1 && i < 21 && i >= 11) {
					screen.render(null, (i - 11) * 8, Screen.h - 8, 0, 2, 0, hudSheet.getSheet()); // Yellow Hearts
				}
				if (i < player.health + 1 && i >= 21) {
					screen.render(null, (i - 21) * 8, Screen.h - 8, 0, 3, 0, hudSheet.getSheet()); // Obsidian Hearts
				}
			}
			for (int i = 0; i < Player.maxStat; i++) {

				// Renders armor
				int armor = player.armor * Player.maxStat / Player.maxArmor;
				if (i <= armor && player.curArmor != null) {
					screen.render(null, i * 8, Screen.h - 16, player.curArmor.sprite);
				}

				if (player.staminaRechargeDelay > 0) {
					// Creates the white/gray blinking effect when you run out of stamina.
					if (player.staminaRechargeDelay / 4 % 2 == 0) {
						screen.render(null, i * 8 + (Screen.w - 80), Screen.h - 8, 1, 2, 0, hudSheet.getSheet());
					} else {
						screen.render(null, i * 8 + (Screen.w - 80), Screen.h - 8, 1, 1, 0, hudSheet.getSheet());
					}
				} else {
					// Renders your current stamina, and uncharged gray stamina.
					if (i < player.stamina) {
						screen.render(null, i * 8 + (Screen.w - 80), Screen.h - 8, 1, 0, 0, hudSheet.getSheet());
					} else {
						screen.render(null, i * 8 + (Screen.w - 80), Screen.h - 8, 1, 1, 0, hudSheet.getSheet());
					}
				}

				// Renders hunger
				if (i < player.hunger) {
					screen.render(null, i * 8 + (Screen.w - 80), Screen.h - 16, 2, 0, 0, hudSheet.getSheet());
				} else {
					screen.render(null, i * 8 + (Screen.w - 80), Screen.h - 16, 2, 1, 0, hudSheet.getSheet());
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


		screen.render(null, x + (max_bar_length * 2), y, 0, INACTIVE_BOSSBAR, 1, hudSheet.getSheet()); // left corner

		// The middle
		for (int bx = 0; bx < max_bar_length; bx++) {
			for (int by = 0; by < 1; by++) {
				screen.render(null, x + bx * 2, y + by * 8, 3, INACTIVE_BOSSBAR, 0, hudSheet.getSheet());
			}
		}

		screen.render(null, x - 5, y, 0, ACTIVE_BOSSBAR, 0, hudSheet.getSheet()); // right corner

		for (int bx = 0; bx < bar_length; bx++) {
			for (int by = 0; by < 1; by++) {
				screen.render(null, x + bx * 2, y + by * 8, 3, ACTIVE_BOSSBAR, 0, hudSheet.getSheet());
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
				new StringEntry(new Localization.LocalizationString(q.key), Color.WHITE) :
				new StringEntry(q.shouldAllCriteriaBeCompleted() && q.getTotalNumCriteria() > 1 ?
					new Localization.LocalizationString(false, String.format("%s (%d/%d)",
						Localization.getLocalized(series.key), q.getNumCriteriaCompleted(),
						q.getTotalNumCriteria())) :
					new Localization.LocalizationString(series.key), Color.WHITE)
			);

			if (questsShown.size() >= length) break;
		}

		if (questsShown.size() > 0) {
			potionRenderOffset = 9 + (Math.min(questsShown.size(), 3)) * 8 + 8 * 2;
			new Menu.Builder(true, 0, RelPos.RIGHT, questsShown)
				.setPositioning(new Point(Screen.w - 9, 9), RelPos.BOTTOM_LEFT)
				.setTitle(new Localization.LocalizationString("minicraft.displays.quests"))
				.createMenu()
				.render(screen);
		} else {
			potionRenderOffset = 0;
		}
	}

	private static void renderDebugInfo() {
		// Should not localize debug info.
		// TODO Reorganize debug screen someday

		int textcol = Color.WHITE;

		if (showDebugInfo) { // Renders show debug info on the screen.
			ArrayList<String> info = new ArrayList<>();
			info.add(String.format("VERSION: %s", Initializer.VERSION));
			info.add(String.format("%d fps", Initializer.fra));
			info.add(String.format("Day tiks: %d (%s)", Updater.tickCount, Updater.getTime()));
			info.add(String.format("%s tps", Updater.normSpeed * Updater.gamespeed));

			info.add(String.format("walk spd: %s", player.moveSpeed));
			info.add(String.format("X: %d-%d", player.x / 16, player.x % 16));
			info.add(String.format("Y: %d-%d", player.y / 16, player.y % 16));
			if (levels[currentLevel] != null)
				info.add(String.format("Tile: %s", levels[currentLevel].getTile(player.x >> 4, player.y >> 4).name));
			if (isMode("minicraft.displays.world_create.options.game_mode.score"))
				info.add(String.format("Score: %d", player.getScore()));

			if (levels[currentLevel] != null) {
				info.add(String.format("Mob Cnt: %d/%d", levels[currentLevel].mobCount, levels[currentLevel].maxMobCount));
			}

			// Displays number of chests left, if on dungeon level.
			if (levels[currentLevel] != null && currentLevel == 5) {
				if (levels[5].chestCount > 0)
					info.add(String.format("Chests: %d", levels[5].chestCount));
				else
					info.add("Chests: Complete!");
			}


			info.add(String.format("Hunger stam: %s", player.getDebugHunger()));
			if (player.armor > 0) {
				info.add(String.format("Armor: %d", player.armor));
				info.add(String.format("Dam buffer: %d", player.armorDamageBuffer));
			}

			if (levels[currentLevel] != null) {
				info.add(String.format("Seed: %d", levels[currentLevel].getSeed()));
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
		screen.render(null, xx - 8, yy - 8, 0, 6, 0, hudSheet.getSheet());
		screen.render(null, xx + w * 8, yy - 8, 0, 6, 1, hudSheet.getSheet());
		screen.render(null, xx - 8, yy + 8, 0, 6, 2, hudSheet.getSheet());
		screen.render(null, xx + w * 8, yy + 8, 0, 6, 3, hudSheet.getSheet());

		// Renders each part of the box...
		for (int x = 0; x < w; x++) {
			screen.render(null, xx + x * 8, yy - 8, 1, 6, 0, hudSheet.getSheet()); // ...Top part
			screen.render(null, xx + x * 8, yy + 8, 1, 6, 2, hudSheet.getSheet()); // ...Bottom part
		}
		for (int y = 0; y < h; y++) {
			screen.render(null, xx - 8, yy + y * 8, 2, 6, 0, hudSheet.getSheet()); // ...Left part
			screen.render(null, xx + w * 8, yy + y * 8, 2, 6, 1, hudSheet.getSheet()); // ...Right part
		}

		// The middle
		for (int x = 0; x < w; x++) {
			screen.render(null, xx + x * 8, yy, 3, 6, 0, hudSheet.getSheet());
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
