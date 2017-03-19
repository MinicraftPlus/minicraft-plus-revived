package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

public class InstructionsMenu extends Menu {
	private Menu parent; // Creates a parent object to go back to

	/** The about menu is a read menu about what you have to do in the game. Only contains text and a black background */
	public InstructionsMenu(Menu parent) {
		this.parent = parent; // The parent Menu that it will go back to.
	}

	public void tick() {
		if (input.getKey("attack").clicked || input.getKey("menu").clicked) {
			game.setMenu(parent);  // If the user presses the "Attack" or "Menu" button, it will go back to the parent menu.
		}
	}

	/** Renders the text on the screen */
	public void render(Screen screen) {
		screen.clear(0); // clears the screen to be a black color.
		
		/* Font.draw Parameters: Font.draw(String text, Screen screen, int x, int y, int color) */

		Font.draw("HOW TO PLAY", screen, 4 * 8 + 4, 1 * 8, Color.get(0, 555, 555, 555)); //draws Title text
		Font.draw("Move your character", screen, 0 * 8 + 4, 3 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("with the arrow keys", screen, 0 * 8 + 4, 4 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("press C to attack", screen, 0 * 8 + 4, 5 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("and X to open the", screen, 0 * 8 + 4, 6 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("inventory and to", screen, 0 * 8 + 4, 7 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("use items.", screen, 0 * 8 + 4, 8 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("Select an item in", screen, 0 * 8 + 4, 9 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("the inventory to", screen, 0 * 8 + 4, 10 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("equip it.", screen, 0 * 8 + 4, 11 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("Kill the air wizard", screen, 0 * 8 + 4, 12 * 8, Color.get(0, 333, 333, 333)); // draws text
		Font.draw("to win the game!", screen, 0 * 8 + 4, 13 * 8, Color.get(0, 333, 333, 333)); // draws text
	}
}
