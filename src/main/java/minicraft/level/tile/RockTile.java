package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
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

/// this is the typical stone you see underground and on the surface, that gives coal.

public class RockTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(RockTile.class, new Sprite(18, 6, 3, 3, 1, 3), new Sprite(21, 8, 2, 2, 1, 3), new Sprite(21, 6, 2, 2, 1, 3));
	
	private int coalLvl = 0;
	
	protected RockTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		sprite.sparse.color = DirtTile.dCol(level.depth);
		sprite.render(screen, level, x, y);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}
	
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 1);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		// creative mode can just act like survival here
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe && player.payStamina(4 - tool.level) && tool.payDurability()) {
				coalLvl = 1;
				hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
				return true;
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int rockHealth = 50;
		if (Game.isMode("creative")) {
			dmg = damage = rockHealth;
			coalLvl = 1;
		}
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= rockHealth) {
			int count = random.nextInt(1) + 0;
			if (coalLvl == 0) {
				level.dropItem(x*16+8, y*16+8, 1, 4, Items.get("Stone"));
			}
			if (coalLvl == 1) {
				level.dropItem(x*16+8, y*16+8, 1, 2, Items.get("Stone"));
				int mincoal = 0, maxcoal = 1;
				if(!Settings.get("diff").equals("Hard")) {
					mincoal++;
					maxcoal++;
				}
				level.dropItem(x*16+8, y*16+8, mincoal, maxcoal, Items.get("coal"));
			}
			level.setTile(x, y, Tiles.get("dirt"));
		} else {
			level.setData(x, y, damage);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
