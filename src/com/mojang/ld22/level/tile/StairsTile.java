package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;

public class StairsTile extends Tile {
	private boolean leadsUp;

	public StairsTile(int id, boolean leadsUp) {
		super(id);
		this.leadsUp = leadsUp;
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col0 = Color.get(322, 000, 333, 444);
		int col1 = Color.get(321, 000, 444, 555);
		int col2 = Color.get(211, 000, 333, 444);
		int col3 = Color.get(100, 000, 222, 333);
		int col4 = Color.get(222, 000, 333, 444);
		if (Game.currentLevel == 5) {
			col4 = Color.get(59, 000, 333, 444);
		}
		if (Game.currentLevel > -1 && Game.currentLevel < 5) {
			col4 = Color.get(222, 000, 333, 444);
		}

		if (level.dirtColor == 322)
			if (Game.time == 0) {
				int color = col0;
				int xt = 0;
				if (leadsUp) xt = 2;

				screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
				screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
				screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
				screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
			}
		if (Game.time == 1) {
			int color = col1;
			int xt = 0;
			if (leadsUp) xt = 2;

			screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
			screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		}
		if (Game.time == 2) {
			int color = col2;
			int xt = 0;
			if (leadsUp) xt = 2;

			screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
			screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		}
		if (Game.time == 3) {
			int color = col3;
			int xt = 0;
			if (leadsUp) xt = 2;

			screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
			screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		}
		if (level.dirtColor != 322) {
			int color = col4;
			int xt = 0;
			if (leadsUp) xt = 2;

			screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
			screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		}
	}
}
