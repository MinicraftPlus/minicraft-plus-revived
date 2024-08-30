package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.VersionInfo;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
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
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;
import minicraft.util.BookData;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Random;

public class TitleDisplay extends Display {
	private static final Random random = new Random();

	private int rand;
	private int count = 0; // This and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;

	public TitleDisplay() {
		super(true, false, new Menu.Builder(false, 2, RelPos.CENTER,
				new SelectEntry(new Localization.LocalizationString("minicraft.displays.title.play"),
					() -> Game.setDisplay(
						WorldSelectDisplay.anyWorld() ? new WorldSelectDisplay() : new WorldCreateDisplay())),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display"),
				() -> Game.setDisplay(new OptionsMainMenuDisplay())),
            new SelectEntry(new Localization.LocalizationString("minicraft.displays.skin"),
	            () -> Game.setDisplay(new SkinDisplay())),
			new SelectEntry(new Localization.LocalizationString("minicraft.displays.achievements"),
				() -> Game.setDisplay(new AchievementsDisplay())),
			new SelectEntry(new Localization.LocalizationString("minicraft.displays.title.help"), () ->
				Game.setDisplay(new Display(true, new Menu.Builder(false, 1, RelPos.CENTER,
					new BlankEntry(),
					new SelectEntry(new Localization.LocalizationString("minicraft.displays.how_to_play"),
						() -> Game.setDisplay(new HowToPlayDisplay())),
						new SelectEntry(new Localization.LocalizationString(
							"minicraft.displays.title.help.storyline_guide"), () -> Game.setDisplay(new PagedDisplay(
							new Localization.LocalizationString("minicraft.displays.title.help.storyline_guide"),
							BookData.storylineGuide.collect()))),
					new SelectEntry(new Localization.LocalizationString(
						"minicraft.displays.title.help.about"), () -> Game.setDisplay(new PagedDisplay(
							new Localization.LocalizationString(
								"minicraft.displays.title.help.about"), BookData.about.collect()))),
					new SelectEntry(new Localization.LocalizationString(
						"minicraft.displays.title.help.credits"), () -> Game.setDisplay(new PagedDisplay(
							new Localization.LocalizationString("minicraft.displays.title.help.credits"),
						BookData.credits.collect())))
				).setTitle(new Localization.LocalizationString("minicraft.displays.title.help")).createMenu()))
			),
			new SelectEntry(new Localization.LocalizationString("minicraft.displays.title.quit"), Game::quit)
			)
				.setPositioning(new Point(Screen.w / 2, Screen.h * 3 / 5), RelPos.CENTER)
				.createMenu()
		);
	}

	@Override
	public void init(Display parent) {
		super.init(null); // The TitleScreen never has a parent.

		Renderer.readyToRenderGameplay = false;

		// Check version
		Game.updateHandler.checkForUpdate();

		LocalDateTime time = LocalDateTime.now();
		if (time.getMonth() == Month.DECEMBER) {
			if (time.getDayOfMonth() == 19) rand = 1;
		} else {
			rand = random.nextInt(splashes.length - 2) + 2;
		}

		World.levels = new Level[World.levels.length];

		if (Game.player == null)
			// Was online, need to reset player
			World.resetGame(false);

		WorldSelectDisplay.updateWorlds();
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("F3-r").isClicked()) rand = random.nextInt(splashes.length - 3) + 3;

		super.tick(input);

		VersionInfo latestVersion = Game.updateHandler.getLatestVersion();
		if (latestVersion != null && input.getMappedKey("U").isClicked()) {
			ArrayList<ListEntry> entries = new ArrayList<>();
			entries.add(new StringEntry(new Localization.LocalizationString(latestVersion.version.isDev() ?
				"minicraft.displays.title.update_checker.popup.display.new_pre_available" :
				"minicraft.displays.title.update_checker.popup.display.new_available")));
			entries.add(new StringEntry(new Localization.LocalizationString(false, latestVersion.version.toString())));
			entries.add(new LinkEntry(Color.WHITE, new Localization.LocalizationString(
				"minicraft.displays.title.update_checker.popup.display.action"), latestVersion.releaseUrl) {
				@Override
				public void tick(InputHandler input) {
					if (input.inputPressed("SELECT"))
						Game.exitDisplay(); // Exits popup first.
					super.tick(input);
				}
			});
			Game.setDisplay(new PopupDisplay(null, entries.toArray(new ListEntry[0])));
		}
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
				screen.render(null, xo + x * 8, yo + y * 8, x, y, 0, sheet);
			}
		}

		boolean isblue = splashes[rand].contains("blue");
		boolean isGreen = splashes[rand].contains("Green");
		boolean isRed = splashes[rand].contains("Red");

		if (reverse) {
			count--;
			if (count == 0) reverse = false;
		} else {
			count++;
			if (count == 25) reverse = true;
		}

		/// This isn't as complicated as it looks. It just gets a color based off of count, which oscilates between 0 and 25.
		int bcol = 5 - count / 5; // This number ends up being between 1 and 5, inclusive.
		int splashColor = isblue ? Color.BLUE : isRed ? Color.RED : isGreen ? Color.GREEN : Color.get(1, bcol * 51, bcol * 51, bcol * 25);

		Font.drawCentered(splashes[rand], screen, (Screen.h / 2) - 44, splashColor);

		Font.draw(Localization.getLocalized("minicraft.displays.title.display.version", Game.VERSION), screen, 1, 1, Color.get(1, 51));

		Font.drawCentered(Localization.getLocalized("minicraft.displays.title.display.help"), screen, Screen.h - 20, Color.DARK_GRAY);

		if (Game.updateHandler.anyCheckDid() && !Settings.get("updatecheck").equals("minicraft.settings.update_check.disabled"))
			Font.draw(Game.updateHandler.getStatusMessage(), screen, 0, Screen.h - 8, Game.updateHandler.getStatusMessageColor());
	}

	private static final String[] splashes = {
		"Secret Splash!",
		"Happy birthday Minicraft!",
		// These two above have id specific functionality. Don't move or remove them.
		"Now with skins!",
		"Now with better fishing!",
		"Now with better tools!",
		"Now with better chests!",
		"Now with better dungeons!",
		"Only on PlayMinicraft.com!",
		"Playminicraft.com is the bomb!",
		"The Wiki is weak! Help it!",
		"Notch is Awesome!",
		"Dillyg10 is cool as Ice!",
		"Shylor is the man!",
		"Chris J is great with portals!",
		"AntVenom loves cows! Honest!",
		"You should read Antidious Venomi!",
		"Keep calm!",
		"Get him, Paul!",
		"Forty-Two!",
		"Kill Creeper, get Gunpowder!",
		"Kill Cow, get Beef!",
		"Kill Zombie, get Cloth!",
		"Kill Slime, get Slime!",
		"Kill Skeleton, get Bones!",
		"Kill Sheep, get Wool!",
		"Kill Pig, get Porkchop!",
		"Gold > Iron",
		"Gem > Gold",
		"Story? Uhh...",
		"Infinite terrain? What's that?",
		"Windows? I prefer Doors!",
		"2.5D FTW!",
		"3rd dimension not included!",
		"Null not included",
		"Mouse not included!",
		"No spiders included!",
		"No chickens included!",
		"Grab your friends!",
		"Creepers included!",
		"Skeletons included!",
		"Knights included!",
		"Snakes included!",
		"Cows included!",
		"Sheep included!",
		"Pigs included!",
		"Bigger Worlds!",
		"World types!",
		"World themes!",
		"Milk is an idea!",
		"So we back in the mine,",
		"Pickaxe swinging from side to side",
		"In search of Gems!",
		"Life itself suspended by a thread",
		"Gimmie a bucket!",
		"Farming with water!",
		"Get the High-Score!",
		"Potions ftw!",
		"Beds ftw!",
		"Defeat the Air Wizard!",
		"Conquer the Dungeon!",
		"One down, one to go...",
		"Loom + Wool = String!",
		"String + Wood = Rod!",
		"Sand + Gunpowder = TNT!",
		"Sleep at Night!",
		"Farm at Day!",
		"Explanation Mark!",
		"!sdrawkcab si sihT",
		"This is forwards!",
		"Why is this blue?",
		"Green is a nice color!",
		"Red is my favorite color!",
		"Punch the Moon!",
		"This is String qq!",
		"Why?",
		"hello down there!",
		"That guy is such a sly fox!",
		"Hola señor!",
		"Sonic Boom!",
		"Hakuna Matata!",
		"One truth prevails!",
		"Awesome!",
		"Sweet!",
		"Great!",
		"Cool!",
		"Radical!",
		"011011000110111101101100!",
		"001100010011000000110001!",
		"011010000110110101101101?",
		"...zzz...",
		"They were too redundant",
		"Echoed whispers...",
		"Of knights and men",
		"Death before dishonor!",
	};
}
