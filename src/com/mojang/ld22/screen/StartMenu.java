package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class StartMenu extends Menu {
	public static int easy = 1;
	public static int norm = 2;
	public static int hard = 3;
	public static int diff = 2;
	private static int selectedlr = diff;
	public static boolean isSoundAct = true;
	private int selected = 0;
	String soundTest = "On";

	private Menu parent;

	public StartMenu() {
	}
	
	
public static boolean hasSetDiff = false;
	public void tick() {
		if (input.left.clicked) selectedlr--;
		if (input.right.clicked) selectedlr++;
		if (input.left.clicked) diff--;
		if (input.right.clicked) diff++;
		if (input.left.clicked) Sound.craft.play(); 
		if (input.right.clicked) Sound.craft.play(); 
		//if (input.mode.clicked) game.setMenu(new ModeMenu());
		if (selectedlr > 3)
		selectedlr = 1;
		if (selectedlr < 1)
		selectedlr = 3;
		if (diff > 3)
		diff = 1;
		if (diff < 1)
		diff = 3;
		
		if (input.attack.clicked) {
			if (selected == 0) {
				Sound.test.play();
				//if (ModeMenu.hasSetDif == false) ModeMenu.survival = true;
				//game.resetstartGame();
				hasSetDiff = true;
				if (TitleMenu.sentFromMenu){ game.setMenu(new TitleMenu());}
				else game.setMenu(new PauseMenu());
				
			}
		
		}
		 if (input.down.clicked) {
			 Sound.craft.play(); 
			 System.out.println("Works!");
			if (isSoundAct == true) {
				isSoundAct = false;
				
			}
			else   {
				isSoundAct = true;
			}
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

		Font.draw("Press Left and Right", screen, 67, screen.h - 150, Color.get(0, 555, 555, 555));

	}
}