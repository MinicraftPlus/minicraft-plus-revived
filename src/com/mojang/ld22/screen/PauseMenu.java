//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.Game; 

public class PauseMenu extends Menu {
	public PauseMenu() {
	}
	public static boolean title = false;
	public static boolean newGame = false;
	public static boolean respawn = false;
	
	public void tick() {
		 if (input.attack.clicked || input.pause.clicked) {
			game.setMenu(null);
		}
		 if (input.menu.clicked) {
			title = true;
			newGame = false;
			respawn = false;
			
			game.setMenu(new ConfirmMenu());
			
		 }
		 if (input.craft.clicked) {
			 newGame = true;
			 respawn = false;
			 title = false; 
			game.setMenu(new ConfirmMenu());
			
		 }
		 if (input.options.clicked) {
			 TitleMenu.sentFromMenu = false;
			 game.setMenu(new StartMenu());
		 }
		
		//This is so that if the user presses x @ respawn menu, they respawn (what a concept)
		//if (input.)
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 1, 15, 13);
		Font.draw("Pause...", screen, 5 * 8, 2 * 8, Color.get(-1, 550, 550, 550));
		
		
		Font.draw("C:Return", screen, 2 * 8, 4 * 8, Color.get(-1, 333, 333, 333));
		Font.draw("X:Quit", screen, 2 * 8, 5 * 8, Color.get(-1, 333, 333, 333));
		Font.draw("Z:New", screen, 2 * 8, 6 * 8, Color.get(-1, 333, 333, 333));
		Font.draw("O:Options", screen, 2 * 8, 7 * 8, Color.get(-1, 333, 333, 333));
		
		
	}
}