// Do not delete or edit this
package com.mojang.ld22.screen;

// Do not delete or edit any of these imports
import java.util.List;

import com.mojang.ld22.crafting.Recipe;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

// Do not delete or edit this
public class BookTestMenu extends Menu {

	// this sets the start page "0" also is the Title page
	public int pages = 1;
	// this sets the last page
	public int lastpage = 1;

    // You don't need to mess with this.
	public BookTestMenu(List<Recipe> recipes, Player player) {
	}

	public void tick() {
		if (input.menu.clicked) game.setMenu(null); // this is what closes the book
		if (input.left.clicked) pages--; // this is what turns the page back
		if (input.right.clicked) pages++; // this is what turns the page forward

	}

	public void render(Screen screen) {
		// These draw out the screen.
		Font.renderFrameBook(screen, "", 14, 0, 21, 3);
		Font.renderFrameBook(screen, "", 1, 4, 34, 20);

		// Don't need to mess with this
		int xo = 12 * 11;
		int xe = 11 * 11;
		int xa = 11 * 12 - 3;
		int xu = 11 * 12 - 7;
	
		// This is makes the numbers appear below "Page"
		if (pages <= 9 && pages >= 1){
		Font.draw("" + pages, screen, xo + 7, 2 * 8, Color.get(-1, 0, 0, 0));
	
		// This makes it that "0" becomes "Title" instead.
		} else if (pages < 1){
		Font.draw("Title", screen, xe + 4, 2 * 8, Color.get(-1, 0, 0, 0));
		
		//This makes it so that the 2 digit numbers pose properly when the pages reach 10.
		} else if (pages > 9 && pages < 100){
			Font.draw("" + pages, screen, xa + 7, 2 * 8, Color.get(-1, 0, 0, 0));
		
		//This makes it so that the 3 digit numbers pose properly when the pages reach 100.
		} else if (pages > 99){
			Font.draw("" + pages, screen, xu + 7, 2 * 8, Color.get(-1, 0, 0, 0));
		}
		
		// This draws the text "Page" at the top of the screen
		Font.draw("Page", screen, 8 * 15 + 8, 1 * 8 - 2, Color.get(-1, 0, 0, 0));
		
		// This is that there are no Negative pages
		if (pages < 1)
			pages = 1;
		
		// This makes is that the player doesn't go past the last page.
		if (pages > lastpage)
			pages = lastpage;
		
		// You can write anywhere in the between the ""
		// Page 0 is the Title page
		if (pages == 0){
			// Make any edits beyond ,screen, to change the position and color.
			Font.draw(" ", screen, 8 * 9 + 8, 5 * 8, Color.get(-1, 0, 0, 0)); 
			Font.draw("", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 6 * 8, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 12 * 9, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 14 * 9, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));
		
			// this is page 1
		} else if (pages == 1){
			Font.draw("There is nothing of use.", screen, 15, 4 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 5 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 6 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 7 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 8 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 9 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 10 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 11 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 12 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw("", screen, 15, 13 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 14 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 15 * 9 + 4, Color.get(-1, 0, 0, 0));
			Font.draw(" ", screen, 15, 16 * 9 + 4, Color.get(-1, 0, 0, 0));

			} 
		
		
	}
}