//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

public class DeadMenu extends Menu {
	private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not. :)
	public static boolean shudrespawn;

	public DeadMenu() {}

	public void tick() {
		if (inputDelay > 0) {
			inputDelay--;
		} else if (input.getKey("escape").clicked) {
			game.setMenu(new TitleMenu());
			shudrespawn = false;
		}
		//This is so that if the user presses x @ respawn menu, they respawn (what a concept)
		if (ModeMenu.hardcore == false) {
			if (input.getKey("enter").clicked) {
				//This makes it so the player respawns
				shudrespawn = true;
				//reset game function
				game.resetGame();
				//sets the menu to nothing
				game.setMenu(null);
			}
		}
	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 3, 18, 10);
		Font.draw("You died! Aww!", screen, 16, 32, Color.get(-1, 555, 555, 555));

		int seconds = game.gameTime / 60;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;

		String timeString = "";
		if (hours > 0) {
			timeString = hours + "h" + (minutes < 10 ? "0" : "") + minutes + "m";
		} else {
			timeString = minutes + "m " + (seconds < 10 ? "0" : "") + seconds + "s";
		}

		Font.draw("Time:", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
		Font.draw(timeString, screen, (2 + 5) * 8, 5 * 8, Color.get(-1, 550, 550, 550));
		Font.draw("Score:", screen, 2 * 8, 6 * 8, Color.get(-1, 555, 555, 555));
		Font.draw("" + game.player.score, screen, (2 + 6) * 8, 6 * 8, Color.get(-1, 550, 550, 550));
		Font.draw("Escape = lose", screen, 2 * 8, 8 * 8, Color.get(-1, 333, 333, 333));
		if (ModeMenu.hardcore == false)
			Font.draw("Enter = respawn", screen, 2 * 8, 9 * 8, Color.get(-1, 333, 333, 333));
	}
}
