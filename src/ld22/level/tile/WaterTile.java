package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;
import java.util.Random;

public class WaterTile extends Tile {
	public WaterTile(int id) {
		super(id);
		connectsToSand = true;
		connectsToWater = true;
	}

	private Random wRandom = new Random();

	public void render(Screen screen, Level level, int x, int y) {

		int col0 = Color.get(115, 115, 225, 225);
		int col00 = Color.get(3, 115, 211, 322);
		int col000 = Color.get(3, 115, 330, 440);

		int col1 = Color.get(005, 105, 115, 115);
		int col11 = Color.get(3, 105, 211, 321);
		int col111 = Color.get(3, 105, 440, 550);

		int col2 = Color.get(005, 015, 115, 115);
		int col22 = Color.get(105, 015, 100, 211);
		int col222 = Color.get(105, 015, 220, 330);

		int col3 = Color.get(015, 015, 115, 115);
		int col33 = Color.get(015, 015, 000, 100);
		int col333 = Color.get(015, 015, 110, 220);

		int col4 = Color.get(005, 105, 115, 115);
		int col44 = Color.get(3, 105, 111, 222);
		int col444 = Color.get(3, 105, 440, 550);

		if (level.dirtColor == 322)
			if (Game.Time == 0) {

				wRandom.setSeed(
						(tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l);
				int col = col0;
				int transitionColor1 = col00;
				int transitionColor2 = col000;

				boolean u = !level.getTile(x, y - 1).connectsToWater;
				boolean d = !level.getTile(x, y + 1).connectsToWater;
				boolean l = !level.getTile(x - 1, y).connectsToWater;
				boolean r = !level.getTile(x + 1, y).connectsToWater;

				boolean su = u && level.getTile(x, y - 1).connectsToSand;
				boolean sd = d && level.getTile(x, y + 1).connectsToSand;
				boolean sl = l && level.getTile(x - 1, y).connectsToSand;
				boolean sr = r && level.getTile(x + 1, y).connectsToSand;

				if (!u && !l) {
					screen.render(x * 16 + 0, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
				} else
					screen.render(
							x * 16 + 0,
							y * 16 + 0,
							(l ? 14 : 15) + (u ? 0 : 1) * 32,
							(su || sl) ? transitionColor2 : transitionColor1,
							0);

				if (!u && !r) {
					screen.render(x * 16 + 8, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
				} else
					screen.render(
							x * 16 + 8,
							y * 16 + 0,
							(r ? 16 : 15) + (u ? 0 : 1) * 32,
							(su || sr) ? transitionColor2 : transitionColor1,
							0);

				if (!d && !l) {
					screen.render(x * 16 + 0, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
				} else
					screen.render(
							x * 16 + 0,
							y * 16 + 8,
							(l ? 14 : 15) + (d ? 2 : 1) * 32,
							(sd || sl) ? transitionColor2 : transitionColor1,
							0);
				if (!d && !r) {
					screen.render(x * 16 + 8, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
				} else
					screen.render(
							x * 16 + 8,
							y * 16 + 8,
							(r ? 16 : 15) + (d ? 2 : 1) * 32,
							(sd || sr) ? transitionColor2 : transitionColor1,
							0);
			}
		if (Game.Time == 1) {

			wRandom.setSeed(
					(tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l);
			int col = col1;
			int transitionColor1 = col11;
			int transitionColor2 = col111;

			boolean u = !level.getTile(x, y - 1).connectsToWater;
			boolean d = !level.getTile(x, y + 1).connectsToWater;
			boolean l = !level.getTile(x - 1, y).connectsToWater;
			boolean r = !level.getTile(x + 1, y).connectsToWater;

			boolean su = u && level.getTile(x, y - 1).connectsToSand;
			boolean sd = d && level.getTile(x, y + 1).connectsToSand;
			boolean sl = l && level.getTile(x - 1, y).connectsToSand;
			boolean sr = r && level.getTile(x + 1, y).connectsToSand;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 0,
						(l ? 14 : 15) + (u ? 0 : 1) * 32,
						(su || sl) ? transitionColor2 : transitionColor1,
						0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 0,
						(r ? 16 : 15) + (u ? 0 : 1) * 32,
						(su || sr) ? transitionColor2 : transitionColor1,
						0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 8,
						(l ? 14 : 15) + (d ? 2 : 1) * 32,
						(sd || sl) ? transitionColor2 : transitionColor1,
						0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 8,
						(r ? 16 : 15) + (d ? 2 : 1) * 32,
						(sd || sr) ? transitionColor2 : transitionColor1,
						0);
		}
		if (Game.Time == 2) {

			wRandom.setSeed(
					(tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l);
			int col = col2;
			int transitionColor1 = col22;
			int transitionColor2 = col222;

			boolean u = !level.getTile(x, y - 1).connectsToWater;
			boolean d = !level.getTile(x, y + 1).connectsToWater;
			boolean l = !level.getTile(x - 1, y).connectsToWater;
			boolean r = !level.getTile(x + 1, y).connectsToWater;

			boolean su = u && level.getTile(x, y - 1).connectsToSand;
			boolean sd = d && level.getTile(x, y + 1).connectsToSand;
			boolean sl = l && level.getTile(x - 1, y).connectsToSand;
			boolean sr = r && level.getTile(x + 1, y).connectsToSand;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 0,
						(l ? 14 : 15) + (u ? 0 : 1) * 32,
						(su || sl) ? transitionColor2 : transitionColor1,
						0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 0,
						(r ? 16 : 15) + (u ? 0 : 1) * 32,
						(su || sr) ? transitionColor2 : transitionColor1,
						0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 8,
						(l ? 14 : 15) + (d ? 2 : 1) * 32,
						(sd || sl) ? transitionColor2 : transitionColor1,
						0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 8,
						(r ? 16 : 15) + (d ? 2 : 1) * 32,
						(sd || sr) ? transitionColor2 : transitionColor1,
						0);
		}
		if (Game.Time == 3) {

			wRandom.setSeed(
					(tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l);
			int col = col3;
			int transitionColor1 = col33;
			int transitionColor2 = col333;

			boolean u = !level.getTile(x, y - 1).connectsToWater;
			boolean d = !level.getTile(x, y + 1).connectsToWater;
			boolean l = !level.getTile(x - 1, y).connectsToWater;
			boolean r = !level.getTile(x + 1, y).connectsToWater;

			boolean su = u && level.getTile(x, y - 1).connectsToSand;
			boolean sd = d && level.getTile(x, y + 1).connectsToSand;
			boolean sl = l && level.getTile(x - 1, y).connectsToSand;
			boolean sr = r && level.getTile(x + 1, y).connectsToSand;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 0,
						(l ? 14 : 15) + (u ? 0 : 1) * 32,
						(su || sl) ? transitionColor2 : transitionColor1,
						0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 0,
						(r ? 16 : 15) + (u ? 0 : 1) * 32,
						(su || sr) ? transitionColor2 : transitionColor1,
						0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 8,
						(l ? 14 : 15) + (d ? 2 : 1) * 32,
						(sd || sl) ? transitionColor2 : transitionColor1,
						0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 8,
						(r ? 16 : 15) + (d ? 2 : 1) * 32,
						(sd || sr) ? transitionColor2 : transitionColor1,
						0);
		}

		if (level.dirtColor == 222) {
			wRandom.setSeed(
					(tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l);
			int col = col4;
			int transitionColor1 = col44;
			int transitionColor2 = col444;

			boolean u = !level.getTile(x, y - 1).connectsToWater;
			boolean d = !level.getTile(x, y + 1).connectsToWater;
			boolean l = !level.getTile(x - 1, y).connectsToWater;
			boolean r = !level.getTile(x + 1, y).connectsToWater;

			boolean su = u && level.getTile(x, y - 1).connectsToSand;
			boolean sd = d && level.getTile(x, y + 1).connectsToSand;
			boolean sl = l && level.getTile(x - 1, y).connectsToSand;
			boolean sr = r && level.getTile(x + 1, y).connectsToSand;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 0,
						(l ? 14 : 15) + (u ? 0 : 1) * 32,
						(su || sl) ? transitionColor2 : transitionColor1,
						0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 0,
						(r ? 16 : 15) + (u ? 0 : 1) * 32,
						(su || sr) ? transitionColor2 : transitionColor1,
						0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 0,
						y * 16 + 8,
						(l ? 14 : 15) + (d ? 2 : 1) * 32,
						(sd || sl) ? transitionColor2 : transitionColor1,
						0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
			} else
				screen.render(
						x * 16 + 8,
						y * 16 + 8,
						(r ? 16 : 15) + (d ? 2 : 1) * 32,
						(sd || sr) ? transitionColor2 : transitionColor1,
						0);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}

	public void tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tile.hole) {
			level.setTile(xn, yn, this, 0);
		}
		if (level.getTile(xn, yn) == Tile.lighthole) {
			level.setTile(xn, yn, this, 0);
		}
		if (level.getTile(xn, yn) == Tile.lava) {
			level.setTile(xn, yn, sbrick, 0);
		}
	}
}
