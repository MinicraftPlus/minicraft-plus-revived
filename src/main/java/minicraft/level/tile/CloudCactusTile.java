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
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class CloudCactusTile extends Tile {
	private static Sprite sprite = new Sprite(6, 2, 2, 2, 1);
	
	protected CloudCactusTile(String name) {
		super(name, sprite);
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Cloud").render(screen, level, x, y);
		sprite.render(screen, x << 4, y << 4);
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
			return false; // Go directly to hurt method
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
		int oreH = random.nextInt(10) + 3;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		int health = 25;
		if (Game.isMode("creative")) dmg = damage = health;
		if (dmg > 0) {
			int count = random.nextInt(1) + 0;
			if (damage >= oreH) {
				level.setTile(x, y, Tiles.get("Cloud"));
				count += 2;
			} else {
				level.setData(x, y, damage);
			}
			level.dropItem(x * 16 + 8, y * 16 + 8, count, Items.get("Cloud Ore"));
		}
	}
}
