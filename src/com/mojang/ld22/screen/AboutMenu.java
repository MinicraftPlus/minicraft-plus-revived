package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

public class AboutMenu extends Menu {
	private Menu parent;

	public AboutMenu(Menu parent) {
		this.parent = parent;
	}

	public void tick() {
		if (input.attack.clicked || input.menu.clicked) {
			game.setMenu(parent);
		}
	}

	public void render(Screen screen) {
		screen.clear(0);

		Font.draw("About MinicraftPlus", screen, 8 * 8 + 4, 1 * 8, Color.get(0, 555, 555, 555));
		Font.draw("Moded by David.b and +Dillyg10+", screen, 2 * 8 + 4, 3 * 8, Color.get(0, 333, 333, 333));
		Font.draw("Our goal is to expand Minicraft to", screen, 1 * 8 + 4, 5 * 8, Color.get(0, 333, 333, 333));
		Font.draw("be more fun and continuous.", screen, 5 * 8 + 4, 6 * 8, Color.get(0, 333, 333, 333));
		Font.draw("Minicraft made by Markus Perrson", screen, 1 * 8 + 4, 8 * 8, Color.get(0, 333, 333, 333));
		Font.draw("for ludum dare 22 competition.", screen, 2 * 8 + 4, 9 * 8, Color.get(0, 333, 333, 333));
	}
}
