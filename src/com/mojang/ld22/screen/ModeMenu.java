//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import com.mojang.ld22.Game; 
public class ModeMenu extends Menu {
	private Menu parent;
	public ModeMenu() {
	}
	public static boolean survival;
	public static boolean creative;
	public static boolean hardcore;
	public static boolean score;
	public static boolean hasSetDif;
	public static int diff = 1;
	public static int loading = 0;
	private static int selectedlr = diff;
	private int selected = 0;
	
	public void tick() {
		if (input.getKey("left").clicked) selectedlr--;
		if (input.getKey("right").clicked) selectedlr++;
		if (input.getKey("left").clicked) diff--;
		if (input.getKey("right").clicked) diff++;
		
		if (input.getKey("left").clicked) Sound.craft.play(); 
		if (input.getKey("right").clicked) Sound.craft.play();
		 /*
		if (input.getKey("survival").clicked) {
			survival = true;
			creative = false;
			hardcore = false;
		}
		if (input.getKey("creative").clicked) {
			survival = false;
			creative = true;
			hardcore = false;
		}
		if (input.getKey("hardcore").clicked) {
			survival = false;
			creative = false;
			hardcore = true;
		}
		*/
		//This is so that if the user presses x @ respawn menu, they respawn (what a concept)
		//if (input.)
		 if (input.getKey("menu").clicked) {
				if (selected == 0) {
					Sound.test.play();
					game.setMenu(new LoadingMenu());
				}
			}
			
			if (input.getKey("attack").clicked) {
				game.setMenu(new TitleMenu());
			}
		    if (input.getKey("craft").clicked) game.setMenu(new WorldGenMenu());
		
			if (diff == 1) {
				survival = true;
				creative = false;
				hardcore = false;
				score = false;
			}
			
			else if (diff == 2) {
				survival = false;
				creative = true;
				hardcore = false;
				score = false;
			}
			
			if (diff == 3) {
				survival = false;
				creative = false;
				hardcore = true;
				score = false;
			}
			if (diff == 4) {
				survival = false;
				creative = false;
				hardcore = false;
				score = true;
			}
			if (selectedlr > 4)
				selectedlr = 1;
				if (selectedlr < 1)
				selectedlr = 4;
				if (diff > 4)
				diff = 1;
				if (diff < 1)
				diff = 4;
	}
	
	public void render(Screen screen) {
		int col = Color.get(0, 300, 300, 300);
		int coll = Color.get(0, 0, 0, 0);
		screen.clear(0);
		Font.draw("Game Mode:", screen, 11 * 6 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		
		if (diff == 1)
		Font.draw("Survival", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		else if (diff == 2)
		Font.draw("Creative", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		else if (diff == 3)
		Font.draw("Hardcore", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		else if (diff == 4)
		Font.draw("Score", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		
		Font.draw("Press X to Start", screen, 80, screen.h - 75, Color.get(0, 555, 555, 555));
		
		if (loading == 0){
		Font.draw("Loading...", screen, 120, screen.h - 105, coll);
		} else {
		Font.draw("Loading...", screen, 120, screen.h - 105, col);
		}
		
		Font.draw("Press Left and Right", screen, 67, screen.h - 150, Color.get(0, 555, 555, 555));
		Font.draw("Press C to Return", screen, 75, screen.h - 55, Color.get(0, 555, 555, 555));
		Font.draw("Press Z for world options", screen, 50, screen.h - 35, Color.get(0, 555, 555, 555));
		
		
		
	}
}