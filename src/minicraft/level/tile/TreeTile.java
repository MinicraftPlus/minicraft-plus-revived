package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

public class TreeTile extends Tile {
	//private ConnectorSprite sprite = new ConnectorSprite();
	
	protected TreeTile(String name) {
		super(name, (ConnectorSprite)null);
		connectsToGrass = true;
	}
	
	public static int col = Color.get(10, 30, 151, 141);
	public static int col1 = Color.get(10, 30, 430, 141);
	public static int col2 = Color.get(10, 30, 320, 141);
	
	public void render(Screen screen, Level level, int x, int y) {
		int barkCol1 = col1;
		int barkCol2 = col2;
		
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
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		/// make arrow fly through trees!
		if(Game.debug && e instanceof minicraft.entity.Arrow && ((minicraft.entity.Arrow)e).owner instanceof Player) {
			hurt(level, x, y, 25);
			return true;
		}
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		hurt(level, x, y, dmg);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Axe) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		if(random.nextInt(100) == 0)
			level.dropItem(x*16+8, y*16+8, Items.get("Apple"));
		
		int damage = level.getData(x, y) + dmg;
		int treeHealth = 20;
		if (ModeMenu.creative) dmg = damage = treeHealth;
		
		level.add(new SmashParticle(x*16, y*16));
		level.add(new TextParticle("" + dmg, x*16+8, y*16+8, Color.get(-1, 500)));
		if (damage >= treeHealth) {
			level.dropItem(x*16+8, y*16+8, 1, 2, Items.get("Wood"));
			level.dropItem(x*16+8, y*16+8, 1, 4, Items.get("Acorn"));
			level.setTile(x, y, Tiles.get("grass"));
		} else {
			level.setData(x, y, damage);
		}
	}
}
