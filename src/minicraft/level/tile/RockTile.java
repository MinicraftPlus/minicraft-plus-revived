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

/// this is the typical stone you see underground and on the surface, that gives coal.
public class RockTile extends Tile {
	public RockTile(int id) {
		super(id);
	}

	int coallvl = 1;

	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(444, 444, 333, 333);
		int colt = Color.get(111, 444, 555, DirtTile.dCol(level.depth));
		
		int transitionColor = colt;

		boolean u = level.getTile(x, y - 1) != this;
		boolean d = level.getTile(x, y + 1) != this;
		boolean l = level.getTile(x - 1, y) != this;
		boolean r = level.getTile(x + 1, y) != this;

		boolean ul = level.getTile(x - 1, y - 1) != this;
		boolean dl = level.getTile(x - 1, y + 1) != this;
		boolean ur = level.getTile(x + 1, y - 1) != this;
		boolean dr = level.getTile(x + 1, y + 1) != this;

		if (!u && !l) {
			if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			else screen.render(x * 16 + 0, y * 16 + 0, 7 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!u && !r) {
			if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			else screen.render(x * 16 + 8, y * 16 + 0, 8 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!d && !l) {
			if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			else screen.render(x * 16 + 0, y * 16 + 8, 7 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
		if (!d && !r) {
			if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			else screen.render(x * 16 + 8, y * 16 + 8, 8 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		hurt(level, x, y, 1);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe && player.payStamina(4 - tool.level)) {
				hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
				coallvl = 0;
				return true;
			}
		}
		/*if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pick) {
				if (player.payStamina(3 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(7) + (tool.level) * 5 + 10);
					coallvl = 1;
					return true;
				}
			}
		}*/
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int rockHealth;
		if (ModeMenu.creative) {
			rockHealth = 1;
			coallvl = 0;
		}
		else {
			rockHealth = 50;
		}
		int damage = level.getData(x, y) + dmg;
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500, 500, 500)));
		if (damage >= rockHealth) {
			int count = random.nextInt(1);
			if (coallvl == 0) {
				count = random.nextInt(4) + 1;
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.stone),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
			}
			if (coallvl == 0) {
				count = random.nextInt(3);
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.coal),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
			}
			if (coallvl == 1) {
				count = random.nextInt(2) + 1;
				for (int i = 0; i < count; i++) {
					level.add(
							new ItemEntity(
									new ResourceItem(Resource.stone),
									x * 16 + random.nextInt(10) + 3,
									y * 16 + random.nextInt(10) + 3));
				}
			}
			level.setTile(x, y, Tile.dirt, 0);
		} else {
			level.setData(x, y, damage);
		}
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
