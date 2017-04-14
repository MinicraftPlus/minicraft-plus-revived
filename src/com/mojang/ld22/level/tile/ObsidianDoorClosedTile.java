package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;

public class ObsidianDoorClosedTile extends Tile {
	public ObsidianDoorClosedTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col0 = Color.get(203, 102, 102, 204);
		
		int col1 = Color.get(203, 102, 203, 102);
		
		int col2 = Color.get(203, 102, 111, 102);
		
		int col3 = Color.get(102, 000, 111, 103);
		
		int col4 = Color.get(102, 102, 203, 103);
		
		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;
				screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
			}
		}
		if (Game.time == 1) {
			int col = col1;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
		}
		if (Game.time == 2) {
			int col = col2;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
		}
		if (Game.time == 3) {
			int col = col3;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
		}

		if (level.dirtColor == 222) {
			int col = col4;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
		}
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.setTile(x, y, Tile.odo, 0);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}
}
