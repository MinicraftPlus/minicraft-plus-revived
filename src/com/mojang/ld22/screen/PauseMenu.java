package com.mojang.ld22.screen;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.saveload.Save;
import com.mojang.ld22.screen.Menu;
import com.mojang.ld22.screen.StartMenu;
import com.mojang.ld22.screen.TitleMenu;
import com.mojang.ld22.screen.WorldSelectMenu;

public class PauseMenu extends Menu {
	
	private int selected, selection; //selection is set when you press enter.
	//private boolean o1 = false;
	//private boolean o2 = false;
	//private boolean o3 = false;
	Player player;
	private static final String[] options = new String[] {
		"Return to Game", "Options", "Save Game", "Load Game", "Main Menu"
	};
	
	
	public PauseMenu(Player player) {
		this.player = player;
		//chosen = false;
		selected = 0;
		selection = -1;
		//prevselect = selected;
	}
	
	public void tick() {
		//prevselect = selected;
		
		if(input.getKey("pause").clicked)
			game.setMenu((Menu)null);
		
		//note: b/c comfirm menus have no cursor, "selected" doesn't matter after presing enter...
		if(input.getKey("up").clicked)
			selected--;
		if(input.getKey("down").clicked)
			selected++;
		
		//int len = options.length;
		if(selected < 0)
			selected = options.length-1;
		
		if(selected >= options.length)
			selected = 0;
		
		//choice chosen; input here is at confirm menu 
		if(input.getKey("enter").clicked) {
			
			//this one is an EXCEPTION: no comfirm menu.
			if(selected == 1) {
				//I bet this is used when exiting options menu to decide whether to go to title screen, or pause menu:
				TitleMenu.sentFromMenu = false;
				game.setMenu(new StartMenu());
			}
			
			if(selection == 2) { //save game option
				game.setMenu((Menu)null);
				new Save(player, WorldSelectMenu.worldname);
			}
			
			if(selection == 3) { //load game option
				WorldSelectMenu m = new WorldSelectMenu(new TitleMenu());
				WorldSelectMenu.loadworld = true;
				m.createworld = false;
				game.setMenu(m);
			}
			
			if(selection == 4) //title menu
				game.setMenu(new TitleMenu());
			
			selection = selected;
		}
		
		if(input.getKey("escape").clicked || selection == 0) {
			//if(prevselect >= 2) //if in sub-menu...right?
				game.setMenu((Menu)null);
		}
		//else if(input.getKey("enter").clicked)
			
		
		
		//if(/*input.getKey("escape").clicked || */input.getKey("enter").clicked) {
		/*	selection = selected;
			//sets no matter the menu
			if(selected == 0) {
				/*o1 = false;
				o2 = false;
				o3 = false;
				*///game.setMenu((Menu)null);
		/*	}
			
			if(selected == 1) {
				/*o1 = false;
				o2 = false;
				o3 = false;
				*///TitleMenu.sentFromMenu = false;
		/*		game.setMenu(new StartMenu());
			}
			/*
			if(selected == 2) {
				o1 = true;
				o2 = false;
				o3 = false;
			}

			if(selected == 3) {
				o1 = false;
				o2 = true;
				o3 = false;
			}

			if(selected == 4) {
				o1 = false;
				o2 = false;
				o3 = true;
			}*/
		//}
	}
	
	public void render(Screen screen) {
		Font.renderFrame(screen, "", 4, 2, 32, 20);
		if(selection == -1) {
			for(int i = 0; i < 5; i++) {
				String msg1 = options[i];
				int col = Color.get(-1, 222, 222, 222);
				if(i == selected) {
					msg1 = ">" + msg1 + "<";
					col = Color.get(-1, 555, 555, 555);
				}
				
				drawCentered(msg1, screen, (8 + i) * 12 - 35, col);
				drawCentered("Paused", screen, 35, Color.get(-1, 550, 550, 550));
				drawCentered("Arrow Keys to Scroll", screen, 135, Color.get(-1, 333, 333, 333));
				drawCentered("Enter: Choose", screen, 145, Color.get(-1, 333, 333, 333));
			}
		} else {
			int confirmY1 = 0, confirmY2 = 0;
			if(selection == 2) {
				drawCentered("Save Game?", screen, 60, Color.get(-1, 555, 555, 555));
				confirmY1 = 80; confirmY2 = 95;
			} else if(selection == 3) {
				drawCentered("Load Game?", screen, 60, Color.get(-1, 555, 555, 555));
				drawCentered("Current game will", screen, 70, Color.get(-1, 500, 500, 500));
				drawCentered("not be saved", screen, 80, Color.get(-1, 500, 500, 500));
				confirmY1 = 100; confirmY2 = 115;
			} else if(selection == 4) {
				drawCentered("Back to Main Menu?", screen, 60, Color.get(-1, 555, 555, 555));
				drawCentered("Current game will", screen, 70, Color.get(-1, 500, 500, 500));
				drawCentered("not be saved", screen, 80, Color.get(-1, 500, 500, 500));
				confirmY1 = 100; confirmY2 = 115;
			}
			
			drawCentered("Enter: Yes", screen, confirmY1, Color.get(-1, 555, 555, 555));
			drawCentered("Esc: No", screen, confirmY2, Color.get(-1, 555, 555, 555));
		}
		
	}
}
