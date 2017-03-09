//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.Game; 
import com.mojang
.ld22.entity.Player;

public class SleepMenu extends Menu {
	private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not :).
	public static boolean shudrespawn;
	public int tickCount = 0;
	int selectState = 0;
	public SleepMenu() {
	}
	
	public void tick() {
	 if (game.isDayNoSleep == false) {
		if (tickCount == 400) {
			selectState=2;
			
				Game.tickCount = 6000;
				Game.Time = 1;
				//game.setMenu(null);
			 System.out.println("SLEEPING!");
		}
		else {
			selectState=1;
			tickCount++;
			 if (input.getKey("menu").clicked) {
				 game.setMenu(null);
				tickCount = 0;
			 }
			
		}
		}
	 else if (game.isDayNoSleep) {
		 if (input.getKey("menu").clicked) {
			 game.setMenu(null);
		 }
		 selectState = 0;
	 }
	 if (selectState == 2) {
		 if (input.getKey("menu").clicked) {
			 game.setMenu(null);
		 }
	 }
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 3, 21, 7);
		//System.out.println(Player.sentFromHome);
		
		if (selectState == 0){ Font.draw("It's Day, no sleep!", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555));
		Font.draw("X:Exit", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
		
		}
		else if (selectState == 1){ Font.draw("Sleeping...", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555));
		Font.draw("X:Exit", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
		}
		else if (selectState == 2){Font.draw("It's daytime!", screen, 2 * 8, 4 * 8, Color.get(-1, 555, 555, 555));
		Font.draw("X:Exit", screen, 2 * 8, 5 * 8, Color.get(-1, 555, 555, 555));
		
		}
		
			
			
		
		
		
		}
		
	}
	
	
	