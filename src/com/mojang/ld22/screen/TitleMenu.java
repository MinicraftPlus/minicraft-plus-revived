//mostly new... fewer comments.
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import java.util.Random;

public class TitleMenu extends Menu {
	private int selected = 0;
	protected final Random random = new Random();

	private static final String[] options = {"New game", "Tutorial", "Options", "About", "Quit"}; // Options that are on the main menu.
	public static boolean sentFromMenu;
	int randcount = 60;
	int rand = random.nextInt(randcount);
	int count = 0;
	boolean reverse = false;

	public TitleMenu() {}

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
			if (selected == 0) {
				WorldSelectMenu.loadworld = false;
				game.setMenu(new WorldSelectMenu(this));
				//(this method should now stop getting called by Game)
				//BUT: this object is passed to WorldSelectMenu...
			}
			if (selected == 1) {
				try {
					//This is for the tutorial Video
					String url = "http://minicraftplus.webs.com/Tutorial.htm";
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (java.io.IOException e) {
					System.out.println(e.getMessage());
				}
			}
			if (selected == 2) {
				sentFromMenu = true;
				game.setMenu(new StartMenu());
			}
			if (selected == 3) game.setMenu(new AboutMenu(this));
			if (selected == 4) System.exit(0);
		}
	}
	
	/* This section is used to display the minicraft title */
	
	public void render(Screen screen) {
		screen.clear(0);
		String splash;
		int h = 2; // Height of squares (on the spritesheet)
		int w = 15; // Width of squares (on the spritesheet)
		int xx = 55;
		int titleColor = Color.get(0, 010, 131, 551);
		int xo = (screen.w - w * 8) / 2; // X location of the title
		int yo = 36; // Y location of the title
		int cols = Color.get(0, 550, 550, 550);
		
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
			"MissingNo " + rand
		};

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

		if (count <= 5) cols = Color.get(0, 505, 550, 550);
		if (count <= 10 && count > 5) cols = Color.get(0, 405, 440, 440);
		if (count <= 15 && count > 10) cols = Color.get(0, 305, 330, 330);
		if (count <= 20 && count > 15) cols = Color.get(0, 205, 220, 220);
		if (count <= 25 && count > 20) cols = Color.get(0, 5, 110, 110);

		drawCentered(splashes[rand], screen, 60, cols);

		drawCentered("(Arrow keys to move)", screen, screen.h - 25, Color.get(0, 111, 111, 111));
		drawCentered("(Enter to accept, Escape to return)", screen, screen.h - 15, Color.get(0, 111, 111, 111));
		
		Font.draw("Version 1.6-1.8 (updating)", screen, 1, screen.h - 190, Color.get(0, 111, 111, 111));
	}
}
