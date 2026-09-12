package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.util.BookData;
import org.json.JSONObject;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Integration checks use real resource loading, menus, input callbacks and preferences. */
public final class ChineseMenuCheck {

	private static void check(boolean value, String message) {
		if (!value) throw new AssertionError(message);
	}

	private static void render(Display display, File folder, String name) throws Exception {
		BufferedImage image = new BufferedImage(Screen.w, Screen.h, BufferedImage.TYPE_INT_RGB);
		Screen screen = new Screen(image);
		screen.clear(0x101010);
		display.render(screen);
		screen.flush();
		BufferedImage scaled = new BufferedImage(Screen.w * 3, Screen.h * 3, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
		g.dispose();
		ImageIO.write(scaled, "png", new File(folder, name + ".png"));
	}

	private static void select(LanguageSettingsDisplay display, String name) {
		Menu menu = display.menus[0];
		ListEntry[] entries = menu.getEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].toString().startsWith(name + " (")) {
				menu.setSelection(i);
				display.tick(new InputHandler() {
					@Override public boolean inputPressed(String input) { return input.equals("select"); }
				});
				return;
			}
		}
		throw new AssertionError("Language not listed: " + name);
	}

	public static void run(File folder) throws Exception {
		FileHandler.determineGameDir(Files.createTempDirectory(folder.toPath(), "menu-data-").toString());
		Game.input = new InputHandler();
		Settings.set("sound", false);
		ResourcePackDisplay.initPacks();
		ResourcePackDisplay.reloadResources();
		Tiles.initTileList();
		Localization.changeLanguage("en-US");
		OptionsMainMenuDisplay options = new OptionsMainMenuDisplay();
		LanguageSettingsDisplay languages = new LanguageSettingsDisplay();
		languages.init(options);
		Game.setDisplay(languages);
		render(languages, folder, "language-list");
		select(languages, "Chinese");
		check(Localization.getSelectedLocale().equals(Locale.forLanguageTag("zh-CN")), "Actual menu selects Chinese");
		check(Game.getDisplay() == options, "Language selection returns to settings");
		render(options, folder, "settings-after-switch");
		check(options.menus[0].getBounds().getLeft() >= 0 && options.menus[0].getBounds().getRight() <= Screen.w, "Chinese settings fit screen after switch");
		check(options.menus[0].getTitle().equals("主菜单设置"), "Parent menu title refreshes immediately");
		check(BookData.instructions.collect().contains("默认操作"), "Loaded book follows language selection");
		options.onExit();
		File prefs = new File(Game.gameDir, "Preferences.json");
		JSONObject saved = new JSONObject(new String(Files.readAllBytes(prefs.toPath()), "UTF-8"));
		check(saved.getString("lang").equals("zh-CN"), "Chinese persists when settings close");
		Localization.changeLanguage("en-US");
		new Load(true, false);
		check(Localization.getSelectedLocale().equals(Locale.forLanguageTag("zh-CN")), "Saved Chinese reloads");
		render(new LanguageSettingsDisplay(), folder, "language-selected");
		render(new BookDisplay(BookData.instructions.collect()), folder, "instructions");
		render(new BookDisplay(BookData.storylineGuide.collect()), folder, "guide");
		Game.player = new Player(null, Game.input);
		for (String item : new String[] {"Workbench", "Gem Pickaxe", "Watering Can", "Tomato Seeds", "Black Bed"})
			Game.player.getInventory().add(Items.get(item));
		render(new PlayerInvDisplay(Game.player), folder, "inventory");
		LanguageSettingsDisplay back = new LanguageSettingsDisplay();
		Game.setDisplay(back);
		select(back, "English");
		check(Localization.getSelectedLocale().equals(Locale.US), "Actual menu switches back to English");
		render(new OptionsMainMenuDisplay(), folder, "english-settings");
		check(BookData.instructions.collect().startsWith("With the default controls"), "English book restored");
		System.out.println("Menu integration passed: Chinese selection, return, immediate title refresh, save/load, English switch, books and inventory.");
	}
}
