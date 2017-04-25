package com.mojang.ld22.screen;

import com.mojang.ld22.Game;
import com.mojang.ld22.GameApplet;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TitleMenu extends Menu {
	private int selected = 0;
	protected final Random random = new Random();
	private static final String[] options = {"New game", "Instructions", "Tutorial", "Options", "About", "Quit"/*, "Kill"*/}; // Options that are on the main menu.
	int rand;
	int count = 0; // this and reverse are for the logo; they produce the fade-in/out effect.
	boolean reverse = false;
	static boolean loadedunlocks = false;
	String location = Game.gameDir;
	File folder;
	
	private static final String[] splashes = {//new ArrayList<String>();
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
		"Story? What's that?",
		"Multiplayer? What's that?",
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
		"Conquer the Dungeon!",
		"Defeat the Air Wizard!",
		"Loom + Wool = String!",
		"String + Wood = Rod!",
		"Sand + Gunpowder = TNT!",
		"Sleep at Night!",
		"Farm at Day!",
		//"Leave a comment below!",
		"Explanation Mark!",
		"!sdrawkcab si sihT",
		"This is forwards!",
		"Why is this blue?"
		//"try with --debug",
	};
	
	public TitleMenu() {
		folder = new File(location);
		
		rand = random.nextInt(splashes.length);
		loadUnlocks();
	}
	
	/*public void getSplashes() {
		if(!loadedsplashes) {
			//The fun little messages that pop up.
			String[] splashes = {
				
				"MissingNo " + rand
			};
			this.splashes = new ArrayList<String>();
			for(int i = 0; i < splashes.length; i++)
				if(!this.splashes.contains(splashes[i]))
					this.splashes.add(splashes[i]);
			*/
			/*try {
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
			*/
			/*loadedsplashes = true;
		}
	}*/
	
	public void loadUnlocks() {
		if(loadedunlocks) return;
		
		ModeMenu.unlockedtimes.clear();
		BufferedReader unlockReader = null;
		this.folder.mkdirs();

		try {
			unlockReader = new BufferedReader(new FileReader(this.location + "/unlocks.miniplussave"));

			String line;
			while((line = unlockReader.readLine()) != null) {
				Iterator unlocks = Arrays.asList(line.split(",")).iterator();

				while(unlocks.hasNext()) {
					String ulText = (String)unlocks.next();
					if(ulText.contains("AirSkin")) {
						OptionsMenu.unlockedskin = true;
					}

					if(ulText.contains("MINUTEMODE") && !ulText.substring(0, ulText.indexOf("M") + 1).equals("M")) {
						ModeMenu.unlockedtimes.add(ulText.substring(0, ulText.indexOf("M") + 1));
					}
				
					if(ulText.contains("HOURMODE") && !ulText.substring(0, ulText.indexOf("H") + 1).equals("H")) {
						ModeMenu.unlockedtimes.add(ulText.substring(0, ulText.indexOf("H") + 1));
					}
				}
			}
		} catch (FileNotFoundException fnfEx) {
			BufferedWriter fileWriter = null;
			
			try {
				fileWriter = new BufferedWriter(new FileWriter(this.location + "/unlocks.miniplussave"));
				fileWriter.write("");
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if(fileWriter != null) {
						fileWriter.flush();
						fileWriter.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		} finally {
			try {
				if(unlockReader != null) {
					unlockReader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}

		ModeMenu.times.clear();
		loadedunlocks = true;
		//}
	}

	public void tick() {
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		
		int len = options.length;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		if (input.getKey("r").clicked) rand = random.nextInt(splashes.length);
		
		if (reverse == false) {
			count++;
			if (count == 25) reverse = true;
		} else if (reverse == true) {
			count--;
			if (count == 0) reverse = false;
		}
		
		if (input.getKey("enter").clicked) {
			if (options[selected] == "New game") {
				WorldSelectMenu.loadworld = false;
				game.setMenu(new WorldSelectMenu(this));
				//(this method should now stop getting called by Game)
				//BUT: this object is passed to WorldSelectMenu... to go back to?
			}
			if(options[selected] == "Instructions") game.setMenu(new InstructionsMenu(this));
			if (options[selected] == "Tutorial") {
				try {
					//This is for the tutorial Video
					String url = "http://minicraftplus.webs.com/Tutorial.htm";
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (java.io.IOException e) {
					if(com.mojang.ld22.Game.debug) System.out.println(e.getMessage());
				}
			}
			if (options[selected] == "Options") game.setMenu(new OptionsMenu(this));
			if (options[selected] == "About") game.setMenu(new AboutMenu(this));
			if (options[selected] == "Quit") System.exit(0);
			//if (options[selected] == "Kill") {game.level.add(game.player); game.setMenu(null);}
		}
	}
	
	/* This section is used to display the minicraft title */
	
	public void render(Screen screen) {
		screen.clear(0);
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int titleColor = Color.get(0, 010, 131, 551);
		int xo = (screen.w - w * 8) / 2; // X location of the title
		int yo = 36; // Y location of the title
		int cols = Color.get(0, 550, 550, 550);
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + (y + 6) * 32, titleColor, 0);
			}
		}
		
		/* This section is used to display this options on the screen */
		for (int i = 0; i < options.length; i++) {
			String msg = options[i];
			int col = Color.get(0, 222, 222, 222); // Color of unselected text
			if (i == selected) { //if current option is selected...
				msg = "> " + msg + " <"; // Add the cursors to the sides of the message
				col = Color.get(0, 555, 555, 555); //make it selected color
			}
			Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, (11 + i) * 8, col);
		}
		
		boolean isblue = splashes[rand].contains("blue");
		
		if (count <= 5) cols = isblue?cols = Color.get(0, 5, 5, 5):Color.get(0, 505, 550, 550);
		if (count <= 10 && count > 5) cols = isblue?cols = Color.get(0, 4, 4, 4):Color.get(0, 405, 440, 440);
		if (count <= 15 && count > 10) cols = isblue?cols = Color.get(0, 3, 3, 3):Color.get(0, 305, 330, 330);
		if (count <= 20 && count > 15) cols = isblue?cols = Color.get(0, 2, 2, 2):Color.get(0, 205, 220, 220);
		if (count <= 25 && count > 20) cols = isblue?cols = Color.get(0, 1, 1, 1):Color.get(0, 5, 110, 110);
		
		writeCentered(splashes[rand], screen, 60, cols);
		
		if(GameApplet.isApplet) {
			String greeting = "Welcome!", name = GameApplet.username;
			if(name.length() < 36) greeting = name+"!";
			if(name.length() < 27) greeting = "Welcome, " + greeting;
			
			writeCentered(greeting, screen, 10, Color.get(0, 330, 330, 330));
		}
		
		Font.draw("Version " + Game.VERSION, screen, 1, 1, Color.get(0, 111, 111, 111));
		
		writeCentered("(Arrow keys to move)", screen, screen.h - 25, Color.get(0, 111, 111, 111));
		writeCentered("(Enter to accept, Escape to return)", screen, screen.h - 15, Color.get(0, 111, 111, 111));
	}
}
