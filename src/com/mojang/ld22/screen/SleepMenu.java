//respawn mod +dillyg10+
package com.mojang.ld22.screen;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.Game; 
import com.mojang.ld22.entity.Player;

public class SleepMenu extends Menu {
	private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not :).
	public static boolean shudrespawn;
	public int tickCount = 0;
	int selectState = 0;
	public SleepMenu() {
	}
	
	public void tick() {
		if (!game.isDayNoSleep) {
			if (tickCount == 400) {
				selectState = 2;
				
				Game.tickCount = 6000;
				Game.Time = 1;
				//game.setMenu(null);
				System.out.println("SLEEPING!");
			}
			else {
				selectState = 1;
				tickCount++;
				if (input.getKey("menu").clicked)
					tickCount = 0;
			}
		}
		else
			selectState = 0;
		
		if (input.getKey("menu").clicked)
			game.setMenu(null);
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "", 1, 3, 21, 7);
		//System.out.println(Player.sentFromHome);
		
		String[] messages = {"It's Day, no sleep!", "Sleeping...", "It's daytime!"};
		
		drawCentered(messages[selectState], screen, 4*8, Color.get(-1, 555, 555, 555));
		drawCentered("Enter:Exit", screen, 5*8, Color.get(-1, 555, 555, 555));
	}
}
