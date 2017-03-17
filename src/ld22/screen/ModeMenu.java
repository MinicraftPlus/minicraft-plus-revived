//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

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
	//private static int selectedlr = mode; //useless?
	private int selected = 0; //useless?

	public ModeMenu() {}

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
			game.setMenu(new LoadingMenu());
		}

		if (input.getKey("escape").clicked) game.setMenu(new TitleMenu());

		if (input.getKey("craft").clicked) game.setMenu(new WorldGenMenu());

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

		//centerx = 60
		Font.draw("Game Mode:	" + modes[mode - 1], screen, 60, 8 * 8, Color.get(-1, 555, 555, 555));

		drawCentered("Press Enter to Start", screen, screen.h - 75, textCol);

		Font.draw("Loading...", screen, 120, screen.h - 105, (loading == 0 ? black : color));

		drawCentered("Press Left and Right", screen, screen.h - 150, textCol);
		drawCentered("Press Esc to Return", screen, screen.h - 55, textCol);
		drawCentered("Press Z for world options", screen, screen.h - 35, textCol);
	}
}
