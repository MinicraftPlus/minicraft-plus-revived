package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class StartMenu extends Menu {
	//This is really the options menu; I don't know why it's called StartMenu...
	public static int easy = 1;
	public static int norm = 2;
	public static int hard = 3;
	public static int diff = 2;
	private static int selectedlr = diff;
	public static boolean isSoundAct = true;
	private int selected = 0;
	String soundTest = "On";
	public static boolean unlockedskin = false;
	public static boolean skinon = false;
	public static boolean hasSetDiff = false;
	
	private Menu parent;
	
	public StartMenu() {}
	
	public void tick() {
		if (input.getKey("left").clicked) selectedlr--;
		if (input.getKey("right").clicked) selectedlr++;
		if (input.getKey("left").clicked) diff--;
		if (input.getKey("right").clicked) diff++;
		if (input.getKey("left").clicked) Sound.craft.play(); 
		if (input.getKey("right").clicked) Sound.craft.play(); 
		//if (input.getKey("mode").clicked) game.setMenu(new ModeMenu());
		if (selectedlr > 3)
		selectedlr = 1;
		if (selectedlr < 1)
		selectedlr = 3;
		if (diff > 3)
		diff = 1;
		if (diff < 1)
		diff = 3;
		
		if (input.getKey("attack").clicked) {
			if (selected == 0) {
				Sound.test.play();
				//if (ModeMenu.hasSetDif == false) ModeMenu.survival = true;
				//game.resetstartGame();
				hasSetDiff = true;
				if (TitleMenu.sentFromMenu){ game.setMenu(new TitleMenu());}
				else game.setMenu(new PauseMenu(game.player));
				
			}
		
		}
		
		if(unlockedskin && this.input.getKey("w").clicked) {
			if(skinon) {
				skinon = false;
			} else {
				skinon = true;
			}
		}
		
		//toggles sound
		if (input.getKey("down").clicked) {
			Sound.craft.play();
			System.out.println("Works!");
			isSoundAct = !isSoundAct;
		}
	}
	
	public void render(Screen screen) {
		int col1 = Color.get(-1,500,500,500);
		int col2 = Color.get(-1,50,50,50);
		int rCol = 0;
		/*
		if (isSoundAct == true) {
			rCol = col2;
			soundTest = "On";
		}
		else {
			rCol = col1;
			soundTest = "Off";
		}*/
		screen.clear(0);
		Font.draw("Difficulty:", screen, 11 * 6 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		
		if (diff == 1)
		Font.draw("Easy", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		else if (diff == 2)
		Font.draw("Normal", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		else if (diff == 3)
		Font.draw("Hard", screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		
		Font.draw("Press C to return", screen, 80, screen.h - 75, Color.get(0, 555, 555, 555));
		
		Font.draw("<S>ound:", screen, 80, screen.h - 100, Color.get(0, 555, 555, 555));
		if (isSoundAct) Font.draw("On", screen, 150, screen.h - 100, Color.get(0, 50, 50, 50));
		else Font.draw("Off", screen, 150, screen.h - 100, Color.get(0, 500, 500, 500));
		
		//Font.draw("" + soundTest, screen, 90, screen.h - 90, rCol);
		
		if(unlockedskin) {
			Font.draw("<W>ear Suit:", screen, 80, screen.h - 110, Color.get(0, 555, 555, 555));
			if(skinon) {
				Font.draw("On", screen, 180, screen.h - 110, Color.get(0, 50, 50, 50));
			} else {
				Font.draw("Off", screen, 180, screen.h - 110, Color.get(0, 500, 500, 500));
			}
		}
		
		Font.draw("Press Left and Right", screen, 67, screen.h - 150, Color.get(0, 555, 555, 555));
		
	}
}