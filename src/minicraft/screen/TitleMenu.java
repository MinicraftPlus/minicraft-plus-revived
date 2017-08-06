package minicraft.screen;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import minicraft.Game;
import minicraft.GameApplet;
import minicraft.InputHandler;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;

public class TitleMenu extends SelectMenu {
	protected final Random random = new Random();
private static final String[] options = {"New game", "Join Online World", "Instructions", "Tutorial", "Options", "Change Key Bindings", "About", "Quit"/*, "Kill"*/}; // Options that are on the main menu.
	int rand;
	int count = 0; // this and reverse are for the logo; they produce the fade-in/out effect.
	boolean reverse = false;
	String location = Game.gameDir;
	File folder;
	
	private static final String[] splashes = {//new ArrayList<String>();
		"Multiplayer Now Included!",
		"Also play InfinityTale!",
		"Also play Minicraft Delux!",
		"Also play Alecraft!",
		"Also play Hackcraft!",
		//"Also play RPGcraft!(When it's done)",
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
		"Alpha? What's that?",
		"Beta? What's that?",
		//"Infdev? What's that?",
		"Story? I've heard of that...",
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
	
	public TitleMenu() {
		super(Arrays.asList(options), 11*8, 1, Color.get(-1, 555), Color.get(-1, 222));
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
		
		folder = new File(location);
		rand = random.nextInt(splashes.length);
		
		Game.levels = new Level[Game.levels.length];
	}
	
	public void init(Game game, InputHandler input) {
		super.init(game, input);
		if(game.player == null || game.player instanceof RemotePlayer) {
			//if(game.player != null) game.player.remove();
			game.player = null;
			game.resetGame();
		}
	}
	
	/*public void getSplashes() {
		if(!loadedsplashes) {
			try {
				URL br = new URL("http://minicraftplus.webs.com/-splashes.txt");
				URLConnection e = br.openConnection();
				e.setReadTimeout(1000);
				Scanner bufferedWriter = new Scanner(br.openStream());
				splashes.clear();

				while(bufferedWriter.hasNextLine()) {
					String splash = bufferedWriter.nextLine();
					if(splash.contains("]")) {
						if(splash.substring(splash.indexOf("]")).length() > 3) {
							splash = splash.substring(splash.indexOf("]") + 2, splash.length());
						} else continue;
					}
					
					if(splash.length() > 0) splashes.add(splash);
				}

				bufferedWriter.close();
			} catch (MalformedURLException urlEx) {
				urlEx.printStackTrace();
				splashes.clear();
				splashes.add("");
			} catch (ConnectException conEx) {
				conEx.printStackTrace();
				splashes.clear();
				splashes.add("Connection issue! D:");
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
				splashes.clear();
				splashes.add("Offline Mode :<");
			}
			
			loadedsplashes = true;
		}
	}*/
	
	public void tick() {
		super.tick();
		
		if (input.getKey("r").clicked) rand = random.nextInt(splashes.length);
		
		if (reverse == false) {
			count++;
			if (count == 25) reverse = true;
		} else if (reverse == true) {
			count--;
			if (count == 0) reverse = false;
		}
		
		if (input.getKey("select").clicked) {
			if (options[selected] == "New game") {
				WorldSelectMenu.loadworld = false;
				game.setMenu(new WorldSelectMenu());
				//(this method should now stop getting called by Game)
			}
			if(options[selected].contains("Join Online")) game.setMenu(new MultiplayerMenu());
			if(options[selected] == "Instructions") game.setMenu(new InstructionsMenu(this));
			if (options[selected] == "Tutorial") {
				try {
					//This is for the tutorial Video
					String url = "http://minicraftplus.webs.com/Tutorial.htm";
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (java.io.IOException e) {
					if(minicraft.Game.debug) System.out.println(e.getMessage());
				}
			}
			if (options[selected] == "Options") game.setMenu(new OptionsMenu(this));
			if (options[selected] == "Change Key Bindings") game.setMenu(new KeyInputMenu(this));
			if (options[selected] == "About") game.setMenu(new AboutDisplay(this));
			if (options[selected] == "Quit") System.exit(0);//game.quit();
			//if (options[selected] == "Kill") {game.levels[currentLevel].add(game.player); game.setMenu(null);}
		}
	}
	
	/* This section is used to display the minicraft title */
	
	public void render(Screen screen) {
		screen.clear(0);
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int titleColor = Color.get(-1, 010, 131, 551);
		int xo = (screen.w - w * 8) / 2; // X location of the title
		int yo = 36; // Y location of the title
		int cols = Color.get(-1, 550);
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + (y + 6) * 32, titleColor, 0);
			}
		}
		
		/* This is used to display this options on the screen */
		super.render(screen);
		
		boolean isblue = splashes[rand].contains("blue");
		boolean isGreen = splashes[rand].contains("Green");
		boolean isRed = splashes[rand].contains("Red");
		
		/// this isn't as complicated as it looks. It just gets a color based off of count, which oscilates between 0 and 25.
		int bcol = 5 - count / 5; // this number ends up being between 1 and 5, inclusive.
		cols = isblue ? Color.get(-1, bcol) : isRed ? Color.get(-1, bcol*100) : isGreen ? Color.get(-1, bcol*10) : Color.get(-1, (bcol-1)*100+5, bcol*100+bcol*10, bcol*100+bcol*10);
		// *100 means red, *10 means green; simple.
		
		Font.drawCentered(splashes[rand], screen, 60, cols);
		
		if(GameApplet.isApplet) {
			String greeting = "Welcome!", name = GameApplet.username;
			if(name.length() < 36) greeting = name+"!";
			if(name.length() < 27) greeting = "Welcome, " + greeting;
			
			Font.drawCentered(greeting, screen, 10, Color.get(-1, 330));
		}
		
		Font.draw("Version " + Game.VERSION, screen, 1, 1, Color.get(-1, 111));
		
		Font.drawCentered("("+input.getMapping("up")+", "+input.getMapping("down")+" to select)", screen, screen.h - 32, Color.get(-1, 111));
		Font.drawCentered("("+input.getMapping("select")+" to accept)", screen, screen.h - 22, Color.get(-1, 111));
		Font.drawCentered("("+input.getMapping("exit")+" to return)", screen, screen.h - 12, Color.get(-1, 111));
	}
}
