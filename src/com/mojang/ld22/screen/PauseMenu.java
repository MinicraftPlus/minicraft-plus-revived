package com.mojang.ld22.screen;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.saveload.Save;

public class PauseMenu extends Menu {

	private int selected, selection; //selection is set when you press enter.
	Player player;

	private static final String[] options =
			new String[] {"Return to Game", "Options", "Save Game", "Load Game", "Main Menu"};

	public PauseMenu(Player player) {
		this.player = player;
		selected = 0;
		selection = -1;
	}

	public void tick() {

		if (input.getKey("pause").clicked) game.setMenu((Menu) null);

		//note: b/c comfirm menus have no cursor, "selected" doesn't matter after presing enter...
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;

		//int len = options.length;
		if (selected < 0) selected = options.length - 1;

		if (selected >= options.length) selected = 0;

		//choice chosen; input here is at confirm menu
		if (input.getKey("enter").clicked) {

			//this one is an EXCEPTION: no comfirm menu.
			if (selected == 1) {
				//I bet this is used when exiting options menu to decide whether to go to title screen, or pause menu:
				game.setMenu(new OptionsMenu(this));
			}

			if (selection == 2) { //save game option
				game.setMenu((Menu) null);
				new Save(player, WorldSelectMenu.worldname);
			}

			if (selection == 3) { //load game option; can't return
				WorldSelectMenu m = new WorldSelectMenu(new TitleMenu());
				WorldSelectMenu.loadworld = true;
				m.createworld = false;
				game.setMenu(m);
			}

			if (selection == 4) //title menu
				game.setMenu(new TitleMenu());
			
			if (selected != 1) selection = selected;
		}

		if (input.getKey("escape").clicked || selection == 0) game.setMenu((Menu) null);
	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "", 4, 2, 32, 20);
		
		if (selection == -1) {
			for (int i = 0; i < 5; i++) {
				String msg1 = options[i];
				int col = Color.get(-1, 222, 222, 222);
				if (i == selected) {
					msg1 = ">" + msg1 + "<";
					col = Color.get(-1, 555, 555, 555);
				}

				writeCentered(msg1, screen, (8 + i) * 12 - 35, col);
				writeCentered("Paused", screen, 35, Color.get(-1, 550, 550, 550));
				writeCentered("Arrow Keys to Scroll", screen, 135, Color.get(-1, 333, 333, 333));
				writeCentered("Enter: Choose", screen, 145, Color.get(-1, 333, 333, 333));
			}
		} else {
			int confirmY1 = 0, confirmY2 = 0;
			if (selection == 2) {
				writeCentered("Save Game?", screen, 60, Color.get(-1, 555, 555, 555));
				confirmY1 = 80;
				confirmY2 = 95;
			} else if (selection == 3) {
				writeCentered("Load Game?", screen, 60, Color.get(-1, 555, 555, 555));
				writeCentered("Current game will", screen, 70, Color.get(-1, 500, 500, 500));
				writeCentered("not be saved", screen, 80, Color.get(-1, 500, 500, 500));
				confirmY1 = 100;
				confirmY2 = 115;
			} else if (selection == 4) {
				writeCentered("Back to Main Menu?", screen, 60, Color.get(-1, 555, 555, 555));
				writeCentered("Current game will", screen, 70, Color.get(-1, 500, 500, 500));
				writeCentered("not be saved", screen, 80, Color.get(-1, 500, 500, 500));
				confirmY1 = 100;
				confirmY2 = 115;
			}

			writeCentered("Enter: Yes", screen, confirmY1, Color.get(-1, 555, 555, 555));
			writeCentered("Esc: No", screen, confirmY2, Color.get(-1, 555, 555, 555));
		}
	}
}
