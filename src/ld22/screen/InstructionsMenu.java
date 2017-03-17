package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Screen;

public class InstructionsMenu extends Menu {
	private Menu parent;

	public InstructionsMenu(Menu parent) {
		this.parent = parent;
	}

	public void tick() {}

	public void render(Screen screen) {
		screen.clear(0);
	}
}
