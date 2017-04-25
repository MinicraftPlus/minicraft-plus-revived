package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;

public class WorldGenMenu extends Menu {
	//this the the "more world options" menu.
	
	private Menu parent;
	public static int normal = 10;
	public static int forest = 11;
	public static int desert = 12;
	public static int plain = 13;
	public static int hell = 14;
	
	public static int island = 0;
	public static int box = 1;
	public static int mount = 2;
	public static int irreg = 3;
	
	public static int sizeNorm = 128;
	public static int sizeBig = 256;
	public static int sizeHuge = 512;
	
	public static int type = 0;
	public static int theme = 10;
	public static int size = 0;
	public static int sized = 128;

	public static int op = 0;

	private static int selected = theme;
	private static int selectedlr = type;
	private static int selecteds = size;

	public WorldGenMenu() {}

	public void tick() {
		if (input.getKey("escape").clicked) {
			game.setMenu(new ModeMenu());
		}

		if (input.getKey("down").clicked) op--;
		if (input.getKey("up").clicked) op++;

		if (op == 0) {
			if (input.getKey("left").clicked) selected--;
			if (input.getKey("right").clicked) selected++;
			if (input.getKey("left").clicked) theme--;
			if (input.getKey("right").clicked) theme++;
		}

		if (op == 1) {
			if (input.getKey("left").clicked) selectedlr--;
			if (input.getKey("right").clicked) selectedlr++;
			if (input.getKey("left").clicked) type--;
			if (input.getKey("right").clicked) type++;
		}

		if (op == 2) {
			if (input.getKey("left").clicked) selecteds--;
			if (input.getKey("right").clicked) selecteds++;
			if (input.getKey("left").clicked) size--;
			if (input.getKey("right").clicked) size++;
		}

		if (input.getKey("left").clicked) Sound.craft.play();
		if (input.getKey("right").clicked) Sound.craft.play();
		if (input.getKey("up").clicked) Sound.craft.play();
		if (input.getKey("down").clicked) Sound.craft.play();

		if (op > 2) op = 0;
		if (op < 0) op = 2;

		if (size > 2) size = 0;
		if (size < 0) size = 2;

		if (selected > 14) selected = 10;
		if (selected < 10) selected = 14;
		if (selectedlr > 3) selectedlr = 0;
		if (selectedlr < 0) selectedlr = 3;

		if (theme > 14) theme = 10;
		if (theme < 10) theme = 14;
		if (type > 3) type = 0;
		if (type < 0) type = 3;

		if (selected == 10) {
			theme = normal;
		} else if (selected == 11) {
			theme = forest;
		} else if (selected == 12) {
			theme = desert;
		} else if (selected == 13) {
			theme = plain;
		} else if (selected == 14) {
			theme = hell;
		}

		if (size == 0) {
			sized = sizeNorm;
		} else if (size == 1) {
			sized = sizeBig;
		} else if (size == 2) {
			sized = sizeHuge;
		}

		if (type == 0) {
			type = island;
		} else if (type == 1) {
			type = box;
		} else if (type == 2) {
			type = mount;
		} else if (type == 3) {
			type = irreg;
		}

		if (theme == 10) {
			theme = normal;
		} else if (theme == 11) {
			theme = forest;
		} else if (theme == 12) {
			theme = desert;
		} else if (theme == 13) {
			theme = plain;
		} else if (theme == 14) {
			theme = hell;
		}
		if (selectedlr == 0) {
			type = island;
		} else if (selectedlr == 1) {
			type = box;
		} else if (selectedlr == 2) {
			type = mount;
		} else if (selectedlr == 3) {
			type = irreg;
		}

		if (size == 0) {
			sized = sizeNorm;
		} else if (size == 1) {
			sized = sizeBig;
		} else if (size == 2) {
			sized = sizeHuge;
		}
	}

	public void render(Screen screen) {
		screen.clear(0);
		if (op == 0) {
			Font.draw("Size:", screen, 11 * 8 + 4, 12 * 8, Color.get(-1, 111, 111, 111));
			Font.draw("Theme:", screen, 11 * 8 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			Font.draw("Type:", screen, 11 * 8 + 4, 8 * 8, Color.get(-1, 111, 111, 111));
		} else if (op == 1) {
			Font.draw("Size:", screen, 11 * 8 + 4, 8 * 8, Color.get(-1, 111, 111, 111));
			Font.draw("Type:", screen, 11 * 8 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			Font.draw("Theme:", screen, 11 * 8 + 4, 12 * 8, Color.get(-1, 111, 111, 111));
		} else if (op == 2) {
			Font.draw("Size:", screen, 11 * 8 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			Font.draw("Type:", screen, 11 * 8 + 4, 12 * 8, Color.get(-1, 111, 111, 111));
			Font.draw("Theme:", screen, 11 * 8 + 4, 8 * 8, Color.get(-1, 111, 111, 111));
		}

		if (op == 0) {
			if (theme == 10)
				Font.draw("Normal", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 12)
				Font.draw("Desert", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 11)
				Font.draw("Forest", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 13)
				Font.draw("Plain", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 14)
				Font.draw("Hell", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
		} else if (op == 1) {
			if (type == 0) Font.draw("Island", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (type == 1)
				Font.draw("Box", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (type == 2)
				Font.draw("Mountain", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (type == 3)
				Font.draw("Irregular", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));

		} else if (op == 2) {
			if (size == 0) {
				Font.draw("Normal (128 x 128)", screen, 11 * 12 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			} else if (size == 1) {
				Font.draw("Big (256 x 256)", screen, 11 * 12 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			} else if (size == 2) {
				Font.draw("Huge (512 x 512)", screen, 11 * 12 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			}
		}

		Font.drawCentered("World Options", screen, 3 * 8, Color.get(0, 555, 555, 555));
		Font.drawCentered("Arrow keys to scroll", screen, 16 * 8, Color.get(-1, 555, 555, 555));
		Font.drawCentered("Press Esc to exit", screen, 18 * 8, Color.get(-1, 555, 555, 555));
		//This is debug info to see if the numbers are working correctly.
		//Font.draw("" + type, screen, 11 * 7 + 4, 21 * 8, Color.get(-1, 555, 555, 555));
		//Font.draw("" + theme, screen, 11 * 8 + 4, 21 * 8, Color.get(-1, 555, 555, 555));
	}
}
