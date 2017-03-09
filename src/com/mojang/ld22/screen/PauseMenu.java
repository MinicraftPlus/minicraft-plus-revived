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

	private int selected = 0;
	private boolean o1 = false;
	private boolean o2 = false;
	private boolean o3 = false;
	Player player;
	private static final String[] options = new String[]{"Return to Game", "Options", "Save Game", "Load Game", "Main Menu"};


	public PauseMenu(Player player) {
		player = player;
	}

	public void tick() {
		if(input.getKey("pause").clicked) {
			game.setMenu((Menu)null);
		}

		if(input.getKey("up").clicked) {
			--selected;
		}

		if(input.getKey("down").clicked) {
			selected++;
		}

		int len = options.length;
		if(selected < 0) {
			selected += len;
		}

		if(selected >= len) {
			selected -= len;
		}

		if(input.getKey("enter").clicked) {
			if(o1 && !o2 && !o3) {
				game.setMenu((Menu)null);
				new Save(player, WorldSelectMenu.worldname);
			}

			if(!o1 && o2 && !o3) {
				WorldSelectMenu m = new WorldSelectMenu(new TitleMenu());
				WorldSelectMenu.loadworld = true;
				m.createworld = false;
				game.setMenu(m);
			}

			if(!o1 && !o2 && o3) {
				game.setMenu(new TitleMenu());
			}
		}

		if(input.getKey("escape").clicked) {
			if(o1 && !o2 && !o3) {
				game.setMenu((Menu)null);
			}

			if(!o1 && o2 && !o3) {
				game.setMenu((Menu)null);
			}

			if(!o1 && !o2 && o3) {
				game.setMenu((Menu)null);
			}
		}
		
		if(input.getKey("escape").clicked || input.getKey("enter").clicked) {
			if(selected == 0) {
				o1 = false;
				o2 = false;
				o3 = false;
				game.setMenu((Menu)null);
			}

			if(selected == 1) {
				o1 = false;
				o2 = false;
				o3 = false;
				TitleMenu.sentFromMenu = false;
				game.setMenu(new StartMenu());
			}

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
			}
		}
	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "", 4, 2, 32, 20);
		if(!o1 && !o2 && !o3) {
			for(int var5 = 0; var5 < 5; ++var5) {
				String msg1 = options[var5];
				int col = Color.get(-1, 222, 222, 222);
				if(var5 == selected) {
					msg1 = ">" + msg1 + "<";
					col = Color.get(-1, 555, 555, 555);
				}

				Font.draw(msg1, screen, (screen.w - msg1.length() * 8) / 2, (8 + var5) * 12 - 35, col);
				Font.draw("Paused", screen, centertext("Paused"), 35, Color.get(-1, 550, 550, 550));
				Font.draw("Arrow Keys to Scroll", screen, centertext("Arrow Keys to Scroll"), 135, Color.get(-1, 333, 333, 333));
				Font.draw("Enter: Choose", screen, centertext("Enter: Choose"), 145, Color.get(-1, 333, 333, 333));
			}
		} else {
			String msg;
			if(o1 && !o2 && !o3) {
				msg = "Save Game?";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 60, Color.get(-1, 555, 555, 555));
				msg = "X: Yes";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 80, Color.get(-1, 555, 555, 555));
				msg = "C: No";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 95, Color.get(-1, 555, 555, 555));
			} else if(!o1 && o2 && !o3) {
				msg = "Load Game?";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 60, Color.get(-1, 555, 555, 555));
				msg = "Current game will";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 70, Color.get(-1, 500, 500, 500));
				msg = "not be saved";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 80, Color.get(-1, 500, 500, 500));
				msg = "X: Yes";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 100, Color.get(-1, 555, 555, 555));
				msg = "C: No";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 115, Color.get(-1, 555, 555, 555));
			} else if(!o1 && !o2 && o3) {
				msg = "Back to Main Menu?";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2 + 1, 60, Color.get(-1, 555, 555, 555));
				msg = "Current game will";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 70, Color.get(-1, 500, 500, 500));
				msg = "not be saved";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 80, Color.get(-1, 500, 500, 500));
				msg = "X: Yes";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 100, Color.get(-1, 555, 555, 555));
				msg = "C: No";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 115, Color.get(-1, 555, 555, 555));
			}
		}

	}
}
