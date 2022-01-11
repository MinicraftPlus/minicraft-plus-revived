package minicraft.screen;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Random;

import minicraft.util.BookData;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Renderer;
import minicraft.core.VersionInfo;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.LinkEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class TitleDisplay extends Display {
	private static final Random random = new Random();
	
	private int rand;
	private int count = 0; // This and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;
	
	public TitleDisplay() {
		super(true, false, new Menu.Builder(false, 2, RelPos.CENTER,
			new StringEntry("Checking for updates...", Color.BLUE),
			new BlankEntry(),
			new BlankEntry(),
			new SelectEntry("Play", () -> /*Game.setMenu(new PlayDisplay())*/{
				if (WorldSelectDisplay.getWorldNames().size() > 0)
					Game.setMenu(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
						new SelectEntry("Load World", () -> Game.setMenu(new WorldSelectDisplay())),
						new SelectEntry("New World", () -> Game.setMenu(new WorldGenDisplay()))
					).createMenu()));
				else Game.setMenu(new WorldGenDisplay());
			}),
			new SelectEntry("Options", () -> Game.setMenu(new OptionsMainMenuDisplay())),
            new SelectEntry("Skins", () -> Game.setMenu(new SkinDisplay())),
			new SelectEntry("Achievements", () -> Game.setMenu(new AchievementsDisplay())),
				new SelectEntry("Help", () ->
						Game.setMenu(new Display(true, new Menu.Builder(false, 1, RelPos.CENTER,
								new BlankEntry(),
								new SelectEntry("Instructions", () -> Game.setMenu(new BookDisplay(BookData.instructions))),
								new SelectEntry("Storyline Guide", () -> Game.setMenu(new BookDisplay(BookData.storylineGuide))),
								new SelectEntry("About", () -> Game.setMenu(new BookDisplay(BookData.about))),
								new SelectEntry("Credits", () -> Game.setMenu(new BookDisplay(BookData.credits)))
						).setTitle("Help").createMenu()))
				),
			new SelectEntry("Quit", Game::quit)
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

		/// This is useful to just ensure that everything is really reset as it should be. 
		if (Game.server != null) {
			if (Game.debug) System.out.println("Wrapping up loose server ends.");
			Game.server.endConnection();
			Game.server = null;
		}
		if (Game.client != null) {
			if (Game.debug) System.out.println("Wrapping up loose client ends.");
			Game.client.endConnection();
			Game.client = null;
		}
		Game.ISONLINE = false;

		LocalDateTime time = LocalDateTime.now();
		if (time.getMonth() == Month.DECEMBER) {
			if (time.getDayOfMonth() == 19) rand = 1;
			if (time.getDayOfMonth() == 25) rand = 2;
		} else {
			rand = random.nextInt(splashes.length - 3) + 3;
		}
		
		World.levels = new Level[World.levels.length];
		
		if(Game.player == null || Game.player instanceof RemotePlayer)
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
				menus[0].updateEntry(0, new StringEntry(Localization.getLocalized("New: ") + latestVersion.releaseName, Color.GREEN));
				menus[0].updateEntry(1, new LinkEntry(Color.CYAN, "--Select here to Download--", latestVersion.releaseUrl, "Direct link to latest version: " + latestVersion.releaseUrl));
			}
			else if (latestVersion.releaseName.length() > 0)
				menus[0].updateEntry(0, new StringEntry(Localization.getLocalized("You have the latest version."), Color.DARK_GRAY));
			else
				menus[0].updateEntry(0, new StringEntry(Localization.getLocalized("Could not check for updates."), Color.RED));
		}
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("r").clicked && Game.debug) rand = random.nextInt(splashes.length - 3) + 3;

		super.tick(input);
	}
	
	@Override
	public void render(Screen screen) {
		super.render(screen);
		
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int xo = (Screen.w - w * 8) / 2; // X location of the title
		int yo = 28; // Y location of the title
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + y * 32, 0, 3);
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
		int splashColor = isblue ? Color.BLUE : isRed ? Color.RED : isGreen ? Color.GREEN : Color.get(1, bcol*51, bcol*51, bcol*25);

		
		Font.drawCentered(splashes[rand], screen, 52, splashColor);
		
		Font.draw("Version " + Game.VERSION, screen, 1, 1, Color.get(1, 51));
		
		
		String upString = "(" + Game.input.getMapping("cursor-up") + ", " + Game.input.getMapping("cursor-down") + Localization.getLocalized(" to select") + ")";
		String selectString = "(" + Game.input.getMapping("select") + Localization.getLocalized(" to accept") +")";
		String exitString = "(" + Game.input.getMapping("exit") + Localization.getLocalized(" to return") +")";
		
		Font.drawCentered(upString, screen, Screen.h - 32, Color.DARK_GRAY);
		Font.drawCentered(selectString, screen, Screen.h - 22, Color.DARK_GRAY);
		Font.drawCentered(exitString, screen, Screen.h - 12, Color.DARK_GRAY);
	}
	
	private static final String[] splashes = {
		"Secret Splash!",
		"Happy birthday Minicraft!",
		"Happy XMAS!",
                "Now with Customizable Skins!",
                "Skin Update by Litorom1 and El Virus!",
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
		"Oh Hi Mark",
		"Use the force!",
		"Keep calm!",
		"Get him, Steve!",
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
		"Test == InDev!",
		"Story? Uhh...",
		"Infinite terrain? What's that?",
		"Redstone? What's that?",
		"Minecarts? What are those?",
		"Windows? I prefer Doors!",
		"2.5D FTW!",
		"3rd dimension not included!",
		"Null not included",
		"Mouse not included!",
		"No spiders included!",
		"No Endermen included!",
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
		"Sugarcane is an idea!",
		"Milk is an idea!",
		"So we back in the mine,",
		"Pickaxe swinging from side to side",
		"In search of Gems!",
		"Life itself suspended by a thread",
		"saying ay-oh, that creeper's KO'd!",
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
		"Made with 10000% Vitamin Z!",
		"Punch the Moon!",
		"This is String qq!",
		"Why?",
		"hello down there!",
		"That guy is such a sly fox!",
		"Hola senor!",
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
	};
}
