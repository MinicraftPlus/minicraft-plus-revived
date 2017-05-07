package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.resource.Resource;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

public class StoneWallTile extends Tile {
	public StoneWallTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		/*int col0 = Color.get(333, 333, 333, 333);
		int col00 = Color.get(111, 222, 333, 333);
		int col000 = Color.get(111, 333, 333, 333);
		*/
		int col = Color.get(444, 444, 444, 444);
		int col1 = Color.get(111, 333, 444, 444);
		int col2 = Color.get(111, 444, 444, 444);
		/*
		int col2 = Color.get(333, 333, 333, 333);
		int col22 = Color.get(111, 222, 333, 333);
		int col222 = Color.get(111, 333, 333, 333);

		int col3 = Color.get(222, 222, 222, 222);
		int col33 = Color.get(000, 111, 222, 222);
		int col333 = Color.get(000, 222, 222, 222);

		int col4 = Color.get(444, 444, 444, 444);
		int col44 = Color.get(111, 333, 444, 444);
		int col444 = Color.get(111, 444, 444, 444);

		if (level.dirtColor == 322) {

			if (Game.time == 0) {

				int col = col0;
				*/int transitionColor = col1;
				int backColor = col2;
				
				boolean u = level.getTile(x, y - 1) != this;
				boolean d = level.getTile(x, y + 1) != this;
				boolean l = level.getTile(x - 1, y) != this;
				boolean r = level.getTile(x + 1, y) != this;

				boolean ul = level.getTile(x - 1, y - 1) != this;
				boolean dl = level.getTile(x - 1, y + 1) != this;
				boolean ur = level.getTile(x + 1, y - 1) != this;
				boolean dr = level.getTile(x + 1, y + 1) != this;

				if (!u && !l) {
					if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 0, 7 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!u && !r) {
					if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 0, 8 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!d && !l) {
					if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 8, 7 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
				if (!d && !r) {
					if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 8, 8 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
			/*}
			if (Game.time == 1) {

				int col = col1;
				int transitionColor = col11;
				int backColor = col111;

				boolean u = level.getTile(x, y - 1) != this;
				boolean d = level.getTile(x, y + 1) != this;
				boolean l = level.getTile(x - 1, y) != this;
				boolean r = level.getTile(x + 1, y) != this;

				boolean ul = level.getTile(x - 1, y - 1) != this;
				boolean dl = level.getTile(x - 1, y + 1) != this;
				boolean ur = level.getTile(x + 1, y - 1) != this;
				boolean dr = level.getTile(x + 1, y + 1) != this;

				if (!u && !l) {
					if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 0, 7 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!u && !r) {
					if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 0, 8 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!d && !l) {
					if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 8, 7 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
				if (!d && !r) {
					if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 8, 8 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
			}
			if (Game.time == 2) {

				int col = col2;
				int transitionColor = col22;
				int backColor = col222;

				boolean u = level.getTile(x, y - 1) != this;
				boolean d = level.getTile(x, y + 1) != this;
				boolean l = level.getTile(x - 1, y) != this;
				boolean r = level.getTile(x + 1, y) != this;

				boolean ul = level.getTile(x - 1, y - 1) != this;
				boolean dl = level.getTile(x - 1, y + 1) != this;
				boolean ur = level.getTile(x + 1, y - 1) != this;
				boolean dr = level.getTile(x + 1, y + 1) != this;

				if (!u && !l) {
					if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 0, 7 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!u && !r) {
					if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 0, 8 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!d && !l) {
					if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 8, 7 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
				if (!d && !r) {
					if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 8, 8 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
			}
			if (Game.time == 3) {

				int col = col3;
				int transitionColor = col33;
				int backColor = col333;

				boolean u = level.getTile(x, y - 1) != this;
				boolean d = level.getTile(x, y + 1) != this;
				boolean l = level.getTile(x - 1, y) != this;
				boolean r = level.getTile(x + 1, y) != this;

				boolean ul = level.getTile(x - 1, y - 1) != this;
				boolean dl = level.getTile(x - 1, y + 1) != this;
				boolean ur = level.getTile(x + 1, y - 1) != this;
				boolean dr = level.getTile(x + 1, y + 1) != this;

				if (!u && !l) {
					if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 0, 7 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!u && !r) {
					if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 0, 8 + 24 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

				if (!d && !l) {
					if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 0, y * 16 + 8, 7 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
				if (!d && !r) {
					if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 7 + 2 * 32, col, 0);
					else screen.render(x * 16 + 8, y * 16 + 8, 8 + 25 * 32, backColor, 3);
				} else
					screen.render(
							x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
			}
		}

		if (level.dirtColor == 222) {
			int col = col4;
			int transitionColor = col44;
			int backColor = col444;

			boolean u = level.getTile(x, y - 1) != this;
			boolean d = level.getTile(x, y + 1) != this;
			boolean l = level.getTile(x - 1, y) != this;
			boolean r = level.getTile(x + 1, y) != this;

			boolean ul = level.getTile(x - 1, y - 1) != this;
			boolean dl = level.getTile(x - 1, y + 1) != this;
			boolean ur = level.getTile(x + 1, y - 1) != this;
			boolean dr = level.getTile(x + 1, y + 1) != this;

			if (!u && !l) {
				if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 7 + 2 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 0, 7 + 24 * 32, backColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

			if (!u && !r) {
				if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 7 + 2 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 0, 8 + 24 * 32, backColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 27 : 26) * 32, transitionColor, 3);

			if (!d && !l) {
				if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 7 + 2 * 32, col, 0);
				else screen.render(x * 16 + 0, y * 16 + 8, 7 + 25 * 32, backColor, 3);
			} else
				screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
			if (!d && !r) {
				if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 7 + 2 * 32, col, 0);
				else screen.render(x * 16 + 8, y * 16 + 8, 8 + 25 * 32, backColor, 3);
			} else
				screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 25 : 26) * 32, transitionColor, 3);
		}*/
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int playDmg;
		if (ModeMenu.creative) playDmg = random.nextInt(5);
		else {
			playDmg = 0;
		}
		hurt(level, x, y, playDmg);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}
			}
		}
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pick) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(6) + (tool.level) * 5 + 5);
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int sbwHealth;
		if (ModeMenu.creative) sbwHealth = 1;
		else {
			sbwHealth = 100;
		}
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
		if (damage >= sbwHealth) {
			int count = random.nextInt(3) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(Resource.sbrick),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			}
			level.setTile(x, y, Tile.sbrick, 0);
		} else {
			level.setData(x, y, damage);
		}
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
