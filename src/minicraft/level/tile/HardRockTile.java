package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class HardRockTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(HardRockTile.class, new Sprite(4, 0, 3, 3, Color.get(001, 334, 445, 321), 3), new Sprite(7, 0, 2, 2, Color.get(001, 334, 445, 321), 3), ConnectorSprite.makeSprite(2, 2, Color.get(445, 334, 223, 223), 0, false, 0, 1, 2, 0));
	
	protected HardRockTile(String name) {
		super(name, sprite);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (Game.isMode("creative")) return true;
			if (tool.type == ToolType.Pickaxe && tool.level == 4) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}
			}
			else Game.notifications.add("Gem Pickaxe Required.");
		}
		return Game.isMode("creative");
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int hrHealth = 200;
		if (Game.isMode("creative")) dmg = damage = hrHealth;
		level.add(new SmashParticle(x * 16, y * 16));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= hrHealth) {
			level.dropItem(x*16+8, y*16+8, 1, 3, Items.get("Stone"));
			level.dropItem(x*16+8, y*16+8, 0, 1, Items.get("coal"));
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
