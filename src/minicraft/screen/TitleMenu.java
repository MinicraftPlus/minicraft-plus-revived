package minicraft.screen;

import java.util.Random;
import minicraft.Game;
import minicraft.GameApplet;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.screen.entry.StringEntry;

public class TitleMenu extends Menu {
	protected final Random random = new Random();
	
	private static final StringEntry[] optionList = new StringEntry[] {
			new StringEntry("New game") {
				public void onSelect() {
					WorldSelectMenu.loadworld = false;
					Game.main.setMenu(new WorldSelectMenu());
					//(this method should now stop getting called by Game)
				}
			},
			new StringEntry("Join Online World") {
				public void onSelect() {
					Game.main.setMenu(new MultiplayerMenu());
				}
			},
			new StringEntry("Instructions") {
				public void onSelect() {
					Game.main.setMenu(Displays.instructions);
				}
			},
			new StringEntry("Tutorial") {
				public void onSelect() {
					try {
						//This is for the tutorial Video
						String url = "http://minicraftplus.webs.com/Tutorial.htm";
						java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
					} catch (java.io.IOException e) {
						if(minicraft.Game.debug) System.out.println(e.getMessage());
					}
				}
			},
			new StringEntry("Options") {
				public void onSelect() {
					Game.main.setMenu(Displays.options);
				}
			},
			new StringEntry("Change Key Bindings") {
				public void onSelect() {
					Game.main.setMenu(new KeyInputMenu(Game.main.input.getKeyPrefs()));
				}
			},
			new StringEntry("About") {
				public void onSelect() {
					Game.main.setMenu(Displays.about);
				}
			},
			new StringEntry("Quit") {
				public void onSelect() {
					System.exit(0);//Game.quit();
				}
			}/*,
			new StringEntry("Kill") {
				public void onSelect() {
					Game.levels[currentLevel].add(Game.player);
					Game.setMenu(null);
				}
			}*/
	}; // Options that are on the main menu.
	//private static final StringEntry[] optionList = StringEntry.useStringArray("New game", "Join Online World", "Instructions", "Tutorial", "Options", "Change Key Bindings", "About", "Quit"/*, "Kill"*/); // Options that are on the main menu.
	
	private int rand;
	private int count = 0; // this and reverse are for the logo; they produce the fade-in/out effect.
	private boolean reverse = false;
	
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
	
	public TitleMenu() {
		super(optionList);
		setTextStyle(new FontStyle());
		//super(options, 11*8, 1, , );
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
	}
	
	public void init(InputHandler input, Display parent) {
		super.init(input, parent);
		if(Game.player == null || Game.player instanceof RemotePlayer) {
			//if(Game.player != null) Game.player.remove();
			Game.player = null;
			Game.resetGame();
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
		
		if (!reverse) {
			count++;
			if (count == 25) reverse = true;
		} else {
			count--;
			if (count == 0) reverse = false;
		}
		
		if (input.getKey("select").clicked) {
			/*Sound.confirm.play();
			
			if (options[selected].equals("New game")) {
				WorldSelectMenu.loadworld = false;
				Game.setMenu(new WorldSelectMenu());
				//(this method should now stop getting called by Game)
			}
			if(options[selected].contains("Join Online")) Game.setMenu(new MultiplayerMenu());
			if(options[selected].equals("Instructions")) Game.setMenu(new InstructionsMenu(this));
			if (options[selected].equals("Tutorial")) {
				try {
					//This is for the tutorial Video
					String url = "http://minicraftplus.webs.com/Tutorial.htm";
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (java.io.IOException e) {
					if(minicraft.Game.debug) System.out.println(e.getMessage());
				}
			}
			if (options[selected].equals("Options")) Game.setMenu(new OptionsMenu(this));
			if (options[selected].equals("Change Key Bindings")) Game.setMenu(new KeyInputMenu(this));
			if (options[selected].equals("About")) Game.setMenu(new AboutMenu(this));
			if (options[selected].equals("Quit")) System.exit(0);//Game.quit();
			//if (options[selected].equals("Kill")) {Game.levels[currentLevel].add(Game.player); Game.setMenu(null);}*/
		}
	}
	
	/* This section is used to display the minicraft title */
	
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
		
		Font.drawCentered("("+input.getMapping("up")+", "+input.getMapping("down")+" to select)", screen, Screen.h - 32, Color.get(-1, 111));
		Font.drawCentered("("+input.getMapping("select")+" to accept)", screen, Screen.h - 22, Color.get(-1, 111));
		Font.drawCentered("("+input.getMapping("exit")+" to return)", screen, Screen.h - 12, Color.get(-1, 111));
	}
}
