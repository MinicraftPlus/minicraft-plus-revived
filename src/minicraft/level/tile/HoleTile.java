package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.level.Level;

public class HoleTile extends Tile {
	public HoleTile(int id) {
		super(id);
		connectsToSand = true;
		connectsToWater = true;
		connectsToLava = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		/*int col0 = Color.get(222, 222, 220, 220);
		int col00 = Color.get(3, 222, 211, 322);
		int col000 = Color.get(3, 222, 330, 440);
		*/
		int col = Color.get(222, 222, 220, 220);
		int col1 = Color.get(3, 222, 211, DirtTile.dCol(level.depth));
		int col2 = Color.get(3, 222, 440, 550);
		/*
		int col2 = Color.get(111, 111, 110, 110);
		int col22 = Color.get(3, 111, 100, 211);
		int col222 = Color.get(3, 111, 220, 330);

		int col3 = Color.get(0, 0, 10, 10);
		int col33 = Color.get(3, 0, 100, 100);
		int col333 = Color.get(3, 0, 110, 220);

		int col4 = Color.get(111, 111, 110, 110);
		int col44 = Color.get(3, 111, 111, 222);
		int col444 = Color.get(3, 111, 440, 550);

		if (level.dirtColor == 322)
			if (Game.time == 0) {
				
				int col = col0;
				*/int transitionColor1 = col1;
				int transitionColor2 = col2;
				
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
			//}
		/*if (Game.time == 1) {

			int col = col1;
			int transitionColor1 = col11;
			int transitionColor2 = col111;

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
		if (Game.time == 2) {

			int col = col2;
			int transitionColor1 = col22;
			int transitionColor2 = col222;

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
		if (Game.time == 3) {

			int col = col3;
			int transitionColor1 = col33;
			int transitionColor2 = col333;

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

		if (level.dirtColor == 222) {
			int col = col4;
			int transitionColor1 = col44;
			int transitionColor2 = col444;

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
		}*/
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}
}
