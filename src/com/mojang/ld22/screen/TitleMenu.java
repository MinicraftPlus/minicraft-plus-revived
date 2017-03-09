package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import java.util.Random;

public class TitleMenu extends Menu {
	private int selected = 0;
	protected final Random random = new Random();
	
	private static final String[] options = { "New game", "Tutorial", "Options", "About", "Quit"};
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
		
		if (reverse == false){
			count++;
			if (count == 25) reverse = true;
		} else if (reverse == true) {
			count--;
			if (count == 0) reverse = false;
		}
		
		if (input.getKey("enter").clicked/*input.getKey("escape").clicked || input.getKey("enter").clicked*/) {
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
			if (selected == 2) {sentFromMenu = true; game.setMenu(new StartMenu());}
			if (selected == 3) game.setMenu(new AboutMenu(this));
			if (selected == 4) System.exit(0);
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		String splash;
		int h = 2;
		int w = 15;
		int xx = 55;
		int titleColor = Color.get(0, 010, 131, 551);
		int xo = (screen.w - w * 8) / 2;
		int yo = 36;
		int cols = Color.get(0, 550, 550, 550);
		
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
		
		for (int i = 0; i < options.length; i++) {
			String msg = options[i];
			int col = Color.get(0, 222, 222, 222);
			if (i == selected) {
				msg = "> " + msg + " <";
				col = Color.get(0, 555, 555, 555);
			}
			Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, (11 + i) * 8, col);
		}
		
		if (count <= 5){
			cols = Color.get(0, 505, 550, 550);
		}
		if (count <= 10 && count > 5){
			cols = Color.get(0, 405, 440, 440);
		}
		if (count <= 15 && count > 10){
			cols = Color.get(0, 305, 330, 330);
		}
		if (count <= 20 && count > 15){
			cols = Color.get(0, 205, 220, 220);
		}
		if (count <= 25 && count > 20){
			cols = Color.get(0, 5, 110, 110);
		}
		
		/*
		if (rand == 1){
			splash = a;
			xx = 55;
		}else if (rand == 2){
			splash = b;
			xx = 45;
		}else if (rand == 3){
			splash = c;
			xx = 80;
		}else if (rand == 4){
			splash = e;
			xx = 40;
		}else if (rand == 5){
			splash = f;
			xx = 70;
		}else if (rand == 6){
			splash = g;
			xx = 70;
		}else if (rand == 7){
			splash = hh;
			xx = 30;
		}else if (rand == 8){
			splash = ii;
			xx = 70;
		}else if (rand == 9){
			splash = j;
			xx = 70;
		}else if (rand == 10){
			splash = k;
			xx = 15;
		}else if (rand == 11){
			splash = l;
			xx = 15;
		}else if (rand == 12){
			splash = m;
			xx = 55;
		}else if (rand == 13){
			splash = n;
			xx = 55;
		}else if (rand == 14){
			splash = o;
			xx = 45;
		}else if (rand == 15){
			splash = p;
			xx = 60;
		}else if (rand == 16){
			splash = q;
			xx = 55;
		}else if (rand == 17){
			splash = r;
			xx = 100;
		}else if (rand == 18){
			splash = s;
			xx = 100;
		}else if (rand == 19){
			splash = t;
			xx = 50;
		}else if (rand == 20){
			splash = u;
			xx = 50;
		}
		else if (rand == 21){
			splash = v;
			xx = 35;
		}
		else if (rand == 22){
			splash = ww;
			xx = 35;
		}
		else if (rand == 23){
			splash = xxs;
			xx = 10;
		}
		else if (rand == 24){
			splash = yy;
			xx = 55;
		}
		else if (rand == 25){
			splash = j;
			xx = 70;
		}
		else if (rand == 26){
			splash = z;
			xx = 15;
		}else if (rand == 27){
			splash = aa;
			xx = 55;
		}
		else if (rand == 28){
			splash = bb;
			xx = 85;
		}
		else if (rand == 29){
			splash = cc;
			xx = 70;
		}
		else if (rand == 30){
			splash = dd;
			xx = 35;
		}
		else if (rand == 31){
			splash = ee;
			xx = 75;
		}
		else if (rand == 32){
			splash = ff;
			xx = 70;
		}
		else if (rand == 33){
			splash = gg;
			xx = 75;
		}
		else if (rand == 34){
			splash = hhh;
			xx = 65;
		}
		else if (rand == 35){
			splash = iii;
			xx = 90;
		}
		else if (rand == 36){
			splash = jj;
			xx = 100;
		}
		else if (rand == 37){
			splash = kk;
			xx = 65;
		}
		else if (rand == 38){
			splash = ll;
			xx = 95;
		}
		else if (rand == 39){
			splash = mm;
			xx = 60;
		}
		else if (rand == 40){
			splash = nn;
			xx = 75;
		}
		else if (rand == 41){
			splash = oo;
			xx = 20;
		}
		else if (rand == 42){
			splash = pp;
			xx = 85;
		}
		else if (rand == 43){
			splash = qq;
			xx = 75;
		}
		else if (rand == 44){
			splash = rr;
			xx = 80;
		}
		else if (rand == 45){
			splash = ss;
			xx = 125;
		}
		else if (rand == 46){
			splash = tt;
			xx = 70;
		}
		else if (rand == 47){
			splash = uu;
			xx = 55;
		}
		else if (rand == 48){
			splash = vv;
			xx = 95;
		}
		else if (rand == 49){
			splash = www;
			xx = 70;
		}
		else if (rand == 50){
			splash = xxx;
			xx = 45;
		}
		else if (rand == 51){
			splash = yyy;
			xx = 25;
		}
		else if (rand == 52){
			splash = zz;
			xx = 50;
		}
		else if (rand == 53){
			splash = aaa;
			xx = 35;
		}
		else if (rand == 54){
			splash = bbb;
			xx = 50;
		}
		else if (rand == 55){
			splash = ccc;
			xx = 55;
		}
		else if (rand == 56){
			splash = ddd;
			xx = 105;
		}
		else if (rand == 57){
			splash = eee;
			xx = 17;
		}
		else if (rand == 58){
			splash = fff;
			xx = 105;
		}
		else if (rand == 59){
			splash = ggg;
			xx = 90;
		}else if (rand == 0){
			splash = d;
			xx = 55;
		} else {
			System.out.println(rand);
			splash = "MissingNo " + rand;
			xx = 105;
		}*/
		
		int[] widths = {55, 45, 80, 40, 70, 70, 30, 70, 70, 15, 15, 55, 55, 45, 60, 55, 100, 100, 50, 50, 35, 35, 10, 55, 70, 15, 55, 85, 70, 35, 75, 70, 75, 65, 90, 100, 65, 95, 60, 75, 20, 85, 75, 80, 125, 70, 55, 95, 70, 45, 25, 50, 35, 50, 55, 105, 17, 105, 90, 55, 105};
		/*
		if (splash == splashes[23]){
			Font.draw(yt, screen, 10, 70, cols);
		}
		*/
		Font.draw(splashes[rand], screen, widths[rand], 60, cols);
		
		Font.draw("(Arrow keys to move)", screen, 65, screen.h - 25, Color.get(0, 111, 111, 111));
		Font.draw("(Enter to accept, Escape to return)", screen, 30, screen.h - 15, Color.get(0, 111, 111, 111));
		Font.draw("Version 1.6-1.8 (updating)", screen, 1, screen.h - 190, Color.get(0, 111, 111, 111));
	}
	/*
	private int centerX(String text) {
		
	}*/
}