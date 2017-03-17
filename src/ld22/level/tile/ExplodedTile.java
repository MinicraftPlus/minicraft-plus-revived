package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;

public class ExplodedTile extends Tile {
	public ExplodedTile(int id) {
		super(id);
		connectsToSand = true;
		connectsToWater = true;
		connectsToLava = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(555, 555, 555, 550);
		int transitionColor1 = Color.get(3, 555, level.dirtColor - 111, level.dirtColor);
		int transitionColor2 = Color.get(3, 555, level.sandColor - 110, level.sandColor);

		boolean u = !level.getTile(x, y - 1).connectsToLiquid();
		boolean d = !level.getTile(x, y + 1).connectsToLiquid();
		boolean l = !level.getTile(x - 1, y).connectsToLiquid();
		boolean r = !level.getTile(x + 1, y).connectsToLiquid();

		boolean su = u && level.getTile(x, y - 1).connectsToSand;
		boolean sd = d && level.getTile(x, y + 1).connectsToSand;
		boolean sl = l && level.getTile(x - 1, y).connectsToSand;
		boolean sr = r && level.getTile(x + 1, y).connectsToSand;

		if (!u && !l) {
			screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
		} else
			screen.render(
					x * 16 + 0,
					y * 16 + 0,
					(l ? 14 : 15) + (u ? 0 : 1) * 32,
					(su || sl) ? transitionColor2 : transitionColor1,
					0);

		if (!u && !r) {
			screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
		} else
			screen.render(
					x * 16 + 8,
					y * 16 + 0,
					(r ? 16 : 15) + (u ? 0 : 1) * 32,
					(su || sr) ? transitionColor2 : transitionColor1,
					0);

		if (!d && !l) {
			screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
		} else
			screen.render(
					x * 16 + 0,
					y * 16 + 8,
					(l ? 14 : 15) + (d ? 2 : 1) * 32,
					(sd || sl) ? transitionColor2 : transitionColor1,
					0);
		if (!d && !r) {
			screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
		} else
			screen.render(
					x * 16 + 8,
					y * 16 + 8,
					(r ? 16 : 15) + (d ? 2 : 1) * 32,
					(sd || sr) ? transitionColor2 : transitionColor1,
					0);
	}

	public void steppedOn(Level level, int x, int y, Entity entity) {
		entity.hurt(this, x, y, 50);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
