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
		if (input.getKey("escape").clicked || input.getKey("enter").clicked) {
			game.setMenu(parent);  // If the user presses escape, it will go back to the parent menu.
		}
	}

	/** Renders the text on the screen */
	public void render(Screen screen) {
		screen.clear(0); // clears the screen to be a black color.
		
		/* Font.draw Parameters: Font.draw(String text, Screen screen, int x, int y, int color) */
		
		Font.drawCentered("HOW TO PLAY", screen, 1 * 8, Color.get(0, 555, 555, 555)); //draws Title text
		boolean wroteAll = writeParagraph(
		  "Move your character with the arrow keys. Press C to attack and X to open the inventory, and to use items. Select an item in the inventory to equip it. Kill the air wizard to win the game!",
		  screen, 0, screen.w, 24, screen.h, Color.get(0, 333, 333, 333), true);
		
		if (!wroteAll) System.out.println("Paragraph was truncated!");
	}
	
	private boolean writeParagraph(String para, Screen screen, int minX, int maxX, int minY, int maxY, int color, boolean breakOnSentence) {
		String[] words = para.split(" ");
		int curWord = 0, y = minY;
		while(curWord < words.length && y <= maxY) {
			String line = words[curWord];
			curWord++;
			while(curWord < words.length && textWidth(line)+textWidth(" "+words[curWord]) < maxX - minX) {
				line += " "+words[curWord];
				curWord++;
				if (breakOnSentence && words[curWord-1].charAt(words[curWord-1].length()-1) == '.')
					break;
			}
			Font.drawCentered(line, screen, y, color);
			if (curWord < words.length || true) y += 8;
		}
		
		if (y > maxY) return false;
		else return true;
	}
	
	private int textWidth(String text) {return text.length() * 8;}
}
