//new class; no comments
//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;

public class InfoMenu extends Menu {
	private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not :).
	public static boolean shudrespawn;

	public InfoMenu() {}

	public void tick() {
		if (input.getKey("enter").clicked || input.getKey("escape").clicked) {
			game.setMenu(null);
			Player.sentFromSetHome = false;
			Player.sentFromHome = false;
		}
	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 3, 18, 7);
		//if(com.mojang.ld22.Game.debug) System.out.println(Player.sentFromHome);

		if (Player.sentFromSetHome) {
			if (Player.canSetHome == true)
				Font.draw("Set your home!", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555));
			else if (Player.canSetHome == false) {
				Font.draw("Can't set home", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555));
				Font.draw("here!", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
			}
		}
	}
}
