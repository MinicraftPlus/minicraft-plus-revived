package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.item.StackableItem;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

public class CactusTile extends Tile {
	private static Sprite sprite = new Sprite(8, 2, 2, 2, Color.get(30, 40, 50, 550));
	
	protected static void addInstances() {
		Tiles.add(new CactusTile("Cactus"));
	}
	
	private CactusTile(String name) {
		super(name, sprite);
		connectsToSand = true;
	}
	
	//public static int col = Color.get(30, 40, 50, 550);
	/*
	public void render(Screen screen, Level level, int x, int y) {
		sprite.render(screen, x * 16, y * 16);
	}
	*/
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int damage = level.getData(x, y) + dmg;
		int cHealth;
		if (ModeMenu.creative) cHealth = 1;
		else cHealth = 10;
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500)));

		if (damage >= cHealth) {
			//int count = random.nextInt(2) + 2;
			level.dropItem(2, 4, Items.get("Cactus"));
			level.setTile(x, y, Tiles.get("sand"), 0);
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
