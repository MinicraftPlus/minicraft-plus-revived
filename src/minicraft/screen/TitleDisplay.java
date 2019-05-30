package minicraft.screen;

import java.util.Random;

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
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import org.jetbrains.annotations.NotNull;

public class TitleDisplay extends Display {
	private static final Random random = new Random();
	
	private int rand;
	private int count = 0; // this and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;
	
	public TitleDisplay() {
		super(true, false, new Menu.Builder(false, 2, RelPos.CENTER,
			new StringEntry("Checking for updates...", Color.BLUE),
			new BlankEntry(),
			new BlankEntry(),
			new SelectEntry("Play", () -> {
				if(WorldSelectDisplay.getWorldNames().size() > 0)
					Game.setMenu(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
						new SelectEntry("Load World", () -> Game.setMenu(new WorldSelectDisplay())),
						new SelectEntry("New World", () -> Game.setMenu(new WorldGenDisplay()))
					).createMenu()));
				else
					Game.setMenu(new WorldGenDisplay());
			}),
			new SelectEntry("Join Online World", () -> Game.setMenu(new MultiplayerDisplay())),
			new SelectEntry("Options", () -> Game.setMenu(new OptionsDisplay())),
			displayFactory("Help",
				new SelectEntry("Instructions", () -> Game.setMenu(new BookDisplay(BookData.instructions))),
				new BlankEntry(),
				new SelectEntry("Storyline Guide (for the weak)", () -> Game.setMenu(new BookDisplay(BookData.storylineGuide))),
				new BlankEntry(),
				new SelectEntry("About", () -> Game.setMenu(new BookDisplay(BookData.about)))
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
		
		// check version
		checkVersion();
		
		/// this is useful to just ensure that everything is really reset as it should be. 
		if(Game.server != null) {
			if (Game.debug) System.out.println("wrapping up loose server ends");
			Game.server.endConnection();
			Game.server = null;
		}
		if(Game.client != null) {
			if (Game.debug) System.out.println("wrapping up loose client ends");
			Game.client.endConnection();
			Game.client = null;
		}
		Game.ISONLINE = false;
		
		rand = random.nextInt(splashes.length);
		
		World.levels = new Level[World.levels.length];
		
		if(Game.player == null || Game.player instanceof RemotePlayer)
			// was online, need to reset player
			World.resetGame(false);
	}
	
	private void checkVersion() {
		VersionInfo latestVersion = Network.getLatestVersion();
		if(latestVersion == null) {
			Network.findLatestVersion(this::checkVersion);
		}
		else {
			if(Game.debug) System.out.println("latest version = "+latestVersion.version);
			if(latestVersion.version.compareTo(Game.VERSION) > 0) { // link new version
				menus[0].updateEntry(0, new StringEntry("New: "+latestVersion.releaseName, Color.GREEN));
				menus[0].updateEntry(1, new LinkEntry(Color.CYAN, "--Select here to Download--", latestVersion.releaseUrl, "Direct link to latest version: " + latestVersion.releaseUrl + "\nCan also be found here with change log: https://www.github.com/chrisj42/minicraft-plus-revived/releases"));
			}
			else if(latestVersion.releaseName.length() > 0)
				menus[0].updateEntry(0, new StringEntry("You have the latest version.", Color.DARK_GRAY));
			else
				menus[0].updateEntry(0, new StringEntry("Connection failed, could not check for updates.", Color.RED));
		}
	}
	
	@NotNull
	private static SelectEntry displayFactory(String entryText, ListEntry... entries) {
		return new SelectEntry(entryText, () -> Game.setMenu(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER, entries).createMenu())));
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("r").clicked) rand = random.nextInt(splashes.length);
		
		if (!reverse) {
			count++;
			if (count == 25) reverse = true;
		} else {
			count--;
			if (count == 0) reverse = false;
		}
		
		super.tick(input);
	}
	
	@Override
	public void render(Screen screen) {
		super.render(screen);
		
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int titleColor = Color.get(-1, 10, 131, 551);
		int xo = (Screen.w - w * 8) / 2; // X location of the title
		int yo = 28; // Y location of the title
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + (y + 6) * 32, titleColor, 0);
			}
		}
		
		boolean isblue = splashes[rand].contains("blue");
		boolean isGreen = splashes[rand].contains("Green");
		boolean isRed = splashes[rand].contains("Red");
		
		/// this isn't as complicated as it looks. It just gets a color based off of count, which oscilates between 0 and 25.
		int bcol = 5 - count / 5; // this number ends up being between 1 and 5, inclusive.
		int splashColor = isblue ? Color.get(-1, bcol) : isRed ? Color.get(-1, bcol*100) : isGreen ? Color.get(-1, bcol*10) : Color.get(-1, (bcol-1)*100+5, bcol*100+bcol*10, bcol*100+bcol*10);
		// *100 means red, *10 means green; simple.
		
		Font.drawCentered(splashes[rand], screen, 52, splashColor);
		
		Font.draw("Version " + Game.VERSION, screen, 1, 1, Color.get(-1, 111));
		
		
		String upString = "("+Game.input.getMapping("cursor-up")+", "+Game.input.getMapping("cursor-down")+Localization.getLocalized(" to select")+")";
		String selectString = "("+Game.input.getMapping("select")+Localization.getLocalized(" to accept")+")";
		String exitString = "("+Game.input.getMapping("exit")+ Localization.getLocalized(" to return")+")";
		
		Font.drawCentered(upString, screen, Screen.h - 32, Color.get(-1, 111));
		Font.drawCentered(selectString, screen, Screen.h - 22, Color.get(-1, 111));
		Font.drawCentered(exitString, screen, Screen.h - 12, Color.get(-1, 111));
	}
	
	private static final String[] splashes = {
		"Multiplayer Now Included!",
		// "Also play InfinityTale!",
		// "Also play Minicraft Deluxe!",
		// "Also play Alecraft!",
		// "Also play Hackcraft!",
		// "Also play MiniCrate!",
		// "Also play MiniCraft Mob Overload!",
		"Only on PlayMinicraft.com!",
		"Playminicraft.com is the bomb!",
		// "@MinicraftPlus on Twitter",
		"MinicraftPlus on Youtube",
		//"Join the Forums!",
		"The Wiki is weak! Help it!",
		"Notch is Awesome!",
		"Dillyg10 is cool as Ice!",
		"Shylor is the man!",
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
		"Sugarcane is a Idea!",
		"Milk is an idea!",
		"So we back in the mine,",
		"pickaxe swinging from side to side",
		"Life itself suspended by a thread",
		"In search of Gems!",
		"saying ay-oh, that creeper's KO'd!",
		"Gimmie a bucket!",
		"Farming with water!",
		"Press \"R\"!",
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
		"Y U NO BOAT!?",
		"Made with 10000% Vitamin Z!",
		"Too much DP!",
		"Punch the Moon!",
		"This is String qq!",
		"Why?",
		"You are null!",
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
		"...zzz..."
	};
}
