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
		this.player = player;
	}

	public void tick() {
		if(this.input.getKey("pause").clicked) {
			this.game.setMenu((Menu)null);
		}

		if(this.input.getKey("up").clicked) {
			--this.selected;
		}

		if(this.input.getKey("down").clicked) {
			++this.selected;
		}

		int len = options.length;
		if(this.selected < 0) {
			this.selected += len;
		}

		if(this.selected >= len) {
			this.selected -= len;
		}

		if(this.input.getKey("menu").clicked) {
			if(this.o1 && !this.o2 && !this.o3) {
				this.game.setMenu((Menu)null);
				new Save(this.player, WorldSelectMenu.worldname);
			}

			if(!this.o1 && this.o2 && !this.o3) {
				WorldSelectMenu m = new WorldSelectMenu(new TitleMenu());
				WorldSelectMenu.loadworld = true;
				m.createworld = false;
				this.game.setMenu(m);
			}

			if(!this.o1 && !this.o2 && this.o3) {
				this.game.setMenu(new TitleMenu());
			}
		}

		if(this.input.getKey("attack").clicked) {
			if(this.o1 && !this.o2 && !this.o3) {
				this.game.setMenu((Menu)null);
			}

			if(!this.o1 && this.o2 && !this.o3) {
				this.game.setMenu((Menu)null);
			}

			if(!this.o1 && !this.o2 && this.o3) {
				this.game.setMenu((Menu)null);
			}
		}

		if(this.input.getKey("attack").clicked || this.input.getKey("menu").clicked) {
			if(this.selected == 0) {
				this.o1 = false;
				this.o2 = false;
				this.o3 = false;
				this.game.setMenu((Menu)null);
			}

			if(this.selected == 1) {
				this.o1 = false;
				this.o2 = false;
				this.o3 = false;
				TitleMenu.sentFromMenu = false;
				this.game.setMenu(new StartMenu());
			}

			if(this.selected == 2) {
				this.o1 = true;
				this.o2 = false;
				this.o3 = false;
			}

			if(this.selected == 3) {
				this.o1 = false;
				this.o2 = true;
				this.o3 = false;
			}

			if(this.selected == 4) {
				this.o1 = false;
				this.o2 = false;
				this.o3 = true;
			}
		}

	}

	public void render(Screen screen) {
		Font.renderFrame(screen, "", 4, 2, 32, 20);
		if(!this.o1 && !this.o2 && !this.o3) {
			for(int var5 = 0; var5 < 5; ++var5) {
				String msg1 = options[var5];
				int col = Color.get(-1, 222, 222, 222);
				if(var5 == this.selected) {
					msg1 = ">" + msg1 + "<";
					col = Color.get(-1, 555, 555, 555);
				}

				Font.draw(msg1, screen, (screen.w - msg1.length() * 8) / 2, (8 + var5) * 12 - 35, col);
				Font.draw("Paused", screen, this.centertext("Paused"), 35, Color.get(-1, 550, 550, 550));
				Font.draw("Arrow Keys to Scroll", screen, this.centertext("Arrow Keys to Scroll"), 135, Color.get(-1, 333, 333, 333));
				Font.draw("X or C: Choose", screen, this.centertext("X or C: Choose"), 145, Color.get(-1, 333, 333, 333));
			}
		} else {
			String msg;
			if(this.o1 && !this.o2 && !this.o3) {
				msg = "Save Game?";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 60, Color.get(-1, 555, 555, 555));
				msg = "X: Yes";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 80, Color.get(-1, 555, 555, 555));
				msg = "C: No";
				Font.draw(msg, screen, (screen.w - msg.length() * 8) / 2, 95, Color.get(-1, 555, 555, 555));
			} else if(!this.o1 && this.o2 && !this.o3) {
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
			} else if(!this.o1 && !this.o2 && this.o3) {
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
