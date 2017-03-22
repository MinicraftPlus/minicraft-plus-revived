package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.particle.SmashParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.sound.Sound;
import java.util.Random;

public class LightTile extends Tile {
	private Tile onType;
	private Random wRandom = new Random();
	int til = 0;

	public LightTile(int id, Tile onType, int tile) {
		super(id);
		til = tile;
		if (tile == 0) connectsToGrass = true;
		if (tile == 1) connectsToSand = true;
		if (tile == 2) connectsToGrass = true;
		if (tile == 3) connectsToSand = true;
		if (tile == 4) {
			connectsToSand = true;
			connectsToWater = true;
		}
		if (tile == 5) connectsToGrass = false;
		if (tile == 6) connectsToGrass = true;
		if (tile == 7) ;
		if (tile == 17) {
			connectsToSand = true;
			connectsToWater = true;
			connectsToLava = true;
		}
		if (tile == 25) connectsToGrass = true;
		if (tile == 26) connectsToSand = true;
	}

	//Grass tile
	public static int col0 = Color.get(141, 141, 252, 322);
	public static int col00 = Color.get(141, 141, 252, 322);

	//Sand tile
	public static int col1 = Color.get(552, 550, 440, 440);
	public static int col11 = Color.get(440, 550, 440, 322);

	//Tree tile
	public static int col2 = Color.get(10, 30, 151, 141);
	public static int col22 = Color.get(10, 30, 430, 141);
	public static int col222 = Color.get(10, 30, 320, 141);

	//Cactus tile
	public static int col3 = Color.get(30, 40, 50, 550);

	//Water tile
	int col4 = Color.get(005, 105, 115, 115);
	int col44 = Color.get(3, 105, 211, 322);
	int col444 = Color.get(3, 105, 440, 550);

	//Dirt tile
	int col5 = Color.get(321, 321, 321 - 111, 321 - 111);

	//Flower Tile
	int col6 = Color.get(10, 141, 555, 440);

	//Stairs Tile
	int col7 = Color.get(321, 000, 444, 555);

	//Plank Tile
	int col8 = Color.get(210, 210, 430, 320);

	//St.Brick Tile
	int col9 = Color.get(333, 333, 444, 444);

	//Wood door (open)
	int col10 = Color.get(320, 430, 430, 210);

	//Wood door (closed)
	int col111 = Color.get(320, 430, 210, 430);

	//Stone door (open)
	int col12 = Color.get(444, 333, 333, 222);

	// Stone door (closed)
	int col13 = Color.get(444, 333, 222, 333);

	// Hole Tile
	int col14 = Color.get(222, 222, 220, 220);
	int col114 = Color.get(3, 222, 211, 321);
	int col1114 = Color.get(3, 222, 440, 550);

	// Wool
	int col15 = Color.get(444, 333, 444, 555);

	// Red Wool
	int col16 = Color.get(400, 500, 400, 500);

	// Blue Wool
	int col17 = Color.get(015, 115, 015, 115);

	// Green Wool
	int col18 = Color.get(30, 40, 40, 50);

	// Yellow Wool
	int col19 = Color.get(550, 661, 440, 550);

	// Black Wool
	int col20 = Color.get(111, 111, 000, 111);

	//Tree Sapling
	int col21 = Color.get(20, 40, 50, 141);

	//Cactus Sapling
	int col2222 = Color.get(20, 40, 50, 550);

