package minicraft.screen;

import java.util.Random;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.VersionInfo;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.network.Network;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.LinkEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.BookData;
import minicraft.util.resource.reloader.SplashManager;

public class TitleDisplay extends Display {
	private static final Random random = new Random();

	private String splash;
	private int count = 0; // This and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;

	public TitleDisplay() {

		super(true, false, new Menu.Builder(false, 2, RelPos.CENTER,
			new StringEntry("minicraft.displays.title.display.checking", Color.BLUE),
			new BlankEntry(),
			new SelectEntry("minicraft.displays.title.play", () -> {
				if (WorldSelectDisplay.getWorldNames().size() > 0)
					Game.setDisplay(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
						new SelectEntry("minicraft.displays.title.play.load_world", () -> Game.setDisplay(new WorldSelectDisplay())),
						new SelectEntry("minicraft.displays.title.play.new_world", () -> Game.setDisplay(new WorldGenDisplay()))
					).createMenu()));
				else Game.setDisplay(new WorldGenDisplay());
			}),
			new SelectEntry("minicraft.display.options_display", () -> Game.setDisplay(new OptionsMainMenuDisplay())),
            new SelectEntry("minicraft.displays.skin", () -> Game.setDisplay(new SkinDisplay())),
			new SelectEntry("minicraft.displays.achievements", () -> Game.setDisplay(new AchievementsDisplay())),
			new SelectEntry("minicraft.displays.title.help", () ->
				Game.setDisplay(new Display(true, new Menu.Builder(false, 1, RelPos.CENTER,
					new BlankEntry(),
					new SelectEntry("minicraft.displays.title.help.instructions", () -> Game.setDisplay(new BookDisplay(BookData.instructions.collect()))),
					new SelectEntry("minicraft.displays.title.help.storyline_guide", () -> Game.setDisplay(new BookDisplay(BookData.storylineGuide.collect()))),
					new SelectEntry("minicraft.displays.title.help.about", () -> Game.setDisplay(new BookDisplay(BookData.about.collect()))),
					new SelectEntry("minicraft.displays.title.help.credits", () -> Game.setDisplay(new BookDisplay(BookData.credits.collect())))
				).setTitle("minicraft.displays.title.help").createMenu()))
			),
			new SelectEntry("minicraft.displays.title.quit", Game::quit)
			)
			.setPositioning(new Point(Screen.w/2, Screen.h*3/5), RelPos.CENTER)
			.createMenu()
		);
	}

	@Override
	public void init(Display parent) {
		super.init(null); // The TitleScreen never has a parent.

		Renderer.readyToRenderGameplay = false;

		// Check version
		checkVersion();

		splash = SplashManager.getRandom();

		World.levels = new Level[World.levels.length];

		if(Game.player == null)
			// Was online, need to reset player
			World.resetGame(false);
	}

	private void checkVersion() {
		VersionInfo latestVersion = Network.getLatestVersion();
		if(latestVersion == null) {
			Network.findLatestVersion(this::checkVersion);
		}
		else {
			if (latestVersion.version.compareTo(Game.VERSION, true) > 0) {
				menus[0].updateEntry(0, new StringEntry(Localization.getLocalized("minicraft.displays.title.display.new_version", latestVersion.releaseName), Color.GREEN));
				menus[0].updateEntry(1, new LinkEntry(Color.CYAN, Localization.getLocalized("minicraft.displays.title.select_to_download"), latestVersion.releaseUrl, Localization.getLocalized("minicraft.displays.title.link_to_version", latestVersion.releaseUrl)));
			} else if (latestVersion.releaseName.length() > 0) {
				menus[0].updateEntry(0, new StringEntry("minicraft.displays.title.display.latest_already", Color.DARK_GRAY, true));
			} else {
				menus[0].updateEntry(0, new StringEntry("minicraft.displays.title.display.cannot_check", Color.RED, true));
			}
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("r").clicked && Game.debug) splash = SplashManager.getRandom();

		super.tick(input);
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		MinicraftImage sheet = Renderer.spriteLinker.getSheet(SpriteType.Gui, "title");
		int h = sheet.height / 8; // Height of squares (on the spritesheet)
		int w = sheet.width / 8; // Width of squares (on the spritesheet)
		int xo = (Screen.w - sheet.width) / 2; // X location of the title
		int yo = 26 - sheet.height / 2; // Y location of the title

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x, y, 0, sheet);
			}
		}

		boolean isblue = splash.contains("blue");
		boolean isGreen = splash.contains("Green");
		boolean isRed = splash.contains("Red");

		if (reverse) {
			count--;
			if (count == 0) reverse = false;
		} else {
			count++;
			if (count == 25) reverse = true;
		}

		/// This isn't as complicated as it looks. It just gets a color based off of count, which oscilates between 0 and 25.
		int bcol = 5 - count / 5; // This number ends up being between 1 and 5, inclusive.
		int splashColor = isblue ? Color.BLUE : isRed ? Color.RED : isGreen ? Color.GREEN : Color.get(1, bcol*51, bcol*51, bcol*25);

		Font.drawCentered(splash, screen, (Screen.h / 2) - 44, splashColor);

		Font.draw(Localization.getLocalized("minicraft.displays.title.display.version", Game.VERSION), screen, 1, 1, Color.get(1, 51));


		String upString = Localization.getLocalized("minicraft.displays.title.display.help.0", Game.input.getMapping("cursor-up"), Game.input.getMapping("cursor-down"));
		String selectString = Localization.getLocalized("minicraft.displays.title.display.help.1", Game.input.getMapping("select"));
		String exitString = Localization.getLocalized("minicraft.displays.title.display.help.2", Game.input.getMapping("exit"));

		Font.drawCentered(upString, screen, Screen.h - 30, Color.DARK_GRAY);
		Font.drawCentered(selectString, screen, Screen.h - 20, Color.DARK_GRAY);
		Font.drawCentered(exitString, screen, Screen.h - 10, Color.DARK_GRAY);
	}
}
