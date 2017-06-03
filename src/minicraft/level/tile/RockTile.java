package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

/// this is the typical stone you see underground and on the surface, that gives coal.

public class RockTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(RockTile.class, new Sprite(4, 0, 3, 3, Color.get(111, 444, 555, 321), 3), new Sprite(7, 0, 2, 2, Color.get(111, 444, 555, 321), 3), Sprite.dots(Color.get(444, 444, 333, 333)));
	
	private int coallvl = 0;
	
	protected RockTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		sprite.sides.color = Color.get(111, 444, 555, DirtTile.dCol(level.depth));
		sprite.sparse.color = Color.get(111, 444, 555, DirtTile.dCol(level.depth));
		sprite.render(screen, level, x, y);
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
				coallvl = 1;
				hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
				return true;
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int rockHealth = 50;
		if (ModeMenu.creative) {
			dmg = damage = rockHealth;
			coallvl = 1;
		}
		level.add(new SmashParticle(x * 16, y * 16));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500)));
		if (damage >= rockHealth) {
			int count = random.nextInt(1) + 0;
			if (coallvl == 0) {
				level.dropItem(x*16+8, y*16+8, 1, 4, Items.get("Stone"));
			}
			if (coallvl == 1) {
				level.dropItem(x*16+8, y*16+8, 1, 2, Items.get("Stone"));
				level.dropItem(x*16+8, y*16+8, 1, 3, Items.get("coal"));
			}
			level.setTile(x, y, Tiles.get("dirt"));
		} else {
			level.setData(x, y, damage);
		}
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
