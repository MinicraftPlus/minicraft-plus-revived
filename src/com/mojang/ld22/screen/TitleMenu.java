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
	
	public TitleMenu() {
	}
	
	public void tick() {
		if (input.up.clicked) selected--;
		if (input.down.clicked) selected++;
		if (input.up.clicked) Sound.pickup.play(); 
		if (input.down.clicked) Sound.pickup.play(); 
		
		int len = options.length;
		if (selected < 0) selected += len;
		if (selected >= len) selected -= len;
		
		if (input.r.clicked) rand = random.nextInt(randcount);
		
		if (reverse == false){
		count++;
		if (count == 25){
			reverse = true;
		}
		} else if (reverse == true){
			count--;
			if (count == 0){
				reverse = false;
			}
		}
		
		if (input.attack.clicked || input.menu.clicked) {
			if (selected == 0) {
				WorldSelectMenu.loadworld = false;
				game.setMenu(new WorldSelectMenu(this));
			}
			if (selected == 1) {
				try {
					//This is for the tutorial Video
			         String url = "http://minicraftplus.webs.com/Tutorial.htm";
			         java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			       }
			       catch (java.io.IOException e) {
			           System.out.println(e.getMessage());
			       }}
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
		String a = "Also play InfinityTale!";
		String b = "Also play Minicraft Delux!";
		String c = "Notch is Awesome!";
		String d = "Dillyg10 is cool as Ice!";
		String e = "Kill Creeper, get Gunpowder!";
		String f = "Also play Alecraft!";
		String g = "Also play Hackcraft!";
		String hh = "Playminicraft.com is the bomb!";
		String ii = "Shylor is the man!";
		String j = "Kill Cow, get Beef!";
		String k = "Guard13007 has too many twitters!";
		String l = "You should read Antidious Venomi!";
		String m = "Kill Zombie, get Cloth!";
		String n = "Kill Slime, get Slime!";
		String o = "Kill Skeleton, get Bones!";
		String p = "Kill Sheep, get Wool!";
		String q = "Kill Pig, get Porkchop!";
		String r = "Gold > Iron";
		String s = "Gem > Gold";
		String t = "@MinicraftPlus on Twitter";
		String u = "MinicraftPlus on Youtube";
		String v = "AntVenom loves cows! Honest!";
		String ww = "Milk is for something later!";
		String xxs = "saying ay-oh, that creeper's KO'd!";
		String yy = "So we back in the mine,";
		String yt = "pickaxe swinging from side to side";
		String z = "Life itself suspended by a thread";
		String aa = "wenubs.com is jamming!";
		String bb = "Gimmie a bucket!";
		String cc = "Farming with water!";
		String dd = "Made with 10000% Vitamin Z!";
		String ee = "In search of Gems!";
		String ff = "Alpha? What's that?";
		String gg = "Beta? What's that?";
		String hhh = "Infdev? What's that?";
		String iii = "Test == InDev!";
		String jj = "Too much DP!";
		String kk = "Sugarcane is a Idea!";
		String ll = "Y U NO BOAT!?";
		String mm = "PBAT is in the house!";
		String nn = "Who is SeaNanners?";
		String oo = "Diggy! Dig... ooh! a jaffacake!";
		String pp = "Punch the Moon!";
		String qq = "This is String qq!";
		String rr = "3D? What's that?";
		String ss = "Why?";
		String tt = "Mouse not included!";
		String uu = "Bosses? What are those?";
		String vv = "You are null!";
		String www = "Story? What's that?";
		String xxx = "Multiplayer? What's that?";
		String yyy = "Infinite terrain? What's that?";
		String zz = "Spiders? What are those?";
		String aaa = "That guy is such a sly fox!";
		String bbb = "Windows? I perfer Doors!";
		String ccc = "3.1D is the new thing!";
		String ddd = "hola senor!";
		String eee = "Vote for the Dead Workers Party!";
		String fff = "Sonic Boom!";
		String ggg = "Hakuna Matata!";
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				screen.render(xo + x * 8, yo + y * 8, x + (y + 6) * 32, titleColor, 0);
			}
		}
		
		for (int i = 0; i < 4; i++) {
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
			splash = "MissingNo";
			xx = 105;
		}
		
		if (splash == yy){
			Font.draw(yt, screen, 10, 70, cols);
		}
		
		Font.draw(splash, screen, xx, 60, cols);
		
		Font.draw("(Arrow keys to move)", screen, 65, screen.h - 25, Color.get(0, 111, 111, 111));
		Font.draw("(X to accept, C to return)", screen, 45, screen.h - 15, Color.get(0, 111, 111, 111));
		Font.draw("Version 1.6", screen, 1, screen.h - 190, Color.get(0, 111, 111, 111));
	}
}