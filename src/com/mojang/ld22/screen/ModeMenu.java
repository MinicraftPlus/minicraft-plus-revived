//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModeMenu extends Menu {
	private Menu parent;

	public static boolean survival;
	public static boolean creative;
	public static boolean hardcore;
	public static boolean score;
	public static boolean hasSetDif;
	public static int mode = 1;
	public static int loading = 0;
	public static String[] modes = {"Survival", "Creative", "Hardcore", "Score"};
	private int selected = 0;
	public static int selection = 0;
	public static String time = "";
	public static String defaultTime = "20M";
	public static List times = new ArrayList();
	public static List unlockedtimes = new ArrayList();
	
	public ModeMenu() {
		if(times.size() < 1) {
			times.add("20M");
			times.add("30M");
			times.add("40M");
			times.add("1H");

			for(int mm = 0; mm < unlockedtimes.size(); ++mm) {
				times.add((String)unlockedtimes.get(mm));
			}
		}

		ArrayList min = new ArrayList();
		ArrayList hm = new ArrayList();
		
		for(int i = 0; i < times.size(); i++) {
			if(((String)times.get(i)).contains("M")) {
				if(!((String)times.get(i)).contains(".")) {
					min.add(Integer.valueOf(Integer.parseInt(((String)times.get(i)).substring(0, ((String)times.get(i)).length() - 1))));
				} else {
					min.add(Integer.valueOf((int)Double.parseDouble(((String)times.get(i)).substring(0, ((String)times.get(i)).length() - 1))));
				}

				Collections.sort(min);
			}
			if(((String)times.get(i)).contains("H")) {
				if(!((String)times.get(i)).contains(".")) {
					hm.add(Integer.valueOf(Integer.parseInt(((String)times.get(i)).substring(0, ((String)times.get(i)).length() - 1))));
				} else {
					hm.add(Integer.valueOf((int)Double.parseDouble(((String)times.get(i)).substring(0, ((String)times.get(i)).length() - 1))));
				}

				Collections.sort(hm);
			}
		}
		
		times.clear();
		
		for(int i = 0; i < min.size(); i++) {
			times.add(min.get(i) + "M");
		}
		
		for(int i = 0; i < hm.size(); i++) {
			times.add(hm.get(i) + "H");
		}
		
		for(int i = 0; i < times.size(); i++) {
			if(((String)times.get(i)).equals(defaultTime) && time.equals("")) {
				time = (String)times.get(i);
				selection = i;
			}
		}
	}
	
	public void tick() {
		if (input.getKey("left").clicked) {
			mode--;
			Sound.craft.play();
		}
		if (input.getKey("right").clicked) {
			mode++;
			Sound.craft.play();
		}

		//This is so that if the user presses enter @ respawn menu, they respawn (what a concept)
		if (input.getKey("enter").clicked && selected == 0) { //selected is always 0..?
			Sound.test.play();
			
			if(mode == 4) {
				if(((String)times.get(selection)).contains("M")) {
					this.game.newscoreTime = Integer.parseInt(((String)times.get(selection)).replace("M", "").replace("H", "")) * 60 * 60;
				} else if(((String)times.get(selection)).contains("H")) {
					this.game.newscoreTime = Integer.parseInt(((String)times.get(selection)).replace("H", "").replace("M", "")) * 60 * 60 * 60;
				}

				System.out.println(Integer.parseInt(((String)times.get(selection)).replace("H", "").replace("M", "")) * 60 * 60 * 60);
			}
			
			game.setMenu(new LoadingMenu());
		}

		if (input.getKey("escape").clicked) game.setMenu(new TitleMenu());

		if (input.getKey("z").clicked) game.setMenu(new WorldGenMenu());
		
		if(input.getKey("t").clicked) {
			selection++;
			if(selection > times.size() - 1) {
				selection = 0;
			}

			time = (String)times.get(selection);
		}
		
		updateModeBools(mode);
		
		if (mode > 4) mode = 1;
		if (mode < 1) mode = 4;
	}

	public static void updateModeBools(int mode) {
		ModeMenu.mode = mode;

		survival = mode == 1;
		creative = mode == 2;
		hardcore = mode == 3;
		score = mode == 4;
	}

	public void render(Screen screen) {
		int color = Color.get(0, 300, 300, 300);
		int black = Color.get(0, 0, 0, 0);
		int textCol = Color.get(0, 555, 555, 555);
		screen.clear(0);
		
		writeCentered("World Name:", screen, screen.h - 180, Color.get(0, 444, 444, 444));
		writeCentered(WorldSelectMenu.worldname, screen, screen.h - 170, Color.get(-1, 5, 5, 5));
		
		String modeText = "Game Mode:	" + modes[mode - 1];
		Font.draw(modeText, screen, screen.centerText(modeText), 8 * 8 + 1, Color.get(-1, 111, 111, 111));
		Font.draw(modeText, screen, screen.centerText(modeText) + 1, 8 * 8, Color.get(-1, 555, 555, 555));
		
		if(mode == 4) writeCentered("<T>ime: " + time, screen, 95, Color.get(0, 555, 555, 555));
		
		writeCentered("Press Enter to Start", screen, screen.h - 75, textCol);
		
		Font.draw("Loading...", screen, 120, screen.h - 105, (loading == 0 ? black : color));
		
		writeCentered("Press Left and Right", screen, screen.h - 150, textCol);
		writeCentered("Press Esc to Return", screen, screen.h - 55, textCol);
		writeCentered("Press Z for world options", screen, screen.h - 35, textCol);
	}
}
