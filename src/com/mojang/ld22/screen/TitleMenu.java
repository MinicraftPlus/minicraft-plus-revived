//mostly new... fewer comments.
package com.mojang.ld22.screen;

import com.mojang.ld22.GameApplet;
import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.screen.AboutMenu;
import com.mojang.ld22.screen.Menu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.screen.WorldSelectMenu;
import com.mojang.ld22.sound.Sound;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class TitleMenu extends Menu {
	private int selected = 0;
	protected final Random random = new Random();
	public static List splashes = new ArrayList<String>();
	private static final String[] options = {"New game", "Instructions", "Tutorial", "Options", "About", "Quit"/*, "Kill"*/}; // Options that are on the main menu.
	//public static boolean sentFromMenu;
	int randcount = 60;
	int rand = random.nextInt(randcount);
	int count = 0; // this and reverse are for the logo; they produce the fade-in/out effect.
	boolean reverse = false;
	static boolean loadedsplashes = false;
	static boolean loadedunlocks = false;
	String location = Game.gameDir;
	File folder;
	boolean isblue;
	
	public TitleMenu() {
		this.folder = new File(this.location);
		this.isblue = false;
		if(splashes.size() == 0) {
			splashes.add("You will never see this!");
		}

		if(splashes.size() == 1) {
			this.getSplashes();
		}

		this.rand = this.random.nextInt(splashes.size());
		if(!loadedunlocks) {
			this.getSplashes();
		}
	}
	
	public void getSplashes() {
		String ee;
		if(!loadedsplashes) {
			//The fun little messages that pop up.
			String[] splashes = {
				"Also play InfinityTale!",
				"Also play Minicraft Delux!",
				"Notch is Awesome!",
				"Dillyg10 is cool as Ice!",
				"Kill Creeper, get Gunpowder!",
				"Also play Alecraft!",
				"Also play Hackcraft!",
				"Playminicraft.com is the bomb!",
				"Shylor is the man!",
				"Kill Cow, get Beef!",
				"Guard13007 has too many twitters!",
				"You should read Antidious Venomi!",
				"Kill Zombie, get Cloth!",
				"Kill Slime, get Slime!",
				"Kill Skeleton, get Bones!",
				"Kill Sheep, get Wool!",
				"Kill Pig, get Porkchop!",
				"Gold > Iron",
				"Gem > Gold",
				"@MinicraftPlus on Twitter",
				"MinicraftPlus on Youtube",
				"AntVenom loves cows! Honest!",
				"Milk is for something later!",
				"saying ay-oh, that creeper's KO'd!",
				"So we back in the mine,",
				"pickaxe swinging from side to side",
				"Life itself suspended by a thread",
				"wenubs.com is jamming!",
				"Gimmie a bucket!",
				"Farming with water!",
				"Made with 10000% Vitamin Z!",
				"In search of Gems!",
				"Alpha? What's that?",
				"Beta? What's that?",
				"Infdev? What's that?",
				"Test == InDev!",
				"Too much DP!",
				"Sugarcane is a Idea!",
				"Y U NO BOAT!?",
				"PBAT is in the house!",
				"Who is SeaNanners?",
				"Diggy! Dig... ooh! a jaffacake!",
				"Punch the Moon!",
				"This is String qq!",
				"3D? What's that?",
				"Why?",
				"Mouse not included!",
				"Bosses? What are those?",
				"You are null!",
				"Story? What's that?",
				"Multiplayer? What's that?",
				"Infinite terrain? What's that?",
				"Spiders? What are those?",
				"That guy is such a sly fox!",
				"Windows? I perfer Doors!",
				"3.1D is the new thing!",
				"hola senor!",
				"Vote for the Dead Workers Party!",
				"Sonic Boom!",
				"Hakuna Matata!",
				"Only on PlayMinicraft.com!",
			//"Also play Minicraft Delux!",
			//"Also play InfinityTale!",
			//"Also play Alecraft!",
				"Also play Hackcraft!",
				"Also play RPGcraft!(When it's done)",
				"Also play MiniCrate!",
				"Also play MiniCraft Mob Overload!",
				"One truth prevails!",
				"1.8? Ehhhh....",
				"Saving Now Included!",
				"Loading Now Included!",
				"011011000110111101101100!",
				"Farming with water!",
				"Buckets? YESH!",
				"Press \"R\"!",
				"Get the High-Score!",
				"Join the Forums!",
				"The Wiki is weak! Help it!",
				"Awesome!",
				"Sweet!",
				"Potions ftw!",
				"Conquer the Dungeon!",
				"Defeat the Air Wizard!",
				"Loom + Wool = String!",
				"String + Wood = Rod!",
				"Sand + Gunpowder = TNT!",
				"Sleep at Night!",
				"Farm at Day!",
				"Leave a comment below!",
				"No spiders included!",
				"No endermen included!",
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
				"Explanation Mark!",
				"!sdrawkcab si siht",
				"This is forwards!",
				"why is this blue?",
				"MissingNo " + rand
			};
			this.splashes = new ArrayList<String>();
			for(int i = 0; i < splashes.length; i++)
				if(!this.splashes.contains(splashes[i]))
					this.splashes.add(splashes[i]);
			/*try {
				URL br = new URL("http://minicraftplus.webs.com/-splashes.txt");
				URLConnection e = br.openConnection();
				e.setReadTimeout(1000);
				Scanner bufferedWriter = new Scanner(br.openStream());
				splashes.clear();

				while(bufferedWriter.hasNextLine()) {
					ee = bufferedWriter.nextLine();
					if(ee.contains("]")) {
						if(ee.substring(ee.indexOf("]")).length() > 3) {
							ee = ee.substring(ee.indexOf("]") + 2, ee.length());
						} else {
							ee = "";
						}
					}

					if(!ee.equals("")) {
						splashes.add(ee);
					}
				}

				bufferedWriter.close();
			} catch (MalformedURLException var43) {
				var43.printStackTrace();
				splashes.clear();
				splashes.add("");
			} catch (ConnectException var44) {
				var44.printStackTrace();
				splashes.clear();
				splashes.add("Connection issue! D:");
			} catch (IOException var45) {
				var45.printStackTrace();
				splashes.clear();
				splashes.add("Offline Mode :<");
			}
			*/
			loadedsplashes = true;
		}

		if(!loadedunlocks) {
			ModeMenu.unlockedtimes.clear();
			BufferedReader br1 = null;
			this.folder.mkdirs();

			try {
				br1 = new BufferedReader(new FileReader(this.location + "/unlocks.miniplussave"));

				String e1;
				while((e1 = br1.readLine()) != null) {
					List bufferedWriter2 = Arrays.asList(e1.split(","));
					Iterator var5 = bufferedWriter2.iterator();

					while(var5.hasNext()) {
						ee = (String)var5.next();
						if(ee.contains("AirSkin")) {
							OptionsMenu.unlockedskin = true;
						}

						if(ee.contains("MINUTEMODE") && !ee.substring(0, ee.indexOf("M") + 1).equals("M")) {
							ModeMenu.unlockedtimes.add(ee.substring(0, ee.indexOf("M") + 1));
						}
					
						if(ee.contains("HOURMODE") && !ee.substring(0, ee.indexOf("H") + 1).equals("H")) {
							ModeMenu.unlockedtimes.add(ee.substring(0, ee.indexOf("H") + 1));
						}
					}
				}
			} catch (FileNotFoundException var40) {
				BufferedWriter bufferedWriter1 = null;

				try {
					bufferedWriter1 = new BufferedWriter(new FileWriter(this.location + "/unlocks.miniplussave"));
					bufferedWriter1.write("");
				} catch (IOException var38) {
					var38.printStackTrace();
				} finally {
					try {
						if(bufferedWriter1 != null) {
							bufferedWriter1.flush();
							bufferedWriter1.close();
						}
					} catch (IOException var37) {
						var37.printStackTrace();
					}

				}
			} catch (IOException var41) {
				var41.printStackTrace();
			} finally {
				try {
					if(br1 != null) {
						br1.close();
					}
				} catch (IOException var36) {
					var36.printStackTrace();
				}

			}

			ModeMenu.times.clear();
			loadedunlocks = true;
		}
	}

	public void tick() {
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;
		if (input.getKey("up").clicked) Sound.pickup.play();
		if (input.getKey("down").clicked) Sound.pickup.play();
		
		int len = options.length;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		if (input.getKey("r").clicked) rand = random.nextInt(randcount);
		
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
				//BUT: this object is passed to WorldSelectMenu...
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
			if (options[selected] == "Options") {
				//sentFromMenu = true;
				game.setMenu(new OptionsMenu(this));
			}
			if (options[selected] == "About") game.setMenu(new AboutMenu(this));
			if (options[selected] == "Quit") System.exit(0);
			//if (options[selected] == "Kill") {game.level.add(game.player); game.setMenu(null);}
		}
	}
	
	/* This section is used to display the minicraft title */
	
	public void render(Screen screen) {
		screen.clear(0);
		//String splash;
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		//int xx = 55;
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
		
		if(((String)splashes.get(rand)).contains("blue")) {
			this.isblue = true;
		} else {
			this.isblue = false;
		}
		
		if (count <= 5) cols = isblue?cols = Color.get(0, 5, 5, 5):Color.get(0, 505, 550, 550);
		if (count <= 10 && count > 5) cols = isblue?cols = Color.get(0, 4, 4, 4):Color.get(0, 405, 440, 440);
		if (count <= 15 && count > 10) cols = isblue?cols = Color.get(0, 3, 3, 3):Color.get(0, 305, 330, 330);
		if (count <= 20 && count > 15) cols = isblue?cols = Color.get(0, 2, 2, 2):Color.get(0, 205, 220, 220);
		if (count <= 25 && count > 20) cols = isblue?cols = Color.get(0, 1, 1, 1):Color.get(0, 5, 110, 110);
		
		writeCentered(((String)splashes.get(rand)), screen, 60, cols);
		
		if(GameApplet.isApplet) {
			if(GameApplet.username.length() < 27) {
				Font.draw("Welcome, " + GameApplet.username + "!", screen, this.centertext("Welcome, " + GameApplet.username + "!"), screen.h - 190, Color.get(0, 330, 330, 330));
			} else {
				Font.draw("Welcome,", screen, this.centertext("Welcome!"), screen.h - 190, Color.get(0, 330, 330, 330));
				Font.draw(GameApplet.username + "!", screen, this.centertext(GameApplet.username + "!"), screen.h - 180, Color.get(0, 330, 330, 330));
			}
			
			writeCentered("Version 1.8.1", screen, screen.h - 10, Color.get(0, 111, 111, 111));
		}
		else {
			Font.draw("Version 1.8.1", screen, 1, screen.h - 190, Color.get(0, 111, 111, 111));
		}
		
		writeCentered("(Arrow keys to move)", screen, screen.h - (GameApplet.isApplet?35:25), Color.get(0, 111, 111, 111));
		writeCentered("(Enter to accept, Escape to return)", screen, screen.h - (GameApplet.isApplet?25:15), Color.get(0, 111, 111, 111));
	}
}