	public void render(Screen screen, Level level, int x, int y) {
		if (til == 0) {
			int col = col0;
			int transitionColor = col00;

			boolean u = !level.getTile(x, y - 1).connectsToGrass;
			boolean d = !level.getTile(x, y + 1).connectsToGrass;
			boolean l = !level.getTile(x - 1, y).connectsToGrass;
			boolean r = !level.getTile(x + 1, y).connectsToGrass;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
		} else if (til == 1) {
			int col = col1;
			int transitionColor = col11;

			boolean u = !level.getTile(x, y - 1).connectsToSand;
			boolean d = !level.getTile(x, y + 1).connectsToSand;
			boolean l = !level.getTile(x - 1, y).connectsToSand;
			boolean r = !level.getTile(x + 1, y).connectsToSand;

			if (!u && !l) {
				screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 11 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!u && !r) {
				screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 13 : 12) + (u ? 0 : 1) * 32, transitionColor, 0);

			if (!d && !l) {
				screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 11 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
			if (!d && !r) {
				screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);

			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 13 : 12) + (d ? 2 : 1) * 32, transitionColor, 0);
		} else if (til == 2) {
			int col = col2;
			int barkCol1 = col22;
			int barkCol2 = col222;

			boolean u = level.getTile(x, y - 1) == this;
			boolean l = level.getTile(x - 1, y) == this;
			boolean r = level.getTile(x + 1, y) == this;
			boolean d = level.getTile(x, y + 1) == this;
			boolean ul = level.getTile(x - 1, y - 1) == this;
			boolean ur = level.getTile(x + 1, y - 1) == this;
			boolean dl = level.getTile(x - 1, y + 1) == this;
			boolean dr = level.getTile(x + 1, y + 1) == this;

			if (u && ul && l) {
				screen.render(x * 16 + 0, y * 16 + 0, 10 + 1 * 32, col, 0);
			} else {
				screen.render(x * 16 + 0, y * 16 + 0, 9 + 0 * 32, col, 0);
			}
			if (u && ur && r) {
				screen.render(x * 16 + 8, y * 16 + 0, 10 + 2 * 32, barkCol2, 0);
			} else {
				screen.render(x * 16 + 8, y * 16 + 0, 10 + 0 * 32, col, 0);
			}
			if (d && dl && l) {
				screen.render(x * 16 + 0, y * 16 + 8, 10 + 2 * 32, barkCol2, 0);
			} else {
				screen.render(x * 16 + 0, y * 16 + 8, 9 + 1 * 32, barkCol1, 0);
			}
			if (d && dr && r) {
				screen.render(x * 16 + 8, y * 16 + 8, 10 + 1 * 32, col, 0);
			} else {
				screen.render(x * 16 + 8, y * 16 + 8, 10 + 3 * 32, barkCol2, 0);
			}
		} else if (til == 3) {
			int col = col3;
			screen.render(x * 16 + 0, y * 16 + 0, 8 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		} else if (til == 4) {
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
		} else if (til == 5) {
			int col = col5;
			screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
		} else if (til == 6) {
			int flowerCol = col6;

			screen.render(x * 16 + 0, y * 16 + 0, 1 + 1 * 32, flowerCol, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 2, Color.get(141, 141, 252, 322), 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2, Color.get(141, 141, 252, 322), 0);
			screen.render(x * 16 + 8, y * 16 + 8, 1 + 1 * 32, flowerCol, 0);
		} else if (til == 7) {
			int color = col7;
			int xt = 2;

			screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
			screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		} else if (til == 8) {
			int color = col7;
			int xt = 0;

			screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
			screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
			screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		} else if (til == 9) {
			int col = col8;
			screen.render(x * 16 + 0, y * 16 + 0, 19 + 1 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 19 + 1 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 19 + 1 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 19 + 1 * 32, col, 0);
		} else if (til == 10) {
			int col = col9;
			screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
		} else if (til == 11) {
			int col = col10;
			screen.render(x * 16 + 0, y * 16 + 0, 0 + 22 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 1 + 22 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 0 + 23 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 1 + 23 * 32, col, 0);
		} else if (til == 12) {
			int col = col111;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 22 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 3 + 22 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 23 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3 + 23 * 32, col, 0);
		} else if (til == 13) {
			int col = col12;
			screen.render(x * 16 + 0, y * 16 + 0, 0 + 24 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 1 + 24 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 0 + 25 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 1 + 25 * 32, col, 0);
		} else if (til == 14) {
			int col = col13;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 24 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 3 + 24 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 25 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 3 + 25 * 32, col, 0);
		} else if (til == 17) {
			int col = col14;
			int transitionColor1 = col114;
			int transitionColor2 = col1114;

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
		} else if (til == 18) {
			int col = col15;
			screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
		} else if (til == 19) {
			int col = col16;
			screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
		} else if (til == 20) {
			int col = col17;
			screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
		} else if (til == 21) {
			int col = col18;
			screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
		} else if (til == 22) {
			int col = col19;
			screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
		} else if (til == 23) {
			int col = col20;
			screen.render(x * 16 + 0, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 17, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 17, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 17, col, 0);
		} else if (til == 25) {
			int col = col21;
			int col2 = col0;
			screen.render(x * 16 + 0, y * 16 + 0, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
		} else if (til == 26) {
			int col = col2222;
			int col2 = col1;
			screen.render(x * 16 + 0, y * 16 + 0, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 0 + 0 * 32, col2, 0);
			screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		if (til == 4) {
			return e.canSwim();
		}
		if (til == 17) {
			return e.canSwim();
		}

		if (til != 2) {
			if (til != 3) {
				if (til != 4) {
					if (til != 12) {
						if (til != 14) {
							return true;
						}
					}
				}
			}
		} else {
			return false;
		}
		return false;
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (til == 3) {
			if (OptionsMenu.diff == OptionsMenu.easy) {
				entity.hurt(this, x, y, 1);
			}
			if (OptionsMenu.diff == OptionsMenu.norm) {
				entity.hurt(this, x, y, 1);
			}
			if (OptionsMenu.diff == OptionsMenu.hard) {
				entity.hurt(this, x, y, 2);
			}
		}
	}

	public void tick(Level level, int xt, int yt) {
		if (level.getTile(xt, yt + 1) != Tile.torchgrass) {
			if (level.getTile(xt, yt - 1) != Tile.torchgrass) {
				if (level.getTile(xt + 1, yt) != Tile.torchgrass) {
					if (level.getTile(xt - 1, yt) != Tile.torchgrass) {
						if (level.getTile(xt, yt + 2) != Tile.torchgrass) {
							if (level.getTile(xt, yt - 2) != Tile.torchgrass) {
								if (level.getTile(xt + 2, yt) != Tile.torchgrass) {
									if (level.getTile(xt - 2, yt) != Tile.torchgrass) {
										if (level.getTile(xt + 1, yt + 1) != Tile.torchgrass) {
											if (level.getTile(xt - 1, yt + 1) != Tile.torchgrass) {
												if (level.getTile(xt - 1, yt - 1) != Tile.torchgrass) {
													if (level.getTile(xt + 1, yt - 1) != Tile.torchgrass) {
														if (level.getTile(xt, yt + 1) != Tile.torchsand) {
															if (level.getTile(xt, yt - 1) != Tile.torchsand) {
																if (level.getTile(xt + 1, yt) != Tile.torchsand) {
																	if (level.getTile(xt - 1, yt) != Tile.torchsand) {
																		if (level.getTile(xt, yt + 2) != Tile.torchsand) {
																			if (level.getTile(xt, yt - 2) != Tile.torchsand) {
																				if (level.getTile(xt + 2, yt) != Tile.torchsand) {
																					if (level.getTile(xt - 2, yt) != Tile.torchsand) {
																						if (level.getTile(xt + 1, yt + 1) != Tile.torchsand) {
																							if (level.getTile(xt - 1, yt + 1) != Tile.torchsand) {
																								if (level.getTile(xt - 1, yt - 1)
																										!= Tile.torchsand) {
																									if (level.getTile(xt + 1, yt - 1)
																											!= Tile.torchsand) {
																										if (level.getTile(xt, yt + 1)
																												!= Tile.torchdirt) {
																											if (level.getTile(xt, yt - 1)
																													!= Tile.torchdirt) {
																												if (level.getTile(xt + 1, yt)
																														!= Tile.torchdirt) {
																													if (level.getTile(xt - 1, yt)
																															!= Tile.torchdirt) {
																														if (level.getTile(xt, yt + 2)
																																!= Tile.torchdirt) {
																															if (level.getTile(xt, yt - 2)
																																	!= Tile.torchdirt) {
																																if (level.getTile(xt + 2, yt)
																																		!= Tile.torchdirt) {
																																	if (level.getTile(xt - 2, yt)
																																			!= Tile.torchdirt) {
																																		if (level.getTile(
																																						xt + 1, yt + 1)
																																				!= Tile.torchdirt) {
																																			if (level.getTile(
																																							xt - 1, yt + 1)
																																					!= Tile.torchdirt) {
																																				if (level.getTile(
																																								xt - 1, yt - 1)
																																						!= Tile.torchdirt) {
																																					if (level.getTile(
																																									xt + 1, yt - 1)
																																							!= Tile.torchdirt) {
																																						if (level.getTile(
																																										xt, yt + 1)
																																								!= Tile
																																										.torchplank) {
																																							if (level.getTile(
																																											xt, yt - 1)
																																									!= Tile
																																											.torchplank) {
																																								if (level.getTile(
																																												xt + 1, yt)
																																										!= Tile
																																												.torchplank) {
																																									if (level.getTile(
																																													xt - 1,
																																													yt)
																																											!= Tile
																																													.torchplank) {
																																										if (level
																																														.getTile(
																																																xt,
																																																yt
																																																		+ 2)
																																												!= Tile
																																														.torchplank) {
																																											if (level
																																															.getTile(
																																																	xt,
																																																	yt
																																																			- 2)
																																													!= Tile
																																															.torchplank) {
																																												if (level
																																																.getTile(
																																																		xt
																																																				+ 2,
																																																		yt)
																																														!= Tile
																																																.torchplank) {
																																													if (level
																																																	.getTile(
																																																			xt
																																																					- 2,
																																																			yt)
																																															!= Tile
																																																	.torchplank) {
																																														if (level
																																																		.getTile(
																																																				xt
																																																						+ 1,
																																																				yt
																																																						+ 1)
																																																!= Tile
																																																		.torchplank) {
																																															if (level
																																																			.getTile(
																																																					xt
																																																							- 1,
																																																					yt
																																																							+ 1)
																																																	!= Tile
																																																			.torchplank) {
																																																if (level
																																																				.getTile(
																																																						xt
																																																								- 1,
																																																						yt
																																																								- 1)
																																																		!= Tile
																																																				.torchplank) {
																																																	if (level
																																																					.getTile(
																																																							xt
																																																									+ 1,
																																																							yt
																																																									- 1)
																																																			!= Tile
																																																					.torchplank) {
																																																		if (level
																																																						.getTile(
																																																								xt,
																																																								yt
																																																										+ 1)
																																																				!= Tile
																																																						.torchsbrick) {
																																																			if (level
																																																							.getTile(
																																																									xt,
																																																									yt
																																																											- 1)
																																																					!= Tile
																																																							.torchsbrick) {
																																																				if (level
																																																								.getTile(
																																																										xt
																																																												+ 1,
																																																										yt)
																																																						!= Tile
																																																								.torchsbrick) {
																																																					if (level
																																																									.getTile(
																																																											xt
																																																													- 1,
																																																											yt)
																																																							!= Tile
																																																									.torchsbrick) {
																																																						if (level
																																																										.getTile(
																																																												xt,
																																																												yt
																																																														+ 2)
																																																								!= Tile
																																																										.torchsbrick) {
																																																							if (level
																																																											.getTile(
																																																													xt,
																																																													yt
																																																															- 2)
																																																									!= Tile
																																																											.torchsbrick) {
																																																								if (level
																																																												.getTile(
																																																														xt
																																																																+ 2,
																																																														yt)
																																																										!= Tile
																																																												.torchsbrick) {
																																																									if (level
																																																													.getTile(
																																																															xt
																																																																	- 2,
																																																															yt)
																																																											!= Tile
																																																													.torchsbrick) {
																																																										if (level
																																																														.getTile(
																																																																xt
																																																																		+ 1,
																																																																yt
																																																																		+ 1)
																																																												!= Tile
																																																														.torchsbrick) {
																																																											if (level
																																																															.getTile(
																																																																	xt
																																																																			- 1,
																																																																	yt
																																																																			+ 1)
																																																													!= Tile
																																																															.torchsbrick) {
																																																												if (level
																																																																.getTile(
																																																																		xt
																																																																				- 1,
																																																																		yt
																																																																				- 1)
																																																														!= Tile
																																																																.torchsbrick) {
																																																													if (level
																																																																	.getTile(
																																																																			xt
																																																																					+ 1,
																																																																			yt
																																																																					- 1)
																																																															!= Tile
																																																																	.torchsbrick) {
																																																														if (level
																																																																		.getTile(
																																																																				xt,
																																																																				yt
																																																																						+ 1)
																																																																!= Tile
																																																																		.torchlo) {
																																																															if (level
																																																																			.getTile(
																																																																					xt,
																																																																					yt
																																																																							- 1)
																																																																	!= Tile
																																																																			.torchlo) {
																																																																if (level
																																																																				.getTile(
																																																																						xt
																																																																								+ 1,
																																																																						yt)
																																																																		!= Tile
																																																																				.torchlo) {
																																																																	if (level
																																																																					.getTile(
																																																																							xt
																																																																									- 1,
																																																																							yt)
																																																																			!= Tile
																																																																					.torchlo) {
																																																																		if (level
																																																																						.getTile(
																																																																								xt,
																																																																								yt
																																																																										+ 2)
																																																																				!= Tile
																																																																						.torchlo) {
																																																																			if (level
																																																																							.getTile(
																																																																									xt,
																																																																									yt
																																																																											- 2)
																																																																					!= Tile
																																																																							.torchlo) {
																																																																				if (level
																																																																								.getTile(
																																																																										xt
																																																																												+ 2,
																																																																										yt)
																																																																						!= Tile
																																																																								.torchlo) {
																																																																					if (level
																																																																									.getTile(
																																																																											xt
																																																																													- 2,
																																																																											yt)
																																																																							!= Tile
																																																																									.torchlo) {
																																																																						if (level
																																																																										.getTile(
																																																																												xt
																																																																														+ 1,
																																																																												yt
																																																																														+ 1)
																																																																								!= Tile
																																																																										.torchlo) {
																																																																							if (level
																																																																											.getTile(
																																																																													xt
																																																																															- 1,
																																																																													yt
																																																																															+ 1)
																																																																									!= Tile
																																																																											.torchlo) {
																																																																								if (level
																																																																												.getTile(
																																																																														xt
																																																																																- 1,
																																																																														yt
																																																																																- 1)
																																																																										!= Tile
																																																																												.torchlo) {
																																																																									if (level
																																																																													.getTile(
																																																																															xt
																																																																																	+ 1,
																																																																															yt
																																																																																	- 1)
																																																																											!= Tile
																																																																													.torchlo) {
																																																																										if (level
																																																																														.getTile(
																																																																																xt,
																																																																																yt
																																																																																		+ 1)
																																																																												!= Tile
																																																																														.torchwool) {
																																																																											if (level
																																																																															.getTile(
																																																																																	xt,
																																																																																	yt
																																																																																			- 1)
																																																																													!= Tile
																																																																															.torchwool) {
																																																																												if (level
																																																																																.getTile(
																																																																																		xt
																																																																																				+ 1,
																																																																																		yt)
																																																																														!= Tile
																																																																																.torchwool) {
																																																																													if (level
																																																																																	.getTile(
																																																																																			xt
																																																																																					- 1,
																																																																																			yt)
																																																																															!= Tile
																																																																																	.torchwool) {
																																																																														if (level
																																																																																		.getTile(
																																																																																				xt,
																																																																																				yt
																																																																																						+ 2)
																																																																																!= Tile
																																																																																		.torchwool) {
																																																																															if (level
																																																																																			.getTile(
																																																																																					xt,
																																																																																					yt
																																																																																							- 2)
																																																																																	!= Tile
																																																																																			.torchwool) {
																																																																																if (level
																																																																																				.getTile(
																																																																																						xt
																																																																																								+ 2,
																																																																																						yt)
																																																																																		!= Tile
																																																																																				.torchwool) {
																																																																																	if (level
																																																																																					.getTile(
																																																																																							xt
																																																																																									- 2,
																																																																																							yt)
																																																																																			!= Tile
																																																																																					.torchwool) {
																																																																																		if (level
																																																																																						.getTile(
																																																																																								xt
																																																																																										+ 1,
																																																																																								yt
																																																																																										+ 1)
																																																																																				!= Tile
																																																																																						.torchwool) {
																																																																																			if (level
																																																																																							.getTile(
																																																																																									xt
																																																																																											- 1,
																																																																																									yt
																																																																																											+ 1)
																																																																																					!= Tile
																																																																																							.torchwool) {
																																																																																				if (level
																																																																																								.getTile(
																																																																																										xt
																																																																																												- 1,
																																																																																										yt
																																																																																												- 1)
																																																																																						!= Tile
																																																																																								.torchwool) {
																																																																																					if (level
																																																																																									.getTile(
																																																																																											xt
																																																																																													+ 1,
																																																																																											yt
																																																																																													- 1)
																																																																																							!= Tile
																																																																																									.torchwool) {
																																																																																						if (level
																																																																																										.getTile(
																																																																																												xt,
																																																																																												yt
																																																																																														+ 1)
																																																																																								!= Tile
																																																																																										.torchwoolred) {
																																																																																							if (level
																																																																																											.getTile(
																																																																																													xt,
																																																																																													yt
																																																																																															- 1)
																																																																																									!= Tile
																																																																																											.torchwoolred) {
																																																																																								if (level
																																																																																												.getTile(
																																																																																														xt
																																																																																																+ 1,
																																																																																														yt)
																																																																																										!= Tile
																																																																																												.torchwoolred) {
																																																																																									if (level
																																																																																													.getTile(
																																																																																															xt
																																																																																																	- 1,
																																																																																															yt)
																																																																																											!= Tile
																																																																																													.torchwoolred) {
																																																																																										if (level
																																																																																														.getTile(
																																																																																																xt,
																																																																																																yt
																																																																																																		+ 2)
																																																																																												!= Tile
																																																																																														.torchwoolred) {
																																																																																											if (level
																																																																																															.getTile(
																																																																																																	xt,
																																																																																																	yt
																																																																																																			- 2)
																																																																																													!= Tile
																																																																																															.torchwoolred) {
																																																																																												if (level
																																																																																																.getTile(
																																																																																																		xt
																																																																																																				+ 2,
																																																																																																		yt)
																																																																																														!= Tile
																																																																																																.torchwoolred) {
																																																																																													if (level
																																																																																																	.getTile(
																																																																																																			xt
																																																																																																					- 2,
																																																																																																			yt)
																																																																																															!= Tile
																																																																																																	.torchwoolred) {
																																																																																														if (level
																																																																																																		.getTile(
																																																																																																				xt
																																																																																																						+ 1,
																																																																																																				yt
																																																																																																						+ 1)
																																																																																																!= Tile
																																																																																																		.torchwoolred) {
																																																																																															if (level
																																																																																																			.getTile(
																																																																																																					xt
																																																																																																							- 1,
																																																																																																					yt
																																																																																																							+ 1)
																																																																																																	!= Tile
																																																																																																			.torchwoolred) {
																																																																																																if (level
																																																																																																				.getTile(
																																																																																																						xt
																																																																																																								- 1,
																																																																																																						yt
																																																																																																								- 1)
																																																																																																		!= Tile
																																																																																																				.torchwoolred) {
																																																																																																	if (level
																																																																																																					.getTile(
																																																																																																							xt
																																																																																																									+ 1,
																																																																																																							yt
																																																																																																									- 1)
																																																																																																			!= Tile
																																																																																																					.torchwoolred) {
																																																																																																		if (level
																																																																																																						.getTile(
																																																																																																								xt,
																																																																																																								yt
																																																																																																										+ 1)
																																																																																																				!= Tile
																																																																																																						.torchwoolblue) {
																																																																																																			if (level
																																																																																																							.getTile(
																																																																																																									xt,
																																																																																																									yt
																																																																																																											- 1)
																																																																																																					!= Tile
																																																																																																							.torchwoolblue) {
																																																																																																				if (level
																																																																																																								.getTile(
																																																																																																										xt
																																																																																																												+ 1,
																																																																																																										yt)
																																																																																																						!= Tile
																																																																																																								.torchwoolblue) {
																																																																																																					if (level
																																																																																																									.getTile(
																																																																																																											xt
																																																																																																													- 1,
																																																																																																											yt)
																																																																																																							!= Tile
																																																																																																									.torchwoolblue) {
																																																																																																						if (level
																																																																																																										.getTile(
																																																																																																												xt,
																																																																																																												yt
																																																																																																														+ 2)
																																																																																																								!= Tile
																																																																																																										.torchwoolblue) {
																																																																																																							if (level
																																																																																																											.getTile(
																																																																																																													xt,
																																																																																																													yt
																																																																																																															- 2)
																																																																																																									!= Tile
																																																																																																											.torchwoolblue) {
																																																																																																								if (level
																																																																																																												.getTile(
																																																																																																														xt
																																																																																																																+ 2,
																																																																																																														yt)
																																																																																																										!= Tile
																																																																																																												.torchwoolblue) {
																																																																																																									if (level
																																																																																																													.getTile(
																																																																																																															xt
																																																																																																																	- 2,
																																																																																																															yt)
																																																																																																											!= Tile
																																																																																																													.torchwoolblue) {
																																																																																																										if (level
																																																																																																														.getTile(
																																																																																																																xt
																																																																																																																		+ 1,
																																																																																																																yt
																																																																																																																		+ 1)
																																																																																																												!= Tile
																																																																																																														.torchwoolblue) {
																																																																																																											if (level
																																																																																																															.getTile(
																																																																																																																	xt
																																																																																																																			- 1,
																																																																																																																	yt
																																																																																																																			+ 1)
																																																																																																													!= Tile
																																																																																																															.torchwoolblue) {
																																																																																																												if (level
																																																																																																																.getTile(
																																																																																																																		xt
																																																																																																																				- 1,
																																																																																																																		yt
																																																																																																																				- 1)
																																																																																																														!= Tile
																																																																																																																.torchwoolblue) {
																																																																																																													if (level
																																																																																																																	.getTile(
																																																																																																																			xt
																																																																																																																					+ 1,
																																																																																																																			yt
																																																																																																																					- 1)
																																																																																																															!= Tile
																																																																																																																	.torchwoolblue) {
																																																																																																														if (level
																																																																																																																		.getTile(
																																																																																																																				xt,
																																																																																																																				yt
																																																																																																																						+ 1)
																																																																																																																!= Tile
																																																																																																																		.torchwoolgreen) {
																																																																																																															if (level
																																																																																																																			.getTile(
																																																																																																																					xt,
																																																																																																																					yt
																																																																																																																							- 1)
																																																																																																																	!= Tile
																																																																																																																			.torchwoolgreen) {
																																																																																																																if (level
																																																																																																																				.getTile(
																																																																																																																						xt
																																																																																																																								+ 1,
																																																																																																																						yt)
																																																																																																																		!= Tile
																																																																																																																				.torchwoolgreen) {
																																																																																																																	if (level
																																																																																																																					.getTile(
																																																																																																																							xt
																																																																																																																									- 1,
																																																																																																																							yt)
																																																																																																																			!= Tile
																																																																																																																					.torchwoolgreen) {
																																																																																																																		if (level
																																																																																																																						.getTile(
																																																																																																																								xt,
																																																																																																																								yt
																																																																																																																										+ 2)
																																																																																																																				!= Tile
																																																																																																																						.torchwoolgreen) {
																																																																																																																			if (level
																																																																																																																							.getTile(
																																																																																																																									xt,
																																																																																																																									yt
																																																																																																																											- 2)
																																																																																																																					!= Tile
																																																																																																																							.torchwoolgreen) {
																																																																																																																				if (level
																																																																																																																								.getTile(
																																																																																																																										xt
																																																																																																																												+ 2,
																																																																																																																										yt)
																																																																																																																						!= Tile
																																																																																																																								.torchwoolgreen) {
																																																																																																																					if (level
																																																																																																																									.getTile(
																																																																																																																											xt
																																																																																																																													- 2,
																																																																																																																											yt)
																																																																																																																							!= Tile
																																																																																																																									.torchwoolgreen) {
																																																																																																																						if (level
																																																																																																																										.getTile(
																																																																																																																												xt
																																																																																																																														+ 1,
																																																																																																																												yt
																																																																																																																														+ 1)
																																																																																																																								!= Tile
																																																																																																																										.torchwoolgreen) {
																																																																																																																							if (level
																																																																																																																											.getTile(
																																																																																																																													xt
																																																																																																																															- 1,
																																																																																																																													yt
																																																																																																																															+ 1)
																																																																																																																									!= Tile
																																																																																																																											.torchwoolgreen) {
																																																																																																																								if (level
																																																																																																																												.getTile(
																																																																																																																														xt
																																																																																																																																- 1,
																																																																																																																														yt
																																																																																																																																- 1)
																																																																																																																										!= Tile
																																																																																																																												.torchwoolgreen) {
																																																																																																																									if (level
																																																																																																																													.getTile(
																																																																																																																															xt
																																																																																																																																	+ 1,
																																																																																																																															yt
																																																																																																																																	- 1)
																																																																																																																											!= Tile
																																																																																																																													.torchwoolgreen) {
																																																																																																																										if (level
																																																																																																																														.getTile(
																																																																																																																																xt,
																																																																																																																																yt
																																																																																																																																		+ 1)
																																																																																																																												!= Tile
																																																																																																																														.torchwoolyellow) {
																																																																																																																											if (level
																																																																																																																															.getTile(
																																																																																																																																	xt,
																																																																																																																																	yt
																																																																																																																																			- 1)
																																																																																																																													!= Tile
																																																																																																																															.torchwoolyellow) {
																																																																																																																												if (level
																																																																																																																																.getTile(
																																																																																																																																		xt
																																																																																																																																				+ 1,
																																																																																																																																		yt)
																																																																																																																														!= Tile
																																																																																																																																.torchwoolyellow) {
																																																																																																																													if (level
																																																																																																																																	.getTile(
																																																																																																																																			xt
																																																																																																																																					- 1,
																																																																																																																																			yt)
																																																																																																																															!= Tile
																																																																																																																																	.torchwoolyellow) {
																																																																																																																														if (level
																																																																																																																																		.getTile(
																																																																																																																																				xt,
																																																																																																																																				yt
																																																																																																																																						+ 2)
																																																																																																																																!= Tile
																																																																																																																																		.torchwoolyellow) {
																																																																																																																															if (level
																																																																																																																																			.getTile(
																																																																																																																																					xt,
																																																																																																																																					yt
																																																																																																																																							- 2)
																																																																																																																																	!= Tile
																																																																																																																																			.torchwoolyellow) {
																																																																																																																																if (level
																																																																																																																																				.getTile(
																																																																																																																																						xt
																																																																																																																																								+ 2,
																																																																																																																																						yt)
																																																																																																																																		!= Tile
																																																																																																																																				.torchwoolyellow) {
																																																																																																																																	if (level
																																																																																																																																					.getTile(
																																																																																																																																							xt
																																																																																																																																									- 2,
																																																																																																																																							yt)
																																																																																																																																			!= Tile
																																																																																																																																					.torchwoolyellow) {
																																																																																																																																		if (level
																																																																																																																																						.getTile(
																																																																																																																																								xt
																																																																																																																																										+ 1,
																																																																																																																																								yt
																																																																																																																																										+ 1)
																																																																																																																																				!= Tile
																																																																																																																																						.torchwoolyellow) {
																																																																																																																																			if (level
																																																																																																																																							.getTile(
																																																																																																																																									xt
																																																																																																																																											- 1,
																																																																																																																																									yt
																																																																																																																																											+ 1)
																																																																																																																																					!= Tile
																																																																																																																																							.torchwoolyellow) {
																																																																																																																																				if (level
																																																																																																																																								.getTile(
																																																																																																																																										xt
																																																																																																																																												- 1,
																																																																																																																																										yt
																																																																																																																																												- 1)
																																																																																																																																						!= Tile
																																																																																																																																								.torchwoolyellow) {
																																																																																																																																					if (level
																																																																																																																																									.getTile(
																																																																																																																																											xt
																																																																																																																																													+ 1,
																																																																																																																																											yt
																																																																																																																																													- 1)
																																																																																																																																							!= Tile
																																																																																																																																									.torchwoolyellow) {
																																																																																																																																						if (level
																																																																																																																																										.getTile(
																																																																																																																																												xt,
																																																																																																																																												yt
																																																																																																																																														+ 1)
																																																																																																																																								!= Tile
																																																																																																																																										.torchwoolblack) {
																																																																																																																																							if (level
																																																																																																																																											.getTile(
																																																																																																																																													xt,
																																																																																																																																													yt
																																																																																																																																															- 1)
																																																																																																																																									!= Tile
																																																																																																																																											.torchwoolblack) {
																																																																																																																																								if (level
																																																																																																																																												.getTile(
																																																																																																																																														xt
																																																																																																																																																+ 1,
																																																																																																																																														yt)
																																																																																																																																										!= Tile
																																																																																																																																												.torchwoolblack) {
																																																																																																																																									if (level
																																																																																																																																													.getTile(
																																																																																																																																															xt
																																																																																																																																																	- 1,
																																																																																																																																															yt)
																																																																																																																																											!= Tile
																																																																																																																																													.torchwoolblack) {
																																																																																																																																										if (level
																																																																																																																																														.getTile(
																																																																																																																																																xt,
																																																																																																																																																yt
																																																																																																																																																		+ 2)
																																																																																																																																												!= Tile
																																																																																																																																														.torchwoolblack) {
																																																																																																																																											if (level
																																																																																																																																															.getTile(
																																																																																																																																																	xt,
																																																																																																																																																	yt
																																																																																																																																																			- 2)
																																																																																																																																													!= Tile
																																																																																																																																															.torchwoolblack) {
																																																																																																																																												if (level
																																																																																																																																																.getTile(
																																																																																																																																																		xt
																																																																																																																																																				+ 2,
																																																																																																																																																		yt)
																																																																																																																																														!= Tile
																																																																																																																																																.torchwoolblack) {
																																																																																																																																													if (level
																																																																																																																																																	.getTile(
																																																																																																																																																			xt
																																																																																																																																																					- 2,
																																																																																																																																																			yt)
																																																																																																																																															!= Tile
																																																																																																																																																	.torchwoolblack) {
																																																																																																																																														if (level
																																																																																																																																																		.getTile(
																																																																																																																																																				xt
																																																																																																																																																						+ 1,
																																																																																																																																																				yt
																																																																																																																																																						+ 1)
																																																																																																																																																!= Tile
																																																																																																																																																		.torchwoolblack) {
																																																																																																																																															if (level
																																																																																																																																																			.getTile(
																																																																																																																																																					xt
																																																																																																																																																							- 1,
																																																																																																																																																					yt
																																																																																																																																																							+ 1)
																																																																																																																																																	!= Tile
																																																																																																																																																			.torchwoolblack) {
																																																																																																																																																if (level
																																																																																																																																																				.getTile(
																																																																																																																																																						xt
																																																																																																																																																								- 1,
																																																																																																																																																						yt
																																																																																																																																																								- 1)
																																																																																																																																																		!= Tile
																																																																																																																																																				.torchwoolblack) {
																																																																																																																																																	if (level
																																																																																																																																																					.getTile(
																																																																																																																																																							xt
																																																																																																																																																									+ 1,
																																																																																																																																																							yt
																																																																																																																																																									- 1)
																																																																																																																																																			!= Tile
																																																																																																																																																					.torchwoolblack) {
																																																																																																																																																		if (til
																																																																																																																																																				== 0) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.grass,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 1) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.sand,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 2) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.tree,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 3) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.cactus,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 4) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.water,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 5) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.dirt,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 6) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.flower,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 7) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.stairsUp,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 8) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.stairsDown,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 9) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.plank,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 10) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.sbrick,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 11) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.wdo,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 12) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.wdc,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 13) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.sdo,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 14) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.sdc,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 15) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.odo,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 16) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.odc,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 17) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.hole,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 18) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.wool,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 19) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.redwool,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 20) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.bluewool,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 21) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.greenwool,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 22) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.yellowwool,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 23) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.blackwool,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 24) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.o,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 25) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.treeSapling,
																																																																																																																																																							0);
																																																																																																																																																		} else if (til
																																																																																																																																																				== 26) {
																																																																																																																																																			level
																																																																																																																																																					.setTile(
																																																																																																																																																							xt,
																																																																																																																																																							yt,
																																																																																																																																																							Tile
																																																																																																																																																									.cactusSapling,
																																																																																																																																																							0);
																																																																																																																																																		}
																																																																																																																																																	}
																																																																																																																																																}
																																																																																																																																															}
																																																																																																																																														}
																																																																																																																																													}
																																																																																																																																												}
																																																																																																																																											}
																																																																																																																																										}
																																																																																																																																									}
																																																																																																																																								}
																																																																																																																																							}
																																																																																																																																						}
																																																																																																																																					}
																																																																																																																																				}
																																																																																																																																			}
																																																																																																																																		}
																																																																																																																																	}
																																																																																																																																}
																																																																																																																															}
																																																																																																																														}
																																																																																																																													}
																																																																																																																												}
																																																																																																																											}
																																																																																																																										}
																																																																																																																									}
																																																																																																																								}
																																																																																																																							}
																																																																																																																						}
																																																																																																																					}
																																																																																																																				}
																																																																																																																			}
																																																																																																																		}
																																																																																																																	}
																																																																																																																}
																																																																																																															}
																																																																																																														}
																																																																																																													}
																																																																																																												}
																																																																																																											}
																																																																																																										}
																																																																																																									}
																																																																																																								}
																																																																																																							}
																																																																																																						}
																																																																																																					}
																																																																																																				}
																																																																																																			}
																																																																																																		}
																																																																																																	}
																																																																																																}
																																																																																															}
																																																																																														}
																																																																																													}
																																																																																												}
																																																																																											}
																																																																																										}
																																																																																									}
																																																																																								}
																																																																																							}
																																																																																						}
																																																																																					}
																																																																																				}
																																																																																			}
																																																																																		}
																																																																																	}
																																																																																}
																																																																															}
																																																																														}
																																																																													}
																																																																												}
																																																																											}
																																																																										}
																																																																									}
																																																																								}
																																																																							}
																																																																						}
																																																																					}
																																																																				}
																																																																			}
																																																																		}
																																																																	}
																																																																}
																																																															}
																																																														}
																																																													}
																																																												}
																																																											}
																																																										}
																																																									}
																																																								}
																																																							}
																																																						}
																																																					}
																																																				}
																																																			}
																																																		}
																																																	}
																																																}
																																															}
																																														}
																																													}
																																												}
																																											}
																																										}
																																									}
																																								}
																																							}
																																						}
																																					}
																																				}
																																			}
																																		}
																																	}
																																}
																															}
																														}
																													}
																												}
																											}
																										}
																									}
																								}
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (til == 0) {
			if (random.nextInt(40) != 0) return;

			int xn = xt;
			int yn = yt;

			if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
			else yn += random.nextInt(2) * 2 - 1;

			if (level.getTile(xn, yn) == Tile.dirt) {
				level.setTile(xn, yn, this, 0);
			}
			if (level.getTile(xn, yn) == Tile.lightdirt) {
				level.setTile(xn, yn, this, 0);
			}
		}
		if (til == 4) {
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
		}
		if (til == 25) {
			int age = level.getData(xt, yt) + 1;
			if (age > 100) {
				level.setTile(xt, yt, Tile.lighttree, 0);
			} else {
				level.setData(xt, yt, age);
			}
		}
		if (til == 26) {
			int age = level.getData(xt, yt) + 1;
			if (age > 100) {
				level.setTile(xt, yt, Tile.lightcac, 0);
			} else {
				level.setData(xt, yt, age);
			}
		}
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		if (til == 3) {
			int damage = level.getData(x, y) + dmg;
			int cHealth;
			if (ModeMenu.creative) cHealth = 1;
			else cHealth = 10;
			level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
			level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));

			if (damage >= cHealth) {
				int count = random.nextInt(2) + 2;
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.cactusFlower),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
				level.setTile(x, y, Tile.sand, 0);
			} else {
				level.setData(x, y, damage);
			}
		}
		if (til == 2) {
			{
				int count = random.nextInt(100) == 0 ? 1 : 0;
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.apple),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
			}
			int damage = level.getData(x, y) + dmg;
			int treeHealth;
			if (ModeMenu.creative) treeHealth = 1;
			else {
				treeHealth = 20;
			}
			level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
			level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
			if (damage >= treeHealth) {
				int count = random.nextInt(2) + 1;
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.wood),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
				count = random.nextInt(random.nextInt(4) + 1);
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.acorn),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
				level.setTile(x, y, Tile.grass, 0);
			} else {
				level.setData(x, y, damage);
			}
		}
		if (til == 6) {
			int count = random.nextInt(2) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.flower),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			}
			count = random.nextInt(2);
			for (int i = 0; i < count; i++)
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.rose),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			{
			}
			level.setTile(x, y, Tile.grass, 0);
		}
		if (til == 11) {
			level.setTile(x, y, Tile.lwdc, 0);
		}
		if (til == 12) {
			level.setTile(x, y, Tile.lwdo, 0);
		}
		if (til == 13) {
			level.setTile(x, y, Tile.lsdc, 0);
		}
		if (til == 14) {
			level.setTile(x, y, Tile.lsdo, 0);
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (til == 0) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.dirt, 0);
						Sound.monsterHurt.play();
						if (random.nextInt(5) == 0) {
							level.add(
									new ItemEntity(
											new ResourceItem(Resource.seeds),
											xt * 16 + random.nextInt(10) + 3,
											yt * 16 + random.nextInt(10) + 3));
							level.add(
									new ItemEntity(
											new ResourceItem(Resource.seeds),
											xt * 16 + random.nextInt(10) + 3,
											yt * 16 + random.nextInt(10) + 3));
							return true;
						}
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.dirt, 0);
						Sound.monsterHurt.play();
						if (random.nextInt(5) == 0) {
							return true;
						}
					}
				}
				if (tool.type == ToolType.hoe) {
					if (player.payStamina(4 - tool.level)) {
						Sound.monsterHurt.play();
						if (random.nextInt(5) == 0) {
							level.add(
									new ItemEntity(
											new ResourceItem(Resource.seeds),
											xt * 16 + random.nextInt(10) + 3,
											yt * 16 + random.nextInt(10) + 3));
							return true;
						}
						level.setTile(xt, yt, Tile.farmland, 0);
						return true;
					}
				}
			}
		}
		if (til == 1) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.dirt, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.sand),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(5 - tool.level)) {
						level.setTile(xt, yt, Tile.dirt, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.sand),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						return true;
					}
				}
			}
		}
		if (til == 2) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.axe) {
					if (player.payStamina(4 - tool.level)) {
						hurt(level, xt, yt, player, random.nextInt(10) + (tool.level) * 5 + 10, attackDir);
						return true;
					}
				}
				if (tool.type == ToolType.hatchet) {
					if (player.payStamina(3 - tool.level)) {
						hurt(level, xt, yt, player, random.nextInt(7) + (tool.level) * 5 + 5, attackDir);
						return true;
					}
				}
			}
		}
		if (til == 5) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.dirt),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(5 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.dirt),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.hoe) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.farmland, 0);
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 6) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(2 - tool.level)) {
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.flower),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.rose),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						level.setTile(xt, yt, Tile.grass, 0);
						return true;
					}
				}
			}
		}
		if (til == 9) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.axe) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.plank),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.hatchet) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.plank),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 11) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.axe) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.plank, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.wdoor),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.hatchet) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.plank, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.wdoor),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 12) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.axe) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.plank, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.wdoor),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.hatchet) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.plank, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.wdoor),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 13) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				int hd = 3;
				if (tool.type == ToolType.pickaxe) {
					if (player.payStamina(4 - tool.level)) {
						if (hd == 0) {
							level.setTile(xt, yt, Tile.sbrick, 0);
							level.add(
									new ItemEntity(
											new ResourceItem(Resource.sdoor),
											xt * 16 + random.nextInt(10) + 3,
											yt * 16 + random.nextInt(10) + 3));
							Sound.monsterHurt.play();
							return true;
						}
						if (hd != 0) {
							hd--;
						}
					}
				}
				if (tool.type == ToolType.pick) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.sbrick, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.sdoor),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 14) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				int hd = 3;
				if (tool.type == ToolType.pickaxe) {
					if (player.payStamina(4 - tool.level)) {
						if (hd == 0) {
							level.setTile(xt, yt, Tile.sbrick, 0);
							level.add(
									new ItemEntity(
											new ResourceItem(Resource.sdoor),
											xt * 16 + random.nextInt(10) + 3,
											yt * 16 + random.nextInt(10) + 3));
							Sound.monsterHurt.play();
							return true;
						}
						if (hd != 0) {
							hd--;
						}
					}
				}
				if (tool.type == ToolType.pick) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.sbrick, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.sdoor),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 18) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(3 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.wool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.wool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 19) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(3 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.redwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.redwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 20) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(3 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.bluewool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.bluewool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 21) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(3 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.greenwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.greenwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 22) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(3 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.yellowwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.yellowwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}
		if (til == 23) {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.shovel) {
					if (player.payStamina(3 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.blackwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
				if (tool.type == ToolType.spade) {
					if (player.payStamina(4 - tool.level)) {
						level.setTile(xt, yt, Tile.hole, 0);
						level.add(
								new ItemEntity(
										new ResourceItem(Resource.blackwool),
										xt * 16 + random.nextInt(10) + 3,
										yt * 16 + random.nextInt(10) + 3));
						Sound.monsterHurt.play();
						return true;
					}
				}
			}
		}

		return false;
	}
}
