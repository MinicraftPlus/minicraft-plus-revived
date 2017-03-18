package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
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

public class WoodWallTile extends Tile {
	public WoodWallTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col0 = Color.get(320, 320, 210, 210);
		int col00 = Color.get(100, 320, 210, 430);

		int col1 = Color.get(430, 430, 320, 320);
		int col11 = Color.get(100, 430, 320, 540);

		int col2 = Color.get(320, 320, 210, 210);
		int col22 = Color.get(100, 320, 210, 430);

		int col3 = Color.get(210, 210, 100, 100);
		int col33 = Color.get(000, 210, 100, 320);

		int col4 = Color.get(430, 430, 320, 320);
		int col44 = Color.get(100, 430, 320, 540);

		if (level.dirtColor == 322)
			if (Game.time == 0) {

				int col = col0;
				int transitionColor = col00;

				boolean u = level.getTile(x, y - 1) != this;
				boolean d = level.getTile(x, y + 1) != this;
				boolean l = level.getTile(x - 1, y) != this;
				boolean r = level.getTile(x + 1, y) != this;

				boolean ul = level.getTile(x - 1, y - 1) != this;
				boolean dl = level.getTile(x - 1, y + 1) != this;
				boolean ur = level.getTile(x + 1, y - 1) != this;
				boolean dr = level.getTile(x + 1, y + 1) != this;

				if (!u && !l) {
					if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 5 + 23 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 0, 7 + 22 * 32, transitionColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

				if (!u && !r) {
					if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 5 + 23 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 0, 8 + 22 * 32, transitionColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

				if (!d && !l) {
					if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 5 + 23 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 8, 7 + 23 * 32, transitionColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
				if (!d && !r) {
					if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 5 + 23 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 8, 8 + 23 * 32, transitionColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
			}
		if (Game.time == 1) {

			int col = col1;
			int transitionColor = col11;

			boolean u = level.getTile(x, y - 1) != this;
			boolean d = level.getTile(x, y + 1) != this;
			boolean l = level.getTile(x - 1, y) != this;
			boolean r = level.getTile(x + 1, y) != this;

			boolean ul = level.getTile(x - 1, y - 1) != this;
			boolean dl = level.getTile(x - 1, y + 1) != this;
			boolean ur = level.getTile(x + 1, y - 1) != this;
			boolean dr = level.getTile(x + 1, y + 1) != this;

			if (!u && !l) {
				if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 0, 7 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!u && !r) {
				if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 0, 8 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!d && !l) {
				if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 8, 7 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
			if (!d && !r) {
				if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 8, 8 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
		}
		if (Game.time == 2) {

			int col = col2;
			int transitionColor = col22;

			boolean u = level.getTile(x, y - 1) != this;
			boolean d = level.getTile(x, y + 1) != this;
			boolean l = level.getTile(x - 1, y) != this;
			boolean r = level.getTile(x + 1, y) != this;

			boolean ul = level.getTile(x - 1, y - 1) != this;
			boolean dl = level.getTile(x - 1, y + 1) != this;
			boolean ur = level.getTile(x + 1, y - 1) != this;
			boolean dr = level.getTile(x + 1, y + 1) != this;

			if (!u && !l) {
				if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 0, 7 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!u && !r) {
				if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 0, 8 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!d && !l) {
				if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 8, 7 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
			if (!d && !r) {
				if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 8, 8 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
		}
		if (Game.time == 3) {

			int col = col3;
			int transitionColor = col33;

			boolean u = level.getTile(x, y - 1) != this;
			boolean d = level.getTile(x, y + 1) != this;
			boolean l = level.getTile(x - 1, y) != this;
			boolean r = level.getTile(x + 1, y) != this;

			boolean ul = level.getTile(x - 1, y - 1) != this;
			boolean dl = level.getTile(x - 1, y + 1) != this;
			boolean ur = level.getTile(x + 1, y - 1) != this;
			boolean dr = level.getTile(x + 1, y + 1) != this;

			if (!u && !l) {
				if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 0, 7 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!u && !r) {
				if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 0, 8 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!d && !l) {
				if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 8, 7 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
			if (!d && !r) {
				if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 8, 8 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
		}

		if (level.dirtColor == 222) {
			int col = col4;
			int transitionColor = col44;

			boolean u = level.getTile(x, y - 1) != this;
			boolean d = level.getTile(x, y + 1) != this;
			boolean l = level.getTile(x - 1, y) != this;
			boolean r = level.getTile(x + 1, y) != this;

			boolean ul = level.getTile(x - 1, y - 1) != this;
			boolean dl = level.getTile(x - 1, y + 1) != this;
			boolean ur = level.getTile(x + 1, y - 1) != this;
			boolean dr = level.getTile(x + 1, y + 1) != this;

			if (!u && !l) {
				if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 0, 7 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!u && !r) {
				if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 0, 8 + 22 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 24 : 23) * 32, transitionColor, 3);

			if (!d && !l) {
				if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 8, 7 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
			if (!d && !r) {
				if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 5 + 23 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 8, 8 + 23 * 32, transitionColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 22 : 23) * 32, transitionColor, 3);
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		hurt(level, x, y, dmg);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.axe) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}
			}
		}
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.hatchet) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(7) + (tool.level) * 5 + 5);
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int wwH;
		if (ModeMenu.creative) wwH = 1;
		else {
			wwH = 5;
		}
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
		if (damage >= wwH) {
			int count = random.nextInt(3) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.plank),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			}
			level.setTile(x, y, Tile.plank, 0);
		} else {
			level.setData(x, y, damage);
		}
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
