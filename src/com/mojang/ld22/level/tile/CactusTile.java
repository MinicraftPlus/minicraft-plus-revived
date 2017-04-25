package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.particle.SmashParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class CactusTile extends Tile {
	//Sprite sprite;
	
	public CactusTile(int id) {
		super(id);
		//sprite = new Sprite(8, 2, 2, 2, Color.get(20, 30, 40, 440), )
		connectsToSand = true;
	}

	public static int col0 = Color.get(20, 30, 40, 440);

	public static int col1 = Color.get(30, 40, 50, 550);

	public static int col2 = Color.get(20, 30, 40, 330);

	public static int col3 = Color.get(10, 20, 30, 220);

	public void render(Screen screen, Level level, int x, int y) {
		
		//sprite.render(screen, x * 16, y * 16);
		
		if (Game.time == 0) {
			int col = col0;
			screen.render(x * 16 + 0, y * 16 + 0, 8 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		}
		if (Game.time == 1) {
			int col = col1;
			screen.render(x * 16 + 0, y * 16 + 0, 8 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		}
		if (Game.time == 2) {
			int col = col2;
			screen.render(x * 16 + 0, y * 16 + 0, 8 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		}
		if (Game.time == 3) {
			int col = col3;
			screen.render(x * 16 + 0, y * 16 + 0, 8 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
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

	public void bumpedInto(Level level, int x, int y, Entity entity) {
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

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
