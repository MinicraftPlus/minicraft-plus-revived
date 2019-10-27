package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class CloudCactusTile extends Tile {
	private static Sprite sprite = new Sprite(6, 2, 2, 2, 1);
	
	protected CloudCactusTile(String name) {
		super(name, sprite);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, 0);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("creative"))
			return false; // go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe) {
				if (player.payStamina(6 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, 1);
					return true;
				}
			}
		}
		return false;
	}
	
	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int health = 10;
		if(Game.isMode("creative")) dmg = damage = health;
		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= health) {
			level.setTile(x, y, Tiles.get("cloud"));
		} else
			level.setData(x, y, damage);
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (entity instanceof AirWizard) return;
		
		if(entity instanceof Mob)
			((Mob)entity).hurt(this, x, y, 1+Settings.getIdx("diff"));
	}
}
