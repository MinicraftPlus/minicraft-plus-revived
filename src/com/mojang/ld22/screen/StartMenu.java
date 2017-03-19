//new class; no comments
package com.mojang.ld22.screen;

import com.mojang.ld22.Game;
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
	//private static int selectedlr = diff;
	public static boolean isSoundAct = true;
	private int selected = 0; //useless?
	//String soundTest = "On";
	public static boolean unlockedskin = false;
	public static boolean skinon = false;
	public static boolean hasSetDiff = false;

	private Menu parent;

	public StartMenu() {}

	public void tick() {
		if (input.getKey("left").clicked) {
			diff--;
			Sound.craft.play();
		}
		if (input.getKey("right").clicked) {
			diff++;
			Sound.craft.play();
		}

		if (diff > 3) diff = 1;
		if (diff < 1) diff = 3;

		if (input.getKey("escape").clicked) {
			if (selected == 0) { //what's this if statement for?
				Sound.test.play();
				//if (ModeMenu.hasSetDif == false) ModeMenu.survival = true;
				//game.resetstartGame();
				hasSetDiff = true;
				if (TitleMenu.sentFromMenu) game.setMenu(new TitleMenu());
				else game.setMenu(new PauseMenu(game.player));
			}
		}

		if (unlockedskin && this.input.getKey("w").clicked) skinon = !skinon;

		//toggles sound
		if (input.getKey("s").clicked) {
			Sound.craft.play();
			//System.out.println("Sound toggle works!");
			isSoundAct = !isSoundAct;
		}

		if (input.getKey("a").clicked) {
			Sound.craft.play();
			Game.autosave = !Game.autosave;
		}
	}

	public void render(Screen screen) {
		/*
		int col1 = Color.get(-1, 500, 500, 500);
		int col2 = Color.get(-1, 50, 50, 50);
		int rCol = 0;
		*/
		int onColor = Color.get(0, 50, 50, 50), offColor = Color.get(0, 500, 500, 500);

		screen.clear(0);
		Font.draw("Difficulty:", screen, 11 * 6 + 4, 8 * 8, Color.get(-1, 555, 555, 555));

		String[] diffs = {"Easy", "Normal", "Hard"};
		Font.draw(diffs[diff - 1], screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));

		Font.draw("<A>utosave:", screen, 80, screen.h - 100, Color.get(0, 555, 555, 555));
		Font.draw(
				(Game.autosave ? "On" : "Off"),
				screen,
				180,
				screen.h - 100,
				(Game.autosave ? onColor : offColor));
		Font.draw("<S>ound:", screen, 80, screen.h - 75, Color.get(0, 555, 555, 555));
		Font.draw(
				(isSoundAct ? "On" : "Off"), screen, 180, screen.h - 75, (isSoundAct ? onColor : offColor));

		Font.draw("Press Esc to return", screen, 80, screen.h - 50, Color.get(0, 555, 555, 555));

		if (unlockedskin) {
			Font.draw("<W>ear Suit:", screen, 80, screen.h - 110, Color.get(0, 555, 555, 555));
			Font.draw(
					(skinon ? "On" : "Off"), screen, 180, screen.h - 110, (skinon ? onColor : offColor));
		}

		Font.draw("Press Left and Right", screen, 67, screen.h - 150, Color.get(0, 555, 555, 555));
	}
}
