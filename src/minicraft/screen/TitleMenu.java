package minicraft.screen;

import java.awt.Point;
import java.util.Random;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;

public class TitleMenu implements MenuData {
	private final Random random = new Random();
	
	private int rand;
	private int count = 0; // this and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;
	
	public TitleMenu() {
		Game.readyToRenderGameplay = false;
		/// this is just in case; though, i do take advantage of it in other places.
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
		
		Game.levels = new Level[Game.levels.length];
		
		// was in init
		if(Game.player == null || Game.player instanceof RemotePlayer) {
			//if(Game.player != null) Game.player.remove();
			Game.player = null;
			Game.resetGame();
		}
	}
	
	@Override
	public Menu getMenu() {
		return new Menu(this);
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ListEntry[] {
			entryFactory("Play", menuFactory(
				entryFactory("Load World", new WorldSelectMenu()),
				entryFactory("New World", new WorldGenMenu())
			)),
			entryFactory("Join Online World", new MultiplayerMenu()),
			entryFactory("Options", new OptionsMenu()),
			entryFactory("Help", menuFactory(
				entryFactory("Instructions", new BookDisplay(Displays.instructions)),
				entryFactory("About", new BookDisplay(Displays.about))
			)),
			new SelectEntry("Quit", () -> System.exit(0))
		};
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
	}
	
	@Override
	public void render(Screen screen) {
		screen.clear(0);
		
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int titleColor = Color.get(-1, 10, 131, 551);
		int xo = (Screen.w - w * 8) / 2; // X location of the title
		int yo = 36; // Y location of the title
		int cols = Color.get(-1, 550);
		
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
		cols = isblue ? Color.get(-1, bcol) : isRed ? Color.get(-1, bcol*100) : isGreen ? Color.get(-1, bcol*10) : Color.get(-1, (bcol-1)*100+5, bcol*100+bcol*10, bcol*100+bcol*10);
		// *100 means red, *10 means green; simple.
		
		Font.drawCentered(splashes[rand], screen, 60, cols);
		
		/*if(GameApplet.isApplet) {
			String greeting = "Welcome!", name = GameApplet.username;
			if(name.length() < 36) greeting = name+"!";
			if(name.length() < 27) greeting = "Welcome, " + greeting;
			
			Font.drawCentered(greeting, screen, 10, Color.get(-1, 330));
		}*/
		
		Font.draw("Version " + Game.VERSION, screen, 1, 1, Color.get(-1, 111));
		
		Font.drawCentered("("+Game.input.getMapping("up")+", "+Game.input.getMapping("down")+" to select)", screen, Screen.h - 32, Color.get(-1, 111));
		Font.drawCentered("("+Game.input.getMapping("select")+" to accept)", screen, Screen.h - 22, Color.get(-1, 111));
		Font.drawCentered("("+Game.input.getMapping("exit")+" to return)", screen, Screen.h - 12, Color.get(-1, 111));
		
		
	}
	
	@Override
	public Centering getCentering() { return Centering.make(new Point(Game.WIDTH/2, Game.HEIGHT*3/5), true); }
	
	@Override
	public int getSpacing() {
		return 2;
	}
	
	private static final String[] splashes = {//new ArrayList<String>();
		"Multiplayer Now Included!",
		"Also play InfinityTale!",
		"Also play Minicraft Deluxe!",
		"Also play Alecraft!",
		"Also play Hackcraft!",
		"Also play MiniCrate!",
		"Also play MiniCraft Mob Overload!",
		"Only on PlayMinicraft.com!",
		"Playminicraft.com is the bomb!",
		"@MinicraftPlus on Twitter",
		"MinicraftPlus on Youtube",
		//"Join the Forums!",
		//"The Wiki is weak! Help it!",
		"Notch is Awesome!",
		"Dillyg10 is cool as Ice!",
		"Shylor is the man!",
		"AntVenom loves cows! Honest!",
		"You should read Antidious Venomi!",
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
		//"Alpha? What's that?",
		//"Beta? What's that?",
		//"Infdev? What's that?",
		"Story? Hmm...",
		"Infinite terrain? What's that?",
		"Redstone? What's that?",
		//"Spiders? What are those?",
		"Minecarts? What are those?",
		"3D? What's that?",
		"3.1D is the new thing!",
		"Windows? I perfer Doors!",
		//"Mouse? I perfer Keyboard!",
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
		//"Saving Now Included!",
		//"Loading Now Included!",
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
		"Made with 10000% Vitamin Z!",
		"Too much DP!",
		"Y U NO BOAT!?",
		//"PBAT is in the house!",
		"Punch the Moon!",
		"This is String qq!",
		"Why?",
		"You are null!",
		"That guy is such a sly fox!",
		"Hola senor!",
		"Sonic Boom!",
		"Hakuna Matata!",
		"One truth prevails!",
		//"1.8? Ehhhh....",
		"011011000110111101101100!",
		"001100010011000000110001!",
		"011010000110110101101101?",
		//"Buckets? YESH!",
		"Press \"R\"!",
		"Get the High-Score!",
		"Awesome!",
		"Sweet!",
		"Cool!",
		"Radical!",
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
		//"Leave a comment below!",
		"Explanation Mark!",
		"!sdrawkcab si sihT",
		"This is forwards!",
		"Why is this blue?",
		"Green is a nice color!",
		"Red is my favorite color!"
		//"try with --debug",
	};
}
