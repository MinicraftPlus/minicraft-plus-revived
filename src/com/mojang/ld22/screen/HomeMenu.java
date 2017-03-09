//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.Game; 
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.screen.ModeMenu;

public class HomeMenu extends Menu {
	private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not :).
	public static boolean shudrespawn;
	public HomeMenu() {
	}
	boolean cgh = Player.canGoHome;
	boolean hsh = Player.hasSetHome;
	
	public void tick() {
		if (input.getKey("menu").clicked/* || input.getKey("attack").clicked*/) {
			game.setMenu(null);
			Player.sentFromHome = false;
		}
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 3, 18, 7);
		//System.out.println(Player.sentFromHome);
		
		if (Player.sentFromHome == true && Player.sentFromSetHome == false) {
			if (hsh== false) {
				if (cgh ==true) {
				Font.draw("You don't have a", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555)); 
				Font.draw("home!", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
				}
				else if (cgh == false) {
					Font.draw("You can't go home", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555)); 
					
					Font.draw("from here!", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
				}
			}
			else if (hsh == true) {
				if (cgh == false) {
					Font.draw("You can't go home", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555)); 
					
					Font.draw("from here!", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
				} else {
					Font.draw("Home sweet home!", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555));
					if (ModeMenu.hardcore)
						Font.draw("HardCore = -2", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
				}
			}
			
		}
	}
}
	
	